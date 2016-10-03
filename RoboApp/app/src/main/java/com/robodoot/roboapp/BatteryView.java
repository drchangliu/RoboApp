package com.robodoot.roboapp;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.robodoot.dr.facetracktest.R;

/**
 * Created by Alex on 1/16/2016.
 */
public class BatteryView extends View {
    private float mCharge = 1.0f;
    private boolean mShowPercent = true;
    private float mTextHeight = 0.0f;
    private float mWidth = 0.0f;
    private float mHeight = 0.0f;
    private int mHighColor = Color.GREEN;
    private int mMidColor = 0xFFFFE600; // "yolk" yellow
    private int mLowColor = Color.RED;
    private int mBackColor = Color.WHITE;
    private boolean mConnected = false;

    // http://freevector.co/vector-icons/interface/empty-battery-2.html
    Bitmap mBatteryOutlineBitmap;
    Bitmap mNotConnectedBitmap;

    // event listener for when charge changes
    OnChargeChangedListener mListener;
    public interface OnChargeChangedListener {
        void onEvent();
    }
    public void setChargeChangedListener(OnChargeChangedListener eventListener) {
        mListener = eventListener;
    }

    // paint objects
    Paint mBitmapPaint;
    Paint mChargeBarPaint;
    Paint mBackBarPaint;
    Paint mTextPaint;

    // RectF objects for drawing
    private RectF mNotConnectedRectF = new RectF(0, 0, 0, 0);
    private RectF mBatteryOutlineRectF = new RectF(0, 0, 0, 0);

    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BatteryView,
                0, 0);

        try {
            mShowPercent = a.getBoolean(R.styleable.BatteryView_batteryShowPercent, true);
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitmapPaint.setColor(Color.WHITE);

        mChargeBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mChargeBarPaint.setStyle(Paint.Style.FILL);

        mBackBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackBarPaint.setColor(mBackColor);
        mBackBarPaint.setStyle(Paint.Style.FILL);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(mTextHeight);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mBatteryOutlineBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.battery_outline);

        mNotConnectedBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.na);

        setCharge(mCharge);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Account for padding
        float xpad = (float)(getPaddingLeft() + getPaddingRight());
        float ypad = (float)(getPaddingTop() + getPaddingBottom());

        float ww = (float)w - xpad;
        float hh = (float)h - ypad;

        mWidth = ww;
        mHeight = hh;
        mTextHeight = hh / 2.0f;
        mTextPaint.setTextSize(mTextHeight);

        float mNotConnectedLeftPos = mWidth * 0.25f;
        float mNotConnectedTopPos = mHeight * 0.25f;
        mNotConnectedRectF = new RectF(mNotConnectedLeftPos, mNotConnectedTopPos, mWidth * 0.7f, mHeight * 0.75f);
        mBatteryOutlineRectF = new RectF(0, 0, mWidth, mHeight);

        invalidate();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mConnected) {
            float left = mWidth * 0.06f;
            float top = mHeight * 0.05f;

            canvas.drawRect(left, top, left + mWidth * 0.82f, top + mHeight * 0.85f, mBackBarPaint);

            canvas.drawRect(left, top, left + mWidth * mCharge * 0.82f, top + mHeight * 0.85f, mChargeBarPaint);

            if (mShowPercent) {
                Paint.FontMetrics metric = mTextPaint.getFontMetrics();
                int textHeight = (int) Math.ceil(metric.descent - metric.ascent);
                canvas.drawText((int) (mCharge * 100.0f) + "%", mWidth / 2.0f, mHeight / 2.0f + textHeight / 3, mTextPaint);
            }
        }
        else {
            canvas.drawBitmap(mNotConnectedBitmap, null, mNotConnectedRectF, mBitmapPaint);
        }

        canvas.drawBitmap(mBatteryOutlineBitmap, null, mBatteryOutlineRectF, mBitmapPaint);
    }

    private void refresh() {
        invalidate();
        requestLayout();
    }

    public float getCharge() {
        return mCharge;
    }
    public void setCharge(float charge) {
        mCharge = Math.max(0.0f, Math.min(1.0f, charge));

        if (mCharge < 0.5f) {
            mChargeBarPaint.setColor((int) new ArgbEvaluator().evaluate(mCharge * 2.0f, mLowColor, mMidColor));
        }
        else {
            mChargeBarPaint.setColor((int) new ArgbEvaluator().evaluate(mCharge * 2.0f - 1.0f, mMidColor, mHighColor));
        }

        refresh();

        // invoke onEvent() of the charge event listener
        if (mListener != null)
            mListener.onEvent();
    }

    public boolean isShowPercent() {
        return mShowPercent;
    }
    public void setShowPercent(boolean showPercent) {
        mShowPercent = showPercent;
        refresh();
    }

    public int getHighColor() {
        return mHighColor;
    }
    public void setHighColor(int highColor) {
        mHighColor = highColor;
        refresh();
    }

    public int getMidColor() {
        return mMidColor;
    }
    public void setMidColor(int midColor) {
        mMidColor = midColor;
        refresh();
    }

    public int getLowColor() {
        return mLowColor;
    }
    public void setLowColor(int lowColor) {
        mLowColor = lowColor;
        refresh();
    }

    public int getBackColor() {
        return mBackColor;
    }
    public void setBackColor(int backColor) {
        mBackColor = backColor;
        mChargeBarPaint.setColor(mBackColor);
        refresh();
    }

    public boolean isConnected() {
        return mConnected;
    }
    public void setConnected(boolean connected) {
        mConnected = connected;
        refresh();
    }
}