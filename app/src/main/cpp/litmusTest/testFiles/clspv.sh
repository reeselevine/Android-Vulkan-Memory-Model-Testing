
kernelList=("2+2-write-coherency-results" "2+2-write-coherency"
            "load-buffer-coherency-results" "load-buffer-coherency"
            "message-passing-coherency-results" "message-passing-coherency"
            "read-coherency-results" "read-coherency"
            "store-buffer-coherency-results" "store-buffer-coherency"
            "store-coherency-results" "store-coherency")

for i in "${kernelList[@]}"
do
    /shared/clspv/build/bin/clspv --cl-std=CL2.0 --inline-entry-points $i.cl -o $i.spv
    /usr/bin/spirv-opt --strip-reflect $i.spv -o $i.spv
done
