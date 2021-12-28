#include <vulkan/vulkan.h>
#include <vector>
#include <array>
#include <iostream>
#include "easyvk.h"
#include <fstream>
#include <filesystem>
#include "assert.h"
#include <fstream>
#include <android/log.h>

#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, "EASYVK", __VA_ARGS__))

#define vulkanCheck(result) { vulkanAssert((result), __FILE__, __LINE__); }
inline void vulkanAssert(VkResult result, const char *file, int line, bool abort = true){
	if (result != VK_SUCCESS) {
		std::ofstream outputFile("/data/data/com.example.litmustestandroid/files/output.txt");
		outputFile << "vulkanAssert: ERROR " << result << "\n" << file << "\nline: " << line;
		outputFile.close();
		//LOGD("vulkanAssert: ERROR %d \n %s \n %d", result, file, line);
		assert(0);
	}
}

namespace easyvk {

	static auto VKAPI_ATTR debugReporter(
			VkDebugReportFlagsEXT , VkDebugReportObjectTypeEXT, uint64_t, size_t, int32_t
			, const char*                pLayerPrefix
			, const char*                pMessage
			, void*                      pUserData)-> VkBool32 {
		/*std::ofstream debugFile("/data/data/com.example.litmustestandroid/files/debug.txt");
		debugFile << "[Vulkan]:" << pLayerPrefix << ": " << pMessage << "\n";
		debugFile.close();
		LOGD("[Vulkan]: %s: %s\n", pLayerPrefix, pMessage);*/

	return VK_FALSE;
}

	Instance::Instance(bool _enableValidationLayers) {
		enableValidationLayers = _enableValidationLayers;
		std::vector<const char *> enabledLayers;
		std::vector<const char *> enabledExtensions;
		if (enableValidationLayers) {
			enabledLayers.push_back("VK_LAYER_KHRONOS_validation");
			enabledExtensions.push_back(VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
			//enabledExtensions.push_back("VK_KHR_shader_non_semantic_info");
		}

		VkApplicationInfo appInfo {
		    VK_STRUCTURE_TYPE_APPLICATION_INFO,
		    nullptr,
		    "Litmus Tester",
		    0,
		    "LSD Lab",
		    0,
		    VK_API_VERSION_1_0
		};

		VkInstanceCreateInfo createInfo {
            VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO,
            nullptr,
			VkInstanceCreateFlags {},
            &appInfo,
            (uint32_t)(enabledLayers.size()),
            enabledLayers.data(),
			(uint32_t)(enabledExtensions.size()),
            enabledExtensions.data()
        };

		vulkanCheck(vkCreateInstance(&createInfo, nullptr, &instance));

		if (enableValidationLayers) {
			VkDebugReportCallbackCreateInfoEXT debugCreateInfo {
				VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT,
				nullptr,
				VK_DEBUG_REPORT_ERROR_BIT_EXT | VK_DEBUG_REPORT_WARNING_BIT_EXT
				| VK_DEBUG_REPORT_PERFORMANCE_WARNING_BIT_EXT,
				debugReporter
			};
			//vkCreateDebugReportCallbackEXT(instance, &debugCreateInfo, nullptr, &debugReportCallback);
			auto createFN = PFN_vkCreateDebugReportCallbackEXT(vkGetInstanceProcAddr(instance, "vkCreateDebugReportCallbackEXT"));
			if(createFN) {
				LOGD("EASYVK createFN SUCCESSFUL");
				createFN(instance, &debugCreateInfo, nullptr, &debugReportCallback);
			}
			else {
				LOGD("EASYVK createFN NOT SUCCESSFUL");
			}
		}
	}

	std::vector<easyvk::Device> Instance::devices() {
		uint32_t deviceCount = 0;
		vulkanCheck(vkEnumeratePhysicalDevices(instance, &deviceCount, nullptr));

		std::vector<VkPhysicalDevice> physicalDevices(deviceCount);
		vulkanCheck(vkEnumeratePhysicalDevices(instance, &deviceCount, physicalDevices.data()));

		auto devices = std::vector<easyvk::Device>{};
		for (auto device : physicalDevices) {
			devices.push_back(easyvk::Device(*this, device));
		}
		return devices;
	}

	void Instance::teardown() {
		if (enableValidationLayers) {
			auto destroyFn = PFN_vkDestroyDebugReportCallbackEXT(vkGetInstanceProcAddr(instance,"vkDestroyDebugReportCallbackEXT"));
			if (destroyFn) {
				destroyFn(instance, debugReportCallback, nullptr);
			}
		}
		vkDestroyInstance(instance, nullptr);
	}

	uint32_t getComputeFamilyId(VkPhysicalDevice physicalDevice) {
		uint32_t queueFamilyPropertyCount = 0;
		vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, &queueFamilyPropertyCount, nullptr);

		std::vector<VkQueueFamilyProperties> familyProperties(queueFamilyPropertyCount);
		vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, &queueFamilyPropertyCount, familyProperties.data());

		uint32_t i = 0;
		uint32_t computeFamilyId = -1;
		for (auto queueFamily : familyProperties) {
			if (queueFamily.queueCount > 0 && (queueFamily.queueFlags & VK_QUEUE_COMPUTE_BIT)) {
				computeFamilyId = i;;
				break;
			}
			i++;
		}
		return computeFamilyId;
	}

	Device::Device(easyvk::Instance &_instance, VkPhysicalDevice _physicalDevice) :
		instance(_instance),
		physicalDevice(_physicalDevice),
		computeFamilyId(getComputeFamilyId(_physicalDevice)) {

			auto priority = float(1.0);
			auto queues = std::array<VkDeviceQueueCreateInfo, 1>{};

			queues[0] = VkDeviceQueueCreateInfo {
				VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO,
				nullptr,
				VkDeviceQueueCreateFlags {},
				computeFamilyId,
				1,
				&priority
			};

			VkDeviceCreateInfo deviceCreateInfo{
				VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO,
				nullptr,
				VkDeviceCreateFlags {},
				1,
				queues.data()
			};
			vulkanCheck(vkCreateDevice(physicalDevice, &deviceCreateInfo, nullptr, &device));

			VkCommandPoolCreateInfo commandPoolCreateInfo {
				VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO,
				nullptr,
				VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT,
				computeFamilyId
			};
			vulkanCheck(vkCreateCommandPool(device, &commandPoolCreateInfo, nullptr, &computePool));

			VkCommandBufferAllocateInfo commandBufferAI {
				VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO,
				nullptr,
				computePool,
				VK_COMMAND_BUFFER_LEVEL_PRIMARY,
				1
			};

			vulkanCheck(vkAllocateCommandBuffers(device, &commandBufferAI, &computeCommandBuffer));
		}

	VkPhysicalDeviceProperties Device::properties() {
		VkPhysicalDeviceProperties physicalDeviceProperties;
		vkGetPhysicalDeviceProperties(physicalDevice, &physicalDeviceProperties);
		return physicalDeviceProperties;
	}

	uint32_t Device::selectMemory(VkBuffer buffer, VkMemoryPropertyFlags flags) {
		VkPhysicalDeviceMemoryProperties memProperties;
		vkGetPhysicalDeviceMemoryProperties(physicalDevice, &memProperties);

		VkMemoryRequirements memoryReqs;
		vkGetBufferMemoryRequirements(device, buffer, &memoryReqs);

		for(uint32_t i = 0; i < memProperties.memoryTypeCount; ++i){
			if( (memoryReqs.memoryTypeBits & (1u << i))
				&& ((flags & memProperties.memoryTypes[i].propertyFlags) == flags))
			{
				return i;
			}
		}
		return uint32_t(-1);
	}

	VkQueue Device::computeQueue() {
		VkQueue queue;
		vkGetDeviceQueue(device, computeFamilyId, 0, &queue);
		return queue;
	}

	void Device::teardown() {
		vkDestroyCommandPool(device, computePool, nullptr);
		vkDestroyDevice(device, nullptr);
	}

	VkBuffer getNewBuffer(easyvk::Device &_device, uint32_t size) {
		VkBuffer newBuffer;
		vulkanCheck(vkCreateBuffer(_device.device, new VkBufferCreateInfo {
			VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO,
			nullptr,
			VkBufferCreateFlags {},
			size * sizeof(uint32_t),
			VK_BUFFER_USAGE_STORAGE_BUFFER_BIT }, nullptr, &newBuffer));
		return newBuffer;
	}

	Buffer::Buffer(easyvk::Device &_device, uint32_t size) :
		device(_device),
		buffer(getNewBuffer(_device, size))
			{
	            auto memId = _device.selectMemory(buffer, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);

				VkMemoryRequirements memReqs;
				vkGetBufferMemoryRequirements(device.device, buffer, &memReqs);

				vulkanCheck(vkAllocateMemory(_device.device, new VkMemoryAllocateInfo {
				    VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO,
				    nullptr,
				    memReqs.size,
				    memId}, nullptr, &memory));

				vulkanCheck(vkBindBufferMemory(_device.device, buffer, memory, 0));

                void* newData = new void*;
				vulkanCheck(vkMapMemory(_device.device, memory, 0, VK_WHOLE_SIZE, VkMemoryMapFlags {}, &newData));
				data = (uint32_t*)newData;
			}


	void Buffer::teardown() {
		vkUnmapMemory(device.device, memory);
		vkFreeMemory(device.device, memory, nullptr);
		vkDestroyBuffer(device.device, buffer, nullptr);
	}

	std::vector<uint32_t> read_spirv(const char* filename) {
		auto fin = std::ifstream(filename, std::ios::binary | std::ios::ate);
		if(!fin.is_open()){
			throw std::runtime_error(std::string("failed opening file ") + filename + " for reading");
		}
		const auto stream_size = unsigned(fin.tellg());
		fin.seekg(0);

		auto ret = std::vector<std::uint32_t>((stream_size + 3)/4, 0);
		std::copy( std::istreambuf_iterator<char>(fin), std::istreambuf_iterator<char>()
				   , reinterpret_cast<char*>(ret.data()));
		return ret;
	}

	VkShaderModule initShaderModule(easyvk::Device& device, const char* filepath) {
		std::vector<uint32_t> code = read_spirv(filepath);
		VkShaderModule shaderModule;
		vulkanCheck(vkCreateShaderModule(device.device, new VkShaderModuleCreateInfo {
			VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO,
			nullptr,
			0,
			code.size() * sizeof(uint32_t),
			code.data()
		}, nullptr, &shaderModule));
		return shaderModule;
	}

	VkDescriptorSetLayout createDescriptorSetLayout(easyvk::Device &device, uint32_t size) {
		std::vector<VkDescriptorSetLayoutBinding> layouts;
		for (uint32_t i = 0; i < size; i++) {
			layouts.push_back(VkDescriptorSetLayoutBinding {
				i,
				VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,
				1,
				VK_SHADER_STAGE_COMPUTE_BIT
			});
		}
		VkDescriptorSetLayoutCreateInfo createInfo {
			VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO,
			nullptr,
			VkDescriptorSetLayoutCreateFlags {},
			size,
			layouts.data()
		};
		VkDescriptorSetLayout descriptorSetLayout;
		vulkanCheck(vkCreateDescriptorSetLayout(device.device, &createInfo, nullptr, &descriptorSetLayout));
		return descriptorSetLayout;
	}

	void writeSets(
			VkDescriptorSet& descriptorSet,
			std::vector<easyvk::Buffer> &buffers,
			std::vector<VkWriteDescriptorSet>& writeDescriptorSets,
			std::vector<VkDescriptorBufferInfo>& bufferInfos) {

		for (int i = 0; i < buffers.size(); i++) {
			bufferInfos.push_back(VkDescriptorBufferInfo{
				buffers[i].buffer,
				0,
				VK_WHOLE_SIZE
			});
		}

		// wow this bug sucked: https://medium.com/@arpytoth/the-dangerous-pointer-to-vector-a139cc42a192
		for (int i = 0; i < buffers.size(); i++) {
			writeDescriptorSets.push_back(VkWriteDescriptorSet {
				VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET,
				nullptr,
				descriptorSet,
				(uint32_t)i,
				0,
				1,
				VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,
				nullptr,
				&bufferInfos[i],
				nullptr
			});
		}
	}

	void Program::prepare() {
		VkSpecializationMapEntry specMap[1] = {VkSpecializationMapEntry{0, 0, sizeof(uint32_t)}};
		uint32_t specMapContent[1] = {workgroupSize};
		VkSpecializationInfo specInfo {1, specMap, sizeof(uint32_t), specMapContent};
		VkPipelineShaderStageCreateInfo stageCI{
			VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO,
			nullptr,
			VkPipelineShaderStageCreateFlags {},
			VK_SHADER_STAGE_COMPUTE_BIT,
			shaderModule,
			"litmus_test",
			&specInfo};
		VkComputePipelineCreateInfo pipelineCI{
			VK_STRUCTURE_TYPE_COMPUTE_PIPELINE_CREATE_INFO,
			nullptr,
			{},
			stageCI,
			pipelineLayout
		};

		/*
		* Error: vkCreatecomputePipelines is returning VK_ERROR_INITIALIZATION_FAILED (12/6/21)
		*/

		vulkanCheck(vkCreateComputePipelines(device.device, {}, 1, &pipelineCI, nullptr,  &pipeline));

		vulkanCheck(vkBeginCommandBuffer(device.computeCommandBuffer, new VkCommandBufferBeginInfo {VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO}));
		vkCmdBindPipeline(device.computeCommandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE, pipeline);
		vkCmdBindDescriptorSets(device.computeCommandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE,
						  pipelineLayout, 0, 1, &descriptorSet, 0, 0);
		vkCmdDispatch(device.computeCommandBuffer, numWorkgroups, 1, 1);
		vulkanCheck(vkEndCommandBuffer(device.computeCommandBuffer));
	}

	void Program::run() {
		VkSubmitInfo submitInfo {
			VK_STRUCTURE_TYPE_SUBMIT_INFO,
			nullptr,
			0,
			nullptr,
			nullptr,
			1,
			&device.computeCommandBuffer
		};

		auto queue = device.computeQueue();
		vulkanCheck(vkQueueSubmit(queue, 1, &submitInfo, VK_NULL_HANDLE));
		vulkanCheck(vkQueueWaitIdle(queue));
	}

	void Program::setWorkgroups(uint32_t _numWorkgroups) {
		numWorkgroups = _numWorkgroups;
	}

	void Program::setWorkgroupSize(uint32_t _workgroupSize) {
		workgroupSize = _workgroupSize;
	}

	Program::Program(easyvk::Device &_device, const char* filepath, std::vector<easyvk::Buffer> &_buffers) :
		device(_device),
		shaderModule(initShaderModule(_device, filepath)),
		buffers(_buffers) {
			descriptorSetLayout = createDescriptorSetLayout(device, buffers.size());
			VkPipelineLayoutCreateInfo createInfo {
				VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO,
				nullptr,
				VkPipelineLayoutCreateFlags {},
				1,
				&descriptorSetLayout
			};
			vulkanCheck(vkCreatePipelineLayout(device.device, &createInfo, nullptr, &pipelineLayout));
			VkDescriptorPoolSize poolSize {
				VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,
				(uint32_t)buffers.size()
			};
			auto descriptorSizes = std::array<VkDescriptorPoolSize, 1>({poolSize});

			vulkanCheck(vkCreateDescriptorPool(device.device, new VkDescriptorPoolCreateInfo {
				VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO,
				nullptr,
				VkDescriptorPoolCreateFlags {},
				1,
				uint32_t(descriptorSizes.size()),
				descriptorSizes.data()}, nullptr, &descriptorPool));

			vulkanCheck(vkAllocateDescriptorSets(device.device, new VkDescriptorSetAllocateInfo {
				VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO,
				nullptr,
				descriptorPool,
				1,
				&descriptorSetLayout}, &descriptorSet));

			writeSets(descriptorSet, buffers, writeDescriptorSets, bufferInfos);

			vkUpdateDescriptorSets(device.device, writeDescriptorSets.size(), &writeDescriptorSets.front(), 0,{});
		}

	void Program::teardown() {
		vkDestroyShaderModule(device.device, shaderModule, nullptr);
		vkDestroyDescriptorPool(device.device, descriptorPool, nullptr);
		vkDestroyDescriptorSetLayout(device.device, descriptorSetLayout, nullptr);
		vkDestroyPipelineLayout(device.device, pipelineLayout, nullptr);
		vkDestroyPipeline(device.device, pipeline, nullptr);
	}
}
