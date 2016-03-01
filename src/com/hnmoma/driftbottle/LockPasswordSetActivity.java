package com.hnmoma.driftbottle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;
/**
 * 手势图案设置--开启
 * @author Administrator
 *
 */
public class LockPasswordSetActivity extends BaseActivity {

	ToggleButton tb_islocked;
	private boolean isLocked;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lockpassword);
		
		tb_islocked = (ToggleButton) findViewById(R.id.tb_islocked);
		isLocked = MyApplication.getApp().getSpUtil().isScreenLocked();
		tb_islocked.setChecked(isLocked);
		tb_islocked.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				isLocked = isChecked;
				if(!isChecked){
					MyApplication.getApp().getSpUtil().setScreenLocked(false);
				}
			}
		});
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_back:
			finish();
			break;

		case R.id.rl_resetlock:
			if(isLocked) {
				Intent intents = new Intent(this, SetLockScreenActivity.class);
				intents.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intents.putExtra("resetlock", true);
				startActivityForResult(intents,200);
			} else {
				showMsg("请先开启手势密码!");
			}
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
		if(arg0 == 200) {
			if(arg1 == Activity.RESULT_OK) {
				isLocked = true;
				tb_islocked.setChecked(true);
				MyApplication.getApp().getSpUtil().setScreenLocked(true);
				String pwd = arg2.getStringExtra("password");
				MyApplication.getApp().getSpUtil().setLockScreenPwd(pwd);
			}else{
				isLocked = false;
				tb_islocked.setChecked(false);
				MyApplication.getApp().getSpUtil().setScreenLocked(false);
			}
		}
	}
	
}
