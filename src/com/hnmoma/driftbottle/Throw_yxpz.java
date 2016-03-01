package com.hnmoma.driftbottle;

import java.util.Random;

import org.apache.http.Header;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.AnimRelativeLayout;
import com.hnmoma.driftbottle.custom.CustomDialog;
import com.hnmoma.driftbottle.custom.CustomDialog.CustomDialogClickListener;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.model.UserAccountInfo;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
/**
 * 划拳瓶
 * @author Administrator
 *
 */
public class Throw_yxpz extends BaseActivity implements OnClickListener, OnGestureListener {
	public final int OK_QCM = 1;	//异步查询魅力值与金钱成功
	
	private RelativeLayout rl_second;
	private RelativeLayout rl_first;
	
	private AnimRelativeLayout chuck_bottle_layout;
	private ImageView chuck_spray1;
	
	AnimationSet set;
	/**
	 * 扔瓶子动画
	 */
	ImageView iv_hand;
	TextView tv_hint;
	View ll_hint;
	private GestureDetector detector; 
	
	Handler myHandler = new Handler();
	MyThread mt = new MyThread();
	boolean canBack = true;

	Random random = new Random();
	RadioGroup rg_money,rg_01,rg_02,rg_03;
	private int domoney;	//提交的价格
	private int no1 = -1,no2 = -1 ,no3 =-1;//第一、二、三局的值
	
	private boolean isShowTrack;//是否显示扔瓶子的轨迹，如果值是false，则是邂逅猜拳
	private String fromUserId;	//游戏挑战者
	private String toUserId;//游戏接收者
	private boolean isThrowable = true;//扔瓶子的状态,默认情况下是可仍的；扔过一次后，就不允许再扔
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.throw_yxpz);
        setupView();
        init();
		setLinstener();
	}

	private void init() {
		// TODO Auto-generated method stub
		Animation animationR = AnimationUtils.loadAnimation(this, R.anim.bottle_throw1);
		Animation animationT = AnimationUtils.loadAnimation(this, R.anim.bottle_throw2);
		set = new AnimationSet(false);
		set.addAnimation(animationR);
		set.addAnimation(animationT);
		
		//RadioGroup默认，并默认提交值
		rg_money.check(R.id.rb_12);
		String str_money = ((RadioButton)rg_money.findViewById(R.id.rb_12)).getText().toString();//带有单位的额度
		domoney = transMoney(str_money);
		
		if(getIntent()!=null){
			isShowTrack = getIntent().getBooleanExtra("isShowTrack", true);
		}
		if(getIntent()!=null){
			toUserId = getIntent().getStringExtra("userId");
			fromUserId = getIntent().getStringExtra("visitUserId");
		}
		
	}

	private void setLinstener() {
		// TODO Auto-generated method stub
		DisplayMetrics dm = new DisplayMetrics();   
		this.getWindowManager().getDefaultDisplay().getMetrics(dm);   
		final int width = dm.widthPixels;   
        final int height = dm.heightPixels; 
        
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
				if(!isShowTrack){
					myHandler.postDelayed(mt, 450);
					return ;
				}
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
		rg_money.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				String str_money =((RadioButton) group.findViewById(checkedId)).getText().toString();
				
				domoney = transMoney(str_money);
			}
		});
		
		rg_01.findViewById(R.id.rb_st).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_01);
				((RadioButton)rg_01.findViewById(R.id.rb_st)).setChecked(true);
				no1 = getNumById(R.id.rb_st);
			}
		});
		
		rg_01.findViewById(R.id.rb_jd).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_01);
				((RadioButton)rg_01.findViewById(R.id.rb_jd)).setChecked(true);
				no1 = getNumById(R.id.rb_jd);
			}
		});
		
		rg_01.findViewById(R.id.rb_bu).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_01);
				((RadioButton)rg_01.findViewById(R.id.rb_bu)).setChecked(true);
				no1 = getNumById(R.id.rb_bu);
			}
		});
		
		rg_02.findViewById(R.id.rb_st).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_02);
				((RadioButton)rg_02.findViewById(R.id.rb_st)).setChecked(true);
				no2 = getNumById(R.id.rb_st);
			}
		});
		
		rg_02.findViewById(R.id.rb_jd).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_02);
				((RadioButton)rg_02.findViewById(R.id.rb_jd)).setChecked(true);
				no2 = getNumById(R.id.rb_jd);
			}
		});
		
		rg_02.findViewById(R.id.rb_bu).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_02);
				((RadioButton)rg_02.findViewById(R.id.rb_bu)).setChecked(true);
				no2 = getNumById(R.id.rb_bu);
			}
		});
		
		rg_03.findViewById(R.id.rb_st).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_03);
				((RadioButton)rg_03.findViewById(R.id.rb_st)).setChecked(true);
				no3 = getNumById(R.id.rb_st);
			}
		});
		
		rg_03.findViewById(R.id.rb_jd).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_03);
				((RadioButton)rg_03.findViewById(R.id.rb_jd)).setChecked(true);
				no3 = getNumById(R.id.rb_jd);
			}
		});
		
		rg_03.findViewById(R.id.rb_bu).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_03);
				((RadioButton)rg_03.findViewById(R.id.rb_bu)).setChecked(true);
				no3 = getNumById(R.id.rb_bu);
			}
		});
		
		findViewById(R.id.bt_close).setOnClickListener(this);
		findViewById(R.id.ib_jx).setOnClickListener(this);
		findViewById(R.id.bt_ok).setOnClickListener(this);
		
	}
	private void setupView() {
		// TODO Auto-generated method stub
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
		
		rg_money = (RadioGroup) findViewById(R.id.rg_money);
		rg_01 = (RadioGroup) findViewById(R.id.rg_01);
		rg_02 = (RadioGroup) findViewById(R.id.rg_02);
		rg_03 = (RadioGroup) findViewById(R.id.rg_03);
	}

	private void showDistance(){
//		int dis = MoMaUtil.random(100, 1000);
//		Toast.makeText(this, "恭喜甩出了"+dis+"米的成绩，瓶子已漂向远方", Toast.LENGTH_SHORT).show();
		if(isShowTrack)
			showMsg(getResources().getStringArray(R.array.throwbottle_tip)[new Random().nextInt(3)]); 
		else{
			if(TextUtils.isEmpty(fromUserId)||TextUtils.isEmpty(toUserId)){ //随机挑战
				showMsg("已提交,请等待邂逅...");
			}else //定向挑战
				showMsg("已发起挑战,请等候应战...");
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_close:
				this.finish();
				break;
			case R.id.bt_ok:
				//扔之前检查内容是否合法
				if(check()){
					queryCharmAndMoney();
				}
				break;
			case R.id.ib_jx:	//帮我选择
				clearRadioButtonState(rg_01);
				clearRadioButtonState(rg_02);
				clearRadioButtonState(rg_03);
				
				int checkId = getRandomId();
				((RadioButton)rg_01.findViewById(checkId)).setChecked(true);
				no1 = getNumById(checkId);
				
				checkId = getRandomId();
				((RadioButton)rg_02.findViewById(checkId)).setChecked(true);
				no2 = getNumById(checkId);
				
				checkId = getRandomId();
				((RadioButton)rg_03.findViewById(checkId)).setChecked(true);
				no3 = getNumById(checkId);
				
				break;
		}
	}
	
	private int getRandomId(){
		int checkId = R.id.rb_st;
		int checkNo = random.nextInt(3);
		switch (checkNo) {
		case 0:
			checkId = R.id.rb_st;
			break;
		case 1:
			checkId = R.id.rb_jd;
			break;
		case 2:
			checkId = R.id.rb_bu;
			break;
		}
		
		return checkId;
	}
	
	public void ready2Send(){
		rl_first.setVisibility(View.GONE);
		
		if(!isShowTrack){
			throwBottle();
			return;
		}
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
	 * 提交前，进行验证，验证的内容时：网络是否可用、用户是否登陆、游戏是否出拳、账户的额度是否足够
	 * @return 如果验证内容都满足，则返回true，否则返回false
	 */
	public boolean check(){
		boolean result = true;
		//检查网络
		if(!MoMaUtil.isNetworkAvailable(this)){
			showMsg("当前网络不可用，请检查");
			return  false;
		}
		
		if(UserManager.getInstance(this).getCurrentUser()==null){
			Intent intent = new Intent(this, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			startActivityForResult(intent, 400);
			showMsg(getResources().getString(R.string.unlogin_msg));
			return false;
		}
		
		result = checkMoneyAndOperator(true)&&isThrowable;
		return result;
	}
	/**
	 * 检查是否出拳和是否选择金额
	 * @param isShowToast 是否显示Toast
	 * @return 
	 */
	private boolean checkMoneyAndOperator(boolean isShowToast) {
		// TODO Auto-generated method stub
		//检查3局是否出拳
		if(no1<0 ||no2<0 || no3<0){
			if(isShowToast)
				showMsg("请先选择出拳");
			return false;
		}
		
		if(domoney==-1){
			if(isShowToast)
				showMsg("请先选择额度");
			return false;
		}
				
		return true;
	}

	private int getNumById(int id){
		int num = 0;
		
		switch (id) {
		case R.id.rb_st:
			num = 0;
			break;
		case R.id.rb_jd:
			num = 1;
			break;
		case R.id.rb_bu:
			num = 2;
			break;
		}
		
		return num;
	}
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case OK_QCM:
				int accountMoney = msg.arg1;//取出扇贝额度
				//如果划拳瓶的额度小于账户的额度
				if(domoney<=accountMoney)
					ready2Send();
				else
					new CustomDialog().
					showSelectDialog(Throw_yxpz.this, "扇贝不足~~请先充值",
							new CustomDialogClickListener() {
								@Override
								public void confirm() {
									Intent intent = new Intent(Throw_yxpz.this, RechargeActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//									startActivityForResult(intent,300);
									startActivity(intent);
								}

								@Override
								public void cancel() {
								}
							}
					);
				break;

			default:
				break;
			}
			
		};
	};
	
	
	/**
	 * 查询魅力值和礼券
	 */
	private void queryCharmAndMoney() {
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("deviceId", getDeviceId());
		BottleRestClient.post("queryAccount", this, jo,
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							UserAccountInfo baseModel = gson.fromJson(str, UserAccountInfo.class);
							if (baseModel != null && !TextUtils.isEmpty(baseModel.getCode())) {
								if ("0".equals(baseModel.getCode())) {
									Message msg = Message.obtain();
									msg.what = OK_QCM;
									msg.arg1 = baseModel.getAccountInfo().getMoney();//扇贝额度
									MyApplication.getApp().getSpUtil().setMyMoney(baseModel.getAccountInfo().getMoney());
									handler.sendMessage(msg);
								}
							} 
						} else {
							 showMsg("查询余额失败");
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						 showMsg("查询余额失败");
					}

					@Override
					public void onFinish() {
						super.onFinish();
					}
				});

	}
	
	/**
	 * 游戏接收者可以为空，挑战者不能为空
	 * @param fromUserId 游戏挑战者
	 * @param toUserId 游戏接收者
	 */
	public void doSubmit(String fromUserId,String toUserId){
		isThrowable  = false;
		String cnt = no1+"|"+no2+"|"+no3;
		JsonObject jo = new JsonObject();
		jo.addProperty("content", cnt);
		jo.addProperty("money", domoney);
		jo.addProperty("bottleType", "4009");
		jo.addProperty("come", "6000");
		if(TextUtils.isEmpty(fromUserId)||TextUtils.isEmpty(toUserId)){ //随机挑战
			jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
			jo.addProperty("isFirst", "0");
			jo.addProperty("comeType", 1000); //发送通知
//			jo.addProperty("comeType", 1001);//不发送通知
		}else{	//定向挑战
			jo.addProperty("userId", fromUserId);
			jo.addProperty("toUserId", toUserId);
			jo.addProperty("isFirst", "1");
			jo.addProperty("comeType", 1000);
		}
		
		BottleRestClient.post("throwGameBottle", this, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				super.onStart();
//				rl_first.setVisibility(View.GONE);
//				if (mpDialog == null) {
//					mpDialog = new MyProgressDialog(Throw_yxpz.this);
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
							if(isFirstThrow&&isShowTrack) {
								getTaskJL("stask1002");
							}
//							MyApplication.getApp().getSpUtil().setLastThrowTime();
							int money = MyApplication.getApp().getSpUtil().getMyMoney();
							MyApplication.getApp().getSpUtil().setMyMoney(money-domoney);
						}else{
							MoMaLog.d("debug", baseModel.getMsg());
							showMsg(baseModel.getMsg());
						}
					}else{
//						showMsg("密封失败,请重试");
						rl_second.setVisibility(View.GONE);
						rl_first.setVisibility(View.VISIBLE);
					}
				}else{
//					showMsg("密封失败,请重试");
					rl_second.setVisibility(View.GONE);
					rl_first.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("请检查网络，或联系客服");
				rl_second.setVisibility(View.GONE);
				rl_first.setVisibility(View.VISIBLE);
			}
        });
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
	
	class MyThread implements Runnable{
		public void run() {
			if(!isShowTrack)
				setResult(Activity.RESULT_OK);
			chuck_spray1.setVisibility(View.GONE);
			finish();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mpDialog = null;
	}
	
	/**
	 * 把带有单位的额度转化为数字。仅仅去数字部分
	 * @param str 需要转化的额度字符串，格式是：数字+单位，例如“50扇贝”
	 * @return 如果转化成功，则返回一个非负数，否则就返回-1
	 */
	private int transMoney(String str) {
		// TODO Auto-generated method stub
		if(TextUtils.isEmpty(str))
			return - 1 ;
		str = str.trim();
		str = str.substring(0, str.length()-2);	//去掉最后两个汉字
		if(TextUtils.isDigitsOnly(str))
			return Integer.valueOf(str);
		return -1;
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
		if(!isShowTrack)
			return true;
		if (e1.getY() - e2.getY() > verticalMinDistance && Math.abs(velocityY) > minVelocity) { 
			throwBottle();
	    }
        return true;  
	}
	
	private void throwBottle() {
		// TODO Auto-generated method stub
		canBack = false;
		chuck_bottle_layout.setOnTouchListener(null);
		
		if(MyApplication.getApp().getSpUtil().getThrowHintFlag()){
			iv_hand.clearAnimation();
			iv_hand.setVisibility(View.GONE);
			ll_hint.setVisibility(View.GONE);
			tv_hint.setVisibility(View.GONE);
			MyApplication.getApp().getSpUtil().setThrowHintFlag(false);
		}
		
		
		if(isShowTrack){//需要显示扔瓶子以及水花的
			doSubmit(UserManager.getInstance(this).getCurrentUserId(),null);
			chuck_bottle_layout.startAnimation(set);
		}else{ 
			chuck_bottle_layout.setVisibility(View.GONE);
			myHandler.postDelayed(mt, 450);
			doSubmit(fromUserId, toUserId);
			showDistance();
		}
		
	}

	/**
	 * 清除RadioGroup下所有的RadioButton状态。因为RadioButton的亲爹是LinearLayout，LinearLayout的亲爹是RadioGroup，RadioGroup无法控制孙子辈的RadioBbutton
	 * @param rg
	 */
	public void clearRadioButtonState(RadioGroup rg){
		((RadioButton)rg.findViewById(R.id.rb_bu)).setChecked(false);
		((RadioButton)rg.findViewById(R.id.rb_st)).setChecked(false);
		((RadioButton)rg.findViewById(R.id.rb_jd)).setChecked(false);
	}
}