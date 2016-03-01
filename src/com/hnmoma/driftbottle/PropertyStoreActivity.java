package com.hnmoma.driftbottle;

import org.apache.http.Header;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.adapter.PersonalityAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.QueryPropertyStoreBModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 道具商城
 * @author Young
 *
 */
public class PropertyStoreActivity extends BaseActivity {
	public static final int REQ_CODE_BUY = 101;//购买道具
	private  final int REQ_CODE_MY = 100;//进入我的道具页面
	private final int FRESH = 1; //刷新ListView
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	ListView lv_personality;
	PersonalityAdapter adapter;
	
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
		
		setContentView(R.layout.activity_personality);
		init();
		pickDatas();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK&&requestCode ==REQ_CODE_MY){
			Intent intent = new Intent(this, DjscActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			startActivity(intent);
		}else if(requestCode==REQ_CODE_BUY&&resultCode==RESULT_OK){
			Intent intent= new Intent();
			intent.putExtra("hasBrush", true);
			setResult(RESULT_OK, intent);
		}
	}

	private void init(){ 
		lv_personality=(ListView) findViewById(R.id.lv_personality);
		((TextView)findViewById(R.id.tv_vzone_name)).setText(getResources().getString(R.string.shop));;
		
		adapter = new PersonalityAdapter(imageLoader, options, this);
		lv_personality.setAdapter(adapter);
	} 
	
	private void pickDatas(){
			
			//查询道具
			JsonObject jo2 = new JsonObject();
			jo2.addProperty("uuid", "0");
			BottleRestClient.post("queryPropertyStore", this, jo2, new AsyncHttpResponseHandler(){
				
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
									msg.what = FRESH;
									mHandler.sendMessage(msg);
								}
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
					showDialog("努力加载...","加载失败...",15*1000);
				}
	        });
		}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_back:
			this.finish();
			break;

		case R.id.bt_mydj://我的道具
			if(UserManager.getInstance(this).getCurrentUser()==null){
				Intent intent = new Intent(this, LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivityForResult(intent, REQ_CODE_MY);
				return;
			}
			Intent intent = new Intent(this, DjscActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			startActivity(intent);
			break;
		}

	}
}
