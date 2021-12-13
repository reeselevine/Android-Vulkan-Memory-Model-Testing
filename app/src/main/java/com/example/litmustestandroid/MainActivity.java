package com.example.litmustestandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;

import com.example.litmustestandroid.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private ActivityMainBinding binding;
    private RecyclerView litmusTestRV;
    private static final int REQUEST_PERMISSION = 10;
    private static final String TAG = "MainActivity";
    private static final String TEST_NAME[] = {"kernel_test", "vect_add", "message_passing", "load_buffer"};
    private static final int TEST_ID[] = {R.raw.kernel_test, R.raw.vect_add, R.raw.message_passing, R.raw.load_buffer};
    private static final int OUTPUT_ID[] = {R.raw.kernel_test_output, R.raw.vect_add_output, R.raw.message_passing_output, R.raw.load_buffer_output};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFileConfig(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
        else {
            //displayLitmusTests();
            tempMain();
        }

    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //displayLitmusTests();
                tempMain();
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

    private void initFileConfig(boolean forceCopy) {
        for(int i = 0; i < TEST_NAME.length; i++) {
            String fileName = TEST_NAME[i] + ".spv";
            String outputName = TEST_NAME[i] + "_output.txt";

            if (existsInFilesDir(fileName) && !forceCopy) {
                Log.d(TAG, "File: " + fileName + " already exists in " + getFilesDir().toString());
            }
            else {
                copyFile(TEST_ID[i], fileName);
                Log.d(TAG, "File: " + fileName + " copied to " + getFilesDir().toString());
            }

            if (existsInFilesDir(outputName) && !forceCopy) {
                Log.d(TAG, "File: " + outputName + " already exists in " + getFilesDir().toString());
            }
            else {
                copyFile(OUTPUT_ID[i], outputName);
                Log.d(TAG, "File: " + outputName + " copied to " + getFilesDir().toString());
            }

        }
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
        //litmusTestRV = findViewById(R.id.litmusTestRecyclerView);

        LitmusTestAdapter litmusTestAdapter = new LitmusTestAdapter(this, litmusTestName);
        litmusTestRV.setAdapter(litmusTestAdapter);
        litmusTestRV.setLayoutManager(new LinearLayoutManager(this));
    }

    public static void litmusTestStart(String testName, Button startButton, Button resultButton) {
        Log.i("TEST", testName + " PRESSED");
        // Call main() with testName

        // Update Start and Result Button (Temporary for testing)
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startButton.setEnabled(true);
                startButton.setBackgroundColor(Color.GREEN);

                resultButton.setEnabled(true);
                resultButton.setBackgroundColor(Color.RED);
            }
        }, 1000);
    }

    public void tempMain() {
        main();
    }

    public String getFileDir() {
        return getFilesDir().toString();
    }

    public native int main();
}