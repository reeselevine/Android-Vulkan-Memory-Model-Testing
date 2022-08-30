package com.example.litmustestandroid.HelperClass;

import java.util.ArrayList;

public class LockTestCase {
    public String testName;
    public String testType;

    public String[] shaderNames;

    public String[] outputNames; // Output text file that will store the test output

    public void setShaderNames(String[] newShaderNames, ArrayList<String> totalShaderNames) {
        this.shaderNames = newShaderNames;
        for(int i = 0; i < newShaderNames.length; i++) {
            if(!totalShaderNames.contains(newShaderNames[i])) {
                totalShaderNames.add(newShaderNames[i]);
            }
        }
    }

    public void setOutputName(String[] newOutputNames, ArrayList<String> totalOutputNames) {
        this.outputNames = newOutputNames;
        for(int i = 0; i < newOutputNames.length; i++) {
            if(!totalOutputNames.contains(newOutputNames[i])) {
                totalOutputNames.add(newOutputNames[i]);
            }
        }
    }

}
