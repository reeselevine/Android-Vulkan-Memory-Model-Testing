#include <cstdio>
#include <cstdlib>
#include <android/log.h>

#define CL_TARGET_OPENCL_VERSION 120
#include <CL/cl.h>

#define BUFFER_SIZE 1024

#define CHECK_CL_ERRCODE(err)                                                  \
    do {                                                                       \
        if (err != CL_SUCCESS) {                                               \
            LOGD("%s:%d error after CL call: %d\n", __FILE__,       \
                    __LINE__, err);                                            \
            return EXIT_FAILURE;                                               \
        }                                                                      \
    } while (0)

#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))

const char *TAG = "CLVK";

const char* program_source = R"(
kernel void test_simple(global int* out)
{
    size_t gid = get_global_id(0);
    out[gid] = gid;
}
)";

int clvkMainFunction() {
    cl_platform_id platform;
    cl_uint num_platforms;

    cl_device_id device;
    cl_uint num_devices;
    cl_int err;

    // Get number of platforms available
    err = clGetPlatformIDs(0, NULL, &num_platforms);
    CHECK_CL_ERRCODE(err);

    // Get the first GPU device of the first platform
    err = clGetPlatformIDs(1, &platform, nullptr);
    CHECK_CL_ERRCODE(err);

    char platform_name[128];
    err = clGetPlatformInfo(platform, CL_PLATFORM_NAME, sizeof(platform_name),
                            platform_name, nullptr);
    CHECK_CL_ERRCODE(err);

    LOGD("Platform: %s\n", platform_name);
    LOGD("Num_platforms: %d\n", num_platforms);

    // Get number of devices available
    err = clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 0, NULL, &num_devices);
    CHECK_CL_ERRCODE(err);

    err = clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 1, &device, nullptr);
    CHECK_CL_ERRCODE(err);

    char device_name[128];
    err = clGetDeviceInfo(device, CL_DEVICE_NAME, sizeof(device_name),
                          device_name, nullptr);
    CHECK_CL_ERRCODE(err);

    LOGD("Device: %s\n", device_name);
    LOGD("Num_devices: %d\n", num_devices);

    auto context = clCreateContext(nullptr, 1, &device, nullptr, nullptr, &err);
    CHECK_CL_ERRCODE(err);

    // Create program
    auto program =
            clCreateProgramWithSource(context, 1, &program_source, nullptr, &err);
    CHECK_CL_ERRCODE(err);

    // Build program
    err = clBuildProgram(program, 1, &device, "-cl-std=CL2.0", nullptr, nullptr);
    if (err == CL_BUILD_PROGRAM_FAILURE) {
        LOGD("CL_BUILD_PROGRAM_FAILURE\n");
    	size_t log_size = 0;
    	auto buildErr = clGetProgramBuildInfo(program, device, CL_PROGRAM_BUILD_LOG, 0, NULL, &log_size);
        CHECK_CL_ERRCODE(buildErr);

    	char *log = (char*) malloc (log_size);

    	buildErr = clGetProgramBuildInfo(program, device, CL_PROGRAM_BUILD_LOG, log_size, log, NULL);
        CHECK_CL_ERRCODE(buildErr);

        LOGD("Build Log: %s\n", log);
    }
    CHECK_CL_ERRCODE(err);

    // Create kernel
    auto kernel = clCreateKernel(program, "test_simple", &err);
    CHECK_CL_ERRCODE(err);

    // Create command queue
    auto queue = clCreateCommandQueue(context, device, 0, &err);
    CHECK_CL_ERRCODE(err);

    // Create buffer
    auto buffer =
            clCreateBuffer(context, CL_MEM_READ_WRITE,
                           BUFFER_SIZE, nullptr, &err);
    CHECK_CL_ERRCODE(err);

    // Set kernel arguments
    err = clSetKernelArg(kernel, 0, sizeof(cl_mem), &buffer);
    CHECK_CL_ERRCODE(err);

    size_t gws = BUFFER_SIZE / sizeof(cl_int);
    size_t lws = 2;

    err = clEnqueueNDRangeKernel(queue, kernel, 1, nullptr, &gws, &lws, 0,
                                 nullptr, nullptr);
    CHECK_CL_ERRCODE(err);

    // Complete execution
    err = clFinish(queue);
    CHECK_CL_ERRCODE(err);

    // Map the buffer
    auto ptr = clEnqueueMapBuffer(queue, buffer, CL_TRUE, CL_MAP_READ, 0,
                                  BUFFER_SIZE, 0, nullptr, nullptr, &err);
    CHECK_CL_ERRCODE(err);

    // Check the expected result
    bool success = true;
    auto buffer_data = static_cast<cl_uint*>(ptr);
    for (cl_uint i = 0; i < BUFFER_SIZE / sizeof(cl_uint); ++i) {
        if (buffer_data[i] != static_cast<cl_uint>(i)) {
            LOGD("Failed comparison at buffer_data[%u]: expected %u but got "
                   "%u\n",
                   i, i, buffer_data[i]);
            success = false;
        }
    }

    // Unmap the buffer
    err = clEnqueueUnmapMemObject(queue, buffer, ptr, 0, nullptr, nullptr);
    CHECK_CL_ERRCODE(err);
    err = clFinish(queue);
    CHECK_CL_ERRCODE(err);

    // Cleanup
    clReleaseMemObject(buffer);
    clReleaseCommandQueue(queue);
    clReleaseKernel(kernel);
    clReleaseProgram(program);
    clReleaseContext(context);

    // Report status
    if (success) {
        LOGD("Buffer content verified, test passed.\n");
        return EXIT_SUCCESS;
    } else {
        LOGD("Test failed.\n");
        return EXIT_FAILURE;
    }
}