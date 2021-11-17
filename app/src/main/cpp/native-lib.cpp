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
    auto b = easyvk::Buffer(device, size);
    auto c = easyvk::Buffer(device, size);

    ofstream outputFile("/data/data/com.example.litmustestandroid/files/output.txt");

    outputFile << "BEFORE:\n";
    for (int i = 0; i < size; i++) {
        outputFile << "Storing " << i << " in a[" << i << "]\n";
        a.store(i, i);
        outputFile << "Loading a[" << i << "]:" << a.load(i) << "\n";

        outputFile << "Storing " << i+1 << " in b[" << i << "]\n";
        b.store(i, i + 1);
        outputFile << "Loading b[" << i << "]:" << b.load(i) << "\n";

        outputFile << "Storing " << 0 << " in c[" << i << "]\n";
        c.store(i, 0);
        outputFile << "Loading c[" << i << "]:" << c.load(i) << "\n";

        outputFile <<  "\n";
    }

    vector<easyvk::Buffer> bufs = {a, b, c};
    const char* testFile = "/data/data/com.example.litmustestandroid/files/vect-add.spv";

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
        outputFile <<  "\n";
        //assert(c.load(i) == a.load(i) + b.load(i));
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

/*jint fileMain() {
    auto file = ifstream("/data/data/com.example.litmustestandroid/files/test.txt");
    if (file.is_open()) {
        return 1;
    }
    else {
        return 2;
    }
}*/

extern "C" JNIEXPORT jint JNICALL
Java_com_example_litmustestandroid_MainActivity_main(
        JNIEnv* env,
        jobject) {

    return (jint) main();
}


