package com.example.litmustestandroid;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.litmustestandroid.databinding.ActivityTestrunnerBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestRunner extends AppCompatActivity {

    static {
        System.loadLibrary("testRunner-main-lib");
    }

    private ActivityTestrunnerBinding binding;
    private RecyclerView testRunnerRV;
    private TestRunnerAdapter testRunnerAdapter;

    private static final String TAG = "TestRunner";
    private static final String TEST_NAME[] = {"parallel_message_passing"};
    private static final String SHADER_NAME[] = {"parallel_message_passing", "parallel_message_passing_strong"};
    private static final String PARAMETER_NAME[] = {"parallel_basic_parameters", "parallel_stress_parameters"};
    private static final int SHADER_ID[] = {R.raw.parallel_message_passing, R.raw.parallel_message_passing_strong};
    private static final int RESULT_ID[] = {R.raw.parallel_message_passing_results};
    private static final int OUTPUT_ID[] = {R.raw.parallel_message_passing_output};
    private static final int PARAMETER_ID[] = {R.raw.parallel_basic_parameters, R.raw.parallel_stress_parameters};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTestrunnerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFileConfig();

        binding.litmusTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TestRunner.this, MainActivity.class);
                startActivity(i);
            }
        });

        displayLitmusTests();
    }

    private boolean existsInFilesDir(String fileName) {
        File file = new File(getFilesDir(), fileName);

        if(file.exists()) return true;

        return false;
    }

    private void copyFile(int fromResId, String toFile) {
        InputStream is =  getResources().openRawResource(fromResId);
        byte[] buffer = new byte[4096];
        try
        {
            FileOutputStream fos = openFileOutput(toFile, Context.MODE_PRIVATE);

            int bytes_read;
            while ((bytes_read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytes_read);
            }

            fos.close();
            is.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void initFileConfig() {
        // Transfer shader files
        for(int i = 0; i < SHADER_NAME.length; i++) {
            String shaderName = SHADER_NAME[i] + ".spv";

            copyFile(SHADER_ID[i], shaderName);
            Log.d(TAG, "File: " + shaderName + " copied to " + getFilesDir().toString());
        }

        // Transfer output and result shader files
        for(int i = 0; i < TEST_NAME.length; i++) {
            String resultName  = TEST_NAME[i] + "_results.spv";
            String outputName = TEST_NAME[i] + "_output.txt";

            copyFile(RESULT_ID[i], resultName);
            Log.d(TAG, "File: " + resultName  + " copied to " + getFilesDir().toString());

            copyFile(OUTPUT_ID[i], outputName);
            Log.d(TAG, "File: " + outputName + " copied to " + getFilesDir().toString());
        }

        // Transfer parameter files
        for(int i = 0; i < PARAMETER_NAME.length; i++) {
            String parameterName = PARAMETER_NAME[i] + ".txt";

            copyFile(PARAMETER_ID[i], parameterName);
            Log.d(TAG, "File: " + parameterName  + " copied to " + getFilesDir().toString());
        }
    }

    public void displayLitmusTests() {
        String litmusTestName[] = getResources().getStringArray(R.array.testRunner);
        testRunnerRV = findViewById(R.id.testRunnerRecyclerView);

        testRunnerAdapter = new TestRunnerAdapter(this, litmusTestName, TestRunner.this);
        testRunnerRV.setAdapter(testRunnerAdapter);
        testRunnerRV.setLayoutManager(new LinearLayoutManager(this));

        String[] testArray = {"parallel_message_passing", "parallel_message_passing",
                            "parallel_message_passing_results", "parallel_basic_parameters"};
        main(testArray);
    }

    public String getFileDir() {
        return getFilesDir().toString();
    }

    public native int main(String[] testName);
}
