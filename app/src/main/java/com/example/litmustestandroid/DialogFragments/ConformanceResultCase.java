package com.example.litmustestandroid.DialogFragments;

public class ConformanceResultCase {

    public String testName;

    public String parameters;

    public String results;

    public int numWeakBehaviors = 0;
    public boolean violated = false;

    public ConformanceResultCase(String testName, String parameters, String results, int numWeakBehaviors) {
        this.testName = testName;
        this.parameters = parameters;
        this.results = results;

        this.numWeakBehaviors = numWeakBehaviors;
        if(this.numWeakBehaviors > 0) {
            this.violated = true;
        }
    }
}
