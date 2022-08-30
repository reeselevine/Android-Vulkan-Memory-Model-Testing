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
  __global atomic_uint* read_results) {
  if (get_local_id(0) == 0) {
    lock(&test_locations[0]);

    uint x = atomic_load(&read_results[0]);
    x++;
    atomic_store(&read_results[0], x);

    unlock(&test_locations[0]);
  }
}
