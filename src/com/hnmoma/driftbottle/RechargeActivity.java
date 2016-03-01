package com.hnmoma.driftbottle;

import org.apache.http.Header;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.SearchManager.OnCancelListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CustomDialog;
import com.hnmoma.driftbottle.custom.CustomDialog.CustomDialogItemClickListener;
import com.hnmoma.driftbottle.custom.TimeOutProgressDialog;
import com.hnmoma.driftbottle.model.NoticeModel;
import com.hnmoma.driftbottle.model.OrderInfo;
import com.hnmoma.driftbottle.model.PayInfo;
import com.hnmoma.driftbottle.model.PayOrderModel;
import com.hnmoma.driftbottle.model.QueryShanbeiBModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.net.UpdateCacheOfUpperLimit;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
/**
 * 充值
 */
public class RechargeActivity extends BaseActivity{
	
	private static final int REQ_CODE_ONE = 100;//一把扇贝

	private static final int REQ_CODE_TWO = 101;	//一盒扇贝

	private static final int REQ_CODE_THREE = 102; //一篮扇贝

	private static final int REQ_CODE_FOUR = 103;	//一箱扇贝
	
	View ll_main;
	TextView tv_ye;
	
	String orderId;
	protected IWXAPI msgApi;
	Button bt_01,bt_02,bt_03,bt_04;
	
	private NewMessageBroadcastReceiver msgReceiver;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		initView();
		
		// 注册一个接收消息的BroadcastReceiver
		msgReceiver = new NewMessageBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
		intentFilter.setPriority(6);
		registerReceiver(msgReceiver, intentFilter);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 400) {
			showDialog();
		}
		if(resultCode==Activity.RESULT_OK){
			switch (requestCode) {
			case REQ_CODE_ONE:
				OrderInfo oi = new OrderInfo();
				oi.setMoney(10);
				oi.setUseType("1000");
				oi.setDesc("一把扇贝");
				oi.setCustomInfo("一把扇贝");
				chooseRechargeChannel(oi);
				break;
			case REQ_CODE_TWO:
				OrderInfo oi2 = new OrderInfo();
				oi2.setMoney(25);
				oi2.setUseType("1000");
				oi2.setDesc("一盒扇贝");
				oi2.setCustomInfo("一盒扇贝");
				chooseRechargeChannel(oi2);
				break;
			case REQ_CODE_THREE:
				OrderInfo oi3 = new OrderInfo();
				oi3.setMoney(98);
				oi3.setUseType("1000");
				oi3.setDesc("一篮扇贝");
				oi3.setCustomInfo("一篮扇贝");
				
				chooseRechargeChannel(oi3);
				break;
			case REQ_CODE_FOUR:
				OrderInfo oi4 = new OrderInfo();
				oi4.setMoney(298);
				oi4.setUseType("1000");
				oi4.setDesc("一箱扇贝");
				oi4.setCustomInfo("一箱扇贝");
				
				chooseRechargeChannel(oi4);
				break;
				
			default:
				break;
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 注销广播接收者
		try {
			unregisterReceiver(msgReceiver);
			msgReceiver = null;
		} catch (Exception e) {
		}
	}
	
		
	/**
	 * 新消息广播接收者
	 * 
	 * 
	 */
	private class NewMessageBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String msgid = intent.getStringExtra("msgid");
			// 收到这个广播的时候，message已经在db和内存里了，可以通过id获取mesage对象
			EMMessage message = EMChatManager.getInstance().getMessage(msgid);
			if(message!=null){
				String type = message.getStringAttribute("type", "0");
				if(type.equals("1001")){//充值通知
					String content = message.getStringAttribute("content", "0");
					try {
						Gson gson = new Gson();
						NoticeModel noticeModel = gson.fromJson(content, NoticeModel.class);
						
						PayInfo payInfo = noticeModel.getPayInfo();
						MyApplication.getApp().getSpUtil().setMyMoney(payInfo.getMdou());
						//刷新总金额
						setBalance(payInfo.getMdou());
						showSuccessDialog();
					} catch (JsonSyntaxException e) {
						// TODO Auto-generated catch block
						queryMoney();
						
					}
				}else if("1006".equals(type)){
					//VIP充值成功，更改数据库中信息，更新用户是VIP以及刷新缓存
					UserManager.getInstance(RechargeActivity.this).updateUserInfo(true);
//					MyApplication.getApp().getSpUtil().defaultNumOfUpperLimit(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
					Integer flag = UserManager.getInstance(RechargeActivity.this).getCurrentUser().getIsVIP();
					UpdateCacheOfUpperLimit.getInstance().cache(flag==null?1:flag.intValue(),RechargeActivity.this);
				}
			}
		}
	}
	private void initView(){
		setContentView(R.layout.activity_recharge);
		ll_main =  findViewById(R.id.ll_main);
		tv_ye = (TextView) findViewById(R.id.tv_ye);
		bt_01 = (Button) findViewById(R.id.bt_01);
		bt_02 = (Button) findViewById(R.id.bt_02);
		bt_03 = (Button) findViewById(R.id.bt_03);
		bt_04 = (Button) findViewById(R.id.bt_04);
		
		if(MoMaUtil.isNetworkAvailable(this)){
			ll_main.setVisibility(View.VISIBLE);
			queryMoney();
		}else{
			ll_main.setVisibility(View.GONE);
			showMsg("网络不可用");
		}
	}
	
	//查询余额
	private void queryMoney(){
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		BottleRestClient.post("queryShanbei", this, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					QueryShanbeiBModel baseModel = gson.fromJson(str, QueryShanbeiBModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
						if("0".equals(baseModel.getCode())){
							MyApplication.getApp().getSpUtil().setMyMoney(baseModel.getShanbei());
							setBalance(baseModel.getShanbei());
						}
					}else{
//						showMsg("查询余额失败");
					}
				}else{
//					showMsg("查询余额失败");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//				showMsg("查询余额失败");
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
			}
        });
	}
	
	private void changeBtnEnable(boolean flag){
		bt_01.setEnabled(flag);
		bt_02.setEnabled(flag);
		bt_03.setEnabled(flag);
		bt_04.setEnabled(flag);
	}
	
	//充值扇贝
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_back:
				this.finish();
				break;
			case R.id.bt_01:
				changeBtnEnable(false);
				if(UserManager.getInstance(RechargeActivity.this).getCurrentUser()==null){
					launchForResult(LoginActivity.class,REQ_CODE_ONE);
					return ;
				}
				OrderInfo oi = new OrderInfo();
				oi.setMoney(10);
				oi.setDesc("一把扇贝");
				oi.setCustomInfo("一把扇贝");
				oi.setUseType("1000");
				chooseRechargeChannel(oi);
				break;
			case R.id.bt_02:
				changeBtnEnable(false);
				if(UserManager.getInstance(RechargeActivity.this).getCurrentUser()==null){
					launchForResult(LoginActivity.class,REQ_CODE_TWO);
					return ;
				}
				OrderInfo oi2 = new OrderInfo();
				oi2.setMoney(25);
				oi2.setUseType("1000");
				oi2.setDesc("一盒扇贝");
				oi2.setCustomInfo("一盒扇贝");
				
				chooseRechargeChannel(oi2);
				break;
			case R.id.bt_03:
				changeBtnEnable(false);
				if(UserManager.getInstance(RechargeActivity.this).getCurrentUser()==null){
					launchForResult(LoginActivity.class,REQ_CODE_THREE);
					return ;
				}
				OrderInfo oi3 = new OrderInfo();
				oi3.setUseType("1000");
				oi3.setMoney(98);
				oi3.setDesc("一篮扇贝");
				oi3.setCustomInfo("一篮扇贝");
				
				chooseRechargeChannel(oi3);
				break;
			case R.id.bt_04:
				changeBtnEnable(false);
				if(UserManager.getInstance(RechargeActivity.this).getCurrentUser()==null){
					launchForResult(LoginActivity.class,REQ_CODE_FOUR);
					return ;
				}
				OrderInfo oi4 = new OrderInfo();
				oi4.setUseType("1000");
				oi4.setMoney(298);
				oi4.setDesc("一箱扇贝");
				oi4.setCustomInfo("一箱扇贝");
				
				chooseRechargeChannel(oi4);
				break;
		}
	}
	
	// 选择支付渠道
	private void chooseRechargeChannel(final OrderInfo oi) {
		handler.postDelayed(run, 1500);//禁止测试MM疯狂点击，1.5秒钟按钮有效
//		String[] item={"支付宝","银联","微信"};
		String[] item={"支付宝", "微信"};
		new CustomDialog().showListDialog(this, "请选择充值方式",item,new CustomDialogItemClickListener() {
			@Override
			public void confirm(String result) {
				if("支付宝".equals(result)) {
					oi.setPayType("ALIPAY");
				} else if("银联".equals(result)) {
					oi.setPayType("UPMP");
				} else if("微信".equals(result)) {
					msgApi = WXAPIFactory.createWXAPI(RechargeActivity.this, null);
					msgApi.registerApp(getResources().getString(R.string.WXAPP_ID));
					oi.setPayType("WEIXIN");//TODO 
				}
				createPayOrder(oi);
			}
			
		});
	}
	
	TimeOutProgressDialog mpDialog;
	
	//请求服务器创建订单
	public void createPayOrder(final OrderInfo oi){
		
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("money", oi.getMoney()+"");
		jo.addProperty("payType", oi.getPayType());
		jo.addProperty("useType", oi.getUseType());//使用类别,1000表示充值，1001表示开通会员
		jo.addProperty("come", "6000");
		
		BottleRestClient.post("createPayOrder", this, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				super.onStart();
				if (mpDialog == null) {
					mpDialog = new TimeOutProgressDialog(RechargeActivity.this);
					//这里要比自己的10*1000要长，超过了说明异常了
					mpDialog.setTimeOut(15*1000, new TimeOutProgressDialog.OnTimeOutListener() {
						public void onTimeOut() {
							if (mpDialog != null && mpDialog.isShowing()) {
								mpDialog.cancel();
							}
							
							BottleRestClient.cancelRequests(RechargeActivity.this, true);
							
							showMsg("提交超时");
							setResult(RESULT_CANCELED);
							finish();
						}
					});
				}
				mpDialog.show();
				mpDialog.setContent("提交中...");
			}
			
			@Override
			public void onFinish() {
				if (mpDialog != null && mpDialog.isShowing()) {
					mpDialog.cancel();
				}
				super.onFinish();
			}
			//TODO 
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					PayOrderModel baseModel = gson.fromJson(str, PayOrderModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
						if("0".equals(baseModel.getCode())){
							orderId = baseModel.getOrderId();
							// 订单创建成功，跳转充值页面
							if("ALIPAY".equals(oi.getPayType())) {
								Intent intent = new Intent(RechargeActivity.this, WebFrameWithCacheActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
								intent.putExtra("webUrl", orderId);
								startActivityForResult(intent, 400);
							} else if("WEIXIN".equals(oi.getPayType())){
								PayReq req = new PayReq();
								req.appId = baseModel.getWxInfo().getAppid();
//								WXPayModel model = baseModel.getWxInfo();
								req.partnerId = baseModel.getWxInfo().getPartnerid();
								req.prepayId = baseModel.getWxInfo().getPrepayid();
								req.nonceStr = baseModel.getWxInfo().getNoncestr();
								req.timeStamp = baseModel.getWxInfo().getTimestamp();
								req.packageValue = baseModel.getWxInfo().getIpackage();
								req.sign = baseModel.getWxInfo().getSign();
								req.extData	 = "app test"; // optional
								msgApi.sendReq(req);
							}
						}else{
							showMsg(baseModel.getMsg());
						}
					}else{
						showMsg("服务器繁忙，创建订单失败");
					}
				}else{
					showMsg("服务器繁忙，创建订单失败");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("服务器繁忙，创建订单失败");
			}
        });
	}
	
	//取消充值
	public void cancelPay(String orderId){
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("orderId", orderId);
		
		BottleRestClient.post("cancelPay", this, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				super.onStart();
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
			}
        });
	}
	
	private void showDialog() {
		AlertDialog.Builder builder = new Builder(RechargeActivity.this);
		builder.setMessage("您充值的扇贝将会在10分钟左右到账，如有疑问请点击设置-联系客服");
		builder.setTitle("温馨提示");
		
		builder.setPositiveButton("我知道了", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.create().show();
	}
	
	private void showSuccessDialog() {
		AlertDialog.Builder builder = new Builder(RechargeActivity.this);
		builder.setMessage("恭喜充值已到账！");
		builder.setTitle("温馨提示");
		
		builder.setPositiveButton("我知道了", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.create().show();
	}
	
	@Override
    protected void onPause() {
    	super.onPause();
	}
	
	@Override
    protected void onResume() {
    	super.onResume();
    	
    	changeBtnEnable(true);
    	
    	//刷新总金额
		setBalance(MyApplication.getApp().getSpUtil().getMyMoney());
	}
	
	
	
	//刷新总金额
	private void setBalance(int balance){
		tv_ye.setText(Html.fromHtml("扇贝余额: <font color=#ff9f19>" +balance + "</font>"));
		MyApplication.getApp().getSpUtil().setMyMoney(balance);
	}
	private void launchForResult(Class<?> clazz,int requestCode) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, clazz);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivityForResult(intent, requestCode);
	}
	
	Handler handler = new Handler();
	public Runnable run = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			changeBtnEnable(true);
		}
	};

}