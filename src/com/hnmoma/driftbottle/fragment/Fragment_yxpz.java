package com.hnmoma.driftbottle.fragment;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.Game_Cq_Yz;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.RechargeActivity;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.AnimRelativeLayout;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.custom.CustomDialog;
import com.hnmoma.driftbottle.custom.CustomDialog.CustomDialogClickListener;
import com.hnmoma.driftbottle.model.ActionNumModel;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.CommentModel;
import com.hnmoma.driftbottle.model.GameOpponentModel;
import com.hnmoma.driftbottle.model.PickBottleModel;
import com.hnmoma.driftbottle.model.UserAccountInfo;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

/**
游戏瓶子
 */
public class Fragment_yxpz extends BaseFragment implements OnClickListener{
	
	PickBottleModel model;
	BottleModel bottleModel;
	ActionNumModel actionNumModel;
	List<CommentModel>  commentModels;
	
	Button bt_left;
	Button bt_right;
	
	CircularImage iv_head;
	TextView tv_money;

	// 瓶子扔掉动画
	LinearLayout ll_first;
	RelativeLayout rl_second;
	AnimRelativeLayout chuck_bottle_layout;
	AnimationSet set;
	private ImageView chuck_spray1;
	
	long waitTime = 500L;  
	long touchTime = 0L;

	public static BaseFragment newInstance(PickBottleModel model) {
		Fragment_yxpz fragment = new Fragment_yxpz();
		Bundle bundle = new Bundle();
		bundle.putSerializable("PickBottleModel", model);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fishing_yxpz, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Bundle bundle = getArguments();
		if(bundle!=null)
			model = (PickBottleModel) bundle.getSerializable("PickBottleModel");
		
		bt_left = (Button) findViewById(R.id.bt_left);
		bt_right = (Button) findViewById(R.id.bt_right);
		bt_left.setOnClickListener(this);
		bt_right.setOnClickListener(this);
		
		iv_head = (CircularImage) findViewById(R.id.iv_head);
		tv_money = (TextView) findViewById(R.id.tv_money);

		ll_first = (LinearLayout) findViewById(R.id.ll_first);
		rl_second = (RelativeLayout) findViewById(R.id.rl_second);
		chuck_bottle_layout = (AnimRelativeLayout) findViewById(R.id.chuck_bottle_layout);
		chuck_spray1=(ImageView) findViewById(R.id.chuck_spray1);
		
		initAnimation();
		
		initDate(model);
	}

	private void initDate(PickBottleModel model){
		if(model==null)
			return;
		bottleModel = model.getBottleInfo();
		
		String headImg = null;
		try {
			headImg = bottleModel.getUserInfo().getHeadImg();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		imageLoader.displayImage(headImg, iv_head, options);
		
		tv_money.setText(Html.fromHtml("<font color=#767676>赌注：</font>" + "<font color=#fe7800>" + bottleModel.getMoney() + "</font>" + "<font color=#fe7800>扇贝</font>"));
	}
	
	@Override
	public boolean onBackPressed() {
//		act.finish();
		long currentTime = System.currentTimeMillis();
		 if((currentTime-touchTime)>=waitTime) {  
	           touchTime = currentTime;  
	           ready2Send();
		    }
		return true;
	}
	
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if(bottleModel.getMoney() > money) {
				new CustomDialog().showSelectDialog(getActivity(), "扇贝不足~~请先充值",
						new CustomDialogClickListener() {
							@Override
							public void confirm() {
								Intent intent = new Intent(getActivity(), RechargeActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
								startActivity(intent);
							}

							@Override
							public void cancel() {
							}
						});
			}  else {
				UserInfoModel uif = bottleModel.getUserInfo();
				Intent intent = new Intent(act, Game_Cq_Yz.class);
				GameOpponentModel gom = new GameOpponentModel();
				gom.setHeadImg(uif.getHeadImg());
				gom.setNickName(uif.getNickName());
				gom.setUserId(uif.getUserId());
				gom.setMoney(bottleModel.getMoney());
				gom.setBottleId(bottleModel.getBottleId());
				gom.setContent(bottleModel.getContent());
				intent.putExtra("GameOpponentModel", gom);
				startActivityForResult(intent, 100);
				act.finish();
			}
	};
	};
	
	/*
	 * 查询魅力值和礼券
	 */
	int money;
	private void queryCharmAndMoney() {
		
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(getActivity()).getCurrentUserId());
		jo.addProperty("deviceId", getDeviceId());
		BottleRestClient.post("queryAccount", getActivity(), jo,
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							UserAccountInfo baseModel = gson.fromJson(str, UserAccountInfo.class);
							if (baseModel != null && !TextUtils.isEmpty(baseModel.getCode())) {
								if ("0".equals(baseModel.getCode())) {
									money = baseModel.getAccountInfo().getMoney();
									Message msg = new Message();
									handler.sendMessage(msg);
								}else{
									showMsg(baseModel.getMsg());
								}
							} else {
								 showMsg("查询余额失败");
							}
						} else {
							 showMsg("查询余额失败");
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						// showMsg("查询余额失败");
					}

					@Override
					public void onFinish() {
						super.onFinish();
					}
				});

	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.bt_left:
				long currentTime = System.currentTimeMillis();
				 if((currentTime-touchTime)>=waitTime) {  
			           touchTime = currentTime;  
			           ready2Send();
				    }
				break;
			case R.id.bt_right:
//				FishingBottleActivity fba = (FishingBottleActivity) act;
//				BaseFragment bf = Fragment_yxpz_cq.newInstance(model);
//				fba.changeContent(model, bf);
				// 判断扇贝是否足够
				bt_right.setClickable(false);
				queryCharmAndMoney();
				break;
		}
	}
	
	public void ready2Send(){
        ll_first.setVisibility(View.GONE);
        showDialog("正在密封瓶子...", "密封失败...", 15*1000);
        rl_second.setVisibility(View.VISIBLE);
        chuck_bottle_layout.startAnimation(set);
        
        getActivity().getSupportFragmentManager().popBackStackImmediate();//退栈
}

private void initAnimation() {
	
	//适配
	DisplayMetrics dm = new DisplayMetrics();   
	act.getWindowManager().getDefaultDisplay().getMetrics(dm);   
	final int width = dm.widthPixels;   
	final int height = dm.heightPixels;  
	
	Animation animationR = AnimationUtils.loadAnimation(act, R.anim.bottle_throw1);
	Animation animationT = AnimationUtils.loadAnimation(act, R.anim.bottle_throw2);
	
	set = new AnimationSet(false);
	set.addAnimation(animationR);
	set.addAnimation(animationT);
	
	set.setAnimationListener(new AnimationListener() {
		@Override
		public void onAnimationStart(Animation animation) {
			if (mpDialog != null && mpDialog.isShowing()) {
				mpDialog.cancel();
			}
		}
		@Override
		public void onAnimationRepeat(Animation animation) {
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			
			chuck_bottle_layout.setVisibility(View.GONE);
			
			int [] location = new int [2];  
			chuck_bottle_layout.getLocationOnScreen(location);  
			
			float x = location[0];  
			float y = location[1];
//			//231*180
			x = (float) (x - width * 0.3);
			y = (float) (y - height * 0.5);
			
//			chuck_spray1.setX(x);
//			chuck_spray1.setY(y);
			
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) chuck_spray1.getLayoutParams();
			lp.leftMargin = (int) x; //Your X coordinate
			lp.topMargin = (int) y; //Your Y coordinate
			
			AnimationDrawable ad1 = (AnimationDrawable) chuck_spray1.getDrawable();
			chuck_spray1.setVisibility(View.VISIBLE);
			ad1.start();
			
			myHandler.postDelayed(mt, 450);
		}
	});
}

Handler myHandler = new Handler();
MyThread mt = new MyThread();

class MyThread implements Runnable{
	public void run() {
		chuck_spray1.setVisibility(View.GONE);
		if(mpDialog!=null){
			mpDialog.dismiss();
			mpDialog = null;
		}
		act.finish();
	}
}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		getActivity();
		if(requestCode==100&&resultCode==Activity.RESULT_OK){
			getActivity().finish();
		}
	}
}
