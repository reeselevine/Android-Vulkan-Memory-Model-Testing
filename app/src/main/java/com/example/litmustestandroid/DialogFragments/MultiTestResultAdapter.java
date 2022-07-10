package com.example.litmustestandroid.DialogFragments;

import android.content.Context;
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

public class MultiTestResultAdapter extends RecyclerView.Adapter<MultiTestResultAdapter.MultiTestResultViewHolder>{

    String testNames[];
    Context context;
    String testType;

    public MultiTestResultAdapter(String[] testNames, Context ct, String testType) {
        this.testNames = testNames;
        this.context = ct;
        this.testType = testType;
    }

    @NotNull
    @Override
    public MultiTestResultViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.multi_test_result_recylcerview_layout, parent, false);
        return new MultiTestResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MultiTestResultAdapter.MultiTestResultViewHolder holder, int position) {
        String currentTestName = testNames[position];
        holder.testName.setText(currentTestName);

        holder.resultButton.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                // call main activity multi test result function
                if (testType.equals("Explorer")) { // Explorer
                    ((MainActivity)context).displayTestResult(currentTestName);
                }
                else { // Tuning
                    ((MainActivity)context).tuningTestResult(currentTestName);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return testNames.length;
    }

    public class MultiTestResultViewHolder extends RecyclerView.ViewHolder {

        public TextView testName;
        public Button resultButton;

        public MultiTestResultViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            testName = itemView.findViewById(R.id.multiTestName);
            resultButton = itemView.findViewById(R.id.multiTestResultOutputButton);
        }
    }
}
