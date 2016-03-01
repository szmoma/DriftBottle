package com.hnmoma.driftbottle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import com.hnmoma.driftbottle.business.MyNotificationManager;
import com.hnmoma.driftbottle.custom.TimeOutProgressDialog;
import com.hnmoma.driftbottle.fragment.BaseFragment;
import com.hnmoma.driftbottle.itfc.ScreenListener;
import com.hnmoma.driftbottle.itfc.ScreenListener.ScreenStateListener;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.umeng.analytics.MobclickAgent;

public abstract class BaseActivity extends FragmentActivity {
	
	public BaseFragment mContent;
	public static ScreenListener screenListener;// 单例
	TimeOutProgressDialog mpDialog;
	private Toast toast;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(mContent!=null){
			getSupportFragmentManager().putFragment(outState, "mContent", mContent);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContent = null;
		if(savedInstanceState != null) {
			mContent = (BaseFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
		}
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		closeDialog(mpDialog);
	}
	
	protected void  beginScreenMonitor() {
		// 检测屏幕
		if(screenListener == null) {
			screenListener = new ScreenListener(this);
		}
		
		screenListener.begin(new ScreenStateListener() {
					
			@Override
			public void onUserPresent() {
				// TODO  解锁
			}
					
			@Override
			public void onScreenOn() {
				// TODO 点亮屏幕
			}
					
			@Override
			public void onScreenOff() {
				// TODO 锁屏
				if(isApplicationBroughtToBackground(BaseActivity.this)) {
					Intent intent = new Intent(BaseActivity.this, LockScreenLoginActivity.class);
					startActivity(intent);	
				}
			}
		});
	}
	
	protected void  closeScreenMonitor() {
		if(screenListener != null) {
			screenListener.unregisterListener();
		}
	}
	/**
	 * Toast重复显示每次都延时累计造成提示框一直显示完累计的时间才退去的问题
	 * @param msg 显示的消息
	 */
	protected void showMsg(String msg){
		if(!TextUtils.isEmpty(msg)){
			if(toast==null){
				toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
			}else{
				toast.setText(msg);
			}
			toast.show();
		}
	}
	
	protected void onResume() {
		super.onResume();
		
		if(MyApplication.getApp().getSpUtil().isScreenLocked()) {
			beginScreenMonitor();
		}
		
		MobclickAgent.onPageStart(this.getClass().getSimpleName());
		MobclickAgent.onResume(this);
		
//		//onresume时，取消notification显示
//		EMChatManager.getInstance().activityResumed();
		//TODO  空指针异常
		MyNotificationManager.getInstance(this).cancleMsgNotice();
	}

	protected void onPause() {
		super.onPause();
		
		MobclickAgent.onPageEnd(this.getClass().getSimpleName()); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
		MobclickAgent.onPause(this);
		
	}
	
	/**
     *判断当前应用程序处于前台
     */
    public  boolean isApplicationBroughtToBackground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (topActivity.getPackageName().equals(context.getPackageName())) {
            	//beginScreenMonitor();
                return true;
            }
        }
        //closeScreenMonitor();
        return false;
    }
	
	/**
     * home或者电话之类未知的挂起
     * onPause之前调用
     */
    @Override
    protected void onUserLeaveHint() {
    	super.onUserLeaveHint();
    	MyApplication.isPending = true;
    }
    /**
     * 获取设备号，如果设备号为null或空字符串时，使用当前的
     * @return 返回唯一的设备号
     */
    public String getDeviceId(){
		String deviceId = MyApplication.getApp().getSpUtil().getDeviceId();
		
		if(deviceId.equals("")){
			deviceId = MoMaUtil.getDeviceIdFromSys(this);
			if(TextUtils.isEmpty(deviceId)){
				SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss");
				Date date = new Date();
				deviceId = format.format(date);
				
				/*********设备号必须是唯一的，如果用当前的时间戳作为设备ID，可能存在重复，不建议这样使用，使用UUID，如下代码：***************/
//				UUID uuid= UUID.randomUUID();
//				deviceId = uuid.toString().replaceAll("-", "");
			}
			
			MyApplication.getApp().getSpUtil().setDeviceId(deviceId);
		}
		
		return deviceId;
	 }
    /**
     * 获取版本号
     * @return 返回版本号的描述
     */
    public String getVersionCode(){
    	String versionCode = "";
    	try {  
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);  
            versionCode = pi.versionCode + "";  
        } catch (Exception e) {  
        	versionCode = 1 + "";  
        }  
		return versionCode;
	 }
	
	public String getChannel(){
		String channelId="";
		try {
			ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			String str =  appInfo.metaData.getString("UMENG_CHANNEL");
			channelId = str==null?"":str;
		} catch (Exception e) {
			channelId="";
		}
		
		return channelId;
	 }
	
	/**
	 * 显示进度对话框
	 * @param msg 提交时显示的内容
	 * @param errMsg 提交错误时，显示的内容
	 */
	public void showDialog(final String msg,final String errMsg,long timeOut){
		if(getBaseContext()!=null){
			if (mpDialog == null) {
				mpDialog = new TimeOutProgressDialog(this);
				// 这里要比自己的10*1000要长，超过了说明异常了
				mpDialog.setTimeOut(timeOut, new TimeOutProgressDialog.OnTimeOutListener() {
					public void onTimeOut() {
						if (mpDialog != null && mpDialog.isShowing()) {
							mpDialog.cancel();
						}

						showMsg(errMsg);
					}
				});
			}
			mpDialog.show();
			mpDialog.setContent(msg);
		}
	}
	
	public void closeDialog(Dialog dialog){
		if(dialog!=null && dialog.isShowing()){
			dialog.cancel();
			dialog = null;
		}
	}

}
	
  	

		

//public class BaseActivity extends Activity{
//	@Override
//	protected void onResume() {
//		super.onResume();
//		//onresume时，取消notification显示
//		EMChatManager.getInstance().activityResumed();
//	}
//}
