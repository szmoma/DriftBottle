package com.hnmoma.driftbottle;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;

import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.ConnectionListener;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.BallonImageView;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.custom.CustomDialog;
import com.hnmoma.driftbottle.custom.CustomDialog.CustomDialogClickListener;
import com.hnmoma.driftbottle.custom.RoundProgressBar;
import com.hnmoma.driftbottle.model.BrushModel;
import com.hnmoma.driftbottle.model.DaoSession;
import com.hnmoma.driftbottle.model.InitInfo;
import com.hnmoma.driftbottle.model.InitModel;
import com.hnmoma.driftbottle.model.KeyWordInfo;
import com.hnmoma.driftbottle.model.ScreenInfo;
import com.hnmoma.driftbottle.model.SensitiveWord;
import com.hnmoma.driftbottle.model.SensitiveWordDao;
import com.hnmoma.driftbottle.model.User;
import com.hnmoma.driftbottle.model.UserDataModel;
import com.hnmoma.driftbottle.model.VersionInfo;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.net.UpdateCacheOfUpperLimit;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.hnmoma.driftbottle.util.download.DownloadListener;
import com.hnmoma.driftbottle.util.download.DownloadManager;
import com.hnmoma.driftbottle.util.download.HttpDownloader;
import com.igexin.sdk.PushManager;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import de.greenrobot.dao.query.QueryBuilder;

public class MainActivity extends BaseActivity implements OnClickListener {
	protected  final int REQUE_BUYPROP = 1002; //购买道具
	private  final int SWITCHPROP = 0; //更换刷频器的背景
	private  final int DEAL_BOTTLES = 1;	//刷新瓶子
	private final int  LOGIN_RESET = 1000;	//用户账号被其他设备踢掉的标志
	private final int REQUE_BOTTLE = 1001; //捡到瓶子后的回调
	
	// 背景音乐
	MediaPlayer mp_bg;
	// 海浪声
	MediaPlayer mp_hl;

	Timer mTimer;
	MyTask mTimerTask;
	
	//系统瓶子刷新定时器
	Timer bottleTimer;	//定时器
	TimerTask bottleTask; //计划任务

	// TextView unreadmsg;
	ImageView unread;
	ImageView iv_lang;
	ImageView iv_shui;
	ImageView main_kj_new;
	
	
	int bgWidth, bgHeight; //屏幕的宽度和高度
	int bottlewidth;
	private BallonImageView bottle_img;
	RelativeLayout rl_container;
	LinearLayout ll_menu;

	int hasBrush = 0;//0表示没有刷屏器，1表示有刷屏器
	private List<ImageView> bottles = new ArrayList<ImageView>(); //所有的可见的瓶子
	private List<ImageView> beachBottles = new ArrayList<ImageView>();//沙滩中可见的瓶子
	private List<ImageView> seaBottles =  new ArrayList<ImageView>();	//海中可见的瓶子
	private List<int[]>  beachBottlePoint = new ArrayList<int[]>();	//沙滩上，瓶子的位置
	ImageView lastBottle;
	
	Animation anim_lang, anim_shui;
	
	private RoundProgressBar pbProper;//刷屏器
	private Timer mProperTimer;
	private TimerTask mProperTimerTask;
	private long delay = 10;
	private  long period = 2000;//2s
	private long TIME = 5*60*10;//5Min分成100份
	
	class MyTask extends TimerTask {
		public void run() {
			if (mp_hl != null) {
				mp_hl.start();
			}
		}
	}

	private NewMessageBroadcastReceiver msgReceiver;
	private ChangeUserInfoBroadcastReceiver cgeReceiver;
	// 账号在别处登录
	private boolean isConflict = false;

	private android.app.AlertDialog.Builder conflictBuilder;
	private boolean isConflictDialogShow;

	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	CircularImage cImage;
	
	private int wavesY;//波浪在Y轴上的起始坐标
	
	ImageView xiaoxi_n;
	LinearLayout ll_bottom;
	private ImageView ivPropBg;//刷频器的背景
	private boolean isInitBottle = true;//是否初始化瓶子
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		// 让该activity默认控制的是媒体音频
//		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		initView();
		initData();
		startBottleTimer();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		bottle_img.onPause();
		if (mp_bg != null && mp_bg.isPlaying()) {
			mp_bg.pause();
		}
		
		// 海浪音效
		stopTimer();
//		stopProperTimer();
		if (mp_hl != null && mp_hl.isPlaying()) {
			mp_hl.pause();
		}
		//停止海中瓶子的动画
		
		//停止海浪的动画
		
		//停止水的动画
		
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		bottle_img.onResume();
		
//		chechBottleAtSea();
		
		if (MyApplication.getApp().getSysSpUtil().isSoundOpend()) {
			startMusic();
		} else {
			stopMusic();
		}
		if (!isConflict) {
			updateUnreadLabel();
			EMChatManager.getInstance().activityResumed();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopBottleTimer();
		stopProperTimer();
		// 旧瓶子入库,用户登陆时，才保证酒瓶子出库
		if(UserManager.getInstance(this).getCurrentUser()!=null){
			int num = MyApplication.getApp().getSpUtil().getOldBottle();
			num+=bottles.size();
			MyApplication.getApp().getSpUtil().setOldBottle(num);
		}

		bottle_img.onDestroy();
		iv_lang.clearAnimation();
		iv_shui.clearAnimation();

		// 浮窗
		// ActWindowManager.removeSmallWindow(this);
		MyApplication.getApp().getSpUtil().clearApplicatioinCache();

		// 暂时不调用
		// ShareSDK.stopSDK(this);

		// 注销广播接收者
		try {
			unregisterReceiver(msgReceiver);
			msgReceiver = null;
		} catch (Exception e) {
		}
		try {
			unregisterReceiver(ackMessageReceiver);
			ackMessageReceiver = null;
		} catch (Exception e) {
		}
		try {
			unregisterReceiver(cgeReceiver);
			cgeReceiver = null;
		} catch (Exception e) {
		}
		
		try {
			unregisterReceiver(cmdMessageReceiver);
			cmdMessageReceiver = null;
		} catch (Exception e) {
		}

		if (conflictBuilder != null) {
			conflictBuilder.create().dismiss();
			conflictBuilder = null;
		}
		MyApplication.getApp().getSysSpUtil().setIsFirstTime(false);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 500) {//TODO  成功打开一个瓶子
			if (RESULT_CANCELED == resultCode) {
			}
			
		} else if (requestCode == 400) {
			if (RESULT_OK == resultCode) {// 登陆成功去扔瓶子界面
				// 对本地记录的数据进行判断是否已经超过可捞次数
				boolean throwNum = MyApplication.getApp().getSpUtil().canThrow();
				if (throwNum) {
					Intent intent = new Intent(this, Throw_ptpz.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivityForResult(intent, 300);
				} else {
					showMsg(getResources().getString(R.string.not_throw_bottle));
				}
			}
		} else if (requestCode == 700) {
			if (resultCode == Activity.RESULT_OK) {
				// refresh();
				Intent intent = new Intent(this, PersonCenterActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", UserManager.getInstance(this).getCurrentUserId());
				startActivityForResult(intent, 800);
			}
		} else if(requestCode == 900) {
			if(resultCode == Activity.RESULT_OK) {
				Intent album = new Intent(this, TalkActivity.class);
				album.putExtra("userId", UserManager.getInstance(this).getCurrentUserId());
				album.putExtra("identityflag", 0);
				album.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(album);
			}
		} else if(requestCode == 101){
			int result = mServerVersion.compareTo(getVersionCode());
			if(UserManager.getInstance(this).getCurrentUser()==null){
				UpdateCacheOfUpperLimit.getInstance().cache(0, this);
			}else{
				Integer flag = UserManager.getInstance(this).getCurrentUser().getIsVIP();
				UpdateCacheOfUpperLimit.getInstance().cache(flag==null?0:flag.intValue(), this);
			}
			
			if(result != 0){
				this.finish();
				android.os.Process.killProcess(android.os.Process.myPid());  
			}
			
		}else if(requestCode== LOGIN_RESET&&resultCode==Activity.RESULT_OK){
			boolean isWipe = getIntent().getBooleanExtra("wipe", false);//是否做出登出操作，清除历史用户的数据
			if(isWipe){
				MyApplication.getApp().getSpUtil().clearApplicatioinCache(true);
				Intent intent = new Intent();  
				intent.setAction(MyConstants.USERINFOCHANGE_BROADCAST_ACTION);  
				sendBroadcast(intent); 
			}
		}else if(requestCode==REQUE_BOTTLE &&resultCode==Activity.RESULT_OK){
			if(MyApplication.getApp().getSpUtil().getOldBottle()==0&&bottles.size()==0)
				Toast.makeText(this, "瓶子已捡完,请等候下一批", Toast.LENGTH_LONG).show();
			if(data!=null&&data.getBooleanExtra("REQUE_BOTTLE", false)){
				int num = MyApplication.getApp().getSpUtil().getOldBottle();
				num+=1;
				MyApplication.getApp().getSpUtil().setOldBottle(num);
			}
		}else if(requestCode==REQUE_BUYPROP&&resultCode==RESULT_OK){
			int tmp = hasBrush;
			if(data!=null&&data.getBooleanExtra("hasBrush", false)){
				hasBrush = 1;
				if(tmp!=1){
					stopProperTimer();
					startProperTimer();
					pbProper.setProgress(100);
				}
			}
		}
	}
	
	public void initView() {
		setContentView(R.layout.activity_main);

		// 海浪
		iv_lang = (ImageView) findViewById(R.id.iv_lang);
		iv_shui = (ImageView) findViewById(R.id.iv_shui);
		main_kj_new = (ImageView) findViewById(R.id.main_kj_new);
		anim_lang = AnimationUtils.loadAnimation(this, R.anim.anim_lang);
		anim_shui = AnimationUtils.loadAnimation(this, R.anim.anim_shui);
		iv_lang.startAnimation(anim_lang);
		iv_shui.startAnimation(anim_shui);
		pbProper = (RoundProgressBar) findViewById(R.id.pb_round);
		ivPropBg = (ImageView) findViewById(R.id.ivPropBg);
		ivPropBg.setImageResource(R.drawable.pick_bottle__props_no);
		
		/******************获取波浪的高度***************************/
		iv_shui.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
		                    @Override
		                    public boolean onPreDraw() {
		                    	int[] location = new int[2];
		                    	iv_shui.getLocationInWindow(location);
		                    	wavesY = location[1];
		                        return true;
		                    }
		                });
		
		// =================气球============================
		bottle_img = (BallonImageView) findViewById(R.id.bottle_img);
		bottle_img.setVisibility(View.VISIBLE);
		bottle_img.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, PhbActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(intent);
			}
		});
		
		// =================瓶子========================
		rl_container = (RelativeLayout) findViewById(R.id.rl_container);
		initMenu();
		
	}
	
	private void initMenu(){
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.menu_head)
				.showImageForEmptyUri(R.drawable.menu_head)
				.showImageOnFail(R.drawable.menu_head)
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build();
		ll_bottom = (LinearLayout) findViewById(R.id.ll_bottom);
		
		//头像
		cImage = (CircularImage) findViewById(R.id.ib_head);
		String headImg ="";
    	if(UserManager.getInstance(this).getCurrentUser() != null && !TextUtils.isEmpty(UserManager.getInstance(this).getCurrentUser().getHeadImg())){
    		headImg = UserManager.getInstance(this).getCurrentUser().getHeadImg();
    		
    	}
    	imageLoader.displayImage(headImg, cImage, options);
    	cImage.setOnClickListener(this);//TODO 
		
    	
    	//设置
		View ll_setting = findViewById(R.id.ll_setting);
		ll_setting.setOnClickListener(this);
//		lpz_n = (TextView) findViewById(R.id.lpz_n);
		//消息
		View fl_xx = findViewById(R.id.fl_xx);// message
		fl_xx.setOnClickListener(this);
		xiaoxi_n = (ImageView) findViewById(R.id.main_msg_new);
		//扔瓶子
		View fl_rpz = findViewById(R.id.fl_rpz);
		fl_rpz.setOnClickListener(this);
	}
	
	// 处理瓶子
	//TODO
	Handler myHandler = new Handler(){
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
            switch (msg.what) {
			case DEAL_BOTTLES:
				Log.e("debug","old="+MyApplication.getApp().getSpUtil().getOldBottle()+",new="+msg.arg1);
				 dealBottle(msg.arg1);
				break;
			case SWITCHPROP://更换刷屏器的背景
				if(pbProper.getProgress()==100)
					ivPropBg.setImageResource(R.drawable.pick_bottle__props_press);
				else {
					ivPropBg.setImageResource(R.drawable.pick_bottle__props_normal);
				}
				
				break;
			default:
				break;
			}
		};
	};
	
	/**
	 * 检查登陆用户是否有道具：刷新器
	 */
	private void queryHasBrush() {
		if(TextUtils.isEmpty(UserManager.getInstance(this).getCurrentUserId())) {
			return;
		}
		JsonObject jso = new JsonObject();
		jso.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		BottleRestClient.post("propertyHas", this, jso, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if (!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					Type type = new TypeToken<BrushModel>(){}.getType();
					//TODO  JsonSyntaxException  
					BrushModel baseModel = gson.fromJson(str, type);
					if (baseModel != null && !TextUtils.isEmpty(baseModel.getCode())) {
						if ("0".equals(baseModel.getCode())) {
							hasBrush = baseModel.getHasBrush();
							if(hasBrush==1){
								startProperTimer();
							}
							MyApplication.getApp().getSpUtil().setBrush(hasBrush);
						}else{
							showMsg(baseModel.getMsg());
						}
					}
				}
			}
			
			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				super.onFinish();
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				showMsg("失败");
			}
		});
	}
	
	protected synchronized void dealBottle(int bottleNum) {
		// TODO Auto-generated method stub
		if(bottleNum == 0) {
			
			return ;	
        }
		int num = 0;
		 //如果沙滩上的瓶子数等于0，则优先级较高
		if(beachBottles.size()==0&&isInitBottle){
			if(bottleNum>=2){
				num = 2;
			}else{
				num = bottleNum;
			}
			isInitBottle = false;
			showBottleAtBeach(num);
		}
		
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
			showBottleAtSea(bottleNum-num);
		}else{
			showBottleAtSeaCompatible(bottleNum-num);
		}
	}

	/**
	 * 在海中的瓶子
	 * @param num 瓶子的数量
	 */
	@TargetApi(value = Build.VERSION_CODES.HONEYCOMB)
	private void showBottleAtSea(int num) {
		for (int i = 0; i < num; i++) {
			final ImageView imageView = new ImageView(this);
			imageView.setBackgroundResource(R.drawable.bottlesea);
			//获取随机位置
			int[] position = getRandomPositionAtSea(i, num);

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.leftMargin = position[0]; // Your X coordinate
			params.topMargin = position[1]; // Your Y coordinate
			android.animation.ObjectAnimator obj = android.animation.ObjectAnimator.ofFloat(imageView, "y", position[1], this.getWindowManager().getDefaultDisplay().getHeight() / 2 + MoMaUtil.dip2px(this, 10)).setDuration(30000);
			obj.setStartDelay(1500);
			obj.addListener(new android.animation.Animator.AnimatorListener() {
				
				@Override
				public void onAnimationStart(android.animation.Animator animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(android.animation.Animator animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(android.animation.Animator animation) {
					// TODO Auto-generated method stub
					imageView.setY(bgHeight / 2 + MoMaUtil.dip2px(MainActivity.this, 10));
				}
				
				@Override
				public void onAnimationCancel(android.animation.Animator animation) {
					// TODO Auto-generated method stub
					
				}
			});
			
//			obj.setRepeatCount(ValueAnimator.INFINITE);
//			obj.setRepeatMode(ValueAnimator.INFINITE);
			obj.start();
//			imageView.setPivotX(0);
//			imageView.setPivotY(0);
//			imageView.invalidate();
			android.animation.ObjectAnimator objanimator = android.animation.ObjectAnimator.ofFloat(imageView, "rotation", 0, 10);//瓶子摇动的动画
			objanimator.setDuration(3000);
			obj.setStartDelay(1500);
			objanimator.setRepeatMode(android.animation.ValueAnimator.REVERSE);
			objanimator.setRepeatCount(android.animation.ValueAnimator.INFINITE);
			objanimator.start();
			
			togetherRun(imageView);
			
			imageView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
				
				@Override
				public boolean onPreDraw() {
					// TODO Auto-generated method stub
					int height = imageView.getMeasuredHeight();
					int[]location = new int[2];
					imageView.getLocationOnScreen(location);
					if(height+location[1]>=(wavesY+height)*2){
						if(location[0] < (bgWidth / 2)) {
							imageView.setBackgroundResource(R.drawable.leftbottle);
						} else {
							imageView.setBackgroundResource(R.drawable.rightbottle);
						}
					}
					
					return true;
				}
			});
			
			bottles.add(imageView);
			seaBottles.add(imageView);
			imageView.setOnClickListener(moc);
			rl_container.addView(imageView, params);
		}
	}
	
	MyOnClickListener moc = new MyOnClickListener();
	/**
	 * 兼容api 2.X系列的动画
	 * @param num
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	private void showBottleAtSeaCompatible(int num) {
//		int bottleImageWidth = getImageWidth();
		int bottleImageWidth = 64;
		MoMaLog.d("瓶子宽", bottleImageWidth+"");
		for (int i = 0; i < num; i++) {
			final ImageView imageView = new ImageView(this);
			imageView.setBackgroundResource(R.drawable.bottlesea);
			//获取随机位置
			int[] position =getRandomPositionAtSea(i, num); 
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.leftMargin = position[0]; // Your X coordinate
			params.topMargin = position[1]; // Your Y coordinate
			
			com.nineoldandroids.animation.ObjectAnimator obj = com.nineoldandroids.animation.ObjectAnimator.ofFloat(imageView, "y", position[1], this.getWindowManager().getDefaultDisplay().getHeight() / 2 + MoMaUtil.dip2px(this, 10)).setDuration(30000);
			obj.setStartDelay(1500);
			obj.addListener(new com.nineoldandroids.animation.Animator.AnimatorListener() {
				
				@Override
				public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(com.nineoldandroids.animation.Animator animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
					// TODO Auto-generated method stub
					int[] location = new int[2];
					imageView.getLocationOnScreen(location);
					RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
					lp.leftMargin = (int) location[0]; //Your X coordinate
					lp.topMargin = (int) location[1]; //Your Y coordinate
					imageView.postInvalidate();
					imageView.invalidate();
					imageView.layout(location[0], location[1], location[0]+imageView.getWidth(), location[1]+imageView.getHeight());
				}
				
				@Override
				public void onAnimationCancel(com.nineoldandroids.animation.Animator animation) {
					// TODO Auto-generated method stub
					
				}

				
			});
			obj.start();

			com.nineoldandroids.animation.ObjectAnimator objanimator = com.nineoldandroids.animation.ObjectAnimator.ofFloat(imageView, "rotation", 0, 10);//瓶子摇动的动画
			objanimator.setDuration(3000);
			obj.setStartDelay(1500);
			objanimator.setRepeatMode(com.nineoldandroids.animation.ValueAnimator.REVERSE);
			objanimator.setRepeatCount(com.nineoldandroids.animation.ValueAnimator.INFINITE);
			objanimator.start();
			
			togetherRunCompatible(imageView);
//			Animator anim = AnimatorInflater.loadAnimator(this, R.anim.anim_bottle_move);  
//	        imageView.setPivotX(0);  
//	        imageView.setPivotY(0);  
//	        //显示的调用invalidate  
//	        imageView.invalidate();  
//	        anim.setTarget(imageView);  
//	        anim.start();  
			
			imageView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
				
				@Override
				public boolean onPreDraw() {
					// TODO Auto-generated method stub
					int height = imageView.getMeasuredHeight();
					int[]location = new int[2];
					imageView.getLocationOnScreen(location);
					if(height+location[1]>=(wavesY+height)*2){
						if(location[0] < (bgWidth / 2)) {
							imageView.setBackgroundResource(R.drawable.leftbottle);
						} else {
							imageView.setBackgroundResource(R.drawable.rightbottle);
						}
					}
					
					return true;
				}
			});
			bottles.add(imageView);
			seaBottles.add(imageView);
			imageView.setOnClickListener(moc);
			rl_container.addView(imageView, params);
		}
	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void togetherRun(View view) {    //从大到小
		android.animation.ObjectAnimator anim1 = android.animation.ObjectAnimator.ofFloat(view, "scaleX", 0.4f, 1f);  
		android.animation.ObjectAnimator anim2 = android.animation.ObjectAnimator.ofFloat(view, "scaleY", 0.4f, 1f);  
		android.animation.AnimatorSet animSet = new android.animation.AnimatorSet();  
        
        animSet.setDuration(30000);  
        animSet.setInterpolator(new LinearInterpolator());  
        //两个动画同时执行  
        animSet.playTogether(anim1, anim2);  
        animSet.start();  
    }  
	/**
	 * 兼容处理
	 * @param view
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	public void togetherRunCompatible(View view) {    //从大到小
		com.nineoldandroids.animation.ObjectAnimator anim1 = com.nineoldandroids.animation.ObjectAnimator.ofFloat(view, "scaleX", 0.4f, 1f);  
		com.nineoldandroids.animation.ObjectAnimator anim2 = com.nineoldandroids.animation.ObjectAnimator.ofFloat(view, "scaleY", 0.4f, 1f);  
		com.nineoldandroids.animation.AnimatorSet animSet = new com.nineoldandroids.animation.AnimatorSet();  
        
        animSet.setDuration(30000);  
        animSet.setInterpolator(new android.view.animation.LinearInterpolator());  
        //两个动画同时执行  
        animSet.playTogether(anim1, anim2);  
        animSet.start();  
    }  
	
	//存储沙滩上的瓶子的位置，最大为2,用来保存沙滩上的瓶子的坐标
	private void showBottleAtBeach(int num) {
		for(int i= 0; i < num; i++) {
			ImageView imageView = new ImageView(this);
			int[] ds = getRandomPosition(i, num);
			if(ds[0] < (bgWidth / 2)) {
				imageView.setBackgroundResource(R.drawable.leftbottle);
			} else {
				imageView.setBackgroundResource(R.drawable.rightbottle);
			}

			// imageView.setX(ds[0]);
			// imageView.setY(ds[1]);
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.leftMargin = ds[0]; // Your X coordinate
			params.topMargin = ds[1]; // Your Y coordinate

			bottles.add(imageView);
			beachBottles.add(imageView);
			beachBottlePoint.add(ds);
			MoMaLog.d("沙滩上瓶子的位置", ds[0]+" "+ds[1]);
			imageView.setOnClickListener(moc);
			rl_container.addView(imageView, params);
		}
		
	}
	
	/**
	 * 在沙滩上随机生成瓶子的坐标位置
	 * @param num 需要生成的第num个瓶子
	 * @param total 瓶子总数
	 * @return 返回表示坐标的一维数组，n[0]表示x坐标点，n[1]表示y坐标的位置
	 */
	public int[] getRandomPosition(int num, int total) {
		//TODO  78*45  bgWidth, bgHeight
		int[] position = new int[2];
		
//		cImage.getLocationOnScreen(position);
		int sportinterval = MoMaUtil.sp2px(this, 185);
		int sportmargin = MoMaUtil.sp2px(this, 165);
//		MoMaLog.d("bottomHeigh", bottomheigh+"");
		if(total == 1) {
			position[0] = MoMaUtil.random(100, bgWidth - 100);//X
			position[1] = MoMaUtil.random(bgHeight - sportinterval, bgHeight - sportmargin);//Y
		} else {
			if(num == 0) {
				position[0] = MoMaUtil.random(100, bgWidth/2-MoMaUtil.dip2px(this, 20));
				position[1] = MoMaUtil.random(bgHeight - sportinterval, bgHeight - sportmargin);
			} else {
				position[0] = MoMaUtil.random(bgWidth/2+MoMaUtil.dip2px(this, 20), bgWidth - 100);
				position[1] = MoMaUtil.random(bgHeight - sportinterval, bgHeight - sportmargin);
			}
		}

		MoMaLog.d("瓶子坐标:", +position[0] + " ," + position[1]);
		return position;
	}

	boolean bottleCanClick = true;
	int clickedBottle;
	
	/**
	 * 点击瓶子处理点击事件，如暂停瓶子移动，瓶子从屏幕消除等
	 * @author Administrator
	 *
	 */
	class MyOnClickListener implements OnClickListener {

		public void onClick(View v) {
			if (!bottleCanClick) {
				return;
			}
			bottleCanClick = false;
			if (UserManager.getInstance(MainActivity.this).getCurrentUser() == null) {// 游客
				int pickUpNum = MyApplication.getApp().getSpUtil().getYkPickUpNum();
				if (pickUpNum <= 0) {
					showMsg(getResources().getString(R.string.tip_unlogin));
				} else {
					// 检查网络
					if (!MoMaUtil.isNetworkAvailable(MainActivity.this)) {
						showMsg("当前网络不可用，请检查");
					} else {
						lastBottle = (ImageView) v;
						int[] clickBottle = new int[2];
						clickBottle[0] = lastBottle.getLeft();
						clickBottle[1] = lastBottle.getTop();
						//int y = lastBottle.getHeight();
//						lastBottle.getLocationOnScreen(clickBottle);
//						clickBottle[0] = lastBottle.gets;
//						clickBottle[1] = lastBottle.getTop();
						for(int i= 0; i < beachBottlePoint.size(); i++) {
							if(beachBottlePoint.get(i)[0] == clickBottle[0] && beachBottlePoint.get(i)[1] == clickBottle[1]) {
								beachBottlePoint.remove(i);
								beachBottles.remove(lastBottle);
							}
						}
						lastBottle.setVisibility(View.GONE);
						rl_container.removeView(lastBottle);
						seaBottles.remove(lastBottle);
						bottles.remove(lastBottle);
						lastBottle = null;
						
						Intent intent = new Intent(MainActivity.this, FishingBottleActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						startActivityForResult(intent, REQUE_BOTTLE);
					}
				}
			} else {
				// 检查网络
				if (!MoMaUtil.isNetworkAvailable(MainActivity.this)) {
					showMsg("当前网络不可用，请检查");
				} else {
					lastBottle = (ImageView) v;
//					clickedBottle = bottles.indexOf(lastBottle);
					int[] clickBottle = new int[2];
					clickBottle[0] = lastBottle.getLeft();
					clickBottle[1] = lastBottle.getTop();
					for(int i= 0; i < beachBottlePoint.size(); i++) {
						if(beachBottlePoint.get(i)[0] == clickBottle[0] && beachBottlePoint.get(i)[1] == clickBottle[1]) {
							beachBottlePoint.remove(i);
							beachBottles.remove(lastBottle);
						}
					}
//					if(beachBottle.contains(clickBottle)) {
//						beachBottle.remove(clickBottle);
//					}
					lastBottle.setVisibility(View.GONE);
					rl_container.removeView(lastBottle);
					seaBottles.remove(lastBottle);
					bottles.remove(lastBottle);
					lastBottle = null;
					Intent intent = new Intent(MainActivity.this, FishingBottleActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivityForResult(intent, REQUE_BOTTLE);
				}
			}
			bottleCanClick = true;
		}
	}
	
	public int[] getRandomPositionAtSea(int num, int total) {
		int[] position = new int[2];
		position[0] = MoMaUtil.random(MoMaUtil.dip2px(this, 5), bgWidth - MoMaUtil.dip2px(this, 40)) + new Random().nextInt(3) * MoMaUtil.dip2px(this, 5);
		position[1] = MoMaUtil.random(bgHeight / 2-260, bgHeight/2-230);
		return position;
	}
	
	public void initData() {
		if (MyApplication.getApp().getSysSpUtil().isSoundOpend()) {
			loadSound(this);
		}

		// greenDao Debug模式
		QueryBuilder.LOG_SQL = MyApplication.DEVELOPER_MODE;
		QueryBuilder.LOG_VALUES = MyApplication.DEVELOPER_MODE;
		// ===================友盟=========================
//		com.umeng.common.Log.LOG = MyApplication.DEVELOPER_MODE;
		// ym更新
		
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.setUpdateAutoPopup(true);
		UmengUpdateAgent.setUpdateListener(null);
		UmengUpdateAgent.update(this);
		MobclickAgent.openActivityDurationTrack(false);
		MobclickAgent.setDebugMode(MyApplication.DEVELOPER_MODE);
		// ===================友盟=========================

		// ==============ShareSDK====================
		ShareSDK.initSDK(this);
		// 去除注释，可以即可使用应用后台配置的应用信息，否则默认使用ShareSDK.conf中的信息
		// ShareSDK.setNetworkDevInfoEnable(true);
		// ==============ShareSDK====================

		// ==============getui====================
		MoMaLog.d("个推初始化", "开始");
		PushManager.getInstance().initialize(this.getApplicationContext());
		// ==============getui====================

		// 背景的长宽
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		bgWidth = dm.widthPixels;
		bgHeight = dm.heightPixels;
		
		//气球的移动
		//TODO
		bottle_img.init(bgWidth);
		bottle_img.start();
		
		//刷瓶器处理
        hasBrush = MyApplication.getApp().getSpUtil().getBrush();
        if(hasBrush==1){
        	int process = (int)(MyApplication.getApp().getSpUtil().getSpqOkTime()/TIME);
        	pbProper.setProgress(process);
        	if(process>=5){
        		Message msg = Message.obtain();
    			msg.arg1 = SWITCHPROP;
    			myHandler.sendMessage(msg);
        	}
        	startProperTimer();
        }else{
        	queryHasBrush();
        }
    	
        //处理上一次剩下的瓶子,如果用户么有登陆，则显示5个瓶子
        Message msg = Message.obtain();
        msg.what = DEAL_BOTTLES;
        if(UserManager.getInstance(this).getCurrentUser()!=null){
        	msg.arg1 = MyApplication.getApp().getSpUtil().getBottles(0);
        }else{
        	msg.arg1 = 5;
        }
        myHandler.sendMessage(msg);
        
//		if (MyApplication.getApp().getSysSpUtil().isFirstTime()) {
//			// 气泡第一次使用提示
//			MyApplication.getApp().getSpUtil().setNoticeMsg(new String[] { "Hi！我是你的新伙伴，我叫小八，点我可以打开菜单哦！","海面会不定时的飘来瓶子，去捡一个吧！点击瓶子就可以将其捡起！","点气球有惊喜哦！" });
//			MyApplication.getApp().getSysSpUtil().setIsFirstTime(false);
//		}

		// Emchat
		// 注册一个接收消息的BroadcastReceiver
		msgReceiver = new NewMessageBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
		intentFilter.setPriority(3);
		registerReceiver(msgReceiver, intentFilter);

		// 注册一个ack回执消息的BroadcastReceiver
		IntentFilter ackMessageIntentFilter = new IntentFilter(EMChatManager.getInstance().getAckMessageBroadcastAction());
		ackMessageIntentFilter.setPriority(3);
		registerReceiver(ackMessageReceiver, ackMessageIntentFilter);
		
		// 用户信息改变广播
		cgeReceiver = new ChangeUserInfoBroadcastReceiver();
		IntentFilter cgeFilter = new IntentFilter(MyConstants.USERINFOCHANGE_BROADCAST_ACTION);
		cgeFilter.setPriority(3);
		registerReceiver(cgeReceiver, cgeFilter);

		// 注册一个cmd消息的BroadcastReceiver
		IntentFilter cmdIntentFilter = new IntentFilter(EMChatManager.getInstance().getCmdMessageBroadcastAction());
		cmdIntentFilter.setPriority(3);
		registerReceiver(cmdMessageReceiver, cmdIntentFilter);
		

		// 注册一个监听连接状态的listener
		EMChatManager.getInstance().addConnectionListener(new MyConnectionListener());
		// 通知sdk，UI 已经初始化完毕，注册了相应的receiver和listener, 可以接受broadcast了
		EMChat.getInstance().setAppInited();

		if(TextUtils.isEmpty(MyApplication.getApp().getSpUtil().getUserHeadImage())) {
			if(UserManager.getInstance(this).getCurrentUser() != null) {
				getUserInfo(UserManager.getInstance(this).getCurrentUserId());
			}
			
		}
		
		initServer();
		initCache();
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
			String action = cmdMsgBody.action;//获取自定义action
//			//获取扩展属性
//			String attr=message.getStringAttribute("a");
			if(MyConstants.MESSAGE_ACTION_VZONETYPE.equals(action)) {
				updateUnreadHead(true);
//				abortBroadcast();
			}else  if(MyConstants.MESSAGE_ATTR_ISSAYHELLO.equals(action)){
//				updateUnreadLabel();
			}
		}
	};
	
	
	/**
	 * 获取服务器数据，版本号，禁言时间
	 */
	String mServerVersion;
	private void initServer() {
		JsonObject jso = new JsonObject();
		jso.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jso.addProperty("come", "6000");
		jso.addProperty("screenVersion", MyApplication.getApp().getSpUtil().getSplashVersion());
		jso.addProperty("version", getVersionCode());
		jso.addProperty("keyVersion", MyApplication.getApp().getSysSpUtil().getKeyVersion());
		BottleRestClient.post("init", this, jso, new AsyncHttpResponseHandler() {
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
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							Type type = new TypeToken<InitModel>(){}.getType();
							//TODO  JsonSyntaxException  
							InitModel baseModel = gson.fromJson(str, type);
							if (baseModel != null && !TextUtils.isEmpty(baseModel.getCode())) {
								if ("0".equals(baseModel.getCode())) {
									InitInfo initInfo = baseModel.getInitInfo();
									
									if (initInfo != null) {
										//禁言处理
										String thawTime = initInfo.getThawTime();
										if(!TextUtils.isEmpty(thawTime)){
											MyApplication.getApp().getSpUtil().setGagTime(thawTime);
										}
										//刷瓶器 ,此处暂时不用
//										hasBrush = initInfo.getHasBrush();
//										if(hasBrush==1){
//											btn_spq.setVisibility(View.VISIBLE);
//										}else if(hasBrush==0){
//											btn_spq.setVisibility(View.GONE);
//										}
//										MyApplication.getApp().getSpUtil().setBrush(hasBrush);
										
										User user = UserManager.getInstance(MainActivity.this).getCurrentUser();
										if(user!=null&&user.getIsVIP()!=null&&user.getIsVIP().intValue()==0&&initInfo.getIsVIP()==1){
											UserManager.getInstance(MainActivity.this).updateUserInfo(true);
										}
										//闪屏动画
										ScreenInfo mScreenInfo = initInfo.getScreenInfo();
										if(mScreenInfo != null) {
											if(!TextUtils.isEmpty(mScreenInfo.getGifUrl())) {
												downloadPic(mScreenInfo.getGifUrl(), "splash.gif");
											}
											if(!TextUtils.isEmpty(mScreenInfo.getBackgroundUrl())) {
												downloadPic(mScreenInfo.getBackgroundUrl(), "splashbg.png");
											}
											if(!TextUtils.isEmpty(mScreenInfo.getDescript())) {
												downloadPic(mScreenInfo.getDescript(), "splashdesc.png");
											}
											MyApplication.getApp().getSpUtil().saveSplashData(mScreenInfo);
										}
										
										//强制升级
										VersionInfo versionInfo = initInfo.getVersionInfo();
										if(versionInfo != null){
											mServerVersion = versionInfo.getVersion();
											int result = mServerVersion.compareTo(getVersionCode());
											if (result > 0) {
												showMsg("当前版本过低，下载升级中...");
												showUpdateDialog(versionInfo.getContent(), versionInfo.getAppUrl());
											}
										}
										
										//敏感词处理
										KeyWordInfo keyWordInfo = initInfo.getKeyWordInfo();
										if(keyWordInfo!=null){
											final int lastVersion = Integer.parseInt(keyWordInfo.getLastVersion());
											final List<SensitiveWord> wordList = keyWordInfo.getWordList();
											final List<SensitiveWord> delWordList = keyWordInfo.getDelWordList();
												new Thread(new Runnable() {
													public void run() {
														DaoSession daoSession = MyApplication.getApp().getDaoSession();
														SensitiveWordDao sensitiveWordDao = daoSession.getSensitiveWordDao();
														//添加敏感词到数据库
														if(wordList != null){
															for(SensitiveWord sw : wordList){
																sensitiveWordDao.insertOrReplace(sw);
															}
														}
														//从数据库删除敏感词
														if(delWordList != null){
															for(SensitiveWord sw : delWordList){
																sensitiveWordDao.delete(sw);
															}
														}
														//记录敏感词数据库版本号
														if(lastVersion!=0){
															MyApplication.getApp().getSysSpUtil().setKeyVersion(lastVersion);
														}
													}
												}).start();
										}
									}
								}
							}
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					}
				});
	}
	
	public void downloadPic(String url, String dest) {
		HttpDownloader downloader = null;
		File  splashfiles = new File(MyApplication.mAppPath + "/splash");
		if(!splashfiles.exists()) {
			splashfiles.mkdirs();
		}
		File splashfile = new File(splashfiles + "/" + dest);
		if(splashfile.exists()) {
			splashfile.delete();
		}
		try {
			downloader = new HttpDownloader(url, splashfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		DownloadListener downlistener = new DownloadListener() {
			@Override
			public void onProgress(Data data) {
			}
			@Override
			public void onError(Exception e) {
			}
			@Override
			public void onCompleted() {
			}
		};
		if(downloader != null) {
			DownloadManager manager = new DownloadManager(downloader, downlistener);
			manager.download();
		}
	}
	
	public Dialog pd;
	protected void showUpdateDialog(String updateMsg, final String appUrl) {
		AlertDialog  builder= new AlertDialog.Builder(this)
		.setTitle("漂流瓶子有新版了么么哒")
		.setIcon(R.drawable.ic_launcher)
		.setMessage(updateMsg)
		.setPositiveButton("准奏", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				downloadNewVersion(appUrl);
			}
		})
		.setNegativeButton("退出", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				onBackPressed();
			}
		})
		.create();
		builder.setCancelable(false);
		builder.show();
	}
	Dialog pb;
	ProgressBar progress;
	TextView progressText;
	private void intiProgress() {
		//Button bt;
		pd = new Dialog(this, R.style.dialogStyle);
		pd.setCancelable(false);
		pd.setCanceledOnTouchOutside(false);
		
		View v =this.getLayoutInflater().inflate(R.layout.updateprogress, null);
		progress = (ProgressBar) v.findViewById(R.id.myProssBar);
		progressText = (TextView) v.findViewById(R.id.myProssBarText);
		v.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		pd.setContentView(v, lp);
		pd.show();
	}
	
	protected void downloadNewVersion(String appUrl) {
		try {
			String externalStorageState = Environment.getExternalStorageState();
			
			if (Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
				
				final File plpz = new File(Environment.getExternalStorageDirectory(),
						"PLPX.apk");
//				if(plpz.exists()) {
//					installApk(plpz);
//				}  else {
					intiProgress();
					BottleRestClient.get(appUrl, new FileAsyncHttpResponseHandler(plpz) {
						@Override
						public boolean deleteTargetFile() {
							return super.deleteTargetFile();
						}
						
						@Override
						public void onStart() {
							super.onStart();
						}

						@Override
						public void onFinish() {
							super.onFinish();
							pd.dismiss();
						}

						@Override
						public void onProgress(int bytesWritten, int totalSize) {
							int result = bytesWritten*100/totalSize;
							progress.setMax(totalSize);
							progress.setProgress(bytesWritten);
							progressText.setText(result+ "%");
						}
						@Override
						public void onSuccess(int statusCode, Header[] headers, File file) {
							if(statusCode == 200) {
								showMsg("文件下载成功，即将安装...");
								installApk(file);
							}
						}
						@Override
						public void onFailure(int statusCode, Header[] headers,
								Throwable throwable, File file) {
						}
					});
				}
				
//			}
		} catch (Exception e) {
			e.printStackTrace();
			showMsg("下载异常...");
		}
	}

	private void installApk(File t) {
		Intent intent = new Intent();
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
		else{
			intent.setAction(Intent.ACTION_VIEW);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setDataAndType(Uri.fromFile(t),"application/vnd.android.package-archive");
		startActivityForResult(intent, 101);
	}
	
	

	// 方法：加载游戏中用到的声音
	public void loadSound(Context context) {
		// MediaPlayer的初始化
		mp_bg = MediaPlayer.create(context, R.raw.bg);
		if (mp_bg != null) {
			mp_bg.setLooping(true);
		}

		mp_hl = MediaPlayer.create(context, R.raw.sea);
	}
	
	long waitTime = 1500;  
	long touchTime = 0; 
	@Override
	public void onBackPressed() {
		if (mContent != null) {
			if (mContent.onBackPressed()) {
				mContent = null;
			}
		} else{
			long currentTime = System.currentTimeMillis();  
		    if((currentTime-touchTime)>=waitTime) {  
		        showMsg("再按一次退出");
		        touchTime = currentTime;  
		    }else {  
		        finish();  
		    }  
		}
	}


	@SuppressWarnings("deprecation")
	private void chechBottleAtSea() {
//		showMsg("bottles: " + bottles.size() + "; beachBottles: " + beachBottle.size());
		int[] location = new int[2];
		for(int i = 0; i < seaBottles.size(); i++) {//海面的瓶子
			MoMaLog.d("seaBottle-"+ i, " y:"+ seaBottles.get(i).getTop()+ " , x:" +  seaBottles.get(i).getLeft());
			if(seaBottles.get(i).getTop() > getWindowManager().getDefaultDisplay().getHeight() / 2 + MoMaUtil.dip2px(this, 10)) {
				if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
					seaBottles.get(i).setY(this.getWindowManager().getDefaultDisplay().getHeight() / 2 + MoMaUtil.dip2px(this, 10));
				else{
					seaBottles.get(i).getLocationOnScreen(location);
					RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) seaBottles.get(i).getLayoutParams();
					lp.leftMargin = (int) location[0]; //Your X coordinate
					lp.topMargin = (int) location[1]; //Your Y coordinate
					MoMaLog.e("debug", "x="+location[0]+",y="+location[1]);
					seaBottles.get(i).layout(location[0]-seaBottles.get(i).getWidth()/2,
							this.getWindowManager().getDefaultDisplay().getHeight() / 2-seaBottles.get(i).getHeight()/2, 
							location[0]+seaBottles.get(i).getWidth()/2, 
							this.getWindowManager().getDefaultDisplay().getHeight() / 2+seaBottles.get(i).getHeight()/2);
				}
			}
		}
	}

	public void stopMusic() {
		if (mp_bg != null && mp_bg.isPlaying()) {
			mp_bg.stop();
		}

		if (mp_hl != null && mp_hl.isPlaying()) {
			mp_hl.stop();
		}

		// 海浪音效
		stopTimer();
	}

	public void startMusic() {
		loadSound(this);

		if (mp_bg != null && !mp_bg.isPlaying()) {
			mp_bg.start();
		}

		if (mp_hl != null && !mp_hl.isPlaying()) {
			mp_hl.start();
		}

		startTimer();
	}

	private void startTimer() {
		if(mTimer!=null&&mTimerTask!=null){
			mTimer.cancel();
			mTimer = null;
			mTimerTask.cancel();
			mTimerTask = null;
		}
		
		if (mTimer == null) {
			mTimer = new Timer();
		}

		if (mTimerTask == null) {
			mTimerTask = new MyTask();
		}

		if (mTimer != null && mTimerTask != null)
			mTimer.schedule(mTimerTask, 2000, 15000);

	}

	private void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}

		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}



	/**
	 * 新消息广播接收者
	 * 
	 * 
	 */
	private class NewMessageBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 主页面收到消息后，主要为了提示未读，实际消息内容需要到chat页面查看

			// 消息id
//			String msgId = intent.getStringExtra("msgid");
			// 收到这个广播的时候，message已经在db和内存里了，可以通过id获取mesage对象
			// EMMessage message =
			// EMChatManager.getInstance().getMessage(msgId);

			// 刷新bottom bar消息未读数
			updateUnreadLabel();
			// if (currentTabIndex == 0) {
			// // 当前页面如果为聊天历史页面，刷新此页面
			// if (chatHistoryFragment != null) {
			// chatHistoryFragment.refresh();
			// }
			// }
			// 注销广播，否则在ChatActivity中会收到这个广播
//			abortBroadcast();
		}
	}

	/**
	 * 消息回执BroadcastReceiver
	 */
	private BroadcastReceiver ackMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String msgid = intent.getStringExtra("msgid");
			String from = intent.getStringExtra("from");
			EMConversation conversation = EMChatManager.getInstance().getConversation(from);
			if (conversation != null) {
				// 把message设为已读
				EMMessage msg = conversation.getMessage(msgid);
				if (msg != null) {
					msg.isAcked = true;
				}
			}
			abortBroadcast();
		}
	};

	/**
	 * 刷新未读消息数
	 */
	public void updateUnreadLabel() {
		int msgCount = getUnreadMsgCountTotal();
		if (msgCount > 0) {
			xiaoxi_n.setVisibility(View.VISIBLE);
		} else {
			xiaoxi_n.setVisibility(View.INVISIBLE);
		}
		
	}

	/**
	 * 获取空间消息未读数
	 */
	public void updateUnreadHead(boolean isShow) {
		if(isShow) {
//			main_kj_new.setVisibility(View.VISIBLE);
			main_kj_new.setVisibility(View.GONE);
		} else {
			main_kj_new.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 获取未读消息数
	 * 
	 * @return
	 */
	public int getUnreadMsgCountTotal() {
		int unreadMsgCountTotal = 0;
		unreadMsgCountTotal = EMChatManager.getInstance().getUnreadMsgsCount();
		return unreadMsgCountTotal;
	}

	/**
	 * 显示帐号在别处登录dialog
	 */
	private void showConflictDialog() {
		isConflictDialogShow = true;
		if (!MainActivity.this.isFinishing()) {
			// clear up global variables
			try {
				if (conflictBuilder == null)
					conflictBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
				conflictBuilder.setTitle("下线通知");
				conflictBuilder.setMessage(R.string.connect_conflict);
				conflictBuilder.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								conflictBuilder = null;
								startActivity(new Intent(MainActivity.this,LoginActivity.class));
							}
						});
				conflictBuilder.setCancelable(false);
				
				Intent intent = new Intent();  
				intent.setAction(MyConstants.USERINFOCHANGE_BROADCAST_ACTION);  
				sendBroadcast(intent); 
				
				conflictBuilder.create().show();
				isConflict = true;
			} catch (Exception e) {
				MoMaLog.e("###", "---------color conflictBuilder error" + e.getMessage());
			}
			MyApplication.getApp().getSpUtil().clearApplicatioinCache(true);
			MyApplication.getApp().logout();
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (getIntent().getBooleanExtra("conflict", false) && !isConflictDialogShow)
			showConflictDialog();
//		if (intent.getIntExtra("flag", 0) == 0){
////			//分别为我的瓶子界面过来，道具商城过来
////			int oldBottle = bottles.size();
////			generatorBottle(oldBottle);
//		}else{
//			//道具商城过来
//			int oldBottle = MyApplication.getApp().getSpUtil().getOldBottle();
//			generatorBottle(oldBottle);
//		}
	}

	/**
	 * 连接监听listener
	 * 
	 */
	private class MyConnectionListener implements ConnectionListener {

		@Override
		public void onConnected() {
			// chatHistoryFragment.errorItem.setVisibility(View.GONE);
		}

		@Override
		public void onDisConnected(String errorString) {
			if (errorString != null && errorString.contains("conflict")) {
				// 显示帐号在其他设备登陆dialog
				showConflictDialog();
				findViewById(R.id.main_msg_new).setVisibility(View.GONE);
				imageLoader.displayImage(null, cImage, options);
			} else {
				// chatHistoryFragment.errorItem.setVisibility(View.VISIBLE);
				// if(NetUtils.hasNetwork(MainActivity.this))
				// chatHistoryFragment.errorText.setText("连接不到聊天服务器");
				// else
				// chatHistoryFragment.errorText.setText("当前网络不可用，请检查网络设置");

			}
		}

		@Override
		public void onReConnected() {
			// chatHistoryFragment.errorItem.setVisibility(View.GONE);
		}

		@Override
		public void onReConnecting() {
		}

		@Override
		public void onConnecting(String progress) {
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fl_xx:
			Intent intent = new Intent(MainActivity.this, MyBottleActivity.class);// 
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			startActivityForResult(intent, 600);
			break;
		case R.id.ib_head:
			if (UserManager.getInstance(this).getCurrentUser() == null) {
				intent = new Intent(this, LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivityForResult(intent, 700);
			} else {
				updateUnreadHead(false);
				intent = new Intent(this, PersonCenterActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", UserManager.getInstance(this).getCurrentUserId());
				startActivityForResult(intent, 800);
			}
			break;
		case R.id.ll_setting:// 我的瓶子
			
			if (UserManager.getInstance(this).getCurrentUser() == null) {
				intent = new Intent(this, LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivityForResult(intent, 900);
			} else {
				Intent album = new Intent(this, TalkActivity.class);
				album.putExtra("userId", UserManager.getInstance(this).getCurrentUserId());
				album.putExtra("identityflag", 0);
				album.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(album);
			}
			break;
		case R.id.fl_rpz:
			//用户登录后才允许扔瓶子
			if (UserManager.getInstance(this).getCurrentUser() == null) {
				intent = new Intent(this, LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivityForResult(intent, 400);
			} else {
//				intent = new Intent(this, ThrowActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//				startActivityForResult(intent, 300);
				boolean canThrow = MyApplication.getApp().getSpUtil().canThrow();
				if (canThrow) {
					intent = new Intent(this, Throw_ptpz.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivityForResult(intent, 300);
				} else {
					showMsg(getResources().getString(R.string.throw_gap_tip));
				}
			}
			break;
			
		case R.id.pb_round://刷瓶器
			if(hasBrush!=1){
				new CustomDialog().showSelectDialog(MainActivity.this, "是否购买刷屏器?", new CustomDialogClickListener() {
					
					@Override
					public void confirm() {
						// TODO Auto-generated method stub
						Intent intent = new Intent(MainActivity.this,PropertyStoreActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						startActivityForResult(intent, REQUE_BUYPROP);
					}
					
					@Override
					public void cancel() {
						// TODO Auto-generated method stub
					}
				});
				return ;
			}
			
			if(pbProper.getProgress()<100){
				showMsg("雅灭蝶，让刷瓶器休息一会");
				return ;
			}else{
				if(bottles.size()>0){
					//清除所有的瓶子
					for(ImageView view:bottles)
						rl_container.removeView(view);
					bottles.clear();
					seaBottles.clear();
					beachBottles.clear();
					beachBottlePoint.clear();
					MyApplication.getApp().getSpUtil().setOldBottle(0);
				}
				
				ivPropBg.setImageResource(R.drawable.pick_bottle__props_normal);
				pbProper.setProgress(0);
				pbProper.setClickable(false);
				MyApplication.isPending = true;
				showMsg("瓶子已经刷新");
				/**此处需要考虑，如果使用刷屏器时候，是否需要记录出瓶子的时间*/
				MyApplication.getApp().getSpUtil().updateSpqRefreshTime();
				int bottleNum = 0;
				if(bottles.size()==0){
					bottleNum = 5;
					MyApplication.getApp().getSpUtil().setOldBottle(5);
				}else {
					bottleNum = 5-bottles.size();
					MyApplication.getApp().getSpUtil().setOldBottle(5);
				}
				if(bottleNum>0){
					Message msg = Message.obtain();
					msg.what = DEAL_BOTTLES;
					msg.arg1 = bottleNum;
					myHandler.sendMessage(msg);
				}
			}
			
			break;
		}
	}
	
	// 查询用户资料信息
	private void getUserInfo(String userId) {
		// success set user
		JsonObject jo = new JsonObject();
		jo.addProperty("id", userId);
		
		BottleRestClient.post("queryUserInfo", this, jo, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				
				if (!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					UserDataModel model = gson.fromJson(str, UserDataModel.class);
					
					if (model != null && !TextUtils.isEmpty(model.getCode())) {
						if ("0".equals(model.getCode())) {
							MyApplication.getApp().getSpUtil().setUserHeadImage(model.getUserInfo().getHeadImg());
						} 
					} 
				}
			}
		
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
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
			if(intent.getBooleanExtra("isLogout", false)){//登出
				imageLoader.displayImage(null, cImage, options);
				//用户登出时，停止任务
				hasBrush  = 0;
				stopBottleTimer();
				stopProperTimer();
			}else{ //登陆
				String headImg ="";  
				if(UserManager.getInstance(context).getCurrentUser() != null && !TextUtils.isEmpty(UserManager.getInstance(context).getCurrentUser().getHeadImg())){
		    		headImg = UserManager.getInstance(context).getCurrentUser().getHeadImg();
		    	}
		    	imageLoader.displayImage(headImg, cImage, options);
//		    	getUserInfo(UserManager.getInstance(context).getCurrentUserId());
		    	if(intent.getBooleanExtra("isLogin", false)){//用户登陆时候，用户登陆时候；如果是用户变更资料，不需要启动任务；
		    		stopProperTimer();
		    		queryHasBrush();
		    	}
			}
		}
	}
	/**
	 * 更新相册、访客、捞网、关注上限值
	 */
	public void initCache(){
		//当前登录用户是null，则不处理缓存
		if(UserManager.getInstance(this).getCurrentUser()==null)
			return ;
		Integer flag = UserManager.getInstance(this).getCurrentUser().getIsVIP();
		UpdateCacheOfUpperLimit.getInstance().cache(flag==null?0:flag.intValue(),this);
	}
	
	private void startProperTimer(){  
		if(mProperTimer!=null&&mProperTimerTask!=null){
			mProperTimer.cancel();
			mProperTimer = null;
			mProperTimerTask.cancel();
			mProperTimerTask = null;
		}
		
        if (mProperTimer == null) {  
            mProperTimer = new Timer();  
        }  
        
        if (mProperTimerTask == null) {  
            mProperTimerTask = new TimerTask() {  
                @Override  
                public void run() {
                	//总进度超过5%才可以点击
                	if(pbProper.getProgress()<=100){
                		pbProper.setProgress((int)(MyApplication.getApp().getSpUtil().getSpqOkTime()/TIME));
                		if(pbProper.getProgress()<5){
                			Message msg = Message.obtain();
                			msg.arg1 = SWITCHPROP;
                			myHandler.sendMessage(msg);
                			
                			pbProper.setClickable(false);
                		}else if (pbProper.getProgress()>=5){
                			pbProper.setClickable(true);
                		}
                		if(pbProper.getProgress()==100){
                			Message msg = Message.obtain();
                			msg.arg1 = SWITCHPROP;
                			myHandler.sendMessage(msg);
                		}
                	}
                }  
            };  
        }  
  
        if(mProperTimer != null && mProperTimerTask != null )  
            mProperTimer.schedule(mProperTimerTask, delay, period);  
    }  
	
	 private void stopProperTimer(){  
         	pbProper.setProgress(0);
	        if (mProperTimer != null) {  
	            mProperTimer.cancel();  
	            mProperTimer = null;  
	        }  
	  
	        if (mProperTimerTask != null) {  
	            mProperTimerTask.cancel();  
	            mProperTimerTask = null;  
	        }     
	        
	        ivPropBg.setImageResource(R.drawable.pick_bottle__props_no);
	        pbProper.setProgress(0);
	}
	 
	 /**
		 * 初始化时比对时间，生成一次瓶子，然后周期执行
		 */
		private void startBottleTimer() {
			if(bottleTimer!=null&&bottleTask!=null){
				bottleTimer.cancel();
				bottleTimer = null;
				bottleTask .cancel();
				bottleTask = null;
			}
			if (bottleTimer == null) {
				bottleTimer = new Timer();
			}

			if (bottleTask == null) {
				bottleTask = new TimerTask() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						int bottleNum =0; //需要补充的瓶子数量
						long lastTime = MyApplication.getApp().getSpUtil().getGenerateOldTime(); //上一次瓶子刷新的时间
						
						if(System.currentTimeMillis()-lastTime>=10*60*1000){
							MyApplication.getApp().getSpUtil().setGenerateOldTime();
							if(bottles.size()==0){
								bottleNum = 5; 
								MyApplication.getApp().getSpUtil().setOldBottle(5);
							}else{
								if(bottles.size()>=5){
									bottleNum = 0;
								}else{
									bottleNum = 5-bottles.size();
								}
								MyApplication.getApp().getSpUtil().setOldBottle(5);
							}
						}else{
							bottleNum = MyApplication.getApp().getSpUtil().getBottles(bottles.size());
						}
						
						Message msg = Message.obtain();
						msg.what = DEAL_BOTTLES;
						msg.arg1 = bottleNum;
						myHandler.sendMessage(msg);
					}
				};
			}
			
			if(bottleTimer!=null&&bottleTask!=null){
				bottleTimer.schedule(bottleTask, 1000, 5000);
			}
		}

		private void stopBottleTimer() {
			if (bottleTimer != null) {
				bottleTimer.cancel();
				bottleTimer = null;
			}

			if (bottleTask != null) {
				bottleTask.cancel();
				bottleTask = null;
			}
		}
}