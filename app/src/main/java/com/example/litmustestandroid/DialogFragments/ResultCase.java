package com.example.litmustestandroid.DialogFragments;

public class ResultCase {

    public String testName;

    public String parameters;

    public String results;

    public int numSeqBehaviors = 0;
    public int numInterleavedBehaviors = 0;
    public int numWeakBehaviors = 0;
    public double duration;
    public boolean violated = false;

    public ResultCase(String testName, String parameters, String results, int numSeqBehaviors, int numInterleavedBehaviors, int numWeakBehaviors, double duration) {
        this.testName = testName;
        this.parameters = parameters;
        this.results = results;

        this.numSeqBehaviors = numSeqBehaviors;
        this.numInterleavedBehaviors = numInterleavedBehaviors;
        this.numWeakBehaviors = numWeakBehaviors;
        this.duration = duration;
        if(this.numWeakBehaviors > 0) {
            this.violated = true;
        }
    }
}
