package com.example.litmustestandroid.DialogFragments;

public class TuningResultCase {

    public String testName;

    public String parameters;

    public String results;

    public int numSeqBehaviors = 0;
    public int numInterleavedBehaviors = 0;
    public int numWeakBehaviors = 0;

    public TuningResultCase(String testName, String parameters, String results,
                            int numSeqBehaviors, int numInterleavedBehaviors, int numWeakBehaviors) {
        this.testName = testName;
        this.parameters = parameters;
        this.results = results;

        this.numSeqBehaviors = numSeqBehaviors;
        this.numInterleavedBehaviors = numInterleavedBehaviors;
        this.numWeakBehaviors = numWeakBehaviors;
    }
}
