package com.hnmoma.driftbottle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * VIP介绍
 * @author yangsy
 *
 */
public class VIPIntroductionActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vip_introduce);
		init();
		setListentr();
	}

	private void init() {
		// TODO Auto-generated method stub
		((TextView)findViewById(R.id.tv_title)).setText(getResources().getString(R.string.vip_introduce));
	}

	private void setListentr() {
		// TODO Auto-generated method stub
		findViewById(R.id.btn_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		findViewById(R.id.tv_vip_buy).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(VIPIntroductionActivity.this, VIPBuyActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(intent);
			}
		});
		
	}
	
	
}
