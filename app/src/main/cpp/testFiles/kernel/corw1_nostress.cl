__kernel void litmus_test(
  __global atomic_uint* test_data,
  __global uint* mem_locations,
  __global atomic_uint* results) {

    const uint x = mem_locations[0];
    uint r0 = atomic_load_explicit(&test_data[x], memory_order_relaxed);
    atomic_store_explicit(&test_data[x], 1, memory_order_relaxed);
    atomic_store_explicit(&results[0], r0, memory_order_seq_cst);
}
