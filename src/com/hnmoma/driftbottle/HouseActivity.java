package com.hnmoma.driftbottle;

import java.util.List;
import java.util.Random;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;
/**
 * 设置页面
 * @author Administrator
 *
 */
public class HouseActivity extends BaseActivity implements OnClickListener{
	
	ImageView tv_txvalue;
	TextView tv_yy;
	ToggleButton iv_yy;
	ImageView iv_new;
	TextView tv_dl;
	RelativeLayout rl_dl;
	LinearLayout ll_02;
	TextView tv_ver;
	TextView tv_ntc;
	ToggleButton iv_ntc;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}

	public void initView(){
		setContentView(R.layout.fragment_house);
		findViewById(R.id.bt_back).setOnClickListener(this);	
		findViewById(R.id.rl_lockscreen).setOnClickListener(this);
		findViewById(R.id.rl_yy).setOnClickListener(this);
		findViewById(R.id.rl_ntc).setOnClickListener(this);
		findViewById(R.id.rl_tjpy).setOnClickListener(this);
		findViewById(R.id.rl_yjfk).setOnClickListener(this);
		findViewById(R.id.rl_jcgx).setOnClickListener(this);
		findViewById(R.id.rl_wxhp).setOnClickListener(this);
		findViewById(R.id.rl_dl).setOnClickListener(this);
		findViewById(R.id.rl_cz).setOnClickListener(this);
//		findViewById(R.id.rl_djsc).setOnClickListener(this);
//		findViewById(R.id.rl_sjrw).setOnClickListener(this);
		findViewById(R.id.rl_bzsm).setOnClickListener(this);
//		findViewById(R.id.rl_mlsc).setOnClickListener(this);
		iv_yy = (ToggleButton) findViewById(R.id.iv_yy);
		iv_new = (ImageView) findViewById(R.id.iv_new);
		tv_yy = (TextView) findViewById(R.id.tv_yy);
		tv_dl = (TextView) findViewById(R.id.tv_dl);
		rl_dl = (RelativeLayout) findViewById(R.id.rl_dl);
		ll_02 = (LinearLayout) findViewById(R.id.ll_02);
		
//		rl_tj = (RelativeLayout) findViewById(R.id.rl_tj);
//		rl_tj.setOnClickListener(this);	
		
		tv_ver = (TextView) findViewById(R.id.tv_ver);
		
		iv_ntc = (ToggleButton) findViewById(R.id.iv_ntc);
		tv_ntc = (TextView) findViewById(R.id.tv_ntc);
	}
	
	public void initData(){
		reflesh();
		
		boolean opend = MyApplication.getApp().getSysSpUtil().isSoundOpend();
		if(opend){
//			tv_yy.setText("背景音乐(开)");
			iv_yy.setChecked(true);
		}else{
//			tv_yy.setText("背景音乐(关)");
			iv_yy.setChecked(false);
		}
		
		boolean ntc_opend = MyApplication.getApp().getSysSpUtil().isNtcSoundOpend();
		if(ntc_opend){
//			tv_ntc.setText("通知铃声(开)");
			iv_ntc.setChecked(true);
		}else{
//			tv_ntc.setText("通知铃声(关)");
			iv_ntc.setChecked(false);
		}
		
//		if(UserManager.getInstance(this).getCurrentUser()!=null){
//			rl_dl.setVisibility(View.VISIBLE);
//			ll_02.setVisibility(View.VISIBLE);
//		}else{
//			rl_dl.setVisibility(View.GONE);
//			ll_02.setVisibility(View.GONE);
//		}
		
		
		String versionName = "1.0.0";
		try {
			PackageManager pm = this.getPackageManager();
			PackageInfo pinfo = pm.getPackageInfo(this.getPackageName(), PackageManager.GET_CONFIGURATIONS);
			versionName = pinfo.versionName;
		} catch (NameNotFoundException e) {
		}
		tv_ver.setText("检查更新 ("+versionName+")");
	}
	
	
	private void reflesh(){
		if(UserManager.getInstance(this).getCurrentUser()==null){
//			rl_dl.setVisibility(View.GONE);
			ll_02.setVisibility(View.GONE);
			tv_dl.setText(getResources().getString(R.string.login));
		}else{
//			rl_dl.setVisibility(View.VISIBLE);
			ll_02.setVisibility(View.VISIBLE);
			tv_dl.setText(getResources().getString(R.string.exit_login));
		}
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_back:
				finish();
				break;
			case R.id.rl_lockscreen:
				Intent intents = new Intent(this, LockPasswordSetActivity.class);
				intents.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(intents);
				break;
				
			case R.id.rl_yy:
				boolean opend = MyApplication.getApp().getSysSpUtil().isSoundOpend();
				if(opend){
//					tv_yy.setText("背景音乐(关)");
					iv_yy.setChecked(false);
					MyApplication.getApp().getSysSpUtil().setSoundOpend(false);
				}else{
//					tv_yy.setText("背景音乐(开)");
					iv_yy.setChecked(true);
					MyApplication.getApp().getSysSpUtil().setSoundOpend(true);
				}
				break;
			case R.id.rl_ntc:
				boolean ntc_opend = MyApplication.getApp().getSysSpUtil().isNtcSoundOpend();
				if(ntc_opend){
//					tv_ntc.setText("通知铃声(关)");
					iv_ntc.setChecked(false);
					MyApplication.getApp().getSysSpUtil().setNtcSoundOpend(false);
				}else{
//					tv_ntc.setText("通知铃声(开)");
					iv_ntc.setChecked(true);
					MyApplication.getApp().getSysSpUtil().setNtcSoundOpend(true);
				}
				break;
			case R.id.rl_tjpy:
				String msg = getRandomMsg();
				
				Intent intent=new Intent(Intent.ACTION_SEND);   
		        intent.setType("text/plain");   
		        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share));   
		        intent.putExtra(Intent.EXTRA_TEXT, msg);    
		        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
		        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		        startActivity(Intent.createChooser(intent, getString(R.string.set_tjpy)));
				break;
			case R.id.rl_yjfk:
				FeedbackAgent agent = new FeedbackAgent(this);
				agent.startFeedbackActivity();
				 break;
			case R.id.rl_jcgx:
				 //检查更新
//				 UmengUpdateAgent.update(this);
//				 UmengUpdateAgent.setUpdateOnlyWifi(false);
//				 UmengUpdateAgent.setUpdateAutoPopup(false);
//				 UmengUpdateAgent.setUpdateListener(updateListener);
				 UmengUpdateAgent.setUpdateCheckConfig(true);
				 UmengUpdateAgent.setUpdateAutoPopup(false);
				 UmengUpdateAgent.setUpdateListener(updateListener);
				 UmengUpdateAgent.forceUpdate(this);
				
				 break;
			case R.id.rl_wxhp:
				doSupport();
				break;
//			case R.id.rl_tj:
//				intent = new Intent(this, AdsActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//				startActivity(intent);
//				break;
			case R.id.rl_dl:
//				v.setEnabled(false);
				//登录，登出
				//及时通讯系统关闭
				if(UserManager.getInstance(this).getCurrentUser() == null) {
					intent = new Intent(this, LoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivityForResult(intent, 700);
				} else {
					MyApplication.getApp().getSpUtil().clearApplicatioinCache(true);
					MyApplication.getApp().logout();
					showMsg("登出成功");
					//发送广播
					Intent intentBoradcast = new Intent();  
					intentBoradcast.setAction(MyConstants.USERINFOCHANGE_BROADCAST_ACTION);  
					intentBoradcast.putExtra("isLogout", true);
					sendBroadcast(intentBoradcast);  
				}
				reflesh();
				break;
			case R.id.rl_cz:
				intent = new Intent(this, RechargeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(intent);
				break;
//			case R.id.rl_djsc:
//				intent = new Intent(this, DjscActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//				startActivity(intent);
//				break;
//			case R.id.rl_mlsc:
//				intent = new Intent(this, MlscActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//				startActivity(intent);
//				break;
//			case R.id.rl_sjrw:
//				intent = new Intent(this, SjrwActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//				startActivity(intent);
//				break;
			case R.id.rl_bzsm:
				intent = new Intent(this, WebFrameWithCacheActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("webUrl", MyConstants.HELPURL);
				startActivity(intent);
				break;
		}
	}
	
	private void deBindClientId(Context ctx, final View view){
		if(UserManager.getInstance(this).getCurrentUser()!=null){
			
			JsonObject jo = new JsonObject();
			jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
			jo.addProperty("clientId", "");
			
			BottleRestClient.post("boundId", ctx, jo, new AsyncHttpResponseHandler(){
				
				@Override
				public void onStart() {
					// TODO Auto-generated method stub
					super.onStart();
					showDialog("正在登出....", "登出失败...", 15*1000);
				}
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					String str = new String(responseBody);
					if(!TextUtils.isEmpty(str)){
						Gson gson = new Gson();
						BaseModel baseModel = gson.fromJson(str, BaseModel.class);
						if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
							if("0".equals(baseModel.getCode())){
								showMsg("登出成功");
								MyApplication.getApp().getSpUtil().clearApplicatioinCache(true);
								MyApplication.getApp().logout();
//								rl_dl.setVisibility(View.GONE);
								reflesh();
							}
						}else{
							showMsg("登出失败");
						}
					}else{
						showMsg("登出失败");
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					showMsg("登出失败");
					closeDialog(mpDialog);
				}
				
				@Override
				public void onFinish() {
					super.onFinish();
					view.setEnabled(true);
					closeDialog(mpDialog);
				}
	        });
		}
	}
	
	private void doSupport(){
		//这里获取渠道标识，只评论相应渠道,如果没有这个市场，就去其他随机市场
		String channelId="";
		int index = -1;
		
		try {
			ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
			String str =  appInfo.metaData.getString("UMENG_CHANNEL");
			channelId = str==null?"":str;
		} catch (NameNotFoundException e) {
		}
		
		String packageName = "com.hnmoma.driftbottle";
		PackageManager localPackageManager = this.getPackageManager();
		Intent localIntent1 = new Intent(Intent.ACTION_VIEW);
		Uri localUri1 = Uri.parse("market://details?id=" + packageName);
		Intent localIntent3 = localIntent1.setData(localUri1);
		List localList = localPackageManager.queryIntentActivities(localIntent1, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
		for (int i = 0; i < localList.size(); i++) {
			ResolveInfo localResolveInfo = (ResolveInfo) localList.get(i);
			String str2 = localResolveInfo.activityInfo.name;
			
			if(str2.contains(channelId)){
				index = i;
			}
		}
		
		//为0时直接返回
		if(localList.size() <= 0){
			return;
		}else if(localList.size() == 1){
			index = 0;
		}else{//大于2时随机
			if(index == -1){
				Random r = new Random();
				index = r.nextInt(localList.size()-1);
			}
		}
		
		
		ResolveInfo localResolveInfo = (ResolveInfo) localList.get(index);
		String str2 = localResolveInfo.activityInfo.packageName;
		String str3 = localResolveInfo.activityInfo.name;
		
		Uri uri = Uri.parse("market://details?id=" + packageName);
		localIntent3.setData(uri);
		localIntent3.setClassName(str2, str3);
		localIntent3.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivity(localIntent3);
	}
	
	UmengUpdateListener updateListener = new UmengUpdateListener() {
		@Override
	    public void onUpdateReturned(int updateStatus,UpdateResponse updateInfo) {
	        switch (updateStatus) {
	        case UpdateStatus.Yes: // has update
	            UmengUpdateAgent.showUpdateDialog(HouseActivity.this, updateInfo);
	            break;
	        case UpdateStatus.No: // has no update
	        	showMsg(getResources().getString(R.string.tip_update_version));
	            break;
	        case UpdateStatus.NoneWifi: // none wifi
	        	showMsg("没有wifi连接， 只在wifi下更新");
	            break;
	        case UpdateStatus.Timeout: // time out
	        	showMsg( "超时");
	            break;
	        }
	    }
	};

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 400){//未登录状态下进入个人中心
			if(resultCode == Activity.RESULT_OK){
				reflesh();
				Intent intent = new Intent(this, VzoneActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("identityflag", 0);
				intent.putExtra("userId", UserManager.getInstance(this).getCurrentUserId());
				intent.putExtra("visitUserId", UserManager.getInstance(this).getCurrentUserId());
				startActivityForResult(intent, 500);
			}
		}else if(requestCode == 500){//个人空间
			if(resultCode == Activity.RESULT_OK){//修改了用户信息
//				refresh();
			}
		}  else if(requestCode == 700){//个人空间
			if(resultCode == Activity.RESULT_OK){//修改了用户信息
				reflesh();
			}
		}
	}
	
	private String getRandomMsg(){
		String[] result = new String[]{"漂流瓶子，90后都在玩的游戏化交友APP，不玩你就Out啦！ http://www.52plp.com",
				"我刚刚在漂流瓶子抛了一句悄悄话，你要来捡捡看吗？ http://www.52plp.com",
				"玩漂流瓶子，丢的不只是节操，捡的不只是肥皂， 你也来感受一下吧？ http://www.52plp.com",
				"快来玩漂流瓶子，抛一句真心，捡一个惊喜~ http://www.52plp.com"};
		
		Random random = new Random();
		int randomInt = random.nextInt(4);
		
		return result[randomInt];
	}
	
}
