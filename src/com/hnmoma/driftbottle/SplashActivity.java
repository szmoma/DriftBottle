	package com.hnmoma.driftbottle;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hnmoma.driftbottle.model.ScreenInfo;

public class SplashActivity extends BaseActivity {
	
	SplashTask splashTask;
	
	ImageView iv_bg;// background
	ImageView iv_below;
	TextView tv_online;
	RelativeLayout rl_channel;
	RelativeLayout rl_bottom;
	GifImageView iv_xiaoba;//动图
	AnimationDrawable ad;
	
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    /***
	     * 首发闪屏页面和正常的闪屏页面，不能同时存在
	     */
	    /***************开启首发闪屏页面*********************/
//    	setContentView(R.layout.splash_new);
	    /******************结束首发闪屏页面*****************************/
	    
	    /*******************开启正常的闪屏页面*************************/
    	setContentView(R.layout.splash);
    	iv_xiaoba = (GifImageView) findViewById(R.id.iv_xiaoba);
    	iv_bg = (ImageView) findViewById(R.id.iv_bg);
    	iv_below = (ImageView) findViewById(R.id.iv_below);
    	tv_online = (TextView) findViewById(R.id.tv_online);
//    	tv_mm = (TextView) findViewById(R.id.tv_mm);
    	rl_channel = (RelativeLayout) findViewById(R.id.rl_channel);
    	rl_bottom = (RelativeLayout) findViewById(R.id.rl_bottom);
    	
    	ScreenInfo mScreenInfo = MyApplication.getApp().getSpUtil().getSplashData();
    	if(mScreenInfo.getManNum() > 0) {
    		tv_online.setText("当前在线小鲜肉  " + mScreenInfo.getManNum());
    	} else {
    		rl_bottom.setVisibility(View.GONE);
    	}
//    	if(mScreenInfo.getWomanNum() > 0) {
//    		tv_mm.setText("当前在线: 鲜肉MM " +mScreenInfo.getWomanNum() +"+");
//    	}
    	Date start = mScreenInfo.getStartTime();
    	Date end = mScreenInfo.getEndTime();
    	Date now = new Date();
    	boolean isShow = false;
    	if(start.before(now) && end.after(now)) {
    		isShow = true;
    	}
    	
    	if(isShow) {
    		rl_channel.setVisibility(View.GONE);
    	} 
    	File file = new File(MyApplication.mAppPath + "/splash/" +"splashbg.png");
    	if(file.exists() && isShow) {
    		Bitmap bmp= BitmapFactory.decodeFile(file.getAbsolutePath());
    		iv_bg.setImageBitmap(bmp);
    	} 
    	file = new File(MyApplication.mAppPath + "/splash/" +"splashdesc.png");
    	if(file.exists() && isShow) {
    		Bitmap bmp= BitmapFactory.decodeFile(file.getAbsolutePath());
    		iv_below.setImageBitmap(bmp);
    	}
    	
    	file = new File(MyApplication.mAppPath + "/splash/" +"splash.gif");
    	if(file.exists() && isShow) {
			try {
				GifDrawable gifDrawable = new GifDrawable(file);
				iv_xiaoba.setImageDrawable(gifDrawable);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	} else {
    		iv_xiaoba.setImageResource(R.drawable.xiaoba);
    		ad = (AnimationDrawable) iv_xiaoba.getDrawable();
    		ad.start();
    	}
    	/**************************正常闪屏页面结束************************************/
    	
    	splashTask = new SplashTask();
    	splashTask.execute();
	}
	
	long startTime;
	class SplashTask extends AsyncTask<Void, Void, Void> {

		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected Void doInBackground(Void... params) {
			startTime = System.currentTimeMillis();
			//TODO
			
			MyApplication.deviceId = getDeviceId();
			MyApplication.channel = getChannel();
			
			long now = System.currentTimeMillis();
			if(now - startTime < 5000){
				try {
		    		Thread.sleep(3000 - (now - startTime));
				} catch (Exception e) {
				}
			}
			return null;
		}

		protected void onPostExecute(Void params) {
			if(ad != null) {
				ad.stop();
				iv_xiaoba.clearAnimation();
				ad = null;
			}
			
			boolean isLocked = MyApplication.getApp().getSpUtil().isScreenLocked();
			if(isLocked) {
				Intent intent = new Intent();
				intent.setClass(SplashActivity.this, LockScreenLoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("fromSplash", true);
				startActivity(intent);
			}  else{
				Intent intent = new Intent();
				if(MyApplication.getApp().getSysSpUtil().isFirstTime())
					intent.setClass(SplashActivity.this, GuideActivity.class);
				else
					intent.setClass(SplashActivity.this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(intent);
			}
			finish();
			
			super.onPostExecute(params);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
//		closeScreenMonitor();
	}
	
	@Override
	public void onBackPressed() {
		//屏蔽back
	}
	
//	//检测版本，低版本要强制登录
//	private void checkVersion(){
//		//强制登出
//		int logoutVerson = MyApplication.getApp().getSysSpUtil().getLogoutVerson();
//		if(UserManager.getInstance(this).getCurrentUser()!=null && logoutVerson != 18){
//			UserManager.getInstance(getApplicationContext()).logout();
//			MyApplication.getApp().mSpUtil = null;
//			MyApplication.getApp().getSysSpUtil().setLogoutVerson(18);
//		}
//	}
}