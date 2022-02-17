#include <jni.h>
#include <string>
#include <stdlib.h>
#include <vector>
#include <thread>
#include <iostream>
#include <fstream>
#include <sstream>
#include <set>
#include <chrono>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

// Printing log tool for Android Studio's Logcat
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))

// The corw1 litmus test checks SC-per-location by ensuring a read and a write to the same address
// cannot be re-ordered on one thread.

namespace corw1_nostress {

    using namespace std;

    constexpr char *TAG = "MainActivityCorw1NoStress";
    constexpr char *SHADER_NAME = "corw1_nostress.spv";
    constexpr char *OUTPUT_NAME = "corw1_nostress_output.txt";

    const int minWorkgroups = 4;
    const int maxWorkgroups = 36;
    const int minWorkgroupSize = 1;
    const int maxWorkgroupSize = 64;
    const int numMemLocations = 1;
    const int testMemorySize = 1024;
    const int numOutputs = 2048;
    const int memStride = 64;
    const int gpuDeviceId = 7857;
    const char* testName = "corw1_nostress";
    const char* weakBehaviorStr = "r0: 1";
    const int testIterations = 100;
    int weakBehavior = 0;
    int nonWeakBehavior = 0;
    const int sampleInterval = 100;

    class LitmusTester {

    public:
        void run(ofstream &outputFile, string testFile) {
            outputFile << "Starting " << testName << " litmus test run \n";

            // Get instance and device
            auto instance = easyvk::Instance(true);
            auto device = getDevice(&instance, outputFile);

            outputFile << "Weak behavior to watch for: " << weakBehaviorStr << "\n";
            outputFile << "Sampling output approximately every " << sampleInterval
                       << " iterations\n";

            // setup devices, memory, and parameters
            auto testData = easyvk::Buffer(device, testMemorySize);
            auto memLocations = easyvk::Buffer(device, numMemLocations);
            auto results = easyvk::Buffer(device, numOutputs);
            std::vector<easyvk::Buffer> testBuffers = {testData, memLocations, results};

            // Start timer
            std::chrono::time_point<std::chrono::system_clock> start, end;
            start = std::chrono::system_clock::now();

            // Run tests
            for (int i = 0; i < testIterations; i++) {
                // Set up program with shader file
                auto program = easyvk::Program(device, testFile.c_str(), testBuffers);

                // Get number and size of work group
                int numWorkgroups = setNumWorkgroups();
                int workgroupSize = setWorkgroupSize();

                clearMemory(testData, testMemorySize);
                setMemLocations(memLocations);
                clearMemory(results, numOutputs);

                program.setWorkgroups(numWorkgroups);
                program.setWorkgroupSize(workgroupSize);
                program.prepare();
                program.run();

                // Checking result
                checkResult(testData, results, memLocations, outputFile);

                // Destroy program
                program.teardown();
            }
            // End timer
            end = std::chrono::system_clock::now();
            std::chrono::duration<double> elapsed_seconds = end - start;

            // Output time duration
            outputFile << "elapsed time: " << elapsed_seconds.count() << "s\n";
            outputFile << "iterations per second: " << testIterations / elapsed_seconds.count()
                       << " \n";

            // Destroy buffer, device, and instance
            for (easyvk::Buffer buffer : testBuffers) {
                buffer.teardown();
            }
            device.teardown();
            instance.teardown();
        }

        // Go through instance's available devices and determine which device to use
        easyvk::Device getDevice(easyvk::Instance* instance, ofstream &outputFile) {
            int idx = 0;
            if (gpuDeviceId != -1) {
                int j = 0;
                for (easyvk::Device _device : instance->devices()) {
                    if (_device.properties().deviceID == gpuDeviceId) {
                        idx = j;
                        break;
                    }
                    j++;
                    _device.teardown();
                }
            }
            easyvk::Device device = instance->devices().at(idx);
            outputFile << "Using device " << device.properties().deviceName << "\n";
            return device;
        }

        // Checks how many weak and non weak behaviors produced from test
        void checkResult(easyvk::Buffer &testData, easyvk::Buffer &results, easyvk::Buffer &memLocations, ofstream &outputFile) {
            if (rand() % sampleInterval == 1) {
                outputFile << "r0: " << results.load(0) << "\n";
            }
            if (results.load(0) == 1) { // instruction re-ordered, weak behavior
                weakBehavior++;
            } else {
                nonWeakBehavior++;
            }
        }

        void clearMemory(easyvk::Buffer &gpuMem, int size) {
            for (int i = 0; i < size; i++) {
                gpuMem.store(i, 0);
            }
        }

        void setMemLocations(easyvk::Buffer &locations) {
            std::set<int> usedRegions;
            int numRegions = testMemorySize / memStride;
            for (int i = 0; i < numMemLocations; i++) {
                int region = rand() % numRegions;
                while(usedRegions.count(region))
                    region = rand() % numRegions;
                int locInRegion = rand() % (memStride);
                locations.store(i, (region * memStride) + locInRegion);
                usedRegions.insert(region);
            }
        }

        // Set random number of work group size
        int setWorkgroupSize() {
            if (minWorkgroupSize == maxWorkgroupSize) {
                return minWorkgroupSize;
            } else {
                int size = rand() % (maxWorkgroupSize - minWorkgroupSize);
                return minWorkgroupSize + size;
            }
        }

        // Set random number of work group
        int setNumWorkgroups() {
            if (minWorkgroups == maxWorkgroups) {
                return minWorkgroups;
            } else {
                int size = rand() % (maxWorkgroups - minWorkgroups);
                return minWorkgroups + size;
            }
        }
    };

    /*
     * Functions below this point are used for running litmus test on Android App
     */
    jint runTest(std::string filePath) {
        srand (time(NULL));
        LitmusTester app;
        // Get output file and shader file.
        std::ofstream outputFile(filePath + "/" + OUTPUT_NAME);
        std::string testFile = filePath + "/" + SHADER_NAME;

        // Run test
        try {
            app.run(outputFile, testFile);
            // Get weak behavior and non weak behavior
            outputFile << "weak behavior: " << weakBehavior << "\n";
            outputFile << "non weak behavior: " << nonWeakBehavior << "\n";
        }
        catch (const std::runtime_error& e) {
            outputFile << e.what() << "\n";
            outputFile.close();
            return 1;
        }
        outputFile.close();
        return 0;
    }

    std::string getFileDirFromJava(JNIEnv *env, jobject obj);
    std::string readOutput(std::string filePath);

    void readFileFromResRaw(JNIEnv *env, jobject obj) {
        LOGD("Get file path via JNI");
        std::string filePath = getFileDirFromJava(env, obj);

        runTest(filePath);
        LOGD(
                "%s/%s:\n%s",
                filePath.c_str(),
                OUTPUT_NAME,
                readOutput(filePath + "/" + OUTPUT_NAME).c_str());
    }

    std::string getFileDirFromJava(JNIEnv *env, jobject obj) {
        jclass clazz = env->GetObjectClass(obj);
        jmethodID method = env->GetMethodID(clazz, "getFileDir", "()Ljava/lang/String;");
        jobject ret = env->CallObjectMethod(obj, method);

        jstring jFilePath = (jstring) ret;

        return std::string(env->GetStringUTFChars(jFilePath, nullptr));
    }

    std::string readOutput(std::string filePath) {
        std::ifstream ifs(filePath);
        std::stringstream ss;

        ss << ifs.rdbuf();

        return ss.str();
    }
}