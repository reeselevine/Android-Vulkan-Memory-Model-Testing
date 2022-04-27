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

public class old_LitmusTestAdapter extends RecyclerView.Adapter<old_LitmusTestAdapter.old_LitmusTestViewHolder>{

    String litmusTestName[];
    Context context;
    old_MainActivity mainActivity;

    public old_LitmusTestAdapter(Context ct, String testNames[], old_MainActivity mainAct) {
        litmusTestName = testNames;
        context = ct;
        mainActivity = mainAct;
    }

    @NotNull
    @Override
    public old_LitmusTestViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.old_main_recylerview_layout, parent, false);
        return new old_LitmusTestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull old_LitmusTestAdapter.old_LitmusTestViewHolder holder, int position) {
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

    public class old_LitmusTestViewHolder extends RecyclerView.ViewHolder {

        TextView testName;
        public Button startButton;
        public Button resultButton;
        Boolean newTest = true;

        public old_LitmusTestViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
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
