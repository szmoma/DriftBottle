package com.hnmoma.driftbottle.custom;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.R;

public class WxFloatMenu extends LinearLayout implements View.OnClickListener{

	/**
	 * 记录小悬浮窗的宽度
	 */
	public static int viewWidth;

	/**
	 * 记录小悬浮窗的高度
	 */
	public static int viewHeight;

	/**
	 * 记录系统状态栏的高度
	 */
	 private static int statusBarHeight;

	/**
	 * 用于更新小悬浮窗的位置
	 */
	private WindowManager windowManager;

	/**
	 * 小悬浮窗的参数
	 */
	private WindowManager.LayoutParams mParams;

	/**
	 * 记录当前手指位置在屏幕上的横坐标值
	 */
	private float xInScreen;

	/**
	 * 记录当前手指位置在屏幕上的纵坐标值
	 */
	private float yInScreen;

	/**
	 * 记录手指按下时在屏幕上的横坐标的值
	 */
	private float xDownInScreen;

	/**
	 * 记录手指按下时在屏幕上的纵坐标的值
	 */
	private float yDownInScreen;

	/**
	 * 记录手指按下时在小悬浮窗的View上的横坐标的值
	 */
	private float xInView;

	/**
	 * 记录手指按下时在小悬浮窗的View上的纵坐标的值
	 */
	private float yInView;
	
	public WxFloatMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WxFloatMenu(Context context) {
		super(context);
		initView(context);
	}
	
	// 初始化窗体
	public void initView(Context context){
		LayoutInflater.from(context).inflate(R.layout.wxfloatmenu, this);
		View view = findViewById(R.id.small_window_layout);
		viewWidth = view.getLayoutParams().width;
		viewHeight = view.getLayoutParams().height;
		
		ImageButton ib_close = (ImageButton) view.findViewById(R.id.ib_close);
		ib_close.setOnClickListener(this);
		
		Button bt_fz = (Button) view.findViewById(R.id.bt_fz);
		bt_fz.setOnClickListener(this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
			xInView = event.getX();
			yInView = event.getY();
			xDownInScreen = event.getRawX();
			yDownInScreen = event.getRawY() - getStatusBarHeight();
			xInScreen = event.getRawX();
			yInScreen = event.getRawY() - getStatusBarHeight();
			break;
		case MotionEvent.ACTION_MOVE:
			xInScreen = event.getRawX();
			yInScreen = event.getRawY() - getStatusBarHeight();
			// 手指移动的时候更新小悬浮窗的位置
			updateViewPosition();
			break;
		case MotionEvent.ACTION_UP:
			// 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
			if (xDownInScreen == xInScreen && yDownInScreen == yInScreen) {
//				openBigWindow();
//				MyApplication.access_type = MyConstants.ACCESS_TYPE_MY;
//				Intent intent = new Intent(getContext(), MainActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				intent.setAction(MyConstants.My_Action);
//				getContext().startActivity(intent);
			}
			break;
		default:
			break;
		}
		return true;
	}
	
	/**
	 * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
	 * 
	 * @param params
	 *            小悬浮窗的参数
	 */
	public void setWindowManagerParams(WindowManager actWindowManager, WindowManager.LayoutParams params) {
		this.windowManager = actWindowManager;
		mParams = params;
	}

	/**
	 * 更新小悬浮窗在屏幕中的位置。
	 */
	private void updateViewPosition() {
		mParams.x = (int) (xInScreen - xInView);
		mParams.y = (int) (yInScreen - yInView);
		windowManager.updateViewLayout(this, mParams);
	}

	/**
	 * 用于获取状态栏的高度。
	 * 
	 * @return 返回状态栏高度的像素值。
	 */
	private int getStatusBarHeight() {
		if (statusBarHeight == 0) {
			try {
				Class<?> c = Class.forName("com.android.internal.R$dimen");
				Object o = c.newInstance();
				Field field = c.getField("status_bar_height");
				int x = (Integer) field.get(o);
				statusBarHeight = getResources().getDimensionPixelSize(x);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusBarHeight;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.ib_close:
				WxFloatWindowManager.removeSmallWindow();
				break;
			case R.id.bt_fz:
				copy("plpz123");
				WxFloatWindowManager.removeSmallWindow();
				break;
		}
	}
	
	/**
	* 实现文本复制功能
	* add by wangqianzhou
	* @param content
	*/
	@SuppressLint("NewApi")
	public static void copy(String content){
//		// 得到剪贴板管理器
//		ClipboardManager cmb = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
//		cmb.setText(content.trim());
		if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB){
			ClipboardManager clipboardManager = (ClipboardManager)MyApplication.getApp().getSystemService(Context.CLIPBOARD_SERVICE);  
			clipboardManager.setPrimaryClip(ClipData.newPlainText(null, content));  
			if (clipboardManager.hasPrimaryClip()){  
			    clipboardManager.getPrimaryClip().getItemAt(0).getText();  
			}  
		}else{
			android.text.ClipboardManager cmb = (android.text.ClipboardManager)MyApplication.getApp().getSystemService(Context.CLIPBOARD_SERVICE);  
			cmb.setText(content.trim());
		}
		
	}
}
