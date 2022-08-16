package com.example.litmustestandroid.HelperClass;

import java.util.ArrayList;

public class TestCase {
    public String testType;
    public String testName;

    public String[] shaderNames;

    public String[] conformanceShaderNames;

    public String[] resultNames; // Shader file that checks for number of behaviors

    public String[] outputNames; // Output text file that will store the test output

    public String testParamName; // Text file that stores test's parameter values

    public String[] paramPresetNames; // Text file that contains parameter preset values

    public void setShaderNames(String[] newShaderNames, ArrayList<String> totalShaderNames) {
        this.shaderNames = newShaderNames;
        for(int i = 0; i < newShaderNames.length; i++) {
            if(!totalShaderNames.contains(newShaderNames[i])) {
                totalShaderNames.add(newShaderNames[i]);
            }
        }
    }

    public void setResultNames(String[] newResultNames, ArrayList<String> totalResultNames) {
        this.resultNames = newResultNames;
        for(int i = 0; i < newResultNames.length; i++) {
            if(!totalResultNames.contains(newResultNames[i])) {
                totalResultNames.add(newResultNames[i]);
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

    public void setTestParamName(String newTestParamName, ArrayList<String> totalTestParamNames) {
        this.testParamName = newTestParamName;
        if(!totalTestParamNames.contains(newTestParamName)) {
            totalTestParamNames.add(newTestParamName);
        }
    }

    public void setParamPresetNames(String[] newParamPresetNames, ArrayList<String> totalParamPresetNames) {
        this.paramPresetNames = newParamPresetNames;
        for(int i = 0; i < newParamPresetNames.length; i++) {
            if(!totalParamPresetNames.contains(newParamPresetNames[i])) {
                totalParamPresetNames.add(newParamPresetNames[i]);
            }
        }
    }

}
