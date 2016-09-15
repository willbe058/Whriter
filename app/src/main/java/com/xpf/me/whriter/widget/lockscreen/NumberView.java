package com.xpf.me.whriter.widget.lockscreen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xpf.me.whriter.R;

/**
 * Created by pengfeixie on 16/7/3.
 */
public class NumberView extends RelativeLayout {

    private DotView mDotView;
    private TextView mTvNumber;

    public NumberView(Context context) {
        super(context);
        init(context);
    }

    public NumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NumberView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_number, this);
        mDotView = (DotView) findViewById(R.id.dot_view);
        mTvNumber = (TextView) findViewById(R.id.view_number_tv_number);
    }

    public void showNumber() {
        final ScaleAnimation showAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        showAnimation.setDuration(200);
        showAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mTvNumber.setVisibility(VISIBLE);
                mDotView.setVisibility(GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ScaleAnimation hideAnimation = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
                hideAnimation.setDuration(500);

                ScaleAnimation dotShowAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
                dotShowAnimation.setDuration(500);

                mDotView.setStatus(DotView.STATUS_AFTER_INPUT);
                mDotView.setVisibility(VISIBLE);
                mDotView.clearAnimation();
                mDotView.setAnimation(dotShowAnimation);
                mTvNumber.clearAnimation();
                mTvNumber.setAnimation(hideAnimation);

                hideAnimation.startNow();
                dotShowAnimation.startNow();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mTvNumber.clearAnimation();
        mTvNumber.setAnimation(showAnimation);
        showAnimation.startNow();
    }


}
