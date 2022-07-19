package com.example.litmustestandroid.DialogFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.litmustestandroid.MainActivity;
import com.example.litmustestandroid.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MultiTuningResultDialogFragment extends DialogFragment {

    private String[] testNames;
    private ArrayList<TuningResultCase> tuningResultCases = new ArrayList<TuningResultCase>();
    private MainActivity mainActivity;

    private TextView resultText;
    private String resultString = "";
    private Button closeButton;

    public MultiTuningResultDialogFragment(String[] testNames, ArrayList<TuningResultCase> tuningResultCases, MainActivity mainActivity) {
        this.testNames = testNames;
        this.tuningResultCases = tuningResultCases;
        this.mainActivity = mainActivity;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_result_dialog_layout, container, false);

        resultText = view.findViewById(R.id.resultText);
        closeButton = view.findViewById(R.id.closeButton);

        // Write parameters
        resultText.append(tuningResultCases.get(0).parameters + "\n");

        for(int i = 0; i < tuningResultCases.size(); i++) {
            resultString = "";
            resultString += testNames[i] + " ";
            resultString += tuningResultCases.get(i).results;
            resultText.append(resultString + "\n");
        }

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
