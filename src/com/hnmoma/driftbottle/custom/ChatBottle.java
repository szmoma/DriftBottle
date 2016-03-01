package com.hnmoma.driftbottle.custom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.SingleImageFrameShowActivity;
import com.hnmoma.driftbottle.VzoneActivity;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.itfc.BottleVoicePlayClickListener;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.way.ui.emoji.EmojiTextView;
/**
 * 在聊天列表显示的瓶子
 * @author bobyoung
 *
 */
public class ChatBottle extends LinearLayout {
	
	Activity context;
	EMMessage message;
	
	CircularImage iv_head;
	ImageView iv;
	TextView tv_name;
	TextView tv_sf;
	TextView tv_dq;
	EmojiTextView tv_cnt;
	TextView tv_da;
	TextView tv_time;
	ImageView iv_vip;
	
	LinearLayout rl_c;
	
	TextView tv_sc;
	TextView tv_rd;
	
	SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
	ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	String headImg = null;
	String nickName = null;
	String idtype = null;
	String province = null;
	String city = null;
	String cnt = null;
	
//	BottleMsg bottleMsg;
//	Stranger stranger;
	BottleModel bottleModel;
	private Date ChatTime;
	
	public ChatBottle(Context context) {
		super(context);
	}

	public ChatBottle(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ChatBottle(Activity context, EMMessage message) {
		super(context);
		this.context = context;
		this.message = message;
		this.setPadding(0, 20, 0, 10);
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)         //加载开始默认的图片      
        .showImageForEmptyUri(R.drawable.defalutimg)     //url爲空會显示该图片，自己放在drawable里面的
        .showImageOnFail(R.drawable.defalutimg)               //加载图片出现问题，会显示该图片
        .cacheInMemory(true)                                               //缓存用
        .cacheOnDisc(true)                                                    //缓存用
        .considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
        .build();
		
		initView();
		
	}

	private void initView(){
		String strBottle = message.getStringAttribute(MyConstants.MESSAGE_ATTR_BOTTLE, "");
		if(TextUtils.isEmpty(strBottle))
			return ;
		Gson gson = new Gson();
		try {
			bottleModel = gson.fromJson(strBottle, BottleModel.class);
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			try {
				JSONObject obj = new JSONObject(strBottle);
				if(bottleModel==null)
					bottleModel = new BottleModel();
				bottleModel.setBottleId(obj.getString("bottleId"));
				bottleModel.setBottleIdPk(obj.getLong("bottleIdPk"));
				bottleModel.setContent(obj.getString("content"));
				bottleModel.setRemark(obj.getString("remark"));
				bottleModel.setContentType(obj.getString("contentType"));
				bottleModel.setBottleType(obj.getString("bottleType"));
				bottleModel.setUbId(obj.getString("ubId"));
				bottleModel.setBottleSort(obj.getString("bottleSort"));
				bottleModel.setSysType(obj.getString("sysType"));
				bottleModel.setCreateTime(new Date(obj.getString("createTime"))); //createTime:May 21, 2015 10:51:36
				bottleModel.setHasAnswer(obj.getBoolean("hasAnswer"));
				bottleModel.setState(obj.getInt("state"));
				bottleModel.setIsBack(obj.getInt("isBack"));
				bottleModel.setFromOther(obj.getInt("fromOther"));
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		ChatTime = bottleModel.getCreateTime();
		if("5001".equals(bottleModel.getContentType())){	//图片
			View v = LayoutInflater.from(context).inflate(R.layout.frament_frame_image_ext, this);
			initBottleHead(v);
			iv = (ImageView) v.findViewById(R.id.iv);
			tv_cnt = (EmojiTextView) v.findViewById(R.id.tv_cnt);
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
		}else if("5002".equals(bottleModel.getContentType())){	//贺卡
			View v = LayoutInflater.from(context).inflate(R.layout.frament_frame_image_ext, this);
			initBottleHead(v);
			tv_sf = (TextView) findViewById(R.id.tv_sf);
			iv = (ImageView) findViewById(R.id.iv);
			tv_cnt = (EmojiTextView) findViewById(R.id.tv_cnt);
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
		}else if("5004".equals(bottleModel.getContentType())){	//语音
			View v =LayoutInflater.from(context).inflate(R.layout.frament_frame_voice_ext, this);
			initBottleHead(v);
			iv = (ImageView) findViewById(R.id.iv);
			tv_cnt = (EmojiTextView) findViewById(R.id.tv_cnt);
			if(!TextUtils.isEmpty(bottleModel.getContent())){
				tv_cnt.setVisibility(View.VISIBLE);
				tv_cnt.setText(bottleModel.getContent());
			}
			
			ImageButton ib_01 =  (ImageButton) v.findViewById(R.id.ib_01);
			ProgressBar spinner = (ProgressBar)v.findViewById(R.id.loading);
			ib_01.setOnClickListener(new BottleVoicePlayClickListener(context, message, bottleModel, ib_01, spinner));
			
		}else{
			View v = LayoutInflater.from(context).inflate(R.layout.frament_frame_text_ext, this);
			initBottleHead(v);
			tv_cnt = (EmojiTextView) findViewById(R.id.tv_cnt);
			if(TextUtils.isEmpty(bottleModel.getContent())) {
				tv_cnt.setText("");
			} else {
				tv_cnt.setText(bottleModel.getContent());
			}
			
			TextView bottletype = (TextView) findViewById(R.id.bottletype);
			if ("4004".equals(bottleModel.getBottleType())) {
				bottletype.setVisibility(View.VISIBLE);
				bottletype.setText("定向瓶");
			}else if("4005".equals(bottleModel.getBottleType())){
				bottletype.setVisibility(View.VISIBLE);
				if(message.getBooleanAttribute(MyConstants.MESSAGE_ATTR_ISSAYHELLO, false)){
					bottletype.setText("打招呼");
				}else{
					bottletype.setText("答谢瓶");
				}
				
			}
		}
	}
	
	private void initBottleHead(View view) {
		iv_head = (CircularImage) view.findViewById(R.id.iv_userhead);
		tv_name = (TextView) view.findViewById(R.id.tv_name);
		tv_sf = (TextView) view.findViewById(R.id.tv_sf);
		tv_dq = (TextView) view.findViewById(R.id.tv_dq);
		tv_time = (TextView) view.findViewById(R.id.tv_time);
		iv_vip = (ImageView) view.findViewById(R.id.iv_vip);
		UserInfoModel userInfoModel = bottleModel.getUserInfo();
		if(userInfoModel != null){
			headImg = userInfoModel.getHeadImg();
			nickName = userInfoModel.getNickName();
			idtype = userInfoModel.getIdentityType();
			province = userInfoModel.getProvince();
			province = TextUtils.isEmpty(province)?"":province;
			city = userInfoModel.getCity();
			city = TextUtils.isEmpty(city)?"":city;
			
			
			imageLoader.displayImage(headImg, iv_head, options);
			tv_name.setText(nickName);
			
			// 如果用户是VIP，则添加VIP的图标
			if (userInfoModel.getIsVIP() == 0) { // 不是VIP
				tv_name.setTextColor(getResources().getColor(R.color.bottlecontent_username));
				iv_vip.setBackgroundResource(R.drawable.ic_vip_not);
			} else {// 是VIP
				tv_name.setTextColor(getResources().getColor(R.color.username_vip));
				iv_vip.setBackgroundResource(R.drawable.ic_vip);
			}
			
			String [] identity = new String[2];
			if(TextUtils.isEmpty(idtype)){
				identity[0] = "男";
				identity[1] = "m";
			}else{
				identity = getIdentityByCode(idtype);
			}
			
			tv_sf.setText(userInfoModel.getAge() + "");
			Drawable drawable;
			if(identity[1].equals("m")){
				drawable= getResources().getDrawable(R.drawable.icon_male_32);
				tv_sf.setBackgroundResource(R.drawable.manbg);
			}else{
				drawable= getResources().getDrawable(R.drawable.icon_female_32);
				tv_sf.setBackgroundResource(R.drawable.womanbg);
			}
			
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			tv_sf.setCompoundDrawables(drawable,null,null,null);
			tv_sf.setPadding(MoMaUtil.dip2px(getContext(), 5), 0, MoMaUtil.dip2px(getContext(), 5), 0);
			//time
//			Date updateTime = bottleModel.getCreateTime();
			if(!TextUtils.isEmpty(userInfoModel.getConstell())){
				tv_time.setText(userInfoModel.getConstell());
			}else{
				tv_time.setText("");
			}
			if(TextUtils.isEmpty(province)){
				if(TextUtils.isEmpty(city)){
					tv_dq.setVisibility(View.GONE);
				}else{
					tv_dq.setText(city);
				}
			}else{
				if(TextUtils.isEmpty(city)){
					tv_dq.setText(province);
				}else{
					tv_dq.setText(province+" "+city);
				}
			}
		}
		
		iv_head.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(bottleModel!=null && bottleModel.getUserInfo()!=null){
					String userId = bottleModel.getUserInfo().getUserId();
					String visitUserId = UserManager.getInstance(context).getCurrentUserId();
//					
					if(userId.equals(visitUserId)){
						Intent intent = new Intent(context, VzoneActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("userId", visitUserId);
						intent.putExtra("identityflag", 0);
						intent.putExtra("visitUserId", userId);
						context.startActivity(intent);
					}else{
						Intent intent = new Intent(context, VzoneActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("identityflag", 1);
						intent.putExtra("userId", userId);
						intent.putExtra("visitUserId", visitUserId);
						context.startActivity(intent);
					}
				}
			}
		});
	}
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
	
	
}
