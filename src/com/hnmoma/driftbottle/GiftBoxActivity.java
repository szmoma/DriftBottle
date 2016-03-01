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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hnmoma.driftbottle.adapter.GiftAdapter;
import com.hnmoma.driftbottle.model.MyGiftsModel;
import com.hnmoma.driftbottle.model.QueryGiftLogBModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 未答谢的礼物/ 我的送礼记录
 * @author Young
 *
 */
public class GiftBoxActivity extends BaseActivity{
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	private String userId;
	private int comeFromTS;
	boolean isloading = false;
	boolean hasMore = false;
	static final int PAGENUM = 15;
	protected  final int REFRESH = 1; 
	String giftUId;
	int pageIndex;
	int index;// 判断送出还是收到
	
	PullToRefreshListView lv_gift;
	GiftAdapter giftAdapter;
	TextView tv_giftname;

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
		outState.putString("userId", userId);
		outState.putInt("comeFromTS", comeFromTS);
		outState.putInt("itemIndex", index);
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
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
		
		if (savedInstanceState != null){
			userId = savedInstanceState.getString("userId");
			comeFromTS = savedInstanceState.getInt("comeFromTS");
			index = savedInstanceState.getInt("itemIndex");
			
		}
		
		Intent intent = getIntent();
		if(intent != null){
			userId = intent.getStringExtra("userId");
			comeFromTS = intent.getIntExtra("comeFromTS", 0);
			index = intent.getIntExtra("itemIndex", 0);
		}
		
		initView();
		initData();
	}

	private void initView() {
		setContentView(R.layout.activity_giftbox);
		tv_giftname = (TextView) findViewById(R.id.tv_giftname);
		lv_gift = (PullToRefreshListView) findViewById(R.id.lv_gift);
		if(index == 0) {
			tv_giftname.setText("未答谢礼物");
			giftAdapter = new GiftAdapter(imageLoader, options, true, this);
		} else if(index == 1){
			tv_giftname.setText("送礼记录");
			giftAdapter = new GiftAdapter(imageLoader, options, false, this);
		}
		lv_gift.setAdapter(giftAdapter);
	}

	private void initData() {
		giftUId = "0";
		pageIndex = 1;
		pickDatas(index);
		lv_gift.setMode(Mode.PULL_FROM_END);//底部刷新
		lv_gift.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {
				//加载更多
				if(hasMore && !isloading){
					isloading = true;
					pickDatas(index);
				 }
			}
		});
		
//		lv_gift.setOnScrollListener(new OnScrollListener() {
//			@Override
//			public void onScrollStateChanged(AbsListView view, int scrollState) {
//				if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
//					if(hasMore && !isloading){
//						isloading = true;
//						pickDatas(index);
//					 }
//				}
//			}
//			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//			}
//		});
		
		lv_gift.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View view, int position, long id) {
				MyGiftsModel mm = (MyGiftsModel) av.getItemAtPosition(position);
				
				if(mm.getUserId().equals(userId)){
					Intent intent = new Intent(GiftBoxActivity.this, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("identityflag", 0);
					intent.putExtra("userId", mm.getUserId());
					intent.putExtra("visitUserId", userId);
					startActivity(intent);
				}else{
					Intent intent = new Intent(GiftBoxActivity.this, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("identityflag", 1);
					intent.putExtra("userId", mm.getUserId());
					intent.putExtra("visitUserId", userId);
					startActivity(intent);
				}
			}
		});
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
	
	// 收送礼物的查询
	private void pickDatas(final int index){
			
			String type = "";
			if(index == 0) {
				type = "1002";
			} else if(index == 1){
				type = "1001";
			}
			JsonObject jo = new JsonObject();
			jo.addProperty("userId", userId);
			jo.addProperty("giftUId", giftUId);
			jo.addProperty("pageIndex", pageIndex);
			jo.addProperty("pageNum", PAGENUM);
			jo.addProperty("type", type);
			BottleRestClient.post("queryGiveGiftLog", this, jo, new AsyncHttpResponseHandler(){
				@Override
				public void onStart() {
					super.onStart();
					lv_gift.setVisibility(View.VISIBLE);
					showDialog("努力加载...", "加载失败...", 15*1000);
				}
				
				@Override
				public void onFinish() {
					super.onFinish();
					closeDialog(mpDialog);
					isloading = false;
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
									if(hasMore) {
										giftUId = myGifts.get(myGifts.size()-1).getGiftUId();
										pageIndex += 1;
									}
									giftAdapter.addItemLast(myGifts);
									Message msg = Message.obtain();
									msg.arg1  =REFRESH;
									handler.sendMessage(msg);
								}
								
							}else if("1000".equals(baseModel.getCode())){
								findViewById(R.id.no_message).setVisibility(View.VISIBLE);
								lv_gift.setVisibility(View.GONE);
								if(index==1){	//自己送礼
									((TextView)findViewById(R.id.tv_no_message)).setText(getResources().getString(R.string.tip_no_send_gift));
								}else
									((TextView)findViewById(R.id.tv_no_message)).setText(getResources().getString(R.string.tip_no_thank_gift));
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