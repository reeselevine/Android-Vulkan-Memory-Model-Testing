package com.example.litmustestandroid;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.litmustestandroid.databinding.ActivityTestrunnerBinding;

public class TestRunner extends AppCompatActivity {

    private ActivityTestrunnerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTestrunnerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.litmusTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TestRunner.this, MainActivity.class);
                startActivity(i);
            }
        });

    }
}
