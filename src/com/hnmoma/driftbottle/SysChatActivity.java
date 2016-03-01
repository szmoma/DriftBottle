package com.hnmoma.driftbottle;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.adapter.ChatAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.fragment.BaseFragment;
import com.hnmoma.driftbottle.itfc.FrameShowCallBack;
import com.hnmoma.driftbottle.itfc.TaskCallBack;
import com.hnmoma.driftbottle.model.Bottle;
import com.hnmoma.driftbottle.model.Chat;
import com.hnmoma.driftbottle.model.ChatDao;
import com.hnmoma.driftbottle.model.DaoSession;
import com.hnmoma.driftbottle.model.DealBottleModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.way.ui.emoji.EmojiEditText;
import com.way.ui.emoji.EmojiKeyboard;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * @author MOMA_PC
 *
 */
public class SysChatActivity extends BaseActivity implements OnClickListener, PlatformActionListener, ShareContentCustomizeCallback, Callback, FrameShowCallBack, TaskCallBack{
	ListView lv;
	ChatAdapter adapter;
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	public Bottle bottleModel;
	
	String msg;
	
	LinearLayout bottom;
	View footView;
	ImageView bt_send;
	
	public static final int CHATTYPE_SINGLE = 1;
	public static final int CHATTYPE_GROUP = 2;
	private final int FRESH  =100;
	/**
	 * 用来处理滑动时回到最底下和界面变形冲突问题
	 */
	Handler myHandler = new Handler();
	MyThread myThread = new MyThread();
	class MyThread implements Runnable{
		public void run() {
			lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		}
	};
	
	InputMethodManager imm;
	
	boolean isloading;
	boolean hasMore = true;
	static final int PAGENUM = 20; 
	int pageIndex = 0;
	
	Handler handler;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	boolean hasChanged;
	
	
	//===================
	private EmojiKeyboard mFaceRoot;// 表情父容器
	EmojiEditText mEditEmojicon;
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("bottleModel", bottleModel);
		outState.putBoolean("hasChanged", hasChanged);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler(this);
		imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		if (savedInstanceState != null){
			bottleModel = (Bottle) savedInstanceState.getSerializable("bottleModel");
			hasChanged = savedInstanceState.getBoolean("hasChanged");
		}
		
		Intent intent = getIntent();
		if(intent != null){
			bottleModel = (Bottle) intent.getSerializableExtra("bottleModel");
		}
		
		if(bottleModel==null){
			this.finish();
			return;
		}
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)
		.showImageForEmptyUri(R.drawable.defalutimg)
		.showImageOnFail(R.drawable.defalutimg)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
//		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.bitmapConfig(Bitmap.Config.RGB_565)
//		.displayer(new RoundedBitmapDisplayer(8))
		.build();
		
		initView();
		
		if(bottleModel.getMsgCount()!=0){
			new Thread(new Runnable(){
				public void run() {
						DaoSession daoSession = MyApplication.getApp().getDaoSession();
						bottleModel.setMsgCount(0);
						daoSession.update(bottleModel);
						
						hasChanged = true;
						MyApplication.getApp().getNotificationManager().cancel(0);
				}
			}).start();
		}
	}
	
	public void initView() {
		setContentView(R.layout.fragment_chat);
		findViewById(R.id.my_bottle_back).setOnClickListener(this);
		TextView tv = (TextView) findViewById(R.id.tv_name);
		
		if(!TextUtils.isEmpty(bottleModel.getRecent_nickName())){
			tv.setText(bottleModel.getRecent_nickName());
		}else{
			tv.setText(bottleModel.getNickName());
		}
		
		mEditEmojicon = (EmojiEditText) findViewById(R.id.eet);
		mEditEmojicon.requestFocus();  
		mEditEmojicon.setOnClickListener(this);
		
		mFaceRoot = (EmojiKeyboard) findViewById(R.id.face_ll);
		mFaceRoot.setEventListener(new com.way.ui.emoji.EmojiKeyboard.EventListener() {
			
			@Override
			public void onEmojiSelected(String res) {
				// TODO Auto-generated method stub
				EmojiKeyboard.input(mEditEmojicon, res);
			}
			
			@Override
			public void onBackspace() {
				// TODO Auto-generated method stub
				EmojiKeyboard.backspace(mEditEmojicon);
			}
		});
		
		Button bt_share = (Button) findViewById(R.id.bt_share);
		bt_share.setOnClickListener(this);
		
		if(bottleModel.getBottleType().equals("4000")){
			bt_share.setVisibility(View.INVISIBLE);
		}else if(bottleModel.getBottleType().equals("4001")){
			bt_share.setVisibility(View.VISIBLE);
		}else if(bottleModel.getBottleType().equals("4002")){
			bt_share.setVisibility(View.VISIBLE);
		}else if(bottleModel.getBottleType().equals("4003")){
			bt_share.setVisibility(View.GONE);
		}
		
		bt_send = (ImageView) findViewById(R.id.bt_send);
		bt_send.setOnClickListener(this);
		
		bottom = (LinearLayout) findViewById(R.id.bottom); 
		if(bottleModel.getBottleSort().equals("3000")){
			bottom.setVisibility(View.GONE);
		}else if(bottleModel.getBottleType().equals("3001")){
			bottom.setVisibility(View.VISIBLE);
		}
		
		lv = (ListView) findViewById(R.id.chat_list);
		footView = findViewById(R.id.footview);  
		adapter = new ChatAdapter(imageLoader, options, bottleModel, this);
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
//				mContent = new ChatFragment();
//				getChildFragmentManager()
//				.beginTransaction()
//				.replace(R.id.second_frame, mContent)
//				.commit();
			}
		});
		
		lv.setAdapter(adapter);
		lv.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (view.getFirstVisiblePosition() == 0) {
					if(hasMore && !isloading){
						lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
						isloading = true;
						footView.setVisibility(View.VISIBLE);
						pickDatas();
						
						myHandler.postDelayed(myThread, 200);
					 }
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
	}
	
	public void initData() {
		pageIndex = 0;
		isloading = false;
		hasMore = true;
		adapter = null;
		adapter = new ChatAdapter(imageLoader, options, bottleModel, this);
		lv.setAdapter(adapter);
		
		//获取总页数
		getTotalPage();
		if(hasMore && !isloading){
			isloading = true;
			footView.setVisibility(View.VISIBLE);
			pickDatas();
		 }
	}
	
	int totalPage;
	public int getTotalPage(){
		DaoSession daoSession = MyApplication.getApp().getDaoSession();
		ChatDao chatDao = daoSession.getChatDao();
		QueryBuilder<Chat> qb = chatDao.queryBuilder();
		qb.where(ChatDao.Properties.BottleIdPk.eq(bottleModel.getBottleIdPk()));
		
		long count = qb.count();
		totalPage = (int) Math.ceil((float)count/PAGENUM);
//		System.out.println("totalPage>"+totalPage);
		Log.d("ChatActivity>getTotalPage>totalPage: ", totalPage+"");
		return totalPage;
	}
	
	public class MyTask extends AsyncTask<Void, List<Chat>, List<Chat>> {
		protected void onPreExecute() {
			super.onPreExecute();
			footView.setVisibility(View.VISIBLE);
		}

		@Override
		protected List<Chat> doInBackground(Void... params) {
			DaoSession daoSession = MyApplication.getApp().getDaoSession();
			ChatDao chatDao = daoSession.getChatDao();
			
			QueryBuilder<Chat> qb = chatDao.queryBuilder();
			qb.where(ChatDao.Properties.BottleIdPk.eq(bottleModel.getBottleIdPk()));
			qb.orderDesc(ChatDao.Properties.ChatTime);
			qb.limit(PAGENUM).offset(PAGENUM * pageIndex);
			
			return qb.list();
		}

		@Override
		protected void onPostExecute(List<Chat> list) {
			super.onPostExecute(list);
			isloading = false;
			footView.setVisibility(View.GONE);
			
			if(list != null && list.size() != 0){
				pageIndex ++ ;
				adapter.addItemTop(list);
			}
			lv.setSelection(list.size());
			Message msg = Message.obtain();
			msg.arg1 = FRESH;
			handler.sendMessage(msg);
			hasMore = pageIndex < totalPage;
		}
	}
	
	private void pickDatas(){
		MyTask myTask = new MyTask();
		myTask.execute();
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.my_bottle_back:
				if(hasChanged){
					setResult(RESULT_OK);
				}else{
					setResult(RESULT_CANCELED);
				}
				this.finish();
				break;
			case R.id.bt_share:
				showShare(true, null);
				break;
			case R.id.bt_send:
				if(bottleModel.getState() == 1){
					showMsg("对方删除了瓶子无法收到您的回复,去Ta的空间扔个定向瓶试试看");
				}else{
					msg = mEditEmojicon.getText().toString();
					msg = msg.trim();
					doSubmit();
				}
				break;
			case R.id.iv_userhead:
				int position = lv.getPositionForView(v);
				String userId = adapter.getItem(position).getUserId();
				String visitUserId = UserManager.getInstance(this).getCurrentUserId();
				Intent intent = new Intent(this, VzoneActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				if(userId.equals(visitUserId)){
					intent.putExtra("identityflag", 0);
				}else{
					intent.putExtra("identityflag", 1);
				}
				intent.putExtra("tousuContent", adapter.getLast10Items());
				intent.putExtra("userId", userId);
				intent.putExtra("visitUserId", visitUserId);
				startActivity(intent);
				break;
			case R.id.eet:
				mFaceRoot.setVisibility(View.GONE);
				imm.showSoftInputFromInputMethod(mEditEmojicon.getWindowToken(), 0);
				break;
			case R.id.bt_emoji:
				if(mFaceRoot.getVisibility()==View.GONE){
					imm.hideSoftInputFromWindow(mEditEmojicon.getWindowToken(),0);
					mFaceRoot.setVisibility(View.VISIBLE);
				}else{
					mFaceRoot.setVisibility(View.GONE);
				}
				break;
		}
	}
	
	
	public void doSubmit(){
		String gagTime = MyApplication.getApp().getSpUtil().getGagTime();
		if(!TextUtils.isEmpty(gagTime)){
			MoMaLog.e("禁言时间", gagTime);
			try {
				Date dateFrom = sdf.parse(gagTime);
				Date dateTo = new Date(System.currentTimeMillis());//获取当前时间
				
				int day = MoMaUtil.getGapCount(dateTo, dateFrom);
				
				if(day>0){
					showMsg("经举报并核实，您的言论存在多次违规已被禁言,离解禁还有"+day+"天");
					return ;
				}else{
					MyApplication.getApp().getSpUtil().setGagTime("");
				}
			} catch (ParseException e) {
				MoMaLog.e("禁言", e.getMessage());
			}
			
		}
		
		if(msg.length()==0){
			showMsg("发送内容不能为空");
		}else{
			if(UserManager.getInstance(this).getCurrentUser()==null){
				return;
			}
			
			JsonObject jo = new JsonObject();
			jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
			jo.addProperty("bottleId", bottleModel.getBottleId());
			jo.addProperty("ubId", bottleModel.getUbId());
			jo.addProperty("content", msg);
			jo.addProperty("contentType", "5000");
			jo.addProperty("dealType", "100");
			
			BottleRestClient.post("dealBottle", this, jo, new AsyncHttpResponseHandler(){
				@Override
				public void onStart() {
					super.onStart();
					MoMaLog.d("dealBottle", "onStart");
					footView.setVisibility(View.VISIBLE);
					bt_send.setVisibility(View.GONE);
					mEditEmojicon.setClickable(false);
					mEditEmojicon.setEnabled(false);
				}
				
				@Override
				public void onFinish() {
					super.onFinish();
					MoMaLog.d("dealBottle", "onFinish");
					footView.setVisibility(View.GONE);
					bt_send.setVisibility(View.VISIBLE);
					mEditEmojicon.setClickable(true);
					mEditEmojicon.setEnabled(true);
				}

				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					
					String str = new String(responseBody);
					
					MoMaLog.d("dealBottle", "onSuccess: "+str);
					
					if(!TextUtils.isEmpty(str)){
						Gson gson = new Gson();
						DealBottleModel model = gson.fromJson(str, DealBottleModel.class);
						if(model != null && !TextUtils.isEmpty(model.getCode())){
							if("100005".equals(model.getCode())){
								DaoSession daoSession = MyApplication.getApp().getDaoSession();
								Bottle bottle = daoSession.load(Bottle.class, bottleModel.getBottleIdPk());
								if(bottle.getState()!=1){
									bottle.setState(1);
									daoSession.update(bottle);
								}
								
								bottleModel.setState(1);
								showMsg("对方删除了瓶子无法收到您的回复,去Ta的空间打招呼");
							}else if("100015".equals(model.getCode())){//禁言
								if(!TextUtils.isEmpty(model.getMsg())){
									MoMaLog.e("禁言时间", model.getMsg());
									try {
										SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
										Date dateFrom = sdf.parse(model.getMsg());
										Date dateTo = new Date(System.currentTimeMillis());//获取当前时间
										
										int day = MoMaUtil.getGapCount(dateTo, dateFrom);
										showMsg("经举报并核实，您的言论存在多次违规已被禁言,离解禁还有"+day+"天");
										
										MyApplication.getApp().getSpUtil().setGagTime(model.getMsg());
									} catch (ParseException e) {
										MoMaLog.e("禁言", e.getMessage());
									}
									
								}
							}else{
								DaoSession daoSession = MyApplication.getApp().getDaoSession();
								//对话
								Chat chat = new Chat();
								Date data = model.getDealTime();
								if(data == null){
									data = new Date();
								}
								chat.setChatTime(data);
								chat.setContent(msg);
								chat.setContentType("5000");
								chat.setHeadImg(UserManager.getInstance(SysChatActivity.this).getCurrentUser().getHeadImg());
								chat.setNickName(UserManager.getInstance(SysChatActivity.this).getCurrentUser().getNickName());
								chat.setUserId(UserManager.getInstance(SysChatActivity.this).getCurrentUser().getUserId());
								chat.setBottleIdPk(bottleModel.getBottleIdPk());
								chat.setIsContent(false);
								daoSession.insert(chat);
								
								//最近对话列表
								Bottle bottle = daoSession.load(Bottle.class, bottleModel.getBottleIdPk());
								bottle.setMessageType(chat.getContentType());
								bottle.setMessage(chat.getContent());
								daoSession.update(bottle);
								
								adapter.addOneItem2Foot(chat);
								mEditEmojicon.setText("");
								lv.setSelection(lv.getCount() - 1);
								Message msg = Message.obtain();
								msg.arg1 = FRESH;
								handler.sendMessage(msg);
								
								hasChanged = true;
							}
						}else{
							showMsg("服务器繁忙");
						}
						}else{
							showMsg("服务器繁忙");
						}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					
					String str1 = "";
					if(responseBody!=null){
						try {
							str1 = new String(responseBody, "UTF-8");
						} catch (UnsupportedEncodingException e) {
						}
					}
					
					String str2 = "";
					if(error!=null && !TextUtils.isEmpty(error.getMessage())){
						str2 = error.getMessage();
					}
					
					MoMaLog.d("dealBottle", "onFailure: responseBody: "+str1 +" ,ThrowableError:"+str2);
					
					showMsg("发送失败");
				}
	        });
		}
	}
	
	
	// 使用快捷分享完成分享（请务必仔细阅读位于SDK解压目录下Docs文件夹中OnekeyShare类的JavaDoc）
	private void showShare(boolean silent, String platform) {
		if(bottleModel.getContentType().equals("5000")){
			textFrameShare(silent, platform);
		}else if(bottleModel.getContentType().equals("5001")){
		}else if(bottleModel.getContentType().equals("5002")){
			voiceFrameShare(silent, platform);
		}else if(bottleModel.getContentType().equals("5003")){
		}
	}
	
	private void textFrameShare(boolean silent, String platform){
		final OnekeyShare oks = new OnekeyShare();
//		oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
//				address是接收人地址，仅在信息和邮件使用，否则可以不提供
		oks.setAddress("12345678901");
//				title标题，在印象笔记、邮箱、信息、微信（包括好友和朋友圈）、人人网和QQ空间使用，否则可以不提供
		oks.setTitle(MyConstants.SHARE_TITLE);
//				titleUrl是标题的网络链接，仅在人人网和QQ空间使用，否则可以不提供
		oks.setTitleUrl(MyConstants.SHARE_URL + bottleModel.getBottleId());
//				text是分享文本，所有平台都需要这个字段
		String cnt = bottleModel.getContent();
		if(!TextUtils.isEmpty(cnt)){
			if(cnt.length()>100){
				cnt=cnt.substring(0, 100)+"...";
			}
		}
		oks.setText(cnt +" "+ MyConstants.SHARE_URL + bottleModel.getBottleId() + " （来自#漂流瓶子#）");
//		imagePath是本地的图片路径，除Linked-In外的所有平台都支持这个字段
//		oks.setImagePath("/sdcard/pic.jpg");
//				imageUrl是图片的网络路径，新浪微博、人人网、QQ空间和Linked-In支持此字段
		oks.setImageUrl(MyConstants.BOTTLE01_URL);
//				url仅在微信（包括好友和朋友圈）中使用，否则可以不提供
		oks.setUrl(MyConstants.SHARE_URL + bottleModel.getBottleId());
//				filePath是待分享应用程序的本地路劲，仅在微信好友和Dropbox中使用，否则可以不提供
//				oks.setFilePath(MainActivity.TEST_IMAGE);
//				comment是我对这条分享的评论，仅在人人网和QQ空间使用，否则可以不提供
		oks.setComment("分享");
//				site是分享此内容的网站名称，仅在QQ空间使用，否则可以不提供
		oks.setSite(getString(R.string.app_name));
		oks.setSiteUrl(MyConstants.MYWEB_URL);
//				foursquare分享时的地方描述
//				foursquare分享时的地方名
//				oks.setVenueName("Share SDK");
//				oks.setVenueDescription("This is a beautiful place!");
//				分享地纬度，新浪微博、腾讯微博和foursquare支持此字段
//				oks.setLatitude(23.056081f);
//				oks.setLongitude(113.385708f);
		oks.setSilent(silent);
		if (platform != null) {
			oks.setPlatform(platform);
		}

		// 去除注释，可令编辑页面显示为Dialog模式
//				oks.setDialogMode();

		// 去除注释，则快捷分享的操作结果将通过OneKeyShareCallback回调
		oks.setShareContentCustomizeCallback(this);
		oks.setCallback(this);

		// 去除注释，演示在九宫格设置自定义的图标
//				Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
//				String label = getResources().getString(R.string.app_name);
//				OnClickListener listener = new OnClickListener() {
//					public void onClick(View v) {
//						String text = "Customer Logo -- Share SDK " + ShareSDK.getSDKVersionName();
//						Toast.makeText(ChatActivity.this, text, Toast.LENGTH_SHORT).show();
//						oks.finish();
//					}
//				};
//				oks.setCustomerLogo(logo, label, listener);

		oks.show(this);
	}
	
	private void voiceFrameShare(boolean silent, String platform){
		final OnekeyShare oks = new OnekeyShare();
//				address是接收人地址，仅在信息和邮件使用，否则可以不提供
		oks.setAddress("12345678901");
//				title标题，在印象笔记、邮箱、信息、微信（包括好友和朋友圈）、人人网和QQ空间使用，否则可以不提供
		oks.setTitle(MyConstants.SHARE_TITLE);
//				titleUrl是标题的网络链接，仅在人人网和QQ空间使用，否则可以不提供
		oks.setTitleUrl(bottleModel.getRedirectUrl());
//				text是分享文本，所有平台都需要这个字段
		String cnt = bottleModel.getContent();
		if(!TextUtils.isEmpty(cnt)){
			if(cnt.length()>100){
				cnt=cnt.substring(0, 100)+"...";
			}
		}
		oks.setText(cnt +" "+ bottleModel.getRedirectUrl() + " （来自#漂流瓶子#）");
//		oks.setImagePath("/sdcard/pic.jpg");
//				imageUrl是图片的网络路径，新浪微博、人人网、QQ空间和Linked-In支持此字段
		oks.setImageUrl(bottleModel.getShortPic());
//				url仅在微信（包括好友和朋友圈）中使用，否则可以不提供
		oks.setUrl(bottleModel.getRedirectUrl());
//				comment是我对这条分享的评论，仅在人人网和QQ空间使用，否则可以不提供
		oks.setComment("分享");
//				site是分享此内容的网站名称，仅在QQ空间使用，否则可以不提供
		oks.setSite(getString(R.string.app_name));
		oks.setSiteUrl(MyConstants.MYWEB_URL);
		oks.setSilent(silent);
		if (platform != null) {
			oks.setPlatform(platform);
		}

		// 去除注释，则快捷分享的操作结果将通过OneKeyShareCallback回调
		oks.setShareContentCustomizeCallback(this);
		oks.setCallback(this);
		oks.show(this);
	}

	/** 处理操作结果 */
	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.arg1) {
			case 1: 
//				closeDialog();
				showMsg("分享成功");
				break;
			case 2: 
//				closeDialog();
				showMsg("分享失败");
				break;
			case 3: 
//				closeDialog();
				showMsg("用户取消");
				break;
			case FRESH:
				adapter.notifyDataSetChanged();
				break;
		}
		return false;
	}
	
	@Override
	public void onShare(Platform platform, ShareParams paramsToShare) {
		//IQ瓶子分享后可看答案
		if(bottleModel.getBottleType().equals("4002")){
			DaoSession daoSession = MyApplication.getApp().getDaoSession();
			Bottle bottle = daoSession.load(Bottle.class, bottleModel.getBottleIdPk());
			bottle.setHasAnswer(true);
			daoSession.update(bottle);
			bottleModel.setHasAnswer(true);
			
			Message msg = Message.obtain();
			msg.arg1 = FRESH;
			handler.sendMessage(msg);
		}
		
		if (Wechat.NAME.equals(platform.getName()) || WechatMoments.NAME.equals(platform.getName())) {
			String title = paramsToShare.getText();
			if(!TextUtils.isEmpty(title)){
				title = title.substring(0, 10);
				paramsToShare.setTitle(title);
			}
		}
	}
	
	@Override
	public void onCancel(Platform platform, int action) {
		Message msg = new Message();
		msg.arg1 = 3;
		msg.arg2 = action;
		msg.obj = platform;
		handler.sendMessage(msg);
	}

	@Override
	public void onComplete(Platform platform, int action, HashMap<String, Object> arg2) {
		Message msg = new Message();
		msg.arg1 = 1;
		msg.arg2 = action;
		msg.obj = platform;
		handler.sendMessage(msg);
	}

	@Override
	public void onError(Platform platform, int action, Throwable arg2) {
		MoMaLog.d("ChatActivity>share>onError: ", arg2.getMessage());
		Message msg = new Message();
		msg.arg1 = 2;
		msg.arg2 = action;
		msg.obj = platform;
		handler.sendMessage(msg);
	}
	
	@Override
	public void startActivity(Intent intent) {
		try {
	        /* First attempt at fixing an HTC broken by evil Apple patents. */
	        if (intent.getComponent() != null && ".HtcLinkifyDispatcherActivity".equals(intent.getComponent().getShortClassName()))
	            intent.setComponent(null);
	        super.startActivity(intent);
	    } catch (ActivityNotFoundException e) {
	        /*
	         * Probably an HTC broken by evil Apple patents. This is not perfect,
	         * but better than crashing the whole application.
	         */
	        super.startActivity(Intent.createChooser(intent, null));
	    }
	}
	
	@Override
	public void onBackPressed() {
		if(mFaceRoot.getVisibility()!=View.GONE){
			mFaceRoot.setVisibility(View.GONE);
		}else{
			if(mContent!=null){
	    		if(mContent.onBackPressed()){
	    			mContent = null;
	    		}
			}else{
				if(hasChanged){
					setResult(RESULT_OK);
				}else{
					setResult(RESULT_CANCELED);
				}
				this.finish();
			}
		}
	}
	
    @Override
    protected void onDestroy() {
    	myHandler.removeCallbacks(myThread);
    	super.onDestroy();
    }
    
	@Override
	public Object[] onFragmentInit(BaseFragment fragment) {
		Object[] objs = new Object[3];
		objs[0] = FrameShowCallBack.COMEFROMCHAT;
		objs[1] = bottleModel;
		objs[2] = adapter.getItem(0);
		
		return objs;
	}
	
	@Override
	public void onTaskEnd() {
		bottleModel.setState(2);
		
		DaoSession daoSession = MyApplication.getApp().getDaoSession();
		Bottle bottle = daoSession.load(Bottle.class, bottleModel.getBottleIdPk());
		bottle.setState(2);
		daoSession.update(bottle);
		
		showMsg("恭喜获得5个瓶子的奖励");
	}
	
	@Override
    protected void onResume() {
    	super.onResume();
    	//刷新消息
    	initData();
	}
}