package com.example.ratemyboba.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ratemyboba.R;
import com.example.ratemyboba.models.Tea;
import com.example.ratemyboba.util.OnSwipeTouchListener;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;

import org.w3c.dom.Text;

import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by adao1 on 5/2/2016.
 */
public class BobaFragment extends Fragment {
    private static final String TAG = "BOBA FRAGMENT";
    private Tea tea;
    private TextView titleTV;
    private LinearLayout bobaLayout;
    private OnBobaSwipeRightListener swipeRightListener;
    private ArrayList<Business> teaShopList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_boba, container, false);
        titleTV = (TextView)view.findViewById(R.id.boba_title_id);
        bobaLayout = (LinearLayout)view.findViewById(R.id.boba_layout_id);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String bobaTitle = getArguments().getString("KEY");
        titleTV.setText(bobaTitle);
        setYelpApi();
        setOnSwipeListener();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        swipeRightListener = (OnBobaSwipeRightListener)getActivity();
    }

    private void setOnSwipeListener(){
        bobaLayout.setOnTouchListener(new OnSwipeTouchListener(getContext()){
            @Override
            public void onSwipeRight() {
                swipeRightListener.onBobaSwipeRight();
            }
        });
    }

    public interface OnBobaSwipeRightListener{
        void onBobaSwipeRight();
    }


    private void setYelpApi(){
        Log.i(TAG, "setYelpApi: inside");
        YelpAPIFactory yelpAPIFactory = new YelpAPIFactory(
                getString(R.string.YELP_CONSUMER_KEY), getString(R.string.YELP_CONSUMER_SECRET),
                getString(R.string.YELP_TOKEN_KEY),getString(R.string.YELP_TOKEN_SECRET));
        YelpAPI yelpAPI = yelpAPIFactory.createAPI();
        Map<String, String> params = new HashMap<>();
        //params.put("category_filter","Bubble Tea");
        params.put("term","Boba");
        Call<SearchResponse> call = yelpAPI.search("San Francisco",params);
        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                teaShopList = response.body().businesses();
                for (Business teaShop : teaShopList){
                    Log.i(TAG, "onResponse: " + teaShop.id());
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.i(TAG, "onFailure: "+t.getMessage());
                t.printStackTrace();
            }
        });
    }
}
