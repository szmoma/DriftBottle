package com.hnmoma.driftbottle.custom;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easemob.chat.EMMessage;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.SingleImageFrameShowActivity;
import com.hnmoma.driftbottle.itfc.BottleVoicePlayClickListener;
import com.hnmoma.driftbottle.model.BottleModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CommonBottle extends LinearLayout{
	
	protected Activity context;
	protected EMMessage message;
	
	DisplayImageOptions options;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	
	BottleModel bottleModel;
	
	public CommonBottle(Activity context) {
		super(context);
		this.context = context;
	}

	public CommonBottle(Activity context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public CommonBottle(Activity context,BottleModel bottleModel, EMMessage message) {
		super(context);
		this.context = context;
		this.message = message;
		this.bottleModel = bottleModel;
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.i_loading_photo)         //加载开始默认的图片      
        .showImageForEmptyUri(R.drawable.i_loading_photo)     //url爲空會显示该图片，自己放在drawable里面的
        .showImageOnFail(R.drawable.i_loading_photo)                //加载图片出现问题，会显示该图片
        .cacheInMemory(true)                                               //缓存用
        .cacheOnDisc(true)                                                    //缓存用
        .considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
        .build();
		
		initView();
	}
	
	
	ImageView iv;
	TextView tv_cnt;
	TextView tv_da;
	TextView tv_sf;
	LinearLayout view_bottle_head;
	private void initView(){
		if(bottleModel.getContentType().equals("5001")){
			LayoutInflater.from(context).inflate(R.layout.frament_frame_image, this);
			view_bottle_head = (LinearLayout) findViewById(R.id.view_bottle_head);
			tv_sf = (TextView) findViewById(R.id.tv_sf);
			iv = (ImageView) findViewById(R.id.iv);
			tv_cnt = (TextView) findViewById(R.id.tv_cnt);
			imageLoader.displayImage(bottleModel.getShortPic(), iv, options);
			tv_cnt.setText(bottleModel.getContent());
			
			iv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(context, SingleImageFrameShowActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("imageUrl", bottleModel.getUrl());
					context.startActivity(intent);
				}
			});
		}else if(bottleModel.getContentType().equals("5002")){
			LayoutInflater.from(context).inflate(R.layout.frament_frame_image, this);
			view_bottle_head = (LinearLayout) findViewById(R.id.view_bottle_head);
			tv_sf = (TextView) findViewById(R.id.tv_sf);
			iv = (ImageView) findViewById(R.id.iv);
			tv_cnt = (TextView) findViewById(R.id.tv_cnt);
			imageLoader.displayImage(bottleModel.getShortPic(), iv, options);
			tv_cnt.setText(bottleModel.getContent());
			
			iv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(context, SingleImageFrameShowActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("imageUrl", bottleModel.getUrl());
					context.startActivity(intent);
				}
			});
		}else if(bottleModel.getContentType().equals("5004")){
			LayoutInflater.from(context).inflate(R.layout.frament_frame_voice, this);
			view_bottle_head = (LinearLayout) findViewById(R.id.view_bottle_head);
			tv_sf = (TextView) findViewById(R.id.tv_sf);
			iv = (ImageView) findViewById(R.id.iv);
			tv_cnt = (TextView) findViewById(R.id.tv_cnt);
			if(!TextUtils.isEmpty(bottleModel.getContent())){
				tv_cnt.setVisibility(View.VISIBLE);
				tv_cnt.setText(bottleModel.getContent());
			}
			
			ImageButton ib_01 =  (ImageButton) findViewById(R.id.ib_01);
			ProgressBar spinner = (ProgressBar)findViewById(R.id.loading);
			ib_01.setOnClickListener(new BottleVoicePlayClickListener(context, message, bottleModel, ib_01, spinner));
			
		}else{
			LayoutInflater.from(context).inflate(R.layout.frament_frame_text, this);
			view_bottle_head = (LinearLayout) findViewById(R.id.view_bottle_head);
			tv_sf = (TextView) findViewById(R.id.tv_sf);
			tv_cnt = (TextView) findViewById(R.id.tv_cnt);
			tv_cnt.setText(bottleModel.getContent());
			
			TextView bottletype = (TextView) findViewById(R.id.bottletype);
			if (bottleModel.getBottleType().equals("4004")) {
				bottletype.setVisibility(View.VISIBLE);
				bottletype.setText("定向瓶");
			}else if(bottleModel.getBottleType().equals("4005")){
				bottletype.setVisibility(View.VISIBLE);
				bottletype.setText("答谢瓶");
			}
		}
	}
}