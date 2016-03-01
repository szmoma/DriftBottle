//package com.hnmoma.driftbottle;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import org.apache.http.Header;
//
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.GridView;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//import com.hnmoma.driftbottle.adapter.BadgeAdapter;
//import com.hnmoma.driftbottle.custom.BadgeDialog;
//import com.hnmoma.driftbottle.model.BadgeListModel;
//import com.hnmoma.driftbottle.model.QueryBadgeModel;
//import com.hnmoma.driftbottle.net.BottleRestClient;
//import com.loopj.android.http.AsyncHttpResponseHandler;
//import com.nostra13.universalimageloader.core.DisplayImageOptions;
//import com.nostra13.universalimageloader.core.ImageLoader;
///**
// * 徽章 页面
// *
// */
//public class BadgeActivity extends BaseActivity {
//	
//	protected ImageLoader imageLoader = ImageLoader.getInstance();
//	DisplayImageOptions options;
//	String userId;
//	List<BadgeListModel> badgelist;
//	
//	GridView gv_badge;
//	BadgeAdapter adapter;
//	
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		
//		options = new DisplayImageOptions.Builder()
//		.showImageOnLoading(R.drawable.defalutimg)
//		.showImageForEmptyUri(R.drawable.defalutimg)
//		.showImageOnFail(R.drawable.defalutimg)
//		.cacheInMemory(true)
//		.cacheOnDisc(true)
//		.considerExifParams(true)
//		.bitmapConfig(Bitmap.Config.RGB_565)
//		.build();
//		
//		setContentView(R.layout.activity_badge);
//		badgelist = new LinkedList<BadgeListModel>();
//		gv_badge = (GridView) findViewById(R.id.gv_badge);
//		adapter = new BadgeAdapter(imageLoader);
//		gv_badge.setAdapter(adapter);
//		gv_badge.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> av, View view, int position, long id) {
//					BadgeListModel model = badgelist.get(position);
//					final BadgeDialog dialogToBuy = new BadgeDialog(BadgeActivity.this, R.style.style_dialog_ballon);
//					dialogToBuy.show();
//					String progress = model.getSpeed() +" / " +model.getTotalSpeed();
//					dialogToBuy.setContent(model.getPicUrl(), model.getDescript(), progress);
//					dialogToBuy.setOnSubmitClick(new OnClickListener() {
//						@Override
//						public void onClick(View v) {
//							dialogToBuy.dismiss();
//						}
//					});
//			}
//		});
//		userId = getIntent().getStringExtra("userId");
//		pickUpData();
//	}
//	
//	private void pickUpData(){
//		JsonObject jo = new JsonObject();
//		jo.addProperty("userId", userId);
//		
//		BottleRestClient.post("queryMyBadge", this, jo, new AsyncHttpResponseHandler() {
//			@Override
//			public void onStart() {
//				super.onStart();
//			}
//	
//			@Override
//			public void onFinish() {
//				super.onFinish();
//			}
//	
//			@Override
//			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//				String str = new String(responseBody);
//				
//				if (!TextUtils.isEmpty(str)) {
//					Gson gson = new Gson();
//					QueryBadgeModel model = gson.fromJson(str, QueryBadgeModel.class);
//					
//					if (model != null && !TextUtils.isEmpty(model.getCode())) {
//						if ("0".equals(model.getCode())) {
//							badgelist = model.getBadgeList();
//							adapter.addItemLast(badgelist);
//							adapter.notifyDataSetChanged();
//						} else {
//							showMsg(model.getMsg());
//							finish();
//						}
//					} else {
//						showMsg("服务器繁忙");
//						setResult(RESULT_CANCELED);
//						finish();
//					}
//				} else {
//					showMsg("服务器繁忙");
//					setResult(RESULT_CANCELED);
//					finish();
//				}
//			}
//	
//			@Override
//			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//			}
//		});
//	}
//
//	public void onClick(View v) {
//		switch (v.getId()) {
//		case R.id.bt_back:
//			this.finish();
//			break;
//
//		default:
//			break;
//		}
//	}
//	
//
//}
