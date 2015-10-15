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
import android.widget.EditText;
import android.widget.ExpandableListView;

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

        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.show();

        try {
            String jsonResult = "";
            if(isOnline()){
                AsyncTask<String, String, String> result = new CallAPI().execute(urlString);
                jsonResult = result.get();
            }else{
                jsonResult = new PhoneStorageHelper().getPatientList();
            }
            expListView = (ExpandableListView) findViewById(R.id.expandableListView);

            // preparing list data
            prepareListData(new JSONObject(jsonResult));

            listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
            listAdapter.setInflater((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), this);
            // setting list adapter
            expListView.setAdapter(listAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        progress.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
