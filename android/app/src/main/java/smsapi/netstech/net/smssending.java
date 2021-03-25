package smsapi.netstech.net;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.gsm.SmsManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class smssending extends Service {
    JSONArray jsonArray ;
    List<SMS> sms = new ArrayList<SMS>();
    String bbc;
    SQLite db;
    Thread smsing;
    String urli;
    public smssending() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences mystate = PreferenceManager.getDefaultSharedPreferences(this);

        urli=mystate.getString("uli","");
        Log.d(">>>>>>>>",urli);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        smsing = new Thread(){
            @Override
            public void run() {

        while(true){
            if(isNetworkAvailable()){
        ConnectionHandler sh = new ConnectionHandler();

        // Making a request to url and getting response
        String jsonStr = sh.makeServiceCall(urli, ConnectionHandler.GET);
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

                    SmsManager sm = SmsManager.getDefault();
                    ArrayList<String> parts =sm.divideMessage(a.getMessage());
                    int numParts = parts.size();

                    ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
                    ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
                    sm.sendMultipartTextMessage(a.getTo(),null, parts, sentIntents, deliveryIntents);
                    bbc=a.getTo();
                    Thread.sleep(1000);
                }
                sms.clear();

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e("Connection Handler", "Couldn't get any data from the url");
        }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

            }
        };
        smsing.start();
    }

    @Override
    public boolean stopService(Intent name) {
        smsing.stop();
        return super.stopService(name);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
