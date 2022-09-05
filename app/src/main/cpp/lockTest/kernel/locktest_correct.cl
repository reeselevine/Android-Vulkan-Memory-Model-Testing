static void lock(__global atomic_uint* m) {
  uint e = 0;
  uint acquired = 0;
  while (acquired == 0) {
    acquired = atomic_compare_exchange_strong_explicit(m, &e, 1, memory_order_acquire, memory_order_relaxed);
    e = 0;
  }
}

static void unlock(__global atomic_uint* m) {
  atomic_store_explicit(m, 0, memory_order_release);
}

__kernel void litmus_test(
  __global atomic_uint* test_locations,
  __global uint* read_results,
  __global uint* test_iterations) {
  if (get_local_id(0) == 0) {
    for(uint i = 0; i < test_iterations[0]; i++) {
      lock(&test_locations[0]);

      uint x = read_results[0];
      x++;
      read_results[0] = x;

      unlock(&test_locations[0]);
    }
  }
}
