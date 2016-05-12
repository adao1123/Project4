package com.example.ratemyboba.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.ratemyboba.R;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_splash);
        View easySplashScreenView = new EasySplashScreen(SplashActivity.this)
                .withFullScreen()
                .withTargetActivity(FacebookActivity.class)
                .withSplashTimeOut(3000)
                .withBackgroundResource(R.color.colorPrimary)
                .withHeaderText("Rate My Boba")
                .withFooterText("Find your fix")
                .withBeforeLogoText("My cool company")
                .withLogo(R.drawable.ic_star_black)
                .withAfterLogoText("Some more details")
                .create();

        setContentView(easySplashScreenView);
    }
}
