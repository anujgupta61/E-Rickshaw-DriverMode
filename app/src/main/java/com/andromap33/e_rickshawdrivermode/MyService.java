package com.andromap33.e_rickshawdrivermode;

import android.provider.Settings;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class MyService extends Service implements GoogleApiClient.ConnectionCallbacks , GoogleApiClient.OnConnectionFailedListener , LocationListener {


    private  GoogleApiClient mGoogleApiClient; // creating google API client

    private LocationRequest mLocationRequest; // location object that holds location values fetched
    private String  driver_id ;
    private  long sTime =0 ;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO for communication return IBinder implementation

        // Service is not binded to any activity , hence no binding required

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // creating Google API Client
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        // Getting Driver id from Shared Preferences
        driver_id = SaveSharedPreference.getDriverID(this);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Connecting API Client . This is done every time service restarts or OnstartComand is invoked
        mGoogleApiClient.connect();
        return Service.START_STICKY;
    }

    void SendData(final String driverID , final String lati , final String lngi) {

        /* this function sends the data to remote server . It Uses Asynctask for background work */
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

        String data = "";
        try {
            data = URLEncoder.encode("d_id", "UTF-8")
                    + "=" + URLEncoder.encode(driverID, "UTF-8");

            data += "&" + URLEncoder.encode("lat", "UTF-8") + "="
                    + URLEncoder.encode(lati, "UTF-8");

            data += "&" + URLEncoder.encode("lng", "UTF-8")
                    + "=" + URLEncoder.encode(lngi, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getApplicationContext(), "Location data not encoded ...", Toast.LENGTH_LONG).show();
        }
        String text = "";
        BufferedReader reader = null;
        // Send data
        try {
            // Defined URL  where to send data
            URL url = new URL("http://andromap33.orgfree.com/insert_location.php");

            // Send POST data request
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get the server response
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while ((line = reader.readLine()) != null) {
                // Append server response in string
                sb.append(line + "\n");
            }
            text = sb.toString();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Location data not sent ...", Toast.LENGTH_LONG).show();
        } finally {
            try {
                reader.close();
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "Buffer Reader not closed ...", Toast.LENGTH_LONG).show();
            }
        }
                return "Location sent ...." ;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute();
    }




    @Override
    public void onDestroy() {
        // Disconected Google Api Client
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000) ;
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest , this);
        }
        catch (SecurityException e) {
            Toast.makeText(getApplicationContext(), "Connection request not completed ...", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Toast.makeText(this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getApplicationContext(), "Connection is Slow", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        String lng= Double.toString(location.getLongitude());
        String lat = Double.toString(location.getLatitude());
        long finalTime= System.currentTimeMillis();
        if((finalTime - sTime)>=5000) {
            SendData(driver_id,lat,lng);
            sTime = finalTime;
        }
    }
}