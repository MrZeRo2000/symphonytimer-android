package com.romanpulov.symphonytimer;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ComposeShader;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
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

class StreamDrawable extends Drawable {
	private static final boolean USE_VIGNETTE = true;

	private final float cornerRadius;
	private final RectF rect = new RectF();
	private final BitmapShader bitmapShader;
	private final Paint paint;
	private final int margin;

	StreamDrawable(Bitmap bitmap, float cornerRadius, int margin) {
		this.cornerRadius = cornerRadius;

		bitmapShader = new BitmapShader(bitmap,
				Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setShader(bitmapShader);

		this.margin = margin;
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		rect.set(margin, margin, bounds.width() - margin, bounds.height() - margin);

		if (USE_VIGNETTE) {
			RadialGradient vignette = new RadialGradient(
					rect.centerX(), rect.centerY() * 1.0f / 0.7f, rect.centerX() * 1.3f,
					new int[] { 0, 0, 0x7f000000 }, new float[] { 0.0f, 0.7f, 1.0f },
					Shader.TileMode.CLAMP);

			Matrix oval = new Matrix();
			oval.setScale(1.0f, 0.7f);
			vignette.setLocalMatrix(oval);

			paint.setShader(
					new ComposeShader(bitmapShader, vignette, PorterDuff.Mode.SRC_OVER));
		}
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void setAlpha(int alpha) {
		paint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		paint.setColorFilter(cf);
	}		
}
