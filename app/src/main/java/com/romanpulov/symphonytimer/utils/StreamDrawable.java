package com.romanpulov.symphonytimer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ComposeShader;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/*
 * taken from Romain Guy
 * 
 */

public class StreamDrawable extends Drawable {
	private static final boolean USE_VIGNETTE = true;

	private final float mCornerRadius;
	private final RectF mRect = new RectF();
	private final BitmapShader mBitmapShader;
	private final Paint mPaint;
	private final int mMargin;

	StreamDrawable(Bitmap bitmap, float cornerRadius, int margin) {
		this.mCornerRadius = cornerRadius;

		mBitmapShader = new BitmapShader(bitmap,
				Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setShader(mBitmapShader);

		this.mMargin = margin;
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		mRect.set(mMargin, mMargin, bounds.width() - mMargin, bounds.height() - mMargin);

		if ((USE_VIGNETTE) && (mRect.centerX() > 0)) {
			RadialGradient vignette = new RadialGradient(
					mRect.centerX(), mRect.centerY() * 1.0f / 0.7f, mRect.centerX() * 1.3f,
					new int[] { 0, 0, 0x7f000000 }, new float[] { 0.0f, 0.7f, 1.0f },
					Shader.TileMode.CLAMP);

			Matrix oval = new Matrix();
			oval.setScale(1.0f, 0.7f);
			vignette.setLocalMatrix(oval);

			mPaint.setShader(
					new ComposeShader(mBitmapShader, vignette, PorterDuff.Mode.SRC_OVER));
		}
	}

	@Override
	public void draw(Canvas canvas) {
        //mPaint.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY));
        //mPaint.setColorFilter(new android.graphics.LightingColorFilter(Color.RED, Color.RED));
        float[] colorTransform = {
                0, 1f, 0, 0, 0,
                0, 0, 0f, 0, 0,
                0, 0, 0, 0f, 0,
                0, 0, 0, 1f, 0};

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0f); //Remove Colour
        colorMatrix.set(colorTransform); //Apply Red say
        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);

		canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}		
}
