package com.example.litmustestandroid;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.litmustestandroid.databinding.ActivityTestrunnerBinding;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TestRunner extends AppCompatActivity {

    static {
        System.loadLibrary("testRunner-main-lib");
    }

    private ActivityTestrunnerBinding binding;
    private RecyclerView testRunnerRV;
    private TestRunnerAdapter testRunnerAdapter;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog optionDialog;
    private TextView optionTestName;
    private EditText[] parameters = new EditText[20];
    private Button startButton, closeButton, defaultParamButton, stressParamButton;

    private static final String TAG = "TestRunner";
    private static final String TEST_NAME[] = {"parallel_message_passing"};
    private static final String SHADER_NAME[] = {"parallel_message_passing", "parallel_message_passing_strong"};
    private static final String PARAMETER_NAME[] = {"parallel_basic_parameters", "parallel_stress_parameters"};
    private static final int SHADER_ID[] = {R.raw.parallel_message_passing, R.raw.parallel_message_passing_strong};
    private static final int RESULT_ID[] = {R.raw.parallel_message_passing_results};
    private static final int OUTPUT_ID[] = {R.raw.parallel_message_passing_output};
    private static final int TEST_PARAM_ID[] = {R.raw.parallel_message_passing_parameters};
    private static final int PARAMETER_ID[] = {R.raw.parallel_basic_parameters, R.raw.parallel_stress_parameters};

    private Handler handler = new Handler();

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

        // Transfer output, parameters, and result shader files
        for(int i = 0; i < TEST_NAME.length; i++) {
            String resultName  = TEST_NAME[i] + "_results.spv";
            String outputName = TEST_NAME[i] + "_output.txt";
            String testParamName = TEST_NAME[i] + "_parameters.txt";

            copyFile(RESULT_ID[i], resultName);
            Log.d(TAG, "File: " + resultName  + " copied to " + getFilesDir().toString());

            copyFile(OUTPUT_ID[i], outputName);
            Log.d(TAG, "File: " + outputName + " copied to " + getFilesDir().toString());

            copyFile(TEST_PARAM_ID[i], testParamName);
            Log.d(TAG, "File: " + testParamName + " copied to " + getFilesDir().toString());
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
    }

    // Automatically fill parameters with basic values
    public void loadParameters(int paramValue){
        InputStream inputStream = getResources().openRawResource(paramValue);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        int index = 0;

        try {
            String line = bufferedReader.readLine();
            while (line != null) {
                String[] words = line.split("=");
                if((!words[0].equals("numMemLocations") && !words[0].equals("numOutputs")) && index < parameters.length) {
                    parameters[index].setText(words[1]);
                    index++;
                }
                line = bufferedReader.readLine();
            }
            inputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Read and write current parameters value for testing
    public void writeParameters(String testName) {
        InputStream inputStream = getResources().openRawResource(R.raw.parallel_basic_parameters);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String fileName = "parallel_" + testName + "_parameters.txt";
        try{
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            String line = bufferedReader.readLine();
            int index = 0;

            while (line != null) {
                String[] words = line.split("=");
                String outputNumber;
                String newLine = "\n";

                if(words[0].equals("numMemLocations") || words[0].equals("numOutputs")
                || words[0].equals("aliasedMemory") || words[0].equals("gpuDeviceId")) {
                    if(words[0].equals("gpuDeviceId")) {
                        newLine = "";
                    }
                    outputNumber = words[1];
                }
                else {
                    outputNumber = parameters[index].getText().toString();
                    index++;
                }


                String outputLine = words[0] + "=" + outputNumber + newLine;
                fos.write(outputLine.getBytes());

                line = bufferedReader.readLine();
            }
            inputStream.close();
            fos.close();
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

    public void enableNonRunningTests(int position, boolean enabled){
        int childCount = testRunnerRV.getChildCount();
        for(int i = 0; i < childCount; i++) {
            final TestRunnerAdapter.TestRunnerViewHolder viewHolder = (TestRunnerAdapter.TestRunnerViewHolder) testRunnerRV.getChildViewHolder(testRunnerRV.getChildAt(i));
            if(i != position) {
                viewHolder.optionButton.setEnabled(enabled);
                viewHolder.resultButton.setEnabled(enabled);

                if(enabled) {
                    viewHolder.optionButton.setBackgroundColor(Color.GREEN);
                    if(viewHolder.newTest) { // If Test is still new, stay GRAY
                        viewHolder.resultButton.setEnabled(false);
                        viewHolder.resultButton.setBackgroundColor(Color.GRAY);
                    }
                    else { // If this Test has result existing, turn RED
                        viewHolder.resultButton.setBackgroundColor(Color.RED);
                    }
                }
                else {
                    viewHolder.optionButton.setBackgroundColor(Color.GRAY);
                    viewHolder.resultButton.setBackgroundColor(Color.GRAY);
                }
            }
        }
    }

    public void openOptionMenu(String testName, int position) {
        Log.i("TEST", testName + " PRESSED, OPENING OPTION MENU");

        dialogBuilder = new AlertDialog.Builder(this);
        final View optionMenuView = getLayoutInflater().inflate(R.layout.parallel_test_option, null);

        optionTestName = (TextView) optionMenuView.findViewById(R.id.parallelTestName);
        optionTestName.setText(testName);

        parameters[0] = (EditText) optionMenuView.findViewById(R.id.parallelTestIteration); // testIteration
        parameters[1] = (EditText) optionMenuView.findViewById(R.id.parallelTestingWorkgroups); // testingWorkgroups
        parameters[2] = (EditText) optionMenuView.findViewById(R.id.parallelMaxWorkgroups); // maxWorkgroups
        parameters[3] = (EditText) optionMenuView.findViewById(R.id.parallelMinWorkgroupSize); // minWorkgroupSize
        parameters[4] = (EditText) optionMenuView.findViewById(R.id.parallelMaxWorkgroupSize); // maxWorkgroupSize
        parameters[5] = (EditText) optionMenuView.findViewById(R.id.parallelShufflePct); // shufflePct
        parameters[6] = (EditText) optionMenuView.findViewById(R.id.parallelBarrierPct); // barrierPct
        parameters[7] = (EditText) optionMenuView.findViewById(R.id.parallelScratchMemorySize); // scratchMemorySize
        parameters[8] = (EditText) optionMenuView.findViewById(R.id.parallelMemoryStride); // memStride
        parameters[9] = (EditText) optionMenuView.findViewById(R.id.parallelMemoryStressPct); // memStressPct
        parameters[10] = (EditText) optionMenuView.findViewById(R.id.parallelMemoryStressIterations); // memStressIterations
        parameters[11] = (EditText) optionMenuView.findViewById(R.id.parallelMemOryStressPattern); // memStressPattern
        parameters[12] = (EditText) optionMenuView.findViewById(R.id.parallelPreStressPct); // preStressPct
        parameters[13] = (EditText) optionMenuView.findViewById(R.id.parallelPreStressIterations); // preStressIterations
        parameters[14] = (EditText) optionMenuView.findViewById(R.id.parallelPreStressPattern); // preStressPattern
        parameters[15] = (EditText) optionMenuView.findViewById(R.id.parallelStressLineSize); // stressLineSize
        parameters[16] = (EditText) optionMenuView.findViewById(R.id.parallelStressTargetLines); // stressTargetLines
        parameters[17] = (EditText) optionMenuView.findViewById(R.id.parallelStressAssignmentStrategy); // stressAssignmentStrategy
        parameters[18] = (EditText) optionMenuView.findViewById(R.id.parallelPermuteFirst); // permuteFirst
        parameters[19] = (EditText) optionMenuView.findViewById(R.id.parallelPermuteSecond); // permuteSecond

        loadParameters(R.raw.parallel_basic_parameters);

        startButton = (Button) optionMenuView.findViewById(R.id.parallelStartButton);
        closeButton = (Button) optionMenuView.findViewById(R.id.parallelCloseButton);
        defaultParamButton = (Button) optionMenuView.findViewById(R.id.parallelDefaultParamButton);
        stressParamButton = (Button) optionMenuView.findViewById(R.id.parallelStressParamButton);

        dialogBuilder.setView(optionMenuView);
        optionDialog = dialogBuilder.create();

        // Prevent from dialog closing when touching outside
        optionDialog.setCanceledOnTouchOutside(false);

        optionDialog.show();

        // Load default parameter
        defaultParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultParamButton.setBackgroundColor(Color.CYAN);
                loadParameters(R.raw.parallel_basic_parameters);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        defaultParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                    }
                }, 200);
            }
        });

        // Load stress parameter
        stressParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stressParamButton.setBackgroundColor(Color.CYAN);
                loadParameters(R.raw.parallel_stress_parameters);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stressParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                    }
                }, 200);
            }
        });

        // Start test
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TEST", testName + " STARTING");
                final TestRunnerAdapter.TestRunnerViewHolder viewHolder = (TestRunnerAdapter.TestRunnerViewHolder) testRunnerRV.getChildViewHolder(testRunnerRV.getChildAt(position));
                writeParameters(testName);
                optionDialog.dismiss();

                viewHolder.optionButton.setEnabled(false);
                viewHolder.optionButton.setBackgroundColor(Color.BLUE);

                enableNonRunningTests(position, false);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String[] testArgument = new String[4];
                        testArgument[0] = "parallel_" + testName;
                        testArgument[1] = "parallel_" + testName;
                        testArgument[2] = "parallel_" + testName + "_results";
                        testArgument[3] = "parallel_" + testName + "_parameters";
                        main(testArgument);

                        // Update Start and Result Button
                        viewHolder.optionButton.setEnabled(true);
                        viewHolder.optionButton.setBackgroundColor(Color.GREEN);

                        viewHolder.resultButton.setEnabled(true);
                        viewHolder.resultButton.setBackgroundColor(Color.RED);

                        viewHolder.newTest = false;

                        enableNonRunningTests(position, true);

                        Toast.makeText(TestRunner.this, "Test " + testName + " finished!", Toast.LENGTH_LONG).show();
                    }
                }, 500);
            }
        });

        // Close menu
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TEST", testName + " MENU CLOSING");
                optionDialog.dismiss();
            }
        });

    }

    public void litmusTestResult(String testName) {
        Log.i("RESULT", testName + " PRESSED");

        ResultDialogFragment dialog = new ResultDialogFragment();

        try
        {
            FileInputStream fis = openFileInput("parallel_" + testName + "_output.txt");
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

    public native int main(String[] testName);
}
