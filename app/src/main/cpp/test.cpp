#include <jni.h>
#include <string>
#include <stdlib.h>
#include <vector>
#include <thread>
#include <iostream>
#include <fstream>
#include "easyvk/easyvk.h"
#include <android/log.h>
#include <unistd.h>

using namespace std;

const int size = 4;

jint main() {
    auto instance = easyvk::Instance(false);
    auto device = instance.devices().at(0);
    auto a = easyvk::Buffer(device, size);

    ofstream outputFile("/data/data/com.example.litmustestandroid/files/output.txt");
    //ofstream outputFile("/sdcard/Download/output.txt");

    outputFile << "BEFORE:\n";
    for (int i = 0; i < size; i++) {
        outputFile << "Storing " << i << " in a[" << i << "]\n";
        a.store(i, i);
        outputFile << "Loading a[" << i << "]:" << a.load(i) << "\n";

        outputFile <<  "\n";
    }

    vector<easyvk::Buffer> bufs = {a};
    const char* testFile = "/data/data/com.example.litmustestandroid/files/kernel_test.spv";
    //const char* testFile = "/sdcard/Download/vect_add.spv";
    auto program = easyvk::Program(device, testFile, bufs);
    program.setWorkgroups(size);
    program.setWorkgroupSize(1);
    program.prepare();
    program.run();
    outputFile << "AFTER:\n";
    for (int i = 0; i < size; i++) {
        outputFile << "a[" << i << "]:" << a.load(i) << "\n";
        outputFile <<  "\n";
        //assert(c.load(i) == a.load(i) + b.load(i));
    }
    outputFile.close();
    program.teardown();
    a.teardown();
    device.teardown();
    instance.teardown();
    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_litmustestandroid_MainActivity_main(
        JNIEnv* env,
        jobject) {

    return (jint) main();
}

