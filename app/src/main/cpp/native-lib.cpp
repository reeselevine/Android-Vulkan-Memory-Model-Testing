#include <jni.h>
#include <string>
#include <stdlib.h>
#include <vector>
#include <thread>
#include <iostream>
#include "easyvk/easyvk.h"

using namespace std;

const int size = 4;

jint main() {
    auto instance = easyvk::Instance(false);
    auto device = instance.devices().at(0);
    auto a = easyvk::Buffer(device, size);
    auto b = easyvk::Buffer(device, size);
    auto c = easyvk::Buffer(device, size);

    for (int i = 0; i < size; i++) {
        a.store(i, i);
        b.store(i, i + 1);
        c.store(i, 0);
    }

    vector<easyvk::Buffer> bufs = {a, b, c};
    const char* testFile = "vect-add.spv";

    auto program = easyvk::Program(device, testFile, bufs);
    program.setWorkgroups(size);
    program.setWorkgroupSize(1);
    program.prepare();
    program.run();
    for (int i = 0; i < size; i++) {
        cout << "c[" << i << "]:" << c.load(i) << "\n";
        assert(c.load(i) == a.load(i) + b.load(i));
    }
    program.teardown();
    a.teardown();
    b.teardown();
    c.teardown();
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


