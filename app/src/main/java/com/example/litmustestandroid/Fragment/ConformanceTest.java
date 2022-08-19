package com.example.litmustestandroid.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.litmustestandroid.HelperClass.ConformanceTestViewObject;
import com.example.litmustestandroid.HelperClass.MultiTestViewObject;
import com.example.litmustestandroid.HelperClass.TestCase;
import com.example.litmustestandroid.MainActivity;
import com.example.litmustestandroid.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ConformanceTest  extends Fragment {

    private Button defaultParamButton, stressParamButton, sendResultButton;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_conformance_test, container, false);

        // Reset MainActivity's conformanceShaders
        for(int i = 0; i < ((MainActivity)getActivity()).conformanceShaders.size(); i++) {
            ((MainActivity)getActivity()).conformanceShaders.replaceAll((k, v) -> v = false);
        }

        TextView note = fragmentView.findViewById(R.id.conformance_test_note);
        note.setText(getResources().getString(R.string.conformance_test_note));

        TextView description = fragmentView.findViewById(R.id.conformance_test_description);
        description.setText(getResources().getString(R.string.conformance_test_description));

        ConformanceTestViewObject conformanceTestViewObject = new ConformanceTestViewObject();

        // Set progress layout to be invisible in default
        conformanceTestViewObject.progressLayout = fragmentView.findViewById(R.id.conformance_test_progressLayout);
        conformanceTestViewObject.progressLayout.setVisibility(View.GONE);

        // Set result layout to be invisible in default
        conformanceTestViewObject.resultLayout = fragmentView.findViewById(R.id.conformance_test_resultLayout);
        conformanceTestViewObject.resultLayout.setVisibility(View.GONE);

        RecyclerView resultRV = fragmentView.findViewById(R.id.conformanceTestResultRV);

        TextView testList = fragmentView.findViewById(R.id.conformanceTestList);
        LinearLayout testListLayout = fragmentView.findViewById(R.id.conformanceTestListLayout);
        testList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(testListLayout.getVisibility() == View.GONE) {
                    testListLayout.setVisibility(View.VISIBLE);
                }
                else {
                    testListLayout.setVisibility(View.GONE);
                }
            }
        });

        TextView parameterList = fragmentView.findViewById(R.id.conformanceTestParameter);
        LinearLayout parameterItemLayout = fragmentView.findViewById(R.id.conformanceTestParameterItemLayout);
        parameterList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(parameterItemLayout.getVisibility() == View.GONE) {
                    parameterItemLayout.setVisibility(View.VISIBLE);
                }
                else {
                    parameterItemLayout.setVisibility(View.GONE);
                }
            }
        });

        View[] testCheckBoxViews = new View[20];
        testCheckBoxViews[0] = fragmentView.findViewById(R.id.message_passing_coherency_checkBox);
        testCheckBoxViews[1] = fragmentView.findViewById(R.id.store_coherency_checkBox);
        testCheckBoxViews[2] = fragmentView.findViewById(R.id.read_coherency_checkBox);
        testCheckBoxViews[3] = fragmentView.findViewById(R.id.load_buffer_coherency_checkBox);
        testCheckBoxViews[4] = fragmentView.findViewById(R.id.store_buffer_coherency_checkBox);
        testCheckBoxViews[5] = fragmentView.findViewById(R.id.write_22_coherency_checkBox);
        testCheckBoxViews[6] = fragmentView.findViewById(R.id.message_passing_barrier_checkBox);
        testCheckBoxViews[7] = fragmentView.findViewById(R.id.store_barrier_checkBox);
        testCheckBoxViews[8] = fragmentView.findViewById(R.id.read_barrier_checkBox);
        testCheckBoxViews[9] = fragmentView.findViewById(R.id.load_buffer_barrier_checkBox);
        testCheckBoxViews[10] = fragmentView.findViewById(R.id.store_buffer_barrier_checkBox);
        testCheckBoxViews[11] = fragmentView.findViewById(R.id.write_22_barrier_checkBox);
        testCheckBoxViews[12] = fragmentView.findViewById(R.id.corr_default_checkBox);
        testCheckBoxViews[13] = fragmentView.findViewById(R.id.coww_default_checkBox);
        testCheckBoxViews[14] = fragmentView.findViewById(R.id.cowr_default_checkBox);
        testCheckBoxViews[15] = fragmentView.findViewById(R.id.corw2_default_checkBox);
        testCheckBoxViews[16] = fragmentView.findViewById(R.id.corr_rmw_checkBox);
        testCheckBoxViews[17] = fragmentView.findViewById(R.id.coww_rmw_checkBox);
        testCheckBoxViews[18] = fragmentView.findViewById(R.id.cowr_rmw_checkBox);
        testCheckBoxViews[19] = fragmentView.findViewById(R.id.corw2_rmw_checkBox);

        CheckBox selectAllCheckBox = fragmentView.findViewById((R.id.selectAllCheckBox));
        selectAllCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectAllCheckBox.isChecked()) {
                    for(int i = 0; i < testCheckBoxViews.length; i++) {
                        ((CheckBox)testCheckBoxViews[i]).setChecked(true);
                        ((MainActivity)getActivity()).conformanceTestCheckBoxesListener(testCheckBoxViews[i]);
                    }
                }
                else {
                    for(int i = 0; i < testCheckBoxViews.length; i++) {
                        ((CheckBox)testCheckBoxViews[i]).setChecked(false);
                        ((MainActivity)getActivity()).conformanceTestCheckBoxesListener(testCheckBoxViews[i]);
                    }
                }
            }
        });

        TestCase currTest = ((MainActivity)getActivity()).findTestCase("corr");
        int basic_parameters = getResources().getIdentifier(currTest.paramPresetNames[0], "raw", getActivity().getPackageName());
        int stress_parameters = getResources().getIdentifier(currTest.paramPresetNames[1], "raw", getActivity().getPackageName());

        EditText[] testParameters = new EditText[18];
        testParameters[0] = (EditText) fragmentView.findViewById(R.id.conformanceTestTestIteration); // testIteration
        testParameters[1] = (EditText) fragmentView.findViewById(R.id.conformanceTestTestingWorkgroups); // testingWorkgroups
        testParameters[2] = (EditText) fragmentView.findViewById(R.id.conformanceTestMaxWorkgroups); // maxWorkgroups
        testParameters[3] = (EditText) fragmentView.findViewById(R.id.conformanceTestMinWorkgroupSize); // minWorkgroupSize
        testParameters[4] = (EditText) fragmentView.findViewById(R.id.conformanceTestMaxWorkgroupSize); // maxWorkgroupSize
        testParameters[5] = (EditText) fragmentView.findViewById(R.id.conformanceTestShufflePct); // shufflePct
        testParameters[6] = (EditText) fragmentView.findViewById(R.id.conformanceTestBarrierPct); // barrierPct
        testParameters[7] = (EditText) fragmentView.findViewById(R.id.conformanceTestScratchMemorySize); // scratchMemorySize
        testParameters[8] = (EditText) fragmentView.findViewById(R.id.conformanceTestMemoryStride); // memStride
        testParameters[9] = (EditText) fragmentView.findViewById(R.id.conformanceTestMemoryStressPct); // memStressPct
        testParameters[10] = (EditText) fragmentView.findViewById(R.id.conformanceTestMemoryStressIterations); // memStressIterations
        testParameters[11] = (EditText) fragmentView.findViewById(R.id.conformanceTestMemoryStressPattern); // memStressPattern
        testParameters[12] = (EditText) fragmentView.findViewById(R.id.conformanceTestPreStressPct); // preStressPct
        testParameters[13] = (EditText) fragmentView.findViewById(R.id.conformanceTestPreStressIterations); // preStressIterations
        testParameters[14] = (EditText) fragmentView.findViewById(R.id.conformanceTestPreStressPattern); // preStressPattern
        testParameters[15] = (EditText) fragmentView.findViewById(R.id.conformanceTestStressLineSize); // stressLineSize
        testParameters[16] = (EditText) fragmentView.findViewById(R.id.conformanceTestStressTargetLines); // stressTargetLines
        testParameters[17] = (EditText) fragmentView.findViewById(R.id.conformanceTestStressAssignmentStrategy); // stressAssignmentStrategy

        ((MainActivity)getActivity()).loadParameters(testParameters, basic_parameters);

        defaultParamButton = fragmentView.findViewById(R.id.conformanceTestDefaultParamButton);
        stressParamButton = fragmentView.findViewById(R.id.conformanceTestStressParamButton);
        sendResultButton = fragmentView.findViewById(R.id.conformanceTestSendResultButton);

        conformanceTestViewObject.startButton = fragmentView.findViewById(R.id.conformance_test_startButton);
        conformanceTestViewObject.currentTestName = fragmentView.findViewById(R.id.conformance_test_currentTestName);
        conformanceTestViewObject.currentIterationNumber = fragmentView.findViewById(R.id.conformance_test_currentIterationNumber);

        defaultParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                ((MainActivity)getActivity()).loadParameters(testParameters, basic_parameters);
            }
        });

        stressParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                ((MainActivity)getActivity()).loadParameters(testParameters, stress_parameters);
            }
        });

        sendResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open dialog for sending result
                ((MainActivity)getActivity()).sendResultEmail("Conformance");
            }
        });

        conformanceTestViewObject.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).conformanceTestBegin(testParameters,  conformanceTestViewObject, resultRV);
            }
        });

        return fragmentView;
    }
}
