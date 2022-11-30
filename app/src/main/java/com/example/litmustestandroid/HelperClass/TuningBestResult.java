package com.example.litmustestandroid.HelperClass;

import java.util.HashMap;

public class TuningBestResult {

    private String testName;
    private HashMap<String, Integer> paramMap;

    public TuningBestResult(String testName, HashMap<String, Integer> paramMap) {
        this.testName = testName;
        this.paramMap = paramMap;
    }

    public String getTestName() {
        return testName;
    }

    public HashMap<String, Integer> getParamMap() {
        return paramMap;
    }
}
