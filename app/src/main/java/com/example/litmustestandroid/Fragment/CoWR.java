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

import com.example.litmustestandroid.HelperClass.ResultButton;
import com.example.litmustestandroid.HelperClass.TestViewObject;
import com.example.litmustestandroid.MainActivity;
import com.example.litmustestandroid.R;

import org.jetbrains.annotations.NotNull;

public class CoWR extends Fragment {

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_cowr, container, false);

        TextView description = fragmentView.findViewById(R.id.cowr_description);
        description.setText(getResources().getString(R.string.cowr_description));

        TestViewObject testViewObject = new TestViewObject();

        testViewObject.testName = "cowr";

        testViewObject.explorerProgressLayout = fragmentView.findViewById(R.id.cowr_explorerProgressLayout);
        testViewObject.explorerCurrentIterationNumber = fragmentView.findViewById(R.id.cowr_explorerCurrentIterationNumber);
        testViewObject.explorerProgressLayout.setVisibility(View.GONE);

        testViewObject.tuningProgressLayout = fragmentView.findViewById(R.id.cowr_tuningProgressLayout);
        testViewObject.tuningCurrentConfigNumber = fragmentView.findViewById(R.id.cowr_tuningCurrentConfigNumber);
        testViewObject.tuningCurrentIterationNumber = fragmentView.findViewById(R.id.cowr_tuningCurrentIterationNumber);
        testViewObject.tuningProgressLayout.setVisibility(View.GONE);

        testViewObject.explorerButton = fragmentView.findViewById(R.id.cowr_explorerButton);
        testViewObject.tuningButton = fragmentView.findViewById(R.id.cowr_tuningButton);

        testViewObject.explorerResultButton = new ResultButton(fragmentView.findViewById(R.id.cowr_explorerResultButton));
        testViewObject.tuningResultButton = new ResultButton(fragmentView.findViewById(R.id.cowr_tuningResultButton));

        // Initial button state
        testViewObject.explorerResultButton.button.setEnabled(false);
        testViewObject.tuningResultButton.button.setEnabled(false);
        testViewObject.explorerResultButton.button.setBackgroundColor(getResources().getColor(R.color.lightgray));
        testViewObject.tuningResultButton.button.setBackgroundColor(getResources().getColor(R.color.lightgray));

        testViewObject. explorerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testViewObject.buttons = new Button[]{testViewObject.explorerButton, testViewObject.tuningButton};
                testViewObject.resultButtons = new ResultButton[]{testViewObject.explorerResultButton, testViewObject.tuningResultButton};
                ((MainActivity)getActivity()).openExploreMenu("cowr", testViewObject);
            }
        });
        testViewObject.explorerResultButton.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).displayTestResult("cowr");
            }
        });
        testViewObject.tuningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testViewObject.buttons = new Button[]{testViewObject.tuningButton, testViewObject.explorerButton};
                testViewObject.resultButtons = new ResultButton[]{testViewObject.tuningResultButton, testViewObject.explorerResultButton};
                ((MainActivity)getActivity()).openTuningMenu("cowr", testViewObject);
            }
        });
        testViewObject.tuningResultButton.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).tuningTestResult("cowr", "Single");
            }
        });

        return fragmentView;
    }
}

