package com.hnmoma.driftbottle;

import java.lang.reflect.Type;

import org.apache.http.Header;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.TimeOutProgressDialog;
import com.hnmoma.driftbottle.fragment.BaseFragment;
import com.hnmoma.driftbottle.fragment.Fragment_hkpz;
import com.hnmoma.driftbottle.fragment.Fragment_jfpz;
import com.hnmoma.driftbottle.fragment.Fragment_kjpz;
import com.hnmoma.driftbottle.fragment.Fragment_ltpz;
import com.hnmoma.driftbottle.fragment.Fragment_plpz;
import com.hnmoma.driftbottle.fragment.Fragment_rwpz;
import com.hnmoma.driftbottle.fragment.Fragment_sspz;
import com.hnmoma.driftbottle.fragment.Fragment_wdpz;
import com.hnmoma.driftbottle.fragment.Fragment_yxpz;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.PickBottleModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
/**
 * 显示捡到的瓶子的页面。
 * <p>
 * bug描述：捡到瓶子，连续点击back键退回，一直出现扔瓶子的动画<br/>
 * 解决bug方案：由于fragment退栈需要时间（个人认为是fragment的bug），因此，控制back键连续点击。当页面在fragment时，在5秒内点击多次，back键一次有效。<br/>
 *  yangsy于2015-4-24解决<br/>
 *  
 * 如果当前用户捡到瓶子，并且按home键回退出，此时会出现问题：Can not perform this action after onSaveInstanceState<br/>
 * 解决方案：http://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-h
 *  </p>
 * @author Administrator
 *
 */
public class FishingBottleActivity extends BaseActivity {
	private final int INITVIEW =1; //初始化，如启动fragment
	
	String userId = "0000000000";
	TimeOutProgressDialog mpDialog;
	PickBottleModel pickBottleModel;
	
	private Handler mHander = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.arg1) {
			case INITVIEW:
				PickBottleModel model = (PickBottleModel) msg.obj;
				if(model.getBottleInfo().getFromOther()==1){
					mContent = Fragment_plpz.newInstance(model);
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			        ft.replace(R.id.content, mContent);
			        ft.commitAllowingStateLoss();
				}else{
					initView(model);
				}
				
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fishing_bottle);
		if(savedInstanceState!=null){
			pickBottleModel = (PickBottleModel) savedInstanceState.getSerializable("model");
		}
		if(getIntent()!=null){
			pickBottleModel = (PickBottleModel) getIntent().getSerializableExtra("model");
		}
		//现场还原的不再重新获取网络数据
		
		if (mContent == null) {
			if(pickBottleModel == null) {
				pickData();
			} else {
				Message msg = Message.obtain();
				msg.arg1 = INITVIEW;
				msg.obj = pickBottleModel;
				mHander.sendMessage(msg);
			}
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putSerializable("model", pickBottleModel);
		super.onSaveInstanceState(outState);
	}
	
	
	public void pickData() {
		if (UserManager.getInstance(this).getCurrentUser() != null) { // 游客
			userId = UserManager.getInstance(this).getCurrentUserId();
		}

		if (TextUtils.isEmpty(MyApplication.version)) {
			MyApplication.version = getVersionCode();
		}

		
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", userId);
		jo.addProperty("come", "6000");
		jo.addProperty("version", MyApplication.version);

		BottleRestClient.post("getBottle", this, jo, new AsyncHttpResponseHandler() {
					@Override
					public void onStart() {
						super.onStart();
						if (mpDialog == null) {
							mpDialog = new TimeOutProgressDialog(FishingBottleActivity.this);
							// 这里要比自己的10*1000要长，超过了说明异常了
							mpDialog.setTimeOut(15* 1000, new TimeOutProgressDialog.OnTimeOutListener() {
								public void onTimeOut() {
									if (mpDialog != null && mpDialog.isShowing()) {
										mpDialog.cancel();
									}

									BottleRestClient.cancelRequests(FishingBottleActivity.this, true);

									showMsg("连接超时，差点就捞到了");
									setResult(RESULT_CANCELED);
									finish();
								}
							});
						}
						mpDialog.show();
						mpDialog.setContent("正在捞取瓶子...");
					}

					@Override
					public void onFinish() {
						if (mpDialog != null && mpDialog.isShowing()) {
							mpDialog.cancel();
						}
						super.onFinish();
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
						String str = new String(responseBody);

						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							Type type = new TypeToken<PickBottleModel>() {}.getType();
							PickBottleModel model = gson.fromJson(str, type);

							if (model != null && !TextUtils.isEmpty(model.getCode())) {
								if ("0".equals(model.getCode())) {
									Message msg = Message.obtain();
									msg.arg1 = INITVIEW;
									msg.obj = model;
									mHander.sendMessage(msg);
									
									if (UserManager.getInstance(FishingBottleActivity.this).getCurrentUser() == null) {// 游客
										MyApplication.getApp().getSpUtil().dealYkPickUpNum();
									}
									setResult(RESULT_OK);
								}else if ("10005".equals(model.getCode())) {
									showMsg("版本过低,请升级到最新版本");
									setResult(RESULT_CANCELED);
									finish();
								}else if("99996".equals(model.getCode())){
									Intent intent = new Intent();
									intent.putExtra("pickState", true);//没有成功捞取瓶子
									setResult(RESULT_OK,intent);
									finish();
								} else {
									showMsg(model.getMsg());
									setResult(RESULT_CANCELED);
									finish();
								}
							} else {
								showMsg("服务器繁忙，请联系客服");
								setResult(RESULT_CANCELED);
								finish();
							}
						} else {
							showMsg("服务器繁忙，请联系客服");
							setResult(RESULT_CANCELED);
							finish();
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
						showMsg("网络异常没捞到瓶子");
						setResult(RESULT_CANCELED);
						if (mpDialog != null && mpDialog.isShowing()) {
							mpDialog.cancel();
						}
						finish();
					}
				});
	}
	
	//
	/**
	 * 初始化各类瓶子<br/>
	 * 2015-4-22 yangsy修改Fragment的提交方式。修改理由如下：<br/>
	 * 如果commit()方法在Activity的onSaveInstanceState()之后调用，会出错。把commit()方法替换为commitAllowingStateLoss(),效果一样。但commitAllowingStateLoss()方法需要支持包或sdk不低于11<br/>
	 * 解决方案的地址：http://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-h
	 * @param model
	 */
	public void initView(PickBottleModel model) {
		BottleModel bottleModel = model.getBottleInfo();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if(ft==null)
			return ;
		if ("4000".equals(bottleModel.getBottleType())) {//聊天瓶子
			mContent = Fragment_ltpz.newInstance(model);//ok
	        ft.replace(R.id.content, mContent);
	        ft.commitAllowingStateLoss();
		} else if ("4001".equals(bottleModel.getBottleType())) {//分享系统瓶子
			mContent = Fragment_plpz.newInstance(model);
	        ft.replace(R.id.content, mContent);
	        ft.commitAllowingStateLoss();
		} else if ("4002".equals(bottleModel.getBottleType())) {//问答瓶子
			mContent = Fragment_wdpz.newInstance(model);
	        ft.replace(R.id.content, mContent);
	        ft.commitAllowingStateLoss();
		} else if ("4003".equals(bottleModel.getBottleType())) {//任务瓶子
			mContent = Fragment_rwpz.newInstance(model);//ok
	        ft.replace(R.id.content, mContent);
	        ft.commitAllowingStateLoss();
		}else if ("4007".equals(bottleModel.getBottleType())) {//空间瓶子
			mContent = Fragment_kjpz.newInstance(model);//ok
	        ft.replace(R.id.content, mContent);
	        ft.commitAllowingStateLoss();
		}else if ("4009".equals(bottleModel.getBottleType())) {//游戏瓶子
			mContent = Fragment_yxpz.newInstance(model);//ok
	        ft.replace(R.id.content, mContent);
	        ft.commitAllowingStateLoss();
		}else if ("4006".equals(bottleModel.getBottleType()) || "4011".equals(bottleModel.getBottleType())) {//说说瓶子
			mContent = Fragment_sspz.newInstance(model);// Fragment_talk
	        ft.replace(R.id.content, mContent);
	        ft.commitAllowingStateLoss();
		}else if ("4008".equals(bottleModel.getBottleType())) {//爆破即焚瓶子
			mContent = Fragment_jfpz.newInstance(model);//ok
	        ft.replace(R.id.content, mContent);
	        ft.commitAllowingStateLoss();
		}else if ("4010".equals(bottleModel.getBottleType())) {//贺卡瓶子
			mContent = Fragment_hkpz.newInstance(model);//ok
	        ft.replace(R.id.content, mContent);
	        ft.commitAllowingStateLoss();
		}
	}
	
	public void changeContent(PickBottleModel model, BaseFragment bf){
		mContent = bf;
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.content, mContent);
//		transaction.commit();
		transaction.commitAllowingStateLoss();
		
	}
	
	@Override
	public void onBackPressed() {
		 if(mContent == null || mContent.onBackPressed()){
	            if(getSupportFragmentManager().getBackStackEntryCount() == 0){
	               super.onBackPressed();
	            }else{
		            getSupportFragmentManager().popBackStack();
	            }
	     }
	}
	
	
	
}