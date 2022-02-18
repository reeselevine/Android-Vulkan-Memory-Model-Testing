package com.example.litmustestandroid;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class TestRunnerAdapter extends RecyclerView.Adapter<TestRunnerAdapter.TestRunnerViewHolder>{

    String litmusTestName[];
    Context context;
    TestRunner testRunner;

    public TestRunnerAdapter(Context ct, String testNames[], TestRunner testRun) {
        litmusTestName = testNames;
        context = ct;
        testRunner = testRun;
    }

    @NotNull
    @Override
    public TestRunnerViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.testrunner_recylerview_layout, parent, false);
        return new TestRunnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull TestRunnerAdapter.TestRunnerViewHolder holder, int position) {
        String currentTestName = litmusTestName[position];
        holder.testName.setText(currentTestName);

        holder.optionButton.setOnClickListener(new View.OnClickListener() { // Open option menu
            public void onClick (View v) {
                testRunner.openOptionMenu(currentTestName, position);
            }
        });
        holder.resultButton.setOnClickListener(new View.OnClickListener() { // Show Result
            public void onClick (View v) {
                testRunner.litmusTestResult(currentTestName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return litmusTestName.length;
    }

    public class TestRunnerViewHolder extends RecyclerView.ViewHolder {

        TextView testName;
        public Button optionButton;
        public Button resultButton;
        Boolean newTest = true;

        public TestRunnerViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            testName = itemView.findViewById(R.id.testName);
            optionButton = itemView.findViewById(R.id.optionButton);
            resultButton = itemView.findViewById(R.id.resultButton);

            // Temporary
            // Look for output file
            // If exists, set it enabled.
            resultButton.setEnabled(false);
            resultButton.setBackgroundColor(Color.GRAY);

        }
    }
}
