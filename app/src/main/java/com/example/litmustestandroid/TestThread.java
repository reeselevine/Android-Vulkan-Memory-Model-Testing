package com.example.litmustestandroid;

public class TestThread extends Thread {

    MainActivity mainActivity;
    String[] testName;
    boolean tuningMode;

    static {
        System.loadLibrary("litmusTest-main-lib");
    }

    TestThread(MainActivity mainActivity, String[] testName, boolean tuningMode) {
        this.mainActivity = mainActivity;
        this.testName = testName;
        this.tuningMode = tuningMode;
    }

    public void run() {
        main(mainActivity, testName, tuningMode);
    }

    public native int main(MainActivity mainActivity, String[] testName, boolean tuningMode);
}
