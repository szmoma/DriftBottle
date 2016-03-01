package com.hnmoma.driftbottle;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.chat.EMContactManager;
import com.easemob.exceptions.EaseMobException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.hnmoma.driftbottle.adapter.VzoneGiftAdapter;
import com.hnmoma.driftbottle.adapter.VzonePhotoAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.custom.CustomDialog;
import com.hnmoma.driftbottle.custom.CustomDialog.CustomDialogItemClickListener;
import com.hnmoma.driftbottle.custom.DampView;
import com.hnmoma.driftbottle.custom.DampView.OnScrollChangedListener;
import com.hnmoma.driftbottle.custom.PropsTextProgressBar;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.model.PhotoWallModel;
import com.hnmoma.driftbottle.model.QueryUInfoBModel;
import com.hnmoma.driftbottle.model.TousuModel;
import com.hnmoma.driftbottle.model.User;
import com.hnmoma.driftbottle.model.UserAccountInfo;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.model.VzoneGiftModel;
import com.hnmoma.driftbottle.model.VzoneUserInfo;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class VzoneActivity extends BaseActivity {
	public static final int REQUEST_CODE_ADDPHOTO = 101; //添加照片的请求,该页面到相册页面中

	private static final int REQUEST_CODE_PHOTO_WALL = 102;	//浏览照片墙
	
	private static final int REQUEST_CODE_USERINFO = 103;	//修改用户信息

	protected  final int REFRESH = 1;
	TextView tvTitle; // 标题名称

	DampView dampView; // 阻尼控件，父类是ScrollView
	ImageView ivDampBackGround;


	CircularImage ivHead; // 用户头像
	TextView tvUserName; // 用户信息名
	TextView tvZone; // 地区
	TextView tvAge; // 年龄和性别
	TextView tvColl; // 星座
	ImageView ivVip;// VIP徽章
	EditText tvSign;// 用户签名

	PropsTextProgressBar pbExp;// 经验条
	TextView tvLevel;//等级
	TextView tvFans; // 粉丝数量
	TextView tvCharm;// 魅力值
	TextView tvLove;// 爱心值

	TextView tvAlbum; // 相册中照片的数量
	LinearLayout llGalleryAlbum; // 相片展示
	TextView tvGift; // 礼物数量
	LinearLayout llGalleryGift; // 礼物展示

	ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;

	VzoneUserInfo vui; // 空间信息
	List<VzoneGiftModel> gifts; // 礼物集合
	List<PhotoWallModel> photos; // 相册集合

	GridView gvAlbum;// 相册
	GridView gvGift;// 礼物
	TextView tvEmptyAlbum;	//相册是空的提示
	TextView tvEmptyGift;	//礼物是空的提示
	
	 VzoneGiftAdapter giftAdapter;// 礼物
	 VzonePhotoAdapter photoAdapter;
	 private boolean isMark;	//是否被浏览者关注
	 
	 private Handler  handler = new Handler(){
			public void handleMessage(android.os.Message msg) {
				switch (msg.arg1) {
				case REFRESH:
					BaseAdapter adapter = (BaseAdapter) msg.obj;
					adapter.notifyDataSetChanged();
					break;

				default:
					break;
				}
			};
		};

	/**
	 * 身份标识，0表示自己看自己,1表示看别人
	 */
	int identityflag = 0;
	String userId;	//被访问的对象
	String visitUserId; //访问者
	
	boolean isSayHello = false;
	
	private ChangeUserInfoBroadcastReceiver cgeReceiver;
	private int[] location = new  int[2] ; //保存头像在当前窗口内的绝对坐标

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("userId", userId);
		outState.putString("visitUserId", visitUserId);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vzone);

		if (savedInstanceState != null) {
			userId = savedInstanceState.getString("userId");
			visitUserId = savedInstanceState.getString("visitUserId");
		}

		Intent intent = getIntent();
		if (intent != null) {
			userId = intent.getStringExtra("userId");
			visitUserId = intent.getStringExtra("visitUserId");
		}
		
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.defalutimg)
				.showImageForEmptyUri(R.drawable.defalutimg)
				.showImageOnFail(R.drawable.defalutimg).cacheInMemory(true)
				.cacheOnDisc(true).considerExifParams(true)
				// .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.bitmapConfig(Bitmap.Config.RGB_565)
				// .displayer(new RoundedBitmapDisplayer(2))
				.build();
		if(TextUtils.isEmpty(userId))
			finish();
		
		setupView();
		init();
		pickData();
		setListener();
		
		// 用户信息改变广播
		cgeReceiver = new ChangeUserInfoBroadcastReceiver();
		IntentFilter cgeFilter = new IntentFilter(MyConstants.USERINFOCHANGE_BROADCAST_ACTION);
		cgeFilter.setPriority(3);
		registerReceiver(cgeReceiver, cgeFilter);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		 if(requestCode == 100) {
			if(resultCode == Activity.RESULT_OK) {
				isSayHello = true;
			}
		}else if(requestCode==REQUEST_CODE_ADDPHOTO){
			if(resultCode==Activity.RESULT_OK){
				if(data==null)
					return ;
				photos =(List<PhotoWallModel>) data.getSerializableExtra("resultList");
				int picNUm = data.getIntExtra("picNUm", 0);
				if(picNUm==0){
					tvAlbum.setText("相册(" + (photos==null?"0": photos.size() )+ ")");
				}else{
					tvAlbum.setText("相册(" + picNUm+ ")");
				}
				initAlbum(photos);
				
			}
		}else if(requestCode==REQUEST_CODE_PHOTO_WALL){
			if(resultCode==Activity.RESULT_OK){
				if(data==null)
					return ;
				photos =(List<PhotoWallModel>) data.getSerializableExtra("resultList");
				initAlbum(photos);
				int picNUm = data.getIntExtra("picNUm", 0);
				if(picNUm==0){
					tvAlbum.setText("相册(" + (photos==null?"0": photos.size() )+ ")");
				}else{
					tvAlbum.setText("相册(" + picNUm+ ")");
				}
			}
		}else if(requestCode==REQUEST_CODE_USERINFO){
			if(resultCode==Activity.RESULT_OK){
				if(data==null)
					return ;
				UserInfoModel model = (UserInfoModel) data.getSerializableExtra("UserInfoModel");
				//刷新当前页面：用户头像、年龄、星座、无法修改性别
				tvAge.setText(String.valueOf(model.getAge()));
				if (TextUtils.isEmpty(model.getDescript()))
					tvSign.setText(getResources().getString(R.string.no_sign));
				else
					tvSign.setText(model.getDescript());// 签名
				tvColl.setText(model.getConstell());
				tvZone.setText(model.getProvince()+ " "+model.getCity());
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unregisterReceiver(cgeReceiver);
			cgeReceiver = null;
		} catch (Exception e) {
		}
	}
	

	private void init() {
		// TODO Auto-generated method stub
		//礼物
		giftAdapter = new VzoneGiftAdapter(imageLoader, options);
		gvGift.setAdapter(giftAdapter);
		//相册
		photoAdapter = new VzonePhotoAdapter(imageLoader, identityflag);
		gvAlbum.setAdapter(photoAdapter);
		
		if(UserManager.getInstance(this).getCurrentUser()!=null&&
				UserManager.getInstance(this).getCurrentUser().getUserId().equals(userId))
			identityflag = 0;
		else
			identityflag =1 ;
		if(TextUtils.isEmpty(visitUserId)){
			identityflag = 0;
		}
		
		if(identityflag==0){	//自己查看空间
			findViewById(R.id.ll_bt).setVisibility(View.GONE);
			if (MyApplication.getApp().getSpUtil().getMyMoney() <= 0) {
				queryCharmAndMoney();
			}
		}else { //查看其他人的空间
			findViewById(R.id.ll_bt).setVisibility(View.VISIBLE);
		}
		
		//默认情况下，都是有礼物和相片的
		tvEmptyAlbum.setVisibility(View.GONE);
		tvEmptyGift.setVisibility(View.GONE);
		gvAlbum.setVisibility(View.VISIBLE);
		gvAlbum.setVisibility(View.VISIBLE);
		
		dampView.scrollTo(0,ivDampBackGround.getTop());;
	}

	// 初始化界面
	public void setupView() {
		tvTitle = (TextView) findViewById(R.id.tv_title);
		dampView = (DampView) findViewById(R.id.dampview);

		ivDampBackGround = (ImageView) findViewById(R.id.iv_background);
		dampView.setImageView(ivDampBackGround);

		ivHead = (CircularImage) findViewById(R.id.iv_head);
		tvUserName = (TextView) findViewById(R.id.tv_name);
		tvColl = (TextView) findViewById(R.id.tv_coll);
		ivVip = (ImageView) findViewById(R.id.iv_vip);
		tvZone = (TextView) findViewById(R.id.tv_dq);
		tvAge = (TextView) findViewById(R.id.tv_age);

		tvSign = (EditText) findViewById(R.id.tv_sign);

		pbExp =  (PropsTextProgressBar) findViewById(R.id.pb_exp);
		tvLevel = (TextView) findViewById(R.id.tv_level);
		tvFans = (TextView) findViewById(R.id.tv_fans);
		tvLove = (TextView) findViewById(R.id.tv_love);
		tvCharm = (TextView) findViewById(R.id.tv_charm);

		tvAlbum = (TextView) findViewById(R.id.tv_album);
		tvGift = (TextView) findViewById(R.id.tv_gift);
		
		gvAlbum =  (GridView) findViewById(R.id.wgv_album);
		gvGift =  (GridView) findViewById(R.id.wgv_gift);
		
		tvEmptyAlbum= (TextView) findViewById(R.id.tv_emty_album);
		tvEmptyGift= (TextView) findViewById(R.id.tv_emty_gift);
	}

	/**
	 * 刷新用户信息：等级、用户名、头像、性别、年龄、星座、Vip、位置（省份）、爱心、魅力、粉丝、相册数量、礼物数量
	 * 
	 * @param userInfo
	 */
	public void initPerson(VzoneUserInfo userInfo) {
		if (userInfo == null)
			return;
		
		tvUserName.setVisibility(View.GONE);
		if (UserManager.getInstance(this).getCurrentUserId().equals(userInfo.getUserId())) {// 自己的空间
			tvTitle.setText(getResources().getString(R.string.tv_myzone));
			findViewById(R.id.iv_beach).setVisibility(View.GONE);
			User user = UserManager.getInstance(this).getCurrentUser();
			if(!TextUtils.isEmpty(user.getTempHeadImg()))//未审核通过
				imageLoader.displayImage(user.getTempHeadImg(), ivHead, options);
			if(TextUtils.isEmpty(user.getTempHeadImg())&&!TextUtils.isEmpty(user.getHeadImg())) //审核通过
					imageLoader.displayImage(user.getHeadImg(), ivHead, options);
		} else {
			tvTitle.setText(userInfo.getNickName());
			tvSign.setClickable(false);
			tvSign.setEnabled(false);
			findViewById(R.id.iv_beach).setVisibility(View.VISIBLE);
			imageLoader.displayImage(userInfo.getHeadImg(), ivHead, options);
		}
		imageLoader.displayImage(userInfo.getHeadImg(), ivHead, options);
		((ImageView)findViewById(R.id.iv_beach)).getLocationOnScreen(location);//获取头像在当前窗口内的绝对坐标
		tvUserName.setText(userInfo.getNickName());
		// 性别
		String idtype = userInfo.getIdentityType();
		String[] its = new String[2];
		if (TextUtils.isEmpty(idtype)) {
			its[0] = "男";
			its[1] = "m";
		} else {
			its = getIdentityByCode(idtype);
		}
		// 年龄和性别
		if (!TextUtils.isEmpty(userInfo.getAge())) {
			tvAge.setText(String.valueOf(userInfo.getAge()));
		} else {
			tvAge.setText("0");
		}

		Drawable drawable;
		if (its[1].equals("m")) {
			drawable = getResources().getDrawable(R.drawable.icon_male_32);
			tvAge.setBackgroundResource(R.drawable.bg_sex_man);
		} else {
			drawable = getResources().getDrawable(R.drawable.icon_female_32);
			tvAge.setBackgroundResource(R.drawable.bg_sex_female);
		}
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		tvAge.setCompoundDrawables(drawable, null, null, null);
		tvAge.setPadding(MoMaUtil.dip2px(this, 5), 0, MoMaUtil.dip2px(this, 5),
				0);
		// 星座
		if (TextUtils.isEmpty(userInfo.getConstell())) {
			tvColl.setText("");
		} else {
			tvColl.setText(userInfo.getConstell());
		}
		// 位置--省市
		String province = TextUtils.isEmpty(userInfo.getProvince()) ? "": userInfo.getProvince();
		String city = TextUtils.isEmpty(userInfo.getCity()) ? "" : userInfo.getCity();

		if (TextUtils.isEmpty(province) && TextUtils.isEmpty(city)) {
			tvZone.setVisibility(View.GONE);
		} else {
			tvZone.setVisibility(View.VISIBLE);
			tvZone.setText(province + " " + city);
		}
		//隐藏地区
		tvZone.setVisibility(View.GONE);
		
		String [] level = MoMaUtil.getLevel(userInfo.getPointNum());//积分转化为等级
		tvLevel.setText("LV."+level[0]);
		pbExp.setMax(Integer.parseInt(level[2]));
		
		pbExp.setProgress(userInfo.getPointNum());

		// 如果用户是VIP，则添加VIP的图标
		if (userInfo.getIsVIP() == 0) { // 不是VIP
			ivVip.setImageDrawable(getResources().getDrawable(R.drawable.ic_vip_not));
		} else {// 是VIP
			ivVip.setImageDrawable(getResources().getDrawable(R.drawable.ic_vip));
		}

		// 礼品数量
		if(userInfo.getGiftNum()<=0){
			tvEmptyGift.setText(getResources().getString(R.string.empty_gift));
			tvEmptyGift.setVisibility(View.VISIBLE);
			tvGift.setText("礼物(0)");
			gvGift.setVisibility(View.GONE);
		}else{
			tvEmptyGift.setVisibility(View.GONE);
			gvGift.setVisibility(View.VISIBLE);
			tvGift.setText("礼物(" + userInfo.getGiftNum() + ")");
		}
		
		// 照片数量
		if(userInfo.getPicNum()<=0){
			if(identityflag==1)
				tvEmptyAlbum.setText(getResources().getString(R.string.empty_album));
			else 
				tvEmptyAlbum.setText(getResources().getString(R.string.empty_album_my));
			tvEmptyAlbum.setVisibility(View.VISIBLE);
			gvAlbum.setVisibility(View.GONE);
			tvAlbum.setText("相册(0)");
		}else{
			tvEmptyAlbum.setVisibility(View.GONE);
			gvAlbum.setVisibility(View.VISIBLE);
			tvAlbum.setText("相册(" + userInfo.getPicNum() + ")");
		}
		
		if (TextUtils.isEmpty(userInfo.getDescript()))
			tvSign.setText(getResources().getString(R.string.no_sign));
		else
			tvSign.setText(userInfo.getDescript());// 签名
		
		if(userInfo.getLoveNum()>99999)
			tvLove.setText(99999+"+"); // 爱心数
		else
			tvLove.setText(String.valueOf(userInfo.getLoveNum())); // 爱心数
		if(userInfo.getCharm()>99999)
			tvCharm.setText(99999+"+"); // 魅力值
		else
			tvCharm.setText(String.valueOf(userInfo.getCharm())); // 魅力值
		if(userInfo.getFansNum()>99999)
			tvFans.setText(99999+"+"); //粉丝数量
		else
			tvFans.setText(String.valueOf(userInfo.getFansNum()));// 粉丝数量
	}

	public void initGift(List<VzoneGiftModel> list) {
		// 礼品数量
				
		if(list==null||list.isEmpty()){
			tvEmptyGift.setText(getResources().getString(R.string.empty_gift));
			tvEmptyGift.setVisibility(View.VISIBLE);
			tvGift.setText("礼物(0)");
			gvGift.setVisibility(View.GONE);
			return ;
		}
		List<VzoneGiftModel> gifts = new LinkedList<VzoneGiftModel>();
		if(list.size() < 4) {
			gifts.addAll(list);
			int emptygift = 4 - list.size();
			VzoneGiftModel gift;
			for(int i= 0; i < emptygift; i++) {
				  gift = new VzoneGiftModel();
				gifts.add(gift);//TODO
			}
		}else if(list.size()>4){
			for(int i=0;i<4;i++){ //只是取4个
				gifts.add(list.get(i));
			}
		}else
			gifts.addAll(list);
			
		gvGift.setVisibility(View.VISIBLE);
		tvEmptyGift.setVisibility(View.GONE);
		giftAdapter.addItemLast(gifts);
		Message msg = Message.obtain();
		msg.arg1  =REFRESH;
		msg.obj = giftAdapter;
		handler.sendMessage(msg);
	}

	public void initAlbum(List<PhotoWallModel> list) {
		if(list==null||list.isEmpty()){
			if(identityflag==1)
				tvEmptyAlbum.setText(getResources().getString(R.string.empty_album));
			else 
				tvEmptyAlbum.setText(getResources().getString(R.string.empty_album_my));
			tvEmptyAlbum.setVisibility(View.VISIBLE);
			gvAlbum.setVisibility(View.GONE);
			tvAlbum.setText("相册(0)");
			return ;
		}
		List<PhotoWallModel> albums = new LinkedList<PhotoWallModel>();
		if(list.size() < 4) {
			albums.addAll(list);
			int emptygift = 4 - list.size();
			//把不够的位置，补足4个
			for(int i= 0; i < emptygift; i++) {
				PhotoWallModel  photo = new PhotoWallModel();
				albums.add(photo);
			}
		}else if(list.size()>4){
			for(int i=0;i<4;i++)
				albums.add(list.get(i));
		}else
			albums.addAll(list);
		
		tvEmptyAlbum.setVisibility(View.GONE);
		gvAlbum.setVisibility(View.VISIBLE);	
		photoAdapter.reset(albums);
		
		Message msg = Message.obtain();
		msg.arg1  =REFRESH;
		msg.obj = photoAdapter;
		handler.sendMessage(msg);
	}

	// 初始获取数据
	private void pickData() {
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", userId.trim());
		if (!UserManager.getInstance(this).getCurrentUserId().equals(userId)&&!TextUtils.isEmpty(visitUserId)) {
			jo.addProperty("visitUserId", visitUserId.trim());
		}

		BottleRestClient.post("querySpaceInfo", this, jo,new AsyncHttpResponseHandler() {
					@Override
					public void onStart() {
						super.onStart();
						showDialog("努力加载...", "加载失败...", 15*1000);
					}

					@Override
					public void onFinish() {
						super.onFinish();
						closeDialog(mpDialog);
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							Type type = new TypeToken<QueryUInfoBModel>() {}.getType();
							QueryUInfoBModel model = gson.fromJson(str, type);

							if (model != null&& !TextUtils.isEmpty(model.getCode())) {
								if ("0".equals(model.getCode())) {
									vui = model.getUserInfo();
									photos = model.getPicList();
									gifts = model.getGiftList();
									initPerson(vui);
									initAlbum(photos);
									initGift(gifts);
								} else {
									showMsg(model.getMsg());
								}
							} else {
								showMsg("服务器繁忙");
								setResult(RESULT_CANCELED);
								// finish();
							}
						} else {
							showMsg("服务器繁忙");
							setResult(RESULT_CANCELED);
							// finish();
						}
					}

					@Override
					public void onFailure(int statusCode, @SuppressWarnings("deprecation")Header[] headers,byte[] responseBody, Throwable error) {
						closeDialog(mpDialog);
					}
				});
	}	
	
	/*
	 * 获取性别
	 */
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
		if (code.equals("2000") || code.equals("2001") || code.equals("2002")
				|| code.equals("2007")) {
			strs[1] = "f";
		} else {
			strs[1] = "m";
		}

		return strs;
	}

	/**
	 * 查询用户的魅力值与扇贝
	 */
	private void queryCharmAndMoney() {

		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId().trim());
		jo.addProperty("deviceId", getDeviceId().trim());
		BottleRestClient.post("queryAccount", this, jo,new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							UserAccountInfo baseModel = gson.fromJson(str,
									UserAccountInfo.class);
							if (baseModel != null
									&& !TextUtils.isEmpty(baseModel.getCode())) {
								if ("0".equals(baseModel.getCode())) {
									MyApplication.getApp().getSpUtil().setMyMoney(baseModel.getAccountInfo().getMoney());
								}
							} else {
								// showMsg("查询余额失败");
							}
						} else {
							// showMsg("查询余额失败");
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						// showMsg("查询余额失败");
					}

					@Override
					public void onFinish() {
						super.onFinish();
					}
				});

	}
	private void setListener() {
		// TODO Auto-generated method stub
		gvGift.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View view, int position, long id) {
				Intent intent = new Intent(VzoneActivity.this, GiftWallActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", userId);
				intent.putExtra("identityflag", identityflag);
				intent.putExtra("visitUserId", visitUserId);
				startActivity(intent);
			}
		});
		gvAlbum.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				PhotoWallModel model = (PhotoWallModel) parent.getItemAtPosition(position);
				Intent intent = new Intent(VzoneActivity.this, PhotoWallShowActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("identityflag", identityflag);
				intent.putExtra("userId", userId);
				//用户可能会点击到空的相片,需要把有效的位置告诉给下一个页面
				if(model==null||TextUtils.isEmpty(model.getPicId())){
					for(int i=photos.size()-1;i>=0;i--){
						//找到一个有效的位置
						if(photos.get(i)!=null&&!TextUtils.isEmpty(photos.get(i).getPicId())){
							intent.putExtra("position", i);
							break;
						}
						//如果相片都是空的，则不允许点击
						if(i==0){
							return ;
						}
					}
						
				}else{
					intent.putExtra("position", position);
				}
				
				List<PhotoWallModel> realPhoto  = new ArrayList<PhotoWallModel>();

				for(PhotoWallModel photo:photos){
					 if(!TextUtils.isEmpty(photo.getPicId()))
						 realPhoto.add(photo);
				}
				intent.putExtra("imageUrls", (Serializable)realPhoto);
				//分页处理：默认情况，只显示最新的4条记录，如果用户的相册数量超过4，必须请求下一页
				if(vui.getPicNum()>photos.size()){
					intent.putExtra("page", true);
				}else
					intent.putExtra("page", false);
				startActivityForResult(intent,REQUEST_CODE_PHOTO_WALL);
			}
		});
		MyClickListener listener = new MyClickListener();
		//点击相册标题，进入相册空间
		tvAlbum.setOnClickListener(listener);
		//点击里有标题，进入礼物列表
		tvGift.setOnClickListener(listener);
		tvLevel.setOnClickListener(listener);
		findViewById(R.id.btn_back).setOnClickListener(listener);
		
		findViewById(R.id.userinfo).setOnClickListener(listener);
		findViewById(R.id.back_space).setOnClickListener(listener);
		tvUserName.setOnClickListener(listener);
		
		ivHead.setOnClickListener(listener);
		
		findViewById(R.id.ll_exp).setOnClickListener(listener);
		pbExp.setOnClickListener(listener);
		findViewById(R.id.rl_fans).setOnClickListener(listener);
		findViewById(R.id.iv_fans).setOnClickListener(listener);
		findViewById(R.id.tv_fans).setOnClickListener(listener);
		findViewById(R.id.tv_fans_label).setOnClickListener(listener);
		findViewById(R.id.rl_charm).setOnClickListener(listener);
		findViewById(R.id.iv_charm).setOnClickListener(listener);
		findViewById(R.id.tv_charm).setOnClickListener(listener);
		findViewById(R.id.tv_charm_label).setOnClickListener(listener);
		findViewById(R.id.rl_love).setOnClickListener(listener);
		findViewById(R.id.iv_love).setOnClickListener(listener);
		findViewById(R.id.tv_love).setOnClickListener(listener);
		findViewById(R.id.tv_love_label).setOnClickListener(listener);
		tvEmptyAlbum.setOnClickListener(listener);
		findViewById(R.id.ll_chat).setOnClickListener(listener);
		findViewById(R.id.tv_chat).setOnClickListener(listener);
		findViewById(R.id.iv_chat).setOnClickListener(listener);
		
		findViewById(R.id.ll_mark).setOnClickListener(listener);
		findViewById(R.id.iv_mark).setOnClickListener(listener);
//		findViewById(R.id.tv_mark).setOnClickListener(listener);
		
		findViewById(R.id.ll_sl).setOnClickListener(listener);
		findViewById(R.id.iv_sl).setOnClickListener(listener);
		findViewById(R.id.tv_sl).setOnClickListener(listener);
		
		findViewById(R.id.ll_more).setOnClickListener(listener);
		findViewById(R.id.iv_more).setOnClickListener(listener);
		findViewById(R.id.tv_more).setOnClickListener(listener);
		
		findViewById(R.id.iv_beach).setOnClickListener(listener);
		
		dampView.setOnScrollListener(new OnScrollChangedListener() {
			
			@Override
			public void onScrollChanged(int x, int y, int oldxX, int oldY) {
				// TODO Auto-generated method stub
			    MoMaLog.e("debug", "x="+location[0]+",y="+location[1]);
				if(dampView.isAtBottom()){
					findViewById(R.id.top_bar).setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_top_bar));
				}else if(y>=location[1]){
					findViewById(R.id.top_bar).setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_top_bar));
				}else
					findViewById(R.id.top_bar).setBackgroundColor(getResources().getColor(R.color.transparent));
			}
			
		});
		
	}
	class MyClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = null;
			switch (v.getId()) {
			case R.id.tv_album:
				intent = new Intent(VzoneActivity.this,AlbumActivity.class);
				intent.putExtra("userId", userId);
				intent.putExtra("identityflag", identityflag);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivityForResult(intent, REQUEST_CODE_PHOTO_WALL);
				break;
			case R.id.tv_gift:
				intent = new Intent(VzoneActivity.this, GiftWallActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", userId);
				intent.putExtra("visitUserId", visitUserId);
				startActivity(intent);
				break;
			case R.id.tv_level:
			case R.id.ll_exp:
			case R.id.pb_exp:
			case R.id.rl_fans:
			case R.id.iv_fans:
			case R.id.tv_fans:
			case R.id.tv_fans_label:
			case R.id.rl_charm:
			case R.id.iv_charm:
			case R.id.tv_charm:
			case R.id.tv_charm_label:
			case R.id.rl_love:
			case R.id.iv_love:
			case R.id.tv_love:
			case R.id.tv_love_label:
				intent = new Intent(VzoneActivity.this,GradeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", userId);
				startActivity(intent);
				break;
			case R.id.btn_back:
				finish();
				break;
			case R.id.back_space:
			case R.id.userinfo:
			case R.id.tv_name:
			case  R.id.iv_head:
				intent = new Intent(VzoneActivity.this,PersonInfoActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", userId);
				startActivityForResult(intent, REQUEST_CODE_USERINFO);
				break;
			case R.id.ll_chat://打招呼
				findViewById(R.id.iv_chat).setPressed(true);
				findViewById(R.id.tv_chat).setPressed(true);
				launchSayHello(vui);
				break;
			case R.id.tv_chat:
				findViewById(R.id.iv_chat).setPressed(true);
				launchSayHello(vui);
				break;
			case R.id.iv_chat:
				findViewById(R.id.tv_chat).setPressed(true);
				launchSayHello(vui);
				break;
			case R.id.ll_mark:
				if(isMark){
					showMsg("已经标记");
				}else{
					findViewById(R.id.iv_mark).setPressed(true);
					findViewById(R.id.tv_mark).setPressed(true);
					addConcern();
				}
				break;
			case R.id.iv_mark:
				if(isMark){
					showMsg("已经标记");
				}else{
					findViewById(R.id.tv_mark).setPressed(true);
					addConcern();
				}
				break;
			case R.id.ll_sl:	//去礼物商城送礼
				findViewById(R.id.iv_sl).setPressed(true);
				findViewById(R.id.tv_sl).setPressed(true);
				launchGift();
				break;
			case R.id.iv_sl:
				findViewById(R.id.tv_sl).setPressed(true);
				launchGift();
				break;
			case R.id.tv_sl:
				findViewById(R.id.iv_sl).setPressed(true);
				launchGift();
				break;
			case R.id.ll_more:	//更多
				findViewById(R.id.iv_more).setPressed(true);
				findViewById(R.id.tv_more).setPressed(true);
				showMore();
				break;
			case R.id.iv_more:
				findViewById(R.id.tv_more).setPressed(true);
				showMore();
				break;
			case R.id.tv_more:
				findViewById(R.id.iv_more).setPressed(true);
				showMore();
				break;
			case R.id.iv_beach:
				//如果是浏览自己的空间，则进入自己的沙滩；如果是浏览他人的空间，则进入其他人的空间
				if(identityflag==0){
//					Intent otherBeach = new Intent(VzoneActivity.this, MainActivity.class);
//					otherBeach.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//					startActivity(otherBeach);
				}else{
					Intent otherBeach = new Intent(VzoneActivity.this, OtherBeachActivity.class);
					otherBeach.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					otherBeach.putExtra("beachHost", userId);
					if(vui!=null)
						otherBeach.putExtra("nickName", vui.getNickName());
					startActivity(otherBeach);
				}
				break;
			case R.id.tv_emty_album:
				if(identityflag==1){
					break;
				}
				intent = new Intent(VzoneActivity.this,AlbumActivity.class);
				intent.putExtra("userId", userId);
				intent.putExtra("addPhoto", true);
				intent.putExtra("identityflag", identityflag);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivityForResult(intent,REQUEST_CODE_ADDPHOTO);
				break;
			default:
				break;
			}
		}

		private void launchGift() {
			// TODO Auto-generated method stub
			Intent intent = new Intent(VzoneActivity.this, LwscActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			intent.putExtra("userId", userId);
			intent.putExtra("visitUserId", visitUserId);
			startActivity(intent);
		}

		private void launchSayHello(VzoneUserInfo zoneUserInfo) {
			// TODO Auto-generated method stub
			if(isSayHello) {
				showMsg("矜持，已经打过招呼咯~");
				return ;
			}
			if(zoneUserInfo==null)
				return ;
			Intent chat = new Intent(VzoneActivity.this, ThrowDxpActivity.class);
			chat.putExtra("userId", userId);
			chat.putExtra("visitUserId", visitUserId);
			chat.putExtra("toUserName", zoneUserInfo.getNickName());
			startActivityForResult(chat, 100);
		}
		
	}
	
	// 显示更多的对话框
		private void showMore() {
			String[] item;
			//获取黑名单用户的usernames
			List<String> blackList = EMContactManager.getInstance().getBlackListUsernames();
			if(blackList.contains(userId)){
				item = new String[] {"猜拳","举报", "移出拉黑"};
			}else{
				item = new String[] {"猜拳","举报", "拉黑"};
			}
			
			new CustomDialog().showListDialog(this, "请选择", item, new CustomDialogItemClickListener() {
				
				@Override
				public void confirm(String result) {
					
					if("猜拳".equals(result)) {
						if(UserManager.getInstance(VzoneActivity.this).getCurrentUserId().equals(userId))
							return ;
						Intent intent = new Intent(VzoneActivity.this,GameActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("userId", userId);
						intent.putExtra("visitUserId", visitUserId);
						startActivity(intent);
					} else if("举报".equals(result)) {
						Intent jubao = new Intent(VzoneActivity.this, VzoneTousuActivity.class);
						jubao.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						TousuModel tm = new TousuModel();
						tm.setUserId(visitUserId);
						tm.setbUserId(userId);
						tm.setReportType(1);
						jubao.putExtra("tousuModel", tm);
						startActivityForResult(jubao, 1000);
					} else if("拉黑".equals(result)){
						//第二个参数如果为true，则把用户加入到黑名单后双方发消息时对方都收不到；false,则
		    			//我能给黑名单的中用户发消息，但是对方发给我时我是收不到的
		    			try {
							EMContactManager.getInstance().addUserToBlackList(userId, false);
							showMsg("已加入黑名单,您不会收到对方的消息");
						} catch (EaseMobException e) {
						}
					}else if("移出拉黑".equalsIgnoreCase(result)){
						try {
							EMContactManager.getInstance().deleteUserFromBlackList(userId);
							showMsg("已移出黑名单,您会接受对方的消息");
						} catch (EaseMobException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			});
		}
		
		private void addConcern() {
			JsonObject jo = new JsonObject();
			jo.addProperty("userId", visitUserId.trim());
			jo.addProperty("bUserId", userId.trim());
			
			BottleRestClient.post("addAttention", this, jo, new AsyncHttpResponseHandler() {
				@Override
				public void onStart() {
					// TODO Auto-generated method stub
					super.onStart();
					((ImageView)findViewById(R.id.iv_mark)).setSelected(true);
					((TextView)findViewById(R.id.tv_mark)).setSelected(true);
					findViewById(R.id.iv_mark).setFocusable(true);
					findViewById(R.id.tv_mark).setFocusable(true);
					showDialog("标记中...", "标记失败", 15*1000);
				}
				@Override
				public void onSuccess(int statusCode, Header[] headers,byte[] responseBody) {
					String str = new String(responseBody);
					((TextView)findViewById(R.id.tv_mark)).setSelected(false);
					if (!TextUtils.isEmpty(str)) {
						Gson gson = new Gson();
						Type type = new TypeToken<BaseModel>(){}.getType();
						BaseModel model = gson.fromJson(str, type);
						
						if (model != null && !TextUtils.isEmpty(model.getCode())) {
							if ("0".equals(model.getCode())) {
								isMark = true;
								findViewById(R.id.iv_mark).setFocusable(false);
								showMsg(getResources().getString(R.string.activity_concern_title) + "成功");
							} else if("100026".equals(model.getCode())){
								showMsg(model.getMsg());
								findViewById(R.id.iv_mark).setFocusable(false);
								
							}else if("100027".equals(model.getCode())){
								showMsg(model.getMsg()); //标记人数达到上限
								((ImageView)findViewById(R.id.iv_mark)).setSelected(false);
							}else{
								showMsg(model.getMsg());
								((ImageView)findViewById(R.id.iv_mark)).setSelected(false);
							}
						} else {
							showMsg("服务器繁忙");
							((ImageView)findViewById(R.id.iv_mark)).setSelected(false);
							((TextView)findViewById(R.id.tv_mark)).setSelected(false);
						}
					} else {
						showMsg("服务器繁忙");
						((ImageView)findViewById(R.id.iv_mark)).setSelected(false);
						((TextView)findViewById(R.id.tv_mark)).setSelected(false);
					}
				
				}
				
				@Override
				public void onFinish() {
					// TODO Auto-generated method stub
					super.onFinish();
					closeDialog(mpDialog);
				}

				@Override
				public void onFailure(int statusCode, Header[] headers,
						byte[] responseBody, Throwable error) {
					showMsg(getResources().getString(R.string.activity_concern_title) + "失败");
					((ImageView)findViewById(R.id.iv_mark)).setSelected(false);
					((TextView)findViewById(R.id.tv_mark)).setSelected(false);
					closeDialog(mpDialog);
				}
				
			});
			
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
}
