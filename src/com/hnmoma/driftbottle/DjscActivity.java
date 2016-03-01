package com.hnmoma.driftbottle;

import org.apache.http.Header;

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
import com.hnmoma.driftbottle.adapter.MyDjAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.BackGridView;
import com.hnmoma.driftbottle.model.QueryPropertyStoreBModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 我的道具
 *
 */
public class DjscActivity extends BaseActivity{
	
	protected static final int REFRESH = 1;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
    BackGridView gv_mydj;
    TextView tv_no_message;
	View view_nodj;
	
    MyDjAdapter adapter;
    
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
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
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
		pickData();
	}
	
	private void initView(){
		setContentView(R.layout.activity_djsc);
		tv_no_message = (TextView) findViewById(R.id.tv_no_message);
		view_nodj = findViewById(R.id.view_nodj);
		
		gv_mydj = (BackGridView) findViewById(R.id.gv_mydj);
		adapter = new MyDjAdapter(imageLoader, options,this);
		gv_mydj.setAdapter(adapter);
		gv_mydj.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(DjscActivity.this, MyDjDetailActivity.class);
				intent.putExtra("propsModel", adapter.getItem(position));
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivityForResult(intent, 500);
			}
		});
		
	}
	
	private void pickData() {
		//查询我的道具
		JsonObject params = new JsonObject();
		params.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		BottleRestClient.post("queryMyProperty", this, params, new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					QueryPropertyStoreBModel baseModel = gson.fromJson(str, QueryPropertyStoreBModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
						if("0".equals(baseModel.getCode())){
							if(baseModel.getPropertyList()==null||baseModel.getPropertyList().isEmpty()){
								gv_mydj.setVisibility(View.GONE);
								view_nodj.setVisibility(View.VISIBLE);
								tv_no_message.setText(getResources().getString(R.string.tip_no_props));
							}else{
								adapter.addItemLast(baseModel.getPropertyList());
								Message msg = Message.obtain();
								msg.arg1  =REFRESH;
								handler.sendMessage(msg);
							}
						}else if("1000".equals(baseModel.getCode())){
							gv_mydj.setVisibility(View.GONE);
							view_nodj.setVisibility(View.VISIBLE);
							tv_no_message.setText(getResources().getString(R.string.tip_no_props));
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
				closeDialog(mpDialog);
			}
			
			@Override
			public void onStart() {
				super.onStart();
				showDialog("努力加载...", "加载失败...", 15*1000);
				gv_mydj.setVisibility(View.VISIBLE);
				view_nodj.setVisibility(View.GONE);
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