package com.example.litmustestandroid;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private RecyclerView weakMemoryTestsRV;
    private LitmusTestAdapter weakMemoryTestsAdapter;
    private RecyclerView coherenceTestsRV;
    private LitmusTestAdapter coherenceTestsAdapter;
    private RecyclerView atomicityTestsRV;
    private LitmusTestAdapter atomicityTestsAdapter;
    private RecyclerView barrierTestsRV;
    private LitmusTestAdapter barrierTestsAdapter;

    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> adapterItems;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog optionDialog;
    private TextView optionTestName;
    private EditText[] parameters = new EditText[20];
    private Button startButton, closeButton, defaultParamButton, stressParamButton, defaultShaderButton, strongShaderButton;

    private static final String TAG = "MainActivity";

    private Handler handler = new Handler();

    private String shaderType = "";

    private ArrayList<TestCase> testCases = new ArrayList<>();

    public ArrayList<String> totalShaderNames = new ArrayList<String>();
    public ArrayList<String> totalResultNames = new ArrayList<String>();
    public ArrayList<String> totalOutputNames = new ArrayList<String>();
    public ArrayList<String> totalTestParamNames = new ArrayList<String>();
    public ArrayList<String> totalParamPresetNames = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registerTestCases();
        initFileConfig();
        displayLitmusTests();
    }

    private String JsonDataFromAsset() {
        String json = null;
        try {
            InputStream inputStream = getAssets().open("test_list.json");
            int sizeOfFile = inputStream.available();
            byte[] bufferData = new byte[sizeOfFile];
            inputStream.read(bufferData);
            inputStream.close();
            json = new String(bufferData, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    private void registerTestCases() {
        try {
            JSONObject testJsonObject = new JSONObject(JsonDataFromAsset());
            JSONArray testArray = testJsonObject.getJSONArray("tests");
            for (int i = 0; i < testArray.length(); i++) {
                JSONObject testData = testArray.getJSONObject(i);

                TestCase newTest = new TestCase();
                newTest.testName = testData.getString("name");
                newTest.testType = testData.getString("type");

                JSONArray shaderArray = testData.getJSONArray("shaders");
                String[] shaderNames = new String[shaderArray.length()];
                for(int j = 0; j < shaderArray.length(); j++) {
                    shaderNames[j] = shaderArray.getString(j);
                }
                newTest.setShaderNames(shaderNames, totalShaderNames);

                newTest.setResultName(testData.getString("result"), totalResultNames);
                newTest.setOutputName(testData.getString("output"), totalOutputNames);
                newTest.setTestParamName(testData.getString("parameter"), totalTestParamNames);

                JSONArray paramPresetArray = testData.getJSONArray("parameter_presets");
                String[] paramPresetNames = new String[paramPresetArray.length()];
                for(int j = 0; j < paramPresetArray.length(); j++) {
                    paramPresetNames[j] = paramPresetArray.getString(j);
                }
                newTest.setParamPresetNames(paramPresetNames, totalParamPresetNames);

                testCases.add(newTest);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public TestCase findTestCase(String testName) {
        for (int i = 0; i < testCases.size(); i++) {
            if(testCases.get(i).testName.equals(testName)) {
                return testCases.get(i);
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
        // Transfer shader files
        for(int i = 0; i < totalShaderNames.size(); i++) {
            int shaderId = this.getResources().getIdentifier(totalShaderNames.get(i), "raw", this.getPackageName());
            copyFile(shaderId, totalShaderNames.get(i) + ".spv");
            Log.d(TAG, "File: " + totalShaderNames.get(i) + ".spv copied to " + getFilesDir().toString());
        }

        // Transfer result files
        for(int i = 0; i < totalResultNames.size(); i++) {
            int resultId = this.getResources().getIdentifier(totalResultNames.get(i), "raw", this.getPackageName());
            copyFile(resultId, totalResultNames.get(i) + ".spv");
            Log.d(TAG, "File: " + totalResultNames.get(i) + ".spv copied to " + getFilesDir().toString());
        }

        // Transfer output files
        for(int i = 0; i < totalOutputNames.size(); i++) {
            int outputId = this.getResources().getIdentifier(totalOutputNames.get(i), "raw", this.getPackageName());
            copyFile(outputId, totalOutputNames.get(i) + ".txt");
            Log.d(TAG, "File: " + totalOutputNames.get(i) + ".txt copied to " + getFilesDir().toString());
        }

        // Transfer test param files
        for(int i = 0; i < totalTestParamNames.size(); i++) {
            int testParamId = this.getResources().getIdentifier(totalTestParamNames.get(i), "raw", this.getPackageName());
            copyFile(testParamId, totalTestParamNames.get(i) + ".txt");
            Log.d(TAG, "File: " + totalTestParamNames.get(i) + ".txt copied to " + getFilesDir().toString());
        }

        // Transfer param files
        for(int i = 0; i < totalParamPresetNames.size(); i++) {
            int paramPresetId = this.getResources().getIdentifier(totalParamPresetNames.get(i), "raw", this.getPackageName());
            copyFile(paramPresetId, totalParamPresetNames.get(i) + ".txt");
            Log.d(TAG, "File: " + totalParamPresetNames.get(i) + ".txt copied to " + getFilesDir().toString());
        }
    }

    public void displayLitmusTests() {
        // Weak Memory Tests
        String weakMemoryTestNames[] = getResources().getStringArray(R.array.weakMemoryTests);
        weakMemoryTestsRV = findViewById(R.id.weakMemoryTestsRecyclerView);

        weakMemoryTestsAdapter = new LitmusTestAdapter(this, weakMemoryTestNames, MainActivity.this);
        weakMemoryTestsRV.setAdapter(weakMemoryTestsAdapter);
        weakMemoryTestsRV.setLayoutManager(new LinearLayoutManager(this));

        // Coherence Tests
        String coherenceTestNames[] = getResources().getStringArray(R.array.coherenceTests);
        coherenceTestsRV = findViewById(R.id.coherenceTestsRecyclerView);

        coherenceTestsAdapter = new LitmusTestAdapter(this, coherenceTestNames, MainActivity.this);
        coherenceTestsRV.setAdapter(coherenceTestsAdapter);
        coherenceTestsRV.setLayoutManager(new LinearLayoutManager(this));

        // Atomicity Tests
        String atomicityTestNames[] = getResources().getStringArray(R.array.atomicityTests);
        atomicityTestsRV = findViewById(R.id.atomicityTestsRecyclerView);

        atomicityTestsAdapter = new LitmusTestAdapter(this, atomicityTestNames, MainActivity.this);
        atomicityTestsRV.setAdapter(atomicityTestsAdapter);
        atomicityTestsRV.setLayoutManager(new LinearLayoutManager(this));

        // Barrier Tests
        String barrierTestNames[] = getResources().getStringArray(R.array.barrierTests);
        barrierTestsRV = findViewById(R.id.barrierTestsRecyclerView);

        barrierTestsAdapter = new LitmusTestAdapter(this, barrierTestNames, MainActivity.this);
        barrierTestsRV.setAdapter(barrierTestsAdapter);
        barrierTestsRV.setLayoutManager(new LinearLayoutManager(this));
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

    public void enableNonRunningTests(int position, boolean enabled, RecyclerView testRV){
        int childCount = weakMemoryTestsRV.getChildCount();
        for(int i = 0; i < childCount; i++) {
            final LitmusTestAdapter.LitmusTestViewHolder viewHolder = (LitmusTestAdapter.LitmusTestViewHolder) weakMemoryTestsRV.getChildViewHolder(weakMemoryTestsRV.getChildAt(i));
            if (testRV == weakMemoryTestsRV && i == position) {
                continue;
            }
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
        childCount = coherenceTestsRV.getChildCount();
        for(int i = 0; i < childCount; i++) {
            final LitmusTestAdapter.LitmusTestViewHolder viewHolder = (LitmusTestAdapter.LitmusTestViewHolder) coherenceTestsRV.getChildViewHolder(coherenceTestsRV.getChildAt(i));
            if (testRV == coherenceTestsRV && i == position) {
                continue;
            }
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
        childCount = atomicityTestsRV.getChildCount();
        for(int i = 0; i < childCount; i++) {
            final LitmusTestAdapter.LitmusTestViewHolder viewHolder = (LitmusTestAdapter.LitmusTestViewHolder) atomicityTestsRV.getChildViewHolder(atomicityTestsRV.getChildAt(i));
            if (testRV == atomicityTestsRV && i == position) {
                continue;
            }
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
        childCount = barrierTestsRV.getChildCount();
        for(int i = 0; i < childCount; i++) {
            final LitmusTestAdapter.LitmusTestViewHolder viewHolder = (LitmusTestAdapter.LitmusTestViewHolder) barrierTestsRV.getChildViewHolder(barrierTestsRV.getChildAt(i));
            if (testRV == barrierTestsRV && i == position) {
                continue;
            }
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

    public RecyclerView findRecyclerView(String testName) {
        RecyclerView resultRV = null;
        int childCount = weakMemoryTestsRV.getChildCount();
        for(int i = 0; i < childCount; i++) {
            final LitmusTestAdapter.LitmusTestViewHolder weakMemoryViewHolder = (LitmusTestAdapter.LitmusTestViewHolder) weakMemoryTestsRV.getChildViewHolder(weakMemoryTestsRV.getChildAt(i));
            if (weakMemoryViewHolder.testName.getText().toString().equals(testName)) {
                resultRV = weakMemoryTestsRV;
            }
        }
        childCount = coherenceTestsRV.getChildCount();
        for(int i = 0; i < childCount; i++) {
            final LitmusTestAdapter.LitmusTestViewHolder coherenceViewHolder = (LitmusTestAdapter.LitmusTestViewHolder) coherenceTestsRV.getChildViewHolder(coherenceTestsRV.getChildAt(i));
            if (coherenceViewHolder.testName.getText().toString().equals(testName)) {
                resultRV = coherenceTestsRV;
            }
        }
        childCount = atomicityTestsRV.getChildCount();
        for(int i = 0; i < childCount; i++) {
            final LitmusTestAdapter.LitmusTestViewHolder atomicityViewHolder = (LitmusTestAdapter.LitmusTestViewHolder) atomicityTestsRV.getChildViewHolder(atomicityTestsRV.getChildAt(i));
            if (atomicityViewHolder.testName.getText().toString().equals(testName)) {
                resultRV = atomicityTestsRV;
            }
        }
        childCount = barrierTestsRV.getChildCount();
        for(int i = 0; i < childCount; i++) {
            final LitmusTestAdapter.LitmusTestViewHolder barrierViewHolder = (LitmusTestAdapter.LitmusTestViewHolder) barrierTestsRV.getChildViewHolder(barrierTestsRV.getChildAt(i));
            if (barrierViewHolder.testName.getText().toString().equals(testName)) {
                resultRV = barrierTestsRV;
            }
        }
        return resultRV;
    }

    public void initializeShaderMenu(String testName, View optionMenuView) {
        autoCompleteTextView = optionMenuView.findViewById(R.id.shaderOptionAutoCompleteText);

        TestCase currTestCase = findTestCase(testName);
        String[] shortShaderNames = new String[currTestCase.shaderNames.length];
        for (int i = 0; i < currTestCase.shaderNames.length; i++) {
            shortShaderNames[i] = currTestCase.shaderNames[i].substring(testName.length() + 12);
        }

        if(currTestCase != null) {
            adapterItems = new ArrayAdapter<String>(this, R.layout.shader_option_list_item, shortShaderNames);
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
        shaderType = "litmustest_" + testName + "_default";

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
                RecyclerView testRV = findRecyclerView(testName);

                if(testRV == null) {
                    Log.e(TAG, testName + " does not exist in any recyclerviews!");
                }
                final LitmusTestAdapter.LitmusTestViewHolder viewHolder = (LitmusTestAdapter.LitmusTestViewHolder) testRV.getChildViewHolder(testRV.getChildAt(position));
                writeParameters(testName);
                optionDialog.dismiss();

                viewHolder.optionButton.setEnabled(false);
                viewHolder.optionButton.setBackgroundColor(Color.BLUE);

                viewHolder.resultButton.setEnabled(false);
                viewHolder.resultButton.setBackgroundColor(Color.GRAY);

                enableNonRunningTests(position, false, testRV);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String[] testArgument = new String[4];
                        testArgument[0] = "litmustest_" + testName; // Test Name

                        // Shader Name
                        testArgument[1] = shaderType;

                        testArgument[2] = "litmustest_" + testName + "_results"; // Result Shader Name
                        testArgument[3] = "litmustest_" + testName + "_parameters"; // Parameter Name

                        main(testArgument);

                        // Update Start and Result Button
                        viewHolder.optionButton.setEnabled(true);
                        viewHolder.optionButton.setBackgroundColor(Color.GREEN);

                        viewHolder.resultButton.setEnabled(true);
                        viewHolder.resultButton.setBackgroundColor(Color.RED);

                        viewHolder.newTest = false;

                        enableNonRunningTests(position, true, testRV);

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
