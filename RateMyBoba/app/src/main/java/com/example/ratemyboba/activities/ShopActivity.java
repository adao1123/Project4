package com.example.ratemyboba.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
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
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.francescocervone.openratingview.RatingView;
import com.squareup.picasso.Picasso;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.Deal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopActivity extends AppCompatActivity implements TeaAdapter.OnTeaClickListener{

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
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

        Button takePhotoButton = (Button)findViewById(R.id.shop_takephoto);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyStoragePermissions(ShopActivity.this);
            }
        });
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
                Review review = new Review(teaShop.reviews().get(0).user().name(), teaShop.reviews().get(0).excerpt(), teaShop.reviews().get(0).rating() + "");
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


    private void setBobaRV(){
        teaList = new ArrayList<>();
        Query queryRef = firebaseTeas.orderByChild("points");

        FirebaseRecyclerAdapter mAdapter = new FirebaseRecyclerAdapter<Tea, BobaViewHolder>(Tea.class, R.layout.rv_tea_item, BobaViewHolder.class, queryRef) {
            @Override
            protected void populateViewHolder(final BobaViewHolder bobaViewHolder, final Tea tea, int i) {
                bobaViewHolder.titleTV.setText(tea.getTitle());
                if (!tea.getImageUrl().contains("http")){
                    byte[] imageAsBytes = Base64.decode(tea.getImageUrl().getBytes(),Base64.DEFAULT);
                    bobaViewHolder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes,0,imageAsBytes.length));
                }else {
                    Picasso.with(ShopActivity.this)
                            .load(tea.getImageUrl())
                            .into(bobaViewHolder.imageView);
                }
                bobaViewHolder.pointsTV.setText(tea.getPoints()*(-1)+"");

                firebaseTeas.child(tea.getTitle()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        bobaViewHolder.pointsTV.setText(tea.getPoints()*(-1)+"");
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
                bobaViewHolder.upButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tea.setNumUp(tea.getNumUp() + 1);
                        tea.setPoints(tea.getNumDown() - tea.getNumUp());
                        firebaseTeas.child(tea.getTitle()).setValue(tea);
                    }
                });
                bobaViewHolder.downButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tea.setNumDown(tea.getNumDown()+1);
                        tea.setPoints(tea.getNumDown() - tea.getNumUp());
                        firebaseTeas.child(tea.getTitle()).setValue(tea);
                    }
                });
            }
        };
        //TeaAdapter teaAdapter = new TeaAdapter(teaList,this);//PLACEHOLDER
        bobaRV.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bobaRV.setLayoutManager(linearLayoutManager);
        RV_Space_Decoration decoration = new RV_Space_Decoration(0);
        bobaRV.addItemDecoration(decoration);
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
        public TextView pointsTV;
        public ImageView imageView;
        public Button upButton;
        public Button downButton;

        public BobaViewHolder(View itemView) {
            super(itemView);
            titleTV = (TextView)itemView.findViewById(R.id.rv_tea_name);
            pointsTV = (TextView)itemView.findViewById(R.id.rv_tea_points);
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
        firebaseTeas.child("Milk Tea").setValue(new Tea("Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Milk-Tea.jpg"));
        firebaseTeas.child("Taro Tea").setValue(new Tea("Taro Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Taro1.jpg"));
        firebaseTeas.child("Oolong Milk Tea").setValue(new Tea("Oolong Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Oolong-Green.jpg"));
        firebaseTeas.child("Almond Milk Tea").setValue(new Tea("Almond Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Almond.jpg"));
        firebaseTeas.child("Jasmine Milk Tea").setValue(new Tea("Jasmine Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Jasmine.jpg"));
        firebaseTeas.child("Honey Milk Tea").setValue(new Tea("Honey Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Honey.jpg"));
    }

    private static final int TAKE_PICTURE = 1;
    private Uri imageUri;

    public void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }else {
            takePhoto();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    storeImageToFirebase();
                    Uri selectedImage = imageUri;
//                    getContentResolver().notifyChange(selectedImage, null);
//                    ImageView imageView = (ImageView) findViewById(R.id.shop_photo);
//                    ContentResolver cr = getContentResolver();
//                    Bitmap bitmap;
//                    Log.i(TAG, "onActivityResult: " + imageUri);
//                    try {
//                        bitmap = android.provider.MediaStore.Images.Media
//                                .getBitmap(cr, selectedImage);
//
//                    //    imageView.setImageBitmap(bitmap);
//                        Toast.makeText(this, selectedImage.toString(),
//                                Toast.LENGTH_LONG).show();
//                        Log.i(TAG, "onActivityResult:tooking picture. In try  ");
//                    } catch (Exception e) {
//                        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
//                                .show();
//                        Log.e("Camera", e.toString());
//                    }


//                    //Bitmap bmp =  BitmapFactory..decodeFile(imageUri);//decodeResource(getResources(), R.drawable.chicken);//your image
//                    Bitmap  bmp = null;
//                    //TODO: PUT THIS IN SEPERATE THREAD
//                    try {
//                        bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
//                    }catch (FileNotFoundException e){
//
//                    }catch (IOException e){
//
//                    }
//                    ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
//                    if (bmp!=null) {
//                        bmp.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
//                        bmp.recycle();
//                        byte[] byteArray = bYtE.toByteArray();
//                        String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);
//                        Log.i(TAG, "onActivityResult: IMAGEFILE BYTE" + imageFile);
//
//                        EditText makeTeaNameET = (EditText)findViewById(R.id.shop_make_tea_id);
//
//
//                        firebaseTeas.child(makeTeaNameET.getText().toString()).setValue(new Tea(makeTeaNameET.getText().toString(),imageFile));
//                    }
//
//
                }
        }
    }

    private void storeImageToFirebase() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8; // shrink it down otherwise we will use stupid amounts of memory
        Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), options);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
        EditText makeTeaNameET = (EditText)findViewById(R.id.shop_make_tea_id);

        firebaseTeas.child(makeTeaNameET.getText().toString()).setValue(new Tea(makeTeaNameET.getText().toString(),base64Image));
        makeTeaNameET.getText().clear();
//         we finally have our base64 string version of the image, save it.
//        firebaseTeas.child().setValue(base64Image);
        System.out.println("Stored image with length: " + bytes.length);
    }

}
