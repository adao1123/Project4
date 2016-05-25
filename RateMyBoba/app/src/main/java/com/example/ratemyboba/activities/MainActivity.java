package com.example.ratemyboba.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


import com.example.ratemyboba.R;
import com.example.ratemyboba.fragments.BobaFragment;
import com.example.ratemyboba.fragments.HomeFragment;
import com.example.ratemyboba.models.Tea;
import com.example.ratemyboba.util.UserLocationListener;
import com.facebook.login.LoginManager;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

public class MainActivity extends AppCompatActivity implements
        HomeFragment.PassClickedTeaListener,
        BobaFragment.OnBobaSwipeRightListener,
        HomeFragment.OnBobaFabClickListener{

    private static final String TAG = "MAIN ACTIVITY";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private LocationManager locationManager = null;
    private UserLocationListener locationListener;
    private HomeFragment homeFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_ID);
        setSupportActionBar(toolbar);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        setFragmentManager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_login,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout:
                Firebase firebaseRef = new Firebase("https://rate-my-boba.firebaseio.com/");
                AuthData authData = firebaseRef.getAuth();
                if (authData != null) {
                    firebaseRef.unauth();
                    LoginManager.getInstance().logOut();
//                    setAuthenticatedUser(null);
                    Intent loginIntent = new Intent(MainActivity.this,FacebookActivity.class);
                    startActivity(loginIntent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Set up Fragment Manager and initially set Home Fragment
     */
    private void setFragmentManager(){
        fragmentManager = getSupportFragmentManager();
        setHomeFragment();
    }

    /**
     * Sets Home Fragment with fragment Transaction
     */
    private void setHomeFragment(){
        fragmentTransaction = fragmentManager.beginTransaction();
        HomeFragment homeFragment = new HomeFragment();
        fragmentTransaction.add(R.id.fragment_contatinerID, homeFragment);
        fragmentTransaction.commit();
    }

    /**
     * From the listener from the tea Shop adapter passing through the home fragment.
     * @param tea
     */
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

    /**
     * Listens to when page is swiped right and moves between fragments
     */
    @Override
    public void onBobaSwipeRight() {
        fragmentTransaction = fragmentManager.beginTransaction();
        homeFragment = new HomeFragment();
        fragmentTransaction.replace(R.id.fragment_contatinerID, homeFragment);
        fragmentTransaction.commit();
    }

    /**
     * On Home Fab button Listener, it will get location then set it in another fragment.
     */
    @Override
    public void onDistanceFabClick() {
        if (checkLocationOn()) {
            homeFragment.setLocation(getLocation());
        }
    }

    /**
     * Gets Location Coordinatates
     * Returns a double array with latitude at [0] and longitude at [1]
     * @return
     */
    private double[] getLocation(){
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

    /**
     * Checks if permission Exists.
     * Returns boolean
     * @return
     */
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

    /**
     * Requestions user for perrmission if android M
     */
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

    /**
     * Check if Locations setting is on
     * @return
     */
    private boolean checkLocationOn(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //All location services are disabled
            Toast.makeText(MainActivity.this,"Please Enable Location",Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * When the User agrees to the permissions, get Location
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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
    }

    /**
     * Overrides Back Pressed So doesn't go back to Facebook
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
