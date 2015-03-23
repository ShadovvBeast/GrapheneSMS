package graphene.cf;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;


public class GrapheneSMS extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphene_sms);
        mContext = this;
        DownloadJsonTask djt = new DownloadJsonTask();
        try {
            URL url = new URL("http://graphene.cf/sms/queue.php");
            String strResult = "";
            djt.execute();
            strResult = djt.get();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graphene_sm, menu);
        return true;
    }

    private static String SENT = "SMS_SENT";
    private static String DELIVERED = "SMS_DELIVERED";
    private static int MAX_SMS_MESSAGE_LENGTH = 160;
    private static Context mContext;
    // ---sends an SMS message to another device---
    public static boolean sendSMS(String phoneNumber, String strTemplateName) {

        String message = getMessageFromTemplateName(strTemplateName);
        PendingIntent piSent = PendingIntent.getBroadcast(mContext, 0, new Intent(SENT), 0);
        PendingIntent piDelivered = PendingIntent.getBroadcast(mContext, 0,new Intent(DELIVERED), 0);
        SmsManager smsManager = SmsManager.getDefault();

        int length = message.length();
        try {
            if (length > MAX_SMS_MESSAGE_LENGTH) {
                ArrayList<String> messagelist = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(phoneNumber, null, messagelist, null, null);
            } else
                smsManager.sendTextMessage(phoneNumber, "Goji SMS", message, piSent, piDelivered);
            return true;
        }
        catch(Exception ex)
        {
            //AlertDialog ad = new AlertDialog();
            //ad.setTitle("Error: " + ex.getMessage());
            return false;
        }
    }

    public static String getMessageFromTemplateName(String strTemplateName)
    {
        return// "<template>\n" +
              //  "<name>default</name>\n" +
               // "<data>\n" +
                "hey <variable>name</variable> we just wanted to welcome you to the <variable>business</variable> family\n";
               // "</data>\n" +
               // "</Template>";
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DownloadJsonTask extends AsyncTask<URL, Integer, String> {
        protected String doInBackground(String url) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet("http://graphene.cf/sms/queue.php");
            HttpResponse httpResponse;
            try {
                httpResponse = httpClient.execute(httpGet);
                //HttpEntity httpEntity = httpResponse.getEntity();
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null;) {
                    builder.append(line).append("\n");
                }
                JSONTokener tokener = new JSONTokener(builder.toString());
                JSONArray finalResult = new JSONArray(tokener);
                for (int i=0; i<finalResult.length();i++)
                {
                    JSONObject jsonObject = (JSONObject)finalResult.get(0);
                    String sPhone = String.valueOf(jsonObject.get("phone"));
                    sendSMS(sPhone, "default");
                }
            } catch(Exception e)
            {
                e.printStackTrace();
            }
            return "Plaufer";
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected String doInBackground(URL... params) {
            return doInBackground(Arrays.toString(params));
        }

        protected void onPostExecute(Long result) {
        }
    }
}
