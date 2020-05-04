package com.stevehwang.basicapp;

import android.app.Application;

import com.localytics.android.Localytics;

import io.branch.referral.Branch;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Localytics.autoIntegrate(this);
        Branch.enableLogging();
        Branch.getAutoInstance(this);
    }




}
