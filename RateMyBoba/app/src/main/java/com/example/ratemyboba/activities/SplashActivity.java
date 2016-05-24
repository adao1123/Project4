package com.example.ratemyboba.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.ratemyboba.R;

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
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            }, secondsDelayed * 1000);

            splashLoaded = true;
        }
        else {
            Intent goToMainActivity = new Intent(SplashActivity.this, MainActivity.class);
            goToMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(goToMainActivity);
            finish();
        }
    }
    private void setFont(){
        textView = (TextView)findViewById(R.id.appName_TV_ID);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "Amatic_Bold.ttf");
        textView.setTypeface(typeface);
    }
}