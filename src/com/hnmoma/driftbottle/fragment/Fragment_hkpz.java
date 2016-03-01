package com.hnmoma.driftbottle.fragment;

import java.lang.reflect.Field;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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

import com.hnmoma.driftbottle.FishingBottleActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.WebFrameWithCacheActivity;
import com.hnmoma.driftbottle.custom.AnimRelativeLayout;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.PickBottleModel;
/**
 * 贺卡瓶子
 * @author Administrator
 *
 */
public class Fragment_hkpz extends BaseFragment implements OnClickListener{

	BottleModel bottleModel;
	PickBottleModel model;
	
	TextView tv_cnt;
	ImageView iv_pic;
	
	Button bt_01;
	Button bt_02;
	
	// 瓶子扔掉动画
	LinearLayout ll_first;
	RelativeLayout rl_second;
	AnimRelativeLayout chuck_bottle_layout;
	AnimationSet set;
	private ImageView chuck_spray1;
	
	 //点击back，回退有效时间
	long waitTime = 5000L; 
	long touchTime = 0L;
	
	public static BaseFragment newInstance(PickBottleModel model) {
		Fragment_hkpz fragment = new Fragment_hkpz();
		Bundle bundle = new Bundle();
		bundle.putSerializable("PickBottleModel", model);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fishing_hkpz, container, false);
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
		
		tv_cnt = (TextView) findViewById(R.id.tv_cnt);
		iv_pic = (ImageView) findViewById(R.id.iv_pic);
		
		ll_first = (LinearLayout) findViewById(R.id.ll_first);
		rl_second = (RelativeLayout) findViewById(R.id.rl_second);
		chuck_bottle_layout = (AnimRelativeLayout) findViewById(R.id.chuck_bottle_layout);
		chuck_spray1=(ImageView) findViewById(R.id.chuck_spray1);
		initAnimation();
		
		initDate(model);
	}
	
	private void initDate(PickBottleModel model){
		bottleModel = model.getBottleInfo();
		
		//bottle content
		String cnt = bottleModel.getContent();
		cnt = cnt == null ? "" : cnt;
		
		final String redirectUrl = bottleModel.getRedirectUrl();
		imageLoader.displayImage(bottleModel.getShortPic(), iv_pic, options);
		tv_cnt.setText(cnt);
		iv_pic.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(act, WebFrameWithCacheActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("webUrl", redirectUrl);
				startActivity(intent);
			}
		});
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
//				Intent intent = new Intent(act, Game_Cq_Activity.class);
//				intent.putExtra("array01", array01);
//				intent.putExtra("array02", array02);
//				intent.putExtra("flag", flag);
//				intent.putExtra("money", bottleModel.getMoney());
//				
//				intent.putExtra("PickBottleModel", model);
//				startActivity(intent);
				if(bottleModel.getIsRedirect() == 0) {
					FishingBottleActivity fba = (FishingBottleActivity) act;
					BaseFragment bf = Fragment_plpz_detail.newInstance(model);
					fba.changeContent(model, bf);
				} else {
					String redirectUrl = bottleModel.getRedirectUrl();
					Intent intent = new Intent(act, WebFrameWithCacheActivity.class);
					intent.putExtra("webUrl", redirectUrl);
					intent.putExtra("PickBottleModel", model);
					startActivity(intent);
				}
//				act.finish();
				break;
		}
	}
	
	public void ready2Send(){
        ll_first.setVisibility(View.GONE);
        showDialog("正在密封瓶子...", "密封失败", 15*1000);
        rl_second.setVisibility(View.VISIBLE);
        chuck_bottle_layout.startAnimation(set);
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
}

