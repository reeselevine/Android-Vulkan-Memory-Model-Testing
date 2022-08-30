package com.example.litmustestandroid.HelperClass;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LockTestViewObject {

    public String testName;

    public LinearLayout progressLayout;
    public TextView currentIterationNumber;

    public Button speedButton;
    public Button speedRelaxedButton;
    public Button correctButton;
    public Button[] buttons;

    public ResultButton speedResultButton;
    public ResultButton speedRelaxedResultButton;
    public ResultButton correctResultButton;
    public ResultButton[] resultButtons;
}
