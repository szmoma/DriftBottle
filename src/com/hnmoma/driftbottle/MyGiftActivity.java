package com.hnmoma.driftbottle;

import org.apache.http.Header;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hnmoma.driftbottle.adapter.MyGiftAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.QueryGiftModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;


public class MyGiftActivity extends BaseActivity {
	private final int MORE = 1;	//加载更多的标志
	private final int FINISH = 0; //没有更多的内容标志
	private final int FRESH = 2;//刷新ListView
	
	private PullToRefreshListView mPullRefreshListView;
	MyGiftAdapter adapter;
	
	LinearLayout ll_load_progressbar;
	View noMessageView;
	
	QueryGiftModel baseModel;
	String userId;
	int changeId = 0;// 记录id
	private static final int  PAGE_NUM = 8;// 页数
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	private boolean isLast; //是否是最后一条记录
	
	private Handler handler = new Handler()	{
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FINISH://
				mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多");
				mPullRefreshListView.onRefreshComplete();
				showMsg("没有更多了");
				break;
			case MORE:	//加载更多
				mPullRefreshListView.getLoadingLayoutProxy().setRefreshingLabel("正在加载");
				mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("上拉加载更多");
				mPullRefreshListView.getLoadingLayoutProxy().setReleaseLabel("释放开始加载");
				queryGift(true);
				break;
			case FRESH:
				adapter.notifyDataSetChanged();
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
		
		options = new DisplayImageOptions.Builder()
						.showImageOnLoading(R.drawable.defalutimg)
						.showImageForEmptyUri(R.drawable.defalutimg)
						.showImageOnFail(R.drawable.defalutimg).cacheInMemory(true)
						.cacheOnDisc(true).considerExifParams(true)
						.bitmapConfig(Bitmap.Config.RGB_565).build();
		
		setContentView(R.layout.activity_mygift);
		noMessageView = (View) findViewById(R.id.no_message);
		noMessageView.setVisibility(View.GONE);
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.list);
		
		adapter = new MyGiftAdapter(imageLoader, options, this);
		ListView actualListView = mPullRefreshListView.getRefreshableView();
		registerForContextMenu(actualListView);
		/**
		 * Add Sound Event Listener
		 */
//		SoundPullEventListener<ListView> soundListener = new SoundPullEventListener<ListView>(this);
//		soundListener.addSoundEvent(State.PULL_TO_REFRESH, R.raw.pull_event);
//		soundListener.addSoundEvent(State.RESET, R.raw.reset_sound);
//		soundListener.addSoundEvent(State.REFRESHING, R.raw.refreshing_sound);
//		mPullToRefreshListView.setOnPullEventListener(soundListener);
		
		mPullRefreshListView.setMode(Mode.PULL_FROM_END);
		actualListView.setAdapter(adapter);
		
		userId = UserManager.getInstance(this).getCurrentUserId(); 
		
		queryGift(false);
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				if(isLast){
					msg.what = FINISH;
				}else{
					msg.what = MORE;
				}
				handler.sendMessage(msg);
			}
		});

		
	}
	/*
	 * 我的礼品的历史记录
	 */
	/**
	 * 查询我的礼品
	 * @param isLoadMore 是否加载更多礼品
	 */
	private void queryGift(final boolean isLoadMore) {
		
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", userId);
		jo.addProperty("deviceId", getDeviceId());
		jo.addProperty("changeId", changeId);
		jo.addProperty("pageNum", PAGE_NUM);
		BottleRestClient.post("queryChangeGift",this, jo,new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,byte[] responseBody) {
						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							baseModel = gson.fromJson(str, QueryGiftModel.class);
							if (baseModel != null && !TextUtils.isEmpty(baseModel.getCode())) {
								mPullRefreshListView.onRefreshComplete();
								if ("0".equals(baseModel.getCode())) {
									if (baseModel.getChangeList() != null) {
										if(isLoadMore){
											adapter.addItemLast(baseModel.getChangeList());
										}else
											adapter.reset(baseModel.getChangeList());
										
										if(!isLoadMore){
											mPullRefreshListView.getRefreshableView().setSelection(mPullRefreshListView.getRefreshableView().getFirstVisiblePosition());
										}
										
										
										String mChangeId = baseModel.getChangeList().get(baseModel.getChangeList().size()-1).getChangeId();
										changeId = Integer.parseInt(mChangeId);
										if("1".equals(baseModel.getIsMore())){
											isLast = false;
										}else if("0".equals(baseModel.getIsMore())){
											isLast = true;
											if(isLoadMore){
												mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多了");
											}
										}
										Message msg = Message.obtain();
										msg.what = FRESH;
										handler.sendMessage(msg);
									} 
								}else if("1000".equals(baseModel.getCode())){
									if(!isLoadMore){
										mPullRefreshListView.setVisibility(View.GONE);
										noMessageView.setVisibility(View.VISIBLE);
										((TextView)findViewById(R.id.tv_no_message)).setText(getResources().getString(R.string.tip_no_exchange_gift));
									}
									isLast = true;
									if(isLoadMore){
										mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多了");
										showMsg("没有更多了");
									}
								}else{
									isLast = true;
									if(isLoadMore){
										mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多了");
										showMsg("没有更多了");
									}
								}
							} else {
								showMsg("加载失败");
								isLast = true;
								if(isLoadMore){
									mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多了");
								}
							}
						} else {
							showMsg("加载失败");
							isLast = true;
							if(isLoadMore){
								mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多了");
							}
						}
						if(isLoadMore)
						mPullRefreshListView.onRefreshComplete();
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						showMsg("加载失败");
						if(!isLoadMore)
							closeDialog(mpDialog);
						else
							mPullRefreshListView.onRefreshComplete();
					}
					
					@Override
					public void onStart() {
					super.onStart();
					if(!isLoadMore)
						showDialog("努力加载...", "加载失败...", 15*1000);
					noMessageView.setVisibility(View.GONE);
					mPullRefreshListView.setVisibility(View.VISIBLE);
					}

					@Override
					public void onFinish() {
						super.onFinish();
						if(!isLoadMore)
							closeDialog(mpDialog);
						else
							mPullRefreshListView.onRefreshComplete();
					}
				});
	}
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_back:
			this.finish();
			break;
		}
	}

	
}
