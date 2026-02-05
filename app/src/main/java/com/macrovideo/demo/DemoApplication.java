package com.macrovideo.demo;

import android.app.Application;

import com.macrovideo.sdk.SDKHelper;

public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SDKHelper.initPhoneType(10);
    }
}
