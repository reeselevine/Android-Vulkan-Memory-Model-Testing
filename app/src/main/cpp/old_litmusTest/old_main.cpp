#include <jni.h>
#include <string>
#include <stdlib.h>
#include <vector>
#include "../easyvk/easyvk.h"
#include "atomicity.cpp"
#include "corr.cpp"
#include "corr4.cpp"
#include "corw1.cpp"
#include "corw2.cpp"
#include "cowr.cpp"
#include "coww.cpp"
#include "corw1_nostress.cpp"
#include "iriw.cpp"
#include "isa2.cpp"
#include "kernel_test.cpp"
#include "load_buffer.cpp"
#include "message_passing.cpp"
#include "read.cpp"
#include "store.cpp"
#include "store_buffer.cpp"
#include "vect_add.cpp"
#include "write_22.cpp"

#define testCount 1

void runLitmusTest(JNIEnv* env, jobject obj, std::string testName) {
    if (testName == "message_passing") {
        for (int i = 0; i < testCount; i++) {
            message_passing::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "store") {
        for (int i = 0; i < testCount; i++) {
            store::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "read") {
        for (int i = 0; i < testCount; i++) {
            read::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "load_buffer") {
        for (int i = 0; i < testCount; i++) {
            load_buffer::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "store_buffer") {
        for (int i = 0; i < testCount; i++) {
            store_buffer::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "write_22") {
        for (int i = 0; i < testCount; i++) {
            write_22::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "corr") {
        for (int i = 0; i < testCount; i++) {
            corr::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "coww") {
        for (int i = 0; i < testCount; i++) {
            coww::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "cowr") {
        for (int i = 0; i < testCount; i++) {
            cowr::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "corw1") {
        for (int i = 0; i < testCount; i++) {
            corw1::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "corw2") {
        for (int i = 0; i < testCount; i++) {
            corw2::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "atomicity") {
        for (int i = 0; i < testCount; i++) {
            atomicity::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "kernel_test") {
        for (int i = 0; i < testCount; i++) {
            kernel_test::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "vect_add") {
        for (int i = 0; i < testCount; i++) {
            vect_add::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "corr4") {
        for (int i = 0; i < testCount; i++) {
            corr4::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "corw1_nostress") {
        for (int i = 0; i < testCount; i++) {
            corw1_nostress::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "iriw") {
        for (int i = 0; i < testCount; i++) {
            iriw::readFileFromResRaw(env, obj);
        }
    }
    else if (testName == "isa2") {
        for (int i = 0; i < testCount; i++) {
            isa2::readFileFromResRaw(env, obj);
        }
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