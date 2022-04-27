#include <jni.h>
#include <string>
#include <stdlib.h>
#include <vector>
#include <thread>
#include <iostream>
#include <fstream>
#include <sstream>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))

namespace vect_add {

    const int size = 4;

    constexpr char *TAG = "MainActivityVectAdd";
    constexpr char *SHADER_NAME = "vect_add.spv";
    constexpr char *OUTPUT_NAME = "vect_add_output.txt";

    jint runTest(std::string filePath) {

        auto instance = easyvk::Instance(true);
        auto device = instance.devices().at(0);
        auto a = easyvk::Buffer(device, size);
        auto b = easyvk::Buffer(device, size);
        auto c = easyvk::Buffer(device, size);

        std::ofstream outputFile(filePath + "/" + OUTPUT_NAME);

        outputFile << "BEFORE:\n";
        for (int i = 0; i < size; i++) {
            outputFile << "Storing " << i << " in a[" << i << "]\n";
            a.store(i, i);
            outputFile << "Loading a[" << i << "]:" << a.load(i) << "\n";

            outputFile << "Storing " << i + 1 << " in b[" << i << "]\n";
            b.store(i, i + 1);
            outputFile << "Loading b[" << i << "]:" << b.load(i) << "\n";

            outputFile << "Storing " << 0 << " in c[" << i << "]\n";
            c.store(i, 0);
            outputFile << "Loading c[" << i << "]:" << c.load(i) << "\n";

            outputFile << "\n";
        }

        std::vector<easyvk::Buffer> bufs = {a, b, c};
        std::string testFilePath = filePath + "/" + SHADER_NAME;
        const char *testFile = testFilePath.c_str();

        auto program = easyvk::Program(device, testFile, bufs);
        program.setWorkgroups(size);
        program.setWorkgroupSize(1);
        program.prepare();
        program.run();
        outputFile << "AFTER:\n";
        for (int i = 0; i < size; i++) {
            outputFile << "a[" << i << "]:" << a.load(i) << "\n";
            outputFile << "b[" << i << "]:" << b.load(i) << "\n";
            outputFile << "c[" << i << "]:" << c.load(i) << "\n";
            outputFile << "\n";
        }
        outputFile.close();
        program.teardown();
        a.teardown();
        b.teardown();
        c.teardown();
        device.teardown();
        instance.teardown();
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

