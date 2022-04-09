package com.example.litmustestandroid;

import java.util.ArrayList;

public class TestCase {
    public String testType;
    public String testName;

    private String[] shaderNames;
    private int[] shaderIds; // Shader file

    private String[] resultNames; // Shader file that checks for number of behaviors
    private int[] resultIds;

    private String outputName; // Output text file that will store the test output
    private int outputId;

    private String testParamName; // Text file that stores test's parameter values
    private int testParamId;

    private String[] paramNames; // Text file that contains parameter preset values
    private int[] paramIds;

    public void setShaderNames(String[] newShaderNames, ArrayList<String> totalShaderNames) {
        this.shaderNames = newShaderNames;
        for(int i = 0; i < newShaderNames.length; i++) {
            if(!totalShaderNames.contains(newShaderNames[i])) {
                totalShaderNames.add(newShaderNames[i]);
            }
        }
    }

    public void setShaderIds(int[] newShaderIds, ArrayList<Integer> totalShaderIds) {
        this.shaderIds = newShaderIds;
        for(int i = 0; i < newShaderIds.length; i++) {
            if(!totalShaderIds.contains(newShaderIds[i])) {
                totalShaderIds.add(newShaderIds[i]);
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

    public void setResultIds(int[] newResultIds, ArrayList<Integer> totalResultIds) {
        this.resultIds = newResultIds;
        for(int i = 0; i < newResultIds.length; i++) {
            if(!totalResultIds.contains(newResultIds[i])) {
                totalResultIds.add(newResultIds[i]);
            }
        }
    }

    public void setOutputName(String newOutputName, ArrayList<String> totalOutputNames) {
        this.outputName = newOutputName;
        if(!totalOutputNames.contains(newOutputName)) {
            totalOutputNames.add(newOutputName);
        }
    }

    public void setOutputId(int newOutputId, ArrayList<Integer> totalOutputIds) {
        this.outputId = newOutputId;
        if(!totalOutputIds.contains(newOutputId)) {
            totalOutputIds.add(newOutputId);
        }
    }

    public void setTestParamName(String newTestParamName, ArrayList<String> totalTestParamNames) {
        this.testParamName = newTestParamName;
        if(!totalTestParamNames.contains(newTestParamName)) {
            totalTestParamNames.add(newTestParamName);
        }
    }

    public void setTestParamId(int newTestParamId, ArrayList<Integer> totalTestParamIds) {
        this.testParamId = newTestParamId;
        if(!totalTestParamIds.contains(newTestParamId)) {
            totalTestParamIds.add(newTestParamId);
        }
    }

    public void setParamNames(String[] newParamNames, ArrayList<String> totalParamNames) {
        this.paramNames = newParamNames;
        for(int i = 0; i < newParamNames.length; i++) {
            if(!totalParamNames.contains(newParamNames[i])) {
                totalParamNames.add(newParamNames[i]);
            }
        }
    }

    public void setParamIds(int[] newParamIds, ArrayList<Integer> totalParamIds) {
        this.paramIds = newParamIds;
        for(int i = 0; i < newParamIds.length; i++) {
            if(!totalParamIds.contains(newParamIds[i])) {
                totalParamIds.add(newParamIds[i]);
            }
        }
    }

}
