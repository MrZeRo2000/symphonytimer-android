package com.romanpulov.symphonytimer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.util.StateSet;

import com.romanpulov.symphonytimer.R;

public class RoundedBitmapBackgroundBuilder {

    final public static int BG_NORMAL = 0;
    final public static int BG_FINAL = 1;
    final private static int BRIGHTENING_FACTOR = 100;

    private Context mContext;
    private int mWidth;
    private int mHeight;
    private float mCornerRadius;

    private Boolean mIsBitmapPrepared = false;

    private Boolean mIsDrawablePrepared = false;
    private Drawable[] mDrawables = new Drawable[2];

    private Bitmap mScaledBg;
    private Bitmap mScaledBrightBg;

    private Bitmap mFinalScaledBg;
    private Bitmap mFinalScaledBrightBg;

    private final ColorFilter mBrightBgColorFilter = new ColorMatrixColorFilter(
            new float[]{
                    1f, 0f, 0f, 0f, 100f,
                    0f, 1f, 0f, 0f, 100f,
                    0f, 0f, 1f, 0f, 100f,
                    0f, 0f, 0f, 1f, 100f
            }
    );

    private final ColorFilter mFinalBrightBgColorFilter = new ColorMatrixColorFilter(
            new float[]{
                    0f, 0f, 1f, 0f, 100f,
                    0f, 1f, 0f, 0f, 100f,
                    1f, 0f, 0f, 0f, 100f,
                    0f, 0f, 0f, 1f, 100f
            }
    );

    private final ColorFilter mFinalBgColorFilter = new ColorMatrixColorFilter(
            new float[]{
                    0f, 0f, 1f, 0f, 0f,
                    0f, 1f, 0f, 0f, 0f,
                    1f, 0f, 0f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
            }
    );


    public RoundedBitmapBackgroundBuilder(Context context, int width, int height, float cornerRadius) {
        this.mContext = context;
        this.mWidth = width;
        this.mHeight = height;
        this.mCornerRadius = cornerRadius;
    }

    private void prepareBitmaps() {

        final Bitmap bg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.sky_home_sm);
        final Bitmap brightBg = createBrightBitmap(bg, BRIGHTENING_FACTOR);
        mScaledBg = Bitmap.createScaledBitmap(bg, mWidth, mHeight, false);
        mScaledBrightBg = Bitmap.createScaledBitmap(brightBg, mWidth, mHeight, false);

        final Bitmap finalBg = createBlueToRedBitmap(bg);
        final Bitmap finalBrightBg = createBrightBitmap(finalBg, BRIGHTENING_FACTOR);
        mFinalScaledBg = Bitmap.createScaledBitmap(finalBg, mWidth, mHeight, false);
        mFinalScaledBrightBg = Bitmap.createScaledBitmap(finalBrightBg, mWidth, mHeight, false);

        mIsBitmapPrepared = true;

    }

    private void prepareDrawables() {
        for (int drawableType = BG_NORMAL; drawableType <= BG_FINAL; drawableType++ ) {
            final Drawable bgDrawable = new StreamDrawable(mScaledBg, mCornerRadius, 0);
            final Drawable bgBrightDrawable = new StreamDrawable(mScaledBrightBg, mCornerRadius, 0);
            final Drawable bgFinalDrawable = new StreamDrawable(mFinalScaledBg, mCornerRadius, 0);
            final Drawable bgFinalBrightDrawable = new StreamDrawable(mFinalScaledBrightBg, mCornerRadius, 0);

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Log.d(toString(), "prepareBitmaps elapsed time=" + elapsedTime);
    }

    private Drawable newDrawable(int drawableType) {
        //ensure bitmaps are prepared
        if (!mIsBitmapPrepared) {
            prepareBitmaps();
        }

        long startTime = System.currentTimeMillis();

        final Drawable bgDrawable = new StreamDrawable(mScaledBg, mCornerRadius, 0);
        final Drawable bgBrightDrawable = new StreamDrawable(mScaledBrightBg, mCornerRadius, 0);
        final Drawable bgFinalDrawable = new StreamDrawable(mFinalScaledBg, mCornerRadius, 0);
        final Drawable bgFinalBrightDrawable = new StreamDrawable(mFinalScaledBrightBg, mCornerRadius, 0);

        final StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_pressed }, (drawableType == BG_NORMAL) ? bgBrightDrawable : bgFinalBrightDrawable);
        drawable.addState(StateSet.WILD_CARD, (drawableType == BG_NORMAL) ? bgDrawable : bgFinalDrawable);

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Log.d(toString(), "newDrawable elapsed time=" + elapsedTime);

        return drawable;
    }

    public Drawable buildDrawable(int type) {

        if (!mIsBitmapPrepared) {
            prepareBitmaps();
        }

        /*
        final Drawable bgDrawable = new StreamDrawable(mScaledBg, mCornerRadius, 0);
        final Drawable bgBrightDrawable = new StreamDrawable(mScaledBrightBg, mCornerRadius, 0);
        final Drawable bgFinalDrawable = new StreamDrawable(mFinalScaledBg, mCornerRadius, 0);
        final Drawable bgFinalBrightDrawable = new StreamDrawable(mFinalScaledBrightBg, mCornerRadius, 0);
        */
        final Drawable bgDrawable = new StreamDrawable(mScaledBg, mCornerRadius, 0);
        final Drawable bgBrightDrawable = new StreamDrawable(mScaledBg, mCornerRadius, 0);
        bgBrightDrawable.setColorFilter(mBrightBgColorFilter);
        final Drawable bgFinalDrawable = new StreamDrawable(mScaledBg, mCornerRadius, 0);
        bgFinalDrawable.setColorFilter(mFinalBgColorFilter);
        final Drawable bgFinalBrightDrawable = new StreamDrawable(mScaledBg, mCornerRadius, 0);
        bgFinalBrightDrawable.setColorFilter(mFinalBrightBgColorFilter);

        final StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_pressed }, (type == BG_NORMAL) ? bgBrightDrawable : bgFinalBrightDrawable);
        drawable.addState(StateSet.WILD_CARD, (type == BG_NORMAL) ? bgDrawable : bgFinalDrawable);

        return drawable;

        /*
        if (!mIsDrawablePrepared) {
            prepareDrawables();
        }
        return mDrawables[type].getConstantState().newDrawable();
        */
    }

    private Bitmap createBrightBitmap(Bitmap src, int value) {

        final int width = src.getWidth();
        final int height = src.getHeight();

        final int[] pixels = new int[height * width];
        src.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < height * width; i++) {
            pixels[i] = Color.rgb(
                    (Color.red(pixels[i]) + value > 255 ? 255 : Color.red(pixels[i]) + value),
                    (Color.green(pixels[i]) + value > 255 ? 255 : Color.green(pixels[i]) + value),
                    (Color.blue(pixels[i]) + value > 255 ? 255 : Color.blue(pixels[i]) + value)
            );
        }

        final Bitmap res = Bitmap.createBitmap(width, height, src.getConfig());
        res.setPixels(pixels, 0, width, 0, 0, width, height);

        return res;
    }

    private Bitmap createBlueToRedBitmap(Bitmap src) {

        final int width = src.getWidth();
        final int height = src.getHeight();

        final int[] pixels = new int[height * width];
        src.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < height * width; i++) {
            pixels[i] = Color.rgb(
                    Color.blue(pixels[i]),
                    Color.green(pixels[i]),
                    Color.red(pixels[i])
            );
        }

        final Bitmap res = Bitmap.createBitmap(width, height, src.getConfig());
        res.setPixels(pixels, 0, width, 0, 0, width, height);

        return res;
    }

}
