package com.romanpulov.symphonytimer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;

import com.romanpulov.symphonytimer.R;

public class RoundedBitmapBackgroundBuilder {

    final public static int BG_NORMAL = 0;
    final public static int BG_FINAL = 1;
    final public static int BG_NORMAL_ONLY = 2;
    final public static int BG_PRESSED_ONLY = 3;
    final private static float BRIGHTENING_FACTOR = 50f;

    private Context mContext;
    private int mWidth;
    private int mHeight;
    private float mCornerRadius;

    private Boolean mIsBitmapPrepared = false;

    private Bitmap mScaledBg;

    private final static ColorFilter BRIGHT_BG_COLOR_FILTER = new ColorMatrixColorFilter(
            new float[]{
                    1f, 0f, 0f, 0f, BRIGHTENING_FACTOR,
                    0f, 1f, 0f, 0f, BRIGHTENING_FACTOR,
                    0f, 0f, 1f, 0f, BRIGHTENING_FACTOR,
                    0f, 0f, 0f, 1f, BRIGHTENING_FACTOR
            }
    );
    private final static ColorFilter FINAL_BG_COLOR_FILTER = new ColorMatrixColorFilter(
            new float[]{
                    0f, 0f, 1f, 0f, 0f,
                    0f, 1f, 0f, 0f, 0f,
                    1f, 0f, 0f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
            }
    );
    private final static ColorFilter FINAL_BRIGHT_BG_COLOR_FILTER = new ColorMatrixColorFilter(
            new float[]{
                    0f, 0f, 1f, 0f, BRIGHTENING_FACTOR,
                    0f, 1f, 0f, 0f, BRIGHTENING_FACTOR,
                    1f, 0f, 0f, 0f, BRIGHTENING_FACTOR,
                    0f, 0f, 0f, 1f, BRIGHTENING_FACTOR
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
        mScaledBg = Bitmap.createScaledBitmap(bg, mWidth, mHeight, false);
        mIsBitmapPrepared = true;
    }

    public Drawable buildDrawable(int bgType) {
        if (!mIsBitmapPrepared) {
            prepareBitmaps();
        }

        StreamDrawable normalDrawable;
        StreamDrawable pressedDrawable;

        switch (bgType) {
            case BG_NORMAL:
                normalDrawable = new StreamDrawable(mScaledBg, mCornerRadius, 0);
                pressedDrawable = new StreamDrawable(mScaledBg, mCornerRadius, 0);
                pressedDrawable.setStreamColorFilter(BRIGHT_BG_COLOR_FILTER);
                break;
            case BG_FINAL:
                normalDrawable = new StreamDrawable(mScaledBg, mCornerRadius, 0);
                normalDrawable.setStreamColorFilter(FINAL_BG_COLOR_FILTER);
                pressedDrawable = new StreamDrawable(mScaledBg, mCornerRadius, 0);
                pressedDrawable.setStreamColorFilter(FINAL_BRIGHT_BG_COLOR_FILTER);
                break;
            case BG_NORMAL_ONLY:
                return new StreamDrawable(mScaledBg, mCornerRadius, 0);
            case BG_PRESSED_ONLY:
                pressedDrawable = new StreamDrawable(mScaledBg, mCornerRadius, 0);
                pressedDrawable.setStreamColorFilter(BRIGHT_BG_COLOR_FILTER);
                return pressedDrawable;
            default:
                return null;
        }

        final StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_pressed }, pressedDrawable);
        drawable.addState(StateSet.WILD_CARD, normalDrawable);
        return drawable;
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
