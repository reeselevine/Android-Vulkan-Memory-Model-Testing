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
import java.util.ArrayList;

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
    private Button startButton, closeButton, defaultParamButton, stressParamButton, defaultShaderButton, strongShaderButton;

    private static final String TAG = "TestRunner";

    private Handler handler = new Handler();

    private String shaderType = "default";

    private static final int NUMTESTS = 3;
    private TestCase[] testCases = new TestCase[NUMTESTS];

    public ArrayList<String> totalShaderNames = new ArrayList<String>();
    public ArrayList<Integer> totalShaderIds = new ArrayList<Integer>();

    public ArrayList<String> totalResultNames = new ArrayList<String>();
    public ArrayList<Integer> totalResultIds = new ArrayList<Integer>();

    public ArrayList<String> totalOutputNames = new ArrayList<String>();
    public ArrayList<Integer> totalOutputIds = new ArrayList<Integer>();

    public ArrayList<String> totalTestParamNames = new ArrayList<String>();
    public ArrayList<Integer> totalTestParamIds = new ArrayList<Integer>();

    public ArrayList<String> totalParamNames = new ArrayList<String>();
    public ArrayList<Integer> totalParamIds = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTestrunnerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registerTestCases();
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

    private void registerTestCases() {
        // Message Passing
        TestCase messagePassing = new TestCase();
        messagePassing.testType = "weakMemory";
        messagePassing.testName = "messagePassing";
        messagePassing.setShaderNames(new String[]{"parallel_message_passing.spv", "parallel_message_passing_strong.spv"}, totalShaderNames);
        messagePassing.setShaderIds(new int[]{R.raw.parallel_message_passing, R.raw.parallel_message_passing_strong}, totalShaderIds);
        messagePassing.setResultNames(new String[]{"parallel_message_passing_results.spv"}, totalResultNames);
        messagePassing.setResultIds(new int[]{R.raw.parallel_message_passing_results}, totalResultIds);
        messagePassing.setOutputName("parallel_message_passing_output.txt", totalOutputNames);
        messagePassing.setOutputId(R.raw.parallel_message_passing_output, totalOutputIds);
        messagePassing.setTestParamName("parallel_message_passing_parameters.txt", totalTestParamNames);
        messagePassing.setTestParamId(R.raw.parallel_message_passing_parameters, totalTestParamIds);
        messagePassing.setParamNames(new String[]{"parallel_basic_parameters.txt", "parallel_stress_parameters.txt"}, totalParamNames);
        messagePassing.setParamIds(new int[]{R.raw.parallel_basic_parameters, R.raw.parallel_stress_parameters}, totalParamIds);
        testCases[0] = messagePassing;

        // Load Buffer
        TestCase loadBuffer = new TestCase();
        loadBuffer.testType = "weakMemory";
        loadBuffer.testName = "loadBuffer";
        loadBuffer.setShaderNames(new String[]{"parallel_load_buffer.spv"}, totalShaderNames);
        loadBuffer.setShaderIds(new int[]{R.raw.parallel_load_buffer}, totalShaderIds);
        loadBuffer.setResultNames(new String[]{"parallel_load_buffer_results.spv"}, totalResultNames);
        loadBuffer.setResultIds(new int[]{R.raw.parallel_load_buffer_results}, totalResultIds);
        loadBuffer.setOutputName("parallel_load_buffer_output.txt", totalOutputNames);
        loadBuffer.setOutputId(R.raw.parallel_load_buffer_output, totalOutputIds);
        loadBuffer.setTestParamName("parallel_load_buffer_parameters.txt", totalTestParamNames);
        loadBuffer.setTestParamId(R.raw.parallel_load_buffer_parameters, totalTestParamIds);
        loadBuffer.setParamNames(new String[]{"parallel_basic_parameters.txt", "parallel_stress_parameters.txt"}, totalParamNames);
        loadBuffer.setParamIds(new int[]{R.raw.parallel_basic_parameters, R.raw.parallel_stress_parameters}, totalParamIds);
        testCases[1] = loadBuffer;

        // Store Buffer
        TestCase storeBuffer = new TestCase();
        storeBuffer.testType = "weakMemory";
        storeBuffer.testName = "storeBuffer";
        storeBuffer.setShaderNames(new String[]{"parallel_store_buffer.spv"}, totalShaderNames);
        storeBuffer.setShaderIds(new int[]{R.raw.parallel_store_buffer}, totalShaderIds);
        storeBuffer.setResultNames(new String[]{"parallel_store_buffer_results.spv"}, totalResultNames);
        storeBuffer.setResultIds(new int[]{R.raw.parallel_store_buffer_results}, totalResultIds);
        storeBuffer.setOutputName("parallel_store_buffer_output.txt", totalOutputNames);
        storeBuffer.setOutputId(R.raw.parallel_store_buffer_output, totalOutputIds);
        storeBuffer.setTestParamName("parallel_store_buffer_parameters.txt", totalTestParamNames);
        storeBuffer.setTestParamId(R.raw.parallel_store_buffer_parameters, totalTestParamIds);
        storeBuffer.setParamNames(new String[]{"parallel_basic_parameters.txt", "parallel_stress_parameters.txt"}, totalParamNames);
        storeBuffer.setParamIds(new int[]{R.raw.parallel_basic_parameters, R.raw.parallel_stress_parameters}, totalParamIds);
        testCases[2] = storeBuffer;
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
        // These variables must have same size since they depend on each other
        if (totalShaderNames.size() != totalShaderIds.size()) {
            Log.e(TAG, "initFileConfig: totalShaderNames.size do not match totalShaderIds.size");
        }
        if (totalResultNames.size() != totalResultIds.size()) {
            Log.e(TAG, "initFileConfig: totalResultNames.size do not match totalResultIds.size");
        }
        if (totalOutputNames.size() != totalOutputIds.size()) {
            Log.e(TAG, "initFileConfig: totalOutputNames.size do not match totalOutputIds.size");
        }
        if (totalTestParamNames.size() != totalTestParamIds.size()) {
            Log.e(TAG, "initFileConfig: totalTestParamNames.size do not match totalTestParamIds.size");
        }
        if (totalParamNames.size() != totalParamIds.size()) {
            Log.e(TAG, "initFileConfig: totalParamNames.size do not match totalParamIds.size");
        }

        // Transfer shader files
        for(int i = 0; i < totalShaderNames.size(); i++) {
            copyFile(totalShaderIds.get(i), totalShaderNames.get(i));
            Log.d(TAG, "File: " + totalShaderNames.get(i) + " copied to " + getFilesDir().toString());
        }

        // Transfer result files
        for(int i = 0; i < totalResultNames.size(); i++) {
            copyFile(totalResultIds.get(i), totalResultNames.get(i));
            Log.d(TAG, "File: " + totalResultNames.get(i) + " copied to " + getFilesDir().toString());
        }

        // Transfer output files
        for(int i = 0; i < totalOutputNames.size(); i++) {
            copyFile(totalOutputIds.get(i), totalOutputNames.get(i));
            Log.d(TAG, "File: " + totalOutputNames.get(i) + " copied to " + getFilesDir().toString());
        }

        // Transfer test param files
        for(int i = 0; i < totalTestParamNames.size(); i++) {
            copyFile(totalTestParamIds.get(i), totalTestParamNames.get(i));
            Log.d(TAG, "File: " + totalTestParamNames.get(i) + " copied to " + getFilesDir().toString());
        }

        // Transfer param files
        for(int i = 0; i < totalParamNames.size(); i++) {
            copyFile(totalParamIds.get(i), totalParamNames.get(i));
            Log.d(TAG, "File: " + totalParamNames.get(i) + " copied to " + getFilesDir().toString());
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
        defaultShaderButton = (Button) optionMenuView.findViewById(R.id.parallelDefaultShaderButton);
        strongShaderButton = (Button) optionMenuView.findViewById(R.id.parallelStrongShaderButton);

        dialogBuilder.setView(optionMenuView);
        optionDialog = dialogBuilder.create();

        // Prevent from dialog closing when touching outside
        optionDialog.setCanceledOnTouchOutside(false);

        optionDialog.show();

        // Reset shader type
        shaderType = "default";

        // Load default parameter
        defaultParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                loadParameters(R.raw.parallel_basic_parameters);
            }
        });

        // Load stress parameter
        stressParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                loadParameters(R.raw.parallel_stress_parameters);
            }
        });

        // Indicate to use default shader
        defaultShaderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultShaderButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                strongShaderButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                shaderType = "default";
            }
        });

        // Indicate to use strong shader
        strongShaderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strongShaderButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                defaultShaderButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                shaderType = "strong";
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

                viewHolder.resultButton.setEnabled(false);
                viewHolder.resultButton.setBackgroundColor(Color.GRAY);

                enableNonRunningTests(position, false);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String[] testArgument = new String[4];
                        testArgument[0] = "parallel_" + testName; // Test Name

                        // Shader Name
                        if (shaderType == "default") {
                            testArgument[1] = "parallel_" + testName;
                        }
                        else {
                            testArgument[1] = "parallel_" + testName + "_" + shaderType;
                        }

                        testArgument[2] = "parallel_" + testName + "_results"; // Result Shader Name
                        testArgument[3] = "parallel_" + testName + "_parameters"; // Parameter Name

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
