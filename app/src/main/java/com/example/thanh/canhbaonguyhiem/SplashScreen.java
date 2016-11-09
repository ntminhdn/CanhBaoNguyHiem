package com.example.thanh.canhbaonguyhiem;

/**
 * Created by Thanh on 11/6/2015.
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by vamsikrishna on 12-Feb-15.
 */
public class SplashScreen extends Activity {
//
//    GPSManager gpsManager = null;
//    LocationManager locationManager;
//    boolean isGPSEnabled = false;
//public static Location Location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(1500);
                    Log.i("", "Sleep");
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    Intent intent = new Intent(SplashScreen.this, MapsActivity.class);
                    startActivity(intent);
                }
            }
        };
        timerThread.start();
//        Log.i("", "Sleep started");
//        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
//        gpsManager = new GPSManager(SplashScreen.this);
//        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        if(isGPSEnabled)
//        {
//            gpsManager.startListening(getApplicationContext());
//            gpsManager.setGPSCallback(this);
//        }
//        else
//        {
//            gpsManager.showSettingsAlert();
//        }


    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SplashScreen.this, MapsActivity.class);
                    startActivity(intent);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }
//
//    @Override
//    public void onGPSUpdate(Location location) {
//        if (isNetworkAvailable()){
//        //setContentView(R.layout.activity_maps);
//            //MapsActivity.locationManager = locationManager;
//            //MapsActivity.gpsManager = gpsManager;
//            //Location = location;
//
//            Intent intent = new Intent(SplashScreen.this, MapsActivity.class);
//            this.onPause();
//            startActivity(intent);
//        }
//        else{
//            Toast.makeText(getApplicationContext(), "Không thể sử dụng Internet, kiểm tra kết nối data",
//                    Toast.LENGTH_LONG).show();
//        }
//    }
//    private boolean isNetworkAvailable() {
//        ConnectivityManager connectivityManager
//                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//    }
}