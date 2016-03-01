package com.hnmoma.driftbottle.custom;

import com.hnmoma.driftbottle.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class TextProgressBar extends ProgressBar{
	private String text;
	private Paint mPaint;
	/**
	 * 中间进度百分比的字符串的颜色
	 */
	private int textColor;
	private static final int TEXT_SIZE = 25;

	public TextProgressBar(Context context, AttributeSet attrs) {    //必须要写的构造方法
		super(context, attrs);
		
		TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar);
		textColor = mTypedArray.getColor(R.styleable.RoundProgressBar_textColor, Color.WHITE);
		
		mTypedArray.recycle();
		
		initText();
	}
	
	@Override
	public synchronized void setProgress(int progress) {
		setText(progress);
		super.setProgress(progress);
	}

	// 设置文字内容
	private void setText(int progress) {
		this.text = progress+"%";
	}
	
	// 初始化，画笔
	private void initText() {
		this.mPaint = new Paint();
		this.mPaint.setAntiAlias(true);
		this.mPaint.setColor(textColor);
		this.mPaint.setTextSize(TEXT_SIZE);
	}
	
	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Rect rect = new Rect();
		this.mPaint.getTextBounds(this.text, 0, this.text.length(), rect);
		int x = (getWidth() / 2) - rect.centerX();
		int y = (getHeight() / 2) - rect.centerY();
		canvas.drawText(this.text, x, y, this.mPaint);
	}
}