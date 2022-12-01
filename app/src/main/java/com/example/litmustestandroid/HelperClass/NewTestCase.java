package com.example.litmustestandroid.HelperClass;
import java.util.ArrayList;

public class NewTestCase {

    public enum TestType {
        WEAK_MEMORY,
        COHERENCY
    };

    /** Weak memory tests ususally involve multiple test locations, while coherency tests
     * usually involve one.
     */
    private TestType testType;

    private String testName;

    private String shaderFile;

    private String resultFile;

    /** If this is a tuning test, it will have an associated conformance test, which is used during
     * the "Tune and Conform" runs.
     */
    private String conformanceTest;

    public NewTestCase(String _testName, String _shaderFile, String _resultFile, TestType _testType, String _conformanceTest) {
        this.testName = _testName;
        this.shaderFile = _shaderFile;
        this.resultFile = _resultFile;
        this.testType = _testType;
        this.conformanceTest = _conformanceTest;
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

    public String getConformanceTest() {
        return conformanceTest;
    }

    public TestType getTestType() {
        return testType;
    }
}