package com.example.litmustestandroid.HelperClass;

public class FileConstants {

    private FileConstants(){}

    public static final String BASIC_PARAM_FILE = "parameters_basic";
    public static final String STRESS_PARAM_FILE = "parameters_stress";
    public static final String COHERENCY_OVERRIDES_FILE = "coherency_overrides";

    public static final String PARAMETERS_FILE = "parameters";
    public static final String OUTPUT_FILE = "output";
    public static final String RESULT_FILE = "result.json";

    public static final String[] allFiles = {BASIC_PARAM_FILE, STRESS_PARAM_FILE, COHERENCY_OVERRIDES_FILE, PARAMETERS_FILE, OUTPUT_FILE};
}
