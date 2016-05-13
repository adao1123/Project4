package com.example.ratemyboba.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.example.ratemyboba.R;
import com.example.ratemyboba.fragments.BobaFragment;
import com.example.ratemyboba.fragments.HomeFragment;
import com.example.ratemyboba.models.Tea;
import com.example.ratemyboba.util.UserLocationListener;

public class MainActivity extends AppCompatActivity implements
        HomeFragment.PassClickedTeaListener,
        BobaFragment.OnBobaSwipeRightListener,
        HomeFragment.OnBobaFabClickListener{

    private static final String TAG = "MAIN ACTIVITY";
    private static final int PERMISSION_REQUEST_CODE = 1;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    LocationManager locationManager = null;
    UserLocationListener locationListener;
    HomeFragment homeFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        setFragmentManager();
    }

    private void setFragmentManager(){
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        HomeFragment homeFragment = new HomeFragment();
//        homeFragment.setLocation(getLocation());
        fragmentTransaction.add(R.id.fragment_contatinerID, homeFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void passClickedTea(Tea tea) {
        fragmentTransaction = fragmentManager.beginTransaction();
        BobaFragment bobaFragment = new BobaFragment();
        Bundle bundle = new Bundle();
        bundle.putString("KEY", tea.getTitle());
        bobaFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.fragment_contatinerID, bobaFragment);
        fragmentTransaction.commit();

    }

    @Override
    public void onBobaSwipeRight() {
        fragmentTransaction = fragmentManager.beginTransaction();
        homeFragment = new HomeFragment();
        fragmentTransaction.replace(R.id.fragment_contatinerID, homeFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onDistanceFabClick() {
        if (checkLocationOn()) {
            homeFragment.setLocation(getLocation());
        }
    }

    private double[] getLocation(){
//        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
//            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//        }
//
//        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
//            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
//            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
//        }else {
//            String locationProvider = LocationManager.NETWORK_PROVIDER;
//            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
//            Log.i(TAG, "getLocation: Lat: " + lastKnownLocation.getLatitude());
//            Log.i(TAG, "getLocation: Long: " + lastKnownLocation.getLongitude());
//            double[] location = {lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()};
//            return location;
//        }
//
        double[] location = new double[2];
        if (permissionExists()){
            String locationProvider = LocationManager.NETWORK_PROVIDER;
            Location lastKnownLocation;
            try {
                lastKnownLocation= locationManager.getLastKnownLocation(locationProvider);
                Log.i(TAG, "getLocation: Lat: " + lastKnownLocation.getLatitude());
                Log.i(TAG, "getLocation: Long: " + lastKnownLocation.getLongitude());
                location[0] = lastKnownLocation.getLatitude();
                location[1] = lastKnownLocation.getLongitude();
            }catch (SecurityException e) {
                Toast.makeText(getApplicationContext(), "You need to grant location permission", Toast.LENGTH_SHORT).show();
            }
            return location;
        } else{
            requestUserForPermission();
        }
        return location;
    }

    @TargetApi(23)
    private boolean permissionExists(){
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion < Build.VERSION_CODES.M){

            // Permissions are already granted during INSTALL TIME for older OS version
            return true;
        }

        int granted = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (granted == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    @TargetApi(23)
    private void requestUserForPermission(){
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion < Build.VERSION_CODES.M){
            // This OS version is lower then Android M, therefore we have old permission model and should not ask for permission
            return;
        }
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        requestPermissions(permissions, PERMISSION_REQUEST_CODE);
    }

    private boolean checkLocationOn(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //All location services are disabled
            Toast.makeText(MainActivity.this,"Please Enable Location",Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode){
            case PERMISSION_REQUEST_CODE:
                if (permissions.length < 0){
                    return; // no permissions were returned, nothing to process here
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // contacts permission was granted! Lets now grab contacts or show them!
                    String locationProvider = LocationManager.NETWORK_PROVIDER;
                    Location lastKnownLocation;
                        try {
                            lastKnownLocation= locationManager.getLastKnownLocation(locationProvider);
                            Log.i(TAG, "getLocation: Lat: " + lastKnownLocation.getLatitude());
                            Log.i(TAG, "getLocation: Long: " + lastKnownLocation.getLongitude());
                            double[] location = {lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()};

                        }catch (SecurityException e) {
                            Toast.makeText(getApplicationContext(), "You need to grant location permission", Toast.LENGTH_SHORT).show();
                        }
                } else {
                    // contacts permission was denied, lets warn the user that we need this permission!
                    Toast.makeText(getApplicationContext(), "You need to grant location permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
//
//        if (requestCode!=PERMISSION_REQUEST_CODE)return;
//        if (permissions.length<=0)return;
//        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
//            String locationProvider = LocationManager.NETWORK_PROVIDER;
//            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
//            Log.i(TAG, "getLocation: Lat: " + lastKnownLocation.getLatitude());
//            Log.i(TAG, "getLocation: Long: " + lastKnownLocation.getLongitude());
//            double[] location = {lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()};
//            //return location;
//        }else Toast.makeText(MainActivity.this,"You need to grant location permission",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
