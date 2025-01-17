package com.example.litmustestandroid.HelperClass;

import java.util.HashMap;
import java.util.Map;

public class ParameterConstants {

    private ParameterConstants(){}

    public static final String ITERATIONS = "iterations";
    public static final String TESTING_WORKGROUPS = "testingWorkgroups";
    public static final String MAX_WORKGROUPS = "maxWorkgroups";
    public static final String WORKGROUP_SIZE = "workgroupSize";
    public static final String SHUFFLE_PCT = "shufflePct";
    public static final String BARRIER_PCT = "barrierPct";
    public static final String NUM_MEM_LOCATIONS = "numMemLocations";
    public static final String NUM_OUTPUTS = "numOutputs";
    public static final String SCRATCH_MEMORY_SIZE = "scratchMemorySize";
    public static final String MEM_STRIDE = "memStride";
    public static final String MEM_STRESS_PCT = "memStressPct";
    public static final String MEM_STRESS_ITERATIONS = "memStressIterations";
    public static final String MEM_STRESS_STORE_FIRST_PCT = "memStressStoreFirstPct";
    public static final String MEM_STRESS_STORE_SECOND_PCT = "memStressStoreSecondPct";
    public static final String PRE_STRESS_PCT = "preStressPct";
    public static final String PRE_STRESS_ITERATIONS = "preStressIterations";
    public static final String PRE_STRESS_STORE_FIRST_PCT = "preStressStoreFirstPct";
    public static final String PRE_STRESS_STORE_SECOND_PCT = "preStressStoreSecondPct";
    public static final String STRESS_LINE_SIZE = "stressLineSize";
    public static final String STRESS_TARGET_LINES = "stressTargetLines";
    public static final String STRESS_STRATEGY_BALANCE_PCT = "stressStrategyBalancePct";
    public static final String PERMUTE_FIRST = "permuteFirst";
    public static final String PERMUTE_SECOND = "permuteSecond";
    public static final String ALIASED_MEMORY = "aliasedMemory";
    public static final String NUM_CONFIGS = "numConfigs";
    public static final String RANDOM_SEED = "randomSeed";

    public static Map<String, Integer> nonOverrideableParams;
    static {
        nonOverrideableParams = new HashMap<>();
        nonOverrideableParams.put(NUM_MEM_LOCATIONS, 2);
        nonOverrideableParams.put(NUM_OUTPUTS, 2);
        nonOverrideableParams.put(PERMUTE_FIRST, 419);
        nonOverrideableParams.put(PERMUTE_SECOND, 1031);
        nonOverrideableParams.put(ALIASED_MEMORY, 0);
    }

    public static Map<String, Integer> coherencyOverrides;
    static {
        coherencyOverrides = new HashMap<>();
        coherencyOverrides.put(PERMUTE_SECOND, 1);
        coherencyOverrides.put(ALIASED_MEMORY, 1);
    }
}
