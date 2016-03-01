package com.hnmoma.driftbottle;

import java.io.File;
import java.util.Date;
import java.util.Random;
import java.util.Set;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.AnimRelativeLayout;
import com.hnmoma.driftbottle.custom.BuyDialog;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.model.BaseModelEx;
import com.hnmoma.driftbottle.model.BaseModelEx2;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.sensitivewords.SensitivewordFilter;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.way.ui.emoji.EmojiEditText;
import com.way.ui.emoji.EmojiKeyboard;
/**
 * 普通瓶
 * @author Administrator
 *
 */
public class Throw_ptpz extends BaseActivity implements OnClickListener, OnGestureListener {
	
	EmojiEditText mEditEmojicon;
	EmojiKeyboard emojicons;
	
	private RelativeLayout rl_second;
	private RelativeLayout rl_first;
	
	private AnimRelativeLayout chuck_bottle_layout;
	private ImageView chuck_spray1;
	private ImageView iv_attach;
	private ImageView bt_voice;
	private FrameLayout fl_fj;
	
	/**
	 * 扔瓶子动画
	 */
	ImageView iv_hand;
	TextView tv_hint;
	View ll_hint;
	private GestureDetector detector; 
	TextView tv_textnumber;
	TextView tv_title;
	/**
	 * 0.没有附件
	 * 1.图片附件
	 * 2.语音附件
	 * 
	 */
	int attachmentType = 0;
	
	AnimationSet set;
	
	Handler myHandler = new Handler();
	MyThread mt = new MyThread();
	
	InputMethodManager imm;
	
	CheckBox chuck_cb;
	boolean canBack = true;

	RelativeLayout rl_more;
	private RadioGroup rg_more;
//	private String bottleType;
	String submitBottleType;
	private boolean isThrowable = true;//扔瓶子的状态,默认情况下是可仍的；扔过一次后，就不允许再扔
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		setContentView(R.layout.throw_sspz);
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText(getResources().getString(R.string.common_bottle));
		rl_more = (RelativeLayout) findViewById(R.id.rl_more);
		rg_more = (RadioGroup) findViewById(R.id.rg_more);
		
		//设置默认值
		rg_more.check(R.id.rb_common);
		submitBottleType = "4000";
		//bottleType = ((RadioButton)rg_more.findViewById(R.id.rb_common)).getText().toString();
		
		rg_more.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId == R.id.rb_blast) {
					Integer flag = UserManager.getInstance(Throw_ptpz.this).getCurrentUser().getIsVIP();
					
					if(flag==null||flag.intValue() == 0) {
						showMsg("你不是VIP，不能扔爆破瓶");
						rg_more.check(R.id.rb_common);
						submitBottleType = "4000";
						tv_title.setText(getResources().getString(R.string.common_bottle));
						return;
					}
				}
				//bottleType = ((RadioButton) group.findViewById(checkedId)).getText().toString();
				if(checkedId == R.id.rb_common) {
					submitBottleType = "4000";
					tv_title.setText(getResources().getString(R.string.common_bottle));
				} else if(checkedId == R.id.rb_comment) {
					submitBottleType = "4011";
					tv_title.setText(getResources().getString(R.string.comment_bottle));
				} else {
					submitBottleType = "4008";
					tv_title.setText(getResources().getString(R.string.rpz_blasting_bottle));
				}
				//showMsg(submitBottleType + "");
			}
		});
		
		tv_textnumber = (TextView) findViewById(R.id.tv_textnumber);
		mEditEmojicon = (EmojiEditText) findViewById(R.id.chuck_edit);
		mEditEmojicon.requestFocus();  
		mEditEmojicon.setOnClickListener(this);
		mEditEmojicon.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				  int number = s.length(); 
				  tv_textnumber.setText(number + "/140"); 
			}
		});
		
		emojicons = (EmojiKeyboard) findViewById(R.id.emojicons);
		emojicons.setEventListener(new com.way.ui.emoji.EmojiKeyboard.EventListener() {
			@Override
			public void onEmojiSelected(String res) {
				EmojiKeyboard.input(mEditEmojicon, res);
			}
			@Override
			public void onBackspace() {
				EmojiKeyboard.backspace(mEditEmojicon);
			}
		});
		emojicons.setVisibility(View.GONE);
		
		rl_first = (RelativeLayout) findViewById(R.id.rl_first);
		rl_second = (RelativeLayout) findViewById(R.id.rl_second);
		
		iv_hand = (ImageView) findViewById(R.id.iv_hand);
		tv_hint = (TextView) findViewById(R.id.tv_hint);
		ll_hint = findViewById(R.id.ll_hint);
		chuck_bottle_layout = (AnimRelativeLayout) findViewById(R.id.chuck_bottle_layout);
		chuck_spray1=(ImageView) findViewById(R.id.chuck_spray1);
		detector = new GestureDetector(this, this);  
		chuck_bottle_layout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return detector.onTouchEvent(event);  
			}
		});
		
		fl_fj = (FrameLayout) findViewById(R.id.fl_fj);
		
		iv_attach=(ImageView) findViewById(R.id.iv_attach);
		iv_attach.setOnClickListener(this);
		bt_voice=(ImageView) findViewById(R.id.bt_voice);
		bt_voice.setOnClickListener(this);
		//适配
		DisplayMetrics dm = new DisplayMetrics();   
		this.getWindowManager().getDefaultDisplay().getMetrics(dm);   
		final int width = dm.widthPixels;   
        final int height = dm.heightPixels;  
		
		
		Animation animationR = AnimationUtils.loadAnimation(this, R.anim.bottle_throw1);
		Animation animationT = AnimationUtils.loadAnimation(this, R.anim.bottle_throw2);
		
		set = new AnimationSet(false);
		set.addAnimation(animationR);
		set.addAnimation(animationT);
		
		set.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				showDistance();
				
				chuck_bottle_layout.setVisibility(View.GONE);
				
				int [] location = new int [2];  
				chuck_bottle_layout.getLocationOnScreen(location);  
				
				float x = location[0];  
				float y = location[1];
//				//231*180
				x = (float) (x - width * 0.3);
				y = (float) (y - height * 0.5);
				
//				chuck_spray1.setX(x);
//				chuck_spray1.setY(y);
				
				RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) chuck_spray1.getLayoutParams();
				lp.leftMargin = (int) x; //Your X coordinate
				lp.topMargin = (int) y; //Your Y coordinate
				
				AnimationDrawable ad1 = (AnimationDrawable) chuck_spray1.getDrawable();
				chuck_spray1.setVisibility(View.VISIBLE);
				ad1.start();
				
				myHandler.postDelayed(mt, 450);
			}
		});
	}
	
	private void showDistance(){
//		int dis = MoMaUtil.random(100, 1000);
//		Toast.makeText(this, "恭喜甩出了"+dis+"米的成绩，瓶子已漂向远方", Toast.LENGTH_SHORT).show(); 
		showMsg(getResources().getStringArray(R.array.throwbottle_tip)[new Random().nextInt(3)]); 
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
			case R.id.bt_close:
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
					ready2Send();
//					doSubmit();
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
			case R.id.bt_pic:
				if(attachmentType==0 || attachmentType==1){
					showPickDialog();
				}else{
					//showMsg("没有可使用的定向瓶，先去道具商城逛逛吧");
					final BuyDialog dialogToBuy = new BuyDialog(Throw_ptpz.this, R.style.style_dialog_ballon, getResources().getString(R.string.confirm));
					dialogToBuy.show();
					dialogToBuy.setContent(null, getResources().getString(R.string.tip_bottol_dialog_audio));
					dialogToBuy.setOnSubmitClick(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							dialogToBuy.dismiss();
							uploadimgPath = "";
				       		attachmentType = 0;
				       		fl_fj.setVisibility(View.GONE);
				       		
				       		showPickDialog();
						}
					});
				}
				break;
			case R.id.bt_voice://更多
//				if(attachmentType==0 || attachmentType==2){
//					Intent intent = new Intent(this, AttachVoiceActivity.class);
//					if(attachmentType==0){
//						intent.putExtra("ifnewAttach", true);
//					}else if(attachmentType==2){
//						intent.putExtra("ifnewAttach", false);
//						intent.putExtra("filePath", uploadimgPath);
//					}
//					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//					startActivityForResult(intent, 1003);
//				}else{
//					final BuyDialog dialogToBuy = new BuyDialog(Throw_ptpz.this, R.style.style_dialog_ballon, getResources().getString(R.string.confirm));
//					dialogToBuy.show();
//					dialogToBuy.setContent(null, getResources().getString(R.string.tip_bottol_dialog_image));
//					dialogToBuy.setOnSubmitClick(new OnClickListener() {
//						
//						@Override
//						public void onClick(View v) {
//							dialogToBuy.dismiss();
//							uploadimgPath = "";
//				       		attachmentType = 0;
//				       		fl_fj.setVisibility(View.GONE);
//				       		
//				       		Intent intent = new Intent(Throw_ptpz.this, AttachVoiceActivity.class);
//							if(attachmentType==0){
//								intent.putExtra("ifnewAttach", true);
//							}else if(attachmentType==2){
//								intent.putExtra("ifnewAttach", false);
//								intent.putExtra("filePath", uploadimgPath);
//							}
//							intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//							startActivityForResult(intent, 1003);
//						}
//					});
//				}
				if(rl_more.getVisibility() == View.VISIBLE) {
					rl_more.setVisibility(View.GONE);
				} else {
					rl_more.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.iv_attach:
				if(attachmentType==1){
					Intent intent = new Intent(this, AttachImageActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("imagePath", uploadimgPath);
					startActivityForResult(intent, 1004);
				}else if(attachmentType==2){
					Intent intent = new Intent(this, AttachVoiceActivity.class);
					if(attachmentType==0){
						intent.putExtra("ifnewAttach", true);
					}else if(attachmentType==2){
						intent.putExtra("ifnewAttach", false);
						intent.putExtra("filePath", uploadimgPath);
					}
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivityForResult(intent, 1003);
				}
				break;
		}
	}
	
	public void ready2Send(){
//		rl_second.setVisibility(View.VISIBLE);
//		chuck_bottle_layout.startAnimation(set);
		
		rl_first.setVisibility(View.GONE);
		rl_second.setVisibility(View.VISIBLE);
		
		if(MyApplication.getApp().getSpUtil().getThrowHintFlag()){
			iv_hand.setVisibility(View.VISIBLE);
			ll_hint.setVisibility(View.VISIBLE);
			tv_hint.setVisibility(View.VISIBLE);
			
			Animation throwHand = AnimationUtils.loadAnimation(this, R.anim.throw_hand);
			iv_hand.startAnimation(throwHand);
		}
		
	}
	
	/**
	 * result true，内容没问题
	 * result false，内容有问题
	 */
	public boolean check(){
		boolean result = true;
		
		if(UserManager.getInstance(this).getCurrentUser()==null){
			Intent intent = new Intent(this, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			startActivityForResult(intent, 400);
			showMsg(getResources().getString(R.string.unlogin_msg));
			return result = false;
		}
		
		//检查内容
		String str = mEditEmojicon.getText().toString().trim();
		if(str.length()<5 && attachmentType!=2){
			showMsg("请至少输入五个字");
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
			showMsg("提交内容含有敏感词，请修正");
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
		
		result = isThrowable;
		
		return result;
	}
	
	/**
	 * result true，提交没问题
	 * result false，提交有问题
	 */
	public void doSubmit(){
		isThrowable = false;
		String str = mEditEmojicon.getText().toString().trim();
//		boolean checked = chuck_cb.isChecked();
		if(attachmentType==0){
			submitText(str);
		}else{
			submitAttachment(str,uploadimgPath);
		}
	}
	/**
	 * 提交文本和附件
	 * @param str 文本消息
	 * @param path 附件路径
	 */
	private void submitAttachment(String str, String path) {
		// TODO Auto-generated method stub
		RequestParams rp = new RequestParams();
		rp.put("userId", UserManager.getInstance(this).getCurrentUserId());
		rp.put("content", str);
		rp.put("remark", fileLength);
		
		rp.put("useType", "1");
		rp.put("bottleType", submitBottleType);
		rp.put("come", "6000");
		rp.put("isShare", "1");
		rp.put("addType", "9000");
		
		String contentType = "5000";
		if(attachmentType == 1){
			if(!TextUtils.isEmpty(path)){
				contentType = "5001";
			}
			try {
				rp.put("attathUrl", new File(path));
			} catch (Exception e) {
			}
		}else if(attachmentType == 2){
			if(!TextUtils.isEmpty(path)){
				contentType = "5004";
			}
			try {
				rp.put("attathUrl", new File(path));
			} catch (Exception e) {
			}
		}
		
		rp.put("contentType", contentType);
		
		BottleRestClient.postWithAttath("bottlefile", rp, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				super.onStart();
//				rl_first.setVisibility(View.GONE);
//				if (mpDialog == null) {
//					mpDialog = new MyProgressDialog(Throw_ptpz.this);
//				}
//				mpDialog.show();
//				mpDialog.setContent("正在密封瓶子...");
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
//				if (mpDialog != null && mpDialog.isShowing()) {
//					mpDialog.cancel();
//				}
				isThrowable = true;
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					try {
						BaseModelEx2 baseModel = gson.fromJson(str, BaseModelEx2.class);
						if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
							if("0".equals(baseModel.getCode())){
								boolean isFirstThrow = MyApplication.getApp().getSpUtil().isFirstTimeThrow();
								if(isFirstThrow) {
									getTaskJL("stask1002");
								}
								MyApplication.getApp().getSpUtil().setLastThrowTime();
//								ready2Send();
							}else{
								MoMaLog.d("debug", baseModel.getMsg());
							}
						}else{
//							showMsg("密封失败,请重试");
							rl_first.setVisibility(View.VISIBLE);
							rl_second.setVisibility(View.GONE);
						}
					} catch (JsonSyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						rl_first.setVisibility(View.VISIBLE);
						rl_second.setVisibility(View.GONE);
					}
				}else{
//					showMsg("密封失败,请重试");
					rl_second.setVisibility(View.GONE);
					rl_first.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//				showMsg("密封失败,请检查网络，或联系客服");
				rl_first.setVisibility(View.VISIBLE);
				rl_second.setVisibility(View.GONE);
			}
        });
	}

	/**
	 * 提交纯文本
	 * @param str 需要提交的文本内容
	 */
	private void submitText(String str) {
		// TODO Auto-generated method stub
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("content", str);
		jo.addProperty("contentType", "5000");
		jo.addProperty("useType", "1");
		jo.addProperty("bottleType", submitBottleType);
		jo.addProperty("come", "6000");
		jo.addProperty("isShare", 0);
		
		BottleRestClient.post("throwBottle", this, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				super.onStart();
//				rl_first.setVisibility(View.GONE);
//				if (mpDialog == null) {
//					mpDialog = new MyProgressDialog(Throw_ptpz.this);
//				}
//				mpDialog.show();
//				mpDialog.setContent("正在密封瓶子...");
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
//				if (mpDialog != null && mpDialog.isShowing()) {
//					mpDialog.cancel();
//				}
				isThrowable = true;
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					BaseModel baseModel = gson.fromJson(str, BaseModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
						if("0".equals(baseModel.getCode())){
							boolean isFirstThrow = MyApplication.getApp().getSpUtil().isFirstTimeThrow();
							if(isFirstThrow) {
								getTaskJL("stask1002");
							}
							MyApplication.getApp().getSpUtil().setLastThrowTime();
//							ready2Send();
						}else {
							MoMaLog.d("debug", baseModel.getMsg());
						}
					}else{
//						showMsg("密封失败,请重试");
						rl_first.setVisibility(View.VISIBLE);
						rl_second.setVisibility(View.GONE);
					}
				}else{
//					showMsg("密封失败,请重试");
					rl_first.setVisibility(View.VISIBLE);
					rl_second.setVisibility(View.GONE);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("请检查网络，或联系客服");
				rl_first.setVisibility(View.VISIBLE);
				rl_second.setVisibility(View.GONE);
			}
        });
	}

	/**
	 * 获取任务奖励
	 * @param type
	 */
	private void getTaskJL(String type){
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("deviceId", getDeviceId());
		jo.addProperty("type", type);
		jo.addProperty("come", "6000");
		jo.addProperty("version", getVersionCode());
		jo.addProperty("channel", getChannel());
		jo.addProperty("bottleId", "0");
		
		BottleRestClient.post("doTask", this, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				try {
					JSONObject obj = new JSONObject(new String(responseBody));
					if("0".equals(obj.getString("code"))){
						String str_money = obj.getString("shanbei");
						int money = MyApplication.getApp().getSpUtil().getMyMoney();
						if(MoMaUtil.isDigist(str_money)){
							money = Integer.valueOf(str_money);
						}else{
							money+=50;
						}
						showMsg(getResources().getString(R.string.firstthrow));
						MyApplication.getApp().getSpUtil().setMyMoney(money);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					MoMaLog.e("debug", e.getMessage());
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
			}
				
			@Override
			public void onFinish() {
				super.onFinish();
			}
		});
	}
	
	
    /**
	 * 选择提示对话框
	 */
	final String[] strarray = new String[] {"拍照", "从图库选择"};
	String cacheDir = MyApplication.mAppPath + "/.temp/upload/";
	File head;
    private void showPickDialog() {
		new AlertDialog.Builder(this)
			.setTitle("图片附件")
			.setItems(strarray, new DialogInterface.OnClickListener() {
				     @Override
				     public void onClick(DialogInterface dialog, int which) {
				    	 if(which == 0){
				    		dialog.dismiss();
				    		
				    		File file = new File(cacheDir);
				    		 if (!file.exists()) {
								file.mkdirs();
				    		 }
	                        outputFileUri = Uri.fromFile(new File(cacheDir, "uploadimg.jpg"));  
	  
	                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
	                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);  
	                        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
	                        startActivityForResult(cameraIntent, CAMERA_WITH_DATA);  
				    	 }else if(which == 1){
				    		dialog.dismiss();
							Intent intent = new Intent(Intent.ACTION_PICK, null);
							intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
							startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
				    	 }
				     }
			    })
			.show();
	}
    
    /*用来标识请求照相功能的activity*/        
    private static final int CAMERA_WITH_DATA = 1001;        
    /*用来标识请求gallery的activity*/        
    private static final int PHOTO_PICKED_WITH_DATA = 1002;  
    private Bitmap bitMap;       //用来保存图片   
    String uploadimgPath;
    int fileLength;
    private Uri outputFileUri;  
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == PHOTO_PICKED_WITH_DATA){
    		if(resultCode==Activity.RESULT_OK){
    			if(data == null){  
                    return;  
                }  
                  
                Uri uri = data.getData();  
                if(uri==null)
                	return ;
                String[] proj = { MediaStore.Images.Media.DATA };  
                //TODO  the below is null
                Cursor cursor = getContentResolver().query(uri, proj, null, null, null); // Order-by clause (ascending by name)  
                if(cursor==null){
                	MoMaLog.e("debug", "cursor is null");
                	return ;
                }
                	
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);  
                cursor.moveToFirst(); 
                
                uploadimgPath = cursor.getString(column_index); 
                if(TextUtils.isEmpty(uploadimgPath))
            		 return ;
                if (bitMap != null)      
                    bitMap.recycle();      
     			BitmapFactory.Options opts = new BitmapFactory.Options();
     			opts.inJustDecodeBounds = true;
     			BitmapFactory.decodeFile(uploadimgPath, opts);

     			opts.inSampleSize = MoMaUtil.computeSampleSize(opts, -1, 128 * 128);
     			// 这里一定要将其设置回false，因为之前我们将其设置成了true
     			opts.inJustDecodeBounds = false;
     			bitMap = BitmapFactory.decodeFile(uploadimgPath, opts);
      
                iv_attach.setImageBitmap(bitMap);
                fl_fj.setVisibility(View.VISIBLE);
                attachmentType = 1;
    		}
    	}else if(requestCode == CAMERA_WITH_DATA){
    		if(resultCode==Activity.RESULT_OK){
    			if(outputFileUri==null)
    				return ;
    			uploadimgPath = outputFileUri.getPath();
    			if(uploadimgPath==null)
    				return ;
           	 	if (bitMap != null)      
                  bitMap.recycle();      
           	 
           	 	BitmapFactory.Options opts = new BitmapFactory.Options();
    			opts = new BitmapFactory.Options();
    			opts.inJustDecodeBounds = true;
    			BitmapFactory.decodeFile(uploadimgPath, opts);

    			opts.inSampleSize = MoMaUtil.computeSampleSize(opts, -1, 128 * 128);
    			// 这里一定要将其设置回false，因为之前我们将其设置成了true
    			opts.inJustDecodeBounds = false;
    			bitMap = BitmapFactory.decodeFile(uploadimgPath, opts);
           	 
    	       	 iv_attach.setImageBitmap(bitMap);
    	       	 fl_fj.setVisibility(View.VISIBLE);
    	       	 attachmentType = 1;
    		}
    	}else if(requestCode == 1004){
    		if(resultCode == Activity.RESULT_FIRST_USER){
       		 uploadimgPath = "";
       		 attachmentType = 0;
       		 fl_fj.setVisibility(View.GONE);
       	 	}
    	}else if(requestCode == 1003){
    		if(resultCode == Activity.RESULT_FIRST_USER){
          		 uploadimgPath = "";
          		 attachmentType = 0;
          		 fl_fj.setVisibility(View.GONE);
          	 }else if(resultCode == Activity.RESULT_OK){
          		uploadimgPath = data.getStringExtra("filePath");
          		fileLength = data.getIntExtra("fileLength", 0);
          		iv_attach.setImageBitmap(null);
          		fl_fj.setVisibility(View.VISIBLE);
                attachmentType = 2;
          	 }
       	}
	}
	
	class MyThread implements Runnable{
		public void run() {
			chuck_spray1.setVisibility(View.GONE);
			finish();
		}
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

	private int verticalMinDistance = 120;  
	private int minVelocity         = 50;  
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (e1.getY() - e2.getY() > verticalMinDistance && Math.abs(velocityY) > minVelocity) { 
			canBack = false;
			chuck_bottle_layout.setOnTouchListener(null);
			
			if(MyApplication.getApp().getSpUtil().getThrowHintFlag()){
				iv_hand.clearAnimation();
				iv_hand.setVisibility(View.GONE);
				ll_hint.setVisibility(View.GONE);
				tv_hint.setVisibility(View.GONE);
				MyApplication.getApp().getSpUtil().setThrowHintFlag(false);
			}
			
			chuck_bottle_layout.startAnimation(set);
			doSubmit();
	    }
        return true;  
	}

	
}