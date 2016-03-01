package com.hnmoma.driftbottle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.conn.ConnectTimeoutException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.renren.Renren;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.TextMessageBody;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.Bottle;
import com.hnmoma.driftbottle.model.BottleDao;
import com.hnmoma.driftbottle.model.Chat;
import com.hnmoma.driftbottle.model.DaoSession;
import com.hnmoma.driftbottle.model.UnionLoginModel;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.net.UpdateCacheOfUpperLimit;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;

import de.greenrobot.dao.query.QueryBuilder;

public class LoginActivity extends BaseActivity implements OnClickListener, PlatformActionListener, Callback{
	private Handler handler;
	String userType = "";
	private boolean isFirst;//是否是新用户
	private EditText etMail;	//用户密码
	private EditText etPwd;	//密码
	
	//点击按钮的时间限制，3s仅有首次有效
	long waitTime = 3000L;  
	long touchTime = 0L;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("userType", userType);
	}
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler(this);
		if (savedInstanceState != null){
			userType = savedInstanceState.getString("userType");
		}
		initView();
	}
	
	public void initView(){
		setContentView(R.layout.activity_login);
		
		((TextView)findViewById(R.id.tv_title)).setText(getResources().getString(R.string.login));
		
		findViewById(R.id.btn_back).setOnClickListener(this);	
		findViewById(R.id.ll_qq).setOnClickListener(this);	
		findViewById(R.id.ll_sina).setOnClickListener(this);	
		findViewById(R.id.ll_rr).setOnClickListener(this);
		findViewById(R.id.go_register).setOnClickListener(this);
		findViewById(R.id.ll_webchat).setOnClickListener(this);
		
		etMail = (EditText) findViewById(R.id.et_mail);
		etPwd = (EditText) findViewById(R.id.et_psw);
	}
    
	@Override
	public void onClick(View v) {
		ShareSDK.initSDK(this);
		long currentTime = System.currentTimeMillis();
	   	 if((currentTime-touchTime)<waitTime&&v.getId()!=R.id.btn_back) {  
	           touchTime = currentTime;  
	           return ;
		    }
		switch (v.getId()) {
			case R.id.btn_back:
				setResult(Activity.RESULT_CANCELED);
				finish();
				break;
			case R.id.ll_qq:
				Platform plat = ShareSDK.getPlatform(this, QZone.NAME);
				if (plat.isValid()) {
					plat.removeAccount();
				}
				plat.setPlatformActionListener(this);
				plat.showUser(null);
				showDialog("正在登陆...","登陆失败",50*1000);
				
				userType = "1002";
				break;
			case R.id.ll_sina:
				plat = ShareSDK.getPlatform(this, SinaWeibo.NAME);
				if (plat.isValid()) {
					plat.removeAccount();
				}
				plat.setPlatformActionListener(this);
				plat.showUser(null);
				
				showDialog("正在登陆...","登陆失败",50*1000);
				
				userType = "1001";
				break;
			case R.id.ll_rr:
				plat = ShareSDK.getPlatform(this, Renren.NAME);
				if (plat.isValid()) {
					plat.removeAccount();
				}
				plat.setPlatformActionListener(this);
				plat.showUser(null);
				
				showDialog("正在登陆...","登陆失败",50*1000);
				
				userType = "1003";
				break;
			case R.id.go_register:
				Intent intent = new Intent(this, RegistActivity.class); 
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		        startActivityForResult(intent, 100);
				break;
			case R.id.btn_login:
				String mail = etMail.getText().toString().trim();
				String pwd = etPwd.getText().toString().trim() ;
				if(isVaile(mail,pwd)){
					loginTask(mail, pwd);
				}
				break;
			case R.id.ll_webchat://微信登陆
				//判断指定平台是否已经完成授权
				plat =  ShareSDK.getPlatform(this, Wechat.NAME);
				if (plat.isValid()) {
					plat.removeAccount();
				}
				plat.setPlatformActionListener(this);
				plat.showUser(null);
				
				showDialog("正在登陆...","登陆失败",50*1000);
				userType = "1006";
			break;
		}
	}
	

//	@Override
//	public void onBackPressed() {
//		if(pd != null && pd.isShowing()) {
//			pd.cancel();
//		} else {
//			super.onBackPressed();
//		}
//		
//	}
	
	
	@Override
	public void onCancel(Platform platform, int action) {
		Message msg = new Message();
		msg.arg1 = 3;
		msg.arg2 = action;
		msg.obj = platform;
		handler.sendMessage(msg);
	}

	@Override
	public void onComplete(Platform platform, int action, HashMap<String, Object> res) {
		Message msg = new Message();
		msg.arg1 = 1;
		msg.arg2 = action;
		msg.obj = platform;
		handler.sendMessage(msg);
	}

	@Override
	public void onError(Platform platform, int action, Throwable t) {
		Message msg = new Message();
		msg.arg1 = 2;
		msg.arg2 = action;
		msg.obj = t;
		handler.sendMessage(msg);
		MoMaLog.e("debug",t.getMessage());
	}

	/** 处理操作结果 */
	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.arg1) {
			case 1: 
				unionLogin((Platform) msg.obj);
				break;
			case 2: 
				closeDialog(mpDialog);
				if("WechatClientNotExistException".equals(msg.obj.getClass().getSimpleName())){
					showMsg(getResources().getString(R.string.not_install_wechat));
				}else{
					showMsg("登录失败");
				}
				
				break;
			case 3: 
				showMsg("用户取消");
				closeDialog(mpDialog);
				break;
		}
		return false;
	}
	
	private void unionLogin(Platform platform){
		String icon = platform.getDb().getUserIcon();
		String userName = platform.getDb().getUserName();
		String userId = platform.getDb().getUserId();
		//性别
		String gender = platform.getDb().getUserGender();
		gender=gender==null?"":gender;
		if(gender.equals("f")){
			gender = "2007";
		}else{
			gender = "2006";
		}
		
		if(TextUtils.isEmpty(MyApplication.deviceId)){
			MyApplication.deviceId = getDeviceId();
		}else if(TextUtils.isEmpty(MyApplication.channel)){
			MyApplication.channel = getChannel();
		}
		
		JsonObject jo = new JsonObject();
		jo.addProperty("userType", userType);
		jo.addProperty("partUserId", userId);
		jo.addProperty("headImg", icon);
		jo.addProperty("nickName", userName);
		jo.addProperty("identityType", gender);
		jo.addProperty("come", "6000");
		jo.addProperty("deviceId", getDeviceId());
		jo.addProperty("channel", getChannel());
		
		BottleRestClient.post("unionLogin", this, jo, new AsyncHttpResponseHandler (){
			@Override
			public void onFinish() {
				super.onFinish();
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					UnionLoginModel ulm = gson.fromJson(str, UnionLoginModel.class);
					if("0".equals(ulm.getCode())){
						final UserInfoModel ui = ulm.getUserInfo();
						if(ui!=null){
							//环信登陆
							UpdateCacheOfUpperLimit.getInstance().cache(ui.getIsVIP(), LoginActivity.this);
							EMChatManager.getInstance().login(ui.getHxUserName(), ui.getHxPassword(), new EMCallBack() {
								@Override
								public void onSuccess() {
									MoMaLog.d("EmChat", "login success");
									closeDialog(mpDialog);
									UserManager.getInstance(LoginActivity.this).login(ui);
									EMChatManager.getInstance().updateCurrentUserNick("您有一条新消息");
									isFirst = ui.getIsNew()==1?true:false;
									if(isFirst){	//新用户
										//启动到完善信息的页面
										Intent intent = new Intent(LoginActivity.this, AppendPersonInfoActivity.class); 
										intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
										intent.putExtra("userId", UserManager.getInstance(LoginActivity.this).getCurrentUserId());
										intent.putExtra("isFirst", true);
								        startActivity(intent);
									}else{
										sendUpdateUserInfo();
									}
									closeDialog(mpDialog);
									setResult(Activity.RESULT_OK);
									LoginActivity.this.finish();
								}
								@Override
								public void onProgress(int arg0, String arg1) {
								}
								@Override
								public void onError(int arg0, final String arg1) {
									runOnUiThread(new Runnable(){    
							            public void run(){    
							            	showMsg("登录失败:"+arg1);
							            	closeDialog(mpDialog);
							            }    
							    
							        });    
								}
							});
						}else{
							showMsg("登录失败");
							closeDialog(mpDialog);
						}
					}else if("100013".equals(ulm.getCode())){
						showMsg("注册用户已超限");
					}else{
						showMsg(ulm.getMsg());
					}
					
					
				}else{
					showMsg("登录失败");
					closeDialog(mpDialog);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				if(error!=null&&!TextUtils.isEmpty(error.getMessage())&&error.getMessage().contains("timed out")){
					showMsg("连接超时"); 
				}else
					showMsg("登录失败");
				closeDialog(mpDialog);
			}
        });
	}
	
//	private void dealOldDate(){
//		//瓶友迁移
//		DaoSession daoSession = MyApplication.getApp().getDaoSession();
//		BottleDao bottleDao = daoSession.getBottleDao();
//		
//		QueryBuilder<Bottle> qb = bottleDao.queryBuilder();
//		if(UserManager.getInstance(this).getCurrentUser()==null){
//			qb.where(BottleDao.Properties.Belongto.eq("0000000000"));
//		}else{
//			qb.where(BottleDao.Properties.Belongto.eq(UserManager.getInstance(this).getCurrentUserId()));
//		}
//		
//		qb.where(BottleDao.Properties.BottleSort.eq("3001"));
//		qb.orderDesc(BottleDao.Properties.CreateTime);
//		qb.limit(20).offset(0);
//		List<Bottle> list = qb.list();
//		for(Bottle bottle : list){
//			List chats = bottle.getBottleToChats();
//			moveBottle(chats);
//		}
//	}
	
	public void moveBottle(List<Chat> chats){
		for(Chat chat:chats){
			boolean isCome = chat.getIsComMsg();
			EMMessage msg;
			if(isCome){
				msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
			}else{
				msg = EMMessage.createSendMessage(EMMessage.Type.TXT);
			}
			
			TextMessageBody body = new TextMessageBody(chat.getContent());
			msg.addBody(body);
			
			msg.setChatType(ChatType.Chat);
			msg.setMsgId(getId());

			
			msg.setTo(UserManager.getInstance(this).getCurrentUserId());
			msg.setFrom(chat.getUserId());
			msg.setMsgTime(System.currentTimeMillis());
			
			//这个方法false是导到数据库，不保存到内存。true保存到内存，但会响铃加两条消息的字样
//			EMChatManager.getInstance().importMessage(msg, true);
			
			//这个方法false是不响铃。true响铃加两条消息的字样
			EMChatManager.getInstance().saveMessage(msg, false);
//			EMChatManager.getInstance().getConversation(userId).resetUnsetMsgCount();
		}
	}
	
	public synchronized static String getId() {
		StringBuilder builder = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		builder.append(sdf.format(new Date()));
		return builder.toString();
	}
	
  	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 100){//官方注册
			if(resultCode == Activity.RESULT_OK){
				Intent intent = new Intent(this, AppendPersonInfoActivity.class); 
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", UserManager.getInstance(this).getCurrentUserId());
				intent.putExtra("isUnionLogin", true);
		        startActivity(intent);
		        
		        setResult(Activity.RESULT_OK);
		        finish();
			}
		}else if(requestCode == 200){//官方登陆
			if(resultCode == Activity.RESULT_OK){
				setResult(Activity.RESULT_OK);
				LoginActivity.this.finish();
			}
		}
	}
  	/**
  	 * 验证邮箱以及密码是否有效
  	 * @param mail 邮箱账号
  	 * @param pwd 密码
  	 * @return 如果邮箱、密码是空字符串，以及邮箱格式不正确，则返回false；否则就返回true
  	 */
  	private boolean isVaile(String mail,String pwd){
  		if(TextUtils.isEmpty(mail)){
			showMsg("邮箱不能为空");
			return false;
		}else if(!MoMaUtil.isEmail(mail)){
			showMsg("邮箱格式有误");
			return false;
		}else if(TextUtils.isEmpty(pwd)){
			showMsg("密码不能为空");
			return false;
		}
  		return true;
  	}
  	/**
  	 * 登陆任务
  	 * @param mail 邮箱账号
  	 * @param pwd 密码
  	 */
  	public void loginTask(String mail,String pwd){
  		if(!MoMaUtil.isEmail(mail)){
  			showMsg("邮箱格式错误");
  			return ;
  		}
  		if(pwd.length()>40){
  			showMsg("密码过长");
  			return ;
  		}
  		
//  		if(pwd.length()<4){
//  			showMsg("密码太短");
//  			return ;
//  		}
  			
		JsonObject jo = new JsonObject();
		jo.addProperty("userName", mail);
		pwd = MoMaUtil.md5(pwd);
		jo.addProperty("pwd", pwd);
		
		BottleRestClient.post("login", this, jo, new AsyncHttpResponseHandler (){
			@Override
			public void onStart() {
				super.onStart();
				showDialog("正在登陆...","登陆失败",50*1000);
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
					UnionLoginModel ulm = gson.fromJson(str, UnionLoginModel.class);
					
					if(ulm.getCode().equals("0")){
						final UserInfoModel ui = ulm.getUserInfo();
						if(ui!=null){
							UpdateCacheOfUpperLimit.getInstance().cache(ui.getIsVIP(), LoginActivity.this);
							//环信登陆
							EMChatManager.getInstance().login(ui.getHxUserName(), ui.getHxPassword(), new EMCallBack() {
								@Override
								public void onSuccess() {
									UserManager.getInstance(LoginActivity.this).login(ui);
									EMChatManager.getInstance().updateCurrentUserNick("您有一条新消息");
									//用户信息改变，发个广播通知
									sendUpdateUserInfo();
									closeDialog(mpDialog);
									setResult(Activity.RESULT_OK);
									LoginActivity.this.finish();
								}
								@Override
								public void onProgress(int arg0, String arg1) {
								}
								@Override
								public void onError(int arg0, final String arg1) {
									runOnUiThread(new Runnable(){    
							            public void run(){    
							            	showMsg("登录失败:"+arg1);
							            }    
							    
							        });    
								}
							});
						}else{
							showMsg("登录失败");
							closeDialog(mpDialog);
						}
					}else{
						showMsg(ulm.getMsg());
						closeDialog(mpDialog);
					}
					
				}else{
					showMsg("登录失败");
					closeDialog(mpDialog);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("登陆失败");
				closeDialog(mpDialog);
			}
        });
	}

  	/**
  	 * 发送用户登陆成功的广播
  	 */
  	private void sendUpdateUserInfo(){
  		Intent intent = new Intent();  
		intent.setAction(MyConstants.USERINFOCHANGE_BROADCAST_ACTION);  
		intent.putExtra("isLogin", true);
		sendBroadcast(intent); 
  	}
}