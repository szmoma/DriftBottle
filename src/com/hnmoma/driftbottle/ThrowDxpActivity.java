package com.hnmoma.driftbottle;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.apache.http.Header;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.TextMessageBody;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.sensitivewords.SensitivewordFilter;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.way.ui.emoji.EmojiEditText;
import com.way.ui.emoji.EmojiKeyboard;
/*
 * 打招呼
 * 
 */
public class ThrowDxpActivity extends BaseActivity implements OnClickListener {
	
	EmojiEditText mEditEmojicon;
	EmojiKeyboard emojicons;
	ImageButton bt_ok;
	TextView tv_username;
//	TextView tv_textnumber;
	
	InputMethodManager imm;
	
	boolean canBack = true;
	
	private String userId;//要打招呼的用户
	private String visitUserId;//当前用户
	private String toUserName;
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("visitUserId", visitUserId);
		outState.putString("userId", userId);
		outState.putString("toUserName", toUserName);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null){
			userId = savedInstanceState.getString("userId");
			visitUserId = savedInstanceState.getString("visitUserId");
			toUserName = savedInstanceState.getString("toUserName");
		}
		
		Intent intent = getIntent();
		if(intent != null){
			userId = intent.getStringExtra("userId");
			visitUserId = intent.getStringExtra("visitUserId");
			toUserName = intent.getStringExtra("toUserName");
		}
		
		imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		setContentView(R.layout.fragment_throwdxp);
		bt_ok = (ImageButton) findViewById(R.id.bt_ok);
		tv_username = (TextView) findViewById(R.id.tv_title);
		tv_username.setText(Html.fromHtml("To <font color=\"#129AF2\">"+toUserName+"</font>"));
//		tv_textnumber = (TextView) findViewById(R.id.tv_textnumber);
		mEditEmojicon = (EmojiEditText) findViewById(R.id.chuck_edit);
		mEditEmojicon.setHint(getResources().getString(R.string.tv_tip_send));
		mEditEmojicon.requestFocus();  
		mEditEmojicon.setOnClickListener(this);
//		mEditEmojicon.addTextChangedListener(new TextWatcher() {
//					
//					@Override 
//					public void onTextChanged(CharSequence s, int start, int before, int count) {
//					}
//					@Override
//					public void beforeTextChanged(CharSequence s, int start, int count,
//							int after) {
//					}
//					
//					@Override
//					public void afterTextChanged(Editable s) {
//						  int number = s.length(); 
//						  tv_textnumber.setText(number + "/140"); 
//					}
//				});
		
		
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
		emojicons.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onBackPressed() {
		if(emojicons.getVisibility()!=View.INVISIBLE){
			emojicons.setVisibility(View.INVISIBLE);
		}else{
			super.onBackPressed();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_back:
				if(emojicons.getVisibility()!=View.INVISIBLE){
					emojicons.setVisibility(View.INVISIBLE);
				}else{
					this.finish();
				}
				break;
			case R.id.bt_ok:
				imm.hideSoftInputFromWindow(mEditEmojicon.getWindowToken(),0);
				//扔之前检查内容是否合法
				if(check()){
//					doSubmit();
					keepBottle();
					setResult(Activity.RESULT_OK);
					showMsg("打招呼成功");
					ThrowDxpActivity.this.finish();
				}
				break;
			case R.id.chuck_edit:
				mEditEmojicon.setHint("");
				emojicons.setVisibility(View.INVISIBLE);
				imm.showSoftInputFromInputMethod(mEditEmojicon.getWindowToken(), 0);
				break;
			case R.id.bt_emoji:
				if(emojicons.getVisibility()==View.INVISIBLE){
					imm.hideSoftInputFromWindow(mEditEmojicon.getWindowToken(),0);
					emojicons.setVisibility(View.VISIBLE);
				}else{
					emojicons.setVisibility(View.INVISIBLE);
				}
				break;
			case R.id.btn_sendgift:
				//打招呼送礼
				Intent intent = new Intent(this, LwscActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", userId);
				intent.putExtra("visitUserId", visitUserId);
				startActivity(intent);
				this.finish();
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
		if(str.length() == 0){
			if(dialog == null){
				dialog = new AlertDialog.Builder(this)
		                .setMessage("请输入内容")
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
		
		String gagTime = MyApplication.getApp().getSpUtil().getGagTime();
		if(!TextUtils.isEmpty(gagTime)){
			MoMaLog.e("禁言时间", gagTime);
			Date dateFrom = new Date(gagTime);
			Date dateTo = new Date(System.currentTimeMillis());//获取当前时间
			int day = MoMaUtil.getGapCount(dateTo, dateFrom);
			
			if(day>0){
				showMsg("经举报并核实，您的言论存在多次违规已被禁言,离解禁还有"+day+"天");
				return result = false;
			}else{
				MyApplication.getApp().getSpUtil().setGagTime("");
			}
		}
		
		return result;
	}
	
//	/**
//	 * result true，提交没问题
//	 * result false，提交有问题
//	 */
//	public void doSubmit(){
//		final String str = mEditEmojicon.getText().toString().trim();
//		
//				JsonObject jo = new JsonObject();
//				jo.addProperty("userId", visitUserId);
//				jo.addProperty("proId", "100001");
////				jo.addProperty("toUserId", userId);
////				jo.addProperty("content", str);
////				jo.addProperty("contentType", "5000");
////				jo.addProperty("bottleType", "4004");
////				jo.addProperty("come", "6000");
//				
//				BottleRestClient.post("useDirectBottle", this, jo, new AsyncHttpResponseHandler() {
//					@Override
//					public void onStart() {
//						super.onStart();
//						showDialog();
//					}
//					
//					@Override
//					public void onFinish() {
//						super.onFinish();
//						closeDialog();
//					}
//		
//					@Override
//					public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//						String rb = new String(responseBody);
//						if (!TextUtils.isEmpty(rb)) {
//							Gson gson = new Gson();
//							BaseModel model = gson.fromJson(rb, BaseModel.class);
//							if (model != null && !TextUtils.isEmpty(model.getCode())) {
//								if ("0".equals(model.getCode())) {
////									MyApplication.getApp().getSpUtil().setDjscCache(1, "");
//									
//									//发送定向瓶子
////									keepBottle(str);
//									setResult(Activity.RESULT_OK);
//									showMsg("发送成功");
//									ThrowDxpActivity.this.finish();
//								} else {
//									showMsg(model.getMsg());
//								}
//							} else {
//								showMsg("发送失败");
//							}
//						} else {
//							showMsg("发送失败");
//						}
//					}
//		
//					@Override
//					public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//						showMsg("发送失败");
//					}
//		        });
//	}
	
	/**
	 * 打招呼
	 */
	private void keepBottle(){
		String str = mEditEmojicon.getText().toString().trim();
		//敏感词处理
		SensitivewordFilter filter = MyApplication.getApp().getSensitiveFilter();
		Set<String> set = filter.getSensitiveWord(str, 1);
		if(set.size()!=0){
			showMsg("含有敏感词，请修正");
			return ;
		}
		
		EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
		CmdMessageBody cmdBody=new CmdMessageBody(MyConstants.MESSAGE_ATTR_ISSAYHELLO);
		cmdMsg.addBody(cmdBody); 
		
		//自定义扩展
		//头像
		if(!TextUtils.isEmpty(UserManager.getInstance(this).getCurrentUser().getHeadImg())){
			cmdMsg.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, MyApplication.getApp().getSpUtil().getUserHeadImage());
		}
		//内容
		cmdMsg.setAttribute(MyConstants.MSG_CONTENT, str);
		cmdMsg.setMsgTime(System.currentTimeMillis());
		cmdMsg.setReceipt(userId); //发送给某个人
		String id = UUID.randomUUID().toString();
		id = id.replace("-", "");
		cmdMsg.setMsgId(id);
		
		sendMsgInBackground(cmdMsg);
	}
	
	/**
	 * 这里瓶子内容可能会发送失败，后续讨论怎么处理
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 */
	public void sendMsgInBackground(final EMMessage message) {
		// 调用sdk发送异步发送方法
		EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

			@Override
			public void onSuccess() {
				EMMessage msg = EMMessage.createSendMessage(EMMessage.Type.TXT);
				if(!TextUtils.isEmpty(UserManager.getInstance(ThrowDxpActivity.this).getCurrentUser().getHeadImg())){
					msg.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, MyApplication.getApp().getSpUtil().getUserHeadImage());
				}
				
				msg.setAttribute(MyConstants.MESSAGE_ATTR_ISSAYHELLO, true);
				msg.setAttribute(MyConstants.MESSAGE_ATTR_NOTICETYPE, 0);
				
				TextMessageBody body = new TextMessageBody(message.getStringAttribute(MyConstants.MSG_CONTENT, ""));
				msg.addBody(body);
				msg.setFrom(message.getFrom());
				msg.setTo(message.getTo());
				msg.setUnread(false);
				msg.direct =message.direct;
				msg.setChatType(ChatType.Chat);
				msg.setMsgTime(message.getMsgTime());
				String id = UUID.randomUUID().toString();
				id = id.replace('-', ' ');
				msg.setMsgId(id);
				msg.status =  EMMessage.Status.SUCCESS;
				msg.setReceipt(message.getTo());
				
				EMChatManager.getInstance().saveMessage(msg,false);
			}

			@Override
			public void onError(int code, String error) {
			}

			@Override
			public void onProgress(int progress, String status) {
			}
		});

	}
	
	@Override
  	protected void onDestroy() {
  		super.onDestroy();
  	}
}