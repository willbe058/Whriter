package com.xpf.me.whriter.common;

import android.app.Application;

/**
 * Created by pengfeixie on 16/5/25.
 */
public class CommonApllication extends Application {

    public CommonApllication() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppData.init(getApplicationContext());
    }
}
