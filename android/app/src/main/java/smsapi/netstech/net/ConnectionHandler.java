package smsapi.netstech.net;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ConnectionHandler {

    static String response = null;
    public final static int GET = 1;
    public final static int POST = 2;

    public ConnectionHandler() {

    }

    /*
     * Making service call
     * @url - url to make request
     * @method - http request method
     * */
    public String makeServiceCall(String url, int method) {
        //return this.makeServiceCall(url, method, null);
        return this.getJSON(url,50000000);
    }

	/*
	 * Making service call
	 * @url - url to make request
	 * @method - http request method
	 * @params - http request params
	 * */



    public String getJSON(String url, int timeout) {
        HttpURLConnection c = null;
        try {
            //System.setProperty("http.keepAlive", "true");
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();

            c.setRequestMethod("GET");
           // c.setRequestProperty("Content-length", "0");
            //c.setRequestProperty("connection", "close");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            if(c != null)
            {
                int status = c.getResponseCode();
                Log.d("StatusCodeChecking: ", "> " + status);
            }
            else
            {
                Log.d("Status code not found","Status code not found");
            }

            int status = c.getResponseCode();
            Log.d("StatusCode: ", "> " + status);
            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    Log.d("Response: ", "> " + sb.toString());
                    return sb.toString();
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }
}
