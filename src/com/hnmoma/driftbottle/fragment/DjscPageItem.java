package com.hnmoma.driftbottle.fragment;

import org.apache.http.Header;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.DjDetailActivity;
import com.hnmoma.driftbottle.MyDjDetailActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.adapter.DjscAdapter;
import com.hnmoma.driftbottle.adapter.MyPropertyAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.PropsModel;
import com.hnmoma.driftbottle.model.QueryPropertyStoreBModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;


public class DjscPageItem extends BaseFragment implements OnClickListener{
	
	protected static final int REFRESH = 1; //刷新
	GridView mygv;
	DjscAdapter adapter;
	MyPropertyAdapter myAdapter;
	View pb;
	
	boolean isloading;
	boolean hasMore = true;
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	int index;
	
	private Handler  handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.arg1) {
			case REFRESH:
				adapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		};
	};
	
	public static DjscPageItem newInstance(int index) {
		DjscPageItem mbpi = new DjscPageItem();
		Bundle args = new Bundle();
		args.putInt("itemIndex", index);
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View contextView = inflater.inflate(R.layout.fragment_djscit, container, false);
		mygv = (GridView) contextView.findViewById(R.id.mygv);
		pb = contextView.findViewById(R.id.fl_pb);
		
		if(index == 0){
			mygv.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
					Intent intent = new Intent(act, DjDetailActivity.class);
					intent.putExtra("propsModel", adapter.getItem(position));
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					act.startActivityForResult(intent, 400);
				}
			});
		}else{
			mygv.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
					Intent intent = new Intent(act, MyDjDetailActivity.class);
					intent.putExtra("propsModel", myAdapter.getItem(position));
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					act.startActivityForResult(intent, 500);
				}
			});
		}
		
		return contextView;
	}
	
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
//		String str = MyApplication.getApp().getSpUtil().getDjscCache(index);
//		if(TextUtils.isEmpty(str)){
//			MoMaLog.e("Application缓存信息", "没有缓存重新请求");
			pickDatas();
//		}else{
//			Gson gson = new Gson();
//			QueryPropertyStoreBModel baseModel = gson.fromJson(str, QueryPropertyStoreBModel.class);
//			if(baseModel != null && baseModel.getPropertyList()!=null && baseModel.getPropertyList().size()!=0){
//				if(index == 0){
//					adapter = new DjscAdapter(imageLoader, options, index);
//					mygv.setAdapter(adapter);
//					
//					adapter.addItemLast(baseModel.getPropertyList());
//					adapter.notifyDataSetChanged();
//				}else{
//					myAdapter = new MyPropertyAdapter(imageLoader, options);
//					mygv.setAdapter(myAdapter);
//					
//					myAdapter.addItemLast(baseModel.getPropertyList());
//					myAdapter.notifyDataSetChanged();
//				}
//				
//				MoMaLog.e("Application缓存信息", "有缓存重用");
//			}else {
//				MoMaLog.e("Application缓存信息", "缓存损坏，重新请求");
//				pickDatas();
//			}
//		}
	}
	
	private void pickDatas(){
		if(index == 0){
			adapter = new DjscAdapter(imageLoader, options, index);
			mygv.setAdapter(adapter);
			
			//查询道具
			JsonObject jo2 = new JsonObject();
			jo2.addProperty("uuid", "0");
			BottleRestClient.post("queryPropertyStore", act, jo2, new AsyncHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					String str = new String(responseBody);
					if(!TextUtils.isEmpty(str)){
						Gson gson = new Gson();
						QueryPropertyStoreBModel baseModel = gson.fromJson(str, QueryPropertyStoreBModel.class);
						if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
							if("0".equals(baseModel.getCode())){
								if(baseModel.getPropertyList()!=null){
									MoMaLog.d("道具商城返回了几条数据 ", baseModel.getPropertyList().size()+"条");
									adapter.addItemLast(baseModel.getPropertyList());
									Message msg = Message.obtain();
									msg.arg1 = REFRESH;
									handler.sendMessage(msg);
									
//									MyApplication.getApp().getSpUtil().setDjscCache(index, str);
								}else{
									MoMaLog.d("道具商城返回了几条数据 ", "0条");
								}
							}
						}else{
							showMsg("道具商城加载失败");
						}
					}else{
						showMsg("道具商城加载失败");
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					showMsg("道具商城加载失败");
				}
				
				@Override
				public void onFinish() {
					super.onFinish();
					pb.setVisibility(View.GONE);
				}
				
				@Override
				public void onStart() {
					super.onStart();
					pb.setVisibility(View.VISIBLE);
				}
	        });
			
		}else{
			myAdapter = new MyPropertyAdapter(imageLoader, options);
			mygv.setAdapter(myAdapter);
			
			//查询我的道具
			JsonObject jo2 = new JsonObject();
			jo2.addProperty("userId", UserManager.getInstance(act).getCurrentUserId());
			BottleRestClient.post("queryMyProperty", act, jo2, new AsyncHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					String str = new String(responseBody);
					if(!TextUtils.isEmpty(str)){
						Gson gson = new Gson();
						QueryPropertyStoreBModel baseModel = gson.fromJson(str, QueryPropertyStoreBModel.class);
						if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
							if("0".equals(baseModel.getCode())){
								if(baseModel.getPropertyList()!=null){
									MoMaLog.d("我的道具返回了几条数据 ", baseModel.getPropertyList().size()+"条");
									myAdapter.addItemLast(baseModel.getPropertyList());
									Message msg = Message.obtain();
									msg.arg1 = REFRESH;
									handler.sendMessage(msg);
								}else{
									MoMaLog.d("道具商城返回了几条数据 ", "0条");
								}
							}else{
								showMsg(baseModel.getMsg());
							}
						}else{
							showMsg("我的道具加载失败");
						}
					}else{
						showMsg("我的道具加载失败");
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					showMsg("我的道具加载失败");
				}
				
				@Override
				public void onFinish() {
					super.onFinish();
					pb.setVisibility(View.GONE);
				}
				
				@Override
				public void onStart() {
					super.onStart();
					pb.setVisibility(View.VISIBLE);
				}
	        });
		}
	}
	

	@Override
	public boolean onBackPressed() {
		return false;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_cancel:
			break;
		}
	}
	
	public void updateItem(){
		pickDatas();
	}

	public int getDXPNum() {
		if(myAdapter==null){
			return 0;
		}
		
		int result = 0;
		for(int i = 0; i < myAdapter.getCount(); i++){
			PropsModel pm = myAdapter.getItem(i);
			String type = pm.getType();
			if(type.equals("1000")){
				result = pm.getNum();
			}
		}
		return result;
	}
}