package aso.unisinos.br.aso_mobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    public final static String urlString = "http://10.0.0.101:8080/aso/patient";
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private HashMap<String, List<JSONObject>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PhoneStorageHelper storageHelper = new PhoneStorageHelper();

        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.show();

        try {
            String jsonResult = "";
            if(isOnline()){
                AsyncTask<String, String, String> result = new CallAPI().execute(urlString);
                jsonResult = result.get();
                //storageHelper.storePatientList(jsonResult, getApplicationContext());
            }else{
                //jsonResult = storageHelper.getPatientList(getApplicationContext());
            }
            expListView = (ExpandableListView) findViewById(R.id.expandableListView);

            // preparing list data
            JSONObject jsonObject = new JSONObject(jsonResult);
            prepareListData(jsonObject);

            listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
            listAdapter.setInflater((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), this);
            listAdapter.setHeaderInBold(true);
            // setting list adapter
            expListView.setAdapter(listAdapter);

            LinearLayout.LayoutParams mParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, listAdapter.getGroupCount()*200);
            expListView.setLayoutParams(mParam);

            AsyncTask<String, String, String> result = new CallAPI().execute(urlString+"/countByDisease");
            String chartUrl = result.get();
            JSONObject jsonChartUrl = new JSONObject(chartUrl);

            WebView charts = (WebView) findViewById(R.id.patientByDisease);
            createWebView(charts);
            charts.loadUrl(jsonChartUrl.getString("patientByDiseaseChart"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        progress.dismiss();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
                //openSearch();
                return true;
            case R.id.action_settings:
                //openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void prepareListData(JSONObject jObj) throws JSONException {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<JSONObject>>();

        Iterator<String> keysItr = jObj.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            listDataHeader.add(key);

            JSONArray jArr = jObj.getJSONArray(key);
            List<JSONObject> patients = new ArrayList<JSONObject>();
            for (int i=0; i < jArr.length(); i++) {
                JSONObject obj = jArr.getJSONObject(i);
                String name = (String) obj.get("name");
                patients.add(obj);
            }
            listDataChild.put(listDataHeader.get(listDataHeader.size()-1), patients);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return true;
        //return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
