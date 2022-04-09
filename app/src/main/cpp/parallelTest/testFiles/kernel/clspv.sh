/shared/clspv/build/bin/clspv --cl-std=CL2.0 --inline-entry-points store_buffer.cl -o store_buffer.spv
/usr/bin/spirv-opt --strip-reflect store_buffer.spv -o parallel_store_buffer.spv