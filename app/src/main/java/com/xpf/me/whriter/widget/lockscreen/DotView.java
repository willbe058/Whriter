package com.xpf.me.whriter.widget.lockscreen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;

import com.xpf.me.whriter.common.AppData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by pengfeixie on 16/6/30.
 */
public class DotView extends View {

    public static final int STATUS_NOT_INPUT = 0;
    public static final int STATUS_AFTER_INPUT = 1;

    @IntDef({STATUS_NOT_INPUT, STATUS_AFTER_INPUT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {
    }

    private int mDotWidth;
    private int mDotHeight;
    @Status
    private int mStatus = STATUS_NOT_INPUT;
    private Paint mStrokePaint;
    private Paint mSolidPaint;


    public DotView(Context context) {
        super(context);
        init(context, null);
    }

    public DotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mStrokePaint = new Paint();
        mSolidPaint = new Paint();

        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setColor(context.getResources().getColor(android.R.color.white));
        mStrokePaint.setStyle(Paint.Style.STROKE);

        mSolidPaint.setAntiAlias(true);
        mSolidPaint.setColor(AppData.getColor(android.R.color.white));
    }

    public void setStatus(@Status int status) {
        this.mStatus = status;
        invalidate();
    }

    @Status
    public int getStatus() {
        return mStatus;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int center = getWidth() / 2;
        int r = 10;

        switch (mStatus) {
            case STATUS_NOT_INPUT:
                mStrokePaint.setStrokeWidth(2);
                canvas.drawCircle(center, center, r, mStrokePaint);
                break;
            case STATUS_AFTER_INPUT:
                canvas.drawCircle(center, center, r, mSolidPaint);
                break;
        }
    }
}
