/shared/clspv/build/bin/clspv --cl-std=CL2.0 --inline-entry-points load_buffer.cl -o load_buffer.spv
/usr/bin/spirv-opt --strip-reflect load_buffer.spv -o load_buffer.spv