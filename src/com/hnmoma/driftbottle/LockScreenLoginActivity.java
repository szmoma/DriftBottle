package com.hnmoma.driftbottle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.hnmoma.driftbottle.custom.LocusPassWordView;
import com.hnmoma.driftbottle.custom.LocusPassWordView.OnCompleteListener;


public class LockScreenLoginActivity extends Activity {
	private LocusPassWordView lpwv;
	private Toast toast;
	int errorNum;
	boolean fromSplash = false;
	private TextView tvForgetPwd;
	
	private long touchTime = 0; 
	
	private final long waitTime = 1500;  
	
	private void showToast(CharSequence message) {
		if (null == toast) {
			toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
			// toast.setGravity(Gravity.CENTER, 0, 0);
		} else {
			toast.setText(message);
		}

		toast.show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		errorNum = 0;
		fromSplash = getIntent().getBooleanExtra("fromSplash", false);
		setContentView(R.layout.activity_lockscreen_login);
		setUpView();
		setLinstener();

	}
	
	private void setUpView() {
		// TODO Auto-generated method stub
		 tvForgetPwd = (TextView) findViewById(R.id.tvNoSetPassword);
		 lpwv = (LocusPassWordView) this.findViewById(R.id.mLocusPassWordView);
		 
		 ((TextView) findViewById(R.id.tv_title)).setText(getResources().getString(R.string.tip_gesture_local_head));
	}

	private void setLinstener() {
		// TODO Auto-generated method stub
		lpwv.setOnCompleteListener(new OnCompleteListener() {
			@Override
			public void onComplete(String mPassword) {
				++errorNum;
				
				// 如果密码正确,则进入主页面。
				if (lpwv.verifyPassword(mPassword)) {
					if(fromSplash) {
						Intent intent = new Intent();
						intent.setClass(LockScreenLoginActivity.this, MainActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						startActivity(intent);
					}
					LockScreenLoginActivity.this.finish();
				} else {
					if(errorNum > 4) {
						//退出登录
						showToast("密码输入错误，请重新登录");
						errorNum = 0;
						launch(MainActivity.class);
					} else {
						showToast("密码输错"+errorNum+"次,请重新输入");
					}
					lpwv.clearPassword();
				}
			}
		});
		
		tvForgetPwd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				launch(LoginActivity.class);
			}
		});
	}
	/**
	 * 启动到登陆页面
	 */
	public void launch(Class<?> mClass){
		Intent intent = new Intent(this,mClass);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivity(intent);
		clearData();
		finish();
	}
	/**
	 * 清除设置文件中的内容
	 */
	private void clearData() {
		// TODO Auto-generated method stub
		MyApplication.getApp().logout();
		lpwv.clearPassword();
	}
	

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	public void onBackPressed() {
		long currentTime = System.currentTimeMillis();  
	    if((currentTime-touchTime)>=waitTime) {  
	    	Toast.makeText(this, getResources().getString(R.string.click_exit_agin), Toast.LENGTH_SHORT).show();
	        touchTime = currentTime;  
	    }else {  
	        finish();  
	    }  
	}

}
