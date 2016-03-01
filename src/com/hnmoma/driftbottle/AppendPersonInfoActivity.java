package com.hnmoma.driftbottle;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.Header;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CustomDialog;
import com.hnmoma.driftbottle.custom.CustomDialog.CustomDialogClickListener;
import com.hnmoma.driftbottle.custom.TimeOutProgressDialog;
import com.hnmoma.driftbottle.fragment.FragmentUserInfoAddr;
import com.hnmoma.driftbottle.fragment.FragmentUserInfoBasic;
import com.hnmoma.driftbottle.fragment.FragmentUserInfoBirthDay;
import com.hnmoma.driftbottle.itfc.IFragmentCallback;
import com.hnmoma.driftbottle.model.UnionLoginModel;
import com.hnmoma.driftbottle.model.User;
import com.hnmoma.driftbottle.model.UserDataModel;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.Base64;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;
/**
 * 完善用户的信息
 * @author yangsy
 * <p>2015-4-22 yangsy修改Fragment的提交方式。修改理由如下：<br/>
 * 如果commit()方法在Activity的onSaveInstanceState()之后调用，会出错。把commit()方法替换为commitAllowingStateLoss(),效果一样。但commitAllowingStateLoss()方法需要支持包或sdk不低于11<br/>
 * 解决方案的地址：http://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-h</p>
 */
public class AppendPersonInfoActivity extends FragmentActivity implements IFragmentCallback{
	//操作类型
	public final static String OPERBASIC = "operBasic";	//打开基本信息页面
	public final static String OPERADDR = "operAddr";	//打开地址页面
	public final static String OPERBIRTH = "operBirth";	//打开出生日期页面
	public final static String OPERSUBMIT = "operSubmit";	//提交操作
	
	public final static String TAG = "currentTag";
	
	private Fragment currentFragment;
	private FragmentManager manager;
	private FragmentTransaction mTransaction; 
	
	private String currentFragmentTag;
	
	private Button btnBack;	//返回键
	private TextView tvTip;	//注册步骤标题
	
	private String userId;
	
	private User user;
	TimeOutProgressDialog mpDialog;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what==1){
				user = (User) msg.obj;
				initView(user);
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(getIntent()!=null){
			userId =getIntent().getStringExtra("userId");
			boolean isUnionLogin = getIntent().getBooleanExtra("isUnionLogin", false);
			if(!TextUtils.isEmpty(userId)&&isUnionLogin){
				user = UserManager.getInstance(this).getCurrentUser();
				Message msg = Message.obtain();
				msg.what = 1;
				msg.obj = user;
				handler.sendMessage(msg);
			}else
				getUserInfo();
		}
		setContentView(R.layout.activity_add_userinfo);
		
		btnBack = (Button) findViewById(R.id.btn_back);
		tvTip = (TextView) findViewById(R.id.tv_tip);
		
		btnBack.setVisibility(View.GONE);
		
		manager = getSupportFragmentManager();
		
		setListener();
		
		if(savedInstanceState!=null){
			currentFragmentTag = savedInstanceState.getString(TAG);
			if(!TextUtils.isEmpty(currentFragmentTag)){
				showFragment(savedInstanceState, currentFragmentTag);
				return;
			}
			
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		closeDialog(mpDialog);
	}
	/**
	 * 初始化第一个控件
	 */
	private void initView(User user) {
		// TODO Auto-generated method stub
		Bundle bundle = new Bundle();
		bundle.putSerializable("user", user);
		
		mTransaction = manager.beginTransaction();
		currentFragment = manager.findFragmentByTag(OPERBASIC);
		if(currentFragment==null)
			currentFragment = FragmentUserInfoBasic.newInstance(bundle);
		mTransaction.replace(R.id.content, currentFragment, OPERBASIC);
		mTransaction.commitAllowingStateLoss();
		currentFragmentTag = OPERBASIC;
		
		tvTip.setText("1/3");
		((TextView) findViewById(R.id.tv_title)).setText(getResources().getString(R.string.addUserInfo));
	};
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putString(TAG, currentFragmentTag);
		if(user!=null){
			outState.putString(FragmentUserInfoBasic.PATH, user.getHeadImg());
			outState.putString(FragmentUserInfoBasic.NICK, user.getNickName());
			outState.putString(FragmentUserInfoBasic.SEX, user.getIdentityType());
			outState.putString(FragmentUserInfoAddr.PROVINCE, user.getProvince());
			outState.putString(FragmentUserInfoAddr.CITY, user.getCity());
			outState.putString(FragmentUserInfoBirthDay.BIRTHDAY, user.getBirthday());
		}
	}

	private void setListener() {
		// TODO Auto-generated method stub
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(OPERBASIC.equals(currentFragmentTag)){
					btnBack.setVisibility(View.GONE);
				}else
					btnBack.setVisibility(View.VISIBLE);
				Bundle args = new Bundle();
				args.putSerializable("user", user);
				
				if(OPERBASIC.equals(currentFragmentTag)){
					showAlertDialog();
					currentFragmentTag = "";
				}else if(OPERADDR.equals(currentFragmentTag)){
					showFragment(args, OPERBASIC);
				}else if(OPERBIRTH.equals(currentFragmentTag)){
					showFragment(args, OPERADDR);
				}
				
			}
		});
	}

	@Override
	public void startDetailFragment(Bundle arg,String tag) {
		// TODO Auto-generated method stub
		//取出回调的结果
		if(OPERADDR.equals(tag)){
			user.setHeadImg(arg.getString(FragmentUserInfoBasic.PATH));
			user.setIdentityType(arg.getString(FragmentUserInfoBasic.SEX));
			user.setNickName(arg.getString(FragmentUserInfoBasic.NICK));
		}else if(OPERBIRTH.equals(tag)){
			user.setProvince(arg.getString(FragmentUserInfoAddr.PROVINCE));
			user.setCity(arg.getString(FragmentUserInfoAddr.CITY));
		}else if(OPERSUBMIT.equals(tag)){
			user.setBirthday(arg.getString(FragmentUserInfoBirthDay.BIRTHDAY));
		}
		Bundle extra = new Bundle();
		extra.putSerializable("user", user);		
		if(!OPERSUBMIT.equals(tag)){
			showFragment(extra,tag);
			if(OPERBASIC.equals(tag)){
				btnBack.setVisibility(View.GONE);
			}else
				btnBack.setVisibility(View.VISIBLE);
		}else{
			MoMaLog.e("debug", "path="+user.getHeadImg()+",nickName="+user.getNickName()+",sex="+user.getIdentityType()+
					",province="+user.getProvince()+",city="+user.getCity()+",birth="+user.getBirthday());
			//提交数据
			if(!TextUtils.isEmpty(user.getHeadImg())&&
					!TextUtils.isEmpty(user.getNickName())&&
					!TextUtils.isEmpty(user.getIdentityType())&&
					!TextUtils.isEmpty(user.getProvince())&&
					!TextUtils.isEmpty(user.getCity())&&
					!TextUtils.isEmpty(user.getBirthday()))
				doSubmit(user.getHeadImg(), user.getNickName(), user.getIdentityType(), user.getProvince(), user.getCity(), user.getBirthday());
			else 
				showMsg("检查提交数据");
			
		}
		
	}

	private void showFragment(Bundle args,String tag) {
		// TODO Auto-generated method stub
		currentFragment = manager.findFragmentByTag(tag);
		if(currentFragment==null)
			if(OPERBASIC.equals(tag)){
				tvTip.setText("1/3");
				currentFragment = FragmentUserInfoBasic.newInstance(args);
			}else if(OPERADDR.equals(tag)){
				tvTip.setText("2/3");
				currentFragment = FragmentUserInfoAddr.newInstance(args);
			}else if(OPERBIRTH.equals(tag)){
				tvTip.setText("3/3");
				currentFragment = FragmentUserInfoBirthDay.newInstance(args);
			}
		mTransaction = manager.beginTransaction();
		mTransaction.replace(R.id.content, currentFragment, tag);
		mTransaction.addToBackStack(null);
		mTransaction.commitAllowingStateLoss();
		currentFragmentTag = tag;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK){
			if(OPERBASIC.equals(currentFragmentTag)){
				btnBack.setVisibility(View.GONE);
			}else
				btnBack.setVisibility(View.VISIBLE);
			
			Bundle args = new Bundle();
			args.putSerializable("user", user);
			if(OPERBASIC.equals(currentFragmentTag)){
				showAlertDialog();
				currentFragmentTag = "";
			}else if(OPERADDR.equals(currentFragmentTag)){
				showFragment(args, OPERBASIC);
			}else if(OPERBIRTH.equals(currentFragmentTag)){
				showFragment(args, OPERADDR);
			}
			return true;
        }
		return super.onKeyDown(keyCode, event);
	}
	/**
	 * 提交事件
	 * @param headUri 头像的地址
	 * @param nikeName 昵称
	 * @param sex 性别
	 * @param privinc 省
	 * @param city 市
	 * @param birth 生日
	 */
	private void doSubmit(String headUri,String nikeName,String sex,String province,String city,String birth){
		JsonObject jo = new JsonObject();
		
		if(headUri!=null){
			try {
				File file = new File(headUri);
				String headStrs = Base64.encode(MoMaUtil.inputStreamToByte(new FileInputStream(file)));
				jo.addProperty("headImg", headStrs);
			} catch (Exception e) {
				MoMaLog.e("debug", e.getMessage());
			}
		}
		
		int identityType =2006;
		if("女".equals(sex))	 {
			identityType = 2007;
		}
		
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("nickName", nikeName);
		jo.addProperty("identityType", identityType);
		jo.addProperty("province", province);
		jo.addProperty("city", city);
		jo.addProperty("birthday", birth);
		
		
		//计算年龄
		String age = calculateAge(birth);
		if(!TextUtils.isEmpty(age)){
			age = (age.split("_"))[0];//只需要获得年
			jo.addProperty("age",age );
		}
		String constell = calculateConstellation(birth);
		if(!TextUtils.isEmpty(constell))
			jo.addProperty("constell", constell);
		
		BottleRestClient.post("updateUserInfo", this, jo, new AsyncHttpResponseHandler (){
			@Override
			public void onStart() {
				super.onStart();
				showDialog("更新用户信息...", "更新失败", 20*1000);
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
					UserInfoModel ui = ulm.getUserInfo();
					if(ui!=null){
						UserManager.getInstance(getBaseContext()).login(ui);
						
						Intent intentBoradcast = new Intent();  
						intentBoradcast.setAction(MyConstants.USERINFOCHANGE_BROADCAST_ACTION);  
						sendBroadcast(intentBoradcast);  
						
						setResult(RESULT_OK);
						onBackPressed();
					}else{
						showMsg("提交失败");
					}
					
				}else{
					showMsg("提交失败");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("提交失败");
				closeDialog(mpDialog);
			}
        });
	}
	
	private void showMsg(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	
	
	
	// 查询用户资料信息
	private void getUserInfo() {
		// success set user
		JsonObject jo = new JsonObject();
		jo.addProperty("id", userId);

		BottleRestClient.post("queryUserInfo", this, jo, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				super.onStart();
				showDialog("努力加载...", "加载失败", 15*1000);
			}
	
			@Override
			public void onFinish() {
				super.onFinish();
				closeDialog(mpDialog);
			}
	
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				
				if (!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					UserDataModel model = gson.fromJson(str, UserDataModel.class);
					
					if (model != null && !TextUtils.isEmpty(model.getCode())) {
						if ("0".equals(model.getCode())) {
							Message msg = Message.obtain();
							msg.what = 1;
							msg.obj = model.getUserInfo();
							handler.sendMessage(msg);
						} else {
							showMsg(model.getMsg());
						}
					} else {
						showMsg("服务器繁忙");
					}
				} else {
					showMsg("服务器繁忙");
				}
			}
	
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				closeDialog(mpDialog);
			}
		});
	}
	
	private void showAlertDialog() {
		// TODO Auto-generated method stub
		new CustomDialog().showSelectDialog(this, "温馨提示",getResources().getString(R.string.tip_add_userinfo),new CustomDialogClickListener() {
			
			@Override
			public void confirm() {
				// TODO Auto-generated method stub
				onBackPressed();
			}
			
			@Override
			public void cancel() {
				// TODO Auto-generated method stub
				
			}
		});
	}
	/**
	 * 根据出生日期，计算详细的年龄
	 * @param birth 出身日期
	 * @return 返回年龄字符串，格式是“年_月_日"
	 */
	private String calculateAge(String birth){
		String age = "";
		try {
			//把字符串的日期，转化为日期格式
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date  _birthday= sdf.parse(birth);	//出生日期
			
			Calendar birthday = Calendar.getInstance();
			birthday.setTime(_birthday);
			
			Calendar now = Calendar.getInstance();
			
			int day = now.get(Calendar.DAY_OF_MONTH) - birthday.get(Calendar.DAY_OF_MONTH);//天数差
			int month = now.get(Calendar.MONTH) - birthday.get(Calendar.MONTH);	//月差
			int year = now.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);	//年差
			
			//按照减法原理，先day相减，不够向month借；然后month相减，不够向year借；最后year相减。
			if(day<0){
				month -= 1;
				now.add(Calendar.MONTH, -1);//得到上一个月，用来得到上个月的天数。
				day = day + now.getActualMaximum(Calendar.DAY_OF_MONTH);
			}
			if(month<0){
				month = (month+12)%12;
				year--;
			}
			age = year+"_"+month+"_"+day;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return age;
	}
	
	/**
	 * 根据出身日期，计算星座
	 * @param birthday 日期字符串，格式为“yyyy-MM-dd”
	 * @return 返回字符串类型，如果返回空格字符串或异常，未计算成功
	 */
	public static String calculateConstellation(String birthday) {
		 int[] constellationEdgeDay = { 21, 20, 21, 21, 22, 22, 23, 24, 24, 24, 23, 22 };
		 String [] astro = new String[] { "摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座" };
		if (birthday == null || birthday.trim().length() == 0){
			throw new IllegalArgumentException("日期不能为空");
		}
		
		String[] birthdayElements = birthday.split("-");
		if (birthdayElements.length != 3) {
			throw new IllegalArgumentException("日期格式无效");
		}
		
		int month = Integer.parseInt(birthdayElements[1]);	//获得月
		int day = Integer.parseInt(birthdayElements[2]);	//获得日
		if (month <= 0 || day <= 0 || month > 12 ||day>31)
		return "";
		
		month = day < constellationEdgeDay[month - 1]? month - 1:month;
		return month > 0 ?astro[month - 1]: astro[11];
		}
	
	/**
	 * 显示进度对话框
	 * @param msg 提交时显示的内容
	 * @param errMsg 提交错误时，显示的内容
	 */
	public void showDialog(final String msg,final String errMsg,long timeOut){
		if (mpDialog == null) {
			mpDialog = new TimeOutProgressDialog(this);
			// 这里要比自己的10*1000要长，超过了说明异常了
			mpDialog.setTimeOut(timeOut, new TimeOutProgressDialog.OnTimeOutListener() {
				public void onTimeOut() {
					if (mpDialog != null && mpDialog.isShowing()) {
						mpDialog.cancel();
					}

					showMsg(errMsg);
					setResult(RESULT_CANCELED);
					onBackPressed();
				}
			});
		}
		mpDialog.show();
		mpDialog.setContent(msg);
	}
	
	public void closeDialog(Dialog dialog){
		if(dialog!=null && dialog.isShowing()){
			dialog.cancel();
			dialog = null;
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setClass(this, GuideActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivity(intent);
		finish();
	}
	
}
