#include <map>
#include <set>
#include <iostream>
#include <string>
#include <sstream>
#include <fstream>
#include <chrono>
#include <jni.h>
#include <android/log.h>
#include "../easyvk/easyvk.h"

#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))

using namespace std;
using namespace easyvk;

const int size = 4;
constexpr char *TAG = "Main";

bool tuningMode = false;

/** Returns the GPU to use for this test run. Users can specify the specific GPU to use
 *  with the 'gpuDeviceId' parameter. If gpuDeviceId is not included in the parameters or the specified
 *  device cannot be found, the first device is used.
 */
Device getDevice(Instance &instance, map<string, int> params, ofstream &outputFile) {
    int idx = 0;
    if (params.find("gpuDeviceId") != params.end()) {
        int j = 0;
        for (Device _device : instance.devices()) {
            if (_device.properties().deviceID == params["gpuDeviceId"]) {
                idx = j;
                _device.teardown();
                break;
            }
            j++;
            _device.teardown();
        }
    }
    Device device = instance.devices().at(idx);
    if(!tuningMode) {
        outputFile << "Using device " << device.properties().deviceName << "\n";
        outputFile << "\n";
    }
    return device;
}

/** Zeroes out the specified buffer. */
void clearMemory(Buffer &gpuMem, int size) {
    for (int i = 0; i < size; i++) {
        gpuMem.store(i, 0);
    }
}

/** Checks whether a random value is less than a given percentage. Used for parameters like memory stress that should only
 *  apply some percentage of iterations.
 */
bool percentageCheck(int percentage) {
    return rand() % 100 < percentage;
}

/** Assigns shuffled workgroup ids, using the shufflePct to determine whether the ids should be shuffled this iteration. */
void setShuffledWorkgroups(Buffer &shuffledWorkgroups, int numWorkgroups, int shufflePct) {
    for (int i = 0; i < numWorkgroups; i++) {
        shuffledWorkgroups.store(i, i);
    }
    if (percentageCheck(shufflePct)) {
        for (int i = numWorkgroups - 1; i > 0; i--) {
            int swap = rand() % (i + 1);
            int temp = shuffledWorkgroups.load(i);
            shuffledWorkgroups.store(i, shuffledWorkgroups.load(swap));
            shuffledWorkgroups.store(swap, temp);
        }
    }
}

/** Sets the stress regions and the location in each region to be stressed. Uses the stress assignment strategy to assign
  * workgroups to specific stress locations. Assignment strategy 0 corresponds to a "round-robin" assignment where consecutive
  * threads access separate scratch locations, while assignment strategy 1 corresponds to a "chunking" assignment where a group
  * of consecutive threads access the same location.
  */
void setScratchLocations(Buffer &locations, int numWorkgroups, map<string, int> params) {
    set <int> usedRegions;
    int numRegions = params["scratchMemorySize"] / params["stressLineSize"];
    for (int i = 0; i < params["stressTargetLines"]; i++) {
        int region = rand() % numRegions;
        while(usedRegions.count(region))
            region = rand() % numRegions;
        int locInRegion = rand() % (params["stressLineSize"]);
        switch (params["stressAssignmentStrategy"]) {
            case 0:
                for (int j = i; j < numWorkgroups; j += params["stressTargetLines"]) {
                    locations.store(j, (region * params["stressLineSize"]) + locInRegion);
                }
                break;
            case 1:
                int workgroupsPerLocation = numWorkgroups/params["stressTargetLines"];
                for (int j = 0; j < workgroupsPerLocation; j++) {
                    locations.store(i*workgroupsPerLocation + j, (region * params["stressLineSize"]) + locInRegion);
                }
                if (i == params["stressTargetLines"] - 1 && numWorkgroups % params["stressTargetLines"] != 0) {
                    for (int j = 0; j < numWorkgroups % params["stressTargetLines"]; j++) {
                        locations.store(numWorkgroups - j - 1, (region * params["stressLineSize"]) + locInRegion);
                    }
                }
                break;
        }
    }
}

/** These parameters vary per iteration, based on a given percentage. */
void setDynamicStressParams(Buffer &stressParams, map<string, int> params) {
    if (percentageCheck(params["barrierPct"])) {
        stressParams.store(0, 1);
    } else {
        stressParams.store(0, 0);
    }
    if (percentageCheck(params["memStressPct"])) {
        stressParams.store(1, 1);
    } else {
        stressParams.store(1, 0);
    }
    if (percentageCheck(params["preStressPct"])) {
        stressParams.store(4, 1);
    } else {
        stressParams.store(4, 0);
    }
}

/** These parameters are static for all iterations of the test. Aliased memory is used for coherence tests. */
void setStaticStressParams(Buffer &stressParams, map<string, int> params) {
    stressParams.store(2, params["memStressIterations"]);
    stressParams.store(3, params["memStressPattern"]);
    stressParams.store(5, params["preStressIterations"]);
    stressParams.store(6, params["preStressPattern"]);
    stressParams.store(7, params["permuteFirst"]);
    stressParams.store(8, params["permuteSecond"]);
    stressParams.store(9, params["testingWorkgroups"]);
    stressParams.store(10, params["memStride"]);
    if (params["aliasedMemory"] == 1) {
        stressParams.store(11, 0);
    } else {
        stressParams.store(11, params["memStride"]);
    }
}

/** Returns a value between the min and max. */
int setBetween(int min, int max) {
    if (min == max) {
        return min;
    } else {
        int size = rand() % (max - min);
        return min + size;
    }
}

/** A test consists of N iterations of a shader and its corresponding result shader. */
void run(string &shader_file, string &result_shader_file, map<string, int> params, ofstream &outputFile)
{
    // initialize settings
    auto instance = Instance(false);
    auto device = getDevice(instance, params, outputFile);
    int workgroupSize = setBetween(params["minWorkgroupSize"], params["maxWorkgroupSize"]);
    int testingThreads = workgroupSize * params["testingWorkgroups"];
    int testLocSize = testingThreads * params["numMemLocations"] * params["memStride"];

    int numSeq = 0;
    int numInter = 0;
    int numWeak = 0;

    // set up buffers
    auto testLocations = Buffer(device, testLocSize);
    auto readResults = Buffer(device, params["numOutputs"] * testingThreads);
    auto testResults = Buffer(device, 4);
    auto shuffledWorkgroups = Buffer(device, params["maxWorkgroups"]);
    auto barrier = Buffer(device, 1);
    auto scratchpad = Buffer(device, params["scratchMemorySize"]);
    auto scratchLocations = Buffer(device, params["maxWorkgroups"]);
    auto stressParams = Buffer(device, 12);
    setStaticStressParams(stressParams, params);
    vector<Buffer> buffers = {testLocations, readResults, shuffledWorkgroups, barrier, scratchpad, scratchLocations, stressParams};
    vector<Buffer> resultBuffers = {testLocations, readResults, testResults, stressParams};

    // run iterations
    chrono::time_point<std::chrono::system_clock> start, end, itStart, itEnd;
    start = chrono::system_clock::now();
    for (int i = 0; i < params["testIterations"]; i++) {
        auto program = Program(device, shader_file.c_str(), buffers);
        auto resultProgram = Program(device, result_shader_file.c_str(), resultBuffers);
        int numWorkgroups = setBetween(params["testingWorkgroups"], params["maxWorkgroups"]);
        clearMemory(testLocations, testLocSize);
        clearMemory(testResults, 4);
        clearMemory(barrier, 1);
        clearMemory(scratchpad, params["scratchMemorySize"]);
        setShuffledWorkgroups(shuffledWorkgroups, numWorkgroups, params["shufflePct"]);
        setScratchLocations(scratchLocations, numWorkgroups, params);
        setDynamicStressParams(stressParams, params);
        program.setWorkgroups(numWorkgroups);
        resultProgram.setWorkgroups(params["testingWorkgroups"]);
        program.setWorkgroupSize(workgroupSize);
        resultProgram.setWorkgroupSize(workgroupSize);
        program.prepare();

        itStart = chrono::system_clock::now();
        program.run();
        itEnd = chrono::system_clock::now();

        resultProgram.prepare();
        resultProgram.run();

        if(!tuningMode) {
            outputFile << "Iteration " << i << "\n";
            outputFile << "seq: " << testResults.load(0) + testResults.load(1) << "\n";
            outputFile << "interleaved: " << testResults.load(2) << "\n";
            outputFile << "weak: " << testResults.load(3) << "\n";

            std::chrono::duration<double> itDuration = itEnd - itStart;
            outputFile << "durationSeconds: " << itDuration.count() << "s\n";
            outputFile << "\n";
        }

        numSeq += testResults.load(0) + testResults.load(1);
        numInter += testResults.load(2);
        numWeak += testResults.load(3);

        program.teardown();
        resultProgram.teardown();
    }

    outputFile << "Total Result:\n";
    outputFile << "seq: " << numSeq << "\n";
    outputFile << "interleaved: " << numInter << "\n";
    outputFile << "weak: " << numWeak << "\n";

    end = std::chrono::system_clock::now();
    std::chrono::duration<double> elapsed_seconds = end - start;
    outputFile << "Total elapsed time: " << elapsed_seconds.count() << "s\n";

    for (Buffer buffer : buffers) {
        buffer.teardown();
    }
    testResults.teardown();
    device.teardown();
    instance.teardown();
}

/** Reads a specified config file and stores the parameters in a map. Parameters should be of the form "key=value", one per line. */
map<string, int> read_config(string &config_file)
{
    map<string, int> m;
    ifstream in_file(config_file);
    string line;
    while (getline(in_file, line))
    {
        istringstream is_line(line);
        string key;
        if (getline(is_line, key, '='))
        {
            string value;
            if (getline(is_line, value))
            {
                m[key] = stoi(value);
            }
        }
    }
    return m;
}

std::string readOutput(std::string filePath) {
    std::ifstream ifs(filePath);
    std::stringstream ss;

    ss << ifs.rdbuf();

    return ss.str();
}

int runTest(string testName, string shaderFile, string resultShaderFile, string configFile, string filePath)
{
    std::ofstream outputFile(filePath + "/" + testName + "_output.txt");

    if(!tuningMode) {
        outputFile << "Test Name: " << testName << "\n";
        outputFile << "\n";
        outputFile << "Shader Name: " << shaderFile << "\n";
        outputFile << "\n";
    }

    configFile = filePath + "/" + configFile + ".txt";
    shaderFile = filePath + "/" + shaderFile + ".spv";
    resultShaderFile = filePath + "/" + resultShaderFile + ".spv";

    LOGD("%s", configFile.c_str());
    LOGD("%s", shaderFile.c_str());
    LOGD("%s", resultShaderFile.c_str());

    srand(time(NULL));
    map<string, int> params = read_config(configFile);

    if(!tuningMode) {
        outputFile << "Parameter:\n";

        for (const auto& [key, value] : params) {
            outputFile << key << " = " << value << ";\n";
        }
        outputFile << "\n";
    }

    try{
        run(shaderFile, resultShaderFile, params, outputFile);
    }
    catch (const std::runtime_error& e) {
        outputFile << e.what() << "\n";
        outputFile.close();
        return 1;
    }
    outputFile.close();

    LOGD(
            "%s/%s:\n%s_output.txt",
            filePath.c_str(),
            testName.c_str(),
            readOutput(filePath + "/" + testName + "_output.txt").c_str());

    return 0;
}

std::string getFileDirFromJava(JNIEnv *env, jobject obj) {
    jclass clazz = env->GetObjectClass(obj);
    jmethodID method = env->GetMethodID(clazz, "getFileDir", "()Ljava/lang/String;");
    jobject ret = env->CallObjectMethod(obj, method);

    jstring jFilePath = (jstring) ret;

    return std::string(env->GetStringUTFChars(jFilePath, nullptr));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_litmustestandroid_MainActivity_main(
        JNIEnv* env,
        jobject obj,
        jobjectArray testArray,
        jboolean tuningModeEnabled) {

    // Convert string array to individual string
    jstring jTestName = (jstring) (env)->GetObjectArrayElement(testArray, 0);
    jstring jShaderFile = (jstring) (env)->GetObjectArrayElement(testArray, 1);
    jstring jResultShaderFile = (jstring) (env)->GetObjectArrayElement(testArray, 2);
    jstring jConfigFile = (jstring) (env)->GetObjectArrayElement(testArray, 3);

    const char* testConvertedValue = (env)->GetStringUTFChars(jTestName, 0);
    std::string testName = testConvertedValue;

    const char* shaderConvertedValue = (env)->GetStringUTFChars(jShaderFile, 0);
    std::string shaderFile = shaderConvertedValue;

    const char* resultConvertedValue = (env)->GetStringUTFChars(jResultShaderFile, 0);
    std::string resultShaderFile = resultConvertedValue;

    const char* configConvertedValue = (env)->GetStringUTFChars(jConfigFile, 0);
    std::string configFile = configConvertedValue;

    LOGD("Get file path via JNI");
    std::string filePath = getFileDirFromJava(env, obj);

    tuningMode = tuningModeEnabled;

    runTest(testName, shaderFile, resultShaderFile, configFile, filePath);
    return 0;
}


