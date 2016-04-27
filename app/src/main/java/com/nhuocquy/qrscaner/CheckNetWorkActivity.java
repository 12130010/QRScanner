package com.nhuocquy.qrscaner;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class CheckNetWorkActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private TextView tvNet;
    private TextView tvGPS;
    private BroadcastReceiver receiver;
    private IntentFilter filter;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private void findViews() {
        tvNet = (TextView) findViewById(R.id.tvNet);
        tvGPS = (TextView) findViewById(R.id.tvGPS);

        filter = new IntentFilter();
        filter.addAction("android.location.PROVIDERS_CHANGED");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                update();
            }
        };

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            Log.e("NhuocQuy", mGoogleApiClient.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_net_work);

        findViews();
        update();

    }

    private void update() {
        boolean networkState = Util.isNetworkAvailable(this);
        String networkStateString = "Network: " + (networkState ? "Connected" : "Disconneted");
        SpannableString networkStateSpan = new SpannableString(networkStateString);
        networkStateSpan.setSpan(new ForegroundColorSpan(networkState ? Color.GREEN : Color.RED), networkStateString.indexOf(' '), networkStateString.length(), 0);
        tvNet.setText(networkStateSpan);

        boolean gpsState = Util.isGPSAvailible(this);
        String gpsStateString = "GPS: " + (gpsState ? "Connected" : "Disconneted");

        boolean startNewActivity = false;

        if (gpsState) {
            Location location = (Location) MyVar.get(MyVar.CURRENT_LOCATION);
            if(location == null )
                gpsStateString += ", getting location...";
            else {
                gpsStateString += ", " + location.getLatitude() + ":" + location.getLongitude() + ":" + location.getAccuracy();
                startNewActivity = true;
            }
        }

        SpannableString gpsStateSpan = new SpannableString(gpsStateString);
        gpsStateSpan.setSpan(new ForegroundColorSpan(gpsState ? Color.GREEN : Color.RED), gpsStateString.indexOf(' '), gpsStateString.length(), 0);
        tvGPS.setText(gpsStateSpan);

        if(startNewActivity && networkState){
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startActivity(new Intent(CheckNetWorkActivity.this, ScannerActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(receiver, filter);
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
        Log.e("NhuocQuy", "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("NhuocQuy", "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("NhuocQuy", "onConnectionFailed" );
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.e("NhuocQuy", "startLocationUpdates");
    }

    @Override
    public void onLocationChanged(Location location) {
        MyVar.put(MyVar.CURRENT_LOCATION, location);
        Log.e("NhuocQuy", "onLocationChanged");
        update();
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        if(mGoogleApiClient != null  && mGoogleApiClient.isConnected())
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.e("NhuocQuy", "stopLocationUpdates");
    }
}
