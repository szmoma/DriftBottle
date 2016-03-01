package com.hnmoma.driftbottle.fragment;

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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.hnmoma.driftbottle.LoginActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.VzoneActivity;
import com.hnmoma.driftbottle.VzoneTousuActivity;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.AnimRelativeLayout;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.model.ActionNumModel;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.CommentModel;
import com.hnmoma.driftbottle.model.PickBottleModel;
import com.hnmoma.driftbottle.model.TousuModel;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.SmileUtils;

/**
 * 即焚瓶子
 */
public class Fragment_jfpz extends BaseFragment implements OnClickListener{
	
	PickBottleModel model;
	BottleModel bottleModel;
	ActionNumModel actionNumModel;
	List<CommentModel>  commentModels;
	
	//第一层
	View view_one;
	CircularImage iv_head;
	
	//第2层
	View view_two;
	FrameLayout cnt;
	FrameLayout cover;
	FrameLayout view_tw;
	ImageView iv_pic;
	TextView tv_desc;
	TextView view_wen;
	
	//第3层
	TextView tv_count;
	
	//head
	ImageView iv_userhead;
	TextView tv_name;
	TextView tv_sf;
	TextView tv_xz;
	TextView tv_gj;
	TextView tv_baoza;
	ImageView iv_vip;
	
	Button bt_left;
	Button bt_right;
	TextView tv_complain;
	
	boolean visiable;
	Animation ani;
	
	// 瓶子扔掉动画
	LinearLayout ll_first;
	RelativeLayout rl_second;
	AnimRelativeLayout chuck_bottle_layout;
	AnimationSet set;
	private ImageView chuck_spray1;
	
	long waitTime = 5000L;  
	long touchTime = 0L;

	
	public static BaseFragment newInstance(PickBottleModel model) {
		Fragment_jfpz fragment = new Fragment_jfpz();
		Bundle bundle = new Bundle();
		bundle.putSerializable("PickBottleModel", model);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fishing_jfpz, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Bundle bundle = getArguments();
		model = (PickBottleModel) bundle.getSerializable("PickBottleModel");
		
		//第1层
		view_one = findViewById(R.id.view_one);
		iv_head = (CircularImage) findViewById(R.id.iv_head);
		tv_name = (TextView) findViewById(R.id.tv_name);
		iv_vip = (ImageView) findViewById(R.id.iv_vip);
		
		//第2层
		view_two = findViewById(R.id.view_two);
		cnt = (FrameLayout) findViewById(R.id.cnt);
		cover = (FrameLayout) findViewById(R.id.cover);
		view_tw = (FrameLayout) findViewById(R.id.view_tw);
		iv_pic = (ImageView) findViewById(R.id.iv_pic);
		tv_desc = (TextView) findViewById(R.id.tv_desc);
		view_wen = (TextView) findViewById(R.id.view_wen);
		
		//第3层
		tv_count = (TextView) findViewById(R.id.tv_count);
		
		//head
		iv_userhead = (ImageView) findViewById(R.id.iv_userhead);
		tv_sf = (TextView) findViewById(R.id.tv_sf);
		tv_xz = (TextView) findViewById(R.id.tv_xz);
		tv_gj = (TextView) findViewById(R.id.tv_gj);
		tv_baoza = (TextView) findViewById(R.id.tv_baoza);
		
		bt_left = (Button) findViewById(R.id.bt_left);
		bt_right = (Button) findViewById(R.id.bt_right);
		bt_left.setOnClickListener(this);
		bt_right.setOnClickListener(this);
		tv_complain = (TextView) findViewById(R.id.tv_complain);
		tv_complain.setOnClickListener(this);
		
		ani = AnimationUtils.loadAnimation(act, R.anim.jfpz);
		
		ll_first = (LinearLayout) findViewById(R.id.ll_first);
		rl_second = (RelativeLayout) findViewById(R.id.rl_second);
		chuck_bottle_layout = (AnimRelativeLayout) findViewById(R.id.chuck_bottle_layout);
		chuck_spray1=(ImageView) findViewById(R.id.chuck_spray1);
		initAnimation();
		
		initData();
	}
	
	private void initData(){
		bottleModel = model.getBottleInfo();
		
		String headImg = bottleModel.getUserInfo().getHeadImg();
		String nickName = bottleModel.getUserInfo().getNickName();
		
		imageLoader.displayImage(headImg, iv_userhead, options);
//		tv_name.setText(nickName);
		String province = bottleModel.getUserInfo().getProvince();
		province = TextUtils.isEmpty(province) ? "" : province;
		String city = bottleModel.getUserInfo().getCity();
		city = TextUtils.isEmpty(city) ? "" : city;
		
		tv_gj.setText(province+"  "+city);
		
		imageLoader.displayImage(headImg, iv_head, options);
		tv_name.setText(nickName);
		if(bottleModel.getUserInfo().getIsVIP() == 0) {
			tv_name.setTextColor(getResources().getColor(R.color.bottlehead_name));
			iv_vip.setBackgroundResource(R.drawable.ic_vip_not);
		} else {
			tv_name.setTextColor(getResources().getColor(R.color.username_vip));
			iv_vip.setBackgroundResource(R.drawable.ic_vip);
		}
		
		tv_xz.setText(bottleModel.getUserInfo().getConstell());
		String idtype = bottleModel.getUserInfo().getIdentityType();
		String [] its = new String[2];
		if(TextUtils.isEmpty(idtype)){
			its[0] = "男";
			its[1] = "m";
		}else{
			its = getIdentityByCode(idtype);
		}
		
		if(bottleModel.getUserInfo().getAge() > 0) {
			tv_sf.setText(bottleModel.getUserInfo().getAge() + "");
		} else {
			tv_sf.setText("0");
		}

		Drawable drawable;
		if(its[1].equals("m")){
			drawable= getResources().getDrawable(R.drawable.icon_male_32);
			tv_sf.setBackgroundResource(R.drawable.manbg);
		}else{
			drawable= getResources().getDrawable(R.drawable.icon_female_32);
			tv_sf.setBackgroundResource(R.drawable.womanbg);
		}
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv_sf.setCompoundDrawables(drawable,null,null,null);
		tv_sf.setPadding(MoMaUtil.dip2px(act, 5), 0, MoMaUtil.dip2px(act, 5), 0);
		tv_baoza.setText(Html.fromHtml("瓶子打开后 <font color=#fb9b2a>3</font> 秒爆炸"));
	}

	/*
	 * 获取性别
	 */
    private String[] getIdentityByCode(String code){
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
		strs[0]=flag;
		//m-lan f-nv
		if(code.equals("2000") || code.equals("2001") || code.equals("2002") || code.equals("2007")){
			strs[1]="f";
		}else{
			strs[1]="m";
		}
		
		return strs;
	}
    
	@Override
	public boolean onBackPressed() {
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
			case R.id.bt_left:
				long currentTime = System.currentTimeMillis();
				if((currentTime-touchTime)>=waitTime) {  
			           touchTime = currentTime;  
			           ready2Send();
				    }
				break;
			case R.id.bt_right:
				Object flag = view.getTag();
				if(flag==null){
					view_one.setVisibility(View.GONE);
					view_two.setVisibility(View.VISIBLE);
					//bottle content
					String cnt = bottleModel.getContent();
					cnt = cnt == null ? "" : cnt;
					if (bottleModel.getContentType().equals("5001")) {// 图片
						view_tw.setVisibility(View.VISIBLE);
						view_wen.setVisibility(View.GONE);
	
						imageLoader.displayImage(bottleModel.getShortPic(), iv_pic, options);
						tv_desc.setText(SmileUtils.getSmiledText(act, cnt),BufferType.SPANNABLE);
					} else {
						view_tw.setVisibility(View.GONE);
						view_wen.setVisibility(View.VISIBLE);
						view_wen.setText(cnt);
					}
					
					countDown.post(ct);
				}else{
					//打开空间
					Intent intent = new Intent(act, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("identityflag", 1);
					intent.putExtra("userId", bottleModel.getUserInfo().getUserId());
					intent.putExtra("visitUserId", UserManager.getInstance(act).getCurrentUserId());
					startActivity(intent);
				}
				break;
				
			case R.id.tv_complain:
				if(UserManager.getInstance(act).getCurrentUser() == null) {
					Intent intent = new Intent(act, LoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivity(intent);
				} else {
					Intent jubao = new Intent(act, VzoneTousuActivity.class);
					jubao.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					TousuModel tm = new TousuModel();
					tm.setId(bottleModel.getBottleId());
					tm.setUserId(UserManager.getInstance(act).getCurrentUserId());
					tm.setbUserId(bottleModel.getUserInfo().getUserId());
					tm.setReportType(3);
					tm.setContent(bottleModel.getContent());
					jubao.putExtra("tousuModel", tm);
					startActivity(jubao);
				}
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
	
	
	//即焚瓶子倒计时
	int count = 3;
	Handler countDown = new Handler();
	CountThread ct = new CountThread();
	
	class CountThread implements Runnable{
		public void run() {
			if(count< 1){
				stopTimer();
			} else {
				tv_count.setText(""+count);
				tv_count.startAnimation(ani);
				tv_count.setVisibility(View.VISIBLE);
				count--;
				countDown.postDelayed(ct, 1000);
			}
		}
	}
	
	private void stopTimer() {
		bt_right.setText("逛空间");
		bt_right.setTag(1);
		
		tv_count.setVisibility(View.GONE);
		cover.setVisibility(View.VISIBLE);
	}
}
