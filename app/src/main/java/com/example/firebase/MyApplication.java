package com.example.firebase;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initCloudinary();
    }

    private void initCloudinary() {
        Map config = new HashMap();
        config.put("cloud_name", "dxxx4z4nu");
        MediaManager.init(this, config);
    }
}