package com.example.ratemyboba.activities;

import android.Manifest;
import android.app.Activity;
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
import com.example.ratemyboba.view_holders.BobaViewHolder;
import com.example.ratemyboba.view_holders.ReviewsViewHolder;
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
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopActivity extends AppCompatActivity implements TeaAdapter.OnTeaClickListener{

    private static final String TAG = "SHOP ACTIVITY";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int TAKE_PICTURE = 2;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private TextView titleTV;
    private TextView openTV;
    private TextView phoneTV;
    private TextView dealsTV;
    private ImageView ratingIV;
    private ImageView shopIV;
    private RecyclerView reviewsRV;
    private RecyclerView bobaRV;
    private RatingView ratingView;
    private EditText reviewBodyET;
    private EditText makeTeaNameET;
    private Button submitButton;
    private Button takePhotoButton;
    private Firebase firebaseRef;
    private Firebase firebaseShops;
    private Firebase firebaseChildShop;
    private Firebase firebaseReviews;
    private Firebase firebaseTeas;
    private FirebaseRecyclerAdapter teaAdapter;
    private FirebaseRecyclerAdapter reviewAdapter;
    private AuthData authData;
    private Business teaShop;
    private Uri imageUri;
    private ArrayList<Tea> teaList;
    private String teaID;
    private String userName;
    private String userImage;
    private double rating = 0;
    private boolean haveVoted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        initViews();
        getTeaId();
        handleYelpAPI();
        setPhoneIntent();
        submitReviewListener();
        setPhotoButton();
    }

    /**
     * After getting back from camera, and result is okay, call method to store image to firebase;
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    storeImageToFirebase();
                }
        }
    }

    /**
     * When a tea in the Tea RecyclerView is clicked, nothing happens
     * Future: Go into search of that type of milk tea.
     * @param tea
     */
    @Override
    public void onTeaClick(Tea tea) {

    }

    /**
     * Initialize global views
     */
    private void initViews(){
        titleTV = (TextView)findViewById(R.id.shop_title_id);
        phoneTV = (TextView)findViewById(R.id.shop_phone_id);
        openTV = (TextView)findViewById(R.id.shop_url_id);
        dealsTV = (TextView)findViewById(R.id.shop_deals_id);
        reviewBodyET = (EditText)findViewById(R.id.shop_review_body_id);
        makeTeaNameET = (EditText)findViewById(R.id.shop_make_tea_id);
        ratingIV = (ImageView)findViewById(R.id.shop_rating_id);
        shopIV = (ImageView)findViewById(R.id.shop_image_id);
        submitButton = (Button)findViewById(R.id.shop_review_submit_id);
        takePhotoButton = (Button)findViewById(R.id.shop_takephoto);
        bobaRV = (RecyclerView)findViewById(R.id.shop_bobaRV_id);
        reviewsRV = (RecyclerView)findViewById(R.id.shop_reviews_id);
        ratingView = (RatingView)findViewById(R.id.shop_review_star_id);
    }

    /**
     * Gets String teaID from intent and stores to global variable teaID
     * Returns teaID
     */
    private String getTeaId(){
        Intent recieveIntent = getIntent();
        teaID = recieveIntent.getStringExtra(HomeFragment.DETAIL_KEY);
        return teaID;
    }

    /**
     * Create Yelp API factory and make call to get teaShop data from ID
     * Calls methods to display Views and manage Firebase
     */
    private void handleYelpAPI(){
        YelpAPIFactory apiFactory = new YelpAPIFactory(getString(R.string.YELP_CONSUMER_KEY), getString(R.string.YELP_CONSUMER_SECRET), getString(R.string.YELP_TOKEN_KEY), getString(R.string.YELP_TOKEN_SECRET));
        YelpAPI yelpAPI = apiFactory.createAPI();
        Call<Business> call = yelpAPI.getBusiness(teaID);
        call.enqueue(new Callback<Business>() {
            @Override
            public void onResponse(Call<Business> call, Response<Business> response) {
                teaShop = response.body();
                displayViews();
                manageFirebase();
            }

            @Override
            public void onFailure(Call<Business> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * Calls methods to create the firebase and add all data of the teaShop
     */
    private void manageFirebase(){
        initFirebase();
        getFirebaseAuth();
        setShopInFirebase();
        setReviewRV();
        setBobaRV();
    }

    /**
     * Sets all the views with data from the teaShop
     */
    private void displayViews(){
        titleTV.setText(teaShop.name());
        loadImagePicasso(shopIV, teaShop.imageUrl().replaceAll("ms", "o"));
        if (teaShop.displayPhone()!=null)phoneTV.setText(teaShop.displayPhone().substring(3,teaShop.displayPhone().length()));
        setOpenStatusTV();
        loadImagePicasso(ratingIV, teaShop.ratingImgUrlLarge());
        setDealsTV();
    }

    /**
     * Set takePhotoButton OnClickListener
     * Calls verifyStoragePermissions when clicked
     */
    private void setPhotoButton(){
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (makeTeaNameET.getText().toString().matches("")) {
                    Toast.makeText(ShopActivity.this, "Enter Tea Name", Toast.LENGTH_LONG).show();
                    return;
                }
                verifyStoragePermissions(ShopActivity.this);
            }
        });
    }

    /**
     * Loads Image from ImageUri into ImageView using Picasso
     * @param imageView
     * @param imageUrl
     */
    private void loadImagePicasso(ImageView imageView, String imageUrl){
        if (imageView==ratingIV) Picasso.with(this).load(imageUrl).resize(600,100).into(imageView);
        else Picasso.with(this).load(imageUrl).into(imageView);
    }

    /**
     * Sets text and textColor for open status textview
     * Depending on whether the teaShop is opened or closed
     */
    private void setOpenStatusTV(){
        if (teaShop.isClosed()){
            openTV.setText("Closed");
            openTV.setTextColor(Color.RED);
        }else{
            openTV.setText("Open");
            openTV.setTextColor(Color.GREEN);
        }
    }

    /**
     * Sets line of text for Deals text view
     * Only if there are deals present, else it will return
     */
    private void setDealsTV(){
        if (teaShop.deals()==null)return;
        String dealDisplay = "";
        for (Deal deal : teaShop.deals()){
            String tempDeal = deal.title() + "\n" + deal.additionalRestrictions() + "\n"
                    + deal.importantRestrictions() + "\n" + deal.whatYouGet()+ "\n"
                    +deal.timeStart()+ "\n" +deal.timeEnd();
            dealDisplay += "\n\n" + tempDeal;
        }
        dealsTV.setText(dealDisplay);
    }

    /**
     * Opens up phone dial on phones with the teaShop's phone number
     */
    private void setPhoneIntent(){
        phoneTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:" + teaShop.phone();
//                if (Intent.ACTION_DIAL==null)return;
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });
    }

    /**
     * Creates the root Firebase ref and makes child "Shops"
     */
    private void initFirebase(){
        firebaseRef = new Firebase("https://rate-my-boba.firebaseio.com/");
        firebaseShops = firebaseRef.child("Shops");
    }

    /**
     * Gets the current user's userName
     * Returns the user name as well as set it to global variable userName
     */
    private String getFirebaseAuth(){
        authData = firebaseRef.getAuth();
//        String userID = authData.getUid();
        userName = (String) authData.getProviderData().get("displayName");
        userImage = (String) authData.getProviderData().get("profileImageURL");
        Log.i(TAG, "initFirebase: PRINT NAME " + userName);
        Log.i(TAG, "initFirebase: PRINT Image " + userImage);
        return userName;
    }

    /**
     * Makes the specific teaShop to Firebase and set child values: name and rating to it.
     * Calls methods to add Reviews and Tea List
     */
    private void setShopInFirebase(){
        firebaseChildShop = firebaseShops.child(teaShop.id());
        firebaseChildShop.child("name").setValue(teaShop.name());
        firebaseChildShop.child("rating").setValue(teaShop.rating());
        makeReviewFireBase();
        makeTeaFireBase();
    }

    /**
     * Makes the review child for specific store in Firebase
     * Checks if there are any reviews, if not, creates a default review from Yelp
     */
    private void makeReviewFireBase(){
        firebaseReviews = firebaseChildShop.child("review");
        firebaseReviews.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Review review = new Review(teaShop.reviews().get(0).user().name(),
                        teaShop.reviews().get(0).user().imageUrl().replaceAll("ms", "ls"),
                        teaShop.reviews().get(0).excerpt(),
                        teaShop.reviews().get(0).rating() + "");
                Log.i(TAG, "onDataChange: " + teaShop.reviews().get(0).user().imageUrl());
                if (!dataSnapshot.hasChildren()) firebaseReviews.push().setValue(review);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "onCancelled: " + firebaseError.getMessage());
            }
        });
    }

    /**
     * Makes the Tea chils for specific store in Firebase
     * Checks if there are any teas in the tea list, if not, creates default placeholder teas
     */
    private void makeTeaFireBase(){
        firebaseTeas = firebaseChildShop.child("teas");
        firebaseTeas.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) addPlaceHolderTeas();
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "onCancelled: " + firebaseError.getMessage());
            }
        });
    }

    /**
     * Makes Firebase RecyclerView Adapter that populate the BobaViewHolder
     * Calls methods to help display images, calculate points, and button listeners
     */
    private void makeFirebaseTeaRVAdapter(){
        Query queryRef = firebaseTeas.orderByChild("points");
        teaAdapter = new FirebaseRecyclerAdapter<Tea, BobaViewHolder>(Tea.class, R.layout.rv_tea_item, BobaViewHolder.class, queryRef) {
            @Override
            protected void populateViewHolder(final BobaViewHolder bobaViewHolder, final Tea tea, int i) {
                bobaViewHolder.titleTV.setText(tea.getTitle());
                setTeaRVImages(tea, bobaViewHolder.imageView);
                bobaViewHolder.pointsTV.setText(tea.getPoints() * (-1) + "");
                updateTeaPoints(tea, bobaViewHolder.pointsTV);
                setVoteButtonListeners(tea, bobaViewHolder.upButton,'+');
                setVoteButtonListeners(tea, bobaViewHolder.downButton,'-');
            }
        };
    }

    /**
     * Sets ImageView for each tea in the TeaRV depending on type of image there is
     * @param tea
     * @param imageView
     */
    private void setTeaRVImages(Tea tea,ImageView imageView){
        if (!tea.getImageUrl().contains("http")){
            byte[] imageAsBytes = Base64.decode(tea.getImageUrl().getBytes(), Base64.DEFAULT);
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
        }else {
            loadImagePicasso(imageView,tea.getImageUrl());
        }
    }

    /**
     * Makes an addValueEventListener to update tea Points
     * @param tea
     * @param textView
     */
    private void updateTeaPoints(final Tea tea, final TextView textView){
        firebaseTeas.child(tea.getTitle()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                textView.setText(tea.getPoints() * (-1) + "");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "onCancelled: " + firebaseError.getMessage());
            }
        });
    }

    /**
     * Sets OnClickListerners for UpVote and DownVote buttons
     * Depending on which button, it will increment numUp or numDown
     * Then it wll calculate points and update to Firebase
     * @param tea
     * @param voteButton
     * @param c
     */
    private void setVoteButtonListeners(final Tea tea, Button voteButton, final char c){
        voteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (haveVoted) return;
                if (c == '+') tea.setNumUp(tea.getNumUp() + 1);
                else if (c == '-') tea.setNumDown(tea.getNumDown() + 1);
                tea.setPoints(tea.getNumDown() - tea.getNumUp());
                firebaseTeas.child(tea.getTitle()).setValue(tea);
                haveVoted = true;
            }
        });

    }

    /**
     * Sets Boba RV adapter and layout manager
     */
    private void setBobaRV(){
        teaList = new ArrayList<>();
        makeFirebaseTeaRVAdapter();
        bobaRV.setAdapter(teaAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bobaRV.setLayoutManager(linearLayoutManager);
        RV_Space_Decoration decoration = new RV_Space_Decoration(0);
        bobaRV.addItemDecoration(decoration);
    }

    /**
     * Sets global variable rating to the number of stars clicked
     */
    private void setStarRatingListener(){
        ratingView.setOnStarClickListener(new RatingView.OnStarClickListener() {
            @Override
            public void onClick(int i) {
                rating = (double) i;
            }
        });
    }

    /**
     * Review Submit Button Listener that pushes review to firebaseReviews
     * Clears Review edit text
     */
    private void submitReviewListener(){
        setStarRatingListener();
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rating == 0) {
                    Toast.makeText(ShopActivity.this, "Click on Stars to Rate", Toast.LENGTH_LONG).show();
                    return;
                }
                Review review = new Review(userName, userImage, reviewBodyET.getText().toString(), rating + "");
                firebaseReviews.push().setValue(review);
                reviewBodyET.getText().clear();
            }
        });
    }

    /**
     * Sets Review RecyclerView adapter and layout manager
     */
    private void setReviewRV(){
        makeFirebaseReviewRVAdapter();
        reviewsRV.setAdapter(reviewAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        reviewsRV.setLayoutManager(linearLayoutManager);
        RV_Space_Decoration decoration = new RV_Space_Decoration(14);
        reviewsRV.addItemDecoration(decoration);
    }

    /**
     * Makes Firebase RecyclerView Adapter that display views in reviewsViewHolder
     */
    private void makeFirebaseReviewRVAdapter(){
        reviewAdapter = new FirebaseRecyclerAdapter<Review, ReviewsViewHolder>(Review.class, R.layout.rv_review_item, ReviewsViewHolder.class, firebaseReviews) {
            @Override
            protected void populateViewHolder(ReviewsViewHolder reviewsViewHolder, Review review, int i) {
                reviewsViewHolder.reviewUserTV.setText(review.getUser());
                reviewsViewHolder.reviewBodyTV.setText(review.getText());
                reviewsViewHolder.reviewRatingTV.setText(review.getRating());
                Picasso.with(ShopActivity.this).load(review.getUserImage()).resize(250,250).into(reviewsViewHolder.reviewUserIV);
            }
        };
    }

    /**
     * Adds placeholder Teas for new tea Shops into Firebase
     */
    private void addPlaceHolderTeas(){
        firebaseTeas.child("Milk Tea").setValue(new Tea("Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Milk-Tea.jpg"));
        firebaseTeas.child("Taro Tea").setValue(new Tea("Taro Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Taro1.jpg"));
        firebaseTeas.child("Oolong Milk Tea").setValue(new Tea("Oolong Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Oolong-Green.jpg"));
        firebaseTeas.child("Almond Milk Tea").setValue(new Tea("Almond Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Almond.jpg"));
        firebaseTeas.child("Jasmine Milk Tea").setValue(new Tea("Jasmine Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Jasmine.jpg"));
        firebaseTeas.child("Honey Milk Tea").setValue(new Tea("Honey Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Honey.jpg"));
    }

    /**
     * Goes to the phone camera using an intent
     * Goes to onActivityforResult with photo
     */
    public void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    /**
     * Checks if permission is granted, if not request for permissions
     * Else, calls method to go to camera
     * @param activity
     */
    public void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
        }else {
            takePhoto();
        }
    }

    /**
     * Gets image taken from camera, changes to byte64 then stores to firebase
     */
    private void storeImageToFirebase() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8; // shrink it down otherwise we will use stupid amounts of memory
        Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), options);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap mutableBitmap = convertToMutable(bitmap);
        mutableBitmap.setWidth(1);
        mutableBitmap.setHeight(1);
        mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
        firebaseTeas.child(makeTeaNameET.getText().toString()).setValue(new Tea(makeTeaNameET.getText().toString(),base64Image));
        makeTeaNameET.getText().clear();
//         we finally have our base64 string version of the image, save it.
//        firebaseTeas.child().setValue(base64Image);
        System.out.println("Stored image with length: " + bytes.length);
    }

    /**
     * Converts a immutable bitmap to a mutable bitmap. This operation doesn't allocates
     * more memory that there is already allocated.
     *
     * @param imgIn - Source image. It will be released, and should not be used more
     * @return a copy of imgIn, but muttable.
     */
    public static Bitmap convertToMutable(Bitmap imgIn) {
        try {
            //this is the file going to use temporally to save the bytes.
            // This file will not be a image, it will store the raw image data.
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

            //Open an RandomAccessFile
            //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
            //into AndroidManifest.xml file
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

            // get the width and height of the source bitmap.
            int width = imgIn.getWidth();
            int height = imgIn.getHeight();
            Bitmap.Config type = imgIn.getConfig();

            //Copy the byte to the file
            //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes()*height);
            imgIn.copyPixelsToBuffer(map);
            //recycle the source bitmap, this will be no longer used.
            imgIn.recycle();
            System.gc();// try to force the bytes from the imgIn to be released

            //Create a new bitmap to load the bitmap again. Probably the memory will be available.
            imgIn = Bitmap.createBitmap(width, height, type);
            map.position(0);
            //load it back from temporary
            imgIn.copyPixelsFromBuffer(map);
            //close the temporary file and channel , then delete that also
            channel.close();
            randomAccessFile.close();

            // delete the temp file
            file.delete();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imgIn;
    }

}
