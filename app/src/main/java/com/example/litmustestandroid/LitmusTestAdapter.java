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

public class LitmusTestAdapter extends RecyclerView.Adapter<LitmusTestAdapter.LitmusTestViewHolder>{

    String litmusTestName[];
    Context context;
    MainActivity mainActivity;

    public LitmusTestAdapter(Context ct, String testNames[], MainActivity testRun) {
        litmusTestName = testNames;
        context = ct;
        mainActivity = testRun;
    }

    @NotNull
    @Override
    public LitmusTestViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.main_recylerview_layout, parent, false);
        return new LitmusTestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull LitmusTestAdapter.LitmusTestViewHolder holder, int position) {
        String currentTestName = litmusTestName[position];
        holder.testName.setText(currentTestName);

        holder.explorerButton.setOnClickListener(new View.OnClickListener() { // Open explorer menu
            public void onClick (View v) {
                mainActivity.openExploreMenu(currentTestName, position);
            }
        });
        holder.explorerResultButton.setOnClickListener(new View.OnClickListener() { // Show Explorer Result
            public void onClick (View v) {
                mainActivity.litmusTestResult(currentTestName);
            }
        });
        holder.tuningButton.setOnClickListener(new View.OnClickListener() { // Open tuning menu
            public void onClick (View v) {
                mainActivity.openTuningMenu(currentTestName, position);
            }
        });
        holder.tuningResultButton.setOnClickListener(new View.OnClickListener() { // Show tuning result
            public void onClick (View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return litmusTestName.length;
    }

    public class LitmusTestViewHolder extends RecyclerView.ViewHolder {

        TextView testName;
        public Button explorerButton;
        public Button explorerResultButton;
        public Button tuningButton;
        public Button tuningResultButton;

        Boolean newExplorerTest = true;
        Boolean newTuningTest = true;

        public LitmusTestViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            testName = itemView.findViewById(R.id.testName);
            explorerButton = itemView.findViewById(R.id.explorerButton);
            explorerResultButton = itemView.findViewById(R.id.explorerResultButton);
            tuningButton = itemView.findViewById(R.id.tuningButton);
            tuningResultButton = itemView.findViewById(R.id.tuningResultButton);

            explorerResultButton.setEnabled(false);
            explorerResultButton.setBackgroundColor(Color.GRAY);
            tuningResultButton.setEnabled(false);
            tuningResultButton.setBackgroundColor(Color.GRAY);

        }
    }
}
