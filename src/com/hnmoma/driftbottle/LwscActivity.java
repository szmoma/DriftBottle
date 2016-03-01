package com.hnmoma.driftbottle;

import org.apache.http.Header;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.adapter.LwscAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.BuyDialog;
import com.hnmoma.driftbottle.custom.BuyLwDialog;
import com.hnmoma.driftbottle.model.GiftsModel;
import com.hnmoma.driftbottle.model.QueryGiftBModel;
import com.hnmoma.driftbottle.model.QueryShanbeiBModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 礼物商城
 * @author young
 *
 */
public class LwscActivity extends BaseActivity{
	private final int FRESH = 1;//刷新GridView
	private String userId;	//接收礼物的对象
	private String visitUserId; //送礼物的对象
	TextView tv_ye;
	
	GridView mygv;
	LwscAdapter adapter;
	
	BuyLwDialog buyDialog;
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	int money;
	
	private  boolean isFromChat;	//是否从聊天页面到达此页面，如果该值是true，则需要返回值
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FRESH:
				adapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		};
	};
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("visitUserId", visitUserId);	//送礼物对象
		outState.putString("userId", userId);	//接收礼物对象
		outState.putInt("money", money);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null){
			userId = savedInstanceState.getString("userId");
			visitUserId = savedInstanceState.getString("visitUserId");
			money = savedInstanceState.getInt("money");
		}
		
		Intent intent = getIntent();
		if(intent != null){
			userId = intent.getStringExtra("userId");
			visitUserId = intent.getStringExtra("visitUserId");
			isFromChat = intent.getBooleanExtra("fromChat", false);
		}
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)
		.showImageForEmptyUri(R.drawable.defalutimg)
		.showImageOnFail(R.drawable.defalutimg)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
		
		initView();
		initData();
	}
	
	private void initView(){
		setContentView(R.layout.activity_lwsc);
        tv_ye = (TextView) findViewById(R.id.tv_ye);
        mygv = (GridView) findViewById(R.id.mygv);
		
		mygv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, final int position, long id) {
				if(buyDialog==null){
					buyDialog = new BuyLwDialog(LwscActivity.this, R.style.style_dialog_ballon, "赠送"); 
					buyDialog.show();  
					
					WindowManager windowManager = getWindowManager();
					Display display = windowManager.getDefaultDisplay();
					WindowManager.LayoutParams lp = buyDialog.getWindow().getAttributes();
					int rewidth = MoMaUtil.dip2px(LwscActivity.this, 300);
					lp.width = (int)(display.getWidth() > rewidth? rewidth: display.getWidth()); //设置宽度
					buyDialog.getWindow().setAttributes(lp);
				}else{
					buyDialog.show();  
				}
				
				buyDialog.setOnSubmitClick(new Button.OnClickListener(){
					public void onClick(View arg0) {
						buyDialog.cancel();
						GiftsModel gm = adapter.getItem(position);
						String giftId = gm.getGiftId();
						
						//余额判断
						if(money >= gm.getPrice()){
							songLW(gm.getGiftName(),gm.getShortPic(),gm.getPicUrl(),giftId, 1, userId, 0,gm.getCharm(),gm.getPrice());
						}else{
							//showMsg("哦哦~扇贝不足了~请先充值");
							final BuyDialog dialogToBuy = new BuyDialog(LwscActivity.this, R.style.style_dialog_ballon, "充值");
							dialogToBuy.show();
							dialogToBuy.setContent(gm.getShortPic(), "哇哦，扇贝不够~");
							dialogToBuy.setOnSubmitClick(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									dialogToBuy.dismiss();
									Intent intent = new Intent(LwscActivity.this, RechargeActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
									startActivity(intent);
								}
							});
						}
					}
				});
				
				GiftsModel gm = adapter.getItem(position);
				String [] msgs = new String[]{gm.getGiftName(), gm.getShortPic(),gm.getPrice()+"",gm.getRemark(),gm.getCharm()+""};
				buyDialog.setContent(msgs);
			}
		});
	}
	
	private void initData(){
		int money = MyApplication.getApp().getSpUtil().getMyMoney();
		if(money == 0){
			queryMoney();
		}else{
			this.money = money;
			tv_ye.setText("扇贝余额:  " + money);
		}
		
		adapter = new LwscAdapter(imageLoader, options);
		mygv.setAdapter(adapter);
		
		String str = MyApplication.getApp().getSpUtil().getGiftCache();
		if(TextUtils.isEmpty(str)){
			MoMaLog.e("Application缓存信息", "没有礼物缓存重新请求");
			pickDatas();
		}else{
			Gson gson = new Gson();
			QueryGiftBModel baseModel = gson.fromJson(str, QueryGiftBModel.class);
			if(baseModel != null && baseModel.getGiftList()!=null && baseModel.getGiftList().size()!=0){
				adapter.addItemLast(baseModel.getGiftList());
				Message msg = Message.obtain();
				msg.what = FRESH;
				mHandler.sendMessage(msg);
				
				MoMaLog.e("Application缓存信息", "有礼物缓存重用");
			}else {
				MoMaLog.e("Application缓存信息", "礼物缓存损坏，重新请求");
				pickDatas();
			}
		}
	}
	
	private void queryMoney(){
		//查询余额
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", visitUserId);
		BottleRestClient.post("queryShanbei", this, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					QueryShanbeiBModel baseModel = gson.fromJson(str, QueryShanbeiBModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
						if("0".equals(baseModel.getCode())){
							money = baseModel.getShanbei();
							tv_ye.setText("扇贝余额:  " + money);
							MyApplication.getApp().getSpUtil().setMyMoney(money);
						}
					}else{
//								showMsg("查询余额失败");
					}
				}else{
//							showMsg("查询余额失败");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//						showMsg("查询余额失败");
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
			}
        });
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_back:
				this.finish();
				break;
			case R.id.bt_charge:
				Intent intent = new Intent(LwscActivity.this, RechargeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(intent);
				break;
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
//    	if(requestCode == 400){
//			if(resultCode == Activity.RESULT_OK){
//				
//			}
//		}
	}
	
	private void pickDatas(){
		BottleRestClient.post("queryGift", null, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				showDialog("努力加载...", "加载失败...", 15*1000);
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					QueryGiftBModel baseModel = gson.fromJson(str, QueryGiftBModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
						if("0".equals(baseModel.getCode())){
							if(baseModel.getGiftList()!=null){
								MoMaLog.d("礼物商城返回了几条数据 ", baseModel.getGiftList().size()+"条");
								adapter.addItemLast(baseModel.getGiftList());
								Message msg = Message.obtain();
								msg.what = FRESH;
								mHandler.sendMessage(msg);
								
								MyApplication.getApp().getSpUtil().setGiftCache(str);
							}
						}
					}else{
						showMsg("礼物加载失败");
					}
				}else{
					showMsg("礼物加载失败");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("礼物加载失败");
				closeDialog(mpDialog);
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				closeDialog(mpDialog);
			}
        });
	}
	/**
	 * 送礼物
	 * @param giftName 礼物名称
	 * @param thumbUrl 礼物略缩图url
	 * @param url 礼物图片的url
	 * @param giftId 礼物ID
	 * @param num 礼物数量
	 * @param toUserId 礼物接收者
	 * @param type 类型，0表示礼物，1表示道具
	 * @param charm 送一件礼物奖励的魅力值
	 * @param price 礼物的价格
	 */
	private void songLW(final String giftName,final String thumbUrl, final String url,final String giftId, final int num, final String toUserId, final int type,final int charm, final int price){
		//查询道具
		String uId = UserManager.getInstance(LwscActivity.this).getCurrentUserId();	//当前用户ID
		if(TextUtils.isEmpty(visitUserId)||!uId.equals(visitUserId))	
			visitUserId = uId;
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", visitUserId);
		jo.addProperty("giftId", giftId);
		jo.addProperty("num", num);
		jo.addProperty("toUserId", toUserId);
		jo.addProperty("type", type);
		//1000表示直接送礼，1001表示从聊天中送礼
		if(isFromChat){
			jo.addProperty("comeType", 1001);
		}else
			jo.addProperty("comeType", 1000);
		BottleRestClient.post("giveGift", this, jo, new AsyncHttpResponseHandler(){
			
			@Override
			public void onStart() {
				super.onStart();
				showDialog("提交中...","提交失败",15*1000);
			}
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					QueryShanbeiBModel baseModel = gson.fromJson(str, QueryShanbeiBModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
						if("0".equals(baseModel.getCode())){
							money = baseModel.getShanbei();
							tv_ye.setText("扇贝余额:  "+money);
							MyApplication.getApp().getSpUtil().setMyMoney(money);
							
							if(isFromChat){
								Intent intent = new Intent();
								Bundle extras = new Bundle();
								extras.putString("giftName", giftName);
								extras.putString("thumbUrl", thumbUrl);
								extras.putString("giftUrl", url);
								extras.putString("userId", visitUserId);
								extras.putString("giftId", giftId);
								extras.putInt("num",num );
								extras.putString("toUserId",toUserId );
								extras.putInt("type", type);
								extras.putInt("charm", charm);
								extras.putInt("price",price);
								intent.putExtras(extras);
								setResult(Activity.RESULT_OK, intent);
								finish();
							}else{
								showMsg("赠送成功");
							}
							
						}else{
							showMsg(baseModel.getMsg());
						}
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
			
			@Override
			public void onFinish() {
				super.onFinish();
				closeDialog(mpDialog);
			}
        });
	}
	
}