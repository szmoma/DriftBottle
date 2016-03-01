package com.hnmoma.driftbottle;

import java.lang.reflect.Type;
import java.util.Date;

import org.apache.http.Header;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.DregeModel;
import com.hnmoma.driftbottle.model.PickBottleModel;
import com.hnmoma.driftbottle.model.PickOtherBottleModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class OtherBeachActivity extends BaseActivity {

	ImageView iv_lang;
	ImageView iv_shui;
	
	ImageView iv_fishingrob;
	TextView tv_beach_name;
	TextView tv_number;
	
	String beachHost;
	int netNum = 0;
	int isVip;
	PickOtherBottleModel pickOtherBottleModel;
	
	boolean isLaunch; //控制是否已经启动了FishingBottle页面
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}

	private void initView() {
		beachHost =  getIntent().getStringExtra("beachHost");
		setContentView(R.layout.activity_otherbeach);
		tv_beach_name = (TextView) findViewById(R.id.tv_beach_name);
//		anim_fishingrob = (AnimRelativeLayout) findViewById(R.id.chuck_bottle_layout);
		iv_fishingrob = (ImageView) findViewById(R.id.iv_fishingrob);
		tv_number = (TextView) findViewById(R.id.tv_number);
		tv_beach_name.setText(getIntent().getStringExtra("nickName"));
		
		iv_lang = (ImageView) findViewById(R.id.iv_lang);
		iv_shui = (ImageView) findViewById(R.id.iv_shui);
		
		initAnimation();
	}
	
	private void initAnimation() {
		// TODO Auto-generated method stub
		Animation anim_lang = AnimationUtils.loadAnimation(this, R.anim.anim_lang);
		Animation anim_shui = AnimationUtils.loadAnimation(this, R.anim.anim_shui);
		iv_lang.startAnimation(anim_lang);
		iv_shui.startAnimation(anim_shui);
	}

	//查询瓶子数量
	private void initData() {
		
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		
		BottleRestClient.post("queryNetNum", this, jo, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				
				String str = new String(responseBody);
				if (!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					DregeModel model = gson.fromJson(str, DregeModel.class);
					if (model != null && !TextUtils.isEmpty(model.getCode())) {
						if ("0".equals(model.getCode())) {
							startNext = true;
							isVip = model.getIsVIP();
							if(isVip == 0) {
								netNum = model.getNetNum();
								tv_number.setText(netNum+"");
							} else {
								tv_number.setText("VIP");
							}
						} 
					} 
				}
			}
		
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
			}
		});
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_dredgeup:// 捞瓶子
			if(isVip == 1 || netNum > 0) {
				showFishingRob();
			} else {
				showMsg("您暂时无法捞瓶子");
			}
			break;

		case R.id.bt_back:
			onBackPressed();
			break;
		}
	}
	
	boolean startNext = false;// 是否可以开始下一次, true 表示可以
	private void showFishingRob() {
		if(!startNext) {
			return;
		}
		iv_fishingrob.setImageResource(R.drawable.fishingrod);
		startNext = false;
		fishBottle();
		Animation  moveToMiddle = AnimationUtils.loadAnimation(this, R.anim.anim_move_middle);
		//final Animation  rorate = AnimationUtils.loadAnimation(this, R.anim.anim_rorate_bottle);
		final Animation  moveToLeft = AnimationUtils.loadAnimation(this, R.anim.anim_move_left);
        final Animation  moveX = AnimationUtils.loadAnimation(this, R.anim.anim_move_x);

        iv_fishingrob.setAnimation(moveToMiddle);
        moveToMiddle.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				if(iv_fishingrob!=null){
					iv_fishingrob.setImageResource(R.drawable.fishingrod_move);
					iv_fishingrob.setAnimation(moveToLeft);
				}
			}
		});
        
        

        
        moveToLeft.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				if(iv_fishingrob!=null)
					iv_fishingrob.setAnimation(moveX);
			}
		});
        
        moveX.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			
			@Override
			public void onAnimationRepeat(Animation anim) {}	
			@Override
			public void onAnimationEnd(Animation animation) {
				if(pickOtherBottleModel != null && "0".equals(pickOtherBottleModel.getCode())) {
					if(isLaunch)
						return ;
					Intent intent = new Intent(OtherBeachActivity.this, FishingBottleActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("model", formatTranslate(pickOtherBottleModel));
					startActivity(intent);
					isLaunch = true;
				} else {
					showMsg("此地没瓶子可捡了");
				}
				startNext = true;
				if(iv_fishingrob!=null){
					iv_fishingrob.clearAnimation();
					iv_fishingrob.setVisibility(View.GONE);
				}
			}
		});
        if(iv_fishingrob!=null)
        	iv_fishingrob.setVisibility(View.VISIBLE);
		
	}
	
	private PickBottleModel formatTranslate(PickOtherBottleModel model) {//TODO 检查判断
		PickBottleModel bottleModel = new PickBottleModel();
		bottleModel.setCode(model.getCode());
		bottleModel.setMsg(model.getMsg());
		if(model.getBottleInfo() != null) {
			bottleModel.getBottleInfo().setIsBack(model.getBottleInfo().getIsBack());
			bottleModel.getBottleInfo().setBottleId(model.getBottleInfo().getBottleId());
			bottleModel.getBottleInfo().setCreateTime(new Date(model.getBottleInfo().getCreateTime()));
			bottleModel.getBottleInfo().setContent(model.getBottleInfo().getContent());
			bottleModel.getBottleInfo().setContentType(model.getBottleInfo().getContentType());
			bottleModel.getBottleInfo().setBottleType(model.getBottleInfo().getBottleType());
			bottleModel.getBottleInfo().setRemark(model.getBottleInfo().getRemark());
			bottleModel.getBottleInfo().setUrl(model.getBottleInfo().getUrl());
			bottleModel.getBottleInfo().setShortPic(model.getBottleInfo().getShortPic());
			bottleModel.getBottleInfo().setBottleTime(model.getBottleInfo().getBottleTime());
			bottleModel.getBottleInfo().setUserInfo(model.getBottleInfo().getUserInfo());
		}
		bottleModel.getBottleInfo().setFromOther(1);//来自别人的沙滩
		return bottleModel;
	}

	/**
	 *捞瓶子
	 */
	boolean GetDoneBottleInfo;
	private void fishBottle() {
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("bUserId", beachHost);
		jo.addProperty("version", getVersionCode());
		jo.addProperty("come", "6000");
		
		BottleRestClient.post("getOtherBottle", this, jo, new AsyncHttpResponseHandler() {
			
			@Override
			public void onStart() {
				super.onStart();
				GetDoneBottleInfo = false;
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				GetDoneBottleInfo = true;
			}
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

				String str = new String(responseBody);

				if (!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					Type type = new TypeToken<PickOtherBottleModel>() {}.getType();
					pickOtherBottleModel = gson.fromJson(str, type);
					if (pickOtherBottleModel != null && "0".equals(pickOtherBottleModel.getCode())) {
						GetDoneBottleInfo = true;
						isLaunch = false;
						if(isVip == 0) {
							netNum--; 
						    tv_number.setText(String.valueOf(netNum));
						}
					} else {
						if(iv_fishingrob!=null)
							iv_fishingrob.clearAnimation();
						showMsg("此地没瓶子可捡了");
					}
				} else {
					
					if(iv_fishingrob!=null)
						iv_fishingrob.clearAnimation();
					showMsg("此地没瓶子可捡了");
				}
			
			}
		
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				if(iv_fishingrob!=null)
					iv_fishingrob.clearAnimation();
				showMsg("捞瓶子失败");
			}
		});
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(iv_lang!=null){
			iv_lang.clearAnimation();
			iv_lang = null;
		}
		if(iv_shui!=null){
			iv_shui.clearAnimation();
			iv_shui = null;
		}
		if(iv_fishingrob!=null){
			iv_fishingrob.clearAnimation();
			iv_fishingrob = null;
		}
			
		
		
	}
	
	@Override
	public void onBackPressed() {
		if (mContent != null) {
			if (mContent.onBackPressed()) {
				mContent = null;
			}
		} else{
			finish(); 
		}
	}
	
}
