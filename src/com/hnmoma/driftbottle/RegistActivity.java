package com.hnmoma.driftbottle;

import org.apache.http.Header;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.UnionLoginModel;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.net.UpdateCacheOfUpperLimit;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;
/**
 * 用户注册页面，提交信息包括：邮箱地址、密码
 * @author yangsy
 *
 */
public class RegistActivity extends BaseActivity {
	private Button btnBack;	//返回
	private TextView tvTitle;	//标题
	private TextView tvProtocol;	//注册协议
	private EditText etMail; 	//邮箱地址
	private EditText etPwd,etPwd2;	//密码
	private Button btnRegister;	//注册的提交按钮
	private String url ;
	
	//点击按钮的时间限制，500ms仅有首次有效
	long waitTime = 3000L;  
	long touchTime = 0L;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		url = "http://ios.52plp.com/userprotocol.html";
		setupview();
		setListener();
	}
	

	/**
	 * 初始化view
	 */
	private void setupview() {
		// TODO Auto-generated method stub
		btnBack = (Button) findViewById(R.id.btn_back);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvProtocol = (TextView) findViewById(R.id.tv_protocol);
		etMail = (EditText) findViewById(R.id.et_mail);
		etPwd = (EditText) findViewById(R.id.et_psw);
		etPwd2 = (EditText) findViewById(R.id.et_psw2);
		btnRegister = (Button) findViewById(R.id.btn_register);
		
		tvTitle.setText(getResources().getString(R.string.register));
		String msg = "注册即表示同意《漂流瓶子用户协议》";
		bindProtocol(msg);
	}
	/**
	 * 控件绑定注册协议----超链接
	 * <p>
	 * 点击“漂流瓶子用户协议”，链接到协议页面
	 * </p>
	 * @param msg 需要显示的内容
	 * @param url 需要连接的地址
	 */
	private void bindProtocol(String msg,String url) {
		// TODO Auto-generated method stub
		SpannableStringBuilder ssb = new SpannableStringBuilder(msg);
		ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#b1b1b1")), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置前景色
		ssb.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置为斜体
		ssb.setSpan(new RelativeSizeSpan(1.2f), 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置字体，默认字体的1.2倍大小
		
		
		ssb.setSpan(new URLSpan(url), 7, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置超链接
		ssb.setSpan(new NoLineClickSpan(url), 7, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//删除下划线
		ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#eeb423")), 7, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置前景色
		ssb.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 7, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置为斜体
		
		tvProtocol.setText(ssb);
		tvProtocol.setMovementMethod(LinkMovementMethod.getInstance());//设置超链接为可点击状态
	}

	/**
	 * 控件绑定注册协议
	 * <p>
	 * 点击“漂流瓶子用户协议”，链接到协议页面
	 * </p>
	 * @param msg 需要显示的内容
	 * @param url 需要连接的地址
	 */
	private void bindProtocol(String msg) {
		// TODO Auto-generated method stub
		SpannableStringBuilder ssb = new SpannableStringBuilder(msg);
		ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#b1b1b1")), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置前景色
		ssb.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置为斜体
		ssb.setSpan(new RelativeSizeSpan(1.2f), 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置字体，默认字体的1.2倍大小
		ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#eeb423")), 7, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置前景色
		ssb.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 7, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置为斜体
		tvProtocol.setText(ssb);
		
		tvProtocol.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String msg =  ((TextView)v).getText().toString();
				SpannableStringBuilder ssb = new SpannableStringBuilder(msg);
				ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#b1b1b1")), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置前景色
				ssb.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置为斜体
				ssb.setSpan(new RelativeSizeSpan(1.2f), 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置字体，默认字体的1.2倍大小
				ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 7, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置前景色
				ssb.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 7, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置为斜体
				tvProtocol.setText(ssb);
				
				processHyperLinkClick(url);
			}
		});
	}
	/**
	 * 设置监听函数
	 */
	private void setListener() {
		// TODO Auto-generated method stub
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnRegister.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(TextUtils.isEmpty(etMail.getText().toString().trim())){
					showMsg("邮箱不能为空");
					return;
				}else if(!MoMaUtil.isEmail(etMail.getText().toString().trim())){
					showMsg("邮箱格式有误");
					return;
				}else if(TextUtils.isEmpty(etPwd.getText().toString().trim())){
					showMsg("密码不能为空");
					return;
				}else if(!(etPwd.getText().toString().trim()).equals(etPwd2.getText().toString().trim())){
					showMsg("密码不一致");
					return;
				}
				long currentTime = System.currentTimeMillis();
				 if((currentTime-touchTime)<waitTime) {  
			           touchTime = currentTime;  
			           return ;
				    }
				registTask();
			}
		});
	}
	
	/**
	 * 注册
	 */
	public void registTask(){
		if(!MoMaUtil.isEmail(etMail.getText().toString())){
			showMsg("邮箱格式有误");
			return ;
		}
		if(etPwd.getText().toString().length()>40){
  			showMsg("密码过长");
  			return ;
  		}
  		
  		if(etPwd.getText().toString().length()<4){
  			showMsg("密码太短");
  			return ;
  		}
  		
		if(TextUtils.isEmpty(MyApplication.deviceId)){
			MyApplication.deviceId = getDeviceId();
		}else if(TextUtils.isEmpty(MyApplication.channel)){
			MyApplication.channel = getChannel();
		}
		
		JsonObject jo = new JsonObject();
		jo.addProperty("userName", etMail.getText().toString().trim());
		String psw = etPwd.getText().toString().trim();
		psw = MoMaUtil.md5(psw);
		jo.addProperty("pwd", psw);
		
		jo.addProperty("deviceId", MyApplication.deviceId);
		jo.addProperty("come", "6000");
		jo.addProperty("nickName", "");
		jo.addProperty("channel", MyApplication.channel);
		
		BottleRestClient.post("reg", this, jo, new AsyncHttpResponseHandler (){
			@Override
			public void onStart() {
				super.onStart();
				showDialog("注册中...","注册失败...",15*1000);
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
					UnionLoginModel ulm = gson.fromJson(str, UnionLoginModel.class);
					if("0".equals(ulm.getCode())){
						final UserInfoModel ui = ulm.getUserInfo();
						if(ui!=null){
							UpdateCacheOfUpperLimit.getInstance().cache(ui.getIsVIP(), RegistActivity.this);
								EMChatManager.getInstance().login(ui.getHxUserName(), ui.getHxPassword(), new EMCallBack() {
									@Override
									public void onSuccess() {
										UserManager.getInstance(RegistActivity.this).login(ui);
										EMChatManager.getInstance().updateCurrentUserNick("您有一条新消息");
										
										//用户信息改变，发个广播通知
										Intent intent = new Intent();  
										intent.setAction(MyConstants.USERINFOCHANGE_BROADCAST_ACTION);  
										RegistActivity.this.sendBroadcast(intent);  
										
										setResult(Activity.RESULT_OK);
										RegistActivity.this.finish();
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
							showMsg("注册失败");
							closeDialog(mpDialog);
						}
					}else if("100001".equals(ulm.getCode())){
						showMsg("邮箱已被注册");
						closeDialog(mpDialog);
					}else{
						showMsg(ulm.getMsg());
						closeDialog(mpDialog);
					}
				}else{
					showMsg("注册失败");
					closeDialog(mpDialog);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("注册失败");
				closeDialog(mpDialog);
			}
        });
	}
	
	private class NoLineClickSpan extends ClickableSpan  {
		String url;	//链接地址

		public NoLineClickSpan(String url) {
		    this.url = url;
		}

		@Override
		public void updateDrawState(TextPaint ds) {
		    ds.setColor(ds.linkColor);
		    ds.setUnderlineText(false); //去掉下划线
		    ds.setColor(Color.parseColor("#FF0000"));
		}

		@Override
		public void onClick(View widget) {
			//此处实现点击超链接时调用
			widget.setBackgroundColor(Color.parseColor("#000000"));
			 processHyperLinkClick(url);
		}
		
		
	}
	/**
	 * 处理超链接的动作，启动到一个activity
	 * @param url 网址，传递到下一个页面的数据
	 */
	private void processHyperLinkClick(String url) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(RegistActivity.this,WebFrameNoCacheActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
	    intent.putExtra("webUrl", url);
	    intent.putExtra("isShowTitle", false);
	    startActivity(intent);
	}
	
	
}