package com.example.litmustestandroid;

public class TestThread extends Thread {

    MainActivity mainActivity;
    String[] testName;
    boolean tuningMode;
    boolean conformanceMode;

    static {
        System.loadLibrary("litmusTest-main-lib");
    }

    TestThread(MainActivity mainActivity, String[] testName, boolean tuningMode, boolean conformanceMode) {
        this.mainActivity = mainActivity;
        this.testName = testName;
        this.tuningMode = tuningMode;
        this.conformanceMode = conformanceMode;
    }

    public void run() {
        main(mainActivity, testName, tuningMode, conformanceMode);
    }

    public native int main(MainActivity mainActivity, String[] testName, boolean tuningMode, boolean conformanceMode);
}
