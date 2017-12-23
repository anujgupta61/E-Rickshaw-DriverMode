package com.andromap33.e_rickshawdrivermode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        if (!checkConnection(this)) {
            Toast.makeText(getApplicationContext(), "Please connect to the network ....", Toast.LENGTH_LONG).show();
            showInternetNotAvailableAlert(this);
        }
    }

    public void showInternetNotAvailableAlert(Activity activity)
    {
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
        catch(Exception e)
        {
            Log.d("Internet log ", "Show Dialog: "+e.getMessage());
        }
    }

    public void getValue(View view) {
        EditText txt = findViewById(R.id.driver_id);
        String d_id = txt.getText().toString();
        SendData(d_id);
    }

    @Override
    protected  void onStop() {
        super.onStop();
    }

    boolean checkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    void SendData(final String driverID) {

        class wrapper {
            String status;
        }
        
        class SendPostReqAsyncTask extends AsyncTask<String, Void, wrapper> {
            wrapper w = new wrapper();
            ProgressDialog dialog = ProgressDialog.show(LoginActivity.this, "",
                        "Loading. Please wait...", true);

             @Override
            protected void onPreExecute() {
                super.onPreExecute();
                 dialog.show();
            }

            @Override
            protected wrapper doInBackground(String... params) {
                
                String data = "";
                try {
                    data = URLEncoder.encode("d_id", "UTF-8")
                            + "=" + URLEncoder.encode(driverID, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                BufferedReader reader = null;

                // Send data
                try {

                    // Defined URL  where to send data
                    URL url = new URL("https://erickshaw.000webhostapp.com/verify.php");

                    // Send POST data request

                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(data);
                    wr.flush();

                    // Get the server response
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    //StringBuilder sb = new StringBuilder();
                    String line, str = "";

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        // Append server response in string
                        str = str + line ;
                    }
                    w.status = str;
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        reader.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return w;
            }

            @Override
            protected void onPostExecute(wrapper w) {
                super.onPostExecute(w);
                 dialog . dismiss() ;
                int data = Integer.parseInt(w.status) ;
                
                if (data == 1) {
                    String text = "Driver successfully logged in ..." ;
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                    SaveSharedPreference.setDriverID(getApplicationContext() , driverID) ;
                    Intent intent = new Intent(getApplicationContext() , MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
                    startActivity(intent);
                    finish(); 

                } else {
                    if(data == 2) {
                        String text = "Driver already exists ..." ;
                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                    }
                     else if(data == 3) {
                        String text = "Invalid Driver ID ..." ;
                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute();
    }
}
