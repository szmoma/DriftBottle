package com.hnmoma.driftbottle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.BottleMsgManager;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.TimeOutProgressDialog;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.model.BaseModelEx2;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.MyGiftsModel;
import com.hnmoma.driftbottle.model.QueryUserInfoModel;
import com.hnmoma.driftbottle.model.Stranger;
import com.hnmoma.driftbottle.model.User;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.sensitivewords.SensitivewordFilter;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.way.ui.emoji.EmojiEditText;
import com.way.ui.emoji.EmojiKeyboard;
/**
 * 答谢瓶子
 *
 */
public class ThrowDaxiePingActivity extends BaseActivity implements OnClickListener {
	
	EmojiEditText mEditEmojicon;
	EmojiKeyboard emojicons;
	ImageButton bt_ok;
	Button btn_sendgift;
	InputMethodManager imm;
	
	boolean canBack = true;
	
	private String userId;
	//private String visitUserId;
	private int position;
	private MyGiftsModel myGiftsModel;
	
	TextView tv_title;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//outState.putString("visitUserId", visitUserId);
		outState.putString("userId", userId);
		outState.putInt("position", position);
		outState.putSerializable("myGiftsModel", myGiftsModel);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null){
			userId = savedInstanceState.getString("userId");
			//visitUserId = savedInstanceState.getString("visitUserId");
			position = savedInstanceState.getInt("position");
			myGiftsModel = (MyGiftsModel) savedInstanceState.getSerializable("myGiftsModel");
		}
		
		Intent intent = getIntent();
		if(intent != null){
			userId = intent.getStringExtra("userId");
			//visitUserId = intent.getStringExtra("visitUserId");
			position = intent.getIntExtra("position", 0);
			myGiftsModel = (MyGiftsModel) intent.getSerializableExtra("myGiftsModel");
		}
		
		imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		setContentView(R.layout.fragment_throwdxp);
		bt_ok = (ImageButton) findViewById(R.id.bt_ok);
		
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText("答谢瓶");
		
		mEditEmojicon = (EmojiEditText) findViewById(R.id.chuck_edit);
		mEditEmojicon.requestFocus();  
		mEditEmojicon.setOnClickListener(this);
		mEditEmojicon.setText("谢谢你的礼物么么哒~聊点什么好呢？");
		
		btn_sendgift = (Button) findViewById(R.id.btn_sendgift);
		btn_sendgift.setVisibility(View.GONE);
		
		emojicons = (EmojiKeyboard) findViewById(R.id.emojicons);
		emojicons.setEventListener(new com.way.ui.emoji.EmojiKeyboard.EventListener() {
			
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
		emojicons.setVisibility(View.GONE);
	}

	@Override
	public void onBackPressed() {
		if(emojicons.getVisibility()!=View.GONE){
			emojicons.setVisibility(View.GONE);
		}else{
			super.onBackPressed();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_back:
				if(emojicons.getVisibility()!=View.GONE){
					emojicons.setVisibility(View.GONE);
				}else{
					this.finish();
				}
				break;
			case R.id.bt_ok:
				imm.hideSoftInputFromWindow(mEditEmojicon.getWindowToken(),0);
				//扔之前检查内容是否合法
				if(check()){
					doSubmit();
				}
				break;
			case R.id.chuck_edit:
				mEditEmojicon.setHint("");
				emojicons.setVisibility(View.GONE);
				imm.showSoftInputFromInputMethod(mEditEmojicon.getWindowToken(), 0);
				break;
			case R.id.bt_emoji:
				if(emojicons.getVisibility()==View.GONE){
					imm.hideSoftInputFromWindow(mEditEmojicon.getWindowToken(),0);
					emojicons.setVisibility(View.VISIBLE);
				}else{
					emojicons.setVisibility(View.GONE);
				}
				break;
		}
	}
	
	Dialog dialog;
	/**
	 * result true，内容没问题
	 * result false，内容有问题
	 */
	public boolean check(){
		boolean result = true;
		
		//检查内容
		String str = mEditEmojicon.getText().toString().trim();
		if(str.length()<5){
			if(dialog == null){
				dialog = new AlertDialog.Builder(this)
		                .setMessage("请至少输入五个字")
		                .setNegativeButton("确定", null)
		                .create(); 
			}
			
			dialog.show();
			return result = false;
		}
		
		//检查网络
		if(!MoMaUtil.isNetworkAvailable(this)){
			showMsg("当前网络不可用，请检查");
			return result = false;
		}
		
		//敏感词处理
		SensitivewordFilter filter = MyApplication.getApp().getSensitiveFilter();
		Set<String> set = filter.getSensitiveWord(str, 1);
		if(set.size()!=0){
			if(dialog == null){
				dialog = new AlertDialog.Builder(this)
		                .setMessage("提交内容含有敏感词，请修正")
		                .setNegativeButton("确定", null)
		                .create(); 
			}
			
			dialog.show();
			return result = false;
		}
		
		return result;
	}
	
	/**
	 * result true，提交没问题
	 * result false，提交有问题
	 */
	public void doSubmit(){
		final String str = mEditEmojicon.getText().toString().trim();
		
				JsonObject jo = new JsonObject();
//				jo.addProperty("userId", visitUserId);
				jo.addProperty("giftUId", myGiftsModel.getGiftUId());
//				jo.addProperty("content", str);
//				jo.addProperty("contentType", "5000");
//				jo.addProperty("toUserId", myGiftsModel.getUserId());
				BottleRestClient.post("thkGift", this, jo, new AsyncHttpResponseHandler(){
					@Override
					public void onStart() {
						super.onStart();
						showDialog("努力加载...","加载失败...",20*1000);
					}
					
					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
						String rb = new String(responseBody);
						if (!TextUtils.isEmpty(rb)) {
							Gson gson = new Gson();
							BaseModelEx2 model = gson.fromJson(rb, BaseModelEx2.class);
							if (model != null && !TextUtils.isEmpty(model.getCode())) {
								if ("0".equals(model.getCode())) {
									//发送定向瓶子
									keepBottle(str);
									
									showMsg("答谢成功");
									Intent intent = new Intent();
									intent.putExtra("position", position);
									setResult(Activity.RESULT_OK, intent);
									ThrowDaxiePingActivity.this.finish();
								} else {
									showMsg(model.getMsg());
									closeDialog(mpDialog);
								}
							} else {
								showMsg("发送失败");
							}
						} else {
							showMsg("发送失败");
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
						showMsg("发送失败");
						closeDialog(mpDialog);
					}
					
					@Override
					public void onFinish() {
						super.onFinish();
					}
		        });
	}
	
	
	/**
	 * 保存并发送瓶子
	 */
	private void keepBottle(String str){
		//注册消息
		EMMessage msg = EMMessage.createSendMessage(EMMessage.Type.TXT);
		msg.setAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 1);
		msg.setAttribute(MyConstants.MESSAGE_ATTR_BOTTLETYPE, 1);
		
		BottleModel bottleModel = new BottleModel();
		User currentUser = UserManager.getInstance(this).getCurrentUser();
		
		UserInfoModel userInfoModel = new UserInfoModel();
		userInfoModel.setUserId(currentUser.getUserId());
		userInfoModel.setHeadImg(currentUser.getHeadImg());
		userInfoModel.setNickName(currentUser.getNickName());
		userInfoModel.setIdentityType(currentUser.getIdentityType());
		userInfoModel.setProvince(currentUser.getProvince());
		userInfoModel.setCity(currentUser.getCity());
		userInfoModel.setIsVIP(currentUser.getIsVIP());
		userInfoModel.setAge(currentUser.getAge());
		
		bottleModel.setUserInfo(userInfoModel);
		bottleModel.setCreateTime(new Date());
		bottleModel.setContent(str);
		bottleModel.setBottleType("4005");
		bottleModel.setContentType("5000");
		
		Gson gson = new Gson();
		String bottle = gson.toJson(bottleModel, BottleModel.class);
		msg.setAttribute(MyConstants.MESSAGE_ATTR_BOTTLE, bottle);
		
		TextMessageBody body = new TextMessageBody(bottleModel.getContent());
		msg.addBody(body);
		
//		msg.setTo(UserManager.getInstance(this).getCurrentUserId());
//		msg.setFrom(bottleModel.getUserInfo().getUserId());
		msg.setReceipt(myGiftsModel.getUserId());
		msg.setMsgTime(System.currentTimeMillis());
		msg.setMsgId(getId());
		
		sendMsgInBackground(msg);
	}
	
	public synchronized static String getId() {
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
				closeDialog(mpDialog);
			}

			@Override
			public void onProgress(int progress, String status) {
			}
		});
		saveStranger(myGiftsModel.getUserId());

	}
	/**
	 * 设置当前好友与自己是可以发送消息的
	 */
	public void saveStranger(String userId){
		final Stranger stranger =MyApplication.getApp().getDaoSession().getStrangerDao().load(userId);
		if(stranger==null){
			JsonObject jo = new JsonObject();
			jo.addProperty("id", userId);
			BottleRestClient.post("queryUserInfo", ThrowDaxiePingActivity.this, jo, new AsyncHttpResponseHandler (){
				@Override
				public void onStart() {
					super.onStart();
				}
				
				@Override
				public void onFinish() {
					super.onFinish();
					closeDialog(mpDialog);
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
									model.setState(1);
									
									//更新数据库
									BottleMsgManager.getInstance( ThrowDaxiePingActivity.this).insertOrReplaceStranger(model);
							 }
						 }
					}
					
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					closeDialog(mpDialog);
				}
	        });
				
		}else{
			if(stranger.getState()!=1||
					(!TextUtils.isEmpty(myGiftsModel.getHeadImg())&&
							myGiftsModel.getHeadImg().equals(stranger.getHeadImg()))){
				boolean isChange = false;
				if(stranger.getState()==1){
					stranger.setState(1);
					isChange = true;
				}
					
				if(!TextUtils.isEmpty(myGiftsModel.getHeadImg())&&myGiftsModel.getHeadImg().equals(stranger.getHeadImg())){
					stranger.setHeadImg(myGiftsModel.getHeadImg());
					isChange = true;
				}
				if(isChange)
					MyApplication.getApp().getDaoSession().getStrangerDao().insertOrReplace(stranger);
				closeDialog(mpDialog);
			}
		}
	}
}