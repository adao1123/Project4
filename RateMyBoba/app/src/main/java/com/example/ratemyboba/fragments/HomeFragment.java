package com.example.ratemyboba.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ratemyboba.R;
import com.example.ratemyboba.activities.ShopActivity;
import com.example.ratemyboba.adapters.TeaAdapter;
import com.example.ratemyboba.adapters.TeaShopAdapter;
import com.example.ratemyboba.models.Tea;
import com.example.ratemyboba.util.RV_Space_Decoration;
import com.github.clans.fab.FloatingActionButton;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;

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
public class HomeFragment extends Fragment implements TeaAdapter.OnTeaClickListener, TeaShopAdapter.OnTeaShopClickListener{

    public static final String DETAIL_KEY = "DETAILKEY";
    private static final String TAG = "HOME FRAGMENT";
    List<Tea> teaList;
    PassClickedTeaListener teaListener;
    OnBobaFabClickListener bobaFabListener;
    FloatingActionButton bobaFab;
    FloatingActionButton distanceFab;
    FloatingActionButton ratingsFab;
    FloatingActionButton dealsFab;
    private ArrayList<Business> teaShopList;
    private RecyclerView teaRV;
    private TeaShopAdapter teaShopAdapter;
    private double latitude;
    private double longitude;
    private LocationManager locationManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        teaRV = (RecyclerView)view.findViewById(R.id.home_RV_id);
        bobaFab = (FloatingActionButton)view.findViewById(R.id.home_fab_boba_id);
        distanceFab = (FloatingActionButton)view.findViewById(R.id.home_fab_distance_id);
        ratingsFab = (FloatingActionButton)view.findViewById(R.id.home_fab_rating_id);
        dealsFab = (FloatingActionButton)view.findViewById(R.id.home_fab_deals_id);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        teaList = new ArrayList<>();
        teaShopList = new ArrayList<>();
        fillList(); //TEMP/PLACEHOLDER
        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        getLocation();
//        setRV();
        setTeaShopRV();
        setFabListener();
    }

    private void setRV(){
        TeaAdapter teaAdapter = new TeaAdapter(teaList,this);
        teaRV.setAdapter(teaAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        teaRV.setLayoutManager(gridLayoutManager);
        RV_Space_Decoration decoration = new RV_Space_Decoration(16);
        teaRV.addItemDecoration(decoration);
    }
    private void setTeaShopRV(){
        teaShopAdapter = new TeaShopAdapter(teaShopList,this,latitude,longitude);
        teaRV.setAdapter(teaShopAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        teaRV.setLayoutManager(gridLayoutManager);
        RV_Space_Decoration decoration = new RV_Space_Decoration(14);
        teaRV.addItemDecoration(decoration);
    }

    private void setFabListener(){
        bobaFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bobaFabListener.onDistanceFabClick();
            }
        });
        distanceFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setYelpApi('d');
            }
        });
        ratingsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setYelpApi('r');
            }
        });
        dealsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setYelpApi('$');
            }
        });
    }


    private void setYelpApi(char c){
        Log.i(TAG, "setYelpApi: inside");
        YelpAPIFactory yelpAPIFactory = new YelpAPIFactory(
                getString(R.string.YELP_CONSUMER_KEY), getString(R.string.YELP_CONSUMER_SECRET),
                getString(R.string.YELP_TOKEN_KEY),getString(R.string.YELP_TOKEN_SECRET));
        YelpAPI yelpAPI = yelpAPIFactory.createAPI();
        Map<String, String> params = new HashMap<>();
        params.put("category_filter","bubbletea");
//        params.put("term", "Boba");
        params.put("limit","20");
        if (c == 'r') params.put("sort","2");
        else params.put("sort","1");
        if (c == '$') params.put("deals_filter","true");
        CoordinateOptions coordinate = CoordinateOptions.builder()
                .latitude(latitude)
                .longitude(longitude).build();
        Call<SearchResponse> call = yelpAPI.search(coordinate,params);
        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                ArrayList<Business> responseList = response.body().businesses();
                teaShopList.clear();
                teaShopList.addAll(responseList);
                Log.d(TAG, "onResponse: Lat & Long" + latitude + longitude);
                for (Business teaShop : teaShopList) {
                    Log.i(TAG, "onResponse: " + teaShop.name());
//                    Log.i(TAG, "onResponse: " + teaShop.deals().get(0).title());
                }
                teaShopAdapter.notifyDataSetChanged();
                //setTeaShopRV();
                //teaShopAdapter.notifyItemRangeInserted(0,teaShopList.size()-1);
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.i(TAG, "onFailure: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    public void setLocation(double[] location){
        this.latitude = location[0];
        this.longitude = location[1];
    }

    private double[] getLocation(){
        if ( ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        Log.i(TAG, "getLocation: Lat: " + lastKnownLocation.getLatitude());
        Log.i(TAG, "getLocation: Long: " + lastKnownLocation.getLongitude());
        latitude = lastKnownLocation.getLatitude();
        longitude = lastKnownLocation.getLongitude();
        double[] location = {lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()};
        return location;
    }



    private void fillList(){
        for (int i = 1; i <= 25; i++){
            teaList.add(new Tea("Boba " + i));
        }
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

}
