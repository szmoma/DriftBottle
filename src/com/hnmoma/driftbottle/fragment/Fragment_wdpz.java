package com.hnmoma.driftbottle.fragment;

import java.lang.reflect.Field;
import java.util.List;

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
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.custom.AnimRelativeLayout;
import com.hnmoma.driftbottle.model.ActionNumModel;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.CommentModel;
import com.hnmoma.driftbottle.model.PickBottleModel;

/**
 * 问答瓶子
 * 
 */
public class Fragment_wdpz extends BaseFragment implements OnClickListener{
	
	PickBottleModel model;
	BottleModel bottleModel;
	ActionNumModel actionNumModel;
	List<CommentModel>  commentModels;
	
	View view_one;
	View view_two;
	
	Button bt_01;
	Button bt_02;
	
	ImageView iv_pic;
	TextView tv_cnt;
	
	// 瓶子扔掉动画
	LinearLayout ll_first;
	RelativeLayout rl_second;
	AnimRelativeLayout chuck_bottle_layout;
	AnimationSet set;
	private ImageView chuck_spray1;
	
	long waitTime = 5000L;  
	long touchTime = 0L; 

	public static BaseFragment newInstance(PickBottleModel model) {
		Fragment_wdpz fragment = new Fragment_wdpz();
		Bundle bundle = new Bundle();
		bundle.putSerializable("PickBottleModel", model);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fishing_wdpz, container, false);
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
		
		iv_pic = (ImageView) findViewById(R.id.iv_pic);
		tv_cnt = (TextView) findViewById(R.id.tv_cnt);
		ll_first = (LinearLayout) findViewById(R.id.ll_first);
		rl_second = (RelativeLayout) findViewById(R.id.rl_second);
		chuck_bottle_layout = (AnimRelativeLayout) findViewById(R.id.chuck_bottle_layout);
		chuck_spray1=(ImageView) findViewById(R.id.chuck_spray1);
		
		initAnimation();
		initDate(model);
	}
	
	private void initDate(PickBottleModel model){
		ActionNumModel actionNumModel;
		List<CommentModel>  commentModels;
		
		bottleModel = model.getBottleInfo();
		actionNumModel = model.getActionNum();
		commentModels = model.getCommentModels();
		
		//bottle content
		String cnt = bottleModel.getContent();
		cnt = cnt == null ? "" : cnt;
		tv_cnt.setText(cnt);
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
				FishingBottleActivity fba = (FishingBottleActivity) act;
				BaseFragment bf = Fragment_wdpz_detail.newInstance(model);
				fba.changeContent(model, bf);
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
				MyApplication.getApp().getSpUtil().updateCommentTime(0);
				MyApplication.getApp().getSpUtil().delBottleComment();
				MyApplication.getApp().getSpUtil().setSupport(false);
				act.finish();
			}
		}
		
}
