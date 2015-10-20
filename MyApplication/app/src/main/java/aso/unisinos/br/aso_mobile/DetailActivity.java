package aso.unisinos.br.aso_mobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DetailActivity extends AppCompatActivity {
    public final static String urlString = "http://10.0.0.101:8080/aso/aso/retrieve/";
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private HashMap<String, List<JSONObject>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        PhoneStorageHelper storageHelper = new PhoneStorageHelper();

        Intent intent = getIntent();
        Integer message = intent.getIntExtra(ExpandableListAdapter.EXTRA_MESSAGE, 0);

        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.show();

        String patientDetail = "Patient";
        try {
            String patientCallURL = urlString + message;
            if(isOnline()) {
                AsyncTask<String, String, String> result = new CallAPI().execute(patientCallURL);
                patientDetail = result.get();
                //storageHelper.savePatientInfo(patientCallURL, patientDetail, getApplicationContext());
            }else{
                //patientDetail = storageHelper.retrievePatientInfo(patientCallURL, getApplicationContext());
            }

            JSONObject jsonObject = new JSONObject(patientDetail);
            populatePatientInfo(jsonObject);

            expListView = (ExpandableListView) findViewById(R.id.relatedPatients);

            prepareListData(jsonObject);
            listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
            listAdapter.setInflater((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), this);
            listAdapter.setHeaderInBold(false);
            // setting list adapter
            expListView.setAdapter(listAdapter);

            LinearLayout.LayoutParams mParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, listAdapter.getGroupCount()*200);
            expListView.setLayoutParams(mParam);

            progress.dismiss();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void populatePatientInfo(JSONObject jsonObject) {
        try {
            TextView textView = (TextView) findViewById(R.id.name);
            createTextView(textView, jsonObject.getString("name"));

            textView = (TextView) findViewById(R.id.age);
            createTextView(textView, jsonObject.getString("age"));

            textView = (TextView) findViewById(R.id.sex);
            createTextView(textView, jsonObject.getString("gender"));

            textView = (TextView) findViewById(R.id.diagnosis);
            createTextView(textView, buildDiseaseListString(jsonObject.getJSONArray("diseases")));

            WebView charts = (WebView) findViewById(R.id.chartView);
            createWebView(charts);
            charts.loadUrl(jsonObject.getString("chartUrl"));
            charts.zoomOut();

            WebView comparisonCharts = (WebView) findViewById(R.id.comparisonChartView);
            createWebView(comparisonCharts);
            comparisonCharts.loadUrl(jsonObject.getString("bloodPressureComparisonChartUrl"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String buildDiseaseListString(JSONArray diseasesJsonArray) throws JSONException {
        StringBuilder diseaseList = new StringBuilder("");

        for(int i = 0; i<diseasesJsonArray.length(); i++){
            JSONObject jsonObject = diseasesJsonArray.getJSONObject(i);
            diseaseList.append(jsonObject.getString("name")).append(",");
        }

        return diseaseList.substring(0, diseaseList.length()-1).toString();
    }

    private void createWebView(WebView charts) {
        charts.getSettings().setBuiltInZoomControls(false);
        charts.getSettings().setSupportZoom(false);
        charts.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        charts.getSettings().setAllowFileAccess(true);
        charts.getSettings().setDomStorageEnabled(true);
        final ProgressDialog progDialog = new ProgressDialog(this);

        charts.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                progDialog.setTitle("Loading");
                progDialog.setMessage("Wait while loading...");
                progDialog.show();
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView view, final String url) {
                progDialog.dismiss();
            }
        });
    }

    private void createTextView(TextView textView, String message) {
        textView.setText(message);
        textView.setTextSize(20);
        textView.setText(message);
    }

    private void prepareListData(JSONObject jObj) throws JSONException {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<JSONObject>>();

        JSONArray patientsWithSameDiagnosisJSON = jObj.getJSONArray("patientsWithSameDiagnosis");
        JSONArray patientsTakingSameMedicationJSON = jObj.getJSONArray("patientsTakingSameMedication");

        populateListHeaderAndChild("Pacientes com mesmo diagnóstico", patientsWithSameDiagnosisJSON);
        populateListHeaderAndChild("Pacientes tomando mesma medicação", patientsTakingSameMedicationJSON);
    }

    private void populateListHeaderAndChild(String headerTitle, JSONArray patientsWithSameDiagnosisJSON) throws JSONException {
        listDataHeader.add(headerTitle);
        List<JSONObject> patients = new ArrayList<JSONObject>();
        for (int i = 0; i < patientsWithSameDiagnosisJSON.length(); i++) {
            JSONObject obj = patientsWithSameDiagnosisJSON.getJSONObject(i);
            String name = (String) obj.get("name");
            patients.add(obj);
        }
        listDataChild.put(listDataHeader.get(listDataHeader.size() - 1), patients);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return true;
        //return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
