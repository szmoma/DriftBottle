package com.hnmoma.driftbottle.custom;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.hnmoma.driftbottle.MyApplication;

public class WxFloatWindowManager {
	/**
	 * 小悬浮窗View的实例
	 */
	private static WxFloatMenu smallWindow;

	/**
	 * 小悬浮窗View的参数
	 */
	private static LayoutParams smallWindowParams;

	/**
	 * 大悬浮窗View的参数
	 */
	private static LayoutParams bigWindowParams;

	/**
	 * 用于控制在屏幕上添加或移除悬浮窗
	 */
	private static WindowManager mWindowManager;

	/**
	 * 用于获取手机可用内存
	 */
//	private static ActivityManager mActivityManager;

	/**
	 * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。
	 * 
	 * @param context
	 *            必须为应用程序的Context.
	 */
	public static void createSmallWindow() {
		Context ctx = MyApplication.getApp();
		WindowManager windowManager = getWindowManager(ctx);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (smallWindow == null) {
			smallWindow = new WxFloatMenu(ctx);
			if (smallWindowParams == null) {
				smallWindowParams = new LayoutParams();
				smallWindowParams.type = LayoutParams.TYPE_PHONE;
				smallWindowParams.format = PixelFormat.RGBA_8888;
				smallWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
				smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
				// smallWindowParams.width = FloatWindowSmallView.viewWidth;
				smallWindowParams.width = screenWidth;
				smallWindowParams.height = WxFloatMenu.viewHeight;
				smallWindowParams.x = screenWidth;
				smallWindowParams.y = screenHeight - WxFloatMenu.viewHeight;
			}
			smallWindow.setWindowManagerParams(windowManager, smallWindowParams);
			windowManager.addView(smallWindow, smallWindowParams);
		}
	}

	private static WindowManager getWindowManager(Context ctx) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		}
		return mWindowManager;
	}

	public static boolean isWindowShowing() {
		return smallWindow == null ? false : true;
	}

	public static void removeSmallWindow() {
		if (smallWindow != null) {
			WindowManager windowManager = getWindowManager(MyApplication.getApp());
			windowManager.removeView(smallWindow);
			smallWindow = null;
			mWindowManager = null;
			smallWindowParams = null;
		}
	}
}