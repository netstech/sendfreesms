package smsapi.netstech.net;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    EditText url;
    EditText pass;
    Button start;
    Button stop;
    Button log;
    SQLite db;
    TextView tvstatus;
    private String urli;
    JSONArray jsonArray = null;
    private ProgressDialog pDialog;
    List<SMS> sms = new ArrayList<SMS>();
    private static final int PERMISSION_REQUEST_SMS = 100;
    String status;
    String bbc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        status="Service: Not Start";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askForSMSPermission();
        url=(EditText)findViewById(R.id.editText);
        pass=(EditText)findViewById(R.id.editText2);
        start=(Button)findViewById(R.id.button);
        log=(Button)findViewById(R.id.button2);
        stop=(Button)findViewById(R.id.stop);
        tvstatus=(TextView)findViewById(R.id.status);
        tvstatus.setText(status);
        if(isMyServiceRunning(smssending.class)){
            tvstatus.setText("Started");
        }
        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(MainActivity.this,log.class);
                startActivity(intent);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,smssending.class);
                stopService(intent);
                Toast.makeText(MainActivity.this,"Service Stoped",Toast.LENGTH_SHORT).show();
                tvstatus.setText("Stoped");
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefrenceSave("url",url.getText().toString());
                prefrenceSave("pass",pass.getText().toString());
                urli=url.getText().toString()+"?pass="+pass.getText().toString();
                prefrenceSave("uli",urli);
                if(isNetworkAvailable()){
                    Intent intent= new Intent(MainActivity.this,smssending.class);
                    startService(intent);
                    Toast.makeText(MainActivity.this,"Service Started",Toast.LENGTH_SHORT).show();
                    tvstatus.setText("Service Started");
                    }
                else{
                    Toast.makeText(MainActivity.this,"No Internet Available",Toast.LENGTH_SHORT).show();
                }

            }
        });
        //Shared Prefrence data In
        SharedPreferences mystate = PreferenceManager.getDefaultSharedPreferences(this);

        url.setText(mystate.getString("url",""));
        pass.setText(mystate.getString("pass",""));

    }
    private void prefrenceSave(String key, String value){
        SharedPreferences mystate = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editer = mystate.edit();
        editer.putString(key,value);
        editer.commit();
    }
    /**
     * Async task class to get json by making HTTP call
     * */
    private class GetData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            //pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ConnectionHandler sh = new ConnectionHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(urli, ConnectionHandler.GET);
            if(jsonStr.substring(0,1).equals("[")){
                status="Service: Started";
                    runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvstatus.setText(status);
                    }
                });
            }
            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONArray jsonObj = new JSONArray(jsonStr);

                    // Getting JSON Array node
                    jsonArray = jsonObj;

                    // looping through All Contacts
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);
                        sms.add(i,new SMS());
                        sms.get(i).setTo(c.getString("to"));
                        sms.get(i).setMessage(c.getString("message"));
                    }
                    // adding contact to contact list
                    db=new SQLite(getApplicationContext());
                    db.insert(sms);
                    List<SMS> list = sms;
                    for(SMS a: list){
                        SmsManager sms = SmsManager.getDefault();
                        sms.sendTextMessage(a.getTo(), null, a.getMessage(), null, null);
                        bbc=a.getTo();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),"Message send to "+bbc,Toast.LENGTH_SHORT).show();
                            }
                        });
                        Thread.sleep(1000);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                Log.e("Connection Handler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);


            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sms.clear();
            if(isNetworkAvailable()){
               new GetData().execute();
            }
            //Toast.makeText(MainActivity.this,sms.get(1).getMessage().toString(),Toast.LENGTH_SHORT).show();
            //listView.setAdapter(new CustomAdapter(MainActivity.this, data ,R.drawable.aro));

        }

    }

    /*
     *Ask Permison START
    */
    public void askForSMSPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // sms permistion
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.SEND_SMS)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("SMS access needed");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setMessage("please confirm SMS Sending access");//TODO put real question
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(
                                        new String[]
                                                {Manifest.permission.SEND_SMS}
                                        , PERMISSION_REQUEST_SMS);
                            }
                        });
                        builder.show();
                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.SEND_SMS},
                                PERMISSION_REQUEST_SMS);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
            }

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(this, "No permission for SMS", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }/*
     *Ask Permison End
    */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    }
