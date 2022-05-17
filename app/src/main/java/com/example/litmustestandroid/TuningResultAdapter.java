package com.example.litmustestandroid;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TuningResultAdapter extends RecyclerView.Adapter<TuningResultAdapter.TuningResultViewHolder>{

    String testName;
    ArrayList<TuningResultCase> tuningResultCases;
    Context context;
    MainActivity mainActivity;

    public TuningResultAdapter(String testName, Context ct, ArrayList<TuningResultCase> tuningResultCases, MainActivity mainActivity) {
        this.testName = testName;
        this.tuningResultCases = tuningResultCases;
        this.context = ct;
        this.mainActivity = mainActivity;
    }

    @NotNull
    @Override
    public TuningResultViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.tuning_result_recylerview_layout, parent, false);
        return new TuningResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull TuningResultAdapter.TuningResultViewHolder holder, int position) {
        holder.testNum.setText(Integer.toString(position + 1));

        holder.paramButton.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                ExplorerResultDialogFragment paramDialog = new ExplorerResultDialogFragment();

                StringBuilder sb = new StringBuilder();
                sb.append("Test " + Integer.toString(position + 1) + ":\n");
                sb.append(tuningResultCases.get(position).parameters);
                paramDialog.setText(sb);
                paramDialog.show(mainActivity.getSupportFragmentManager(), "TuningResultParamDialog");
            }
        });
        holder.resultButton.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                ExplorerResultDialogFragment outputDialog = new ExplorerResultDialogFragment();

                StringBuilder sb = new StringBuilder();
                sb.append("Test " + Integer.toString(position + 1) + " ");
                sb.append(tuningResultCases.get(position).results);
                outputDialog.setText(sb);
                outputDialog.show(mainActivity.getSupportFragmentManager(), "TuningResultOutputDialog");
            }
        });
    }

    @Override
    public int getItemCount() {
        return tuningResultCases.size();
    }

    public class TuningResultViewHolder extends RecyclerView.ViewHolder {

        TextView testNum;
        public Button paramButton;
        public Button resultButton;

        public TuningResultViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            testNum = itemView.findViewById(R.id.tuningTestNum);
            paramButton = itemView.findViewById(R.id.tuningResultParamButton);
            resultButton = itemView.findViewById(R.id.tuningResultOutputButton);

        }
    }
}
