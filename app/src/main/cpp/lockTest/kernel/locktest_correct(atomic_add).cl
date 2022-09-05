static void lock(__global atomic_uint* m) {
  uint expected = 0;
  while	(atomic_compare_exchange_strong_explicit(m, &expected, 1, memory_order_relaxed, memory_order_relaxed)) {
    expected = 0;
  }
}

static void unlock(__global atomic_uint* m) {
  atomic_store_explicit(m, 0, memory_order_relaxed);
}

__kernel void litmus_test(
  __global atomic_uint* test_locations,
  __global uint* read_results,
  __global uint* test_iterations) {
  if (get_local_id(0) == 0) {
    for(uint i = 0; i < test_iterations[0]; i++) {
      lock(&test_locations[0]);
      uint unused = atomic_add(&read_results[0], 1);
      unlock(&test_locations[0]);
    }
  }
}
