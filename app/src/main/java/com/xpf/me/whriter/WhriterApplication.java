package com.xpf.me.whriter;

import com.xpf.me.whriter.common.CommonApllication;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by pengfeixie on 16/5/23.
 */
public class WhriterApplication extends CommonApllication {
    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Anonymous-Pro.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
