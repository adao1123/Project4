package com.example.ratemyboba.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
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
import com.example.ratemyboba.fragments.SearchFragment;
import com.example.ratemyboba.models.Tea;
import com.example.ratemyboba.util.UserLocationListener;

public class MainActivity extends AppCompatActivity implements
        HomeFragment.PassClickedTeaListener,
        BobaFragment.OnBobaSwipeRightListener,
        HomeFragment.OnDistanceFabClickListener,
        HomeFragment.OnRatingFabClickListener,
        HomeFragment.OnDealFabClickListener{

    private static final String TAG = "MAIN ACTIVITY";
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
        checkLocationOn();
        homeFragment.setLocation(getLocation());
    }

    @Override
    public void onDealFabClick() {
        checkLocationOn();
        homeFragment.setLocation(getLocation());
    }

    @Override
    public void onRatingFabClick() {
        checkLocationOn();
        homeFragment.setLocation(getLocation());
    }

    private double[] getLocation(){
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        Log.i(TAG, "getLocation: Lat: " + lastKnownLocation.getLatitude());
        Log.i(TAG, "getLocation: Long: " + lastKnownLocation.getLongitude());
        double[] location = {lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()};
        return location;
    }

    private void checkLocationOn(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //All location services are disabled
            Toast.makeText(MainActivity.this,"Please Enable Location",Toast.LENGTH_LONG).show();
            return;
        }

    }


}
