package com.example.litmustestandroid;
import static com.example.litmustestandroid.HelperClass.FileConstants.*;
import static com.example.litmustestandroid.HelperClass.ParameterConstants.*;
import static com.example.litmustestandroid.HelperClass.NewTestCase.TestType;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.JsonWriter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.litmustestandroid.DialogFragments.*;
import com.example.litmustestandroid.Fragment.*;
import com.example.litmustestandroid.HelperClass.*;
import com.example.litmustestandroid.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;

    private DrawerLayout drawer;

    private TestViewObject currTestViewObject;
    private ConformanceTestViewObject conformanceTestViewObject;
    private LockTestViewObject lockTestViewObject;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog exploreDialog;
    private TextView exploreTestName;
    private Button exploreStartButton, exploreCloseButton, defaultParamButton, stressParamButton;
    private String currTestIterations;

    private AlertDialog tuningDialog;
    private TextView tuningTestName;
    private EditText[] tuningParameters = new EditText[6];
    private Button tuningStartButton, tuningCloseButton;
    private Random tuningRandom;
    private String tuningRandomSeed;
    private String[] tuningTestArgument = new String[4];
    private Map<String, String> tuningParameter;
    private int tuningTestWorkgroups, tuningMaxWorkgroups, tuningWorkgroupSize;
    private ArrayList<TuningResultCase> currTuningResults = new ArrayList<TuningResultCase>();
    private HashMap<String, ArrayList<TuningResultCase>> tuningResultCases = new HashMap<>();
    public HashMap<String, ArrayList<ConformanceResultCase>> conformanceTuningResultCases = new HashMap<>();

    private Map<String, EditText> conformanceParamMap;
    private RecyclerView conformanceTestRV;
    private ArrayList<ConformanceResultCase> conformanceTestResults = new ArrayList<ConformanceResultCase>();

    private FileOutputStream conformanceTuningFOS;
    private JsonWriter conformanceTuningResultWriter;

    private static final String TAG = "MainActivity";

    private RunType currTestType;
    private NewTestCase currNewTestCase;
    private String GPUName = "";
    private TestThread testThread;
    private LockTestThread lockTestThread;

    private Handler handler = new Handler();

    private static final String CONFORMANCE_TEST_LIST = "conformance_tests.json";
    private static final String TUNING_TEST_LIST = "tuning_tests.json";
    private static final String MISC_TEST_LIST = "misc_tests.json";

    private HashMap<String, NewTestCase> tuningTests = new HashMap<>();
    private HashMap<String, NewTestCase> conformanceTests = new HashMap<>();
    private HashMap<String, NewTestCase> allTests = new HashMap<>();

    /** Keeps track of what tests the user has currently selected. */
    public HashSet<String> selectedTests = new HashSet<>();
    /** Keeps track of the list of tests when running. */
    private ArrayList<String> runningTests = new ArrayList<>();

    private int curTestIndex;
    private int curConfigIndex;
    private int numConfigs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new Introduction()).commit();
        navigationView.setCheckedItem(R.id.nav_introduction);

        registerTestCases();
        initFileConfig();

        // Keeps screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_introduction:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new Introduction()).commit();
                break;
            case R.id.nav_conformance_test:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ConformanceTest()).commit();
                break;
            case R.id.nav_lock_test:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new LockTest()).commit();
                break;
            case R.id.nav_message_passing:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MessagePassing()).commit();
                break;
            case R.id.nav_store:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new Store()).commit();
                break;
            case R.id.nav_read:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new Read()).commit();
                break;
            case R.id.nav_load_buffer:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new LoadBuffer()).commit();
                break;
            case R.id.nav_store_buffer:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new StoreBuffer()).commit();
                break;
            case R.id.nav_22_write:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new Write22()).commit();
                break;
            case R.id.nav_corr:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CoRR()).commit();
                break;
            case R.id.nav_coww:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CoWW()).commit();
                break;
            case R.id.nav_cowr:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CoWR()).commit();
                break;
            case R.id.nav_corw1:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CoRW1()).commit();
                break;
            case R.id.nav_corw2:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CoRW2()).commit();
                break;
            case R.id.nav_atomicity:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new Atomicity()).commit();
                break;
            case R.id.nav_barrier_store_load:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new BarrierStoreLoad()).commit();
                break;
            case R.id.nav_barrier_load_store:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new BarrierLoadStore()).commit();
                break;
            case R.id.nav_barrier_store_store:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new BarrierStoreStore()).commit();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
    }

    private String JsonDataFromAsset(String name) {
        String json = null;
        try {
            InputStream inputStream = getAssets().open(name);
            int sizeOfFile = inputStream.available();
            byte[] bufferData = new byte[sizeOfFile];
            inputStream.read(bufferData);
            inputStream.close();
            json = new String(bufferData, "UTF-8");
            System.out.println(json);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    /** Loads tests from the specified file. */
    private void loadTests(String testFile, HashMap<String, NewTestCase> testList) {
        try {
            JSONArray testArray = new JSONArray(JsonDataFromAsset(testFile));
            for (int i = 0; i < testArray.length(); i++) {
                JSONObject testData = testArray.getJSONObject(i);

                String testName = testData.getString("name");
                NewTestCase testCase = new NewTestCase(
                        testName,
                        testData.getString("shader"),
                        testData.getString("resultShader"),
                        NewTestCase.TestType.valueOf(testData.getString("type"))
                );
                testList.put(testName, testCase);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /** Loads conformance, tuning, and misc tests. */
    private void registerTestCases() {
        loadTests(CONFORMANCE_TEST_LIST, conformanceTests);
        allTests.putAll(conformanceTests);
        loadTests(TUNING_TEST_LIST, tuningTests);
        allTests.putAll(tuningTests);
        loadTests(MISC_TEST_LIST, allTests);
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
        // some shaders use the same result file, so keep track of which ones we've already copied
        Set<String> seenResultFiles = new HashSet<>();
        for (NewTestCase testCase : allTests.values()) {
            // copy shader file
            int shaderId = this.getResources().getIdentifier(testCase.getShaderFile(), "raw", this.getPackageName());
            Log.d(TAG, "File: " + testCase.getShaderFile() + ".spv copying to " + getFilesDir().toString());
            copyFile(shaderId, testCase.getShaderFile() + ".spv");
            if (!seenResultFiles.contains(testCase.getResultFile())) {
                seenResultFiles.add(testCase.getResultFile());
                // copy result file
                int resultShaderId = this.getResources().getIdentifier(testCase.getResultFile(), "raw", this.getPackageName());
                copyFile(resultShaderId, testCase.getResultFile() + ".spv");
                Log.d(TAG, "File: " + testCase.getResultFile() + ".spv copied to " + getFilesDir().toString());
            }
        }
        // Transfer the other files
        for (String file : allFiles) {
            int fileId = this.getResources().getIdentifier(file, "raw", this.getPackageName());
            copyFile(fileId, file + ".txt");
            Log.d(TAG, "File: " + file + ".txt copied to " + getFilesDir().toString());
        }
    }

    // Automatically fill parameters with basic values
    public void loadParameters(Map<String, EditText> parameters, int paramValue){
        InputStream inputStream = getResources().openRawResource(paramValue);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String line = bufferedReader.readLine();
            while (line != null) {
                String[] words = line.split("=");
                parameters.get(words[0]).setText(words[1]);
                line = bufferedReader.readLine();
            }
            inputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void writeNonOverrideableParams(FileOutputStream fos, TestType testType) throws IOException {
        for (Map.Entry<String, Integer> entry : nonOverrideableParams.entrySet()) {
            int value;
            if (testType == TestType.COHERENCY && coherencyOverrides.containsKey(entry.getKey())) {
                value = coherencyOverrides.get(entry.getKey());
            } else {
                value = entry.getValue();
            }
            String outputLine = entry.getKey() + "=" + value + "\n";
            fos.write(outputLine.getBytes());
        }
    }

    // Read and write current parameters value for testing
    public void writeParameters(Map<String, EditText> parameters, TestType testType) {
        String fileName = PARAMETERS_FILE + ".txt";
        try{
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            // write user chosen parameters
            for (Map.Entry<String, EditText> entry : parameters.entrySet()) {
                if (entry.getKey() == ITERATIONS) {
                    currTestIterations = entry.getValue().getText().toString();
                }
                String outputLine = entry.getKey() + "=" + entry.getValue().getText().toString() + "\n";
                fos.write(outputLine.getBytes());
            }
            // write non overrideable parameters and coherency params, if they apply
            writeNonOverrideableParams(fos, testType);
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

    public int randomGenerator(int min, int max) {
        return tuningRandom.nextInt() % (max - min + 1) + min;
    }

    public int roundedPercentage() {
        return (randomGenerator(0, 100) / 5) * 5;
    }

    public int getPercentage(boolean smoothedParameters) {
        if (smoothedParameters) {
            return roundedPercentage();
        }
        else {
            return randomGenerator(0, 100);
        }
    }

    public void writeTuningParameters(TestType testType, boolean reset) {
        if (reset) {
            boolean smoothedParameters = false;
            int workgroupLimiter = tuningMaxWorkgroups;

            int testingWorkgroups = randomGenerator(tuningTestWorkgroups, workgroupLimiter);
            int workgroupSize = randomGenerator(1, tuningWorkgroupSize);
            int maxWorkgroups = randomGenerator(testingWorkgroups, workgroupLimiter);
            int stressLineSize = (int) Math.pow(2, randomGenerator(2, 10));
            int stressTargetLines = randomGenerator(1, 16);
            int memStride = randomGenerator(1, 7);
            tuningParameter = new TreeMap<>();
            tuningParameter.put("iterations", currTestIterations);
            tuningParameter.put("testingWorkgroups", Integer.toString(testingWorkgroups));
            tuningParameter.put("maxWorkgroups", Integer.toString(maxWorkgroups));
            tuningParameter.put("workgroupSize", Integer.toString(workgroupSize));
            tuningParameter.put("shufflePct", Integer.toString(getPercentage(smoothedParameters)));
            tuningParameter.put("barrierPct", Integer.toString(getPercentage(smoothedParameters)));
            tuningParameter.put("scratchMemorySize", Integer.toString(32 * stressLineSize * stressTargetLines));
            tuningParameter.put("memStride", Integer.toString(memStride));
            tuningParameter.put("memStressPct", Integer.toString(getPercentage(smoothedParameters)));
            tuningParameter.put("preStressPct", Integer.toString(getPercentage(smoothedParameters)));
            tuningParameter.put("memStressIterations", Integer.toString(randomGenerator(0, 1024)));
            tuningParameter.put("preStressIterations", Integer.toString(randomGenerator(0, 128)));
            tuningParameter.put("stressLineSize", Integer.toString(stressLineSize));
            tuningParameter.put("stressTargetLines", Integer.toString(stressTargetLines));
            tuningParameter.put("stressStrategyBalancePct", Integer.toString(getPercentage(smoothedParameters)));
            tuningParameter.put("memStressStoreFirstPct", Integer.toString(getPercentage(smoothedParameters)));
            tuningParameter.put("memStressStoreSecondPct", Integer.toString(getPercentage(smoothedParameters)));
            tuningParameter.put("preStressStoreFirstPct", Integer.toString(getPercentage(smoothedParameters)));
            tuningParameter.put("preStressStoreSecondPct", Integer.toString(getPercentage(smoothedParameters)));
        }
        // Now insert the parameter values to text file that will be used during test
        try {
            FileOutputStream fos = openFileOutput(PARAMETERS_FILE + ".txt", Context.MODE_PRIVATE);
            for (String key : tuningParameter.keySet()) {
                fos.write((key + "=" + tuningParameter.get(key) + "\n").getBytes());
            }
            writeNonOverrideableParams(fos, testType);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        try {
            FileInputStream fis = openFileInput(PARAMETERS_FILE + ".txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String text;
            while ((text = br.readLine()) != null) {
                Log.i(TAG, text);
            }

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
    }



    public void handleButtons(boolean testBegin, Button[] buttons, ResultButton[] resultButtons) {
        // button[0] = currently running test's button
        // button[1+] = currently not running test's buttons
        // resultButton[0] = currently running test's result button
        // resultButton[1+] = currently not running test's result buttons
        if(testBegin) { // Test starting
            for(int i = 0; i < buttons.length; i++) {
                buttons[i].setEnabled(false);

                if(i == 0) {
                    buttons[i].setBackgroundColor(getResources().getColor(R.color.cyan));
                }
                else {
                    buttons[i].setBackgroundColor(getResources().getColor(R.color.lightgray));
                }
                resultButtons[i].button.setEnabled(false);
                resultButtons[i].button.setBackgroundColor(getResources().getColor(R.color.lightgray));
            }
        }
        else { // Test ended
            for(int i = 0; i < buttons.length; i++) {
                buttons[i].setEnabled(true);
                buttons[i].setBackgroundColor(getResources().getColor(R.color.lightblue));
            }

            resultButtons[0].button.setEnabled(true);
            resultButtons[0].button.setBackgroundColor(getResources().getColor(R.color.red));
            resultButtons[0].isNew = false;

            for(int i = 0; i < resultButtons.length; i++) {
                if(!resultButtons[i].isNew) {
                    resultButtons[i].button.setEnabled(true);
                    resultButtons[i].button.setBackgroundColor(getResources().getColor(R.color.red));
                }
            }
        }
    }

    public void openExploreMenu(String testName, TestViewObject testViewObject) {
        Log.i("TEST", testName + " PRESSED, OPENING Explore MENU");

        currTestViewObject = testViewObject;
        currTestType = RunType.EXPLORER;

        dialogBuilder = new AlertDialog.Builder(this);
        final View exploreMenuView = getLayoutInflater().inflate(R.layout.main_test_explore, null);

        exploreTestName = (TextView) exploreMenuView.findViewById(R.id.testExploreTestName);
        exploreTestName.setText(testName);

        HashMap<String, EditText> exploreParamMap = new HashMap<>();
        exploreParamMap.put(ITERATIONS, exploreMenuView.findViewById(R.id.testExploreTestIteration));
        exploreParamMap.put(TESTING_WORKGROUPS, exploreMenuView.findViewById(R.id.testExploreTestingWorkgroups));
        exploreParamMap.put(MAX_WORKGROUPS, exploreMenuView.findViewById(R.id.testExploreMaxWorkgroups));
        exploreParamMap.put(WORKGROUP_SIZE, exploreMenuView.findViewById(R.id.testExploreWorkgroupSize));
        exploreParamMap.put(SHUFFLE_PCT, exploreMenuView.findViewById(R.id.testExploreShufflePct));
        exploreParamMap.put(BARRIER_PCT, exploreMenuView.findViewById(R.id.testExploreBarrierPct));
        exploreParamMap.put(SCRATCH_MEMORY_SIZE, exploreMenuView.findViewById(R.id.testExploreScratchMemorySize));
        exploreParamMap.put(MEM_STRIDE, exploreMenuView.findViewById(R.id.testExploreScratchMemorySize));
        exploreParamMap.put(MEM_STRESS_PCT, exploreMenuView.findViewById(R.id.testExploreMemoryStressPct));
        exploreParamMap.put(MEM_STRESS_ITERATIONS, exploreMenuView.findViewById(R.id.testExploreMemoryStressIterations));
        exploreParamMap.put(MEM_STRESS_STORE_FIRST_PCT, exploreMenuView.findViewById(R.id.testExploreMemoryStressStoreFirstPct));
        exploreParamMap.put(MEM_STRESS_STORE_SECOND_PCT, exploreMenuView.findViewById(R.id.testExploreMemoryStressStoreSecondPct));
        exploreParamMap.put(PRE_STRESS_PCT, exploreMenuView.findViewById(R.id.testExplorePreStressPct));
        exploreParamMap.put(PRE_STRESS_ITERATIONS, exploreMenuView.findViewById(R.id.testExplorePreStressIterations));
        exploreParamMap.put(PRE_STRESS_STORE_FIRST_PCT, exploreMenuView.findViewById(R.id.testExplorePreStressStoreFirstPct));
        exploreParamMap.put(PRE_STRESS_STORE_SECOND_PCT, exploreMenuView.findViewById(R.id.testExplorePreStressStoreSecondPct));
        exploreParamMap.put(STRESS_LINE_SIZE, exploreMenuView.findViewById(R.id.testExploreStressLineSize));
        exploreParamMap.put(STRESS_TARGET_LINES, exploreMenuView.findViewById(R.id.testExploreStressTargetLines));
        exploreParamMap.put(STRESS_STRATEGY_BALANCE_PCT, exploreMenuView.findViewById(R.id.testExploreStressStrategyBalancePct));

        currNewTestCase = allTests.get(testName);
        int basic_parameters = getResources().getIdentifier(BASIC_PARAM_FILE, "raw", this.getPackageName());
        int stress_parameters = getResources().getIdentifier(STRESS_PARAM_FILE, "raw", this.getPackageName());

        loadParameters(exploreParamMap, basic_parameters);

        exploreStartButton = (Button) exploreMenuView.findViewById(R.id.testExploreStartButton);
        exploreCloseButton = (Button) exploreMenuView.findViewById(R.id.testExploreCloseButton);
        defaultParamButton = (Button) exploreMenuView.findViewById(R.id.testExploreDefaultParamButton);
        stressParamButton = (Button) exploreMenuView.findViewById(R.id.testExploreStressParamButton);

        dialogBuilder.setView(exploreMenuView);
        exploreDialog = dialogBuilder.create();
        exploreDialog.show();

        // Load default parameter
        defaultParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                loadParameters(exploreParamMap, basic_parameters);
            }
        });

        // Load stress parameter
        stressParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                loadParameters(exploreParamMap, stress_parameters);
            }
        });

        // Start test
        exploreStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("EXPLORER TEST", testName + " STARTING");

                writeParameters(exploreParamMap, currNewTestCase.getTestType());
                exploreDialog.dismiss();

                // Disable buttons and change their color
                handleButtons(true, testViewObject.buttons, testViewObject.resultButtons);

                // Make progress layout visible
                testViewObject.explorerProgressLayout.setVisibility(View.VISIBLE);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String[] testArgument = new String[4];
                        testArgument[0] = testName; // Test Name

                        // Shader Name
                        testArgument[1] = currNewTestCase.getShaderFile(); // Current selected shader
                        testArgument[2] = currNewTestCase.getResultFile(); // Result Shader
                        testArgument[3] = PARAMETERS_FILE; // Txt file that stores parameter

                        testThread = new TestThread(MainActivity.this, testArgument, false, false);
                        testThread.start();
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

    public void displayTestResult(String testName) {
        Log.i("RESULT", testName + " PRESSED");

        TestResultDialogFragment dialog = new TestResultDialogFragment();

        try
        {
            FileInputStream fis = openFileInput(OUTPUT_FILE + ".txt");
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

        dialog.show(getSupportFragmentManager(), "TestResultDialog");
    }

    public void displayLockTestResult(String shaderName) {
        TestResultDialogFragment dialog = new TestResultDialogFragment();
        try
        {
            FileInputStream fis = openFileInput(shaderName + "_output.txt");
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
        dialog.show(getSupportFragmentManager(), "TestResultDialog");
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

    public void openTuningMenu(String testName, TestViewObject testViewObject) {
        Log.i("TUNING TEST", testName + " PRESSED, OPENING TUNING MENU");

        currTestViewObject = testViewObject;
        currTestType = RunType.TUNING;

        dialogBuilder = new AlertDialog.Builder(this);
        final View tuningMenuView = getLayoutInflater().inflate(R.layout.main_test_tuning, null);

        tuningTestName = (TextView) tuningMenuView.findViewById(R.id.testTuningTestName);
        tuningTestName.setText(testName);

        tuningParameters[0] = (EditText) tuningMenuView.findViewById(R.id.testTuningConfigNum); // testConfigNum
        tuningParameters[1] = (EditText) tuningMenuView.findViewById(R.id.testTuningTestIteration); // testIteration
        tuningParameters[2] = (EditText) tuningMenuView.findViewById(R.id.testTuningRandomSeed); // randomSeed
        tuningParameters[3] = (EditText) tuningMenuView.findViewById(R.id.testTuningTestingWorkgroups); // testingWorkgroups
        tuningParameters[4] = (EditText) tuningMenuView.findViewById(R.id.testTuningMaxWorkgroups); // maxWorkgroups
        tuningParameters[5] = (EditText) tuningMenuView.findViewById(R.id.testTuningWorkgroupSize); // workgroupSize

        tuningStartButton = (Button) tuningMenuView.findViewById(R.id.testTuningStartButton);
        tuningCloseButton = (Button) tuningMenuView.findViewById(R.id.testTuningCloseButton);

        dialogBuilder.setView(tuningMenuView);
        tuningDialog = dialogBuilder.create();
        tuningDialog.show();

        currNewTestCase = allTests.get(testName);

        // Start tuning test
        tuningStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TUNING TEST", testName + " STARTING");

                currTuningResults = new ArrayList<TuningResultCase>();
                tuningRandomSeed = tuningParameters[2].getText().toString();
                tuningTestWorkgroups = Integer.parseInt(tuningParameters[3].getText().toString());
                tuningMaxWorkgroups = Integer.parseInt(tuningParameters[4].getText().toString());
                tuningWorkgroupSize =  Integer.parseInt(tuningParameters[5].getText().toString());

                tuningDialog.dismiss();

                currTestIterations = tuningParameters[1].getText().toString();

                // Disable buttons and change their color
                handleButtons(true, testViewObject.buttons, testViewObject.resultButtons);

                // Make progress layout visible
                testViewObject.tuningProgressLayout.setVisibility(View.VISIBLE);

                tuningTestArgument[0] = testName; // Test Name

                // Shader Name
                tuningTestArgument[1] = currNewTestCase.getShaderFile(); // Current selected shader
                tuningTestArgument[2] = currNewTestCase.getResultFile(); // Result Shader
                tuningTestArgument[3] = PARAMETERS_FILE; // Txt file that stores parameter

                curConfigIndex = 0;
                numConfigs = Integer.parseInt(tuningParameters[0].getText().toString());

                if(tuningRandomSeed.length() == 0) {
                    tuningRandom = new PRNG(new Random().nextInt());
                }
                else {
                    tuningRandom = new PRNG(tuningRandomSeed);
                }

                tuningTestLoop();
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

    public void tuningTestLoop() {

        currTestViewObject.tuningCurrentConfigNumber.setText(curConfigIndex+1 + "/" + numConfigs);

        writeTuningParameters(currNewTestCase.getTestType(), true);

        // Run test in different thread
        testThread = new TestThread(MainActivity.this, tuningTestArgument, true, false);
        testThread.start();
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

    /** Called by C++ driver. */
    public void iterationProgress(String iterationNum) { ;
        //Log.i(TAG, "IterationProgress: " + iterationNum + "/" + currTestIterations);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(currTestType.equals(RunType.EXPLORER)) {
                    currTestViewObject.explorerCurrentIterationNumber.setText(iterationNum + "/" + currTestIterations);
                }
                else if (currTestType.equals(RunType.TUNING)) {
                    currTestViewObject.tuningCurrentIterationNumber.setText(iterationNum + "/" + currTestIterations);
                }
                else if (currTestType.equals(RunType.MULTI_EXPLORER)){ // Conformance Explorer
                    conformanceTestViewObject.currentIterationNumber.setText(iterationNum + "/" + currTestIterations);
                }
                else if (currTestType.equals(RunType.MULTI_TUNING)){ // Conformance Explorer
                    conformanceTestViewObject.currentIterationNumber.setText(iterationNum + "/" + currTestIterations);
                }
                else { // Lock Test
                    lockTestViewObject.currentIterationNumber.setText(iterationNum + "/" + currTestIterations);
                }
            }
        });
    }

    /** Called by C++ driver. */
    public void setGPUName(String gpuName) {
        GPUName = gpuName;
        Log.i(TAG, gpuName);
    }

    public void sendResultEmail(RunType testMode) {
        Log.i(TAG, "Sending result via email");

        String subject = "Android Vulkan Memory Model Testing";
        String message = "GPU: " + GPUName;

        if(testMode.equals(RunType.MULTI_EXPLORER)) {
            subject += " MultiTest Explorer Result";
        }
        else if (testMode.equals(RunType.MULTI_TUNING)) {
            subject += " MultiTest Tuning Result";
        }

        File fileLocation = new File(getFileDir(), RESULT_FILE);
        Uri path = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", fileLocation);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);

        emailIntent.setType("plain/text");

        Intent chooser = Intent.createChooser(emailIntent, "Share File");

        List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            this.grantUriPermission(packageName, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        try {
            startActivity(chooser);
        }
        catch(android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
        }
    }

    public void conformanceTestCheckBoxesListener(View view) {
        CheckBox currCheckBox = (CheckBox)view;
        String shaderName = view.getTag().toString();
        if(currCheckBox.isChecked()) { // Clicked
            selectedTests.add(shaderName);
        }
        else { // Un-clicked
            selectedTests.remove(shaderName);
        }
    }

    public void beginRunningTests(
            RunType runType,
            ConformanceTestViewObject viewObject,
            RecyclerView resultRV) {
        currTestType = runType;
        conformanceTestViewObject = viewObject;
        conformanceTestRV = resultRV;
        if(selectedTests.size() == 0) { // No test selected
            Toast.makeText(MainActivity.this, "No test selected!", Toast.LENGTH_LONG).show();
            return;
        }
        // Disable start button
        conformanceTestViewObject.startButton.setEnabled(false);
        conformanceTestViewObject.startButton.setBackgroundColor(getResources().getColor(R.color.cyan));
        // Set progress layout visible
        conformanceTestViewObject.progressLayout.setVisibility(View.VISIBLE);
        // Set config layout visible
        conformanceTestViewObject.configLayout.setVisibility(View.VISIBLE);
        conformanceTestViewObject.resultLayout.setVisibility(View.GONE);
        conformanceTestResults = new ArrayList<>();

        runningTests.clear();
        runningTests.addAll(selectedTests);
        curTestIndex = 0;
        curConfigIndex = 0;
        if (runType.equals(RunType.MULTI_EXPLORER)) {
            conformanceExplorerTestLoop();
        } else if (runType.equals(RunType.MULTI_TUNING)) {
            conformanceTuningTestLoop();
        }
    }

    public void conformanceExplorerTestBegin(Map<String, EditText> parameters, ConformanceTestViewObject viewObject, RecyclerView resultRV) {
        conformanceParamMap = parameters;
        numConfigs = 1;
        beginRunningTests(RunType.MULTI_EXPLORER, viewObject, resultRV);
    }

    public void conformanceExplorerTestLoop()  {
        String testName = runningTests.get(curTestIndex);
        currNewTestCase = allTests.get(testName);
        String[] testArgument = new String[4];
        testArgument[0] = testName; // Test Name
        // Shader Name
        testArgument[1] = currNewTestCase.getShaderFile(); // Current selected shader
        testArgument[2] = currNewTestCase.getResultFile();
        testArgument[3] = PARAMETERS_FILE; // Txt file that stores parameter
        // Update test name
        conformanceTestViewObject.currentTestName.setText(currNewTestCase.getTestName());
        conformanceTestViewObject.currentConfigNumber.setText(curConfigIndex+1 + "/" + numConfigs);

        writeParameters(conformanceParamMap, currNewTestCase.getTestType());

        // Run test in different thread
        testThread = new TestThread(MainActivity.this, testArgument, false, true);
        testThread.start();
    }

    public void conformanceTuningTestBegin(EditText[] parameters, ConformanceTestViewObject viewObject, RecyclerView resultRV) {
        currTestIterations = parameters[1].getText().toString();
        tuningRandomSeed = parameters[2].getText().toString();
        tuningTestWorkgroups = Integer.parseInt(parameters[3].getText().toString());
        tuningMaxWorkgroups = Integer.parseInt(parameters[4].getText().toString());
        tuningWorkgroupSize = Integer.parseInt(parameters[5].getText().toString());
        numConfigs = Integer.parseInt(parameters[0].getText().toString());
        if(tuningRandomSeed.length() == 0) {
            tuningRandom = new PRNG(new Random().nextInt());
        }
        else {
            tuningRandom = new PRNG(tuningRandomSeed);
        }
        try {
            conformanceTuningFOS = openFileOutput(RESULT_FILE, Context.MODE_PRIVATE);
            conformanceTuningResultWriter = new JsonWriter(new OutputStreamWriter(conformanceTuningFOS, "UTF-8"));
            conformanceTuningResultWriter.setIndent("  ");
            conformanceTuningResultWriter.beginArray();
            conformanceTuningResultWriter.beginObject();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        beginRunningTests(RunType.MULTI_TUNING, viewObject, resultRV);
    }

    public void conformanceTuningTestLoop()  {
        String testName = runningTests.get(curTestIndex);
        currNewTestCase = allTests.get(testName);
        String[] testArgument = new String[4];
        testArgument[0] = testName; // Test Name
        // Shader Name
        testArgument[1] = currNewTestCase.getShaderFile(); // Current selected shader
        // Choosing result shader
        testArgument[2] = currNewTestCase.getResultFile();
        testArgument[3] = PARAMETERS_FILE; // Txt file that stores parameter
        // Update test name
        conformanceTestViewObject.currentTestName.setText(testName);
        // Update current config number
        conformanceTestViewObject.currentConfigNumber.setText(curConfigIndex+1 + "/" + numConfigs);

        Log.i(TAG, "TestName: " + testName + " Shader: " + currNewTestCase.getShaderFile());

        boolean reset = curTestIndex == 0;
        writeTuningParameters(currNewTestCase.getTestType(), reset);

        // Run test in different thread
        testThread = new TestThread(MainActivity.this, testArgument, false, true);
        testThread.start();
    }

    public void lockTestBegin(LockTestViewObject viewObject, String shaderName, boolean checkCorrect) {
        currTestType = RunType.LOCK_TEST;
        lockTestViewObject = viewObject;

        handleButtons(true, viewObject.buttons, viewObject.resultButtons);

        // Set progress layout visible
        lockTestViewObject.progressLayout.setVisibility(View.VISIBLE);

        currTestIterations = lockTestViewObject.testIteration.getText().toString();

        // Start multi tuning test loop
        String[] testArgument = new String[5];
        testArgument[0] = "lockTest"; // Test Name
        testArgument[1] = shaderName; // Shader Name
        testArgument[2] = lockTestViewObject.testIteration.getText().toString();// Test Iteration
        testArgument[3] = lockTestViewObject.workgroupNumber.getText().toString();// Workgroup Number
        testArgument[4] = lockTestViewObject.workgroupSize.getText().toString();// Workgroup Size

        // Run test in different thread
        lockTestThread = new LockTestThread(MainActivity.this, testArgument, checkCorrect);
        lockTestThread.start();

    }

    private void writeToExternalStorage(String fileName) {
        try {
            FileInputStream fis = openFileInput(fileName);
            FileChannel inChannel = fis.getChannel();
            File output = new File(getExternalFilesDir(Environment.DIRECTORY_DCIM),fileName);
            FileChannel outChannel =  new FileOutputStream(output).getChannel();

            inChannel.transferTo(0, inChannel.size(), outChannel);
            fis.close();
            inChannel.close();
            outChannel.close();
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

    public void testComplete() {
        if(testThread != null) {
            testThread.interrupt();
            testThread = null;
        }
        if(lockTestThread != null) {
            lockTestThread.interrupt();
            lockTestThread = null;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "testComplete");
                if(currTestType.equals(RunType.EXPLORER)) {
                    // Enable buttons and change their color
                    handleButtons(false, currTestViewObject.buttons, currTestViewObject.resultButtons);
                    currTestViewObject.explorerProgressLayout.setVisibility(View.GONE);
                    writeToExternalStorage(OUTPUT_FILE + ".txt");
                    Toast.makeText(MainActivity.this, "Test " + currTestViewObject.testName + " finished!", Toast.LENGTH_LONG).show();
                }
                else if (currTestType.equals(RunType.TUNING)) {
                    // Save param value
                    String currParamValue = convertFileToString(PARAMETERS_FILE + ".txt");

                    // Save result value
                    String currResultValue = convertFileToString(OUTPUT_FILE + ".txt");

                    // Go through result and get number of sequential behaviors
                    String startIndexIndicator = "seq: ";
                    String endIndexIndicator = "\ninterleaved:";
                    String numSeqBehaviors = currResultValue.substring(currResultValue.indexOf(startIndexIndicator) + startIndexIndicator.length(), currResultValue.indexOf(endIndexIndicator));

                    // Go through result and get number of interleaved behaviors
                    startIndexIndicator = "interleaved: ";
                    endIndexIndicator = "\nweak:";
                    String numInterleavedBehaviors = currResultValue.substring(currResultValue.indexOf(startIndexIndicator) + startIndexIndicator.length(), currResultValue.indexOf(endIndexIndicator));

                    // Go through result and get number of weak behaviors
                    startIndexIndicator = "weak: ";
                    endIndexIndicator = "\nTotal elapsed time";
                    String numWeakBehaviors = currResultValue.substring(currResultValue.indexOf(startIndexIndicator) + startIndexIndicator.length(), currResultValue.indexOf(endIndexIndicator));

                    // Transfer over the tuning result case
                    TuningResultCase currTuningResult = new TuningResultCase(currNewTestCase.getTestName(), currParamValue, currResultValue,
                            Integer.parseInt(numSeqBehaviors), Integer.parseInt(numInterleavedBehaviors), Integer.parseInt(numWeakBehaviors));

                    currTuningResults.add(currTuningResult);

                    if(curConfigIndex == numConfigs - 1) {
                        tuningResultCases.put(currTestViewObject.testName, currTuningResults);

                        // Enable buttons and change their color
                        handleButtons(false, currTestViewObject.buttons, currTestViewObject.resultButtons);

                        currTestViewObject.tuningProgressLayout.setVisibility(View.GONE);

                        Toast.makeText(MainActivity.this, "Tuning Test " + currTestViewObject.testName + " finished!", Toast.LENGTH_LONG).show();
                    }
                    else {
                        curConfigIndex++;
                        tuningTestLoop();
                    }
                }
                else if (currTestType.equals(RunType.MULTI_EXPLORER)) {
                    // Save param value
                    String currParamValue = convertFileToString(PARAMETERS_FILE + ".txt");

                    // Save result value
                    String currResultValue = convertFileToString(OUTPUT_FILE + ".txt");

                    // Go through result and get number of weak behaviors
                    String startIndexIndicator = "Non-weak: ";
                    String endIndexIndicator = "\nWeak:";
                    String numNonWeakBehaviors = currResultValue.substring(currResultValue.indexOf(startIndexIndicator) + startIndexIndicator.length(), currResultValue.indexOf(endIndexIndicator));


                    // Go through result and get number of weak behaviors
                    startIndexIndicator = "Weak: ";
                    endIndexIndicator = "\nTotal elapsed time";
                    String numWeakBehaviors = currResultValue.substring(currResultValue.indexOf(startIndexIndicator) + startIndexIndicator.length(), currResultValue.indexOf(endIndexIndicator));

                    // Transfer over the tuning result case
                    ConformanceResultCase currConformanceResult = new ConformanceResultCase(conformanceTestViewObject.currentTestName.getText().toString(), currParamValue, currResultValue,
                            Integer.parseInt(numNonWeakBehaviors), Integer.parseInt(numWeakBehaviors));

                    conformanceTestResults.add(currConformanceResult);

                    if(curTestIndex == runningTests.size() - 1) { // All test ended, update result

                        Toast.makeText(MainActivity.this, "All tests have been completed!", Toast.LENGTH_LONG).show();

                        // Write a json file
                        try {
                            FileOutputStream conformanceFOS = openFileOutput(RESULT_FILE, Context.MODE_PRIVATE);
                            JsonWriter conformanceResultWriter = new JsonWriter(new OutputStreamWriter(conformanceFOS, "UTF-8"));
                            conformanceResultWriter.setIndent("  ");
                            conformanceResultWriter.beginArray();
                            conformanceResultWriter.beginObject();
                            conformanceResultWriter.name("0");
                            conformanceResultWriter.beginObject();

                            // Result
                            for(int i = 0; i < conformanceTestResults.size(); i++) {
                                ConformanceResultCase resultCase = conformanceTestResults.get(i);

                                String resultName = resultCase.testName;

                                conformanceResultWriter.name(resultName);
                                conformanceResultWriter.beginObject();
                                conformanceResultWriter.name("Non-weak").value(resultCase.numNonWeakBehaviors);
                                conformanceResultWriter.name("Weak").value(resultCase.numWeakBehaviors);
                                conformanceResultWriter.endObject();
                            }

                            // Parameter
                            conformanceResultWriter.name("Test Parameters");
                            conformanceResultWriter.beginObject();

                            FileInputStream fis = openFileInput(PARAMETERS_FILE + ".txt");
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader br = new BufferedReader(isr);

                            String line = br.readLine();
                            while (line != null) {
                                String[] words = line.split("=");
                                if (!nonOverrideableParams.containsKey(words[0])) {
                                    conformanceResultWriter.name(words[0]).value(Integer.parseInt(words[1]));
                                }
                                line = br.readLine();
                            }
                            fis.close();
                            isr.close();
                            br.close();
                            conformanceResultWriter.endObject();
                            conformanceResultWriter.endObject();

                            conformanceResultWriter.name("gpu").value(GPUName);
                            conformanceResultWriter.name("testCount").value(numConfigs);

                            conformanceResultWriter.endObject();
                            conformanceResultWriter.endArray();
                            conformanceResultWriter.close();
                            conformanceFOS.close();

                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        writeToExternalStorage(RESULT_FILE);
                        // Enable start button
                        conformanceTestViewObject.startButton.setEnabled(true);
                        conformanceTestViewObject.startButton.setBackgroundColor(getResources().getColor(R.color.lightblue));

                        // Set progress layout invisible
                        conformanceTestViewObject.progressLayout.setVisibility(View.GONE);

                        // Set result layout visible
                        conformanceTestViewObject.resultLayout.setVisibility(View.VISIBLE);

                        // Update result
                        ConformanceTestResultAdapter conformanceTestResultAdapter = new ConformanceTestResultAdapter(MainActivity.this, conformanceTestResults);
                        conformanceTestRV.setAdapter(conformanceTestResultAdapter);
                        conformanceTestRV.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        conformanceTestRV.addItemDecoration(new DividerItemDecoration(MainActivity.this, LinearLayoutManager.VERTICAL));
                    }
                    else {
                        curTestIndex++;
                        conformanceExplorerTestLoop();
                    }
                }
                else if (currTestType.equals(RunType.MULTI_TUNING)) { // Conformance Tuning
                    // Save param value
                    String currParamValue = convertFileToString(PARAMETERS_FILE + ".txt");

                    // Save result value
                    String currResultValue = convertFileToString(OUTPUT_FILE + ".txt");

                    // Go through result and get number of weak behaviors
                    String startIndexIndicator = "Non-weak: ";
                    String endIndexIndicator = "\nWeak:";
                    String numNonWeakBehaviors = currResultValue.substring(currResultValue.indexOf(startIndexIndicator) + startIndexIndicator.length(), currResultValue.indexOf(endIndexIndicator));


                    // Go through result and get number of weak behaviors
                    startIndexIndicator = "Weak: ";
                    endIndexIndicator = "\nTotal elapsed time";
                    String numWeakBehaviors = currResultValue.substring(currResultValue.indexOf(startIndexIndicator) + startIndexIndicator.length(), currResultValue.indexOf(endIndexIndicator));

                    // Transfer over the tuning result case
                    ConformanceResultCase currConformanceResult = new ConformanceResultCase(conformanceTestViewObject.currentTestName.getText().toString(), currParamValue, currResultValue,
                            Integer.parseInt(numNonWeakBehaviors), Integer.parseInt(numWeakBehaviors));

                    conformanceTestResults.add(currConformanceResult);

                    if(curTestIndex == runningTests.size() - 1) { // One tuning config test completed
                        conformanceTuningResultCases.put(Integer.toString(curConfigIndex), conformanceTestResults);

                        // Append to the result file
                        try {
                            conformanceTuningResultWriter.name(Integer.toString(curConfigIndex));
                            conformanceTuningResultWriter.beginObject();

                            // Result
                            for(int i = 0; i < conformanceTestResults.size(); i++) {
                                ConformanceResultCase resultCase = conformanceTestResults.get(i);

                                String resultName = resultCase.testName;

                                conformanceTuningResultWriter.name(resultName);
                                conformanceTuningResultWriter.beginObject();
                                conformanceTuningResultWriter.name("Non-weak").value(resultCase.numNonWeakBehaviors);
                                conformanceTuningResultWriter.name("Weak").value(resultCase.numWeakBehaviors);
                                conformanceTuningResultWriter.endObject();
                            }

                            // Parameter
                            conformanceTuningResultWriter.name("Test Parameters");
                            conformanceTuningResultWriter.beginObject();

                            FileInputStream fis = openFileInput(PARAMETERS_FILE + ".txt");
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader br = new BufferedReader(isr);

                            String line = br.readLine();
                            while (line != null) {
                                String[] words = line.split("=");
                                if (!nonOverrideableParams.containsKey(words[0])) {
                                    conformanceTuningResultWriter.name(words[0]).value(Integer.parseInt(words[1]));
                                }
                                line = br.readLine();
                            }
                            fis.close();
                            isr.close();
                            br.close();

                            conformanceTuningResultWriter.endObject();

                            conformanceTuningResultWriter.endObject();

                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Reset currTuningResults for next test config
                        conformanceTestResults = new ArrayList<ConformanceResultCase>();

                        // Reset tuning config
                        curTestIndex = 0;
                        curConfigIndex++;

                        if(curConfigIndex == numConfigs) { // All tuning tests completed

                            Toast.makeText(MainActivity.this, "All tests have been completed!", Toast.LENGTH_LONG).show();

                            // Enable start button
                            conformanceTestViewObject.startButton.setEnabled(true);
                            conformanceTestViewObject.startButton.setBackgroundColor(getResources().getColor(R.color.lightblue));

                            // Set progress layout invisible
                            conformanceTestViewObject.progressLayout.setVisibility(View.GONE);

                            // Set result layout visible
                            conformanceTestViewObject.resultLayout.setVisibility(View.VISIBLE);

                            // Close result writer
                            try {
                                conformanceTuningResultWriter.name("gpu").value(GPUName);
                                conformanceTuningResultWriter.name("configurations").value(numConfigs);
                                conformanceTuningResultWriter.name("randomSeed").value(tuningRandomSeed);

                                conformanceTuningResultWriter.endObject();
                                conformanceTuningResultWriter.endArray();
                                conformanceTuningResultWriter.close();
                                conformanceTuningFOS.close();
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }

                            writeToExternalStorage(RESULT_FILE);

                            // Get string array of test names
                            String[] testNumbers = new String[numConfigs];
                            for(int i = 0; i < numConfigs; i++) {
                                testNumbers[i] = Integer.toString(i);
                            }

                            // Update result
                            ConformanceTestResultAdapter conformanceTestResultAdapter = new ConformanceTestResultAdapter(MainActivity.this, testNumbers);
                            conformanceTestRV.setAdapter(conformanceTestResultAdapter);
                            conformanceTestRV.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            conformanceTestRV.addItemDecoration(new DividerItemDecoration(MainActivity.this, LinearLayoutManager.VERTICAL));
                        }
                        else {
                            conformanceTuningTestLoop();
                        }
                    }
                    else {
                        curTestIndex++;
                        conformanceTuningTestLoop();
                    }
                }
                else { // Lock Test
                    // Enable buttons and change their color
                    handleButtons(false, lockTestViewObject.buttons, lockTestViewObject.resultButtons);

                    lockTestViewObject.progressLayout.setVisibility(View.GONE);
                }
            }
        });
    }
}
