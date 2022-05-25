package com.example.litmustestandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TuningResultDialogFragment extends DialogFragment {

    private static final String TAG = "TuningResultDialog";

    private String testName;
    private ArrayList<TuningResultCase> tuningResultCases = new ArrayList<TuningResultCase>();
    private MainActivity mainActivity;

    private TextView testNameTextView;
    private TextView maxWeakBehaviorTextView;
    private TextView maxWeakBehaviorRowTextView;
    private TextView avgWeakBehaviorTextView;
    private RecyclerView tuningResultRV;
    private TuningResultAdapter tuningResultAdapter;
    private Button closeButton;

    public TuningResultDialogFragment(String testName, ArrayList<TuningResultCase> tuningResultCases, MainActivity mainActivity) {
        this.testName = testName;
        this.tuningResultCases = tuningResultCases;
        this.mainActivity = mainActivity;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tuning_result_dialog_layout, container, false);

        testNameTextView = view.findViewById(R.id.tuningResultTestName);
        maxWeakBehaviorTextView = view.findViewById(R.id.tuningResultMaxWeakBehaviors);
        maxWeakBehaviorRowTextView = view.findViewById(R.id.tuningResultMaxWeakBehaviorRows);
        avgWeakBehaviorTextView = view.findViewById(R.id.tuningResultAverageWeakBehaviors);
        tuningResultRV = view.findViewById(R.id.tuningResultRecyclerView);
        closeButton = view.findViewById(R.id.tuningResultCloseButton);

        testNameTextView.setText(testName);

        // Get max weak behavior
        int maxWeakBehavior = 0;
        String maxWeakBehaviorRow = "";
        int sumWeakBehavior = 0;
        for (int i = 0; i < tuningResultCases.size(); i++) {
            int numWeakBehavior = tuningResultCases.get(i).numWeakBehaviors;
            if(numWeakBehavior == maxWeakBehavior) {
                maxWeakBehaviorRow = maxWeakBehaviorRow + "," + Integer.toString(i + 1);
            }
            else if (numWeakBehavior > maxWeakBehavior) {
                maxWeakBehavior = numWeakBehavior;
                maxWeakBehaviorRow = Integer.toString(i + 1);
            }
            sumWeakBehavior += numWeakBehavior;
        }

        maxWeakBehaviorTextView.setText(Integer.toString(maxWeakBehavior));
        maxWeakBehaviorRowTextView.setText(maxWeakBehaviorRow);
        avgWeakBehaviorTextView.setText(Integer.toString(sumWeakBehavior / tuningResultCases.size()));

        tuningResultAdapter = new TuningResultAdapter(testName, getActivity(), tuningResultCases, mainActivity);
        tuningResultRV.setAdapter(tuningResultAdapter);
        tuningResultRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        tuningResultRV.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
