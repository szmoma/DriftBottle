package com.hnmoma.driftbottle;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.http.Header;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Application;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Environment;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.ConnectionListener;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.EMMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.BottleMsgManager;
import com.hnmoma.driftbottle.business.MyNotificationManager;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.exception.ExceptionMonitor;
import com.hnmoma.driftbottle.model.DaoMaster;
import com.hnmoma.driftbottle.model.DaoMaster.OpenHelper;
import com.hnmoma.driftbottle.model.DaoSession;
import com.hnmoma.driftbottle.model.QueryUserInfoModel;
import com.hnmoma.driftbottle.model.Stranger;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.net.UpdateCacheOfUpperLimit;
import com.hnmoma.driftbottle.sensitivewords.SensitivewordFilter;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.hnmoma.driftbottle.util.MyConstants.Config;
import com.hnmoma.driftbottle.util.SharePreferenceUtil;
import com.hnmoma.driftbottle.util.SysSharePreferenceUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class MyApplication extends Application {
	
	  //是否home等挂起过
	  public static boolean isPending = false;
	
	  DaoMaster daoMaster;  
	  DaoSession daoSession;  
	
	  private static MyApplication app;
	  public static String mAppPath;
	  public float pixHeight;
	  public float pixWidth;
	  
	  //保存的基本信息，初始化一次
	  public static String deviceId;
	  public static String channel;
	  public static String version;
	  
	  //====================发布处理参数==========================//
	  /**
	   * 开发模式开关 ：false为发布模式，true为测试模式
	   */
	  public static boolean DEVELOPER_MODE = false;
		/**
		 * 特权渠道
		 */
	  public static String[] TQ_CHANNELS = {""};
		/**
		 * 特权结束时间(给定的最后期限是没有特权的)
		 */
	  public static String TQ_ENDDATE = "";
	  //====================发布处理参数==========================//

	 static{
	    File localFile = Environment.getExternalStorageDirectory();
	    mAppPath = localFile + "/DriftBottle/";
	    app = null;
	 }
	 
	 // 单例模式，才能及时返回数据
	 SharePreferenceUtil mSpUtil;
	 SysSharePreferenceUtil mSysSpUtil;
	 public static final String PREFERENCE_NAME = "_sharedinfo";
	 public static final String SYS_PREFERENCE_NAME = "sys_sharedinfo";
	 MediaPlayer mMediaPlayer;
	 NotificationManager mNotificationManager;
	 SensitivewordFilter sensitivewordFilter;
	 
	@Override
	public void onCreate() {
		super.onCreate();
		
		int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);
        // 如果使用到百度地图或者类似启动remote service的第三方库，这个if判断不能少
        if (TextUtils.isEmpty(processAppName)||!processAppName.equalsIgnoreCase("com.hnmoma.driftbottle")) {
            // workaround for baidu location sdk
            // 百度定位sdk，定位服务运行在一个单独的进程，每次定位服务启动的时候，都会调用application::onCreate
            // 创建新的进程。
            // 但环信的sdk只需要在主进程中初始化一次。 这个特殊处理是，如果从pid 找不到对应的processInfo
            // processName，
            // 则此application::onCreate 是被service 调用的，直接返回
            return;
        }
		
		if (Config.DEVELOPER_MODE ) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
		}
		app = this;
		
		initImageLoader(getApplicationContext());
		if(!DEVELOPER_MODE){
			initExceptionMonitor(getApplicationContext());
		}
		
		initEmChat();
		initSensitiveWords();
	}
	
	private void initSensitiveWords(){
		SensitivewordFilter filter = new SensitivewordFilter();
		MoMaLog.d("敏感词的数量：", filter.sensitiveWordMap.size()+"");
	}
	
	public synchronized SensitivewordFilter getSensitiveFilter() {
 		if (sensitivewordFilter == null) {
 			sensitivewordFilter = new SensitivewordFilter();
 		}
 		return sensitivewordFilter;
 	}
	
	private void initEmChat(){
		// 初始化环信SDK,一定要先调用init()
				EMChat.getInstance().init(app);
				EMChat.getInstance().setDebugMode(DEVELOPER_MODE);
				MoMaLog.d("EMChat Demo", "initialize EMChat SDK");
				// debugmode设为true后，就能看到sdk打印的log了

				// 获取到EMChatOptions对象
				EMChatOptions options = EMChatManager.getInstance().getChatOptions();
				// 默认环信是不维护好友关系列表的，如果app依赖环信的好友关系，把这个属性设置为true
//				options.setUseRoster(true);
				options.setRequireAck(true);
				options.setRequireDeliveryAck(true);
//				// 默认添加好友时，是不需要验证的，改成需要验证
//				options.setAcceptInvitationAlways(false);
//				// 设置收到消息是否有新消息通知，默认为true
				options.setNotifyBySoundAndVibrate(false);
//				// 设置收到消息是否有声音提示，默认为true
//				options.setNoticeBySound(PreferenceUtils.getInstance(applicationContext).getSettingMsgSound());
//				// 设置收到消息是否震动 默认为true
				options.setNoticedByVibrate(false);
//				// 设置语音消息播放是否设置为扬声器播放 默认为true
//				options.setUseSpeaker(PreferenceUtils.getInstance(applicationContext).getSettingMsgSpeaker());
				options.setShowNotificationInBackgroud(false);
				
//				// 设置notification消息点击时，跳转的intent为自定义的intent
//				options.setOnNotificationClickListener(new OnNotificationClickListener() {
//
//					@Override
//					public Intent onNotificationClick(EMMessage message) {
//						Intent intent = new Intent(app, ChatActivity.class);
//						ChatType chatType = message.getChatType();
//						if (chatType == ChatType.Chat) { // 单聊信息
//							intent.putExtra("userId", message.getFrom());
//							intent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
//						} else { // 群聊信息
//									// message.getTo()为群聊id
//							intent.putExtra("groupId", message.getTo());
//							intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
//						}
//						return intent;
//					}
//				});
				// 设置一个connectionlistener监听账户重复登陆
				EMChatManager.getInstance().addConnectionListener(new MyConnectionListener());
//				// 取消注释，app在后台，有新消息来时，状态栏的消息提示换成自己写的
//				options.setNotifyText(new OnMessageNotifyListener() {
		//
//					@Override
//					public String onNewMessageNotify(EMMessage message) {
//						// 可以根据message的类型提示不同文字(可参考微信或qq)，demo简单的覆盖了原来的提示
//						return "你的好基友" + message.getFrom() + "发来了一条消息哦";
//					}
		//
//					@Override
//					public String onLatestMessageNotify(EMMessage message, int fromUsersNum, int messageNum) {
//						return fromUsersNum + "个基友，发来了" + messageNum + "条消息";
//					}
		//
//					@Override
//					public String onSetNotificationTitle(EMMessage message) {
//						//修改标题
//						return "环信notification";
//					}
		//
		//
//				});
				
				// 注册一个接收消息的BroadcastReceiver
				msgReceiver = new NewMessageBroadcastReceiver();
				IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
				intentFilter.setPriority(1000);
				registerReceiver(msgReceiver, intentFilter);
				
				// 注册一个cmd消息的BroadcastReceiver
				IntentFilter cmdIntentFilter = new IntentFilter(EMChatManager.getInstance().getCmdMessageBroadcastAction());
				cmdIntentFilter.setPriority(1000);
				registerReceiver(cmdMessageReceiver, cmdIntentFilter);
	}
	
	/**
	 * cmd消息BroadcastReceiver
	 */
	private BroadcastReceiver cmdMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//获取cmd message对象
//			String msgId = intent.getStringExtra("msgid");
			EMMessage message = intent.getParcelableExtra("message");
			//获取消息body
			CmdMessageBody cmdMsgBody = (CmdMessageBody) message.getBody();
			String aciton = cmdMsgBody.action;//获取自定义action
//			//获取扩展属性
			if(aciton.equals(MyConstants.MESSAGE_ACTION_DELBOTTLE)){
				//瓶子消息保存后加一条系统消息（对方收藏了瓶子，已建立连接/对方删除了瓶子无法建立联系）
				BottleMsgManager.getInstance(app).addMsgNotice(2, message.getFrom(),message.getStringAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, ""));
			} else if(aciton.equals(MyConstants.MESSAGE_ACTION_VZONETYPE)) {
				// 空间消息来了
				
			}else if(MyConstants.UPDATEGAMESTATUS.equals(aciton)){
				//更新聊天中的游戏状态
				String userId = message.getFrom();//这里的接收者是自己
				BottleMsgManager.getInstance(app).updateGameMessageOfChat(message,userId);
			}else if(MyConstants.MESSAGE_ACTION_BOTTLE.equals(aciton)){
				MyApplication.getApp().getSpUtil().setMsgRecord(true);
			}else if(MyConstants.MESSAGE_ATTR_ISSAYHELLO.equals(aciton)){
				//打招呼的消息
				BottleMsgManager.getInstance(app).insertMessageOfHello(message);
			}
		}
	};
	
	private NewMessageBroadcastReceiver msgReceiver;
	/**
	 * 消息广播接收者
	 * 
	 */
	private class NewMessageBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			
//			String username = intent.getStringExtra("from");
			String msgid = intent.getStringExtra("msgid");
			// 收到这个广播的时候，message已经在db和内存里了，可以通过id获取mesage对象
			EMMessage message = EMChatManager.getInstance().getMessage(msgid);
			if(message==null)
				return ;
			boolean isSayHello = message.getBooleanAttribute(MyConstants.MESSAGE_ATTR_ISSAYHELLO, false);
			if(isSayHello) {
				String fromUserId = message.getFrom();
				Stranger mStranger = MyApplication.getApp().getDaoSession().getStrangerDao().load(fromUserId);
				if(mStranger==null){
					queryUserInfo(fromUserId,1);
				}else{
					String headUrl = message.getStringAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, "");
					//没有联通
					if(mStranger.getState()!=1||!headUrl.equals(mStranger.getHeadImg())){
						boolean isChange = false;
						if(mStranger.getState()!=1){
							mStranger.setState(1);
							isChange = true;
						}
						if(!headUrl.equals(mStranger.getHeadImg())){
							mStranger.setHeadImg(headUrl);
							isChange = true;
						}
						if(isChange)
							MyApplication.getApp().getDaoSession().getStrangerDao().update(mStranger);
					}
				}
//				
				return;
			} else {
				MyNotificationManager.getInstance(context).newMsgNotice("您有新消息，请注意查收", 3);
//				如果是瓶子，就把陌生人对象和瓶子消息对象拿出来保存
				if (message.getIntAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 0) == 1){
//					判断瓶子类型：0为捡起的瓶子，1为定向道具瓶子
					if (message.getIntAttribute(MyConstants.MESSAGE_ATTR_BOTTLETYPE, 0) == 0){
						//瓶子消息保存后加一条系统消息（对方收藏了瓶子，已建立连接/对方删除了瓶子无法建立联系）
						BottleMsgManager.getInstance(app).addMsgNotice(1, message.getFrom(), message.getStringAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, ""));
					}
				}else{
					String fromUserId = message.getFrom();
					Stranger mStranger = MyApplication.getApp().getDaoSession().getStrangerDao().load(fromUserId);
					if(mStranger==null){
						queryUserInfo(fromUserId,1);
					}else{
						String headUrl = message.getStringAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, "");
						//没有联通
						boolean isChange = false;
						if(mStranger.getState()!=1){
							mStranger.setState(1);
							isChange = true;

						}
						if(headUrl.equals(mStranger.getHeadImg())){
							mStranger.setHeadImg(headUrl);
							isChange = true;
						}
						if(isChange)
							MyApplication.getApp().getDaoSession().getStrangerDao().update(mStranger);
					}
				}
				
				if("1006".equals(message.getStringAttribute("type", "0"))){
					//VIP充值成功，更改数据库中信息，更新用户是VIP以及刷新缓存
					UserManager.getInstance(getApplicationContext()).updateUserInfo(true);
					Integer flag = UserManager.getInstance(getApplicationContext()).getCurrentUser().getIsVIP();
					UpdateCacheOfUpperLimit.getInstance().cache(flag==null?1:flag.intValue(),getApplicationContext());
				}
			}
			
		}
	}
	
	public static MyApplication getApp(){
	    return app;
	}
	
	public static void initImageLoader(Context context) {
//		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
//		.threadPriority(Thread.NORM_PRIORITY - 2)
//		.denyCacheImageMultipleSizesInMemory()
//		.discCacheFileNameGenerator(new HashCodeFileNameGenerator())
//		.tasksProcessingOrder(QueueProcessingType.FIFO)
//		.discCache(new FileCountLimitedDiscCache(new File(mAppPath, ".cache"), 500)) 
//		.build();
//	
//		// Initialize ImageLoader with configuration.
//		ImageLoader.getInstance().init(config);
		
		File cacheDir = StorageUtils.getOwnCacheDirectory(context, "driftbottle/cache");// 获取到缓存的目录地址
		// 创建配置ImageLoader(所有的选项都是可选的,只使用那些你真的想定制)，这个可以设定在APPLACATION里面，设置为全局的配置参数
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
		// 线程池内加载的数量
		.threadPoolSize(3)
		.threadPriority(Thread.NORM_PRIORITY - 2)
		.memoryCache(new WeakMemoryCache())
		.denyCacheImageMultipleSizesInMemory()
		.discCacheFileNameGenerator(new Md5FileNameGenerator())
		// 将保存的时候的URI名称用MD5 加密
		.tasksProcessingOrder(QueueProcessingType.LIFO)
		.discCache(new UnlimitedDiscCache(cacheDir))// 自定义缓存路径
		// .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
		.writeDebugLogs() // Remove for release app
		.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);// 全局初始化此配置
	}
	
	private static void initExceptionMonitor(Context context) {
		ExceptionMonitor exceptionMonitor = ExceptionMonitor.getInstance();
		exceptionMonitor.init(context);
	}
	
	/** 
     * 取得DaoMaster 
     *  
     * @param context 
     * @return 
     */  
    public synchronized DaoMaster getDaoMaster() {  
        if (daoMaster == null) {  
            OpenHelper helper = new DaoMaster.DevOpenHelper(getApplicationContext(), MyConstants.DB_NAME, null);  
            daoMaster = new DaoMaster(helper.getWritableDatabase());  
        }  
        return daoMaster;  
    }  
      
    /** 
     * 取得DaoSession 
     *  
     * @param context 
     * @return 
     */  
    public synchronized DaoSession getDaoSession() {  
        if (daoSession == null) {  
            daoMaster = getDaoMaster();  
            daoSession = daoMaster.newSession();  
        }  
        return daoSession;  
    }  
    
 	public synchronized SharePreferenceUtil getSpUtil() {
 		if (mSpUtil == null) {
 			String currentId = UserManager.getInstance(getApplicationContext()).getCurrentUserId();
 			String sharedName = currentId + PREFERENCE_NAME;
 			mSpUtil = new SharePreferenceUtil(this, sharedName);
 		}
 		return mSpUtil;
 	}
 	/**
 	 * 重新创建sp
 	 * @param isNew 是否需要重新创建
 	 * @param userId sp的名称
 	 * @return
 	 */
 	public  synchronized SharePreferenceUtil getSpUtil(boolean isNew,String userId){
 		if(!isNew){
 			return getSpUtil();
 		}else{
 			if(TextUtils.isEmpty(userId))
 				return null;
 			String sharedName = userId + PREFERENCE_NAME;
 			mSpUtil = new SharePreferenceUtil(this, sharedName);
 			return  mSpUtil;
 		}
 	}
 	
 	public synchronized SysSharePreferenceUtil getSysSpUtil() {
 		if (mSysSpUtil == null) {
 			mSysSpUtil = new SysSharePreferenceUtil(this, SYS_PREFERENCE_NAME);
 		}
 		return mSysSpUtil;
 	}
 	
 	public NotificationManager getNotificationManager() {
		if (mNotificationManager == null)
			mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		return mNotificationManager;
	}
 	
	public synchronized MediaPlayer getMediaPlayer() {
		if (mMediaPlayer == null)
			mMediaPlayer = MediaPlayer.create(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		return mMediaPlayer;
	}
 	
 	/**
	 * 退出登录,清空缓存数据
	 */
	public void logout() {
		// 先调用sdk logout，在清理app中自己的数据
		EMChatManager.getInstance().logout();
		UserManager.getInstance(getApplicationContext()).logout();
		mSpUtil.clearApplicatioinCache(true);
		mSpUtil = null;
	}
	
	public void onLowMemory() { 
        super.onLowMemory();     
        System.gc(); 
    }
	
	class MyConnectionListener implements ConnectionListener {
		@Override
		public void onReConnecting() {
		}

		@Override
		public void onReConnected() {
		}

		@Override
		public void onDisConnected(String errorString) {
			if (errorString != null && errorString.contains("conflict")) {
				if(getPackageName().equalsIgnoreCase(MoMaUtil.getTopActivity(app))){
					Intent intent = new Intent(app, MainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("conflict", true);
					startActivity(intent);
				}else{
					MyApplication.getApp().getSpUtil().clearApplicatioinCache(true);
					MyApplication.getApp().logout();
				}
				
			}

		}

		@Override
		public void onConnecting(String progress) {

		}

		@Override
		public void onConnected() {
		}
	}
	
	private String getAppName(int pID) {
		ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
		List<?> l = am.getRunningAppProcesses();
		Iterator<?> i = l.iterator();
//		PackageManager pm = this.getPackageManager();
		while (i.hasNext()) {
			ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
			if (info.pid == pID)
				return info.processName;
//			try {
//				if (info.pid == pID) {
//					CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
					// Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
					// info.processName +"  Label: "+c.toString());
					// processName = c.toString();
//					return info.processName;
//				}
//			} catch (Exception e) {
//				// Log.d("Process", "Error>> :"+ e.toString());
//			}
		}
		return  null;
	}
	
	
	public void queryUserInfo(final String userId ,final int state ){
		JsonObject jo = new JsonObject();
		jo.addProperty("id", userId);
		BottleRestClient.post("queryUserInfo", this, jo, new AsyncHttpResponseHandler (){
			@Override
			public void onStart() {
				super.onStart();
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					QueryUserInfoModel userModel = gson.fromJson(str, QueryUserInfoModel.class);
					 
					 if(userModel!=null&&"0".equals(userModel.getCode())){
						 UserInfoModel userObj = userModel.getUserInfo();
						 if(userObj!=null){
							 Stranger model = new Stranger();
								model.setUserId( userObj.getUserId());
								model.setNickName(userObj.getNickName());
								try {
									model.setIdentityType(userObj.getIdentityType());
								} catch (Exception e) {
									// TODO: handle exception
								}
								model.setHeadImg(userObj.getHeadImg());
								model.setProvince(userObj.getProvince());
								model.setCity( userObj.getCity());
								if(state==1){
									model.setState(1);
								}else if(state == 2){
									model.setState(2);
								}else if(state == 3){
									model.setState(0);
								}
								//更新数据库
								BottleMsgManager.getInstance( app).insertOrReplaceStranger(model);
						 }
					 }
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
			}
        });
	}
	
}
