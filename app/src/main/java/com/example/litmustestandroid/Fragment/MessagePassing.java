package com.example.litmustestandroid.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.litmustestandroid.HelperClass.TestViewObject;
import com.example.litmustestandroid.MainActivity;
import com.example.litmustestandroid.R;
import com.example.litmustestandroid.HelperClass.ResultButton;

import org.jetbrains.annotations.NotNull;

public class MessagePassing extends Fragment {

    private int count = 0;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_message_passing, container, false);

        TextView description = fragmentView.findViewById(R.id.message_passing_description);
        description.setText(getResources().getString(R.string.message_passing_description));

        TestViewObject testViewObject = new TestViewObject();

        testViewObject.testName = "message_passing";

        testViewObject.explorerProgressLayout = fragmentView.findViewById(R.id.message_passing_explorerProgressLayout);
        testViewObject.explorerCurrentIterationNumber = fragmentView.findViewById(R.id.message_passing_explorerCurrentIterationNumber);
        testViewObject.explorerProgressLayout.setVisibility(View.GONE);

        testViewObject.tuningProgressLayout = fragmentView.findViewById(R.id.message_passing_tuningProgressLayout);
        testViewObject.tuningCurrentConfigNumber = fragmentView.findViewById(R.id.message_passing_tuningCurrentConfigNumber);
        testViewObject.tuningCurrentIterationNumber = fragmentView.findViewById(R.id.message_passing_tuningCurrentIterationNumber);
        testViewObject.tuningProgressLayout.setVisibility(View.GONE);

        testViewObject.explorerButton = fragmentView.findViewById(R.id.message_passing_explorerButton);
        testViewObject.tuningButton = fragmentView.findViewById(R.id.message_passing_tuningButton);

        testViewObject.explorerResultButton = new ResultButton(fragmentView.findViewById(R.id.message_passing_explorerResultButton));
        testViewObject.tuningResultButton = new ResultButton(fragmentView.findViewById(R.id.message_passing_tuningResultButton));

        // Initial button state
        testViewObject.explorerResultButton.button.setEnabled(false);
        testViewObject.tuningResultButton.button.setEnabled(false);
        testViewObject.explorerResultButton.button.setBackgroundColor(getResources().getColor(R.color.lightgray));
        testViewObject.tuningResultButton.button.setBackgroundColor(getResources().getColor(R.color.lightgray));

        testViewObject.explorerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testViewObject.buttons = new Button[]{testViewObject.explorerButton, testViewObject.tuningButton};
                testViewObject.resultButtons = new ResultButton[]{testViewObject.explorerResultButton, testViewObject.tuningResultButton};
                ((MainActivity)getActivity()).openExploreMenu("message_passing", testViewObject);
            }
        });
        testViewObject.explorerResultButton.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).displayTestResult("message_passing");
            }
        });
        testViewObject.tuningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testViewObject.buttons = new Button[]{testViewObject.tuningButton, testViewObject.explorerButton};
                testViewObject.resultButtons = new ResultButton[]{testViewObject.tuningResultButton, testViewObject.explorerResultButton};
                ((MainActivity)getActivity()).openTuningMenu("message_passing", testViewObject);
            }
        });
        testViewObject.tuningResultButton.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).tuningTestResult("message_passing", "Single");
            }
        });

        return fragmentView;
    }

}

