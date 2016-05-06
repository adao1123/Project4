package com.example.ratemyboba;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.firebase.client.Firebase;

/**
 * Created by adao1 on 5/5/2016.
 */
public class RateMyBobaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        Firebase.setAndroidContext(this);
    }
}
