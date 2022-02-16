package com.example.litmustestandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.litmustestandroid.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("litmusTest-main-lib");
    }

    private ActivityMainBinding binding;
    private RecyclerView litmusTestRV;
    private LitmusTestAdapter litmusTestAdapter;
    private static final int REQUEST_PERMISSION = 10;
    private static final String TAG = "MainActivity";
    private static final String TEST_NAME[] = {"corr", "corr4", "corw1", "corw1_nostress", "iriw",
                                               "isa2", "kernel_test", "load_buffer",
                                               "message_passing", "store_buffer", "vect_add"};
    private static final int TEST_ID[] = {R.raw.corr, R.raw.corr4, R.raw.corw1, R.raw.corw1_nostress, R.raw.iriw, R.raw.isa2,
                                          R.raw.kernel_test, R.raw.load_buffer, R.raw.message_passing,
                                          R.raw.store_buffer, R.raw.vect_add};
    private static final int OUTPUT_ID[] = {R.raw.corr_output, R.raw.corr4_output, R.raw.corw1_output, R.raw.corw1_nostress_output,
                                            R.raw.iriw_output, R.raw.isa2_output, R.raw.kernel_test_output,
                                            R.raw.load_buffer_output, R.raw.message_passing_output,
                                            R.raw.store_buffer_output, R.raw.vect_add_output};

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFileConfig();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
        else {
            displayLitmusTests();
        }

        binding.testRunnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, TestRunner.class);
                startActivity(i);
            }
        });

        Intent i = new Intent(MainActivity.this, TestRunner.class);
        startActivity(i);

    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayLitmusTests();
            } else {
                // User refused to grant permission.
            }
        }
    }

    private boolean existsInFilesDir(String fileName) {
        File file = new File(getFilesDir(), fileName);

        if(file.exists()) return true;

        return false;
    }

    private void initFileConfig() {
        for(int i = 0; i < TEST_NAME.length; i++) {
            String fileName = TEST_NAME[i] + ".spv";
            String outputName = TEST_NAME[i] + "_output.txt";

            copyFile(TEST_ID[i], fileName);
            Log.d(TAG, "File: " + fileName + " copied to " + getFilesDir().toString());
            copyFile(OUTPUT_ID[i], outputName);
            Log.d(TAG, "File: " + outputName + " copied to " + getFilesDir().toString());
        }
        copyFile(R.raw.debug, "debug.txt");
        Log.d(TAG, "File: debug.txt copied to " + getFilesDir().toString());
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

    public void displayLitmusTests() {
        String litmusTestName[] = getResources().getStringArray(R.array.litmusTests);
        litmusTestRV = findViewById(R.id.litmusTestRecyclerView);

        litmusTestAdapter = new LitmusTestAdapter(this, litmusTestName, MainActivity.this);
        litmusTestRV.setAdapter(litmusTestAdapter);
        litmusTestRV.setLayoutManager(new LinearLayoutManager(this));
    }

    public void enableNonRunningTests(int position, boolean enabled){
        int childCount = litmusTestRV.getChildCount();
        for(int i = 0; i < childCount; i++) {
            final LitmusTestAdapter.LitmusTestViewHolder viewHolder = (LitmusTestAdapter.LitmusTestViewHolder) litmusTestRV.getChildViewHolder(litmusTestRV.getChildAt(i));
            if(i != position) {
                viewHolder.startButton.setEnabled(enabled);
                viewHolder.resultButton.setEnabled(enabled);

                if(enabled) {
                    viewHolder.startButton.setBackgroundColor(Color.GREEN);
                    if(viewHolder.newTest) { // If Test is still new, stay GRAY
                        viewHolder.resultButton.setEnabled(false);
                        viewHolder.resultButton.setBackgroundColor(Color.GRAY);
                    }
                    else { // If this Test has result existing, turn RED
                        viewHolder.resultButton.setBackgroundColor(Color.RED);
                    }
                }
                else {
                    viewHolder.startButton.setBackgroundColor(Color.GRAY);
                    viewHolder.resultButton.setBackgroundColor(Color.GRAY);
                }
            }
        }
    }

    public void litmusTestStart(String testName, int position) {
        Log.i("TEST", testName + " PRESSED");
        enableNonRunningTests(position, false);

        final LitmusTestAdapter.LitmusTestViewHolder viewHolder = (LitmusTestAdapter.LitmusTestViewHolder) litmusTestRV.getChildViewHolder(litmusTestRV.getChildAt(position));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Call main() with testName
                main(testName);

                // Update Start and Result Button
                viewHolder.startButton.setEnabled(true);
                viewHolder.startButton.setBackgroundColor(Color.GREEN);

                viewHolder.resultButton.setEnabled(true);
                viewHolder.resultButton.setBackgroundColor(Color.RED);

                viewHolder.newTest = false;

                enableNonRunningTests(position, true);

                Toast.makeText(MainActivity.this, "Test " + testName + " finished!", Toast.LENGTH_LONG).show();
            }
        }, 500);
    }

    public void litmusTestResult(String testName) {
        Log.i("RESULT", testName + " PRESSED");

        ResultDialogFragment dialog = new ResultDialogFragment();

        try
        {
            FileInputStream fis = openFileInput(testName + "_output.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }
            dialog.setText(sb);
            Log.d(TAG, sb.toString());

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        dialog.show(getSupportFragmentManager(), "ResultDialog");
    }

    public String getFileDir() {
        return getFilesDir().toString();
    }

    public native int main(String testName);
}