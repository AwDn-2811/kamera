package com.example.android_projects;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.cloudinary.android.MediaManager;

import java.util.HashMap;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Init Firebase
        FirebaseApp.initializeApp(this);

        // Init Cloudinary
        HashMap config = new HashMap();
        config.put("cloud_name", "dskqwokji");
        config.put("api_key", "868821764693376");

        try {
            MediaManager.init(this, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
