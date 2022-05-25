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
import androidx.recyclerview.widget.DividerItemDecoration;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

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
    private RecyclerView[] RVLists = new RecyclerView[4];

    private ArrayList<LitmusTestAdapter.LitmusTestViewHolder> litmusTestViewHolders = new ArrayList<LitmusTestAdapter.LitmusTestViewHolder>();

    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> adapterItems;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog exploreDialog;
    private TextView exploreTestName;
    private EditText[] exploreParameters = new EditText[20];
    private Button exploreStartButton, exploreCloseButton, defaultParamButton, stressParamButton;

    private AlertDialog tuningDialog;
    private TextView tuningTestName;
    private EditText[] tuningParameters = new EditText[3];
    private Button tuningStartButton, tuningCloseButton;
    private HashMap<String, ArrayList<TuningResultCase>> tuningResultCases = new HashMap<>();

    private static final String TAG = "MainActivity";

    private Handler handler = new Handler();
    private Handler tuningHandler = new Handler();

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

        weakMemoryTestsAdapter = new LitmusTestAdapter(this, weakMemoryTestNames, MainActivity.this, litmusTestViewHolders);
        weakMemoryTestsRV.setAdapter(weakMemoryTestsAdapter);
        weakMemoryTestsRV.setLayoutManager(new LinearLayoutManager(this));
        weakMemoryTestsRV.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        RVLists[0] = weakMemoryTestsRV;

        // Coherence Tests
        String coherenceTestNames[] = getResources().getStringArray(R.array.coherenceTests);
        coherenceTestsRV = findViewById(R.id.coherenceTestsRecyclerView);

        coherenceTestsAdapter = new LitmusTestAdapter(this, coherenceTestNames, MainActivity.this, litmusTestViewHolders);
        coherenceTestsRV.setAdapter(coherenceTestsAdapter);
        coherenceTestsRV.setLayoutManager(new LinearLayoutManager(this));
        coherenceTestsRV.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        RVLists[1] = coherenceTestsRV;

        // Atomicity Tests
        String atomicityTestNames[] = getResources().getStringArray(R.array.atomicityTests);
        atomicityTestsRV = findViewById(R.id.atomicityTestsRecyclerView);

        atomicityTestsAdapter = new LitmusTestAdapter(this, atomicityTestNames, MainActivity.this, litmusTestViewHolders);
        atomicityTestsRV.setAdapter(atomicityTestsAdapter);
        atomicityTestsRV.setLayoutManager(new LinearLayoutManager(this));
        atomicityTestsRV.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        RVLists[2] = atomicityTestsRV;

        // Barrier Tests
        String barrierTestNames[] = getResources().getStringArray(R.array.barrierTests);
        barrierTestsRV = findViewById(R.id.barrierTestsRecyclerView);

        barrierTestsAdapter = new LitmusTestAdapter(this, barrierTestNames, MainActivity.this, litmusTestViewHolders);
        barrierTestsRV.setAdapter(barrierTestsAdapter);
        barrierTestsRV.setLayoutManager(new LinearLayoutManager(this));
        barrierTestsRV.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        RVLists[3] = barrierTestsRV;
    }

    // Automatically fill parameters with basic values
    public void loadExploreParameters(int paramValue){
        InputStream inputStream = getResources().openRawResource(paramValue);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        int index = 0;

        try {
            String line = bufferedReader.readLine();
            while (line != null) {
                String[] words = line.split("=");
                if((!words[0].equals("numMemLocations") && !words[0].equals("numOutputs")) && index < exploreParameters.length) {
                    exploreParameters[index].setText(words[1]);
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
    public void writeExploreParameters(String testName, int paramValue) {
        InputStream inputStream = getResources().openRawResource(paramValue);
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
                    outputNumber = exploreParameters[index].getText().toString();
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

    public int randomGenerator(int min, int max, String tuningRandomSeed) {
        double generator;
        Random random;
        if(tuningRandomSeed.length() == 0) {
            random = new Random();
        }
        else {
            random = new Random(tuningRandomSeed.hashCode());
        }
        generator = random.nextDouble();
        return (int) Math.floor(generator * (max - min + 1) + min);
    }

    public int roundedPercentage(String tuningRandomSeed) {
        return (int) Math.floor(randomGenerator(0, 100, tuningRandomSeed) / 5) * 5;
    }

    public int getPercentage(String tuningRandomSeed, boolean smoothedParameters) {
        if (smoothedParameters) {
            return roundedPercentage(tuningRandomSeed);
        }
        else {
            return randomGenerator(0, 1, tuningRandomSeed) * 100;
        }
    }

    public void writeTuningParameters(String testName, String tuningRandomSeed, int testIteration, int paramPresetValue) {
        boolean smoothedParameters = true;
        int workgroupLimiter = 1024;
        int testingWorkgroups = randomGenerator(2, workgroupLimiter, tuningRandomSeed);
        int maxWorkgroups = randomGenerator(testingWorkgroups, workgroupLimiter, tuningRandomSeed);
        int stressLineSize = (int) Math.pow(2, randomGenerator(2, 10, tuningRandomSeed));
        int stressTargetLines = randomGenerator(1, 16, tuningRandomSeed);
        int memStride = randomGenerator(1, 7, tuningRandomSeed);
        Map<String, String> parameterFormat = new TreeMap<String, String>();

        InputStream inputStream = getResources().openRawResource(paramPresetValue);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String fileName = "litmustest_" + testName + "_parameters.txt";

        // Get parameter format by reading preset file
        try {
            String line = bufferedReader.readLine();
            while (line != null) {
                String[] words = line.split("=");
                parameterFormat.put(words[0], words[1]);
                line = bufferedReader.readLine();
            }
            inputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // Now randomize certain parmeter values
        parameterFormat.put("testIterations", Integer.toString(testIteration));
        parameterFormat.put("testingWorkgroups", Integer.toString(testingWorkgroups));
        parameterFormat.put("maxWorkgroups", Integer.toString(maxWorkgroups));
        parameterFormat.put("shufflePct", Integer.toString(getPercentage(tuningRandomSeed, smoothedParameters)));
        parameterFormat.put("barrierPct", Integer.toString(getPercentage(tuningRandomSeed, smoothedParameters)));
        parameterFormat.put("scratchMemorySize", Integer.toString(32 * stressLineSize * stressTargetLines));
        parameterFormat.put("memStride", Integer.toString(memStride));
        parameterFormat.put("memStressPct", Integer.toString(getPercentage(tuningRandomSeed, smoothedParameters)));
        parameterFormat.put("memStressIterations", Integer.toString(randomGenerator(0, 1024, tuningRandomSeed)));
        parameterFormat.put("preStressPct", Integer.toString(getPercentage(tuningRandomSeed, smoothedParameters)));
        parameterFormat.put("preStressIterations", Integer.toString(randomGenerator(0, 128, tuningRandomSeed)));
        parameterFormat.put("stressLineSize", Integer.toString(stressLineSize));
        parameterFormat.put("stressTargetLines", Integer.toString(stressTargetLines));
        parameterFormat.put("stressAssignmentStrategy", Integer.toString(getPercentage(tuningRandomSeed, smoothedParameters)));

        boolean memStressStoreFirst = Math.floor(Math.random() * 100) < getPercentage(tuningRandomSeed, smoothedParameters);
        boolean memStressStoreSecond = Math.floor(Math.random() * 100) < getPercentage(tuningRandomSeed, smoothedParameters);
        int memStressPattern;
        if (memStressStoreFirst) {
            if (memStressStoreSecond) {
                memStressPattern = 0;
            }
            else {
                memStressPattern = 1;
            }
        }
        else {
            if (memStressStoreSecond) {
                memStressPattern = 2;
            }
            else {
                memStressPattern = 3;
            }
        }
        parameterFormat.put("memStressPattern", Integer.toString(memStressPattern));

        boolean preStressStoreFirst = Math.floor(Math.random() * 100) < getPercentage(tuningRandomSeed, smoothedParameters);
        boolean preStressStoreSecond = Math.floor(Math.random() * 100) < getPercentage(tuningRandomSeed, smoothedParameters);
        int preStressPattern;
        if (preStressStoreFirst) {
            if (preStressStoreSecond) {
                preStressPattern = 0;
            }
            else {
                preStressPattern = 1;
            }
        }
        else {
            if (preStressStoreSecond) {
                preStressPattern = 2;
            }
            else {
                preStressPattern = 3;
            }
        }
        parameterFormat.put("preStressPattern", Integer.toString(preStressPattern));

        // Now insert the parameter values to text file that will be used during test

        try {
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);

            for (String key: parameterFormat.keySet()) {
                fos.write((key + "=" + parameterFormat.get(key) + "\n").getBytes());
            }
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

    public void enableAllTests(boolean enabled){
        for(int i = 0; i < litmusTestViewHolders.size(); i++) {
            LitmusTestAdapter.LitmusTestViewHolder viewHolder = litmusTestViewHolders.get(i);

            viewHolder.explorerButton.setEnabled(enabled);
            viewHolder.explorerResultButton.setEnabled(enabled);
            viewHolder.tuningButton.setEnabled(enabled);
            viewHolder.tuningResultButton.setEnabled(enabled);

            if(enabled) {
                viewHolder.explorerButton.setBackgroundColor(Color.GREEN);
                viewHolder.tuningButton.setBackgroundColor(Color.GREEN);
                if(viewHolder.newExplorerTest) { // If explorer test is still new, stay gray
                    viewHolder.explorerResultButton.setEnabled(false);
                    viewHolder.explorerResultButton.setBackgroundColor(Color.GRAY);
                }
                else { // If explorer test has result existing, turn RED
                    viewHolder.explorerResultButton.setBackgroundColor(Color.RED);
                }
                if(viewHolder.newTuningTest) { // If tuning test is still new, stay gray
                    viewHolder.tuningResultButton.setEnabled(false);
                    viewHolder.tuningResultButton.setBackgroundColor(Color.GRAY);
                }
                else { // If tuning test has result existing, turn RED
                    viewHolder.tuningResultButton.setBackgroundColor(Color.RED);
                }
            }
            else {
                viewHolder.explorerButton.setBackgroundColor(Color.GRAY);
                viewHolder.explorerResultButton.setBackgroundColor(Color.GRAY);
                viewHolder.tuningButton.setBackgroundColor(Color.GRAY);
                viewHolder.tuningResultButton.setBackgroundColor(Color.GRAY);
            }
        }
    }

    public RecyclerView findRecyclerView(String testName) {
        for(int i = 0; i < RVLists.length; i++) {
            for(int j = 0; j < RVLists[i].getAdapter().getItemCount(); j++) {
                LitmusTestAdapter.LitmusTestViewHolder viewHolder = (LitmusTestAdapter.LitmusTestViewHolder) RVLists[i].getChildViewHolder(RVLists[i].getChildAt(j));
                if (viewHolder.testName.getText().toString().equals(testName)) {
                    return RVLists[i];
                }
            }
        }
        return null;
    }

    public void initializeShaderMenu(String testName, View exploreMenuView) {
        autoCompleteTextView = exploreMenuView.findViewById(R.id.shaderExploreAutoCompleteText);

        TestCase currTestCase = findTestCase(testName);
        String[] shortShaderNames = new String[currTestCase.shaderNames.length];
        for (int i = 0; i < currTestCase.shaderNames.length; i++) {
            shortShaderNames[i] = currTestCase.shaderNames[i].substring(testName.length() + 12);
        }

        if(currTestCase != null) {
            adapterItems = new ArrayAdapter<String>(this, R.layout.shader_explore_list_item, shortShaderNames);
            autoCompleteTextView.setAdapter(adapterItems);
            autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String item = adapterView.getItemAtPosition(i).toString();
                    shaderType = "litmustest_" + testName + "_" + item;
                }
            });
        }
        else {
            Log.e(TAG, "initializeShaderMenu(), currTestCase is NULL");
        }

    }

    public void openExploreMenu(String testName, int position) {
        Log.i("TEST", testName + " PRESSED, OPENING Explore MENU");

        dialogBuilder = new AlertDialog.Builder(this);
        final View exploreMenuView = getLayoutInflater().inflate(R.layout.main_test_explore, null);

        exploreTestName = (TextView) exploreMenuView.findViewById(R.id.testExploreTestName);
        exploreTestName.setText(testName);

        exploreParameters[0] = (EditText) exploreMenuView.findViewById(R.id.testExploreTestIteration); // testIteration
        exploreParameters[1] = (EditText) exploreMenuView.findViewById(R.id.testExploreTestingWorkgroups); // testingWorkgroups
        exploreParameters[2] = (EditText) exploreMenuView.findViewById(R.id.testExploreMaxWorkgroups); // maxWorkgroups
        exploreParameters[3] = (EditText) exploreMenuView.findViewById(R.id.testExploreMinWorkgroupSize); // minWorkgroupSize
        exploreParameters[4] = (EditText) exploreMenuView.findViewById(R.id.testExploreMaxWorkgroupSize); // maxWorkgroupSize
        exploreParameters[5] = (EditText) exploreMenuView.findViewById(R.id.testExploreShufflePct); // shufflePct
        exploreParameters[6] = (EditText) exploreMenuView.findViewById(R.id.testExploreBarrierPct); // barrierPct
        exploreParameters[7] = (EditText) exploreMenuView.findViewById(R.id.testExploreScratchMemorySize); // scratchMemorySize
        exploreParameters[8] = (EditText) exploreMenuView.findViewById(R.id.testExploreMemoryStride); // memStride
        exploreParameters[9] = (EditText) exploreMenuView.findViewById(R.id.testExploreMemoryStressPct); // memStressPct
        exploreParameters[10] = (EditText) exploreMenuView.findViewById(R.id.testExploreMemoryStressIterations); // memStressIterations
        exploreParameters[11] = (EditText) exploreMenuView.findViewById(R.id.testExploreMemOryStressPattern); // memStressPattern
        exploreParameters[12] = (EditText) exploreMenuView.findViewById(R.id.testExplorePreStressPct); // preStressPct
        exploreParameters[13] = (EditText) exploreMenuView.findViewById(R.id.testExplorePreStressIterations); // preStressIterations
        exploreParameters[14] = (EditText) exploreMenuView.findViewById(R.id.testExplorePreStressPattern); // preStressPattern
        exploreParameters[15] = (EditText) exploreMenuView.findViewById(R.id.testExploreStressLineSize); // stressLineSize
        exploreParameters[16] = (EditText) exploreMenuView.findViewById(R.id.testExploreStressTargetLines); // stressTargetLines
        exploreParameters[17] = (EditText) exploreMenuView.findViewById(R.id.testExploreStressAssignmentStrategy); // stressAssignmentStrategy
        exploreParameters[18] = (EditText) exploreMenuView.findViewById(R.id.testExplorePermuteFirst); // permuteFirst
        exploreParameters[19] = (EditText) exploreMenuView.findViewById(R.id.testExplorePermuteSecond); // permuteSecond

        TestCase currTest = findTestCase(testName);
        int basic_parameters = this.getResources().getIdentifier(currTest.paramPresetNames[0], "raw", this.getPackageName());
        int stress_parameters = this.getResources().getIdentifier(currTest.paramPresetNames[1], "raw", this.getPackageName());

        loadExploreParameters(basic_parameters);

        exploreStartButton = (Button) exploreMenuView.findViewById(R.id.testExploreStartButton);
        exploreCloseButton = (Button) exploreMenuView.findViewById(R.id.testExploreCloseButton);
        defaultParamButton = (Button) exploreMenuView.findViewById(R.id.testExploreDefaultParamButton);
        stressParamButton = (Button) exploreMenuView.findViewById(R.id.testExploreStressParamButton);

        dialogBuilder.setView(exploreMenuView);
        exploreDialog = dialogBuilder.create();
        exploreDialog.show();

        // Reset shader type
        shaderType = currTest.shaderNames[0];

        // Initialize shader drop down explore menu
        initializeShaderMenu(testName, exploreMenuView);

        // Load default parameter
        defaultParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                loadExploreParameters(basic_parameters);
            }
        });

        // Load stress parameter
        stressParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                loadExploreParameters(stress_parameters);
            }
        });

        // Start test
        exploreStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("EXPLORER TEST", testName + " STARTING");
                RecyclerView testRV = findRecyclerView(testName);

                if(testRV == null) {
                    Log.e(TAG, testName + " does not exist in any recyclerviews!");
                }
                final LitmusTestAdapter.LitmusTestViewHolder viewHolder = (LitmusTestAdapter.LitmusTestViewHolder) testRV.getChildViewHolder(testRV.getChildAt(position));
                writeExploreParameters(testName, basic_parameters);
                exploreDialog.dismiss();

                enableAllTests(false);

                // Turn this button's color to indicate which test is currently running
                viewHolder.explorerButton.setBackgroundColor(Color.BLUE);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String[] testArgument = new String[4];
                        testArgument[0] = "litmustest_" + testName; // Test Name

                        // Shader Name
                        testArgument[1] = shaderType;

                        testArgument[2] = "litmustest_" + testName + "_results"; // Result Shader Name
                        testArgument[3] = "litmustest_" + testName + "_parameters"; // Parameter Name

                        main(testArgument, false);

                        viewHolder.newExplorerTest = false;

                        enableAllTests(true);

                        Toast.makeText(MainActivity.this, "Test " + testName + " finished!", Toast.LENGTH_LONG).show();
                    }
                }, 500);
            }
        });

        // Close menu
        exploreCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TEST", testName + " MENU CLOSING");
                exploreDialog.dismiss();
            }
        });

    }

    public void explorerTestResult(String testName) {
        Log.i("EXPLORER RESULT", testName + " PRESSED");

        ExplorerResultDialogFragment dialog = new ExplorerResultDialogFragment();

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

            fis.close();
            isr.close();
            br.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        dialog.show(getSupportFragmentManager(), "ExplorerResultDialog");
    }

    public String convertFileToString(String fileName) {
        String result = "";

        try {
            FileInputStream fis = openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }
            result = sb.toString();

            fis.close();
            isr.close();
            br.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public void openTuningMenu(String testName, int position) {
        Log.i("TUNING TEST", testName + " PRESSED, OPENING TUNING MENU");

        dialogBuilder = new AlertDialog.Builder(this);
        final View tuningMenuView = getLayoutInflater().inflate(R.layout.main_test_tuning, null);

        tuningTestName = (TextView) tuningMenuView.findViewById(R.id.testTuningTestName);
        tuningTestName.setText(testName);

        tuningParameters[0] = (EditText) tuningMenuView.findViewById(R.id.testTuningConfigNum); // testConfigNum
        tuningParameters[1] = (EditText) tuningMenuView.findViewById(R.id.testTuningTestIteration); // testIteration
        tuningParameters[2] = (EditText) tuningMenuView.findViewById(R.id.testTuningRandomSeed);

        tuningStartButton = (Button) tuningMenuView.findViewById(R.id.testTuningStartButton);
        tuningCloseButton = (Button) tuningMenuView.findViewById(R.id.testTuningCloseButton);

        dialogBuilder.setView(tuningMenuView);
        tuningDialog = dialogBuilder.create();
        tuningDialog.show();

        TestCase currTest = findTestCase(testName);
        shaderType = currTest.shaderNames[0];

        // Start tuning test
        tuningStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TUNING TEST", testName + " STARTING");
                RecyclerView testRV = findRecyclerView(testName);

                if(testRV == null) {
                    Log.e(TAG, testName + " does not exist in any recyclerviews!");
                }
                final LitmusTestAdapter.LitmusTestViewHolder viewHolder = (LitmusTestAdapter.LitmusTestViewHolder) testRV.getChildViewHolder(testRV.getChildAt(position));
                int tuningConfigNum = Integer.parseInt(tuningParameters[0].getText().toString());
                int tuningTestIteration = Integer.parseInt(tuningParameters[1].getText().toString());
                String tuningRandomSeed = tuningParameters[2].getText().toString();
                ArrayList<TuningResultCase> currTuningResults = new ArrayList<TuningResultCase>();

                tuningDialog.dismiss();

                enableAllTests(false);

                // Turn this button's color to indicate which test is currently running
                viewHolder.tuningButton.setBackgroundColor(Color.BLUE);

                String[] testArgument = new String[4];
                testArgument[0] = "litmustest_" + testName; // Test Name

                // Shader Name
                testArgument[1] = shaderType;

                testArgument[2] = "litmustest_" + testName + "_results"; // Result Shader Name
                testArgument[3] = "litmustest_" + testName + "_parameters"; // Parameter Name

                tuningTestLoop(testName, tuningRandomSeed, testArgument, currTuningResults, 0, tuningConfigNum, viewHolder, tuningTestIteration, currTest);
            }
        });

        // Close menu
        tuningCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TEST", testName + " MENU CLOSING");
                tuningDialog.dismiss();
            }
        });
    }

    public void tuningTestLoop(String testName, String tuningRandomSeed, String[] testArgument, ArrayList<TuningResultCase> currTuningResults, int currConfig, int endConfig,
                                LitmusTestAdapter.LitmusTestViewHolder viewHolder, int tuningTestIteration, TestCase currTest) {

        viewHolder.tuningButton.setText(currConfig+1 + "/" + endConfig);

        writeTuningParameters(testName, tuningRandomSeed, tuningTestIteration, this.getResources().getIdentifier(currTest.paramPresetNames[1], "raw", this.getPackageName()));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                main(testArgument, true);

                // Save param value
                String currParamValue = convertFileToString(currTest.testParamName + ".txt");

                // Save result value
                String currResultValue = convertFileToString(currTest.outputName + ".txt");

                // Go through result and get number of weak behaviors
                String startIndexIndicator = "weak: ";
                String endIndexIndicator = "\nTotal elapsed time";
                String numWeakBehaviors = currResultValue.substring(currResultValue.indexOf(startIndexIndicator) + startIndexIndicator.length(), currResultValue.indexOf(endIndexIndicator));

                // Transfer over the tuning result case
                TuningResultCase currTuningResult = new TuningResultCase(currParamValue, currResultValue, Integer.parseInt(numWeakBehaviors));

                currTuningResults.add(currTuningResult);

                if(currConfig == endConfig - 1) {
                    tuningResultCases.put(testName, currTuningResults);
                    viewHolder.newTuningTest = false;
                    viewHolder.tuningButton.setText("Tuning");
                    enableAllTests(true);
                    Toast.makeText(MainActivity.this, "Tuning Test " + testName + " finished!", Toast.LENGTH_LONG).show();
                }
                else {
                    tuningTestLoop(testName, tuningRandomSeed, testArgument, currTuningResults, currConfig+1, endConfig, viewHolder, tuningTestIteration, currTest);
                }
            }
        }, 500);
    }

    public void tuningTestResult(String testName) {
        Log.i("TUNING RESULT", testName + " PRESSED");

        ArrayList<TuningResultCase> currTestList = tuningResultCases.get(testName);

        if(currTestList == null) {
            Log.e(TAG, testName + " cannot find result cases!");
        }

        TuningResultDialogFragment dialog = new TuningResultDialogFragment(testName, currTestList, MainActivity.this);
        dialog.show(getSupportFragmentManager(), "TuningResultDialog");
    }

    public String getFileDir() {
        return getFilesDir().toString();
    }

    public native int main(String[] testName, boolean tuningMode);
}
