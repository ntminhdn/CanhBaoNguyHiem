package com.example.thanh.canhbaonguyhiem;

import android.location.Location;

public interface GPSCallback
{
        public abstract void onGPSUpdate(Location location);
}