package com.example.litmustestandroid.DialogFragments;

public class ConformanceResultCase {

    public String testName;

    public String parameters;

    public String results;

    public int numNonWeakBehaviors = 0;
    public int numWeakBehaviors = 0;
    public boolean violated = false;

    public ConformanceResultCase(String testName, String parameters, String results, int numNonWeakBehaviors, int numWeakBehaviors) {
        this.testName = testName;
        this.parameters = parameters;
        this.results = results;

        this.numNonWeakBehaviors = numNonWeakBehaviors;
        this.numWeakBehaviors = numWeakBehaviors;
        if(this.numWeakBehaviors > 0) {
            this.violated = true;
        }
    }
}
