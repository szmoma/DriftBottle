package com.hnmoma.driftbottle.fragment;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import android.widget.TextView.BufferType;

import com.hnmoma.driftbottle.FishingBottleActivity;
import com.hnmoma.driftbottle.ImageFrameShowActivity;
import com.hnmoma.driftbottle.LoginActivity;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.SingleImageFrameShowActivity;
import com.hnmoma.driftbottle.TalkDetailActivity;
import com.hnmoma.driftbottle.VzoneActivity;
import com.hnmoma.driftbottle.WebFrameWithCacheActivity;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.AnimRelativeLayout;
import com.hnmoma.driftbottle.model.ActionNumModel;
import com.hnmoma.driftbottle.model.Attachment;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.CommentModel;
import com.hnmoma.driftbottle.model.PickBottleModel;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.SmileUtils;
import com.way.ui.emoji.EmojiTextView;
/**
 * 系统分享瓶
 * @author Administrator
 *
 */
public class Fragment_plpz extends BaseFragment implements OnClickListener{
	
	PickBottleModel model;
	
	BottleModel bottleModel;
	ActionNumModel actionNumModel;
	List<CommentModel>  commentModels;
	
	Button bt_01;
	Button bt_02;
	TextView tv_complain;
	
	ImageView iv_userhead;
	TextView tv_name;
	TextView tv_sf;
	TextView tv_xz;
	TextView tv_gj;
	ImageView iv_bottletype;
	ImageView ivVip;
	
	LinearLayout view_one;
	EmojiTextView view_two;
	
	ImageView iv;
	EmojiTextView tv_desc;
	// 瓶子扔掉动画
	LinearLayout ll_first;
	RelativeLayout rl_second;
	AnimRelativeLayout chuck_bottle_layout;
	AnimationSet set;
	private ImageView chuck_spray1;
	
	long waitTime = 5000L;  
	long touchTime = 0L; 
	
	public static BaseFragment newInstance(PickBottleModel model) {
		Fragment_plpz fragment = new Fragment_plpz();
		Bundle bundle = new Bundle();
		bundle.putSerializable("PickBottleModel", model);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fishing_plpz, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Bundle bundle = getArguments();
		model = (PickBottleModel) bundle.getSerializable("PickBottleModel");
		
		bt_01 = (Button) findViewById(R.id.bt_01);
		bt_02 = (Button) findViewById(R.id.bt_02);
		
		bt_01.setOnClickListener(this);
		bt_02.setOnClickListener(this);
		
		tv_complain = (TextView) findViewById(R.id.tv_complain);
		tv_complain.setVisibility(View.GONE);
		
		view_one = (LinearLayout) findViewById(R.id.view_one);
		view_two = (EmojiTextView) findViewById(R.id.view_two);
		
		
		iv_userhead = (ImageView) findViewById(R.id.iv_userhead);
		iv_userhead.setOnClickListener(this);
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_sf = (TextView) findViewById(R.id.tv_sf);
		tv_xz = (TextView) findViewById(R.id.tv_xz);
		tv_gj = (TextView) findViewById(R.id.tv_gj);
		ivVip = (ImageView) findViewById(R.id.iv_vip);
		
		iv = (ImageView) findViewById(R.id.iv);
		tv_desc = (EmojiTextView) findViewById(R.id.tv_desc);
		
		ll_first = (LinearLayout) findViewById(R.id.ll_first);
		rl_second = (RelativeLayout) findViewById(R.id.rl_second);
		chuck_bottle_layout = (AnimRelativeLayout) findViewById(R.id.chuck_bottle_layout);
		chuck_spray1=(ImageView) findViewById(R.id.chuck_spray1);
		
		if(model.getBottleInfo().getFromOther()==1){
			((TextView)findViewById(R.id.tv_title)).setText("");
		}else{
			((TextView)findViewById(R.id.tv_title)).setText("讨论瓶");
		}
		
		initAnimation();
		initDate();
	}
	
	private void initDate(){
		bottleModel = model.getBottleInfo();
		actionNumModel = model.getActionNum();
		commentModels = model.getCommentModels();
		
		UserInfoModel userInfo = bottleModel.getUserInfo();
		
		String headImg = userInfo.getHeadImg();
		String nickName = userInfo.getNickName();
//		idtype = bottleModel.getUserInfo().getIdentityType();
//		ChatTime = bottleModel.getCreateTime();
//		province = bottleModel.getUserInfo().getProvince();
//		province = TextUtils.isEmpty(province) ? "" : province;
//		city = bottleModel.getUserInfo().getCity();
//		city = TextUtils.isEmpty(city) ? "" : city;
//		cnt = bottleModel.getContent();
//		cnt = cnt == null ? "" : cnt;
		
		imageLoader.displayImage(headImg, iv_userhead, options);
		tv_name.setText(nickName);
		String province = bottleModel.getUserInfo().getProvince();
		province = TextUtils.isEmpty(province) ? "" : province;
		String city = bottleModel.getUserInfo().getCity();
		city = TextUtils.isEmpty(city) ? "" : city;
		
		tv_gj.setText(city);
		
		tv_sf.setText(userInfo.getAge()+"");
		
		// 如果用户是VIP，则添加VIP的图标
		
		if (userInfo.getIsVIP() == 0) { // 不是VIP
			tv_name.setTextColor(getResources().getColor(R.color.username_normal));
			ivVip.setImageDrawable(getResources().getDrawable(R.drawable.ic_vip_not));
		} else {// 是VIP
			tv_name.setTextColor(getResources().getColor(R.color.username_vip));
			ivVip.setImageDrawable(getResources().getDrawable(R.drawable.ic_vip));
		}
		
		String[] identity = new String[2];
		String idtype = userInfo.getIdentityType();
		if (TextUtils.isEmpty(idtype)) {
			identity[0] = "男";
			identity[1] = "m";
		} else {
			identity = getIdentityByCode(idtype);
		}
		
		Drawable drawable;
		if (identity[1].equals("m")) {
			tv_sf.setBackgroundResource(R.drawable.bg_sex_man);
			drawable = getResources().getDrawable(R.drawable.icon_male_32);
		} else {
			tv_sf.setBackgroundResource(R.drawable.bg_sex_female);
			drawable = getResources().getDrawable(R.drawable.icon_female_32);
		}
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv_sf.setCompoundDrawables(drawable, null, null, null);
		tv_sf.setPadding(MoMaUtil.dip2px(act, 5), 0, MoMaUtil.dip2px(act, 5), 0);
		
		//bottle content
		String cnt = bottleModel.getContent();
		cnt = cnt == null ? "" : cnt;
		
		if (bottleModel.getContentType().equals("5000")) {//文字
			view_one.setVisibility(View.GONE);
			view_two.setVisibility(View.VISIBLE);
			
			view_two.setText(SmileUtils.getSmiledText(act, cnt),BufferType.SPANNABLE);
		} else if (bottleModel.getContentType().equals("5001")) {//图片
			view_one.setVisibility(View.VISIBLE);
			view_two.setVisibility(View.GONE);
			if(TextUtils.isEmpty(bottleModel.getShortPic())){
				iv.setVisibility(View.GONE);
			}else{
				iv.setVisibility(View.VISIBLE);
				imageLoader.displayImage(bottleModel.getShortPic(), iv, options);
			}
			
			tv_desc.setText(SmileUtils.getSmiledText(act, cnt),BufferType.SPANNABLE);
			iv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					int num = bottleModel.getPicNum();
					String url = bottleModel.getUrl();
//					String redirectUrl = bottleModel.getRedirectUrl();
					// 是否跳转(0表示不跳转，1表示跳转)
					List<Attachment> subList;
					if (num > 1) {
						subList = bottleModel.getSubList();
						Intent intent = new Intent(act, ImageFrameShowActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("imageUrls", (Serializable) subList);
						startActivity(intent);
					} else {
						Intent intent = new Intent(act, SingleImageFrameShowActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("imageUrl", url);
						startActivity(intent);
					}
				}
			});
		} else if (bottleModel.getContentType().equals("5003")) {//视频
		} else if (bottleModel.getContentType().equals("5004")) {//语音
			
		} else if (bottleModel.getContentType().equals("5005")) {//web
			view_one.setVisibility(View.VISIBLE);
			view_two.setVisibility(View.GONE);
			
			final String redirectUrl = bottleModel.getRedirectUrl();
			imageLoader.displayImage(bottleModel.getShortPic(), iv, options);
			tv_desc.setText(SmileUtils.getSmiledText(getActivity(), cnt),BufferType.SPANNABLE);
			iv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(act, WebFrameWithCacheActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("webUrl", redirectUrl);
					startActivity(intent);
				}
			});
		}
	}
	
	private String[] getIdentityByCode(String code) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("2000", "小萝莉");
		map.put("2001", "花样少女");
		map.put("2002", "知性熟女");
		map.put("2003", "小正太");
		map.put("2004", "魅力少年");
		map.put("2005", "成熟男生");
		map.put("2006", "男");
		map.put("2007", "女");
		String flag = map.get(code);
		map = null;

		String[] strs = new String[2];
		strs[0] = flag;
		// m-lan f-nv
		if (code.equals("2000") || code.equals("2001") || code.equals("2002") || code.equals("2007")) {
			strs[1] = "f";
		} else {
			strs[1] = "m";
		}

		return strs;
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

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.bt_01:
				long currentTime = System.currentTimeMillis();
				 if((currentTime-touchTime)>=waitTime) {  
			           touchTime = currentTime;  
			           ready2Send();
				    }
				break;
			case R.id.bt_02:
				if(model.getBottleInfo().getFromOther()==1){
					Intent reply = new Intent(act, TalkDetailActivity.class);
					reply.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					reply.putExtra("bottleId", model.getBottleInfo().getBottleId());
					startActivity(reply);
					getActivity().finish();
				}else{
					FishingBottleActivity fba = (FishingBottleActivity) act;
					BaseFragment bf = Fragment_plpz_detail.newInstance(model);
					fba.changeContent(model, bf);
				}
				
				break;
			case R.id.iv_userhead:
				if(UserManager.getInstance(act).getCurrentUser() == null) {
					Intent intent = new Intent(act, LoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivity(intent);
				} else {
					Intent intent = new Intent(act, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("userId", bottleModel.getUserInfo().getUserId());
					if(bottleModel.getUserInfo().getUserId().equals(UserManager.getInstance(act).getCurrentUserId())){
						intent.putExtra("identityflag", 0);
						intent.putExtra("visitUserId", bottleModel.getUserInfo().getUserId());	//访问ID
					}else{
						intent.putExtra("identityflag", 1);
						intent.putExtra("visitUserId", UserManager.getInstance(act).getCurrentUserId());	//访问ID
					}
					startActivity(intent);
				}
				break;
		}
	}
	
	public void ready2Send(){
	        ll_first.setVisibility(View.GONE);
	        showDialog("正在密封瓶子...", "密封失败...", 15*1000);
	        rl_second.setVisibility(View.VISIBLE);
	        chuck_bottle_layout.startAnimation(set);
	        
	        MyApplication.getApp().getSpUtil().setSupport(false);
	        MyApplication.getApp().getSpUtil().delBottleComment();
	        if(getActivity()!=null)
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
//				//231*180
				x = (float) (x - width * 0.3);
				y = (float) (y - height * 0.5);
				
//				chuck_spray1.setX(x);
//				chuck_spray1.setY(y);
				
				RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) chuck_spray1.getLayoutParams();
				lp.leftMargin = (int) x; //Your X coordinate
				lp.topMargin = (int) y; //Your Y coordinate
				
				AnimationDrawable ad1 = (AnimationDrawable) chuck_spray1.getDrawable();
				chuck_spray1.setVisibility(View.VISIBLE);
				ad1.start();
				
				MyApplication.getApp().getSpUtil().updateCommentTime(0);
				MyApplication.getApp().getSpUtil().delBottleComment();
				MyApplication.getApp().getSpUtil().setSupport(false);
				
				myHandler.postDelayed(mt, 1000);
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
	
}
