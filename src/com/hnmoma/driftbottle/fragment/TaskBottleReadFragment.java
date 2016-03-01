package com.hnmoma.driftbottle.fragment;

import java.util.List;
import java.util.Random;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.itfc.FrameShowCallBack;
import com.hnmoma.driftbottle.itfc.TaskCallBack;
import com.hnmoma.driftbottle.model.BottleModel;
import com.way.ui.emoji.EmojiTextView;

/**
 *
 */
public class TaskBottleReadFragment extends BaseFragment {
	
	ImageView iv;
	Button bt;
	EmojiTextView tv_cnt;
	
	BottleModel bottleModel;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("bottleModel", bottleModel);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null){
			bottleModel = (BottleModel) savedInstanceState.getSerializable("bottleModel");
		}
		
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frament_frame_taskbottle, null);
		iv = (ImageView) view.findViewById(R.id.iv);
		bt = (Button) view.findViewById(R.id.bt);
		tv_cnt = (EmojiTextView) view.findViewById(R.id.tv_cnt);
		
		bt.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				bt.setEnabled(false);
				
				//用state来做表示区分，0表示未开始做任务，1表示做了任务没领取奖励，2表示任务完结
				if(bottleModel.getState()==0){
					beginTask();
				}else if(bottleModel.getState()==1){
					getReward();
				}
			}
		});
		
		return view;
	}
	
	TaskCallBack taskCallBack;
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FrameShowCallBack fsc = (FrameShowCallBack) act;
		taskCallBack = (TaskCallBack) act;
		Object [] objects = fsc.onFragmentInit(this);
		bottleModel = (BottleModel) objects[1];
		initData();
	}
	
	public void initData(){
		imageLoader.displayImage(bottleModel.getShortPic(), iv, options);
		tv_cnt.setText(bottleModel.getContent());
		
		if(bottleModel.getState()==0){
			bt.setVisibility(View.VISIBLE);
			bt.setText("开始任务");
			Drawable drawable= getResources().getDrawable(R.drawable.task_start);  
			/// 这一步必须要做,否则不会显示.  
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());  
			bt.setCompoundDrawables(drawable,null,null,null);  
		}else if(bottleModel.getState()==1){
			bt.setVisibility(View.VISIBLE);
			bt.setText("领取奖励");
			Drawable drawable= getResources().getDrawable(R.drawable.task_jl);  
			/// 这一步必须要做,否则不会显示.  
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());  
			bt.setCompoundDrawables(drawable,null,null,null);  
		}else{
			bt.setVisibility(View.GONE);
			tv_cnt.setText("任务已完成");
		}
	}
	
	public void changeTaskState(int state) {
		if(state == 0){
			bt.setText("开始任务");
			Drawable drawable= getResources().getDrawable(R.drawable.task_start);  
			/// 这一步必须要做,否则不会显示.  
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());  
			bt.setCompoundDrawables(drawable,null,null,null);  
		}else if(state == 1){
			bt.setText("领取奖励");
			Drawable drawable= getResources().getDrawable(R.drawable.task_jl);  
			/// 这一步必须要做,否则不会显示.  
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());  
			bt.setCompoundDrawables(drawable,null,null,null);  
		}else{
			bt.setVisibility(View.GONE);
			tv_cnt.setText("任务已完成");
		}
		bottleModel.setState(state);
		bt.setEnabled(true);
	}
	
	private void beginTask(){
		if(bottleModel.getRemark().equals("10100")){
			doSupport();
		}
	}
	
	private void getReward(){
		//领取奖励,更新入库，刷新
		MyApplication.isPending = true;
		MyApplication.getApp().getSpUtil().setOldBottle(10);
		
		taskCallBack.onTaskEnd();
		changeTaskState(2);
	}
	
	private void doSupport(){
		//这里获取渠道标识，只评论相应渠道,如果没有这个市场，就去其他随机市场
		String channelId="";
		int index = -1;
		
		try {
			ApplicationInfo appInfo = act.getPackageManager().getApplicationInfo(act.getPackageName(), PackageManager.GET_META_DATA);
			String str =  appInfo.metaData.getString("UMENG_CHANNEL");
			channelId = str==null?"":str;
		} catch (NameNotFoundException e) {
		}
		
		String packageName = "com.hnmoma.driftbottle";
		PackageManager localPackageManager = act.getPackageManager();
		Intent localIntent1 = new Intent(Intent.ACTION_VIEW);
		Uri localUri1 = Uri.parse("market://details?id=" + packageName);
		Intent localIntent3 = localIntent1.setData(localUri1);
		List localList = localPackageManager.queryIntentActivities(localIntent1, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
		for (int i = 0; i < localList.size(); i++) {
			ResolveInfo localResolveInfo = (ResolveInfo) localList.get(i);
			String str2 = localResolveInfo.activityInfo.name;
			
			if(str2.contains(channelId)){
				index = i;
			}
		}
		
		//为0时直接返回
		if(localList.size() <= 0){
			return;
		}else if(localList.size() == 1){
			index = 0;
		}else{//大于2时随机
			if(index == -1){
				Random r = new Random();
				index = r.nextInt(localList.size()-1);
			}
		}
		
		
		ResolveInfo localResolveInfo = (ResolveInfo) localList.get(index);
		String str2 = localResolveInfo.activityInfo.packageName;
		String str3 = localResolveInfo.activityInfo.name;
		
		Uri uri = Uri.parse("market://details?id=" + packageName);
		localIntent3.setData(uri);
		localIntent3.setClassName(str2, str3);
		localIntent3.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivityForResult(localIntent3, 100);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 100){
			changeTaskState(1);
		}
	}

	@Override
	public boolean onBackPressed() {
		// TODO Auto-generated method stub
		return false;
	}
}