package com.xpf.me.whriter.lockscreen;

/**
 * Created by pengfeixie on 16/6/29.
 */
public interface CheckCallback {

    void onPassed();

    void onDenied(String s);
}
