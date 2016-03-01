package com.hnmoma.driftbottle.fragment;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.google.gson.Gson;
import com.hnmoma.driftbottle.ChatActivity;
import com.hnmoma.driftbottle.ImageFrameShowActivity;
import com.hnmoma.driftbottle.LoginActivity;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.SingleImageFrameShowActivity;
import com.hnmoma.driftbottle.VzoneActivity;
import com.hnmoma.driftbottle.VzoneTousuActivity;
import com.hnmoma.driftbottle.WebFrameWithCacheActivity;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.AnimRelativeLayout;
import com.hnmoma.driftbottle.itfc.BottleVoicePlayClickListener;
import com.hnmoma.driftbottle.model.ActionNumModel;
import com.hnmoma.driftbottle.model.Attachment;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.CommentModel;
import com.hnmoma.driftbottle.model.PickBottleModel;
import com.hnmoma.driftbottle.model.Stranger;
import com.hnmoma.driftbottle.model.TousuModel;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.hnmoma.driftbottle.util.SmileUtils;
import com.way.ui.emoji.EmojiTextView;

/**
 * 聊天瓶子
 */
public class Fragment_ltpz extends BaseFragment implements OnClickListener{
	
	PickBottleModel model;
	
	BottleModel bottleModel;
	ActionNumModel actionNumModel;
	List<CommentModel>  commentModels;
	
	Button bt_left;
	Button bt_right;
	TextView tv_complain;
	
	ImageView iv_userhead;
	TextView tv_name;
	TextView tv_sf;
	TextView tv_xz;
	TextView tv_gj;
	ImageView iv_bottletype;
	
	View view_one;
	EmojiTextView view_two;
	
	ImageView iv;
	EmojiTextView tv_desc;
	ImageView ivVip;
	
	LinearLayout ll_voice;
	ImageButton ib_play;
	TextView tv_cd;
	ProgressBar loading;

	// 瓶子扔掉动画
	LinearLayout ll_first;
	RelativeLayout rl_second;
	AnimRelativeLayout chuck_bottle_layout;
	AnimationSet set;
	private ImageView chuck_spray1;
	
	long waitTime = 5000L;  
	long touchTime = 0L; 
	
	public static BaseFragment newInstance(PickBottleModel model) {
		Fragment_ltpz fragment = new Fragment_ltpz();
		Bundle bundle = new Bundle();
		bundle.putSerializable("PickBottleModel", model);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fishing_ltpz, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Bundle bundle = getArguments();
		model = (PickBottleModel) bundle.getSerializable("PickBottleModel");
		
		view_one = findViewById(R.id.view_one);
		view_two = (EmojiTextView) findViewById(R.id.view_two);
		
		ll_voice = (LinearLayout) findViewById(R.id.ll_voice);
		ib_play = (ImageButton) findViewById(R.id.ib_play);
		tv_cd = (TextView) findViewById(R.id.tv_cd);
		loading = (ProgressBar) findViewById(R.id.loading);
		
		bt_left = (Button) findViewById(R.id.bt_left);
		bt_right = (Button) findViewById(R.id.bt_right);
		bt_left.setOnClickListener(this);
		bt_right.setOnClickListener(this);
		
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
		tv_complain = (TextView) findViewById(R.id.tv_complain);
		tv_complain.setOnClickListener(this);
		((TextView)findViewById(R.id.tv_title)).setText(getActivity().getResources().getString(R.string.general_bottle));;
		
		initAnimation();
		initDate();
	}
	
	private void initDate(){
		bottleModel = model.getBottleInfo();
		
		UserInfoModel userInfo = bottleModel.getUserInfo();
		String headImg = userInfo.getHeadImg();
		String nickName = userInfo.getNickName();
		
		imageLoader.displayImage(headImg, iv_userhead, options);
		tv_name.setText(nickName);
		String province = bottleModel.getUserInfo().getProvince();
		province = TextUtils.isEmpty(province) ? "" : province;
		String city = bottleModel.getUserInfo().getCity();
		city = TextUtils.isEmpty(city) ? "" : city;
		
		tv_gj.setText(city);
		
		tv_sf.setText(userInfo.getAge()+"");
		
		if(bottleModel.getFromOther() == 1 ) {
			bt_right.setTextColor(getResources().getColor(R.color.ltpz_notalk));
			if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
				bt_right.setAlpha(0.7f);
			else{
				bt_right.setTextColor(Color.GRAY);
			}
		}
		
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
			
			ll_voice.setVisibility(View.GONE);
			loading.setVisibility(View.GONE);
			
			imageLoader.displayImage(bottleModel.getShortPic(), iv, options);
			if(!TextUtils.isEmpty(bottleModel.getContent())){
				tv_desc.setVisibility(View.VISIBLE);
				tv_desc.setText(SmileUtils.getSmiledText(act, cnt),BufferType.SPANNABLE);
			}
			iv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					int num = bottleModel.getPicNum();
					String url = bottleModel.getUrl();
					// 是否跳转(0表示不跳转，1表示跳转)
					List<Attachment> subList;
					if(num > 1) {
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
			if(!TextUtils.isEmpty(bottleModel.getContent())){
				tv_desc.setVisibility(View.VISIBLE);
				tv_desc.setText(SmileUtils.getSmiledText(act, cnt),BufferType.SPANNABLE);
			}
			
			tv_cd.setText(String.valueOf(bottleModel.getRemark()));
			
			iv.setImageResource(R.drawable.card_voice_bg);
			
			EMMessage msg = EMMessage.createSendMessage(EMMessage.Type.TXT);
			msg.setMsgId("10000");
			
			ll_voice.setOnClickListener(new BottleVoicePlayClickListener(act, msg, bottleModel, ib_play, loading));
		} else if (bottleModel.getContentType().equals("5005")) {//web
			view_one.setVisibility(View.VISIBLE);
			view_two.setVisibility(View.GONE);
			
			ll_voice.setVisibility(View.GONE);
			loading.setVisibility(View.GONE);
			
			final String redirectUrl = bottleModel.getRedirectUrl();
			imageLoader.displayImage(bottleModel.getShortPic(), iv, options);
			if(!TextUtils.isEmpty(bottleModel.getContent())){
				tv_desc.setVisibility(View.VISIBLE);
				tv_desc.setText(SmileUtils.getSmiledText(act, cnt),BufferType.SPANNABLE);
			}
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
			case R.id.bt_left:
				long currentTime = System.currentTimeMillis();
			   	 if((currentTime-touchTime)>=waitTime) {  
			           touchTime = currentTime;  
			           ready2Send();
				    }
				break;
			case R.id.iv_userhead:
				if(UserManager.getInstance(act).getCurrentUser() == null) {
					Intent intent = new Intent(act, LoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivity(intent);
				} else {
					if(bottleModel.getUserInfo().getUserId().equals(UserManager.getInstance(act).getCurrentUserId())){
						Intent intent = new Intent(act, VzoneActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("userId", bottleModel.getUserInfo().getUserId());
						intent.putExtra("identityflag", 0);
						intent.putExtra("visitUserId", bottleModel.getUserInfo().getUserId());	//访问ID
						startActivity(intent);
					}else{
						Intent intent = new Intent(act, VzoneActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("identityflag", 1);
						intent.putExtra("userId", bottleModel.getUserInfo().getUserId());	//被访问者的ID
//						intent.putExtra("visitUserId", userId);
						intent.putExtra("visitUserId", UserManager.getInstance(act).getCurrentUserId());	//访问ID
						startActivity(intent);
					}
				}
				break;
			case R.id.bt_right:
				if(bottleModel.getFromOther() == 1 ) {
					showMsg("瓶子已被其他人回复");
					break;
				}
				// 保存瓶子到环信
				keepUserBottle();
				
				insertOrUpdateStranger(bottleModel.getUserInfo());
				
				// 进入聊天页面
				Intent mChatIntent = new Intent(act, ChatActivity.class);
				mChatIntent.putExtra("userId", bottleModel.getUserInfo().getUserId());
				mChatIntent.putExtra("nickName", bottleModel.getUserInfo().getNickName());
				mChatIntent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(mChatIntent);
				getActivity().finish();
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
	/**
	 * 如果本地数据库存在该用户信息，则更新用户信息；如果用户不存在本地数据库，则插入数据库
	 * @param userinfo
	 */
	private void insertOrUpdateStranger(UserInfoModel userinfo) {
		// TODO Auto-generated method stub
		Stranger stranger =MyApplication.getApp().getDaoSession().getStrangerDao().load(userinfo.getUserId());
		if(stranger==null){
			stranger = new Stranger();
			stranger.setCity(userinfo.getCity());
			stranger.setProvince(userinfo.getProvince());
			stranger.setUserId(userinfo.getUserId());
			stranger.setHeadImg(userinfo.getHeadImg());
			try {
				stranger.setIdentityType(userinfo.getIdentityType());
			} catch (Exception e) {
				// TODO: handle exception
			}
			stranger.setNickName(userinfo.getNickName());
			stranger.setIsVIP(userinfo.getIsVIP());
			stranger.setState(1);
			MyApplication.getApp().getDaoSession().getStrangerDao().insertOrReplace(stranger);
		}else{
			boolean isChange = false;
			if(stranger.getState()!=1){
				stranger.setState(1);
				isChange = true;
			}
			if(!TextUtils.isEmpty(userinfo.getHeadImg())&&!userinfo.getHeadImg().equals(stranger.getHeadImg())){
				stranger.setHeadImg(userinfo.getHeadImg());
				isChange = true;
			}
			if(isChange)
				MyApplication.getApp().getDaoSession().getStrangerDao().insertOrReplace(stranger);
		}
	}

	/**
	 * 先保存配套的用户对象
	 * 再保存瓶子对象
	 */
	private void keepUserBottle(){
		//注册消息
		EMMessage msg  = EMMessage.createSendMessage(EMMessage.Type.TXT);
//		if ("5001".equals(bottleModel.getContentType())) {
//			msg = EMMessage.createSendMessage(EMMessage.Type.IMAGE);
//		} else if("5004".equals(bottleModel.getContentType())) {
//			msg = EMMessage.createSendMessage(EMMessage.Type.VOICE);
//		}  else {
//			msg = EMMessage.createSendMessage(EMMessage.Type.TXT);
//		}
		
		msg.setAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 1);
		if(!TextUtils.isEmpty(bottleModel.getUserInfo().getHeadImg()))  {
			msg.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, bottleModel.getUserInfo().getHeadImg());
		}
		
		Gson gson = new Gson();
		String bottle = gson.toJson(bottleModel, BottleModel.class);
		msg.setAttribute(MyConstants.MESSAGE_ATTR_BOTTLE, bottle);
		
		TextMessageBody body = new TextMessageBody(bottleModel.getContent());
		msg.addBody(body);
		
//		msg.setTo(UserManager.getInstance(this).getCurrentUserId());
//		msg.setFrom(bottleModel.getUserInfo().getUserId());
		msg.setReceipt(bottleModel.getUserInfo().getUserId());
		msg.setMsgTime(System.currentTimeMillis());
		msg.setMsgId(getMsgId());
		
		sendMsgInBackground(msg);
	}
	
	public synchronized static String getMsgId() {
		StringBuilder builder = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		builder.append(sdf.format(new Date()));
		return builder.toString();
	}
	
	/**
	 * 这里瓶子内容可能会发送失败，后续讨论怎么处理
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 */
	public void sendMsgInBackground(EMMessage message) {
		// 调用sdk发送异步发送方法
		EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

			@Override
			public void onSuccess() {
			}

			@Override
			public void onError(int code, String error) {
			}

			@Override
			public void onProgress(int progress, String status) {
			}
		});

	}
	
	// 扔掉瓶子的动画
	public void ready2Send(){
		ll_first.setVisibility(View.GONE);
        showDialog("正在密封瓶子...", "密封失败...", 15*1000);
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