package com.example.litmustestandroid.HelperClass;

import java.util.HashMap;

public class TuningBestResult {

    private String testName;
    private double rate;
    private HashMap<String, Integer> paramMap;

    public TuningBestResult(String testName, double rate, HashMap<String, Integer> paramMap) {
        this.testName = testName;
        this.rate = rate;
        this.paramMap = paramMap;
    }

    public String getTestName() {
        return testName;
    }

    public HashMap<String, Integer> getParamMap() {
        return paramMap;
    }

    public double getRate() {
        return rate;
    }
}
