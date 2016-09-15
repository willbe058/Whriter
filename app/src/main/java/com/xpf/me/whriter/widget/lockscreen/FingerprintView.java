package com.xpf.me.whriter.widget.lockscreen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xpf.me.whriter.R;

/**
 * Created by pengfeixie on 16/6/29.
 */
public class FingerprintView extends RelativeLayout {

    private TextView mStatus;

    public FingerprintView(Context context) {
        super(context);
        init(context);
    }

    public FingerprintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FingerprintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_fingerprint, this);
        mStatus = (TextView) findViewById(R.id.fingerprint_tips);
    }

    public void setStatusText(String info) {
        mStatus.setText(info);
    }

}
