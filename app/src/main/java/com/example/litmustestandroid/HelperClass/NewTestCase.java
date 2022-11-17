package com.example.litmustestandroid.HelperClass;
import java.util.ArrayList;

public class NewTestCase {

    public enum TestType {
        WEAK_MEMORY,
        COHERENCY
    };

    private TestType testType;

    private String testName;

    private String shaderFile;

    private String resultFile;

    public NewTestCase(String _testName, String _shaderFile, String _resultFile, TestType _testType) {
        this.testName = _testName;
        this.shaderFile = _shaderFile;
        this.resultFile = _resultFile;
        this.testType = _testType;
    }

    public String getTestName() {
        return testName;
    }

    public String getShaderFile() {
        return shaderFile;
    }

    public String getResultFile() {
        return resultFile;
    }

    public TestType getTestType() {
        return testType;
    }
}