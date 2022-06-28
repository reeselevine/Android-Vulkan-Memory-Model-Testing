package com.example.litmustestandroid.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.litmustestandroid.HelperClass.ResultButton;
import com.example.litmustestandroid.HelperClass.TestCase;
import com.example.litmustestandroid.MainActivity;
import com.example.litmustestandroid.R;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MultiTests extends Fragment {

    private Button defaultParamButton, stressParamButton, startButton;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_multi_tests, container, false);

        TextView description = fragmentView.findViewById(R.id.multi_tests_description);
        description.setText(getResources().getString(R.string.multi_tests_description));

        // Set result layout to be invisible in default
        LinearLayout resultLayout = fragmentView.findViewById(R.id.multiTestResultLayout);
        resultLayout.setVisibility(View.GONE);

        RecyclerView multiTestResultRV = fragmentView.findViewById(R.id.multiTestResultRV);

        TextView testList = fragmentView.findViewById(R.id.multiTestList);
        TextView parameterList = fragmentView.findViewById(R.id.multiTestParameter);

        testList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout testListLayout = fragmentView.findViewById(R.id.multiTestListLayout);
                if(testListLayout.getVisibility() == View.GONE) {
                    testListLayout.setVisibility(View.VISIBLE);
                }
                else {
                    testListLayout.setVisibility(View.GONE);
                }
            }
        });

        parameterList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout parameterLayout = fragmentView.findViewById(R.id.multiTestParameterLayout);
                if(parameterLayout.getVisibility() == View.GONE) {
                    parameterLayout.setVisibility(View.VISIBLE);
                }
                else {
                    parameterLayout.setVisibility(View.GONE);
                }
            }
        });

        TestCase currTest = ((MainActivity)getActivity()).findTestCase("message_passing");
        int basic_parameters = getResources().getIdentifier(currTest.paramPresetNames[0], "raw", getActivity().getPackageName());
        int stress_parameters = getResources().getIdentifier(currTest.paramPresetNames[1], "raw", getActivity().getPackageName());

        EditText[] multiTestParameters = new EditText[18];
        multiTestParameters[0] = (EditText) fragmentView.findViewById(R.id.multiTestExploreTestIteration); // testIteration
        multiTestParameters[1] = (EditText) fragmentView.findViewById(R.id.multiTestExploreTestingWorkgroups); // testingWorkgroups
        multiTestParameters[2] = (EditText) fragmentView.findViewById(R.id.multiTestExploreMaxWorkgroups); // maxWorkgroups
        multiTestParameters[3] = (EditText) fragmentView.findViewById(R.id.multiTestExploreMinWorkgroupSize); // minWorkgroupSize
        multiTestParameters[4] = (EditText) fragmentView.findViewById(R.id.multiTestExploreMaxWorkgroupSize); // maxWorkgroupSize
        multiTestParameters[5] = (EditText) fragmentView.findViewById(R.id.multiTestExploreShufflePct); // shufflePct
        multiTestParameters[6] = (EditText) fragmentView.findViewById(R.id.multiTestExploreBarrierPct); // barrierPct
        multiTestParameters[7] = (EditText) fragmentView.findViewById(R.id.multiTestExploreScratchMemorySize); // scratchMemorySize
        multiTestParameters[8] = (EditText) fragmentView.findViewById(R.id.multiTestExploreMemoryStride); // memStride
        multiTestParameters[9] = (EditText) fragmentView.findViewById(R.id.multiTestExploreMemoryStressPct); // memStressPct
        multiTestParameters[10] = (EditText) fragmentView.findViewById(R.id.multiTestExploreMemoryStressIterations); // memStressIterations
        multiTestParameters[11] = (EditText) fragmentView.findViewById(R.id.multiTestExploreMemOryStressPattern); // memStressPattern
        multiTestParameters[12] = (EditText) fragmentView.findViewById(R.id.multiTestExplorePreStressPct); // preStressPct
        multiTestParameters[13] = (EditText) fragmentView.findViewById(R.id.multiTestExplorePreStressIterations); // preStressIterations
        multiTestParameters[14] = (EditText) fragmentView.findViewById(R.id.multiTestExplorePreStressPattern); // preStressPattern
        multiTestParameters[15] = (EditText) fragmentView.findViewById(R.id.multiTestExploreStressLineSize); // stressLineSize
        multiTestParameters[16] = (EditText) fragmentView.findViewById(R.id.multiTestExploreStressTargetLines); // stressTargetLines
        multiTestParameters[17] = (EditText) fragmentView.findViewById(R.id.multiTestExploreStressAssignmentStrategy); // stressAssignmentStrategy

        ((MainActivity)getActivity()).loadParameters(multiTestParameters, basic_parameters);

        defaultParamButton = fragmentView.findViewById(R.id.multiTestDefaultParamButton);
        stressParamButton = fragmentView.findViewById(R.id.multiTestStressParamButton);
        startButton = fragmentView.findViewById(R.id.multiTestStartButton);

        defaultParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                ((MainActivity)getActivity()).loadParameters(multiTestParameters, basic_parameters);
            }
        });

        stressParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                ((MainActivity)getActivity()).loadParameters(multiTestParameters, stress_parameters);
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).multiTestBegin(multiTestParameters, startButton, resultLayout, multiTestResultRV);
            }
        });

        return fragmentView;
    }

}

