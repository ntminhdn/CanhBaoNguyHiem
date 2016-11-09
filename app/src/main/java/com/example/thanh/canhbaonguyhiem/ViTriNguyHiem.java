package com.example.thanh.canhbaonguyhiem;

import com.google.android.gms.maps.model.LatLng;


/**
 * Created by Thanh on 10/10/2015.
 */
public class ViTriNguyHiem {
    public LatLng LatLng;
    public String Name;
    public int Radius;
    public int MucDoNguyHiem;
    public int ThongTin;




    public ViTriNguyHiem(LatLng latlng, String name, int radius, int mucdonguyhiem, int thongtin){
        LatLng = latlng;
        Name = name;
        Radius = radius;
        MucDoNguyHiem = mucdonguyhiem;
        ThongTin = thongtin;
    }
    public void setLatLng(com.google.android.gms.maps.model.LatLng latLng) {
        LatLng = latLng;
    }

    public void setMucDoNguyHiem(int mucDoNguyHiem) {
        MucDoNguyHiem = mucDoNguyHiem;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setRadius(int radius) {
        Radius = radius;
    }
}
