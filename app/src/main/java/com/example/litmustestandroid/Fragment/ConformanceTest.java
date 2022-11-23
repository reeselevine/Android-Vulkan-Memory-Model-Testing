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

import java.util.ArrayList;
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

        ArrayList<CheckBox> conformanceCheckBoxes = conformanceTestViews(fragmentView);
        ArrayList<CheckBox> tuningCheckBoxes = tuningTestViews(fragmentView);
        ArrayList<CheckBox> allTestCheckBoxes = new ArrayList<>();
        allTestCheckBoxes.addAll(conformanceCheckBoxes);
        allTestCheckBoxes.addAll(tuningCheckBoxes);

        Button selectConformanceTestsButton = fragmentView.findViewById(R.id.selectConformanceTestsButton);
        selectConformanceTestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTests(conformanceCheckBoxes, true);
            }
        });

        Button selectTuningTestsButton = fragmentView.findViewById(R.id.selectTuningTestsButton);
        selectTuningTestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTests(tuningCheckBoxes, true);
            }
        });

        Button selectAllTestsButton = fragmentView.findViewById(R.id.selectAllTestsButton);
        selectAllTestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTests(allTestCheckBoxes, true);
            }
        });

        Button clearTestsButton = fragmentView.findViewById(R.id.clearTestsButton);
        clearTestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTests(allTestCheckBoxes, false);
            }
        });

        int basic_parameters = getResources().getIdentifier(BASIC_PARAM_FILE, "raw", getActivity().getPackageName());
        int stress_parameters = getResources().getIdentifier(STRESS_PARAM_FILE, "raw", getActivity().getPackageName());
        int tuning_parameters = getResources().getIdentifier(TUNING_PARAM_FILE, "raw", getActivity().getPackageName());

        HashMap<String, EditText> paramMap = buildParamMap(fragmentView);
        ((MainActivity)getActivity()).loadParameters(paramMap, basic_parameters);
        HashMap<String, EditText> tuningParamMap = buildTuningParamMap(fragmentView);
        ((MainActivity)getActivity()).loadParameters(tuningParamMap, tuning_parameters);

        EditText[] conformanceTestTuningParameters = new EditText[6];
        conformanceTestTuningParameters [0] = (EditText) fragmentView.findViewById(R.id.conformanceTestTuningConfigNum); // testConfigNum
        conformanceTestTuningParameters [1] = (EditText) fragmentView.findViewById(R.id.conformanceTestTuningTestIteration); // testIteration
        conformanceTestTuningParameters [2] = (EditText) fragmentView.findViewById(R.id.conformanceTestTuningRandomSeed); // randomSeed
        conformanceTestTuningParameters [3] = (EditText) fragmentView.findViewById(R.id.conformanceTestTuningTestingWorkgroups); // testingWorkgroups
        conformanceTestTuningParameters [4] = (EditText) fragmentView.findViewById(R.id.conformanceTestTuningMaxWorkgroups); // maxWorkgroups
        conformanceTestTuningParameters [5] = (EditText) fragmentView.findViewById(R.id.conformanceTestTuningWorkgroupSize); // workgroupSize



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
                ((MainActivity)getActivity()).loadParameters(paramMap, basic_parameters);
            }
        });

        stressParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                ((MainActivity)getActivity()).loadParameters(paramMap, stress_parameters);
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
                ((MainActivity)getActivity()).beginRunningTests(testMode, paramMap, conformanceTestTuningParameters, conformanceTestViewObject, conformanceTestTuningResultRV);
            }
        });

        return fragmentView;
    }

    private HashMap<String, EditText> buildParamMap(View fragmentView) {
        HashMap<String, EditText> paramMap = new HashMap<>();
        paramMap.put(ITERATIONS, fragmentView.findViewById(R.id.conformanceTestExplorerTestIteration));
        paramMap.put(TESTING_WORKGROUPS, fragmentView.findViewById(R.id.conformanceTestExplorerTestingWorkgroups));
        paramMap.put(MAX_WORKGROUPS, fragmentView.findViewById(R.id.conformanceTestExplorerMaxWorkgroups));
        paramMap.put(WORKGROUP_SIZE, fragmentView.findViewById(R.id.conformanceTestExplorerWorkgroupSize));
        paramMap.put(SHUFFLE_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerShufflePct));
        paramMap.put(BARRIER_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerBarrierPct));
        paramMap.put(SCRATCH_MEMORY_SIZE, fragmentView.findViewById(R.id.conformanceTestExplorerScratchMemorySize));
        paramMap.put(MEM_STRIDE, fragmentView.findViewById(R.id.conformanceTestExplorerScratchMemorySize));
        paramMap.put(MEM_STRESS_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerMemoryStressPct));
        paramMap.put(MEM_STRESS_ITERATIONS, fragmentView.findViewById(R.id.conformanceTestExplorerMemoryStressIterations));
        paramMap.put(MEM_STRESS_STORE_FIRST_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerMemoryStressStoreFirstPct));
        paramMap.put(MEM_STRESS_STORE_SECOND_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerMemoryStressStoreSecondPct));
        paramMap.put(PRE_STRESS_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerPreStressPct));
        paramMap.put(PRE_STRESS_ITERATIONS, fragmentView.findViewById(R.id.conformanceTestExplorerPreStressIterations));
        paramMap.put(PRE_STRESS_STORE_FIRST_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerPreStressStoreFirstPct));
        paramMap.put(PRE_STRESS_STORE_SECOND_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerPreStressStoreSecondPct));
        paramMap.put(STRESS_LINE_SIZE, fragmentView.findViewById(R.id.conformanceTestExplorerStressLineSize));
        paramMap.put(STRESS_TARGET_LINES, fragmentView.findViewById(R.id.conformanceTestExplorerStressTargetLines));
        paramMap.put(STRESS_STRATEGY_BALANCE_PCT, fragmentView.findViewById(R.id.conformanceTestExplorerStressStrategyBalancePct));
        return paramMap;
    }

    private HashMap<String, EditText> buildTuningParamMap(View fragmentView) {
        HashMap<String, EditText> tuningParamMap = new HashMap<>();
        tuningParamMap.put(NUM_CONFIGS, fragmentView.findViewById(R.id.conformanceTestTuningConfigNum));
        tuningParamMap.put(ITERATIONS, fragmentView.findViewById(R.id.conformanceTestTuningTestIteration));
        tuningParamMap.put(RANDOM_SEED, fragmentView.findViewById(R.id.conformanceTestTuningRandomSeed));
        tuningParamMap.put(TESTING_WORKGROUPS, fragmentView.findViewById(R.id.conformanceTestTuningTestingWorkgroups));
        tuningParamMap.put(MAX_WORKGROUPS, fragmentView.findViewById(R.id.conformanceTestTuningMaxWorkgroups));
        tuningParamMap.put(WORKGROUP_SIZE, fragmentView.findViewById(R.id.conformanceTestTuningWorkgroupSize));
        return tuningParamMap;
    }

    private ArrayList<CheckBox> conformanceTestViews(View view) {
        ArrayList<CheckBox> conformanceTests = new ArrayList<>();
        conformanceTests.add(view.findViewById(R.id.message_passing_coherency_checkBox));
        conformanceTests.add(view.findViewById(R.id.message_passing_barrier_checkBox));

        conformanceTests.add(view.findViewById(R.id.load_buffer_coherency_checkBox));
        conformanceTests.add(view.findViewById(R.id.load_buffer_barrier_checkBox));

        conformanceTests.add(view.findViewById(R.id.store_coherency_checkBox));
        conformanceTests.add(view.findViewById(R.id.store_barrier_checkBox));

        conformanceTests.add(view.findViewById(R.id.store_buffer_coherency_checkBox));
        conformanceTests.add(view.findViewById(R.id.store_buffer_rmw_barrier_checkBox));

        conformanceTests.add(view.findViewById(R.id.read_coherency_checkBox));
        conformanceTests.add(view.findViewById(R.id.read_rmw_barrier_checkBox));

        conformanceTests.add(view.findViewById(R.id.write_22_coherency_checkBox));
        conformanceTests.add(view.findViewById(R.id.write_22_rmw_barrier_checkBox));

        conformanceTests.add(view.findViewById(R.id.rr_checkBox));
        conformanceTests.add(view.findViewById(R.id.rr_rmw_checkBox));

        conformanceTests.add(view.findViewById(R.id.rw_checkBox));
        conformanceTests.add(view.findViewById(R.id.rw_rmw_checkBox));

        conformanceTests.add(view.findViewById(R.id.wr_checkBox));
        conformanceTests.add(view.findViewById(R.id.wr_rmw_checkBox));

        conformanceTests.add(view.findViewById(R.id.ww_checkBox));
        conformanceTests.add(view.findViewById(R.id.ww_rmw_checkBox));
        return conformanceTests;
    }

    private ArrayList<CheckBox> tuningTestViews(View view) {
        ArrayList<CheckBox> tuningTests = new ArrayList<>();
        tuningTests.add(view.findViewById(R.id.message_passing_coherency_tuning_checkBox));
        tuningTests.add(view.findViewById(R.id.message_passing_default_checkBox));
        tuningTests.add(view.findViewById(R.id.message_passing_barrier1_checkBox));
        tuningTests.add(view.findViewById(R.id.message_passing_barrier2_checkBox));

        tuningTests.add(view.findViewById(R.id.load_buffer_coherency_tuning_checkBox));
        tuningTests.add(view.findViewById(R.id.load_buffer_default_checkBox));
        tuningTests.add(view.findViewById(R.id.load_buffer_barrier1_checkBox));
        tuningTests.add(view.findViewById(R.id.load_buffer_barrier2_checkBox));

        tuningTests.add(view.findViewById(R.id.store_coherency_tuning_checkBox));
        tuningTests.add(view.findViewById(R.id.store_default_checkBox));
        tuningTests.add(view.findViewById(R.id.store_barrier1_checkBox));
        tuningTests.add(view.findViewById(R.id.store_barrier2_checkBox));

        tuningTests.add(view.findViewById(R.id.store_buffer_coherency_tuning_checkBox));
        tuningTests.add(view.findViewById(R.id.store_buffer_rmw_checkBox));
        tuningTests.add(view.findViewById(R.id.store_buffer_rmw_barrier1_checkBox));
        tuningTests.add(view.findViewById(R.id.store_buffer_rmw_barrier2_checkBox));

        tuningTests.add(view.findViewById(R.id.read_coherency_tuning_checkBox));
        tuningTests.add(view.findViewById(R.id.read_rmw_checkBox));
        tuningTests.add(view.findViewById(R.id.read_rmw_barrier1_checkBox));
        tuningTests.add(view.findViewById(R.id.read_rmw_barrier2_checkBox));

        tuningTests.add(view.findViewById(R.id.write_22_coherency_tuning_checkBox));
        tuningTests.add(view.findViewById(R.id.write_22_rmw_checkBox));
        tuningTests.add(view.findViewById(R.id.write_22_rmw_barrier1_checkBox));
        tuningTests.add(view.findViewById(R.id.write_22_rmw_barrier2_checkBox));

        tuningTests.add(view.findViewById(R.id.rr_mutant_checkBox));
        tuningTests.add(view.findViewById(R.id.rr_rmw_mutant_checkBox));

        tuningTests.add(view.findViewById(R.id.rw_mutant_checkBox));
        tuningTests.add(view.findViewById(R.id.rw_rmw_mutant_checkBox));

        tuningTests.add(view.findViewById(R.id.wr_mutant_checkBox));
        tuningTests.add(view.findViewById(R.id.wr_rmw_mutant_checkBox));

        tuningTests.add(view.findViewById(R.id.ww_mutant_checkBox));
        tuningTests.add(view.findViewById(R.id.ww_rmw_mutant_checkBox));
        return tuningTests;
    }

    private void setTests(ArrayList<CheckBox> tests, boolean set) {
        for (CheckBox test : tests) {
            test.setChecked(set);
            ((MainActivity)getActivity()).conformanceTestCheckBoxesListener(test);
        }
    }
}
