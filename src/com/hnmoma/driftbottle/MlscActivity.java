package com.hnmoma.driftbottle;

import java.util.LinkedList;

import org.apache.http.Header;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.adapter.CharmAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CharmTranslateDialog;
import com.hnmoma.driftbottle.model.ChangeCharmModel;
import com.hnmoma.driftbottle.model.QueryCharmModel;
import com.hnmoma.driftbottle.model.StoreModel;
import com.hnmoma.driftbottle.model.UserAccountInfo;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 魅力商城
 * @author Administrator
 *
 */
public class MlscActivity extends BaseActivity {
	private final int FRESH  =1;//刷新GridView
	private TextView charmValue;
	TextView myCharmValue;
	TextView myCoupon;
	GridView giftlist;
	
	CharmAdapter charmAdapter;
	String userId;
	int giftMoney;// 购物礼券
	int charm; // 魅力值
	LinkedList<StoreModel>  mInfos;

	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	private Handler mHandlelr = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FRESH:
				charmAdapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		};
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("giftMoney", giftMoney);
		outState.putString("userId", userId);
		outState.putInt("charm", charm);
		outState.putSerializable("mInfos", mInfos);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInfos = new LinkedList<StoreModel>();
		if(savedInstanceState != null) {
			giftMoney = savedInstanceState.getInt("giftMoney");
			userId = savedInstanceState.getString("userId");
			charm = savedInstanceState.getInt("charm");
			mInfos = (LinkedList<StoreModel>) savedInstanceState.getSerializable("mInfos");
		}
		
		options = new DisplayImageOptions.Builder()
						.showImageOnLoading(R.drawable.defalutimg)
						.showImageForEmptyUri(R.drawable.defalutimg)
						.showImageOnFail(R.drawable.defalutimg)
						.cacheInMemory(true)
						.cacheOnDisc(true).considerExifParams(true)
						.bitmapConfig(Bitmap.Config.RGB_565).build();
		
		initView();
		initData();
	}

	private void initView() {
		setContentView(R.layout.activity_charm);
		userId = UserManager.getInstance(this).getCurrentUserId();
		myCharmValue = (TextView) findViewById(R.id.tv_mycharmvalue);
		myCoupon = (TextView) findViewById(R.id.tv_mycoupon);
		giftlist = (GridView) findViewById(R.id.giftlist);
		
		charmAdapter = new CharmAdapter(this, imageLoader, options);
		giftlist.setAdapter(charmAdapter);
		
		giftlist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
					StoreModel gift = mInfos.get(position);
					Intent intent = new Intent(MlscActivity.this, GiftDetailActivity.class);
					intent.putExtra("gift", gift);
					intent.putExtra("giftMoney", giftMoney);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivityForResult(intent, 1000);
			}
		});
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultData, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultData, data);
		if(requestCode == 1000) {
			if(resultData == RESULT_OK ) {
				giftMoney = data.getIntExtra("remaining", 0);
				myCoupon.setText(giftMoney+"");
			}
		}
	}
	
	private void initData() {
		queryCharmAndMoney();
		String str = MyApplication.getApp().getSpUtil().getCharmCache();// 缓存
		if (TextUtils.isEmpty(str)) {
			pickDatas(false);
		} else {
			Gson gson = new Gson();
			QueryCharmModel baseModel = gson.fromJson(str, QueryCharmModel.class);
			if (baseModel != null && baseModel.getStoreList() != null && baseModel.getStoreList().size() != 0) {
				mInfos.addAll(baseModel.getStoreList());
				charmAdapter.addItemLast(mInfos);
				Message msg = Message.obtain();
				msg.what = FRESH;
				mHandlelr.sendMessage(msg);
			} else {
				pickDatas(false);
			}
		}

	}

	/*
	 * 查询魅力值和礼券
	 */
	private void queryCharmAndMoney() {
		
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", userId);
		jo.addProperty("deviceId", getDeviceId());
		BottleRestClient.post("queryAccount", this, jo,new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							UserAccountInfo baseModel = gson.fromJson(str, UserAccountInfo.class);
							if (baseModel != null && !TextUtils.isEmpty(baseModel.getCode())) {
								if ("0".equals(baseModel.getCode())) {
									giftMoney = baseModel.getAccountInfo().getGiftMoney();
									charm = baseModel.getAccountInfo().getCharm();
									myCoupon.setText(giftMoney + "");
									myCharmValue.setText(charm + "");
								}
							} else {
								 showMsg("查询余额失败");
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
	/*
	 * 魅力商城数据请求
	 */
	private void pickDatas(final boolean isRefresh) {
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", userId);
		jo.addProperty("deviceId", getDeviceId());
		BottleRestClient.post("queryCharmStore",this, jo,new AsyncHttpResponseHandler() {
					@Override
					public void onStart() {
					// TODO Auto-generated method stub
					super.onStart();
						showDialog("努力加载...","加载失败...",15*1000);
					}
					@Override
					public void onSuccess(int statusCode, Header[] headers,byte[] responseBody) {
						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							QueryCharmModel baseModel = gson.fromJson(str, QueryCharmModel.class);
							if (baseModel != null && !TextUtils.isEmpty(baseModel.getCode())) {
								if ("0".equals(baseModel.getCode())) {
									if (baseModel.getStoreList() != null) {
										MoMaLog.d("魅力商城返回了几条数据 ", baseModel.getStoreList().size() + "条");
										mInfos.addAll(baseModel.getStoreList());
										charmAdapter.addItemLast(baseModel.getStoreList());
										Message msg = Message.obtain();
										msg.what = FRESH;
										mHandlelr.sendMessage(msg);

										MyApplication.getApp().getSpUtil().setCharmCache(str);
									} else {
										MoMaLog.d("道具商城返回了几条数据 ", "0条");
									}
								}
							} else {
								showMsg("加载失败");
							}
						} else {
							showMsg("加载失败");
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						showMsg("加载失败");
						closeDialog(mpDialog);
					}

					@Override
					public void onFinish() {
						super.onFinish();
						closeDialog(mpDialog);
					}
				});

	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_back:
			this.finish();
			break;

		case R.id.tv_rechange: //领取礼券
			final CharmTranslateDialog dialog = new CharmTranslateDialog(this, R.style.style_dialog_ballon);
			dialog.show();
			// 设置宽度
			WindowManager windowManager = getWindowManager();
			Display display = windowManager.getDefaultDisplay();
			WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
			lp.width = (int) (display.getWidth()); 
			dialog.getWindow().setAttributes(lp);
			charmValue = (TextView) dialog.findViewById(R.id.tv_charmvalue);
			
			charmValue.setText(getResources().getString(R.string.tv_translatable_charm)+charm);
			
			dialog.setOnConfirmClick(new Button.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(TextUtils.isEmpty(dialog.getValue())) {
						showMsg(getResources().getString(R.string.tip_input_charm_value));
						return;
					}
					int convertValue = Integer.valueOf(dialog.getValue());
					if(convertValue > charm) {
						showMsg(getResources().getString(R.string.tip_warm_charm_value));
					} else if(convertValue < 1 ){
						showMsg(getResources().getString(R.string.tip_error_charm_value));
					}  else {
						doConvertCharm(convertValue);
						dialog.dismiss();
					}
				}
			});
			break;

		case R.id.rl_myGift://我的礼品
			Intent intent = new Intent(this, MyGiftActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			startActivity(intent);
			break;
		}
	}
	/**
	 * 把魅力值转化为礼券
	 * @param convertValue 魅力值
	 */
	protected void doConvertCharm(int convertValue) {
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", userId);
		jo.addProperty("price", convertValue+"");
		jo.addProperty("deviceId", getDeviceId());
		BottleRestClient.post("changeCharm",this, jo,new AsyncHttpResponseHandler() {
					@Override
					public void onStart() {
					// TODO Auto-generated method stub
					super.onStart();
						showDialog("正在领取礼券...", "领取礼券失败...", 15*1000);
					}
					@Override
					public void onSuccess(int statusCode, Header[] headers,byte[] responseBody) {
						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							ChangeCharmModel baseModel = gson.fromJson(str, ChangeCharmModel.class);
							if (baseModel != null && !TextUtils.isEmpty(baseModel.getCode())) {
								if ("0".equals(baseModel.getCode())) {
									myCoupon.setText(baseModel.getGiftMoney());
									myCharmValue.setText(baseModel.getCharm());
									charm = Integer.parseInt(baseModel.getCharm());
									giftMoney = Integer.parseInt(baseModel.getGiftMoney());
								}
							} else {
								showMsg(getResources().getString(R.string.tip_error_trainslate));
							}
						} else {
							showMsg(getResources().getString(R.string.tip_error_trainslate));
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						showMsg(getResources().getString(R.string.tip_error_trainslate));
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
