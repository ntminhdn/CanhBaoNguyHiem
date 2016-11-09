package com.example.thanh.canhbaonguyhiem;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Thanh on 10/24/2015.
 */
public class DoanNguyHiem {
    public com.google.android.gms.maps.model.LatLng startLatLng;
    public com.google.android.gms.maps.model.LatLng endLatLng;
    public String Name;
    public int MucDoNguyHiem;
    public int ThongTin;




    public DoanNguyHiem(LatLng startlatlng,LatLng endlatlng, String name, int mucdonguyhiem, int thongtin){
        startLatLng = startlatlng;
        endLatLng = endlatlng;
        Name = name;
        MucDoNguyHiem = mucdonguyhiem;
        ThongTin = thongtin;
    }
    public void setStartLatLng(com.google.android.gms.maps.model.LatLng latLng) {
        startLatLng = latLng;
    }
    public void setEndLatLng(com.google.android.gms.maps.model.LatLng latLng) {
        endLatLng = latLng;
    }

    public void setMucDoNguyHiem(int mucDoNguyHiem) {
        MucDoNguyHiem = mucDoNguyHiem;
    }

    public void setName(String streetName) {
        Name = streetName;
    }

}
