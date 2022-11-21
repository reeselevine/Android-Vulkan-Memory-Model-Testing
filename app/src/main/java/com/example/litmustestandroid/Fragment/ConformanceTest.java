package com.example.litmustestandroid.Fragment;

import static com.example.litmustestandroid.HelperClass.FileConstants.*;
import static com.example.litmustestandroid.HelperClass.ParameterConstants.*;

import android.os.Bundle;
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
import com.example.litmustestandroid.HelperClass.RunType;
import com.example.litmustestandroid.MainActivity;
import com.example.litmustestandroid.R;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ConformanceTest  extends Fragment {

    private Button explorerButton, tuningButton, defaultParamButton, stressParamButton, tuningSendResultButton;
    private RunType testMode = RunType.MULTI_EXPLORER;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_conformance_test, container, false);

        // Reset MainActivity's conformanceShaders
        ((MainActivity)getActivity()).selectedTests.clear();

        TextView note = fragmentView.findViewById(R.id.conformance_test_note);
        note.setText(getResources().getString(R.string.conformance_test_note));

        TextView description = fragmentView.findViewById(R.id.conformance_test_description);
        description.setText(getResources().getString(R.string.conformance_test_description));

        ConformanceTestViewObject conformanceTestViewObject = new ConformanceTestViewObject();

        // Set progress layout to be invisible in default
        conformanceTestViewObject.progressLayout = fragmentView.findViewById(R.id.conformance_test_progressLayout);
        conformanceTestViewObject.progressLayout.setVisibility(View.GONE);

        // Set progress config layout to be invisible in default
        conformanceTestViewObject.configLayout = fragmentView.findViewById(R.id.conformance_test_currentConfigLayout);
        conformanceTestViewObject.configLayout.setVisibility(View.GONE);

        // Set tuning result layout to be invisible in default
        conformanceTestViewObject.resultLayout = fragmentView.findViewById(R.id.conformance_test_tuningResultLayout);
        conformanceTestViewObject.resultLayout.setVisibility(View.GONE);

        RecyclerView conformanceTestTuningResultRV = fragmentView.findViewById(R.id.conformanceTestTuningResultRV);

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

        TextView explorerParameterList = fragmentView.findViewById(R.id.conformanceTestExplorerParameter);
        LinearLayout explorerParameterLayout = fragmentView.findViewById(R.id.conformanceTestExplorerParameterLayout);
        LinearLayout explorerParameterItemLayout = fragmentView.findViewById(R.id.conformanceTestExplorerParameterItemLayout);
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

        TextView tuningParameterList = fragmentView.findViewById(R.id.conformanceTestTuningParameter);
        LinearLayout tuningParameterLayout = fragmentView.findViewById(R.id.conformanceTestTuningParameterLayout);
        LinearLayout tuningParameterItemLayout = fragmentView.findViewById(R.id.conformanceTestTuningParameterItemLayout);
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

        explorerButton = fragmentView.findViewById(R.id.conformanceTestExplorerButton);
        tuningButton = fragmentView.findViewById(R.id.conformanceTestTuningButton);

        explorerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                explorerButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                tuningButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                explorerParameterLayout.setVisibility(View.VISIBLE);
                explorerParameterItemLayout.setVisibility(View.VISIBLE);
                conformanceTestViewObject.resultLayout.setVisibility(View.GONE);
                tuningParameterLayout.setVisibility(View.GONE);
                testMode = RunType.MULTI_EXPLORER;
            }
        });

        tuningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tuningButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                explorerButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                tuningParameterLayout.setVisibility(View.VISIBLE);
                tuningParameterItemLayout.setVisibility(View.VISIBLE);

                conformanceTestViewObject.resultLayout.setVisibility(View.GONE);
                explorerParameterLayout.setVisibility(View.GONE);
                testMode = RunType.MULTI_TUNING;
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

        int basic_parameters = getResources().getIdentifier(BASIC_PARAM_FILE, "raw", getActivity().getPackageName());
        int stress_parameters = getResources().getIdentifier(STRESS_PARAM_FILE, "raw", getActivity().getPackageName());

        HashMap<String, EditText> conformanceTestExplorerParamMap = new HashMap<>();
        conformanceTestExplorerParamMap.put(ITERATIONS, fragmentView.findViewById(R.id.conformanceTestExplorerTestIteration));
        conformanceTestExplorerParamMap.put(TESTING_WORKGROUPS, fragmentView.findViewById(R.id.conformanceTestExplorerTestingWorkgroups));
        conformanceTestExplorerParamMap.put(MAX_WORKGROUPS, fragmentView.findViewById(R.id.conformanceTestExplorerMaxWorkgroups));
        conformanceTestExplorerParamMap.put(WORKGROUP_SIZE, fragmentView.findViewById(R.id.conformanceTestExplorerWorkgroupSize));
        conformanceTestExplorerParamMap.put(SHUFFLE_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerShufflePct));
        conformanceTestExplorerParamMap.put(BARRIER_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerBarrierPct));
        conformanceTestExplorerParamMap.put(SCRATCH_MEMORY_SIZE, fragmentView.findViewById(R.id.conformanceTestExplorerScratchMemorySize));
        conformanceTestExplorerParamMap.put(MEM_STRIDE, fragmentView.findViewById(R.id.conformanceTestExplorerScratchMemorySize));
        conformanceTestExplorerParamMap.put(MEM_STRESS_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerMemoryStressPct));
        conformanceTestExplorerParamMap.put(MEM_STRESS_ITERATIONS, fragmentView.findViewById(R.id.conformanceTestExplorerMemoryStressIterations));
        conformanceTestExplorerParamMap.put(MEM_STRESS_STORE_FIRST_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerMemoryStressStoreFirstPct));
        conformanceTestExplorerParamMap.put(MEM_STRESS_STORE_SECOND_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerMemoryStressStoreSecondPct));
        conformanceTestExplorerParamMap.put(PRE_STRESS_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerPreStressPct));
        conformanceTestExplorerParamMap.put(PRE_STRESS_ITERATIONS, fragmentView.findViewById(R.id.conformanceTestExplorerPreStressIterations));
        conformanceTestExplorerParamMap.put(PRE_STRESS_STORE_FIRST_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerPreStressStoreFirstPct));
        conformanceTestExplorerParamMap.put(PRE_STRESS_STORE_SECOND_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerPreStressStoreSecondPct));
        conformanceTestExplorerParamMap.put(STRESS_LINE_SIZE, fragmentView.findViewById(R.id.conformanceTestExplorerStressLineSize));
        conformanceTestExplorerParamMap.put(STRESS_TARGET_LINES, fragmentView.findViewById(R.id.conformanceTestExplorerStressTargetLines));
        conformanceTestExplorerParamMap.put(STRESS_STRATEGY_BALANCE_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerStressStrategyBalancePct));

        EditText[] conformanceTestTuningParameters = new EditText[6];
        conformanceTestTuningParameters [0] = (EditText) fragmentView.findViewById(R.id.conformanceTestTuningConfigNum); // testConfigNum
        conformanceTestTuningParameters [1] = (EditText) fragmentView.findViewById(R.id.conformanceTestTuningTestIteration); // testIteration
        conformanceTestTuningParameters [2] = (EditText) fragmentView.findViewById(R.id.conformanceTestTuningRandomSeed); // randomSeed
        conformanceTestTuningParameters [3] = (EditText) fragmentView.findViewById(R.id.conformanceTestTuningTestingWorkgroups); // testingWorkgroups
        conformanceTestTuningParameters [4] = (EditText) fragmentView.findViewById(R.id.conformanceTestTuningMaxWorkgroups); // maxWorkgroups
        conformanceTestTuningParameters [5] = (EditText) fragmentView.findViewById(R.id.conformanceTestTuningWorkgroupSize); // workgroupSize

        ((MainActivity)getActivity()).loadParameters(conformanceTestExplorerParamMap, basic_parameters);

        defaultParamButton = fragmentView.findViewById(R.id.conformanceTestDefaultParamButton);
        stressParamButton = fragmentView.findViewById(R.id.conformanceTestStressParamButton);
        tuningSendResultButton = fragmentView.findViewById(R.id.conformanceTestTuningSendResultButton);

        conformanceTestViewObject.startButton = fragmentView.findViewById(R.id.conformance_test_startButton);
        conformanceTestViewObject.currentTestName = fragmentView.findViewById(R.id.conformance_test_currentTestName);
        conformanceTestViewObject.currentConfigNumber = fragmentView.findViewById(R.id.conformance_test_currentConfigNumber);
        conformanceTestViewObject.currentIterationNumber = fragmentView.findViewById(R.id.conformance_test_currentIterationNumber);

        defaultParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                ((MainActivity)getActivity()).loadParameters(conformanceTestExplorerParamMap, basic_parameters);
            }
        });

        stressParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                ((MainActivity)getActivity()).loadParameters(conformanceTestExplorerParamMap, stress_parameters);
            }
        });

        tuningSendResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open dialog for sending result
                ((MainActivity)getActivity()).sendResultEmail(testMode);
            }
        });

        conformanceTestViewObject.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(testMode.equals(RunType.MULTI_EXPLORER)) {
                    ((MainActivity)getActivity()).conformanceExplorerTestBegin(conformanceTestExplorerParamMap,  conformanceTestViewObject, conformanceTestTuningResultRV);
                }
                else { // Tuning
                    ((MainActivity)getActivity()).conformanceTuningTestBegin(conformanceTestTuningParameters,  conformanceTestViewObject, conformanceTestTuningResultRV);
                }
            }
        });

        return fragmentView;
    }
}
