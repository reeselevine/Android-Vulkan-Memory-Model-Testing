package com.example.litmustestandroid.DialogFragments;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.litmustestandroid.MainActivity;
import com.example.litmustestandroid.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ConformanceTestResultAdapter extends RecyclerView.Adapter<ConformanceTestResultAdapter.ConformanceTestResultViewHolder>{

    Context context;
    ArrayList<ConformanceResultCase> conformanceResultCases;

    public ConformanceTestResultAdapter(Context ct, ArrayList<ConformanceResultCase> conformanceResultCases) {
        this.context = ct;
        this.conformanceResultCases = conformanceResultCases;
    }

    @NotNull
    @Override
    public ConformanceTestResultViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.conformance_test_result_recylcerview_layout, parent, false);
        return new ConformanceTestResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ConformanceTestResultAdapter.ConformanceTestResultViewHolder holder, int position) {
        String currentTestName = conformanceResultCases.get(position).testName;
        holder.testName.setText(currentTestName);

        // Display test result
        if(conformanceResultCases.get(position).violated) {
            holder.testResult.setText("Failed");
            holder.testResult.setTextColor(Color.RED);
        }
        else {
            holder.testResult.setText("Passed");
            holder.testResult.setTextColor(Color.GREEN);
        }

        holder.resultButton.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                TestResultDialogFragment outputDialog = new TestResultDialogFragment();

                StringBuilder sb = new StringBuilder();
                sb.append(conformanceResultCases.get(position).results);
                outputDialog.setText(sb);
                outputDialog.show(((MainActivity)context).getSupportFragmentManager(), "ConformanceResultOutputDialog");
            }
        });
    }

    @Override
    public int getItemCount() {
        return conformanceResultCases.size();
    }

    public class ConformanceTestResultViewHolder extends RecyclerView.ViewHolder {

        public TextView testName;
        public TextView testResult;
        public Button resultButton;

        public ConformanceTestResultViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            testName = itemView.findViewById(R.id.conformanceTestName);
            testResult = itemView.findViewById(R.id.conformanceTestResult);
            resultButton = itemView.findViewById(R.id.conformanceTestResultOutputButton);
        }
    }
}
