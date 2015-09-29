package aso.unisinos.br.aso_mobile;

import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CallAPI extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... params) {
        String urlString=params[0]; // URL to call
        String resultToDisplay = "";
        InputStream in = null;

        // HTTP Get
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());
        } catch (Exception e ) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
        try {
            resultToDisplay = convertInputStreamToString(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultToDisplay;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    protected void onPostExecute(String result) {
        System.out.println(result);
    }

} // end CallAPI
