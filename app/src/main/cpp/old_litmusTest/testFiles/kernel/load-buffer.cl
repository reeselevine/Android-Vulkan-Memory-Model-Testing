

static void spin(__global atomic_uint* barrier) {
  int i = 0;
  uint val = atomic_fetch_add_explicit(barrier, 1, memory_order_relaxed);
  while (i < 1024 && val < 2) {
    val = atomic_load_explicit(barrier, memory_order_relaxed);
    i++;
  }
}

static void do_stress(__global uint* scratchpad, __global uint* scratch_locations, uint iterations, uint pattern) {
  for (uint i = 0; i < iterations; i++) {
    if (pattern == 0) {
      scratchpad[scratch_locations[get_group_id(0)]] = i;
      scratchpad[scratch_locations[get_group_id(0)]] = i + 1;
    } else if (pattern == 1) {
      scratchpad[scratch_locations[get_group_id(0)]] = i;
      uint tmp1 = scratchpad[scratch_locations[get_group_id(0)]];
      if (tmp1 > 100) {
        break;
      }
    } else if (pattern == 2) {
      uint tmp1 = scratchpad[scratch_locations[get_group_id(0)]];
      if (tmp1 > 100) {
        break;
      }
      scratchpad[scratch_locations[get_group_id(0)]] = i;
    } else if (pattern == 3) {
      uint tmp1 = scratchpad[scratch_locations[get_group_id(0)]];
      if (tmp1 > 100) {
        break;
      }
      uint tmp2 = scratchpad[scratch_locations[get_group_id(0)]];
      if (tmp2 > 100) {
        break;
      }
    }
  }
}

__kernel void litmus_test(
  __global atomic_uint* test_data,
  __global uint* mem_locations,
  __global atomic_uint* results,
  __global uint* shuffled_ids,
  __global atomic_uint* barrier,
  __global uint* scratchpad,
  __global uint* scratch_locations,
  __global uint* stress_params) {
  const uint y = mem_locations[0];
  const uint x = mem_locations[1];
  if (shuffled_ids[get_global_id(0)] == get_local_size(0) * 0 + 0) {
    if (stress_params[4]) {
      do_stress(scratchpad, scratch_locations, stress_params[5], stress_params[6]);
    }
    if (stress_params[0]) {
      spin(barrier);
    }
    uint r0 = atomic_load_explicit(&test_data[y], memory_order_relaxed);
    atomic_store_explicit(&test_data[x], 1, memory_order_relaxed);
    atomic_store_explicit(&results[0], r0, memory_order_seq_cst);
  } else if (shuffled_ids[get_global_id(0)] == get_local_size(0) * 1 + 0) {
    if (stress_params[4]) {
      do_stress(scratchpad, scratch_locations, stress_params[5], stress_params[6]);
    }
    if (stress_params[0]) {
      spin(barrier);
    }
    uint r1 = atomic_load_explicit(&test_data[x], memory_order_relaxed);
    atomic_store_explicit(&test_data[y], 1, memory_order_relaxed);
    atomic_store_explicit(&results[1], r1, memory_order_seq_cst);
  } else if (stress_params[1]) {  
    do_stress(scratchpad, scratch_locations, stress_params[2], stress_params[3]);  
  }
}
