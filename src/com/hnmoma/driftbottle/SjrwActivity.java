package com.hnmoma.driftbottle;

import java.util.HashMap;
import java.util.Random;

import org.apache.http.Header;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.adapter.SjrwAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.WxFloatWindowManager;
import com.hnmoma.driftbottle.model.SinaTopicModel;
import com.hnmoma.driftbottle.model.SjrwModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class SjrwActivity extends BaseActivity implements PlatformActionListener, Callback{
	
	ListView actualListView;
	SjrwAdapter adapter;
	String sinaTopic;
	String pyquan;
	Handler handler;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		//为了避免出现异常，在这里再一次调用该函数
		ShareSDK.initSDK(this);
		handler = new Handler(this);
		initView();
	}
	
	private void initView(){
		setContentView(R.layout.activity_sjrw);
		actualListView = (ListView) findViewById(R.id.mylv);
		
		registerForContextMenu(actualListView);
		
		adapter = new SjrwAdapter(this, getVersionCode());
		actualListView.setAdapter(adapter);
		
		getSinaTopic(true);
	}
	
	private void doTask(SjrwModel sjm){
		if(sjm.getTaskId().equals("stask1000")){
			// 关注新浪微博
			Platform plat = ShareSDK.getPlatform(this, SinaWeibo.NAME);
			plat.setPlatformActionListener(this);
			
			String msg = getRandomMsg(0);
			showShare(true, plat.getName(), msg);
		}else if(sjm.getTaskId().equals("stask1001")){
			// 分享到微信朋友圈
			String msg = getRandomMsg(1);
//			showMsg(msg);
//			showMsg(pyquan);
			
			ShareParams sp = new ShareParams();
			sp.setTitle(msg);
			sp.setText(msg);
			sp.setImageUrl(pyquan);
			sp.setShareType(Platform.SHARE_WEBPAGE);
			sp.setUrl("http://a.app.qq.com/o/simple.jsp?pkgname=com.hnmoma.driftbottle");
			
			Platform plat = ShareSDK.getPlatform(this, WechatMoments.NAME);
			plat.setPlatformActionListener(this);
			plat.share(sp);
		}else if(sjm.getTaskId().equals("stask1003")){
			//打开微信，再弹出系统框
			try{
				Intent intent = new Intent();
				ComponentName cmp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
				intent.setAction(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setComponent(cmp);
				startActivityForResult(intent, 400);
				
				//打开复制面板
				WxFloatWindowManager.createSmallWindow();
				
			}catch(Exception e){
				showMsg("该手机未装微信或版本过低");
			}
		}else if(sjm.getTaskId().equals("stask1004")){
			MyApplication.getApp().getSpUtil().setLastVersion("stask1004", getVersionCode());
			SjrwModel sjrwm = adapter.getItem(3);
			sjrwm.setCanClick(false);
			adapter.notifyDataSetChanged();
			getTaskJL("stask1004");
		}
	}
	// 只有新浪微博会调用这里
	private void showShare(boolean silent, String platform, String msg) {
		OnekeyShare oks = new OnekeyShare();
//		oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
//				address是接收人地址，仅在信息和邮件使用，否则可以不提供
		//oks.setAddress("12345678901");
//				title标题，在印象笔记、邮箱、信息、微信（包括好友和朋友圈）、人人网和QQ空间使用，否则可以不提供
		oks.setTitle(msg);
//				titleUrl是标题的网络链接，仅在人人网和QQ空间使用，否则可以不提供
		oks.setTitleUrl("www.52plp.com");
//				text是分享文本，所有平台都需要这个字段
		String cnt = msg;
		if(!TextUtils.isEmpty(cnt)){
			if(cnt.length()>140){
				cnt=cnt.substring(0, 140)+"...";
			}
		}
		cnt = cnt + " http://www.52plp.com " + " #漂流瓶子# " + sinaTopic;
		oks.setText(cnt);
//		imagePath是本地的图片路径，除Linked-In外的所有平台都支持这个字段
//		oks.setImagePath("/sdcard/pic.jpg");
//				imageUrl是图片的网络路径，新浪微博、人人网、QQ空间和Linked-In支持此字段
		oks.setImageUrl("http://52plp.com/share/sina.jpg");
//				url仅在微信（包括好友和朋友圈）中使用，否则可以不提供
		oks.setUrl("www.52plp.com");
//				filePath是待分享应用程序的本地路劲，仅在微信好友和Dropbox中使用，否则可以不提供
//				oks.setFilePath(MainActivity.TEST_IMAGE);
//				comment是我对这条分享的评论，仅在人人网和QQ空间使用，否则可以不提供
		oks.setComment("精品推荐");
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
//		oks.setShareContentCustomizeCallback(this);
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
	
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_back:
				this.finish();
				break;
			case R.id.tv_bt:
				int position = actualListView.getPositionForView(v);
				SjrwModel sjm = adapter.getItem(position);
				if(sjm.isCanClick()){
					doTask(sjm);
				}else{
					showMsg("该任务已完成^_^");
				}
				break;
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
		if(action == Platform.ACTION_SHARE){
			if(platform.getName().equals(SinaWeibo.NAME)){
				platform.followFriend(MyConstants.SDK_SINAWEIBO_UID);
			}
			Message msg = new Message();
			msg.arg1 = 1;
			msg.arg2 = action;
			msg.obj = platform;
			handler.sendMessage(msg);
		}
	}

	@Override
	public void onError(Platform platform, int action, Throwable arg2) {
		if(action != Platform.ACTION_FOLLOWING_USER){
			if(!TextUtils.isEmpty(arg2.getMessage())){
				Log.d("SjrwActivity:onError: ", arg2.getMessage());
			}
			Message msg = new Message();
			msg.arg1 = 2;
			msg.arg2 = action;
			msg.obj = platform;
			handler.sendMessage(msg);
		}
	}

	/** 处理操作结果 */
	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.arg1) {
			case 1: 
				showMsg("分享成功");
				Platform platform = (Platform) msg.obj;
				String name = platform.getName();
				if(name.equals(SinaWeibo.NAME)){
					MyApplication.getApp().getSpUtil().setLastDateById("stask1000");
					SjrwModel sjrwm = adapter.getItem(0);
					sjrwm.setLastTime(System.currentTimeMillis());
					sjrwm.setCanClick(false);
					adapter.notifyDataSetChanged();
					
					getTaskJL("stask1000");
				}else if(name.equals(WechatMoments.NAME)){
					MyApplication.getApp().getSpUtil().setLastDateById("stask1001");
					SjrwModel sjrwm = adapter.getItem(1);
					sjrwm.setLastTime(System.currentTimeMillis());
					sjrwm.setCanClick(false);
					adapter.notifyDataSetChanged();
//					showMsg("stask1001");
					getTaskJL("stask1001");
				}
				
				break;
			case 2: 
				showMsg("分享失败");
				break;
			case 3: 
				showMsg("取消分享");
				break;
		}
		return false;
	}
	
	//获取任务奖励
		private void getTaskJL(String type){
			JsonObject jo = new JsonObject();
			jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
			jo.addProperty("deviceId", getDeviceId());
			jo.addProperty("type", type);
			jo.addProperty("come", "6000");
			jo.addProperty("version", getVersionCode());
			jo.addProperty("channel", getChannel());
			
			BottleRestClient.post("doTask", this, jo, new AsyncHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					//获取奖励，重置余额
					MyApplication.getApp().getSpUtil().setMyMoney(0);
//					String str = new String(responseBody);
//					if(!TextUtils.isEmpty(str)){
//						Gson gson = new Gson();
//						BaseModel baseModel = gson.fromJson(str, BaseModel.class);
//						if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
//							if("0".equals(baseModel.getCode())){
//								showMsg("登出成功");
//								spu.logOut();
//								rl_dl.setVisibility(View.GONE);
//								reflesh();
//							}
//						}else{
//							showMsg("登出失败");
//						}
//					}else{
//						showMsg("登出失败");
//					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//					showMsg("登出失败");
				}
				
				@Override
				public void onFinish() {
					super.onFinish();
				}
	        });
	}
	/**
	 * 	
	 * @param isRefresh 如果isRefresh是true，表示刷新，否则表示加载更多
	 */
	private void getSinaTopic(final boolean isRefresh) {
			JsonObject obj = new JsonObject();
			obj.addProperty("bottleId", "0");
			BottleRestClient.post("querySinaShare", this, obj,  new AsyncHttpResponseHandler(){

				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					String response = new String(responseBody);
					Gson gson = new Gson();
					SinaTopicModel baseModel = gson.fromJson(response, SinaTopicModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode()) ) {
						if("0".equals(baseModel.getCode())) {
							sinaTopic = " #" + baseModel.getTopicContent() +"#";
						}
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					sinaTopic = "";
				}
				
			});
	}
		
	
	private String getRandomMsg(int index){
		String[][] result = {{"画面太美！我捞的漂流瓶里全是小鲜肉~你也来捞捞看？",
										"人丑就该多读书，有时间刷微博还不如捞一瓶妹纸？",
										"你造吗？有兽为直在想…要漂多少瓶子，才会遇见你？" },
										{"画面太美！我捞的漂流瓶里全是小鲜肉~你也来捞捞看？",
											"人丑就该多读书，有时间刷微博还不如捞一瓶妹纸？",
											"你造吗？有兽为直在想…要漂多少瓶子，才会遇见你？" }};
														 
		Random random = new Random();
		int randomInt = random.nextInt(3);
		if(index == 1) {
			pyquan = "http://52plp.com/share/wx"+ (randomInt+1) +".jpg";
		}
		return result[index][randomInt];
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 400){//微信公众号
			MyApplication.getApp().getSpUtil().setLastDateById("stask1003");
			SjrwModel sjrwm = adapter.getItem(2);
			sjrwm.setLastTime(System.currentTimeMillis());
			sjrwm.setCanClick(false);
			adapter.notifyDataSetChanged();
			getTaskJL("stask1003");
		}
	}
}