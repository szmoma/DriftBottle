package com.hnmoma.driftbottle.custom;

import com.hnmoma.driftbottle.util.MoMaLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CircularImage extends ImageView {
	private static final Xfermode MASK_XFERMODE;
	private Bitmap mask;
	private Paint paint;

	public int mBorderWidth = 5;
	private int mBorderColor = Color.parseColor("#f5f5f5");
	private boolean isUseBorder = false; // 是否需要描边，默认情况下，不需要描边

	static {
		PorterDuff.Mode localMode = PorterDuff.Mode.DST_IN;
		MASK_XFERMODE = new PorterDuffXfermode(localMode);
	}

	public CircularImage(Context paramContext) {
		super(paramContext);
	}

	public CircularImage(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
	}

	public CircularImage(Context paramContext, AttributeSet paramAttributeSet,
			int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
	}

	public Bitmap createMask() {
		int width = getWidth();
		int height = getHeight();
		Bitmap.Config localConfig = Bitmap.Config.ARGB_8888;
		Bitmap localBitmap = Bitmap.createBitmap(width, height, localConfig);
		Canvas localCanvas = new Canvas(localBitmap);
		Paint localPaint = new Paint(1);
		localPaint.setColor(-16777216);
		int padding = 0;
		if (isUseBorder())
			padding = mBorderWidth != 0 ? mBorderWidth - 2 : 0;
		else
			padding = 2;
		/**
		 * 设置椭圆的大小(因为椭圆的最外边会和border的最外边重合的，如果图片最外边的颜色很深，有看出有棱边的效果，所以为了让体验更加好，
		 * 让其缩进padding px)
		 */
		RectF localRectF = new RectF(padding, padding, width, height);
		localCanvas.drawOval(localRectF, localPaint);
		return localBitmap;
	}

	protected void onDraw(Canvas paramCanvas) {
		Drawable localDrawable = getDrawable();
		if (localDrawable == null)
			return;
		try {
			if (this.paint == null) {
				Paint localPaint1 = new Paint();
				this.paint = localPaint1;
				this.paint.setFilterBitmap(false);
				Paint localPaint2 = this.paint;
				Xfermode localXfermode1 = MASK_XFERMODE;
				@SuppressWarnings("unused")
				Xfermode localXfermode2 = localPaint2
						.setXfermode(localXfermode1);
			}
			int width = getWidth();
			int height = getHeight();
			/** 保存layer */
			int layer = paramCanvas.saveLayer(0.0F, 0.0F, width, height, null,
					31);
			localDrawable.setBounds(0, 0, width, height);
			localDrawable.draw(paramCanvas);
			if ((this.mask == null) || (this.mask.isRecycled())) {
				Bitmap localBitmap1 = createMask();
				this.mask = localBitmap1;
			}
			Bitmap localBitmap2 = this.mask;
			Paint localPaint3 = this.paint;
			/** 保存layer */
			paramCanvas.drawBitmap(localBitmap2, 0.0F, 0.0F, localPaint3);
			/** 将画布复制到layer上 */
			paramCanvas.restoreToCount(layer);
			// 是否需要描边
			if (isUseBorder)
				drawBorder(paramCanvas, width, height);
			return;
		} catch (Exception localException) {
			MoMaLog.d("debug",
					"Attempting to draw with recycled bitmap. View ID =");
		}
	}

	/**
	 * 绘制最外面的边框
	 * 
	 * @param canvas
	 * @param width
	 * @param height
	 */
	private void drawBorder(Canvas canvas, final int width, final int height) {
		if (mBorderWidth == 0) {
			return;
		}
		final Paint mBorderPaint = new Paint();
		mBorderPaint.setStyle(Paint.Style.STROKE);
		mBorderPaint.setAntiAlias(true);
		mBorderPaint.setColor(mBorderColor);
		mBorderPaint.setStrokeWidth(mBorderWidth);
		/**
		 * 坐标x：view宽度的一般 坐标y：view高度的一般 半径r：因为是view宽度的一半-border
		 */
		canvas.drawCircle(width >> 1, height >> 1, (width >> 1) - mBorderWidth,
				mBorderPaint);
		canvas = null;
	}

	public boolean isUseBorder() {
		return isUseBorder;
	}

	public void setUseBorder(boolean isUseBorder) {
		this.isUseBorder = isUseBorder;
	}
}