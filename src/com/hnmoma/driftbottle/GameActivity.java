package com.hnmoma.driftbottle;

import com.hnmoma.driftbottle.business.UserManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * 邂逅猜拳
 * @author yangsy
 *
 */
public class GameActivity extends BaseActivity {
	protected static final int REQ_CODE_GAME = 1000; //进入游戏界面，成功后，回调后结束当前页面
	protected static final int REQ_CODE_THROW = 1001;//进入登陆页面，成功后，回调后进入游戏界面
	protected static final int REQ_CODE_GAMEMSG = 1002;//进入登陆页面，成功后，回调后进入游戏消息页面
	private String userId;
	private String visitorId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		init();
		setListener();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==Activity.RESULT_OK){
			switch (requestCode) {
			case REQ_CODE_GAME:
				//定向猜拳后，关闭当前页面
				if(UserManager.getInstance(GameActivity.this).getCurrentUserId().equals(visitorId)&&!TextUtils.isEmpty(userId)){
					finish();
				}
				break;
			case REQ_CODE_THROW:
				Intent intent = new Intent(GameActivity.this, Throw_yxpz.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				//随机挑战，游戏挑战者visitorId和游戏接收者userId可能是空的
				if(TextUtils.isEmpty(visitorId))
					visitorId = UserManager.getInstance(GameActivity.this).getCurrentUserId();
				intent.putExtra("visitUserId", visitorId);	//游戏挑战者
				intent.putExtra("userId", userId); //游戏接收者
				intent.putExtra("isShowTrack", false);
				startActivityForResult(intent,REQ_CODE_GAME);
				break;
			case REQ_CODE_GAMEMSG:
				Intent intent2 = new Intent(GameActivity.this, GameMsgActivity.class);
				intent2.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(intent2);
				break;
			default:
				break;
			}
		}
		
		
	}

	private void init() {
		// TODO Auto-generated method stub
		((TextView)findViewById(R.id.tv_title)).setText(getResources().getString(R.string.game));
		if(getIntent()!=null){
			userId = getIntent().getStringExtra("userId"); //被挑战者
			visitorId = getIntent().getStringExtra("visitUserId");	//挑战者
			if(UserManager.getInstance(this).getCurrentUserId().equals(visitorId)&&!TextUtils.isEmpty(userId)){
				((TextView)findViewById(R.id.tv_title)).setText("猜拳");
			}
		}
	}

	private void setListener() {
		// TODO Auto-generated method stub
		findViewById(R.id.iv_vs).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(UserManager.getInstance(GameActivity.this).getCurrentUser()==null){
					Intent intent = new Intent(GameActivity.this, LoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivityForResult(intent, REQ_CODE_THROW);
					return ;
				}
					
				Intent intent = new Intent(GameActivity.this, Throw_yxpz.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				//随机挑战，游戏挑战者visitorId和游戏接收者userId可能是空的
				intent.putExtra("userId", userId);
				if(TextUtils.isEmpty(visitorId)||visitorId.equalsIgnoreCase(UserManager.getInstance(GameActivity.this).getCurrentUserId()))
					visitorId = UserManager.getInstance(GameActivity.this).getCurrentUserId();
				intent.putExtra("visitUserId", visitorId);
				intent.putExtra("isShowTrack", false);
				startActivityForResult(intent,REQ_CODE_GAME);
			}
		});
		
		findViewById(R.id.ib_rank).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(GameActivity.this, HotTopItemActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("item", 0);
				intent.putExtra("position", 3);
				startActivity(intent);
			}
		});
		
		findViewById(R.id.ib_result).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(UserManager.getInstance(GameActivity.this).getCurrentUser()==null){
					Intent intent = new Intent(GameActivity.this, LoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivityForResult(intent, REQ_CODE_GAMEMSG);
					return ;
				}
				Intent intent = new Intent(GameActivity.this, GameMsgActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(intent);
			}
		});
		findViewById(R.id.btn_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});
	}
}
