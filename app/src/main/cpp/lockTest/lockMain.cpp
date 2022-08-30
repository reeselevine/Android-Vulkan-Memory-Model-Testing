#include <map>
#include <set>
#include <iostream>
#include <string>
#include <sstream>
#include <fstream>
#include <chrono>
#include <unistd.h>
#include <jni.h>
#include <android/log.h>
#include "../easyvk/easyvk.h"

#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))

using namespace std;
using namespace easyvk;

constexpr char *TAG = "LockTest";
const int TEST_ITERATIONS = 1000;
const int WORKGROUP_SIZE = 16;
const int WORKGROUP_NUM = 16;

bool checkCorrect = false;

Device getDevice(Instance &instance, ofstream &outputFile) {
    int idx = 0;
    Device device = instance.devices().at(idx);
    outputFile << "Using device " << device.properties().deviceName << "\n";
    outputFile << "\n";
    return device;
}

void clearMemory(Buffer &gpuMem, int size) {
    for (int i = 0; i < size; i++) {
        gpuMem.store(i, 0);
    }
}

void run(JNIEnv* env, jobject obj, string &shader_file, ofstream &outputFile) {
    bool allTestsCorrect = true;

    // initialize settings
    auto instance = Instance(true);
    auto device = getDevice(instance, outputFile);

    // set up buffers
    auto testLocations = Buffer(device, WORKGROUP_NUM * WORKGROUP_SIZE);
    auto readResults = Buffer(device, 1);
    vector<Buffer> buffers = {testLocations, readResults};

    jclass clazz = env->GetObjectClass(obj);
    jmethodID iterationMethod = env->GetMethodID(clazz, "iterationProgress", "(Ljava/lang/String;)V");
    jmethodID deviceMethod = env->GetMethodID(clazz, "setGPUName", "(Ljava/lang/String;)V");

    string gpuStr = device.properties().deviceName;
    const char* gpuChar =  gpuStr.c_str();
    jstring gpuName = env->NewStringUTF(gpuChar);
    env->CallVoidMethod(obj, deviceMethod, gpuName);

    // run iterations
    chrono::time_point<std::chrono::system_clock> itStart, itEnd;
    double total_elapsed = 0;

    for (int i = 0; i < TEST_ITERATIONS; i++)  {
        // Update iteration number
        string iterationStr = to_string(i+1);
        const char* iterationChar = iterationStr.c_str();
        jstring iterationNum = env->NewStringUTF(iterationChar);
        env->CallVoidMethod(obj, iterationMethod, iterationNum);

        auto program = Program(device, shader_file.c_str(), buffers);
        clearMemory(testLocations, WORKGROUP_NUM * WORKGROUP_SIZE);
        clearMemory(readResults, 1);
        program.setWorkgroups(WORKGROUP_NUM);
        program.setWorkgroupSize(WORKGROUP_SIZE);
        program.prepare();

        itStart = chrono::system_clock::now();
        program.run();
        itEnd = chrono::system_clock::now();
        program.teardown();

        std::chrono::duration<double> it_duration = itEnd - itStart;
        total_elapsed += it_duration.count();

        if(checkCorrect) {
            if(readResults.load(0) != 1) {
                if(readResults.load(0) == 0) {
                    outputFile << "Iteration: " << i << ": Untouched output = "
                               << readResults.load(0) << "\n";
                }
                else {
                    outputFile << "Iteration: " << i << ": Wrong output = "
                               << readResults.load(0) << "\n";
                }
                allTestsCorrect = false;
            }
        }
    }
    outputFile << "Total elapsed time: " << total_elapsed << "s\n";
    if(checkCorrect && allTestsCorrect) {
        outputFile << "Test was successful \n";
    }

    for (Buffer buffer : buffers) {
        buffer.teardown();
    }
    device.teardown();
    instance.teardown();
}

std::string readOutput(std::string filePath) {
    std::ifstream ifs(filePath);
    std::stringstream ss;

    ss << ifs.rdbuf();

    return ss.str();
}

int runTest(JNIEnv* env, jobject obj, string testName, string shaderFile, string filePath)
{
    std::ofstream outputFile;
    string outputFilePath = "";
    outputFilePath = filePath + "/" + shaderFile + "_output.txt";
    outputFile.open(outputFilePath);

    outputFile << "Test Name: " << testName << "\n";
    outputFile << "Shader Name: " << shaderFile << "\n";
    outputFile << "\n";

    shaderFile = filePath + "/" + shaderFile + ".spv";

    LOGD("%s", shaderFile.c_str());

    srand(time(NULL));

    try{
        run(env, obj, shaderFile, outputFile);
    }
    catch (const std::runtime_error& e) {
        outputFile << e.what() << "\n";
        outputFile.close();
        return 1;
    }
    outputFile.close();

    LOGD(
            "%s/%s:\n%s",
            filePath.c_str(),
            testName.c_str(),
            readOutput(outputFilePath).c_str());

    return 0;
}

std::string getFileDirFromJava(JNIEnv *env, jobject obj) {
    jclass clazz = env->GetObjectClass(obj);
    jmethodID method = env->GetMethodID(clazz, "getFileDir", "()Ljava/lang/String;");
    jobject ret = env->CallObjectMethod(obj, method);

    jstring jFilePath = (jstring) ret;

    return std::string(env->GetStringUTFChars(jFilePath, nullptr));
}

extern "C" JNIEXPORT jint
Java_com_example_litmustestandroid_LockTestThread_main(
        JNIEnv* env,
        jobject obj,
        jobject mainObj,
        jobjectArray testArray,
        jboolean checkingModeEnabled) {

    // Convert string array to individual string
    jstring jTestName = (jstring) (env)->GetObjectArrayElement(testArray, 0);
    jstring jShaderFile = (jstring) (env)->GetObjectArrayElement(testArray, 1);

    const char* testConvertedValue = (env)->GetStringUTFChars(jTestName, 0);
    std::string testName = testConvertedValue;

    const char* shaderConvertedValue = (env)->GetStringUTFChars(jShaderFile, 0);
    std::string shaderFile = shaderConvertedValue;

    LOGD("Get file path via JNI");
    std::string filePath = getFileDirFromJava(env, mainObj);

    checkCorrect = checkingModeEnabled;

    runTest(env, mainObj, testName, shaderFile, filePath);

    jclass clazz = env->GetObjectClass(mainObj);
    jmethodID completeMethod = env->GetMethodID(clazz, "testComplete", "()V");
    env->CallVoidMethod(mainObj, completeMethod);

    return 0;
}