package com.hnmoma.driftbottle;

import org.apache.http.Header;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.AccountInfo;
import com.hnmoma.driftbottle.model.UserAccountInfo;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

/**
 * 我的钱包页面
 * @author yangsy
 *
 */
public class WalletActivity extends BaseActivity {
	private TextView tvTitle;	//标题
	private Button btnBack;	//返回键
	private TextView tvScallop;// 扇贝
	private TextView tvCharm;	//魅力值
	private TextView tvRecharge; //充值
	
	private AccountInfo info;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wallet);
		setupView();
		queryWallet(UserManager.getInstance(this).getCurrentUserId());
		registerListener();
	}
	//初始化view控件
	private void setupView() {
		// TODO Auto-generated method stub
		btnBack = (Button) findViewById(R.id.btn_back);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvScallop = (TextView) findViewById(R.id.tv_scallop);
		tvCharm = (TextView) findViewById(R.id.tv_charm);
		tvRecharge = (TextView) findViewById(R.id.tv_recharge);
		
		tvTitle.setText(getResources().getString(R.string.my_wallet));
		tvRecharge.setText(getResources().getString(R.string.recharge));
	}
	
	/**
	 * 注册控件的监听事件
	 */
	private void registerListener() {
		// TODO Auto-generated method stub
		//返回键
		btnBack.setOnClickListener(new OnClickListener(	) {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		//点击魅力，进入魅力商城
		findViewById(R.id.rl_charm).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				launch(MlscActivity.class, null);
			}
		});
		/**
		 * 进入赏金任务列表
		 */
		findViewById(R.id.rl_task).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				launch(SjrwActivity.class, null);
			}
		});
		/**
		 * 进入VIP介绍页面
		 */
		findViewById(R.id.rl_vip).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				launch(VIPIntroductionActivity.class, null);
			}
		});
		/**
		 * 进入充值页面
		 */
		tvRecharge.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				launch(RechargeActivity.class,null);
			}
		});
		
		
	}
	/**
	 * 显式跳转到页面
	 * @param mClass  指定的页面类
	 */
	private void launch(Class<?> mClass,Bundle extras){
		Intent intent = new Intent(this, mClass);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		if(extras!=null)
			intent.putExtras(extras);
		startActivity(intent);
	}
	
	/**
	 * 查询个人钱包的函数
	 */
	private void queryWallet(String userId){
		JsonObject params = new JsonObject();
		params.addProperty("userId", userId);
		params.addProperty("deviceId", getDeviceId());
		BottleRestClient.post("queryAccount",this, params, new AsyncHttpResponseHandler() {
			
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				showDialog("努力加载...", "加载失败....", 15*1000);
			}
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				// TODO Auto-generated method stub
				String msg = new String(responseBody);
				Gson gson = new Gson();
				UserAccountInfo model = gson.fromJson(msg, UserAccountInfo.class);
				if(model!=null&&"0".equals(model.getCode())){
					info = model.getAccountInfo();
					//刷新UI
					tvCharm.setText("魅力值："+info.getCharm());
					tvScallop.setText("扇贝："+info.getMoney());
					
					//刷新缓存
					MyApplication.getApp().getSpUtil().setMyMoney(info.getMoney());
				}else{
					closeDialog(mpDialog);
					if(model==null){
						showMsg("解析错误");
					}else{
						showMsg(model.getMsg());
					}
				}
			
			}
			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				super.onFinish();
				closeDialog(mpDialog);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				// TODO Auto-generated method stub
				closeDialog(mpDialog);
			}
		});
	}
	
}
