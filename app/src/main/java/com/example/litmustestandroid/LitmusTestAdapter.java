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

    public LitmusTestAdapter(Context ct, String testNames[]) {
        litmusTestName = testNames;
        context = ct;
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
                holder.startButton.setBackgroundColor(Color.GRAY);

                holder.resultButton.setEnabled(false);
                holder.resultButton.setBackgroundColor(Color.GRAY);
                MainActivity.litmusTestStart(currentTestName, holder.startButton, holder.resultButton);
            }
        });
        holder.resultButton.setOnClickListener(new View.OnClickListener() { // Show Result
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
        Button startButton;
        Button resultButton;

        public LitmusTestViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
            super(itemView);
            testName = itemView.findViewById(R.id.testName);
            startButton = itemView.findViewById(R.id.startButton);
            resultButton = itemView.findViewById(R.id.resultButton);

            resultButton.setEnabled(false);
            resultButton.setBackgroundColor(Color.GRAY);
        }
    }
}
