package com.xpf.me.whriter.widget;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.github.mr5.icarus.Icarus;
import com.github.mr5.icarus.entity.Link;
import com.github.mr5.icarus.popover.Popover;
import com.google.gson.Gson;

/**
 * Created by pengfeixie on 16/5/25.
 */
public class FontPopoverImpl implements Popover {

    protected TextView textView;
    protected Icarus icarus;
    protected Handler mainLopperHandler;

    public FontPopoverImpl(TextView textView, Icarus icarus) {
        this.textView = textView;
        this.icarus = icarus;
        mainLopperHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void show(String params, final String callbackName) {
        Gson gson = new Gson();
        Log.d("@popover params", params);
        final Font font = gson.fromJson(params, Font.class);

        mainLopperHandler.post(new Runnable() {
            @Override
            public void run() {
                font.setScale("large");
                icarus.jsCallback(callbackName, font, Font.class);
            }
        });
    }

    @Override
    public void hide() {

    }
}
