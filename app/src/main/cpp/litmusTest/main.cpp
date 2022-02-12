#include <jni.h>
#include <string>
#include <stdlib.h>
#include <vector>
#include "../easyvk/easyvk.h"
#include "corr.cpp"
#include "corr4.cpp"
#include "corw1.cpp"
#include "corw1_nostress.cpp"
#include "iriw.cpp"
#include "isa2.cpp"
#include "kernel_test.cpp"
#include "load_buffer.cpp"
#include "message_passing.cpp"
#include "store_buffer.cpp"
#include "vect_add.cpp"

void runLitmusTest(JNIEnv* env, jobject obj, std::string testName) {
    if (testName == "kernel_test") {
        kernel_test::readFileFromResRaw(env, obj);
    }
    else if (testName == "vect_add") {
        vect_add::readFileFromResRaw(env, obj);
    }
    else if (testName == "corr") {
        corr::readFileFromResRaw(env, obj);
    }
    else if (testName == "corr4") {
        corr4::readFileFromResRaw(env, obj);
    }
    else if (testName == "corw1") {
        corw1::readFileFromResRaw(env, obj);
    }
    else if (testName == "corw1_nostress") {
        corw1_nostress::readFileFromResRaw(env, obj);
    }
    else if (testName == "iriw") {
        iriw::readFileFromResRaw(env, obj);
    }
    else if (testName == "isa2") {
        isa2::readFileFromResRaw(env, obj);
    }
    else if (testName == "load_buffer") {
        load_buffer::readFileFromResRaw(env, obj);
    }
    else if (testName == "message_passing") {
        message_passing::readFileFromResRaw(env, obj);
    }
    else if (testName == "store_buffer") {
        store_buffer::readFileFromResRaw(env, obj);
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_litmustestandroid_MainActivity_main(
        JNIEnv* env,
        jobject obj,
        jstring testName) {

    jboolean isCopy;
    const char* convertedValue = (env)->GetStringUTFChars(testName, &isCopy);
    std::string testNameStr = convertedValue;

    runLitmusTest(env, obj, testNameStr);
    return 0;
}