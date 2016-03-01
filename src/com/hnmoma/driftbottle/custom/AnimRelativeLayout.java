package com.hnmoma.driftbottle.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class AnimRelativeLayout extends RelativeLayout{

	public AnimRelativeLayout(Context context) {
		super(context);
	}

	public AnimRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public AnimRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		//子view添加抗锯齿标志和位图进行滤波处理：RotateAnimation等动画会出现锯齿
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));   
		super.dispatchDraw(canvas);
	}
}
