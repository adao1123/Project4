package com.example.ratemyboba.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.ratemyboba.R;
import com.example.ratemyboba.activities.ShopActivity;
import com.example.ratemyboba.adapters.TeaAdapter;
import com.example.ratemyboba.adapters.TeaShopAdapter;
import com.example.ratemyboba.models.Tea;
import com.example.ratemyboba.util.RV_Space_Decoration;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by adao1 on 5/1/2016.
 */
public class HomeFragment extends Fragment implements TeaAdapter.OnTeaClickListener, TeaShopAdapter.OnTeaShopClickListener, TeaShopAdapter.OnEndOfListListener{

    private static final String TAG = "HOME FRAGMENT";
    public static final String DETAIL_KEY = "DETAILKEY";
    private static final int PERMISSION_REQUEST_CODE = 12;
    private EditText addressET;
    private Button useLocationButton;
    private CardView locationCard;
    private RecyclerView teaRV;
    private FloatingActionButton bobaFab;
    private FloatingActionButton distanceFab;
    private FloatingActionButton ratingsFab;
    private FloatingActionButton dealsFab;
    private FloatingActionMenu fabMenu;
    private MaterialSpinner withinDistanceSpinner;
    private LinearLayout enterLocationLayout;
    private List<Tea> teaList;
    private ArrayList<Business> teaShopList;
    private TeaShopAdapter teaShopAdapter;
    private TeaAdapter teaAdapter;
    private PassClickedTeaListener teaListener;
    private OnBobaFabClickListener bobaFabListener;
    private LocationManager locationManager;
    private boolean isSameSearch=false;
    private char current;
    private double latitude;
    private double longitude;
    private double withinDistance = 16100; //default within 10 miles

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        addressET = (EditText)view.findViewById(R.id.shop_address_et_id);
        useLocationButton = (Button)view.findViewById(R.id.shop_address_clear);
        locationCard = (CardView)view.findViewById(R.id.home_address_cardview);
        teaRV = (RecyclerView)view.findViewById(R.id.home_RV_id);
        distanceFab = (FloatingActionButton)view.findViewById(R.id.home_fab_distance_id);
        ratingsFab = (FloatingActionButton)view.findViewById(R.id.home_fab_rating_id);
        dealsFab = (FloatingActionButton)view.findViewById(R.id.home_fab_deals_id);
        fabMenu = (FloatingActionMenu)view.findViewById(R.id.shop_fab_menu);
        withinDistanceSpinner = (MaterialSpinner) view.findViewById(R.id.shop_spinner);
        enterLocationLayout = (LinearLayout)view.findViewById(R.id.shop_enter_location_id);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        teaList = new ArrayList<>();
        teaShopList = new ArrayList<>();
        addPlaceholderTeas(); //TEMP/PLACEHOLDER
        setDistanceSpinner();
        if (addressET.getText().toString().matches("")) {
            if (checkLocationOn()) getLocation();
        }
        setRV();
        teaRV.setAdapter(teaShopAdapter);
        setYelpApi('d', 0);
        setFabListener();
        setEnterListener();
        useLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressET.getText().clear();
            }
        });
    }

    private void setDistanceSpinner(){
        withinDistanceSpinner.setItems("Within 5 miles", "Within 10 miles", "Within 15 miles", "Within 20 miles");
        withinDistanceSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                switch (position) {
                    case 0:
                        withinDistance = 8050; // 5 miles to meters
                        break;
                    case 1:
                        withinDistance = 16100; // 10 miles to meters
                        break;
                    case 2:
                        withinDistance = 24150; // 15 miles to meters
                        break;
                    case 3:
                        withinDistance = 32200; // 20 miles to meters
                        break;
                    default:
                        withinDistance = 16100; //default 10 miles
                        break;
                }
            }
        });
    }

    private void setRV(){
        teaAdapter = new TeaAdapter(teaList,this);
//        if (checkLocationOn()) getLocation();
        teaShopAdapter = new TeaShopAdapter(teaShopList,this,this,latitude,longitude);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        teaRV.setLayoutManager(gridLayoutManager);
        RV_Space_Decoration decoration = new RV_Space_Decoration(14);
        teaRV.addItemDecoration(decoration);
    }

    private double[] getCoorfromAddress(String address){
        Geocoder geocoder = new Geocoder(getContext());
        List<Address> coordinates;
        double[] result = new double[2];
        try{
            coordinates = geocoder.getFromLocationName(address,1);
            if (coordinates.size()<=0) return null;
            result[0] = coordinates.get(0).getLatitude();
            result[1] = coordinates.get(0).getLongitude();
        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }

    private void setYelpApi(final char c,int offset){
        Log.i(TAG, "setYelpApi: inside");
        current = c;
        YelpAPIFactory yelpAPIFactory = new YelpAPIFactory(
                getString(R.string.YELP_CONSUMER_KEY), getString(R.string.YELP_CONSUMER_SECRET),
                getString(R.string.YELP_TOKEN_KEY),getString(R.string.YELP_TOKEN_SECRET));
        YelpAPI yelpAPI = yelpAPIFactory.createAPI();
        Map<String, String> params = new HashMap<>();
        params.put("category_filter","bubbletea");
//        params.put("term", "Boba");

        params.put("limit","20");
        params.put("offset",offset+"");
        params.put("radius_filter",withinDistance+"");
        if (c == 'r') params.put("sort","2");
        else params.put("sort","1");
        if (c == '$') params.put("deals_filter","true");
        CoordinateOptions coordinate;
        if (!addressET.getText().toString().matches("")){
            double[] targetCoor = getCoorfromAddress(addressET.getText().toString());
            coordinate = CoordinateOptions.builder()
                    .latitude(targetCoor[0])
                    .longitude(targetCoor[1]).build();
        }else {
            coordinate = CoordinateOptions.builder()
                    .latitude(latitude)
                    .longitude(longitude).build();
        }
        Call<SearchResponse> call = yelpAPI.search(coordinate, params);
        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                ArrayList<Business> responseList = response.body().businesses();
                if (c == '$' && responseList.size()==0)Toast.makeText(getContext(),"No Deals :(",Toast.LENGTH_LONG).show();
                if (teaShopList.size()==0)isSameSearch=false;
                else isSameSearch=true;
                teaShopList.addAll(responseList);
                if (!isSameSearch) teaShopAdapter.notifyDataSetChanged();
                else {
                    int currentSize = teaShopAdapter.getItemCount();
                    teaShopAdapter.notifyItemRangeInserted(currentSize, teaShopList.size());
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.i(TAG, "onFailure: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void addPlaceholderTeas(){
        teaList.add(new Tea("Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Milk-Tea.jpg"));
        teaList.add(new Tea("Taro Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Taro1.jpg"));
        teaList.add(new Tea("Oolong Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Oolong-Green.jpg"));
        teaList.add(new Tea("Almond Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Almond.jpg"));
        teaList.add(new Tea("Jasmine Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Jasmine.jpg"));
        teaList.add(new Tea("Honey Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Honey.jpg"));
    }

    @Override
    public void onEndOfList(int position) {
        setYelpApi(current,position+3);
    }

    private void setEnterListener(){
        addressET.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    teaShopList.clear();
                    teaRV.setAdapter(teaShopAdapter);
                    if (addressET.getText().toString().matches("")) {
                        if (checkLocationOn()) getLocation();
                    }
                    setYelpApi('d',0);
                    fabMenu.close(true);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(addressET.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }

    public interface OnBobaFabClickListener {
        void onDistanceFabClick();
    }

    public interface PassClickedTeaListener{
        void passClickedTea(Tea tea);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        teaListener = (PassClickedTeaListener)getActivity();
        bobaFabListener = (OnBobaFabClickListener)getActivity();
    }

    private void setFabListener(){
        fabMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if (opened){
                    locationCard.setVisibility(View.VISIBLE);
                    withinDistanceSpinner.setVisibility(View.VISIBLE);
                }
                else {
                    locationCard.setVisibility(View.INVISIBLE);
                    withinDistanceSpinner.setVisibility(View.INVISIBLE);
                }
                Log.i(TAG, "onMenuToggle: ");
            }
        });

//        bobaFab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                teaRV.setAdapter(teaAdapter);
//                fabMenu.close(true);
////                bobaFabListener.onDistanceFabClick();
//            }
//        });
        distanceFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teaShopList.clear();
                teaRV.setAdapter(teaShopAdapter);
                if (addressET.getText().toString().matches("")) {
                    if (checkLocationOn()) getLocation();
                }
                setYelpApi('d',0);
                fabMenu.close(true);

            }
        });
        ratingsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teaShopList.clear();
                teaRV.setAdapter(teaShopAdapter);
                if (addressET.getText().toString().matches("")) {
                    if (checkLocationOn()) getLocation();
                }
                setYelpApi('r',0);
                fabMenu.close(true);
            }
        });
        dealsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teaShopList.clear();
                teaRV.setAdapter(teaShopAdapter);
                if (addressET.getText().toString().matches("")) {
                    if (checkLocationOn()) getLocation();
                }
                setYelpApi('$',0);
                fabMenu.close(true);
            }
        });
    }

    @Override
    public void onTeaClick(Tea tea) {
        teaListener.passClickedTea(tea);
    }

    @Override
    public void onTeaShopClick(Business teaShop) {
        Intent detailIntent = new Intent(getActivity(), ShopActivity.class);
        detailIntent.putExtra(DETAIL_KEY, teaShop.id());
        startActivity(detailIntent);
    }

    public void setLocation(double[] location){
        this.latitude = location[0];
        this.longitude = location[1];
    }

    private double[] getLocation(){
        double[] location = new double[2];
        if (permissionExists()){
            String locationProvider = LocationManager.NETWORK_PROVIDER;
            Location lastKnownLocation;
            try {
                locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
                lastKnownLocation= locationManager.getLastKnownLocation(locationProvider);
                if (lastKnownLocation!=null) {
                    Log.i(TAG, "getLocation: Lat: " + lastKnownLocation.getLatitude());
                    Log.i(TAG, "getLocation: Long: " + lastKnownLocation.getLongitude());
                    latitude = lastKnownLocation.getLatitude();
                    longitude = lastKnownLocation.getLongitude();
                    location[0] = lastKnownLocation.getLatitude();
                    location[1] = lastKnownLocation.getLongitude();
                    if (teaShopAdapter!=null)teaShopAdapter.setLocation(latitude,longitude);
                }else Toast.makeText(getContext(),"Acquiring Location",Toast.LENGTH_LONG).show();
            }catch (SecurityException e) {
                Toast.makeText(getContext(), "You need to grant location permission", Toast.LENGTH_SHORT).show();
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
        int granted = getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
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
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //All location services are disabled
            Toast.makeText(getContext(),"Please Enable Location or Enter Address",Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (permissions.length < 0) {
                    return; // no permissions were returned, nothing to process here
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // contacts permission was granted! Lets now grab contacts or show them!
                    String locationProvider = LocationManager.NETWORK_PROVIDER;
                    Location lastKnownLocation;
                    try {
                        lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                        Log.i(TAG, "getLocation: Lat: " + lastKnownLocation.getLatitude());
                        Log.i(TAG, "getLocation: Long: " + lastKnownLocation.getLongitude());
                        double[] location = {lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()};

                    } catch (SecurityException e) {
                        Toast.makeText(getContext(), "You need to grant location permission", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // contacts permission was denied, lets warn the user that we need this permission!
                    Toast.makeText(getContext(), "You need to grant location permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
