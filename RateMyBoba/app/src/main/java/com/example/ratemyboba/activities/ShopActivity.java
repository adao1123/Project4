package com.example.ratemyboba.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ratemyboba.R;
import com.example.ratemyboba.fragments.SearchFragment;
import com.squareup.picasso.Picasso;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopActivity extends AppCompatActivity {

    private static final String TAG = "SHOP ACTIVITY";
    private Business teaShop;
    private TextView titleTV;
    private ImageView shopIV;
    private String teaID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        Intent recieveIntent = getIntent();
        teaID = recieveIntent.getStringExtra(SearchFragment.DETAIL_KEY);
        Log.i(TAG, "onCreate: " + teaID);
        initViews();
        handleYelpAPI();


    }

    private void handleYelpAPI(){
        YelpAPIFactory apiFactory = new YelpAPIFactory(getString(R.string.YELP_CONSUMER_KEY), getString(R.string.YELP_CONSUMER_SECRET), getString(R.string.YELP_TOKEN_KEY), getString(R.string.YELP_TOKEN_SECRET));
        YelpAPI yelpAPI = apiFactory.createAPI();
        Call<Business> call = yelpAPI.getBusiness(teaID);
        call.enqueue(new Callback<Business>() {
            @Override
            public void onResponse(Call<Business> call, Response<Business> response) {
                teaShop = response.body();
                displayViews();
            }

            @Override
            public void onFailure(Call<Business> call, Throwable t) {

            }
        });
    }
    private void initViews(){
        titleTV = (TextView)findViewById(R.id.shop_title_id);
        shopIV = (ImageView)findViewById(R.id.shop_image_id);
    }
    private void displayViews(){
        titleTV.setText(teaShop.name());
        Picasso.with(this)
                .load(teaShop.imageUrl().replaceAll("ms", "o"))
                .into(shopIV);
    }
}
