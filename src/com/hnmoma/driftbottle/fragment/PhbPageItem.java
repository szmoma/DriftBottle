package com.hnmoma.driftbottle.fragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.PhbActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.adapter.PhbAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.HotTopBModel;
import com.hnmoma.driftbottle.model.TopUserModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;


public class PhbPageItem extends BaseFragment implements OnClickListener{
	
	ListView mylv;
	PhbAdapter adapter;
	View pb;
	
	boolean isloading;
	boolean hasMore = true;
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	int index;
	
//	PhbHeadAdapter phbHeadAdapter;
//	GridView gv_phbhead;
	HotTopBModel baseModel;
	String visitUserId;
	PhbActivity phba;
	
	public static PhbPageItem newInstance(int index) {
		PhbPageItem mbpi = new PhbPageItem();
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
		View contextView = inflater.inflate(R.layout.fragment_phbit, container, false);
		mylv = (ListView) contextView.findViewById(R.id.mylv);
		pb = contextView.findViewById(R.id.fl_pb); //进度条
		
//		mylv.setOnItemClickListener(new OnItemClickListener(){
//			@Override
//			public void onItemClick(AdapterView<?> av, View view, int position, long id) {
//				if(UserManager.getInstance(act).getCurrentUser()==null){
//					Intent intent = new Intent(act, LoginActivity.class);
//					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//					startActivityForResult(intent, 400);
//				}else{
//					List<TopUserModel> list = adapter.getItem(position);
//					Intent HotItem = new Intent(act, HotTopItemActivity.class);
//					HotItem.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//					HotItem.putExtra("item", index);// 0：总榜 
//					if(adapter.getCount() < 4) {
//						int clickItem = 0;// 0 1 2 3
//						if(list.equals(baseModel.getFortuneList())) {
//							clickItem = 0;
//						} else if(list.equals(baseModel.getCharmList())) {
//							clickItem = 1;
//						} else if(list.equals(baseModel.getGamewinList())) {
//							clickItem = 2;
//						} else {
//							clickItem = 3;
//						}
//						HotItem.putExtra("position", clickItem);
//					} else {
//						HotItem.putExtra("position", position);
//					}
//					startActivity(HotItem);
//				}
//			}
//		});
		
		return contextView;
	}
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		phba = (PhbActivity)act;
		if(UserManager.getInstance(act).getCurrentUser()!=null){
			visitUserId = UserManager.getInstance(act).getCurrentUserId();
		}
		
		adapter = new PhbAdapter(getActivity(),imageLoader, options, index);
		mylv.setAdapter(adapter);
		
		String str = MyApplication.getApp().getSpUtil().getHotTopCache(index);
		if(TextUtils.isEmpty(str)){
			MoMaLog.e("没有热榜缓存重新请求", "没有热榜缓存重新请求");
			pickDatas();
		}else{
			Gson gson = new Gson();
			baseModel = gson.fromJson(str, HotTopBModel.class);
			if(baseModel != null){
//				List<TopUserModel> userList = baseModel.getUserList();
//				phbHeadAdapter.addItemLast(userList.subList(0, 6));
//				phbHeadAdapter.notifyDataSetChanged();
				Map<Integer, List<TopUserModel>> newDatas  = new HashMap<Integer, List<TopUserModel>>();
//				newDatas.put(0, baseModel.getFortuneList());
//				newDatas.put(1, baseModel.getCharmList());
//				newDatas.put(2, baseModel.getGamewinList());
//				newDatas.put(3, baseModel.getNewpList());
				
				newDatas.put(0, baseModel.getNewpList());
				newDatas.put(1, baseModel.getCharmList());
				newDatas.put(2, baseModel.getFortuneList());
				newDatas.put(3, baseModel.getGamewinList());
				adapter.addItemLast(newDatas);
				adapter.notifyDataSetChanged();
//				MyApplication.getApp().getSpUtil().setHotTopCache(index, str);
//				adapter.addItemLast(userList.subList(6, userList.size()));
//				adapter.notifyDataSetChanged();
				MoMaLog.e("有热榜缓存重用", "有热榜缓存重用");
			}else {
				MoMaLog.e("热榜缓存损坏，重新请求", "热榜缓存损坏，重新请求");
				pickDatas();
			}
		}
	}
	
	private void pickDatas(){
		pb.setVisibility(View.VISIBLE);
		
		JsonObject jo2 = new JsonObject();
//		if(index == 0){
//			jo2.addProperty("qtype", "hot1001");
//		}else{
		jo2.addProperty("qtype", "hot1000");
//		}
			
		//查询道具
		BottleRestClient.post("hotTop", act, jo2, new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					baseModel = gson.fromJson(str, HotTopBModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
						if("0".equals(baseModel.getCode())){
							Map<Integer, List<TopUserModel>> newDatas  = new HashMap<Integer, List<TopUserModel>>();;
//							if(baseModel.getFortuneList()!=null && baseModel.getFortuneList().size()> 0){
							newDatas.put(0, baseModel.getNewpList());
							newDatas.put(1, baseModel.getCharmList());
							newDatas.put(2, baseModel.getFortuneList());
							newDatas.put(3, baseModel.getGamewinList());
							adapter.addItemLast(newDatas);
							adapter.notifyDataSetChanged();
							MyApplication.getApp().getSpUtil().setHotTopCache(index, str);
//							}
						}else{
							showMsg(baseModel.getMsg());
						}
					}else{
						showMsg("加载失败");
					}
				}else{
					showMsg("加载失败");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("加载失败");
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				pb.setVisibility(View.GONE);
			}
        });
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
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 400){//未登录状态下进入个人中心
			if(resultCode == Activity.RESULT_OK){
				phba.reflash();
				if(UserManager.getInstance(act).getCurrentUser()!=null){
					visitUserId = UserManager.getInstance(act).getCurrentUserId();
				}
			}
		}
	}
}