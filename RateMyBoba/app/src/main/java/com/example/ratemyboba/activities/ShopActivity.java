package com.example.ratemyboba.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ratemyboba.R;
import com.example.ratemyboba.adapters.TeaAdapter;
import com.example.ratemyboba.adapters.TeaShopAdapter;
import com.example.ratemyboba.fragments.HomeFragment;
import com.example.ratemyboba.models.Review;
import com.example.ratemyboba.models.Tea;
import com.example.ratemyboba.util.RV_Space_Decoration;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.Deal;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopActivity extends AppCompatActivity implements TeaAdapter.OnTeaClickListener{

    private static final String TAG = "SHOP ACTIVITY";
    private Business teaShop;
    private TextView titleTV;
    private TextView openTV;
    private TextView phoneTV;
    private TextView dealsTV;
    private TextView reviewsTV;
    private ImageView ratingIV;
    private ImageView shopIV;
    private String teaID;
    private String userName;
    private RecyclerView bobaRV;
    private ArrayList<Tea> teaList;
    private Firebase firebaseRef;

    private Firebase firebaseShops;
    private Firebase firebaseChildShop;
    private Firebase firebaseReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        Intent recieveIntent = getIntent();
        teaID = recieveIntent.getStringExtra(HomeFragment.DETAIL_KEY);
        Log.i(TAG, "onCreate: " + teaID);
        initViews();
        handleYelpAPI();
        setPhoneIntent();
        setBobaRV();
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
                initFirebase();
            }

            @Override
            public void onFailure(Call<Business> call, Throwable t) {

            }
        });
    }
    private void initViews(){
        titleTV = (TextView)findViewById(R.id.shop_title_id);
        phoneTV = (TextView)findViewById(R.id.shop_phone_id);
        openTV = (TextView)findViewById(R.id.shop_url_id);
        dealsTV = (TextView)findViewById(R.id.shop_deals_id);
        reviewsTV = (TextView)findViewById(R.id.shop_reviews_id);
        ratingIV = (ImageView)findViewById(R.id.shop_rating_id);
        shopIV = (ImageView)findViewById(R.id.shop_image_id);
        bobaRV = (RecyclerView)findViewById(R.id.shop_bobaRV_id);
    }
    private void displayViews(){
        titleTV.setText(teaShop.name());
        Picasso.with(this)
                .load(teaShop.imageUrl().replaceAll("ms", "o"))
                .into(shopIV);
        phoneTV.setText(teaShop.displayPhone());
        if (teaShop.isClosed()){
            openTV.setText("Closed");
            openTV.setTextColor(Color.RED);
        }else{
            openTV.setText("Open");
            openTV.setTextColor(Color.GREEN);
        }
//        if (teaShop.reviews()!=null){
//            firebaseReviews.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    reviewsTV.setText(dataSnapshot.getValue(String.class));
//                }
//
//                @Override
//                public void onCancelled(FirebaseError firebaseError) {
//
//                }
//            });
//        }
        Picasso.with(this)
                .load(teaShop.ratingImgUrlLarge())
                .into(ratingIV);
        if (teaShop.deals()!=null) {
            String dealDisplay = "";
            for (Deal deal : teaShop.deals()){
                String tempDeal = deal.title() + "\n" + deal.additionalRestrictions() + "\n" + deal.importantRestrictions() + "\n" + deal.whatYouGet()+ "\n" +deal.timeStart()+ "\n" +deal.timeEnd();
                dealDisplay += "\n\n" + tempDeal;
            }
            dealsTV.setText(dealDisplay);
        }
    }
    private void setPhoneIntent(){
        phoneTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:" + teaShop.phone();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });
    }
    private void setBobaRV(){
        teaList = new ArrayList<>();
        fillTempList();
        TeaAdapter teaAdapter = new TeaAdapter(teaList,this);//PLACEHOLDER
        bobaRV.setAdapter(teaAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bobaRV.setLayoutManager(linearLayoutManager);
        RV_Space_Decoration decoration = new RV_Space_Decoration(14);
        bobaRV.addItemDecoration(decoration);
    }

    private void fillTempList(){
        for (int i = 0; i<25; i++){
            teaList.add(new Tea("Boba Tea " + i));
        }
    }

    private void initFirebase(){
        firebaseRef = new Firebase("https://rate-my-boba.firebaseio.com/");
        AuthData authData = firebaseRef.getAuth();
        String userID = authData.getUid();
        firebaseShops = firebaseRef.child("Shops");
        firebaseChildShop = firebaseShops.child(teaShop.id());
        firebaseChildShop.child("name").setValue(teaShop.name());
        firebaseChildShop.child("rating").setValue(teaShop.rating());
        firebaseReviews = firebaseChildShop.child("review");
        firebaseReviews.setValue(teaShop.reviews().get(0).ratingImageLargeUrl());
        Log.i(TAG, "initFirebase: Rating image " + teaShop.ratingImgUrlLarge());
        Log.i(TAG, "initFirebase: USER-ID" + userID);
        firebaseRef.child("users").child(userID).child("displayName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName = dataSnapshot.getValue(String.class);
                Log.i(TAG, "onDataChange: " + userName);
                Review review = new Review(userName,"getReview from Edit Text","4.5");
                firebaseReviews.push().setValue(review);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        Log.i(TAG, "initFirebase: " + userName);
        // Review review = new Review()
    }

    @Override
    public void onTeaClick(Tea tea) {

    }
}
