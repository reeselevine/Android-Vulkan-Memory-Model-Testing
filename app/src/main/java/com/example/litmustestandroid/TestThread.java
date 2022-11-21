package com.example.litmustestandroid;

public class TestThread extends Thread {

    MainActivity mainActivity;
    String[] testParameter;
    boolean tuningMode;
    boolean conformanceMode;

    static {
        System.loadLibrary("litmusTest-main-lib");
    }

    TestThread(MainActivity mainActivity, String[] testParameter) {
        this.mainActivity = mainActivity;
        this.testParameter = testParameter;
    }

    public void run() {
        main(mainActivity, testParameter);
    }

    public native int main(MainActivity mainActivity, String[] testParameter);
}
