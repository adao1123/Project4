package com.example.ratemyboba.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by adao1 on 5/4/2016.
 */
public class UserLocationListener implements LocationListener {

    public double latitude;
    public double longitude;

    @Override
    public void onLocationChanged(Location location) {
        location.getLatitude();
        location.getLongitude();
        latitude=location.getLatitude();
        longitude=location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
