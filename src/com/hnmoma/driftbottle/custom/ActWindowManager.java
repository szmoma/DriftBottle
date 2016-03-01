//package com.hnmoma.driftbottle.custom;
//
//import android.graphics.PixelFormat;
//import android.view.Gravity;
//import android.view.WindowManager;
//import android.view.WindowManager.LayoutParams;
//
//import com.hnmoma.driftbottle.BaseActivity;
//import com.hnmoma.driftbottle.itfc.FloatMenuCallBack;
//import com.hnmoma.driftbottle.util.MyConstants;
//
//public class ActWindowManager {
//	/**
//	 * 小悬浮窗View的实例
//	 */
//	private static ActFloatMenu floatMenu;
//	/**
//	 * 用于控制在屏幕上添加或移除悬浮窗
//	 */
//	private static WindowManager actWindowManager;
//	private static LayoutParams actWindowManagerParams;;
//
//	public static void createActFloatMenu(BaseActivity ba) {
//		WindowManager windowManager = getActWindowManager(ba);
//		if (floatMenu == null) {
//			floatMenu = new ActFloatMenu(ba);
////			floatMenu.setFloatMenuCallBack((FloatMenuCallBack) ba);
//			
//			if (actWindowManagerParams == null) {
//				actWindowManagerParams = new LayoutParams();
//				actWindowManagerParams.format = PixelFormat.RGBA_8888;	// 背景透明
//				actWindowManagerParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//				// 调整悬浮窗口至左上角，便于调整坐标
//				actWindowManagerParams.gravity = Gravity.LEFT | Gravity.TOP; 
//				// 以屏幕左上角为原点，设置x、y初始值
//				actWindowManagerParams.x = 0;
//				actWindowManagerParams.y = MyConstants.XIAOBA_Y;
//				// 设置悬浮窗口长宽数据
//				actWindowManagerParams.width = LayoutParams.WRAP_CONTENT;
//				actWindowManagerParams.height = LayoutParams.WRAP_CONTENT;
//			}
//			floatMenu.setWindowManagerParams(windowManager, actWindowManagerParams);
//			windowManager.addView(floatMenu, actWindowManagerParams);
//		}
//	}
//
//	private static WindowManager getActWindowManager(BaseActivity ba) {
//		if (actWindowManager == null) {
//			actWindowManager = (WindowManager) ba.getWindowManager();
//		}
//		return actWindowManager;
//	}
//
//	public static boolean isWindowShowing() {
//		return floatMenu == null ? false : true;
//	}
//
//	public static void removeSmallWindow(BaseActivity ba) {
//		if (floatMenu != null) {
//			WindowManager windowManager = getActWindowManager(ba);
//			windowManager.removeView(floatMenu);
//			floatMenu = null;
//			actWindowManager = null;
//			actWindowManagerParams = null;
//		}
//	}
//	
//	public static void showMsgQiPao(String[] msgs) {
//		if (floatMenu != null) {
//			floatMenu.showMsgQiPao(msgs);
//		}
//	}
//	
//	public static void newMsgChange(int msgCount) {
//		if (floatMenu != null) {
//			floatMenu.newMsgChange(msgCount);
//		}
//	}
//	
//	public static void cancelMenu() {
//		if (floatMenu != null) {
//			floatMenu.cancelMenu();
//		}
//	}
//	
//	public static boolean isMenuShowing() {
//		if (floatMenu != null) {
//			return floatMenu.isMenuShowing();
//		}else{
//			return false;
//		}
//	}
//}