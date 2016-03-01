package com.hnmoma.driftbottle.custom;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.util.MoMaUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

public class BackGridView extends GridView {


	int vs;

	// private Bitmap background;
	Rect mMyDrawRect = new Rect();// 书架的矩形位置
	Drawable mInterlayer = this.getResources().getDrawable(R.drawable.giftitembg);// 书架图片

	public BackGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// background = BitmapFactory.decodeResource(getResources(),
		// R.drawable.giftitembg);
		vs = MoMaUtil.dip2px(getContext(), 50);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int count = getChildCount();
		for(int num = 0; num < count; num += 4 ) {
			View v = getChildAt(num);
			if (v != null) {
				int gridview_height = this.getHeight();
				int interlayerHeight = mInterlayer.getIntrinsicHeight();
				int blockGapHeight = v.getHeight();
				mMyDrawRect.left = 0;
				mMyDrawRect.right = getWidth();
				int initPos = v.getTop() + (blockGapHeight * 1 / 2);
				
				if(initPos <= gridview_height) {
					mMyDrawRect.top = initPos;
					mMyDrawRect.bottom = mMyDrawRect.top + interlayerHeight;
					mInterlayer.setBounds(mMyDrawRect);
					mInterlayer.draw(canvas);// 画书架图片
				}
				initPos += (blockGapHeight + vs);
				
		}
		}
		super.onDraw(canvas);// 画每一个Item
	}

}
