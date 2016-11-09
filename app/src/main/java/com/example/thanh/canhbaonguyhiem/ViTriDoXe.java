package com.example.thanh.canhbaonguyhiem;


import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by PhiLong on 05/11/2016.
 */
@IgnoreExtraProperties
public class ViTriDoXe {
    public String X;
    public String Y;
    public String status;

    public ViTriDoXe(String x,String y,String status){
        X = x;
        Y = y;
        this.status = status;
    }

    public ViTriDoXe(){

    }

    public LatLng getLocation(){
        return new LatLng(Double.parseDouble(X),Double.parseDouble(Y));
    }
}
