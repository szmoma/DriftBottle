package com.hnmoma.driftbottle.fragment;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.hnmoma.driftbottle.BaseActivity;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.custom.TimeOutProgressDialog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

/**
 * 聊天瓶子
 * 
 * 
 *onAttach(Activity)
当Fragment与Activity发生关联时调用。
onCreateView(LayoutInflater, ViewGroup,Bundle)
创建该Fragment的视图
onActivityCreated(Bundle)
当Activity的onCreate方法返回时调用
onDestoryView()
与onCreateView想对应，当该Fragment的视图被移除时调用
onDetach()
与onAttach相对应，当Fragment与Activity关联被取消时调用
注意：除了onCreateView，其他的所有方法如果你重写了，必须调用父类对于该方法的实现，
 */
public abstract class BaseFragment extends Fragment {
	
	protected BaseActivity act;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	TimeOutProgressDialog mpDialog;
	private Toast toast;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)         //加载开始默认的图片      
        .showImageForEmptyUri(R.drawable.defalutimg)     //url爲空會显示该图片，自己放在drawable里面的
        .showImageOnFail(R.drawable.defalutimg)                //加载图片出现问题，会显示该图片
        .cacheInMemory(true)                                               //缓存用
        .cacheOnDisc(true)                                                    //缓存用
        .considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
        .build();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		closeDialog(mpDialog);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		act = (BaseActivity) activity;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(this.getClass().getSimpleName()); 
	}
	
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(this.getClass().getSimpleName());
	}
	
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		try {

			Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(this, null);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
	}
	
	  public String getDeviceId(){
			String deviceId = MyApplication.getApp().getSpUtil().getDeviceId();
			
			if(deviceId.equals("")){
				deviceId = MoMaUtil.getDeviceIdFromSys(getActivity());
				if(TextUtils.isEmpty(deviceId)){
					SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss");
					Date date = new Date();
					deviceId = format.format(date);
				}
				
				MyApplication.getApp().getSpUtil().setDeviceId(deviceId);
			}
			
			return deviceId;
		 }
	
	
	/**
	 * 回退事件
	 * 
	 * 1.一级Fragment getSupportFragmentManager
	 * 2.二级Fragment getChildFragmentManager
	 * 
	 * 有子Fragment时调用子fragment的此事件，返回的是false
	 * 没有子Fragment时把自己从Fragment栈移除，返回true
	 * 
	 * 新特性：怎么来的就怎么回去，第一个fragment可以有mcontemt，后面的不要有，会为空，所以直接取为好
	 * @return
	 */
	public abstract boolean onBackPressed();
	/**
	 * Toast重复显示每次都延时累计造成提示框一直显示完累计的时间才退去的问题
	 * @param msg 显示的消息
	 */
	public void showMsg(String msg){
		if(null!=getActivity()){
			if(!TextUtils.isEmpty(msg)){
				if(toast==null){
					toast = Toast.makeText(act, msg, Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
				}else{
					toast.setText(msg);
				}
				toast.show();
			}
		}
	}
	
	public View findViewById(int paramInt) {
		return getView().findViewById(paramInt);
	}
	
	/**
	 * 显示进度对话框
	 * @param msg 提交时显示的内容
	 * @param errMsg 提交错误时，显示的内容
	 */
	public void showDialog(final String msg,final String errMsg,long timeOut){
		if(null!=getActivity()){
			if (mpDialog == null) {
				mpDialog = new TimeOutProgressDialog(getActivity());
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
