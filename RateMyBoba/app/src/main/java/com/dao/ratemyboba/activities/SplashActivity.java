package com.dao.ratemyboba.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Bundle;
import android.widget.TextView;

import com.dao.ratemyboba.R;

public class SplashActivity extends Activity {
    private static boolean splashLoaded = false;
    private TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!splashLoaded) {
            setContentView(R.layout.activity_splash);
            int secondsDelayed = 1;
            setFont();

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    startActivity(new Intent(SplashActivity.this, FacebookActivity.class));
                    finish();
                }
            }, secondsDelayed * 1000);

            splashLoaded = true;
        }
        else {
            Intent goToFacebookActivity = new Intent(SplashActivity.this, FacebookActivity.class);
            goToFacebookActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(goToFacebookActivity);
            finish();
        }
    }
    private void setFont(){
        textView = (TextView)findViewById(R.id.appName_TV_ID);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "Amatic_Bold.ttf");
        textView.setTypeface(typeface);
    }
}