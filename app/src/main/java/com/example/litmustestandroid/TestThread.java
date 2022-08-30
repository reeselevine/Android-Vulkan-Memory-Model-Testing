package com.example.litmustestandroid;

public class TestThread extends Thread {

    MainActivity mainActivity;
    String[] testParameter;
    boolean tuningMode;
    boolean conformanceMode;

    static {
        System.loadLibrary("litmusTest-main-lib");
    }

    TestThread(MainActivity mainActivity, String[] testParameter, boolean tuningMode, boolean conformanceMode) {
        this.mainActivity = mainActivity;
        this.testParameter = testParameter;
        this.tuningMode = tuningMode;
        this.conformanceMode = conformanceMode;
    }

    public void run() {
        main(mainActivity, testParameter, tuningMode, conformanceMode);
    }

    public native int main(MainActivity mainActivity, String[] testParameter, boolean tuningMode, boolean conformanceMode);
}
