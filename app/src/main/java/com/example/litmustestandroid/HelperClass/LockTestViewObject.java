package com.example.litmustestandroid.HelperClass;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LockTestViewObject {

    public String testName;

    public EditText testIteration;
    public EditText workgroupNumber;
    public EditText workgroupSize;

    public LinearLayout progressLayout;
    public TextView currentIterationNumber;

    public Button speedButton;
    public Button speedRelaxedButton;
    public Button correctButton;
    public Button correctRelaxedButton;
    public Button[] buttons;

    public ResultButton speedResultButton;
    public ResultButton speedRelaxedResultButton;
    public ResultButton correctResultButton;
    public ResultButton correctRelaxedResultButton;
    public ResultButton[] resultButtons;
}
