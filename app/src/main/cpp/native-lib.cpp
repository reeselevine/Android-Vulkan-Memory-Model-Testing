#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_litmustestandroid_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {

    std::string hello = "Hello from C++";

    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_example_litmustestandroid_MainActivity_intFromJNI(
        JNIEnv* env,
        jobject, /* this */
        jobject number) {

    return number + 1;
}