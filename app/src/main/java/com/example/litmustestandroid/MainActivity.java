package com.example.litmustestandroid;
import static com.example.litmustestandroid.HelperClass.FileConstants.*;
import static com.example.litmustestandroid.HelperClass.ParameterConstants.*;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;

    private DrawerLayout drawer;

    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> adapterItems;

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
    private int tuningCurrConfig, tuningEndConfig, tuningTestWorkgroups, tuningMaxWorkgroups, tuningWorkgroupSize;
    private ArrayList<TuningResultCase> currTuningResults = new ArrayList<TuningResultCase>();
    private HashMap<String, ArrayList<TuningResultCase>> tuningResultCases = new HashMap<>();
    public HashMap<String, ArrayList<ConformanceResultCase>> conformanceTuningResultCases = new HashMap<>();

    private EditText[] conformanceParameters = new EditText[19];
    private ArrayList<String> conformanceSelectedShaders = new ArrayList<String>();
    private ArrayList<TestCase> conformanceSelectedTestCases = new ArrayList<TestCase>();
    private int conformanceCurrIteration;
    private int conformanceCurrConfig;
    private int conformanceEndConfig;
    private RecyclerView conformanceTestRV;
    private ArrayList<ConformanceResultCase> conformanceTestResults = new ArrayList<ConformanceResultCase>();

    private FileOutputStream conformanceTuningFOS;
    private JsonWriter conformanceTuningResultWriter;

    private static final String TAG = "MainActivity";

    private String currTestType = "";
    private TestCase currTestCase;
    private String GPUName = "";
    private TestThread testThread;
    private LockTestThread lockTestThread;

    private Handler handler = new Handler();

    private String shaderType = "";

    private ArrayList<TestCase> testCases = new ArrayList<>();
    public LinkedHashMap<String, Boolean> conformanceShaders = new LinkedHashMap<String, Boolean>();

    /*** New stuff for test lists ***/
    private static final String CONFORMANCE_TEST_LIST = "conformance_tests.json";
    private static final String TUNING_TEST_LIST = "tuning_tests.json";
    private static final String MISC_TEST_LIST = "misc_tests.json";

    private HashMap<String, NewTestCase> tuningTests = new HashMap<>();
    private HashMap<String, NewTestCase> conformanceTests = new HashMap<>();
    private HashMap<String, NewTestCase> allTests = new HashMap<>();

    private ArrayList<String> selectedTests = new ArrayList<>();

    /*** End new stuff ***/

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

    public TestCase findTestCase(String testName) {
        for (int i = 0; i < testCases.size(); i++) {
            if(testCases.get(i).testName.equals(testName)) {
                return testCases.get(i);
            }
        }
        return null;
    }

    public TestCase findTestCaseWithConformanceShader(String shaderName) {
        for (int i = 0; i < testCases.size(); i++) {
            TestCase currentTestCase = testCases.get(i);
            for (int j = 0; j < currentTestCase.conformanceShaderNames.length; j++) {
                if(currentTestCase.conformanceShaderNames[j].equals(shaderName)) {
                    return currentTestCase;
                }
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
        // some shaders use the same result file, so keep track of which ones we've already copied
        Set<String> seenResultFiles = new HashSet<>();
        for (NewTestCase testCase : allTests.values()) {
            // copy shader file
            int shaderId = this.getResources().getIdentifier(testCase.getShaderFile(), "raw", this.getPackageName());
            copyFile(shaderId, testCase.getShaderFile() + ".spv");
            Log.d(TAG, "File: " + testCase.getShaderFile() + ".spv copied to " + getFilesDir().toString());
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
        Set<String> keys = parameters.keySet();

        try {
            String line = bufferedReader.readLine();
            while (line != null) {
                String[] words = line.split("=");
                if((!words[0].equals(NUM_MEM_LOCATIONS) && !words[0].equals(NUM_OUTPUTS)) && keys.contains(words[0])) {
                    parameters.get(words[0]).setText(words[1]);
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
    public void writeParameters(String testName, EditText[] parameters, int paramValue) {
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
                || words[0].equals("permuteFirst") || words[0].equals("permuteSecond")
                || words[0].equals("aliasedMemory")) {
                    outputNumber = words[1];
                }
                else {
                    if(words[0].equals("iterations")) {
                        currTestIterations = parameters[index].getText().toString();
                    }
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

    public void writeTuningParameters(TestCase testCase, boolean reset) {
        if (reset) {
            boolean smoothedParameters = false;
            int workgroupLimiter = tuningMaxWorkgroups;

            int testingWorkgroups = randomGenerator(tuningTestWorkgroups, workgroupLimiter);
            int workgroupSize = randomGenerator(1, tuningWorkgroupSize);
            int maxWorkgroups = randomGenerator(testingWorkgroups, workgroupLimiter);
            int stressLineSize = (int) Math.pow(2, randomGenerator(2, 10));
            int stressTargetLines = randomGenerator(1, 16);
            int memStride = randomGenerator(1, 7);
            tuningParameter = new TreeMap<String, String>();

            int paramPresetValue = this.getResources().getIdentifier(testCase.paramPresetNames[0], "raw", this.getPackageName());
            InputStream inputStream = getResources().openRawResource(paramPresetValue);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            // Get parameter format by reading preset file
            try {
                String line = bufferedReader.readLine();
                while (line != null) {
                    String[] words = line.split("=");
                    tuningParameter.put(words[0], words[1]);
                    line = bufferedReader.readLine();
                }
                inputStream.close();
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Now randomize certain parameter values
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
            String fileName = "litmustest_" + testCase.testName + "_parameters.txt";
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);

            for (String key : tuningParameter.keySet()) {
                if (key.equals("permuteSecond") || key.equals("aliasedMemory")) {
                    if (testCase.testType.equals("coherence") || testCase.testType.equals("weakMemory_coherence")) {
                        fos.write((key + "=1\n").getBytes());
                    } else {
                        if (key.equals("permuteSecond")) {
                            fos.write((key + "=1031\n").getBytes());
                        } else {
                            fos.write((key + "=0\n").getBytes());
                        }
                    }
                } else {
                    fos.write((key + "=" + tuningParameter.get(key) + "\n").getBytes());
                }
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
        try {
            String fileName = "litmustest_" + testCase.testName + "_parameters.txt";
            FileInputStream fis = openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

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

    public void openExploreMenu(String testName, TestViewObject testViewObject) {
        Log.i("TEST", testName + " PRESSED, OPENING Explore MENU");

        currTestViewObject = testViewObject;
        currTestType = "Explorer";

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

        EditText[] exploreParameters = new EditText[19];
        exploreParameters[0] = (EditText) exploreMenuView.findViewById(R.id.testExploreTestIteration); // testIteration
        exploreParameters[1] = (EditText) exploreMenuView.findViewById(R.id.testExploreTestingWorkgroups); // testingWorkgroups
        exploreParameters[2] = (EditText) exploreMenuView.findViewById(R.id.testExploreMaxWorkgroups); // maxWorkgroups
        exploreParameters[3] = (EditText) exploreMenuView.findViewById(R.id.testExploreWorkgroupSize); // workgroupSize
        exploreParameters[4] = (EditText) exploreMenuView.findViewById(R.id.testExploreShufflePct); // shufflePct
        exploreParameters[5] = (EditText) exploreMenuView.findViewById(R.id.testExploreBarrierPct); // barrierPct
        exploreParameters[6] = (EditText) exploreMenuView.findViewById(R.id.testExploreScratchMemorySize); // scratchMemorySize
        exploreParameters[7] = (EditText) exploreMenuView.findViewById(R.id.testExploreMemoryStride); // memStride
        exploreParameters[8] = (EditText) exploreMenuView.findViewById(R.id.testExploreMemoryStressPct); // memStressPct
        exploreParameters[9] = (EditText) exploreMenuView.findViewById(R.id.testExploreMemoryStressIterations); // memStressIterations
        exploreParameters[10] = (EditText) exploreMenuView.findViewById(R.id.testExploreMemoryStressStoreFirstPct); // memStressPattern
        exploreParameters[11] = (EditText) exploreMenuView.findViewById(R.id.testExploreMemoryStressStoreSecondPct); // memStressPattern
        exploreParameters[12] = (EditText) exploreMenuView.findViewById(R.id.testExplorePreStressPct); // preStressPct
        exploreParameters[13] = (EditText) exploreMenuView.findViewById(R.id.testExplorePreStressIterations); // preStressIterations
        exploreParameters[14] = (EditText) exploreMenuView.findViewById(R.id.testExplorePreStressStoreFirstPct); // preStressPattern
        exploreParameters[15] = (EditText) exploreMenuView.findViewById(R.id.testExplorePreStressStoreSecondPct); // preStressPattern
        exploreParameters[16] = (EditText) exploreMenuView.findViewById(R.id.testExploreStressLineSize); // stressLineSize
        exploreParameters[17] = (EditText) exploreMenuView.findViewById(R.id.testExploreStressTargetLines); // stressTargetLines
        exploreParameters[18] = (EditText) exploreMenuView.findViewById(R.id.testExploreStressStrategyBalancePct); // stressAssignmentStrategy

        currTestCase = findTestCase(testName);
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

        // Reset shader type
        shaderType = currTestCase.shaderNames[0];

        // Initialize shader drop down explore menu
        initializeShaderMenu(testName, exploreMenuView);

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

                writeParameters(testName, exploreParameters, basic_parameters);
                exploreDialog.dismiss();

                // Disable buttons and change their color
                handleButtons(true, testViewObject.buttons, testViewObject.resultButtons);

                // Make progress layout visible
                testViewObject.explorerProgressLayout.setVisibility(View.VISIBLE);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String[] testArgument = new String[4];
                        testArgument[0] = "litmustest_" + testName; // Test Name

                        // Shader Name
                        testArgument[1] = shaderType; // Current selected shader
                        testArgument[2] = currTestCase.resultNames[0]; // Result Shader
                        testArgument[3] = currTestCase.testParamName; // Txt file that stores parameter

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
            FileInputStream fis = openFileInput("litmustest_" + testName + "_output_explorer.txt");
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
        currTestType = "Tuning";

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

        currTestCase = findTestCase(testName);
        shaderType = currTestCase.shaderNames[0];

        // Start tuning test
        tuningStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TUNING TEST", testName + " STARTING");

                int tuningConfigNum = Integer.parseInt(tuningParameters[0].getText().toString());
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

                tuningTestArgument[0] = "litmustest_" + testName; // Test Name

                // Shader Name
                tuningTestArgument[1] = shaderType; // Current selected shader
                tuningTestArgument[2] = currTestCase.resultNames[0]; // Result Shader
                tuningTestArgument[3] = currTestCase.testParamName; // Txt file that stores parameter

                tuningCurrConfig = 0;
                tuningEndConfig = tuningConfigNum;

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

        currTestViewObject.tuningCurrentConfigNumber.setText(tuningCurrConfig+1 + "/" + tuningEndConfig);

        writeTuningParameters(currTestCase, true);

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

    public void iterationProgress(String iterationNum) { ;
        //Log.i(TAG, "IterationProgress: " + iterationNum + "/" + currTestIterations);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(currTestType.equals("Explorer")) {
                    currTestViewObject.explorerCurrentIterationNumber.setText(iterationNum + "/" + currTestIterations);
                }
                else if (currTestType.equals("Tuning")) {
                    currTestViewObject.tuningCurrentIterationNumber.setText(iterationNum + "/" + currTestIterations);
                }
                else if (currTestType.equals("ConformanceExplorer")){ // Conformance Explorer
                    conformanceTestViewObject.currentIterationNumber.setText(iterationNum + "/" + currTestIterations);
                }
                else if (currTestType.equals("ConformanceTuning")){ // Conformance Explorer
                    conformanceTestViewObject.currentIterationNumber.setText(iterationNum + "/" + currTestIterations);
                }
                else { // Lock Test
                    lockTestViewObject.currentIterationNumber.setText(iterationNum + "/" + currTestIterations);
                }
            }
        });
    }

    public void setGPUName(String gpuName) {
        GPUName = gpuName;
        Log.i(TAG, gpuName);
    }

    public void sendResultEmail(String testMode) {
        Log.i(TAG, "Sending result via email");

        String subject = "Android Vulkan Memory Model Testing";
        String message = "GPU: " + GPUName;
        String fileName = "";

        if(testMode.equals("Explorer")) {
            subject += " MultiTest Explorer Result";
            fileName = "litmustest_multitest_explorer_result.txt";
        }
        else if (testMode.equals("Tuning")) {
            subject += " MultiTest Tuning Result";
            fileName = "litmustest_multitest_tuning_result.json";
        }
        else if (testMode.equals("ConformanceExplorer")) {
            subject += " Conformance Test Explorer Result";
            fileName = "litmustest_conformance_explorer_result.json";
        }
        else if (testMode.equals("ConformanceTuning")) {
            subject += " Conformance Test Tuning Result";
            fileName = "litmustest_conformance_tuning_result.json";
        }
        else { // Shouldn't be here
            Log.e(TAG, "multiTestSendResult invalid currTestType!: " + currTestType);
        }

        File fileLocation = new File(getFileDir(), fileName);
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
            conformanceShaders.put(shaderName, true);
        }
        else { // Un-clicked
            conformanceShaders.put(shaderName, false);
        }
        //Log.i(TAG, "conformance: " + shaderName + " " + currCheckBox.isChecked());
    }

    public void conformanceExplorerTestBegin(EditText[] parameters, ConformanceTestViewObject viewObject, RecyclerView resultRV) {
        currTestType = "ConformanceExplorer";
        conformanceParameters = parameters;
        conformanceTestViewObject = viewObject;
        conformanceTestRV = resultRV;
        conformanceSelectedShaders = new ArrayList<String>();

        // Check if at least one test selected
        for (LinkedHashMap.Entry<String, Boolean> entry :  conformanceShaders.entrySet()) {
            if(entry.getValue() == true) {
                conformanceSelectedShaders.add(entry.getKey());
            }
        }
        if(conformanceSelectedShaders.size() == 0) { // No test selected
            Toast.makeText(MainActivity.this, "No test selected!", Toast.LENGTH_LONG).show();
            return;
        }

        // Disable start button
        conformanceTestViewObject.startButton.setEnabled(false);
        conformanceTestViewObject.startButton.setBackgroundColor(getResources().getColor(R.color.cyan));

        // Set progress layout visible
        conformanceTestViewObject.progressLayout.setVisibility(View.VISIBLE);

        // Set config layout remain invisible
        conformanceTestViewObject.configLayout.setVisibility(View.GONE);

        // Set result layout invisible
        conformanceTestViewObject.explorerResultLayout.setVisibility(View.GONE);

        conformanceTestResults = new ArrayList<ConformanceResultCase>();
        conformanceCurrConfig = 0;
        conformanceEndConfig = conformanceSelectedShaders.size();

        // Start multi tuning test loop
        conformanceExplorerTestLoop();
    }

    public void conformanceExplorerTestLoop()  {
        String shaderName = conformanceSelectedShaders.get(conformanceCurrConfig);
        currTestCase = findTestCaseWithConformanceShader(shaderName);
        String[] testArgument = new String[4];

        testArgument[0] = "litmustest_" + currTestCase.testName; // Test Name

        // Shader Name
        testArgument[1] = shaderName; // Current selected shader

        // Choosing result shader
        String coherencyCheck = "coherency";
        String barrierCheck = "barrier";
        String rmwCheck = "rmw";
        if(shaderName.contains(coherencyCheck)) {
            testArgument[2] = currTestCase.resultNames[1]; // Coherency result shader
        }
        else {
            testArgument[2] = currTestCase.resultNames[0]; // Default result Shader
        }
        testArgument[3] = currTestCase.testParamName; // Txt file that stores parameter

        // Update test name
        if(shaderName.contains(coherencyCheck)) { // Weak memory Tests (single memory)
            conformanceTestViewObject.currentTestName.setText(currTestCase.testName + " (single)");
        }
        else if(shaderName.contains(barrierCheck)) { // Weak memory Tests (barrier)
            conformanceTestViewObject.currentTestName.setText(currTestCase.testName + " (barrier)");
        }
        else if(shaderName.contains(rmwCheck)) { // Coherency Tests (RMW)
            conformanceTestViewObject.currentTestName.setText(currTestCase.testName + " (RMW)");
        }
        else { // Coherency Tests (default)
            conformanceTestViewObject.currentTestName.setText(currTestCase.testName);
        }

        // Write parameter
        int paramPresetValue;
        if(shaderName.contains(coherencyCheck)) {
            paramPresetValue = this.getResources().getIdentifier(currTestCase.paramPresetNames[2], "raw", this.getPackageName());
        }
        else {
            paramPresetValue = this.getResources().getIdentifier(currTestCase.paramPresetNames[0], "raw", this.getPackageName());
        }
        writeParameters(currTestCase.testName, conformanceParameters, paramPresetValue);

        // Run test in different thread
        testThread = new TestThread(MainActivity.this, testArgument, false, true);
        testThread.start();
    }

    public void conformanceTuningTestBegin(EditText[] parameters, ConformanceTestViewObject viewObject, RecyclerView resultRV) {
        currTestType = "ConformanceTuning";
        conformanceTestViewObject = viewObject;
        conformanceTestRV = resultRV;
        conformanceSelectedShaders = new ArrayList<String>();
        conformanceSelectedTestCases = new ArrayList<TestCase>();

        // Check if at least one test selected
        for (LinkedHashMap.Entry<String, Boolean> entry :  conformanceShaders.entrySet()) {
            if(entry.getValue() == true) {
                conformanceSelectedShaders.add(entry.getKey());
                conformanceSelectedTestCases.add(findTestCaseWithConformanceShader(entry.getKey()));
            }
        }
        if(conformanceSelectedTestCases.size() == 0) { // No test selected
            Toast.makeText(MainActivity.this, "No test selected!", Toast.LENGTH_LONG).show();
            return;
        }

        // Go through selected test case and change test type
        for(int i = 0; i < conformanceSelectedTestCases.size(); i++) {
            if (conformanceSelectedShaders.get(i).contains("coherency")) {
                conformanceSelectedTestCases.get(i).testType = "coherency";
            }
        }

        // Disable start button
        conformanceTestViewObject.startButton.setEnabled(false);
        conformanceTestViewObject.startButton.setBackgroundColor(getResources().getColor(R.color.cyan));

        // Set progress layout visible
        conformanceTestViewObject.progressLayout.setVisibility(View.VISIBLE);

        // Set config layout visible
        conformanceTestViewObject.configLayout.setVisibility(View.VISIBLE);

        // Set result layout invisible
        conformanceTestViewObject.tuningResultLayout.setVisibility(View.GONE);

        int tuningConfigNum = Integer.parseInt(parameters[0].getText().toString());
        conformanceTestResults = new ArrayList<ConformanceResultCase>();
        currTestIterations = parameters[1].getText().toString();
        tuningRandomSeed = parameters[2].getText().toString();
        tuningTestWorkgroups = Integer.parseInt(parameters[3].getText().toString());
        tuningMaxWorkgroups = Integer.parseInt(parameters[4].getText().toString());
        tuningWorkgroupSize = Integer.parseInt(parameters[5].getText().toString());

        tuningCurrConfig = 0;
        tuningEndConfig = tuningConfigNum;

        conformanceCurrIteration = 0;

        if(tuningRandomSeed.length() == 0) {
            tuningRandom = new PRNG(new Random().nextInt());
        }
        else {
            tuningRandom = new PRNG(tuningRandomSeed);
        }

        // Initialize result writer
        String outputFileName = "litmustest_conformance_tuning_result.json";
        try {
            conformanceTuningFOS = openFileOutput(outputFileName, Context.MODE_PRIVATE);
            conformanceTuningResultWriter = new JsonWriter(new OutputStreamWriter(conformanceTuningFOS, "UTF-8"));
            conformanceTuningResultWriter.setIndent("  ");
            conformanceTuningResultWriter.beginArray();
            conformanceTuningResultWriter.beginObject();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // Start multi tuning test loop
        conformanceTuningTestLoop();
    }

    public void conformanceTuningTestLoop()  {
        currTestCase = conformanceSelectedTestCases.get(conformanceCurrIteration);
        String currShader = conformanceSelectedShaders.get(conformanceCurrIteration);
        String[] testArgument = new String[4];

        testArgument[0] = "litmustest_" + currTestCase.testName; // Test Name

        // Shader Name
        testArgument[1] = currShader; // Current selected shader

        // Choosing result shader
        String coherencyCheck = "coherency";
        String barrierCheck = "barrier";
        String rmwCheck = "rmw";
        if(currShader.contains(coherencyCheck)) {
            currTestCase.testType = "weakMemory_coherence";
            testArgument[2] = currTestCase.resultNames[1]; // Coherency result shader
        }
        else {
            if(currTestCase.testType.equals("weakMemory_coherence")) {
                currTestCase.testType = "weakMemory";
            }
            testArgument[2] = currTestCase.resultNames[0]; // Default result Shader
        }
        testArgument[3] = currTestCase.testParamName; // Txt file that stores parameter

        // Update test name
        if(currShader.contains(coherencyCheck)) { // Weak memory Tests (single memory)
            conformanceTestViewObject.currentTestName.setText(currTestCase.testName + " (single)");
        }
        else if(currShader.contains(barrierCheck)) { // Weak memory Tests (barrier)
            conformanceTestViewObject.currentTestName.setText(currTestCase.testName + " (barrier)");
        }
        else if(currShader.contains(rmwCheck)) { // Coherency Tests (RMW)
            conformanceTestViewObject.currentTestName.setText(currTestCase.testName + " (RMW)");
        }
        else { // Coherency Tests (default)
            conformanceTestViewObject.currentTestName.setText(currTestCase.testName);
        }

        // Update current config number
        conformanceTestViewObject.currentConfigNumber.setText(tuningCurrConfig+1 + "/" + tuningEndConfig);

        Log.i(TAG, "TestName: " + currTestCase.testName + " Shader: " + currShader);

        if(conformanceCurrIteration == 0) {
            // Write tuning parameter to current test case
            writeTuningParameters(currTestCase, true);
        }
        else {
            writeTuningParameters(currTestCase, false);
        }

        // Run test in different thread
        testThread = new TestThread(MainActivity.this, testArgument, false, true);
        testThread.start();
    }

    public void lockTestBegin(LockTestViewObject viewObject, String shaderName, boolean checkCorrect) {
        currTestType = "LockTest";
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
                if(currTestType.equals("Explorer")) {
                    // Enable buttons and change their color
                    handleButtons(false, currTestViewObject.buttons, currTestViewObject.resultButtons);

                    currTestViewObject.explorerProgressLayout.setVisibility(View.GONE);

                    // Write to external storage
                    String fileName = "litmustest_" + currTestViewObject.testName + "_output_explorer.txt";
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

                    Toast.makeText(MainActivity.this, "Test " + currTestViewObject.testName + " finished!", Toast.LENGTH_LONG).show();
                }
                else if (currTestType.equals("Tuning")) {
                    // Save param value
                    String currParamValue = convertFileToString(currTestCase.testParamName + ".txt");

                    // Save result value
                    String currResultValue = convertFileToString(currTestCase.outputNames[1] + ".txt");

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
                    TuningResultCase currTuningResult = new TuningResultCase(currTestCase.testName, currParamValue, currResultValue,
                            Integer.parseInt(numSeqBehaviors), Integer.parseInt(numInterleavedBehaviors), Integer.parseInt(numWeakBehaviors));

                    currTuningResults.add(currTuningResult);

                    if(tuningCurrConfig == tuningEndConfig - 1) {
                        tuningResultCases.put(currTestViewObject.testName, currTuningResults);

                        // Enable buttons and change their color
                        handleButtons(false, currTestViewObject.buttons, currTestViewObject.resultButtons);

                        currTestViewObject.tuningProgressLayout.setVisibility(View.GONE);

                        Toast.makeText(MainActivity.this, "Tuning Test " + currTestViewObject.testName + " finished!", Toast.LENGTH_LONG).show();
                    }
                    else {
                        tuningCurrConfig++;
                        tuningTestLoop();
                    }
                }
                else if (currTestType.equals("ConformanceExplorer")) { // Conformance Explorer
                    // Save param value
                    String currParamValue = convertFileToString(currTestCase.testParamName + ".txt");

                    // Save result value
                    String currResultValue = convertFileToString(currTestCase.outputNames[0] + ".txt");

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

                    if(conformanceCurrConfig == conformanceEndConfig-1) { // All test ended, update result

                        Toast.makeText(MainActivity.this, "All tests have been completed!", Toast.LENGTH_LONG).show();

                        // Write a json file
                        try {
                            String outputFileName = "litmustest_conformance_explorer_result.json";
                            FileOutputStream conformanceFOS = openFileOutput(outputFileName, Context.MODE_PRIVATE);
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

                            FileInputStream fis = openFileInput(currTestCase.testParamName + ".txt");
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader br = new BufferedReader(isr);

                            String line = br.readLine();
                            while (line != null) {
                                String[] words = line.split("=");
                                if(!words[0].equals("numMemLocations") && !words[0].equals("numOutputs")
                                        && !words[0].equals("permuteFirst") && !words[0].equals("permuteSecond")
                                        && !words[0].equals("aliasedMemory")) {
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
                            conformanceResultWriter.name("testCount").value(conformanceEndConfig);

                            conformanceResultWriter.endObject();
                            conformanceResultWriter.endArray();
                            conformanceResultWriter.close();
                            conformanceFOS.close();

                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Write to external storage
                        String fileName = "litmustest_conformance_explorer_result.json";
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

                        // Enable start button
                        conformanceTestViewObject.startButton.setEnabled(true);
                        conformanceTestViewObject.startButton.setBackgroundColor(getResources().getColor(R.color.lightblue));

                        // Set progress layout invisible
                        conformanceTestViewObject.progressLayout.setVisibility(View.GONE);

                        // Set result layout visible
                        conformanceTestViewObject.explorerResultLayout.setVisibility(View.VISIBLE);

                        // Indicate that there is result to be displayed
                        conformanceTestViewObject.newExplorer = false;

                        // Update result
                        ConformanceTestResultAdapter conformanceTestResultAdapter = new ConformanceTestResultAdapter(MainActivity.this, conformanceTestResults);
                        conformanceTestRV.setAdapter(conformanceTestResultAdapter);
                        conformanceTestRV.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        conformanceTestRV.addItemDecoration(new DividerItemDecoration(MainActivity.this, LinearLayoutManager.VERTICAL));
                    }
                    else {
                        conformanceCurrConfig++;
                        conformanceExplorerTestLoop();
                    }
                }
                else if (currTestType.equals("ConformanceTuning")) { // Conformance Tuning
                    // Save param value
                    String currParamValue = convertFileToString(currTestCase.testParamName + ".txt");

                    // Save result value
                    String currResultValue = convertFileToString(currTestCase.outputNames[0] + ".txt");

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

                    if(conformanceCurrIteration == conformanceSelectedTestCases.size() - 1) { // One tuning config test completed
                        conformanceTuningResultCases.put(Integer.toString(tuningCurrConfig), conformanceTestResults);

                        // Append to the result file
                        try {
                            conformanceTuningResultWriter.name(Integer.toString(tuningCurrConfig));
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

                            FileInputStream fis = openFileInput(currTestCase.testParamName + ".txt");
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader br = new BufferedReader(isr);

                            String line = br.readLine();
                            while (line != null) {
                                String[] words = line.split("=");
                                if(!words[0].equals("numMemLocations") && !words[0].equals("numOutputs")
                                        && !words[0].equals("permuteFirst") && !words[0].equals("permuteSecond")
                                        && !words[0].equals("aliasedMemory")) {
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
                        conformanceCurrIteration = 0;
                        tuningCurrConfig++;

                        if(tuningCurrConfig == tuningEndConfig) { // All tuning tests completed

                            Toast.makeText(MainActivity.this, "All tests have been completed!", Toast.LENGTH_LONG).show();

                            // Enable start button
                            conformanceTestViewObject.startButton.setEnabled(true);
                            conformanceTestViewObject.startButton.setBackgroundColor(getResources().getColor(R.color.lightblue));

                            // Set progress layout invisible
                            conformanceTestViewObject.progressLayout.setVisibility(View.GONE);

                            // Set result layout visible
                            conformanceTestViewObject.tuningResultLayout.setVisibility(View.VISIBLE);

                            // Indicate that there is result to be displayed
                            conformanceTestViewObject.newTuning = false;

                            // Close result writer
                            try {
                                conformanceTuningResultWriter.name("gpu").value(GPUName);
                                conformanceTuningResultWriter.name("configurations").value(tuningEndConfig);
                                conformanceTuningResultWriter.name("randomSeed").value(tuningRandomSeed);

                                conformanceTuningResultWriter.endObject();
                                conformanceTuningResultWriter.endArray();
                                conformanceTuningResultWriter.close();
                                conformanceTuningFOS.close();
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }

                            // Write to external storage
                            String fileName = "litmustest_conformance_tuning_result.json";
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

                            // Get string array of test names
                            String[] testNumbers = new String[tuningEndConfig];
                            for(int i = 0; i < tuningEndConfig; i++) {
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
                        conformanceCurrIteration++;
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
