package com.hnmoma.driftbottle;

import org.apache.http.Header;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.model.User;
import com.hnmoma.driftbottle.model.UserDataModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 个人中心
 * @author Administrator
 *
 */
public class PersonCenterActivity extends BaseActivity {
	private static final int REQ_CODE_GIFT = 100;	//礼物
	private static final int REQ_CODE_ALBUM = 101;	//相册
	private static final int REQ_CODE_VISITOR = 102;	//访客
	private static final int REQ_CODE_WALLET = 107;	//钱包
	private final static int REQ_CODE_VZONE = 108;	//主页：空间
	private static final int REQ_CODE_USERINFO = 109;//我的资料
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	protected DisplayImageOptions options;
	
	private TextView tvTitle;	//标题
	private CircularImage ivHead;//头像
	private TextView tvUserName;//姓名
	private EditText tvSign;//签名
	private String userId;
	private ImageView ivVip;	//vip
	
	private ChangeUserInfoBroadcastReceiver cgeReceiver;
	private boolean isRealTime;//是否去过实时的用户信息
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_person_center);
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)         //加载开始默认的图片      
        .showImageForEmptyUri(R.drawable.defalutimg)     //url爲空會显示该图片，自己放在drawable里面的
        .showImageOnFail(R.drawable.defalutimg)                //加载图片出现问题，会显示该图片
        .cacheInMemory(true)                                               //缓存用
        .cacheOnDisc(true)                                                    //缓存用
        .considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
        .build();
		
		setupView();
		if(UserManager.getInstance(this).getCurrentUser()!=null){
			getUserInfo(UserManager.getInstance(this).getCurrentUser().getUserId());
		}else{
			refreshUI(null);
		}
		
		registerListener();
		
		// 用户信息改变广播
		cgeReceiver = new ChangeUserInfoBroadcastReceiver();
		IntentFilter cgeFilter = new IntentFilter(MyConstants.USERINFOCHANGE_BROADCAST_ACTION);
		cgeFilter.setPriority(3);
		registerReceiver(cgeReceiver, cgeFilter);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Intent intent = null;
		if(resultCode==Activity.RESULT_OK){
			switch (requestCode) {
			case REQ_CODE_GIFT:
				intent = new Intent(PersonCenterActivity.this, GiftWallActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", UserManager.getInstance(PersonCenterActivity.this).getCurrentUserId());
				intent.putExtra("visitUserId", UserManager.getInstance(PersonCenterActivity.this).getCurrentUserId());
				intent.putExtra("identityflag", 0);
				startActivity(intent);
				break;
			case REQ_CODE_ALBUM:
				intent = new Intent(PersonCenterActivity.this,AlbumActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("identityflag",0 );	//自己看自己的照片
				intent.putExtra("userId", UserManager.getInstance(PersonCenterActivity.this).getCurrentUserId());
				startActivity(intent);
				break;
			case REQ_CODE_VISITOR:
				intent = new Intent(PersonCenterActivity.this, VisitorActivity.class);
				intent.putExtra("userId", UserManager.getInstance(PersonCenterActivity.this).getCurrentUserId());// 自己ID
				intent.putExtra("visitsNum",UserManager.getInstance(PersonCenterActivity.this).getCurrentUserId());
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(intent);
				break;
			case REQ_CODE_WALLET:
				launch(WalletActivity.class);
				break;
			case REQ_CODE_VZONE:
				intent = new Intent(PersonCenterActivity.this, VzoneActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", UserManager.getInstance(PersonCenterActivity.this).getCurrentUserId());// 自己ID
				intent.putExtra("visitsNum",UserManager.getInstance(PersonCenterActivity.this).getCurrentUserId());
				startActivity(intent);
				break;
			case REQ_CODE_USERINFO:
				intent = new Intent(PersonCenterActivity.this,PersonInfoActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", UserManager.getInstance(PersonCenterActivity.this).getCurrentUserId());
				startActivity(intent);
				break;
			default:
				break;
			}
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(!isRealTime){
			User user = UserManager.getInstance(this).getCurrentUser();
			refreshUI(user);
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		isRealTime = false;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			unregisterReceiver(cgeReceiver);
			cgeReceiver = null;
			closeDialog(mpDialog);//防止窗体泄露
		} catch (Exception e) {
		}
	}
	private void setupView() {
		// TODO Auto-generated method stub
		findViewById(R.id.rl_content).setVisibility(View.GONE);
		findViewById(R.id.sv_content).setVisibility(View.GONE);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		ivHead =  (CircularImage) findViewById(R.id.iv_userhead);
		tvUserName = (TextView) findViewById(R.id.tv_username);
		tvSign = (EditText) findViewById(R.id.tv_sign);
		ivVip = (ImageView) findViewById(R.id.iv_vip);
	}
	
	private void refreshUI(User user) {
		// TODO Auto-generated method stub
		if(user==null){
			tvTitle.setText(null);
			imageLoader.displayImage(null, ivHead,options);
			tvSign.setText(null);
			ivVip.setVisibility(View.GONE);
			tvUserName.setText(null);
			return ;
		}
		tvTitle.setText(getResources().getString(R.string.centre_person));
		
		if(!TextUtils.isEmpty(user.getTempHeadImg()))//未审核通过
			imageLoader.displayImage(user.getTempHeadImg(), ivHead, options);
		else{
			if(!TextUtils.isEmpty(user.getHeadImg())) //审核通过
				imageLoader.displayImage(user.getHeadImg(), ivHead, options);
		}
	
		tvUserName.setText(user.getNickName());
		if(TextUtils.isEmpty(user.getDescript()))
			tvSign.setText(getResources().getString(R.string.no_sign));
		else
			tvSign.setText(user.getDescript());
		ivVip.setVisibility(View.VISIBLE);
		if(user.getIsVIP()!=null&&user.getIsVIP()==1){
			ivVip.setImageDrawable(getResources().getDrawable(R.drawable.ic_vip));
			tvUserName.setTextColor(Color.parseColor("#fc8686"));
		}else{
			ivVip.setImageDrawable(getResources().getDrawable(R.drawable.ic_vip_not));
			tvUserName.setTextColor(Color.WHITE);
		}
		
	}

	private void registerListener() {
		// TODO Auto-generated method stub
		MyClickListener listener = new MyClickListener();
		findViewById(R.id.btn_back).setOnClickListener(listener);
		findViewById(R.id.userinfo).setOnClickListener(listener);
		ivHead.setOnClickListener(listener);
		findViewById(R.id.iv_write).setOnClickListener(listener);
		tvSign.setOnClickListener(listener);
		findViewById(R.id.tv_home_hint).setOnClickListener(listener);
		tvUserName.setOnClickListener(listener);
		ivVip.setOnClickListener(listener);
		findViewById(R.id.tv_gift).setOnClickListener(listener);
		findViewById(R.id.tv_album).setOnClickListener(listener);
		findViewById(R.id.tv_visitor).setOnClickListener(listener);
		findViewById(R.id.rl_personInfo).setOnClickListener(listener);
		findViewById(R.id.rl_wallet).setOnClickListener(listener);
		findViewById(R.id.iv_wallet).setOnClickListener(listener);
		findViewById(R.id.tv_wallet).setOnClickListener(listener);
		findViewById(R.id.tv_wallet_hint).setOnClickListener(listener);
		findViewById(R.id.rl_game).setOnClickListener(listener);
		findViewById(R.id.iv_game).setOnClickListener(listener);
		findViewById(R.id.tv_game).setOnClickListener(listener);
		findViewById(R.id.rl_defend).setOnClickListener(listener);
		findViewById(R.id.iv_defend).setOnClickListener(listener);
		findViewById(R.id.tv_defend).setOnClickListener(listener);
		findViewById(R.id.rl_shop).setOnClickListener(listener);
		findViewById(R.id.iv_shop).setOnClickListener(listener);
		findViewById(R.id.tv_shop).setOnClickListener(listener);
		findViewById(R.id.tv_shop_hint).setOnClickListener(listener);
		findViewById(R.id.rl_settings).setOnClickListener(listener);
		findViewById(R.id.iv_settings).setOnClickListener(listener);
		findViewById(R.id.tv_settings).setOnClickListener(listener);
		
		ivVip.setOnClickListener(listener);
	}
	
	class MyClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = null;
			switch (v.getId()) {
			case R.id.btn_back:
				onBackPressed();
				break;
			case R.id.tv_home_hint:
			case R.id.userinfo:
			case R.id.iv_userhead:
			case R.id.tv_username:
			case R.id.iv_write:
			case R.id.tv_sign:
				if(UserManager.getInstance(PersonCenterActivity.this).getCurrentUser()==null){
					launchForResult(LoginActivity.class,REQ_CODE_VZONE);
					return ;
				}
				intent = new Intent(PersonCenterActivity.this, VzoneActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", UserManager.getInstance(PersonCenterActivity.this).getCurrentUserId());// 自己ID
				intent.putExtra("visitsNum",UserManager.getInstance(PersonCenterActivity.this).getCurrentUserId());
				startActivity(intent);
				break;
			case R.id.tv_gift: //我的礼物
				if(UserManager.getInstance(PersonCenterActivity.this).getCurrentUser()==null){
					launchForResult(LoginActivity.class,REQ_CODE_GIFT);
					return ;
				}
				
				intent = new Intent(PersonCenterActivity.this, GiftWallActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", userId);
				intent.putExtra("visitUserId", userId);
				intent.putExtra("identityflag", 0);
				startActivity(intent);
				break;
			case R.id.tv_album:
				if(UserManager.getInstance(PersonCenterActivity.this).getCurrentUser()==null){
					launchForResult(LoginActivity.class,REQ_CODE_ALBUM);
					return ;
				}
				intent = new Intent(PersonCenterActivity.this,AlbumActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("identityflag",0 );	//自己看自己的照片
				intent.putExtra("userId", UserManager.getInstance(PersonCenterActivity.this).getCurrentUserId());
				startActivity(intent);
				break;
			case R.id.tv_visitor: //访客
				if(UserManager.getInstance(PersonCenterActivity.this).getCurrentUser()==null){
					launchForResult(LoginActivity.class,REQ_CODE_VISITOR);
					return ;
				}
				intent = new Intent(PersonCenterActivity.this, VisitorActivity.class);
				intent.putExtra("userId", UserManager.getInstance(PersonCenterActivity.this).getCurrentUserId());// 自己ID
				intent.putExtra("visitsNum",UserManager.getInstance(PersonCenterActivity.this).getCurrentUserId());
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(intent);
				break;
			case R.id.rl_personInfo:
			case R.id.iv_personInfo:
			case R.id.tv_personInfo:
				if(UserManager.getInstance(PersonCenterActivity.this).getCurrentUser()==null){
					launchForResult(LoginActivity.class,REQ_CODE_USERINFO);
					return ;
				}
				intent = new Intent(PersonCenterActivity.this,PersonInfoActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", UserManager.getInstance(PersonCenterActivity.this).getCurrentUserId());
				startActivity(intent);
				break;
			case R.id.rl_wallet:
			case R.id.iv_wallet:
			case R.id.tv_wallet:
			case R.id.tv_wallet_hint:
				if(UserManager.getInstance(PersonCenterActivity.this).getCurrentUser()==null){
					launchForResult(LoginActivity.class,REQ_CODE_WALLET);
					return ;
				}
				launch(WalletActivity.class);
				break;
//			case R.id.tv_wallet_hint:
//				launch(RechargeActivity.class);
//				break;
			case R.id.rl_game:
			case R.id.iv_game:
			case R.id.tv_game:
				launch(GameActivity.class);
				break;
			case R.id.rl_defend:
			case R.id.iv_defend:
			case R.id.tv_deadline:
				//我的守护
				break;
			case R.id.rl_shop:
			case R.id.iv_shop:
			case R.id.tv_shop:
			case R.id.tv_shop_hint:
				launch(PropertyStoreActivity.class);
				break;
//			case R.id.tv_shop_hint:
//				launch(VIPIntroductionActivity.class);
//				break;
			case R.id.rl_settings:
			case R.id.iv_settings:
			case R.id.tv_settings:
				launch(HouseActivity.class);
				break;
			case R.id.iv_vip:
				Integer isVIP = UserManager.getInstance(PersonCenterActivity.this).getCurrentUser().getIsVIP();
				if(isVIP==null||isVIP==0)
				launch(VIPIntroductionActivity.class);
				break;
			default:
				break;
			}
		}
	}
	
	private void launch(Class<?> clazz) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, clazz);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivity(intent);
	}
	
	private void launchForResult(Class<?> clazz,int requestCode) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, clazz);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivityForResult(intent, requestCode);
	}
	
	/**
	 * 用户信息改变广播接收者
	 * 
	 * 
	 */
	private class ChangeUserInfoBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String headImg =""; 
			if(UserManager.getInstance(context).getCurrentUser() != null && !TextUtils.isEmpty(UserManager.getInstance(context).getCurrentUser().getHeadImg())){
	    		headImg = UserManager.getInstance(context).getCurrentUser().getHeadImg();
	    	}
	    	imageLoader.displayImage(headImg, ivHead, options);
		}
	}
	// 查询用户资料信息
		private void getUserInfo(String userId) {
			// success set user
			JsonObject jo = new JsonObject();
			jo.addProperty("id", userId);

			BottleRestClient.post("queryUserInfo", this, jo, new AsyncHttpResponseHandler() {
				@Override
				public void onStart() {
					super.onStart();
					showDialog("努力加载...","加载失败...",15*1000);
				}
		
				@Override
				public void onFinish() {
					super.onFinish();
					closeDialog(mpDialog);
					findViewById(R.id.rl_content).setVisibility(View.VISIBLE);
					findViewById(R.id.sv_content).setVisibility(View.VISIBLE);
				}
		
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					String str = new String(responseBody);
					closeDialog(mpDialog);
					if (!TextUtils.isEmpty(str)) {
						Gson gson = new Gson();
						UserDataModel model = gson.fromJson(str, UserDataModel.class);
						
						if (model != null && !TextUtils.isEmpty(model.getCode())) {
							if ("0".equals(model.getCode())) {
								isRealTime = true;
								final User user = model.getUserInfo();
								User currentUser = MyApplication.getApp().getDaoSession().getUserDao().load(UserManager.getInstance(PersonCenterActivity.this).getCurrentUser().getUserId());
								String headUrl = getHeadUrl( user.getHeadImg(),user.getTempHeadImg());
								boolean isChange = false;
								if(!TextUtils.isEmpty(currentUser.getHeadImg())){
									if(!TextUtils.isEmpty(headUrl)&&!headUrl.equals(currentUser.getHeadImg())){
										isChange = true;
									}
								}
								//强制更新数据库：如果用户是会员
								if(user.getIsVIP()==1&&UserManager.getInstance(PersonCenterActivity.this).getCurrentUser()!=null&&UserManager.getInstance(PersonCenterActivity.this).getCurrentUser().getIsVIP()==0){
									isChange = true;
								}
								if(isChange){
									runOnUiThread(new Runnable() {
										public void run() {
											MyApplication.getApp().getDaoSession().getUserDao().insertOrReplace(user);
										}
									});
								}
								
								refreshUI(user);
							} else {
								showMsg(model.getMsg());
							}
						} else {
							showMsg("服务器繁忙");
						}
					} else {
						showMsg("服务器繁忙");
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					closeDialog(mpDialog);
				}
			});
		}
		/**
		 * 取出头像地址：优先取出自己浏览时的头像
		 * @param headUrl 别人看自己的头像地址
		 * @param tmpHeadUrl 自己浏览是，查看自己的头像
		 * @return
		 */
		private String getHeadUrl(String headUrl, String tmpHeadUrl) {
			// TODO Auto-generated method stub
			if(TextUtils.isEmpty(headUrl)){
				if(TextUtils.isEmpty(tmpHeadUrl))
					return null;
				else
					return tmpHeadUrl;
			}else{
				if(TextUtils.isEmpty(tmpHeadUrl))
					return headUrl;
				else{
					if(headUrl.equals(tmpHeadUrl))
						return headUrl;
					else 
						return tmpHeadUrl;
				}
			}
		}
		
		
	
		
}
