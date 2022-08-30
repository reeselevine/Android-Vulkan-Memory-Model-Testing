package com.example.litmustestandroid;

public class LockTestThread extends Thread {

    MainActivity mainActivity;
    String[] testParameter;
    boolean checkCorrect;

    static {
        System.loadLibrary("lockTest-main-lib");
    }

    LockTestThread(MainActivity mainActivity, String[] testParameter, boolean checkCorrect) {
        this.mainActivity = mainActivity;
        this.testParameter = testParameter;
        this.checkCorrect = checkCorrect;
    }

    public void run() {
        main(mainActivity, testParameter, checkCorrect);
    }

    public native int main(MainActivity mainActivity, String[] testParameter, boolean checkCorrect);
}
