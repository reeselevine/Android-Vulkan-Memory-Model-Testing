package com.example.litmustestandroid;

import android.Manifest;
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

    public LitmusTestAdapter(Context ct, String testNames[], MainActivity mainAct) {
        litmusTestName = testNames;
        context = ct;
        mainActivity = mainAct;
    }

    @NotNull
    @Override
    public LitmusTestViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recylerview_layout, parent, false);
        return new LitmusTestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull LitmusTestAdapter.LitmusTestViewHolder holder, int position) {
        String currentTestName = litmusTestName[position];

        holder.testName.setText(currentTestName);

        holder.startButton.setOnClickListener(new View.OnClickListener() { // Start Test
            public void onClick (View v) {
                holder.startButton.setEnabled(false);
                holder.startButton.setBackgroundColor(Color.BLUE);

                holder.resultButton.setEnabled(false);
                holder.resultButton.setBackgroundColor(Color.GRAY);
                mainActivity.litmusTestStart(currentTestName, position);

            }
        });
        holder.resultButton.setOnClickListener(new View.OnClickListener() { // Show Result
            public void onClick (View v) {
                mainActivity.litmusTestResult(currentTestName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return litmusTestName.length;
    }

    public class LitmusTestViewHolder extends RecyclerView.ViewHolder {

        TextView testName;
        public Button startButton;
        public Button resultButton;
        Boolean newTest = true;

        public LitmusTestViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
            super(itemView);
            testName = itemView.findViewById(R.id.testName);
            startButton = itemView.findViewById(R.id.startButton);
            resultButton = itemView.findViewById(R.id.resultButton);

            // Temporary
            // Look for output file
            // If exists, set it enabled.
            resultButton.setEnabled(false);
            resultButton.setBackgroundColor(Color.GRAY);

        }
    }
}