package com.hnmoma.driftbottle;

import java.util.List;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.adapter.GiftAdapter;
import com.hnmoma.driftbottle.model.MyGiftsModel;
import com.hnmoma.driftbottle.model.QueryGiftLogBModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class GiftActivity extends BaseActivity{
	
	private String userId;
	private String visitUserId;
	private int comeFromTS;
	
	ListView lv_gift;
	GiftAdapter giftAdapter;
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	boolean isloading;
	boolean hasMore = true;
	static final int PAGENUM = 15;
	protected  final int REFRESH = 1; 
	
	int pageIndex;
	String giftUId;
	
	private Handler  handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.arg1) {
			case REFRESH:
				giftAdapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		};
	};
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("visitUserId", visitUserId);
		outState.putString("userId", userId);
		outState.putInt("comeFromTS", comeFromTS);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)
		.showImageForEmptyUri(R.drawable.defalutimg)
		.showImageOnFail(R.drawable.defalutimg)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
//		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.bitmapConfig(Bitmap.Config.RGB_565)
//		.displayer(new RoundedBitmapDisplayer(2))
		.build();
		
		
		giftUId = "0";
		pageIndex = 1;
		if (savedInstanceState != null){
			userId = savedInstanceState.getString("userId");
			visitUserId = savedInstanceState.getString("visitUserId");
			comeFromTS = savedInstanceState.getInt("comeFromTS");
		}
		
		Intent intent = getIntent();
		if(intent != null){
			userId = intent.getStringExtra("userId");
			visitUserId = intent.getStringExtra("visitUserId");
			comeFromTS = intent.getIntExtra("comeFromTS", 0);
		}
		
		setContentView(R.layout.activity_gift);
		lv_gift = (ListView) findViewById(R.id.lv_gift);
		
		boolean isMySelf = userId.equals(visitUserId);
		
		giftAdapter = new GiftAdapter(imageLoader, options, isMySelf, this);
		lv_gift.setAdapter(giftAdapter);
		
		lv_gift.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
					if(hasMore && !isloading){
						isloading = true;
						pickDatas();
					 }
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
		lv_gift.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View view, int position, long id) {
				MyGiftsModel mm = (MyGiftsModel) av.getItemAtPosition(position);
				
				if(mm.getUserId().equals(visitUserId)){
					Intent intent = new Intent(GiftActivity.this, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("identityflag", 0);
					intent.putExtra("userId", mm.getUserId());
					intent.putExtra("visitUserId", visitUserId);
					startActivity(intent);
				}else{
					Intent intent = new Intent(GiftActivity.this, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("identityflag", 1);
					intent.putExtra("userId", mm.getUserId());
					intent.putExtra("visitUserId", visitUserId);
					startActivity(intent);
				}
			}
		});
		
		pickDatas();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		// 答谢后设置
		int position = this.getIntent().getIntExtra("position", -1);
		if(position != -1) {
			if(giftAdapter!= null && giftAdapter.getItem(position) != null&&giftAdapter.getItem(position).getIsBack() ==0) {
				//giftAdapter.getItem(position).setIsBack(1);
				giftAdapter.remove(giftAdapter.getItem(position));
				Message msg = Message.obtain();
				msg.arg1  =REFRESH;
				handler.sendMessage(msg);
			}
		}
	}
	
	private void pickDatas(){
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", userId);
		jo.addProperty("giftUId", giftUId);
		jo.addProperty("pageNum", PAGENUM);
		jo.addProperty("pageIndex", pageIndex);
		jo.addProperty("pageNum", PAGENUM);
		jo.addProperty("type", "1002");
		BottleRestClient.post("queryGiveGiftLog", this, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				super.onStart();
				showDialog("努力加载....","加载失败...",15*1000);
			}
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					QueryGiftLogBModel baseModel = gson.fromJson(str, QueryGiftLogBModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
						if("0".equals(baseModel.getCode())){
							List<MyGiftsModel> myGifts = baseModel.getLogList();
							
							if(myGifts != null && myGifts.size() != 0){
								hasMore = baseModel.getIsMore()==0 ? false:true;
								giftUId = myGifts.get(myGifts.size()-1).getGiftUId();
								
								giftAdapter.addItemLast(myGifts);
								Message msg = Message.obtain();
								msg.arg1  =REFRESH;
								handler.sendMessage(msg);
							}
							
						}else{
							showMsg(baseModel.getMsg());
						}
					}else{
						showMsg("查询失败");
					}
				}else{
					showMsg("查询失败");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("查询失败");
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				closeDialog(mpDialog);
				isloading = false;
			}
        });
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_back:
				if(comeFromTS==1){
					//从通知栏来的消息
					Intent intent = new Intent(this, MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivity(intent);
				}else{
					this.finish();
				}
				break;
//			case R.id.bt_dx:
//				int position = lv_gift.getPositionForView(v);
//				MyGiftsModel mm = giftAdapter.getItem(position);
////				doDx(mm);
//				
//				Intent intent = new Intent(GiftActivity.this, ThrowDaxiePingActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//				intent.putExtra("userId", userId);
//				intent.putExtra("visitUserId", visitUserId);
//				intent.putExtra("position", position);
//				intent.putExtra("myGiftsModel", mm);
//				showMsg("开启答谢");
//				startActivityForResult(intent, 400);
//				break;
		}
	}
	
	@Override
	public void onBackPressed() {
		if(comeFromTS==1){
			//从通知栏来的消息
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			startActivity(intent);
		}else{
			super.onBackPressed();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 500){
			if(resultCode == Activity.RESULT_OK){
				int position = data.getIntExtra("position", -1);
				getIntent().putExtra("position", position);
			}
		}
	}
}