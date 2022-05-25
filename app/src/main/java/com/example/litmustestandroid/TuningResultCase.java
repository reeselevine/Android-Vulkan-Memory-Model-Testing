package com.example.litmustestandroid;

public class TuningResultCase {

    public String parameters;

    public String results;

    public int numWeakBehaviors = 0;

    public TuningResultCase(String parameters, String results, int numWeakBehaviors) {
        this.parameters = parameters;
        this.results = results;
        this.numWeakBehaviors = numWeakBehaviors;
    }
}
