package com.example.litmustestandroid.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.litmustestandroid.HelperClass.LockTestViewObject;
import com.example.litmustestandroid.HelperClass.ResultButton;
import com.example.litmustestandroid.HelperClass.TestViewObject;
import com.example.litmustestandroid.MainActivity;
import com.example.litmustestandroid.R;

import org.jetbrains.annotations.NotNull;

public class LockTest extends Fragment {

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_lock_test, container, false);

        TextView description = fragmentView.findViewById(R.id.lockTest_description);
        description.setText(getResources().getString(R.string.lock_test_description));

        LockTestViewObject testViewObject = new LockTestViewObject();

        testViewObject.testName = "lockTest";

        testViewObject.testIteration = fragmentView.findViewById(R.id.lockTestTestIteration);
        testViewObject.workgroupNumber = fragmentView.findViewById(R.id.lockTestWorkgroupNumber);
        testViewObject.workgroupSize = fragmentView.findViewById(R.id.lockTestWorkgroupSize);

        testViewObject.progressLayout = fragmentView.findViewById(R.id.lockTest_progressLayout);
        testViewObject.currentIterationNumber = fragmentView.findViewById(R.id.lockTest_currentIterationNumber);
        testViewObject.progressLayout.setVisibility(View.GONE);

        testViewObject.speedButton = fragmentView.findViewById(R.id.lockTest_speedStartButton);
        testViewObject.speedRelaxedButton = fragmentView.findViewById(R.id.lockTest_speedRelaxedStartButton);
        testViewObject.correctButton = fragmentView.findViewById(R.id.lockTest_correctStartButton);
        testViewObject.correctRelaxedButton = fragmentView.findViewById(R.id.lockTest_correctRelaxedStartButton);

        testViewObject.speedResultButton = new ResultButton(fragmentView.findViewById(R.id.lockTest_speedResultButton));
        testViewObject.speedRelaxedResultButton = new ResultButton(fragmentView.findViewById(R.id.lockTest_speedRelaxedResultButton));
        testViewObject.correctResultButton = new ResultButton(fragmentView.findViewById(R.id.lockTest_correctResultButton));
        testViewObject.correctRelaxedResultButton = new ResultButton(fragmentView.findViewById(R.id.lockTest_correctRelaxedResultButton));

        // Initial button state
        testViewObject.speedResultButton.button.setEnabled(false);
        testViewObject.speedRelaxedResultButton.button.setEnabled(false);
        testViewObject.correctResultButton.button.setEnabled(false);
        testViewObject.correctRelaxedResultButton.button.setEnabled(false);
        testViewObject.speedResultButton.button.setBackgroundColor(getResources().getColor(R.color.lightgray));
        testViewObject.speedRelaxedResultButton.button.setBackgroundColor(getResources().getColor(R.color.lightgray));
        testViewObject.correctResultButton.button.setBackgroundColor(getResources().getColor(R.color.lightgray));
        testViewObject.correctRelaxedResultButton.button.setBackgroundColor(getResources().getColor(R.color.lightgray));

        testViewObject.speedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testViewObject.buttons = new Button[]{testViewObject.speedButton, testViewObject.speedRelaxedButton, testViewObject.correctButton, testViewObject.correctRelaxedButton};
                testViewObject.resultButtons = new ResultButton[]{testViewObject.speedResultButton, testViewObject.speedRelaxedResultButton, testViewObject.correctResultButton, testViewObject.correctRelaxedResultButton};
                ((MainActivity)getActivity()).lockTestBegin(testViewObject, "locktest_speed", false);
            }
        });
        testViewObject.speedResultButton.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).displayLockTestResult("locktest_speed");
            }
        });
        testViewObject.speedRelaxedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testViewObject.buttons = new Button[]{testViewObject.speedRelaxedButton, testViewObject.speedButton, testViewObject.correctButton, testViewObject.correctRelaxedButton};
                testViewObject.resultButtons = new ResultButton[]{testViewObject.speedRelaxedResultButton, testViewObject.speedResultButton, testViewObject.correctResultButton, testViewObject.correctRelaxedResultButton};
                ((MainActivity)getActivity()).lockTestBegin(testViewObject, "locktest_speedrelaxed", false);
            }
        });
        testViewObject.speedRelaxedResultButton.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).displayLockTestResult("locktest_speedrelaxed");
            }
        });
        testViewObject.correctButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testViewObject.buttons = new Button[]{testViewObject.correctButton, testViewObject.speedRelaxedButton, testViewObject.speedButton, testViewObject.correctRelaxedButton};
                testViewObject.resultButtons = new ResultButton[]{testViewObject.correctResultButton, testViewObject.speedRelaxedResultButton, testViewObject.speedResultButton, testViewObject.correctRelaxedResultButton};
                ((MainActivity)getActivity()).lockTestBegin(testViewObject, "locktest_correct", true);
            }
        });
        testViewObject.correctResultButton.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).displayLockTestResult("locktest_correct");
            }
        });
        testViewObject.correctRelaxedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testViewObject.buttons = new Button[]{testViewObject.correctRelaxedButton, testViewObject.correctButton, testViewObject.speedRelaxedButton, testViewObject.speedButton};
                testViewObject.resultButtons = new ResultButton[]{testViewObject.correctRelaxedResultButton, testViewObject.correctResultButton, testViewObject.speedRelaxedResultButton, testViewObject.speedResultButton};
                ((MainActivity)getActivity()).lockTestBegin(testViewObject, "locktest_correctrelaxed", true);
            }
        });
        testViewObject.correctRelaxedResultButton.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).displayLockTestResult("locktest_correctrelaxed");
            }
        });

        return fragmentView;
    }
}

