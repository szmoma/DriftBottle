package com.hnmoma.driftbottle;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.adapter.GiftWallAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.BackGridView;
import com.hnmoma.driftbottle.model.MyGiftsModel;
import com.hnmoma.driftbottle.model.QueryGiftLogBModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 礼物墙
 *
 */
public class GiftWallActivity extends BaseActivity implements OnClickListener {

	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	String userId;//被查看用户Id
	String visitUserId;//查看用户Id
	String giftUId;
	static final int PAGENUM = 16;
	protected  final int REFRESH = 1; 
	int pageIndex;
	boolean hasMore = false;
	boolean isloading = false;
	List<MyGiftsModel> myGifts;
	SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
	
	BackGridView gv_giftwall;
	GiftWallAdapter giftwallAdapter;
	TextView tv_nodx;
	RelativeLayout rl_mycharm;
	
	/**
	 * 身份标识，0表示自己看自己,1表示看别人
	 */
	int identityflag = 1;
	private Handler  handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.arg1) {
			case REFRESH:
				giftwallAdapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		};
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
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
		
		userId = getIntent().getStringExtra("userId");
		visitUserId = getIntent().getStringExtra("visitUserId");
		if(TextUtils.isEmpty(userId)){
			userId = visitUserId = UserManager.getInstance(this).getCurrentUserId();
			identityflag = 0;
		}else{
			if(TextUtils.isEmpty(visitUserId)){
				identityflag= 0;
				visitUserId = userId;
			}else{
				if(userId.equals(visitUserId)) {
					identityflag = 0;
				} else {
					identityflag = 1;
				}
			}
			
		}
		
		myGifts = new LinkedList<MyGiftsModel>();
		initView();
	}
	
	private void initView() {
		setContentView(R.layout.fragment_giftwall);
		tv_nodx = (TextView) findViewById(R.id.tv_nodx);
		tv_nodx.setOnClickListener(this);
		rl_mycharm = (RelativeLayout) findViewById(R.id.rl_mycharm);
		if(identityflag == 0) {// 看自己
			rl_mycharm.setVisibility(View.VISIBLE);
			tv_nodx.setVisibility(View.VISIBLE);
		} else {
			rl_mycharm.setVisibility(View.GONE);
			tv_nodx.setVisibility(View.GONE);
		}
		gv_giftwall = (BackGridView) findViewById(R.id.wgv_giftwall);
		
		giftwallAdapter = new GiftWallAdapter(imageLoader, options, true);
		gv_giftwall.setAdapter(giftwallAdapter);
		
		
		gv_giftwall.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> av, View view, int position, long id) {
				// TODO 礼物墙的每个礼物点击事件
				final MyGiftsModel model =  myGifts.get(position);
				View popupView = getLayoutInflater().inflate(R.layout.view_popup_giftwall, null);
				TextView tv_name = (TextView) popupView.findViewById(R.id.tv_name);
				TextView tv_charm = (TextView) popupView.findViewById(R.id.tv_charm);
				TextView tv_time = (TextView) popupView.findViewById(R.id.tv_time);
				ImageView iv_head = (ImageView) popupView.findViewById(R.id.iv_head);
				imageLoader.displayImage(model.getHeadImg(), iv_head, options);
				iv_head.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
							Intent intent = new Intent(GiftWallActivity.this, VzoneActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
							intent.putExtra("identityflag", identityflag);
							intent.putExtra("userId", model.getUserId());
							if(identityflag==0)
								intent.putExtra("visitUserId", userId);
							else
								intent.putExtra("visitUserId", visitUserId==null?UserManager.getInstance(GiftWallActivity.this).getCurrentUserId():visitUserId);
							startActivity(intent);
					}
				});
				tv_name.setText("赠送者: " + model.getNickName());
				tv_charm.setText("魅力: +" +model.getCharm());
				tv_time.setText("赠送时间: " +sdf.format(model.getGiveTime()));
				
				PopupWindow popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
				
				popupWindow.setTouchable(true);
				popupWindow.setOutsideTouchable(true);
				popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
				
				//设置popupwindow 显示位置
				// 获取屏幕宽高（方法1）  
//				int screenWidth = getWindowManager().getDefaultDisplay().getWidth(); // 屏幕宽 
				int screenHeight = getWindowManager().getDefaultDisplay().getHeight(); // 屏幕高 
				int[] location = new int[2];  
		        view.getLocationOnScreen(location);// 点击的控件的宽高
		        int popHeight = MoMaUtil.dip2px(GiftWallActivity.this, 90);// popupview的高
//		        int popWidth = popupView.getWidth();// popupview的宽
		        int[] popupLocation = new int[2];
		        view.getLocationInWindow(popupLocation);// 点击的控件的在所在window的位置
		        int paddingbottom = MoMaUtil.dip2px(GiftWallActivity.this, 140);
//		        showMsg(msg)
//				popupWindow.showAsDropDown(view);
		        if((location[1]+popHeight + paddingbottom) > screenHeight) {
		        	popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0], location[1]-popHeight);
		        } else {
		        	popupWindow.showAsDropDown(view);
		        }
				
			}
		});
		
//		gv_giftwall.setMode(Mode.PULL_FROM_END);//底部刷新
//		gv_giftwall.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
//
//			@Override
//			public void onLastItemVisible() {
//				if(hasMore && !isloading){
//					isloading = true;
//					pickupData();
//				 }
//			}
//		});
		
		gv_giftwall.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
					if(hasMore && !isloading){
						isloading = true;
						pickupData();
					 }
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
		
		giftUId = "0";
		pageIndex = 1;
		myGifts.clear();
		pickupData();
	}

	private void pickupData() {
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", userId);
		jo.addProperty("giftUId", giftUId);
		jo.addProperty("pageIndex", pageIndex);
		jo.addProperty("pageNum", PAGENUM);
		jo.addProperty("type", "1000");
		BottleRestClient.post("queryGiveGiftLog", this, jo, new AsyncHttpResponseHandler(){

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					QueryGiftLogBModel baseModel = gson.fromJson(str, QueryGiftLogBModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
						if("0".equals(baseModel.getCode())){
							myGifts.addAll(baseModel.getLogList());
							
							if(myGifts != null && myGifts.size() != 0){
								hasMore = baseModel.getIsMore()==0 ? false:true;
								if(hasMore) {
									pageIndex += 1;
									giftUId = myGifts.get(myGifts.size()-1).getGiftUId();
								}
								giftwallAdapter.reset(myGifts);
								
								Message msg = Message.obtain();
								msg.arg1  =REFRESH;
								handler.sendMessage(msg);
							}
							
							findViewById(R.id.no_message).setVisibility(View.GONE);
						}else if("1000".equals(baseModel.getCode())){
							gv_giftwall.setVisibility(View.VISIBLE);
							findViewById(R.id.no_message).setVisibility(View.VISIBLE);
							((TextView)findViewById(R.id.tv_no_message)).setText(getResources().getString(R.string.tip_no_gift));
								
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
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				showMsg("查询失败");
				closeDialog(mpDialog);
			}
			
			@Override
			public void onStart() {
				super.onStart();
				showDialog("努力加载...", "加载失败", 15*1000);
				gv_giftwall.setVisibility(View.VISIBLE);
				findViewById(R.id.no_message).setVisibility(View.GONE);

			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				closeDialog(mpDialog);
				isloading = false;
			}
			
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_back:
			this.finish();
			break;
		case R.id.tv_nodx:
			// 未答谢的礼物
			Intent intent = new Intent(this, GiftBoxActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			intent.putExtra("userId", userId);
			intent.putExtra("itemIndex", 0);
			startActivity(intent);
			break;
		case R.id.rl_mycharm:
			// 我的送礼记录
			Intent sendgift = new Intent(this, GiftBoxActivity.class);
			sendgift.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			sendgift.putExtra("userId", userId);
			sendgift.putExtra("itemIndex", 1);
			startActivity(sendgift);
			break;
		}
	}

}
