package com.xpf.me.whriter.widget.lockscreen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.xpf.me.whriter.R;

/**
 * Created by pengfeixie on 16/6/29.
 */
public class PasswordView extends RelativeLayout {

    private NumberView numberView;
    private Button test;

    public PasswordView(Context context) {
        super(context);
        init(context);
    }

    public PasswordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PasswordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_password, this);
        numberView = (NumberView) findViewById(R.id.number1);
        test = (Button) findViewById(R.id.test);
        test.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                numberView.showNumber();
            }
        });
    }
}
