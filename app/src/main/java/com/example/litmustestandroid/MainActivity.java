package com.example.litmustestandroid;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.litmustestandroid.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("litmusTest-main-lib");
    }

    private ActivityMainBinding binding;
    private RecyclerView litmusTestRV;
    private LitmusTestAdapter litmusTestAdapter;

    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> adapterItems;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog optionDialog;
    private TextView optionTestName;
    private EditText[] parameters = new EditText[20];
    private Button startButton, closeButton, defaultParamButton, stressParamButton, defaultShaderButton, strongShaderButton;

    private static final String TAG = "MainActivity";

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

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registerTestCases();
        initFileConfig();

        /*binding.litmusTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MainActivity.class);
                startActivity(i);
            }
        });*/

        displayLitmusTests();
    }

    private void registerTestCases() {
        // Message Passing
        TestCase messagePassing = new TestCase();
        messagePassing.testType = "weakMemory";
        messagePassing.testName = "message_passing";
        messagePassing.setShaderNames(new String[]{"litmustest_message_passing_default.spv", "litmustest_message_passing_strong.spv"}, totalShaderNames);
        messagePassing.setShaderIds(new int[]{R.raw.litmustest_message_passing_default, R.raw.litmustest_message_passing_strong}, totalShaderIds);
        messagePassing.setResultNames(new String[]{"litmustest_message_passing_results.spv"}, totalResultNames);
        messagePassing.setResultIds(new int[]{R.raw.litmustest_message_passing_results}, totalResultIds);
        messagePassing.setOutputName("litmustest_message_passing_output.txt", totalOutputNames);
        messagePassing.setOutputId(R.raw.litmustest_message_passing_output, totalOutputIds);
        messagePassing.setTestParamName("litmustest_message_passing_parameters.txt", totalTestParamNames);
        messagePassing.setTestParamId(R.raw.litmustest_message_passing_parameters, totalTestParamIds);
        messagePassing.setParamNames(new String[]{"parameters_basic_parameters.txt", "parameters_stress_parameters.txt"}, totalParamNames);
        messagePassing.setParamIds(new int[]{R.raw.parameters_basic, R.raw.parameters_stress}, totalParamIds);
        testCases[0] = messagePassing;

        // Load Buffer
        TestCase loadBuffer = new TestCase();
        loadBuffer.testType = "weakMemory";
        loadBuffer.testName = "load_buffer";
        loadBuffer.setShaderNames(new String[]{"litmustest_load_buffer_default.spv"}, totalShaderNames);
        loadBuffer.setShaderIds(new int[]{R.raw.litmustest_load_buffer_default}, totalShaderIds);
        loadBuffer.setResultNames(new String[]{"litmustest_load_buffer_results.spv"}, totalResultNames);
        loadBuffer.setResultIds(new int[]{R.raw.litmustest_load_buffer_results}, totalResultIds);
        loadBuffer.setOutputName("litmustest_load_buffer_output.txt", totalOutputNames);
        loadBuffer.setOutputId(R.raw.litmustest_load_buffer_output, totalOutputIds);
        loadBuffer.setTestParamName("litmustest_load_buffer_parameters.txt", totalTestParamNames);
        loadBuffer.setTestParamId(R.raw.litmustest_load_buffer_parameters, totalTestParamIds);
        loadBuffer.setParamNames(new String[]{"parameters_basic_parameters.txt", "parameters_stress_parameters.txt"}, totalParamNames);
        loadBuffer.setParamIds(new int[]{R.raw.parameters_basic, R.raw.parameters_stress}, totalParamIds);
        testCases[1] = loadBuffer;

        // Store Buffer
        TestCase storeBuffer = new TestCase();
        storeBuffer.testType = "weakMemory";
        storeBuffer.testName = "store_buffer";
        storeBuffer.setShaderNames(new String[]{"litmustest_store_buffer_default.spv"}, totalShaderNames);
        storeBuffer.setShaderIds(new int[]{R.raw.litmustest_store_buffer_default}, totalShaderIds);
        storeBuffer.setResultNames(new String[]{"litmustest_store_buffer_results.spv"}, totalResultNames);
        storeBuffer.setResultIds(new int[]{R.raw.litmustest_store_buffer_results}, totalResultIds);
        storeBuffer.setOutputName("litmustest_store_buffer_output.txt", totalOutputNames);
        storeBuffer.setOutputId(R.raw.litmustest_store_buffer_output, totalOutputIds);
        storeBuffer.setTestParamName("litmustest_store_buffer_parameters.txt", totalTestParamNames);
        storeBuffer.setTestParamId(R.raw.litmustest_store_buffer_parameters, totalTestParamIds);
        storeBuffer.setParamNames(new String[]{"parameters_basic_parameters.txt", "parameters_stress_parameters.txt"}, totalParamNames);
        storeBuffer.setParamIds(new int[]{R.raw.parameters_basic, R.raw.parameters_stress}, totalParamIds);
        testCases[2] = storeBuffer;
    }

    public TestCase findTestCase(String testName) {
        for (int i = 0; i < testCases.length; i++) {
            if(testCases[i].testName.equals(testName)) {
                return testCases[i];
            }
        }
        return null;
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
        String litmusTestName[] = getResources().getStringArray(R.array.litmusTests);
        litmusTestRV = findViewById(R.id.LitmusTestRecyclerView);

        litmusTestAdapter = new LitmusTestAdapter(this, litmusTestName, MainActivity.this);
        litmusTestRV.setAdapter(litmusTestAdapter);
        litmusTestRV.setLayoutManager(new LinearLayoutManager(this));
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
        InputStream inputStream = getResources().openRawResource(R.raw.parameters_basic);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String fileName = "litmustest_" + testName + "_parameters.txt";
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
        int childCount = litmusTestRV.getChildCount();
        for(int i = 0; i < childCount; i++) {
            final LitmusTestAdapter.LitmusTestViewHolder viewHolder = (LitmusTestAdapter.LitmusTestViewHolder) litmusTestRV.getChildViewHolder(litmusTestRV.getChildAt(i));
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

    public void initializeShaderMenu(String testName, View optionMenuView) {
        autoCompleteTextView = optionMenuView.findViewById(R.id.shaderOptionAutoCompleteText);

        TestCase currTestCase = findTestCase(testName);
        if(currTestCase != null) {
            adapterItems = new ArrayAdapter<String>(this, R.layout.shader_option_list_item, currTestCase.getShaderNames());
            autoCompleteTextView.setAdapter(adapterItems);
            autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String item = adapterView.getItemAtPosition(i).toString();
                    shaderType = item;
                }
            });
        }
        else {
            Log.e(TAG, "initializeShaderMenu(), currTestCase is NULL");
        }

    }

    public void openOptionMenu(String testName, int position) {
        Log.i("TEST", testName + " PRESSED, OPENING OPTION MENU");

        dialogBuilder = new AlertDialog.Builder(this);
        final View optionMenuView = getLayoutInflater().inflate(R.layout.main_test_option, null);

        optionTestName = (TextView) optionMenuView.findViewById(R.id.testOptionTestName);
        optionTestName.setText(testName);

        parameters[0] = (EditText) optionMenuView.findViewById(R.id.testOptionTestIteration); // testIteration
        parameters[1] = (EditText) optionMenuView.findViewById(R.id.testOptionTestingWorkgroups); // testingWorkgroups
        parameters[2] = (EditText) optionMenuView.findViewById(R.id.testOptionMaxWorkgroups); // maxWorkgroups
        parameters[3] = (EditText) optionMenuView.findViewById(R.id.testOptionMinWorkgroupSize); // minWorkgroupSize
        parameters[4] = (EditText) optionMenuView.findViewById(R.id.testOptionMaxWorkgroupSize); // maxWorkgroupSize
        parameters[5] = (EditText) optionMenuView.findViewById(R.id.testOptionShufflePct); // shufflePct
        parameters[6] = (EditText) optionMenuView.findViewById(R.id.testOptionBarrierPct); // barrierPct
        parameters[7] = (EditText) optionMenuView.findViewById(R.id.testOptionScratchMemorySize); // scratchMemorySize
        parameters[8] = (EditText) optionMenuView.findViewById(R.id.testOptionMemoryStride); // memStride
        parameters[9] = (EditText) optionMenuView.findViewById(R.id.testOptionMemoryStressPct); // memStressPct
        parameters[10] = (EditText) optionMenuView.findViewById(R.id.testOptionMemoryStressIterations); // memStressIterations
        parameters[11] = (EditText) optionMenuView.findViewById(R.id.testOptionMemOryStressPattern); // memStressPattern
        parameters[12] = (EditText) optionMenuView.findViewById(R.id.testOptionPreStressPct); // preStressPct
        parameters[13] = (EditText) optionMenuView.findViewById(R.id.testOptionPreStressIterations); // preStressIterations
        parameters[14] = (EditText) optionMenuView.findViewById(R.id.testOptionPreStressPattern); // preStressPattern
        parameters[15] = (EditText) optionMenuView.findViewById(R.id.testOptionStressLineSize); // stressLineSize
        parameters[16] = (EditText) optionMenuView.findViewById(R.id.testOptionStressTargetLines); // stressTargetLines
        parameters[17] = (EditText) optionMenuView.findViewById(R.id.testOptionStressAssignmentStrategy); // stressAssignmentStrategy
        parameters[18] = (EditText) optionMenuView.findViewById(R.id.testOptionPermuteFirst); // permuteFirst
        parameters[19] = (EditText) optionMenuView.findViewById(R.id.testOptionPermuteSecond); // permuteSecond

        loadParameters(R.raw.parameters_basic);

        startButton = (Button) optionMenuView.findViewById(R.id.testOptionStartButton);
        closeButton = (Button) optionMenuView.findViewById(R.id.testOptionCloseButton);
        defaultParamButton = (Button) optionMenuView.findViewById(R.id.testOptionDefaultParamButton);
        stressParamButton = (Button) optionMenuView.findViewById(R.id.testOptionStressParamButton);

        dialogBuilder.setView(optionMenuView);
        optionDialog = dialogBuilder.create();

        // Prevent from dialog closing when touching outside
        optionDialog.setCanceledOnTouchOutside(false);

        optionDialog.show();

        // Reset shader type
        shaderType = "default";

        // Initialize shader drop down option menu
        initializeShaderMenu(testName, optionMenuView);

        // Load default parameter
        defaultParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                loadParameters(R.raw.parameters_basic);
            }
        });

        // Load stress parameter
        stressParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                loadParameters(R.raw.parameters_stress);
            }
        });

        // Start test
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TEST", testName + " STARTING");
                final LitmusTestAdapter.LitmusTestViewHolder viewHolder = (LitmusTestAdapter.LitmusTestViewHolder) litmusTestRV.getChildViewHolder(litmusTestRV.getChildAt(position));
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
                        testArgument[0] = "litmustest_" + testName; // Test Name

                        // Shader Name
                        testArgument[1] = "litmustest_" + testName + "_" + shaderType;

                        testArgument[2] = "litmustest_" + testName + "_results"; // Result Shader Name
                        testArgument[3] = "litmustest_" + testName + "_parameters"; // Parameter Name

                        main(testArgument);

                        // Update Start and Result Button
                        viewHolder.optionButton.setEnabled(true);
                        viewHolder.optionButton.setBackgroundColor(Color.GREEN);

                        viewHolder.resultButton.setEnabled(true);
                        viewHolder.resultButton.setBackgroundColor(Color.RED);

                        viewHolder.newTest = false;

                        enableNonRunningTests(position, true);

                        Toast.makeText(MainActivity.this, "Test " + testName + " finished!", Toast.LENGTH_LONG).show();
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
            FileInputStream fis = openFileInput("litmustest_" + testName + "_output.txt");
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
