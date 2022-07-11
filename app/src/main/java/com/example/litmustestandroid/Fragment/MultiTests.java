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

import com.example.litmustestandroid.HelperClass.MultiTestViewObject;
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

    private Button explorerButton, tuningButton, defaultParamButton, stressParamButton, sendResultButton;
    private String testMode = "Explorer";

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_multi_tests, container, false);

        TextView description = fragmentView.findViewById(R.id.multi_tests_description);
        description.setText(getResources().getString(R.string.multi_tests_description));

        MultiTestViewObject multiTestViewObject= new MultiTestViewObject();

        // Set progress layout to be invisible in default
        multiTestViewObject.progressLayout = fragmentView.findViewById(R.id.multiTestProgressLayout);
        multiTestViewObject.progressLayout.setVisibility(View.GONE);

        // Set progress config layout to be invisible in default
        multiTestViewObject.configLayout = fragmentView.findViewById(R.id.multiTestCurrentConfigLayout);
        multiTestViewObject.configLayout.setVisibility(View.GONE);

        // Set result layout to be invisible in default
        multiTestViewObject.resultLayout = fragmentView.findViewById(R.id.multiTestResultLayout);
        multiTestViewObject.resultLayout.setVisibility(View.GONE);

        RecyclerView multiTestResultRV = fragmentView.findViewById(R.id.multiTestResultRV);

        TextView testList = fragmentView.findViewById(R.id.multiTestList);

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

        TextView explorerParameterList = fragmentView.findViewById(R.id.multiTestExplorerParameter);
        LinearLayout explorerParameterLayout = fragmentView.findViewById(R.id.multiTestExplorerParameterLayout);
        LinearLayout explorerParameterItemLayout = fragmentView.findViewById(R.id.multiTestExplorerParameterItemLayout);
        explorerParameterList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(explorerParameterItemLayout.getVisibility() == View.GONE) {
                    explorerParameterItemLayout.setVisibility(View.VISIBLE);
                }
                else {
                    explorerParameterItemLayout.setVisibility(View.GONE);
                }
            }
        });

        TextView tuningParameterList = fragmentView.findViewById(R.id.multiTestTuningParameter);
        LinearLayout tuningParameterLayout = fragmentView.findViewById(R.id.multiTestTuningParameterLayout);
        LinearLayout tuningParameterItemLayout = fragmentView.findViewById(R.id.multiTestTuningParameterItemLayout);
        tuningParameterList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tuningParameterItemLayout.getVisibility() == View.GONE) {
                    tuningParameterItemLayout.setVisibility(View.VISIBLE);
                }
                else {
                    tuningParameterItemLayout.setVisibility(View.GONE);
                }
            }
        });

        explorerButton = fragmentView.findViewById(R.id.multiTestExplorerButton);
        tuningButton = fragmentView.findViewById(R.id.multiTestTuningButton);

        explorerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                explorerButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                tuningButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                explorerParameterLayout.setVisibility(View.VISIBLE);
                explorerParameterItemLayout.setVisibility(View.VISIBLE);
                tuningParameterLayout.setVisibility(View.GONE);
                testMode = "Explorer";
            }
        });

        tuningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tuningButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                explorerButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                tuningParameterLayout.setVisibility(View.VISIBLE);
                tuningParameterItemLayout.setVisibility(View.VISIBLE);
                explorerParameterLayout.setVisibility(View.GONE);
                testMode = "Tuning";
            }
        });

        TestCase currTest = ((MainActivity)getActivity()).findTestCase("message_passing");
        int basic_parameters = getResources().getIdentifier(currTest.paramPresetNames[0], "raw", getActivity().getPackageName());
        int stress_parameters = getResources().getIdentifier(currTest.paramPresetNames[1], "raw", getActivity().getPackageName());

        EditText[] multiTestExplorerParameters = new EditText[18];
        multiTestExplorerParameters[0] = (EditText) fragmentView.findViewById(R.id.multiTestExploreTestIteration); // testIteration
        multiTestExplorerParameters[1] = (EditText) fragmentView.findViewById(R.id.multiTestExploreTestingWorkgroups); // testingWorkgroups
        multiTestExplorerParameters[2] = (EditText) fragmentView.findViewById(R.id.multiTestExploreMaxWorkgroups); // maxWorkgroups
        multiTestExplorerParameters[3] = (EditText) fragmentView.findViewById(R.id.multiTestExploreMinWorkgroupSize); // minWorkgroupSize
        multiTestExplorerParameters[4] = (EditText) fragmentView.findViewById(R.id.multiTestExploreMaxWorkgroupSize); // maxWorkgroupSize
        multiTestExplorerParameters[5] = (EditText) fragmentView.findViewById(R.id.multiTestExploreShufflePct); // shufflePct
        multiTestExplorerParameters[6] = (EditText) fragmentView.findViewById(R.id.multiTestExploreBarrierPct); // barrierPct
        multiTestExplorerParameters[7] = (EditText) fragmentView.findViewById(R.id.multiTestExploreScratchMemorySize); // scratchMemorySize
        multiTestExplorerParameters[8] = (EditText) fragmentView.findViewById(R.id.multiTestExploreMemoryStride); // memStride
        multiTestExplorerParameters[9] = (EditText) fragmentView.findViewById(R.id.multiTestExploreMemoryStressPct); // memStressPct
        multiTestExplorerParameters[10] = (EditText) fragmentView.findViewById(R.id.multiTestExploreMemoryStressIterations); // memStressIterations
        multiTestExplorerParameters[11] = (EditText) fragmentView.findViewById(R.id.multiTestExploreMemOryStressPattern); // memStressPattern
        multiTestExplorerParameters[12] = (EditText) fragmentView.findViewById(R.id.multiTestExplorePreStressPct); // preStressPct
        multiTestExplorerParameters[13] = (EditText) fragmentView.findViewById(R.id.multiTestExplorePreStressIterations); // preStressIterations
        multiTestExplorerParameters[14] = (EditText) fragmentView.findViewById(R.id.multiTestExplorePreStressPattern); // preStressPattern
        multiTestExplorerParameters[15] = (EditText) fragmentView.findViewById(R.id.multiTestExploreStressLineSize); // stressLineSize
        multiTestExplorerParameters[16] = (EditText) fragmentView.findViewById(R.id.multiTestExploreStressTargetLines); // stressTargetLines
        multiTestExplorerParameters[17] = (EditText) fragmentView.findViewById(R.id.multiTestExploreStressAssignmentStrategy); // stressAssignmentStrategy

        EditText[] multiTestTuningParameters = new EditText[7];
        multiTestTuningParameters [0] = (EditText) fragmentView.findViewById(R.id.multiTestTuningConfigNum); // testConfigNum
        multiTestTuningParameters [1] = (EditText) fragmentView.findViewById(R.id.multiTestTuningTestIteration); // testIteration
        multiTestTuningParameters [2] = (EditText) fragmentView.findViewById(R.id.multiTestTuningRandomSeed); // randomSeed
        multiTestTuningParameters [3] = (EditText) fragmentView.findViewById(R.id.multiTestTuningTestingWorkgroups); // testingWorkgroups
        multiTestTuningParameters [4] = (EditText) fragmentView.findViewById(R.id.multiTestTuningMaxWorkgroups); // maxWorkgroups
        multiTestTuningParameters [5] = (EditText) fragmentView.findViewById(R.id.multiTestTuningMinWorkgroupSize); // minWorkgroupSize
        multiTestTuningParameters [6] = (EditText) fragmentView.findViewById(R.id.multiTestTuningMaxWorkgroupSize); // maxWorkgroupSize

        ((MainActivity)getActivity()).loadParameters(multiTestExplorerParameters, basic_parameters);

        defaultParamButton = fragmentView.findViewById(R.id.multiTestDefaultParamButton);
        stressParamButton = fragmentView.findViewById(R.id.multiTestStressParamButton);
        sendResultButton = fragmentView.findViewById(R.id.multiTestSendResultButton);
        multiTestViewObject.startButton = fragmentView.findViewById(R.id.multiTestStartButton);

        multiTestViewObject.currentTestName = fragmentView.findViewById(R.id.multiTestCurrentName);
        multiTestViewObject.currentConfigNumber = fragmentView.findViewById(R.id.multiTestCurrentConfigNumber);
        multiTestViewObject.currentIterationNumber = fragmentView.findViewById(R.id.multiTestCurrentIterationNumber);

        defaultParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                ((MainActivity)getActivity()).loadParameters(multiTestExplorerParameters, basic_parameters);
            }
        });

        stressParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                ((MainActivity)getActivity()).loadParameters(multiTestExplorerParameters, stress_parameters);
            }
        });

        sendResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open dialog for sending result
                ((MainActivity)getActivity()).multiTestSendResult();
            }
        });

        multiTestViewObject.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(testMode.equals("Explorer")) {
                    ((MainActivity)getActivity()).multiExplorerTestBegin(multiTestExplorerParameters,  multiTestViewObject, multiTestResultRV);
                }
                else { // Tuning
                    ((MainActivity)getActivity()).multiTuningTestBegin(multiTestTuningParameters,  multiTestViewObject, multiTestResultRV);
                }

            }
        });

        return fragmentView;
    }

}

