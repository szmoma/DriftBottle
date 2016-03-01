package com.hnmoma.driftbottle.fragment;

import java.util.List;

import org.apache.http.Header;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.VzoneActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.adapter.GiftAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.MyGiftsModel;
import com.hnmoma.driftbottle.model.QueryGiftLogBModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BoxDetailFragment extends BaseFragment {
	private final int REFRESH = 1;
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	private String userId;
	boolean isloading = false;
	boolean hasMore = false;
	static final int PAGENUM = 15; 
	String giftUId;
	int pageIndex;
	int index;// 判断送出还是收到
	
	ListView lv_gift;
	GiftAdapter giftAdapter;
	View pb_progressbar;
	
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
	public boolean onBackPressed() {
		return false;
	}
	// 收送礼物实例
	public static BoxDetailFragment newInstance(int position) {
		BoxDetailFragment mbpi = new BoxDetailFragment();
		Bundle args = new Bundle();
		args.putInt("itemIndex", position);
		mbpi.setArguments(args);
		return mbpi;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("itemIndex", index);
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null){
			index = savedInstanceState.getInt("itemIndex");
		}
		
		Bundle bundle = getArguments();
		if(bundle != null){
			index = bundle.getInt("itemIndex");
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
		userId = UserManager.getInstance(act).getCurrentUserId();
//		showMsg("22222");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View convertView = inflater.inflate(R.layout.fragment_boxdetail, container, false);
		pb_progressbar = convertView.findViewById(R.id.pb_progressbar);
		lv_gift = (ListView) convertView.findViewById(R.id.lv_gift);
		if(index == 0) {
			giftAdapter = new GiftAdapter(imageLoader, options, true, act);
		} else if(index == 1){
			giftAdapter = new GiftAdapter(imageLoader, options, false, act);
		}
		lv_gift.setAdapter(giftAdapter);
//		showMsg("33333");
		return convertView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
//		showMsg("55555");
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 答谢后设置
		int position = getActivity().getIntent().getIntExtra("position", -1);
		if(position != -1) {
			if(giftAdapter!= null && giftAdapter.getItem(position) != null&&giftAdapter.getItem(position).getIsBack() ==0) {
//				giftAdapter.getItem(position).setIsBack(1);
				giftAdapter.remove(giftAdapter.getItem(position));
				Message msg = Message.obtain();
				msg.arg1  =REFRESH;
				handler.sendMessage(msg);
			}
		}
//		showMsg("66666");
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		giftUId = "0";
		pageIndex = 1;
		pickDatas(index);
		lv_gift.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
					if(hasMore && !isloading){
						isloading = true;
						pickDatas(index);
					 }
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
		
		lv_gift.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View view, int position, long id) {
				MyGiftsModel mm = (MyGiftsModel) av.getItemAtPosition(position);
				
				if(mm.getUserId().equals(userId)){
					Intent intent = new Intent(act, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("identityflag", 0);
					intent.putExtra("userId", userId);
					intent.putExtra("visitUserId", userId);
					startActivity(intent);
				}else{
					Intent intent = new Intent(act, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("identityflag", 1);
					intent.putExtra("userId", mm.getUserId());
					intent.putExtra("visitUserId", userId);
					startActivity(intent);
				}
			}
		});
//		showMsg("44444");
	}
	// 收送礼物的查询
	private void pickDatas(int index){
		
		String type = "";
		if(index == 0) {
			type = "1000";
		} else if(index == 1){
			type = "1001";
		}
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", userId);
		jo.addProperty("giftUId", giftUId);
		jo.addProperty("pageIndex", pageIndex);
		jo.addProperty("pageNum", PAGENUM);
		jo.addProperty("type", type);
		BottleRestClient.post("queryGiveGiftLog", act, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				super.onStart();
				pb_progressbar.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				pb_progressbar.setVisibility(View.GONE);
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
								msg.arg1 = REFRESH;
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
			
        });
	}
	
}
