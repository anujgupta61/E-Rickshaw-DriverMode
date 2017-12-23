package com.andromap33.e_rickshawdrivermode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         if (SaveSharedPreference.getDriverID(this).length() == 0) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
            startActivity(intent);
            finish(); 

        } else {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Check Permissions Now
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
            ShowGPSSettings(this) ;
            String driver_id = SaveSharedPreference.getDriverID(this);
            setContentView(R.layout.activity_main);
            TextView d_id = findViewById(R.id.d_id);
             try {
                 String demo =  "Driver ID - " + driver_id;
                 d_id.setText(demo);
             } catch( NullPointerException e ){
                 Log.i("paramMessage" , "nullpointerException");
             }

             if (!checkConnection(this)) {
                 showInternetNotAvailableAlert(this);
             } else if(isGooglePlayServicesAvailable(this)) {
                startService(new Intent(getBaseContext(), MyService.class));
            }
        }
    }

    public void showInternetNotAvailableAlert(Activity activity) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("NO INTERNET")
                    .setMessage("Please enable internet")
                    .setCancelable(true)
                    .setPositiveButton("Cancel",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog . cancel() ;
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            	ShowGPSSettings(this) ;
                this.recreate() ;
            }
        }
    }

    public void ShowGPSSettings(Activity activity) {
        String provider = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(!provider.contains("gps")) { //if gps is disabled
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Please enable GPS")
                        .setMessage("Please select High Accuracy Location Mode")
                        .setCancelable(true)
                        .setPositiveButton("Cancel",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog . cancel() ;
                            }
                        })
                        .setNegativeButton("GPS Settings",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) ;
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }        
    }

    boolean checkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return  activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
