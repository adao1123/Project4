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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ratemyboba.R;
import com.example.ratemyboba.adapters.TeaAdapter;
import com.example.ratemyboba.fragments.HomeFragment;
import com.example.ratemyboba.models.Review;
import com.example.ratemyboba.models.Tea;
import com.example.ratemyboba.util.RV_Space_Decoration;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.francescocervone.openratingview.RatingView;
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
    private RecyclerView reviewsRV;
    private EditText reviewRatingET;
    private EditText reviewBodyET;
    private Button submitButton;
    private RatingView ratingView;
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
    private Firebase firebaseTeas;
    private AuthData authData;
    private int rating =0;


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
        submitReviewListener();
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
        reviewsRV = (RecyclerView)findViewById(R.id.shop_reviews_id);
        ratingIV = (ImageView)findViewById(R.id.shop_rating_id);
        shopIV = (ImageView)findViewById(R.id.shop_image_id);
        bobaRV = (RecyclerView)findViewById(R.id.shop_bobaRV_id);
        reviewBodyET = (EditText)findViewById(R.id.shop_review_body_id);
        reviewRatingET = (EditText)findViewById(R.id.shop_review_rating_id);
        submitButton = (Button)findViewById(R.id.shop_review_submit_id);
        ratingView = (RatingView)findViewById(R.id.shop_review_star_id);
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
//                    reviewsRV.setText(dataSnapshot.getValue(String.class));
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
        FirebaseRecyclerAdapter mAdapter = new FirebaseRecyclerAdapter<Tea, BobaViewHolder>(Tea.class, R.layout.rv_tea_item, BobaViewHolder.class, firebaseTeas) {
            @Override
            protected void populateViewHolder(BobaViewHolder bobaViewHolder, Tea tea, int i) {
                bobaViewHolder.titleTV.setText(tea.getTitle());
                Picasso.with(ShopActivity.this)
                        .load(tea.getImageUrl())
                        .into(bobaViewHolder.imageView);
            }
        };
        //TeaAdapter teaAdapter = new TeaAdapter(teaList,this);//PLACEHOLDER
        bobaRV.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bobaRV.setLayoutManager(linearLayoutManager);
        RV_Space_Decoration decoration = new RV_Space_Decoration(14);
        bobaRV.addItemDecoration(decoration);
    }

    private void initFirebase(){
        firebaseRef = new Firebase("https://rate-my-boba.firebaseio.com/");
        authData = firebaseRef.getAuth();
        String userID = authData.getUid();
        userName = (String) authData.getProviderData().get("displayName");
        Log.i(TAG, "initFirebase: PRINT NAME " + userName);
        firebaseShops = firebaseRef.child("Shops");
        firebaseChildShop = firebaseShops.child(teaShop.id());
        firebaseChildShop.child("name").setValue(teaShop.name());
        firebaseChildShop.child("rating").setValue(teaShop.rating());
        //firebaseReviews.setValue(teaShop.reviews().get(0).ratingImageLargeUrl());
        Log.i(TAG, "initFirebase: Rating image " + teaShop.ratingImgUrlLarge());
        firebaseReviews = firebaseChildShop.child("review");
        firebaseTeas = firebaseChildShop.child("teas");
        firebaseReviews.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Review review = new Review(teaShop.reviews().get(0).user().name(),teaShop.reviews().get(0).excerpt(),teaShop.reviews().get(0).rating()+"");
                if (!dataSnapshot.hasChildren()) firebaseReviews.push().setValue(review);
                Log.i(TAG, "onDataChange: inside ");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        firebaseTeas.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) fillTempList();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        setReviewRV();
        setBobaRV();
    }

    private void submitReviewListener(){
        ratingView.setOnStarClickListener(new RatingView.OnStarClickListener() {
            @Override
            public void onClick(int i) {
                rating = i;
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rating==0){
                    Toast.makeText(ShopActivity.this,"Click on Stars to Rate",Toast.LENGTH_LONG).show();
                    return;
                }
                Review review = new Review(userName,reviewBodyET.getText().toString(),rating+"");
                firebaseReviews.push().setValue(review);
                reviewBodyET.getText().clear();
                reviewRatingET.getText().clear();
            }
        });
    }

    @Override
    public void onTeaClick(Tea tea) {

    }

    private void setReviewRV(){
//        RecyclerView recycler = (RecyclerView) findViewById(R.id.shop_reviews_id);
//        recycler.setHasFixedSize(true);
//        recycler.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerAdapter mAdapter = new FirebaseRecyclerAdapter<Review, ReviewsViewHolder>(Review.class, R.layout.rv_review_item, ReviewsViewHolder.class, firebaseReviews) {
            @Override
            protected void populateViewHolder(ReviewsViewHolder reviewsViewHolder, Review review, int i) {
                reviewsViewHolder.reviewUserTV.setText(review.getUser());
                reviewsViewHolder.reviewBodyTV.setText(review.getText());
                reviewsViewHolder.reviewRatingTV.setText(review.getRating());
            }
        };
        reviewsRV.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        reviewsRV.setLayoutManager(linearLayoutManager);
        RV_Space_Decoration decoration = new RV_Space_Decoration(14);
        reviewsRV.addItemDecoration(decoration);

    }

    public static class ReviewsViewHolder extends RecyclerView.ViewHolder {
        TextView reviewUserTV;
        TextView reviewBodyTV;
        TextView reviewRatingTV;

        public ReviewsViewHolder(View itemView) {
            super(itemView);
            reviewUserTV = (TextView)itemView.findViewById(R.id.rv_review_user);
            reviewBodyTV = (TextView) itemView.findViewById(R.id.rv_review_body);
            reviewRatingTV = (TextView)itemView.findViewById(R.id.rv_review_rating);
        }
    }

    public static class BobaViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTV;
        public ImageView imageView;
        public Button upButton;
        public Button downButton;

        public BobaViewHolder(View itemView) {
            super(itemView);
            titleTV = (TextView)itemView.findViewById(R.id.rv_tea_name);
            imageView = (ImageView)itemView.findViewById(R.id.rv_tea_image);
            upButton = (Button)itemView.findViewById(R.id.rv_tea_plus);
            downButton = (Button)itemView.findViewById(R.id.rv_tea_minus);
        }
    }

    private void fillTempList(){
//        for (int i = 0; i<25; i++){
//            teaList.add(new Tea("Boba Tea " + i));
//            firebaseTeas.push().setValue(new Tea("Boba Tea " + i));
//        }
        firebaseTeas.push().setValue(new Tea("Milk Tea","http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Milk-Tea.jpg"));
        firebaseTeas.push().setValue(new Tea("Taro Tea","http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Taro1.jpg"));
        firebaseTeas.push().setValue(new Tea("Oolong Tea","http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Oolong-Green.jpg"));
        firebaseTeas.push().setValue(new Tea("Almond Tea","http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Almond.jpg"));
        firebaseTeas.push().setValue(new Tea("Jasmine Tea","http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Jasmine.jpg"));
        firebaseTeas.push().setValue(new Tea("Honey Tea","http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Honey.jpg"));
    }


}
