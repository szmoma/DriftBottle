package com.hnmoma.driftbottle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
/*
 * 扔瓶子
 */
public class ThrowActivity extends BaseActivity implements OnClickListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_throw);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ll_main:
				this.finish();
			break;
			case R.id.ll_01://说说瓶子
				// 对本地记录的数据进行判断是否已经超过可捞次数
				Intent intent;
				boolean  canThrow = MyApplication.getApp().getSpUtil().canThrow();
				if (canThrow) {
					intent = new Intent(this, Throw_sspz.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivityForResult(intent, 300);
					this.finish();
				} else {
					showMsg(getResources().getString(R.string.throw_gap_tip));
				}
				break;
			case R.id.ll_02:	//普通瓶
				canThrow = MyApplication.getApp().getSpUtil().canThrow();
				if (canThrow) {
					intent = new Intent(this, Throw_ptpz.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivityForResult(intent, 300);
					this.finish();
				} else {
					showMsg(getResources().getString(R.string.throw_gap_tip));
				}
				break;
			case R.id.ll_03:	//划拳瓶
				intent = new Intent(this, Throw_yxpz.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivityForResult(intent, 300);
				this.finish();
				break;
			case R.id.ll_04://爆破瓶
				canThrow = MyApplication.getApp().getSpUtil().canThrow();
				if (canThrow) {
					intent = new Intent(this, Throw_jfpz.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivityForResult(intent, 300);
					this.finish();
				} else {
					showMsg(getResources().getString(R.string.throw_gap_tip));
				}
				break;
			case R.id.bt_close:
				setContentView(R.layout.fragment_throw);
				break;
		}
	}
}