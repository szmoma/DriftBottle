package com.hnmoma.driftbottle;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CustomDialog;
import com.hnmoma.driftbottle.custom.CustomDialog.CustomDialogItemClickListener;
import com.hnmoma.driftbottle.custom.TimeOutProgressDialog;
import com.hnmoma.driftbottle.model.NoticeModel;
import com.hnmoma.driftbottle.model.OrderInfo;
import com.hnmoma.driftbottle.model.PayInfo;
import com.hnmoma.driftbottle.model.PayOrderModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.net.UpdateCacheOfUpperLimit;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * 购买会员:在编码的时候，有些怪异。从UI可知，使用人民币支付，但是从接口上来说，使用扇贝作为支付。此处有一个很大的问题，需要和投胎处理。
 * @author yangsy
 *
 */
public class VIPBuyActivity extends BaseActivity {
	private static final int REQ_CODE_ONE = 100;//一个月的会员

	private static final int REQ_CODE_TWO = 101;	//三个月的会员

	private static final int REQ_CODE_THREE = 102; //六个月的会员

	private static final int REQ_CODE_FOUR = 103;	//一年的会员

	private static final int STATE_BTN_VALID = 1;	//启用整个页面的控件--可点击

	private static final int STATE_BTN_INVALID = 0;//禁用整个页面的控件--不可点击
	
	private int money;	//账户的金钱---扇贝数
	private TextView tvMoney;
	private IWXAPI msgApi;
	private TimeOutProgressDialog mpDialog;
	private String orderId;//订单
	private NewMessageBroadcastReceiver msgReceiver;
	
	private Handler  hander = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STATE_BTN_VALID:
				changeBtnEnable(true);
				break;
			case STATE_BTN_INVALID:
				changeBtnEnable(false);
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vip_buy);
		
		// 注册一个接收消息的BroadcastReceiver
		msgReceiver = new NewMessageBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
		intentFilter.setPriority(6);
		registerReceiver(msgReceiver, intentFilter);
		init();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 注销广播接收者
		try {
			unregisterReceiver(msgReceiver);
			msgReceiver = null;
		} catch (Exception e) {
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==Activity.RESULT_OK){
			switch (requestCode) {
			case REQ_CODE_ONE:
				OrderInfo oi = new OrderInfo();
				oi.setMoney(12);
				oi.setUseType("1001");
				oi.setDesc("购买1个月的会员");
				oi.setCustomInfo("购买1个月的会员");
				chooseRechargeChannel(oi);
				break;
			case REQ_CODE_TWO:
				OrderInfo oi2 = new OrderInfo();
				oi2.setMoney(30);
				oi2.setUseType("1001");
				oi2.setDesc("购买3个月的会员");
				oi2.setCustomInfo("购买3个月的会员");
				chooseRechargeChannel(oi2);
				break;
			case REQ_CODE_THREE:
				OrderInfo oi3 = new OrderInfo();
				oi3.setUseType("1001");
				oi3.setMoney(60);
				oi3.setDesc("购买6个月的会员");
				oi3.setCustomInfo("购买6个月的会员");
				
				chooseRechargeChannel(oi3);
				break;
			case REQ_CODE_FOUR:
				OrderInfo oi4 = new OrderInfo();
				oi4.setMoney(108);
				oi4.setUseType("1001");
				oi4.setDesc("购买12个月的会员");
				oi4.setCustomInfo("购买12个月的会员");
				
				chooseRechargeChannel(oi4);
				break;
				
			default:
				break;
			}
		}
	}
	
	private void init() {
		// TODO Auto-generated method stub
		tvMoney = (TextView) findViewById(R.id.tv_vip_buy);
		//初始化标题
		((TextView)findViewById(R.id.tv_title)).setText(getResources().getString(R.string.vip_buy));
		//icon返回事件注册
		findViewById(R.id.btn_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		//去充值
		findViewById(R.id.btn_recharge).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(VIPBuyActivity.this,RechargeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(intent);
			}
		});
		
		money = MyApplication.getApp().getSpUtil().getMyMoney();
		if(money>0)
			tvMoney.setText("扇贝余额:"+String.valueOf(money));
		else
			if(UserManager.getInstance(this).getCurrentUser()!=null)
				queryMoney(UserManager.getInstance(this).getCurrentUserId());
			else
				tvMoney.setText("扇贝余额:0");
		
	}

	public void onClick(View v){
		OrderInfo oi = new OrderInfo();
		switch (v.getId()) {
		case R.id.bt_01://购买一个月的会员
			changeBtnEnable(false);
			if(UserManager.getInstance(VIPBuyActivity.this).getCurrentUser()==null){
				launchForResult(LoginActivity.class,REQ_CODE_ONE);
				return ;
			}
			oi.setMoney(12);
			oi.setUseType("1001");
			oi.setDesc("购买1个月的会员");
			oi.setCustomInfo("购买1个月的会员");
			
			break;
		case R.id.bt_02:	//购买三个月的会员
			changeBtnEnable(false);
			if(UserManager.getInstance(VIPBuyActivity.this).getCurrentUser()==null){
				launchForResult(LoginActivity.class,REQ_CODE_TWO);
				return ;
			}
			oi.setMoney(30);
			oi.setUseType("1001");
			oi.setDesc("购买3个月的会员");
			oi.setCustomInfo("购买3个月的会员");
			break;
		case R.id.bt_03:	//购买六个月的会员
			changeBtnEnable(false);
			if(UserManager.getInstance(VIPBuyActivity.this).getCurrentUser()==null){
				launchForResult(LoginActivity.class,REQ_CODE_THREE);
				return ;
			}
			oi.setMoney(60);
			oi.setUseType("1001");
			oi.setDesc("购买6个月的会员");
			oi.setCustomInfo("购买6个月的会员");
			break;
		case R.id.bt_04: //购买一年的会员:
			changeBtnEnable(false);
			oi.setUseType("1001");
			if(UserManager.getInstance(VIPBuyActivity.this).getCurrentUser()==null){
				launchForResult(LoginActivity.class,REQ_CODE_FOUR);
				return ;
			}
			oi.setMoney(108);
			oi.setUseType("1001");
			oi.setDesc("购买12个月的会员");
			oi.setCustomInfo("购买12个月的会员");
			break;
		default:
			break;
		}
		chooseRechargeChannel(oi);
	}
	
	/**
	 * 查询账户的扇贝额度
	 * @param userId 账户id
	 */
	private void queryMoney(String userId){
		JsonObject params = new JsonObject();
		params.addProperty("userId", userId); 
		BottleRestClient.post("queryShanbei", this, params, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				// TODO Auto-generated method stub
				try {
					String respon = new String(responseBody);
					JSONObject obj = new JSONObject(respon);
					String code = obj.getString("code");
					if("0".equals(code)){
						String str_money = obj.getString("shanbei");
						if(MoMaUtil.isDigist(str_money)){
							tvMoney.setText("扇贝余额:"+str_money);
							MyApplication.getApp().getSpUtil().setMyMoney(Integer.valueOf(str_money));
						}
					}else 
						showMsg(obj.getString("msg"));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					MoMaLog.d("debug",e.getMessage());
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				// TODO Auto-generated method stub
				showMsg(new String(responseBody));
			}
		});
		
	}
	
	// 选择支付渠道
		private void chooseRechargeChannel(final OrderInfo oi) {
			Message msg = Message.obtain();
			msg.what = 	STATE_BTN_VALID;
			hander.sendMessageDelayed(msg, 1000);
			
//			String[] item={"支付宝","银联","微信"};
			String[] item={"支付宝", "微信"};
			new CustomDialog().showListDialog(this, "请选择充值方式",item,new CustomDialogItemClickListener() {
				@Override
				public void confirm(String result) {
					if("支付宝".equals(result)) {
						oi.setPayType("ALIPAY");
					} else if("银联".equals(result)) {
						oi.setPayType("UPMP");
					} else if("微信".equals(result)) {
						msgApi = WXAPIFactory.createWXAPI(VIPBuyActivity.this, null);
						msgApi.registerApp(getResources().getString(R.string.WXAPP_ID));
						oi.setPayType("WEIXIN");
					}
					createPayOrder(oi);
				}
			});
		}
		
		
		
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
						mpDialog = new TimeOutProgressDialog(VIPBuyActivity.this);
						//这里要比自己的10*1000要长，超过了说明异常了
						mpDialog.setTimeOut(15*1000, new TimeOutProgressDialog.OnTimeOutListener() {
							public void onTimeOut() {
								if (mpDialog != null && mpDialog.isShowing()) {
									mpDialog.cancel();
								}
								
								BottleRestClient.cancelRequests(VIPBuyActivity.this, true);
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
									Intent intent = new Intent(VIPBuyActivity.this, WebFrameWithCacheActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
									intent.putExtra("webUrl", orderId);
									startActivityForResult(intent, 400);
								} else if("WEIXIN".equals(oi.getPayType())){
									PayReq req = new PayReq();
									req.appId = baseModel.getWxInfo().getAppid();
//									WXPayModel model = baseModel.getWxInfo();
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
		
		private void launchForResult(Class<?> clazz,int requestCode) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(this, clazz);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			startActivityForResult(intent, requestCode);
		}
		
		private void changeBtnEnable(boolean flag){
			findViewById(R.id.bt_01).setEnabled(flag);
			findViewById(R.id.bt_02).setEnabled(flag);
			findViewById(R.id.bt_03).setEnabled(flag);
			findViewById(R.id.bt_04).setEnabled(flag);
			
		}
		
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
						Gson gson = new Gson();
						NoticeModel noticeModel = gson.fromJson(content, NoticeModel.class);
						
						PayInfo payInfo = noticeModel.getPayInfo();
						MyApplication.getApp().getSpUtil().setMyMoney(payInfo.getMdou());
						//刷新总金额
						MyApplication.getApp().getSpUtil().setMyMoney(payInfo.getMdou());
						showSuccessDialog();
					}else if("1006".equals(type)){
						//VIP充值成功，更改数据库中信息，更新用户是VIP以及刷新缓存
						UserManager.getInstance(VIPBuyActivity.this).updateUserInfo(true);
						Integer flag = UserManager.getInstance(VIPBuyActivity.this).getCurrentUser().getIsVIP();
						UpdateCacheOfUpperLimit.getInstance().cache(flag==null?1:flag.intValue(),VIPBuyActivity.this);
					}
				}
			}
		}
		
		private void showSuccessDialog() {
			AlertDialog.Builder builder = new Builder(this);
			builder.setMessage("恭喜充值已到账！");
			builder.setTitle("温馨提示");
			
			builder.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
				});
			builder.create().show();
		}
}
