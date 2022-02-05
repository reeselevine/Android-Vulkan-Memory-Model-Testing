// Copyright 2018 The clvk authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include <cstdio>
#include <cstdlib>
#include <iostream>

#define CL_TARGET_OPENCL_VERSION 120
#include "CL/cl.h"

#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))

#define BUFFER_SIZE 1024

#define CHECK_CL_ERRCODE(err)                                                  \
    do {                                                                       \
        if (err != CL_SUCCESS) {                                               \
            fprintf(stderr, "%s:%d error after CL call: %d\n", __FILE__,       \
                    __LINE__, err);                                            \
            return EXIT_FAILURE;                                               \
        }                                                                      \
    } while (0)

int clvkPlatformFunction() {
    cl_int err;
    cl_uint num_of_platforms;

    err = clGetPlatformIDs(0, NULL, &num_of_platforms);
    CHECK_CL_ERRCODE(err);

    LOGD("num_of_platforms: %d\n", num_of_platforms);

    return 0;

}