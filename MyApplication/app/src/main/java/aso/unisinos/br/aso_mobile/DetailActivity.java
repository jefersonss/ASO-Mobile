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
                storageHelper.savePatientInfo(patientCallURL, patientDetail);
            }else{
                patientDetail = storageHelper.retrievePatientInfo(patientCallURL);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        populatePatientInfo(patientDetail);

        progress.dismiss();
    }

    private void populatePatientInfo(String patientJson) {
        try {
            JSONObject jsonObject = new JSONObject(patientJson);

            TextView textView = (TextView) findViewById(R.id.name);
            createTextView(textView, jsonObject.getString("name"));

            textView = (TextView) findViewById(R.id.age);
            createTextView(textView, jsonObject.getString("age"));

            textView = (TextView) findViewById(R.id.sex);
            createTextView(textView, jsonObject.getString("gender"));

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

        Iterator<String> keysItr = jObj.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            listDataHeader.add(key);

            JSONArray jArr = jObj.getJSONArray(key);
            List<JSONObject> patients = new ArrayList<JSONObject>();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject obj = jArr.getJSONObject(i);
                String name = (String) obj.get("name");
                patients.add(obj);
            }
            listDataChild.put(listDataHeader.get(listDataHeader.size() - 1), patients);
        }
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
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
