package com.example.litmustestandroid.HelperClass;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TestViewObject {

    public String testName;

    public LinearLayout explorerProgressLayout;
    public TextView explorerCurrentIterationNumber;

    public LinearLayout tuningProgressLayout;
    public TextView tuningCurrentConfigNumber;
    public TextView tuningCurrentIterationNumber;

    public Button explorerButton;
    public Button tuningButton;
    public Button[] buttons;

    public ResultButton explorerResultButton;
    public ResultButton tuningResultButton;
    public ResultButton[] resultButtons;
}
