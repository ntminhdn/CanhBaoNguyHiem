package com.example.thanh.canhbaonguyhiem;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Thanh on 12/3/2015.
 */
public class GetRoadName{

    public String getRoadName(String lat, String lng) {
        String roadName = "";
        android.os.StrictMode.ThreadPolicy policy = new android.os.StrictMode.ThreadPolicy.Builder().permitAll().build();
        android.os.StrictMode.setThreadPolicy(policy);
        try {
            String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=__LAT__,__LNG__&sensor=false";

            url = url.replaceAll("__LAT__", lat);
            url = url.replaceAll("__LNG__", lng);

            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpGet httpget = new HttpGet(url);

            InputStream inputStream = null;
            String result = null;
            try {
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();
                // json is UTF-8 by default
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                result = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
            }

            JSONObject jObject = new JSONObject(result);
            JSONArray jArray = jObject.getJSONArray("results");
            if (jArray != null && jArray.length() > 0) {
                try {
                    JSONArray array = jArray.getJSONObject(0).getJSONArray("address_components");
                    for (int i = 0; i < array.length(); i++) {
                        if (array.getJSONObject(i).getString("types") == "route"){
                            roadName = array.getJSONObject(i).getString("long_name");
                            return roadName;
                        }
                    }
                    // Pulling items from the array
                    //roadName = oneObject.getString("formatted_address");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return roadName;
    }
}
