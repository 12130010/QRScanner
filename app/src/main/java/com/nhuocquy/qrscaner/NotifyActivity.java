package com.nhuocquy.qrscaner;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class NotifyActivity extends Activity {
    public static final String URL = "url";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        String myUrl = getIntent().getStringExtra(URL);

        final TextView tvNotify = (TextView) findViewById( R.id.tvNotify);

        new AsyncTask<String, Void, String>(){
            RestTemplate restTemplate = new RestTemplate();
            @Override
            protected void onPreExecute() {
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {
                String s = null;
                try {
                    Location location = (Location) MyVar.get(MyVar.CURRENT_LOCATION);
                    SharedPreferences ref = getSharedPreferences(ScannerActivity.MY_DATA,MODE_PRIVATE);
                    s = restTemplate.getForObject(String.format("%s&lat=%s&longi=%s&acc=%s",params[0],location.getLatitude(), location.getLongitude(), ref.getString(ScannerActivity.ACCOUNT_NAME,"")), String.class);
                }catch (RestClientException e){
                    s = "Can't connect to server!!";
                }
                return s;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                tvNotify.setText(s);
            }
        }.execute(myUrl);
    }

}
