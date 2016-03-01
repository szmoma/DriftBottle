package com.hnmoma.driftbottle;


import org.apache.http.Header;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hnmoma.driftbottle.adapter.VisitorAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.VisistorModel;
import com.hnmoma.driftbottle.model.VisitorListModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class VisitorActivity extends BaseActivity {
	private final int MORE = 1;	//加载更多的标志
	private final int FINISH = 0; //没有更多的内容标志
	private final int REFRESH = 2; //刷新adapter
	
	
	String userId; //如果是自己查看自己的访客，则当前登录ID与userID
	String recordId; //记录最后一个访问对象的ID
	PullToRefreshListView mPullRefreshListView;
	VisitorAdapter visitorAdapter;
	private boolean isLast; //是否是最后一项
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	private TextView tvVip;
	private ImageView ivVip;
	
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
		initView();
		pickData(recordId,false);
	}
	
	private void initView() {
		setContentView(R.layout.activity_visitor);
		userId = getIntent().getStringExtra("userId");
		tvVip = (TextView) findViewById(R.id.tv_vip);
		ivVip = (ImageView) findViewById(R.id.iv_vip);
		
		if(UserManager.getInstance(this).getCurrentUserId().equals(userId)){
			findViewById(R.id.rl_bottom).setVisibility(View.VISIBLE);
			Integer flag = UserManager.getInstance(this).getCurrentUser().getIsVIP();
			int sum = MyApplication.getApp().getSpUtil().getVisitorNumLimit();
			if(flag!=null&&flag.intValue()==1){
				ivVip.setImageDrawable(getResources().getDrawable(R.drawable.ic_vip));
				if(sum==Integer.MAX_VALUE){
					tvVip.setText("会员访客容量:无限制");
				}else{
					tvVip.setText("会员访客容量:"+sum);
				}
				
			}else{
				ivVip.setImageDrawable(getResources().getDrawable(R.drawable.ic_vip_not));
				tvVip.setText("普通访客容量:"+sum);
			}
		}else{
			findViewById(R.id.rl_bottom).setVisibility(View.GONE);
		}
		
//		tv_today = (TextView) findViewById(R.id.tv_today);
//		tv_total = (TextView) findViewById(R.id.tv_total);
		mPullRefreshListView =  (PullToRefreshListView) findViewById(R.id.lv_allvisitor);
		
		visitorAdapter = new VisitorAdapter(this,imageLoader, options);
		ListView actualListView = mPullRefreshListView.getRefreshableView();
//		actualListView.setLayoutAnimation(getAnimationController());
		registerForContextMenu(actualListView);
		actualListView.setAdapter(visitorAdapter);
		
		recordId = "0";
		isLast = false; //默认不是最后一项
		
		mPullRefreshListView.setMode(Mode.PULL_FROM_END);//加载更多
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
					msg.arg1 = FINISH;
				}else{
					msg.arg1 = MORE;
				}
				handler.sendMessage(msg);
			}
		});
		
		
		
		
		mPullRefreshListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> av, View view, int position, long id) {
				// TODO 点击item
				VisitorListModel model = (VisitorListModel) av.getItemAtPosition(position);
				String currentUserId = UserManager.getInstance(VisitorActivity.this).getCurrentUserId();
				
				if(model.getUserId().equals(currentUserId)){
					Intent intent = new Intent(VisitorActivity.this, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("userId", currentUserId);
					intent.putExtra("identityflag", 0);
					intent.putExtra("visitUserId", currentUserId);	//访问ID
					startActivity(intent);
				}else{
					Intent intent = new Intent(VisitorActivity.this, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("identityflag", 1);
					intent.putExtra("userId", model.getUserId());	//被访问者的ID
//					intent.putExtra("visitUserId", userId);
					intent.putExtra("visitUserId", currentUserId);	//访问ID
					startActivity(intent);
					/**bug描述：用户A查看自己的访客，从访客列表查看访客B的空间，从B的访客列表查看c的空间，浏览c的访客。发现访问者最新的记录是B，而不是A。**/
					/**解决bug方式：把visitorUserId的值，是当前登录用户，即currentUserId*/
					/**yangsy于2015-4-24修复次bug。 */
				}
			}
		});
		
		((TextView)findViewById(R.id.tv_vzone_name)).setText(getResources().getString(R.string.visitor));
		
		String visitorSum = getIntent().getStringExtra("visitsNum");
		((TextView)findViewById(R.id.tv_vistor_sum)).setText("总浏览量："+visitorSum);
		findViewById(R.id.tv_vistor_sum).setVisibility(View.GONE);
		
	}
	
	private Handler handler = new Handler()	{
		public void handleMessage(android.os.Message msg) {
			switch (msg.arg1) {
			case FINISH://
				showMsg("没有更多了");
				mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多了");
				mPullRefreshListView.onRefreshComplete();
				break;
			case MORE:	//刷新
				pickData(recordId ,true);
				break;
			case REFRESH:
				visitorAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		};
	};
	/**
	 * 请求数据
	 * @param id
	 * @param isLoadMore 是否加载更多
	 */
	private void pickData(String id,final boolean isLoadMore) {
		JsonObject jo = new JsonObject();
		jo.addProperty("id", id);
		jo.addProperty("userId", userId);
		jo.addProperty("pageNum", "10");
		
		BottleRestClient.post("queryVisitor", this, jo, new AsyncHttpResponseHandler() {

			@Override
			public void onStart() {
				super.onStart();
				if(!isLoadMore)
					showDialog("努力加载...","加载失败...",15*1000);
				mPullRefreshListView.setVisibility(View.VISIBLE);
				findViewById(R.id.no_message).setVisibility(View.GONE);
			}

			@Override
			public void onFinish() {
				super.onFinish();
				if(!isLoadMore)
					closeDialog(mpDialog);
				else
					mPullRefreshListView.onRefreshComplete();
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,byte[] responseBody) {
				// TODO Auto-generated method stub
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					VisistorModel visitorModel = gson.fromJson(str, VisistorModel.class);
					
					if("0".equals(visitorModel.getCode())) {
						if(isLoadMore)
							visitorAdapter.addItemLast(visitorModel.getVisitorList());
						else 
							visitorAdapter.reset(visitorModel.getVisitorList());
						
						recordId = visitorModel.getVisitorList().get(visitorModel.getVisitorList().size()-1).getId();
						if("1".equals(visitorModel.getIsMore())) {
							isLast = false;
						} else {
							isLast = true;
							if(isLoadMore)
								mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多了");
						}
						Message msg = Message.obtain();
						msg.arg1 = REFRESH;
						handler.sendMessage(msg);
						if(isLoadMore)
							mPullRefreshListView.onRefreshComplete();
					}else if ("1000".equals(visitorModel.getCode())){
						isLast = false;
						if(isLoadMore){
							mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多了");
							mPullRefreshListView.onRefreshComplete();
						}
						if(!isLoadMore){
							//显示提示
							findViewById(R.id.content).setVisibility(View.GONE);
							findViewById(R.id.no_message).setVisibility(View.VISIBLE);
							TextView tvTip = (TextView)findViewById(R.id.tv_no_message);
							tvTip.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.default_empty_visitor), null, null);
							tvTip.setText(R.string.tip_no_visitor);
						}
					}else{
						showMsg(visitorModel.getMsg());
						isLast = false;
						if(isLoadMore){
							mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多了");
							mPullRefreshListView.onRefreshComplete();
						}
						
						if(!isLoadMore){
							//显示提示
							findViewById(R.id.content).setVisibility(View.GONE);
							findViewById(R.id.no_message).setVisibility(View.VISIBLE);
							TextView tvTip = (TextView)findViewById(R.id.tv_no_message);
							tvTip.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.default_empty_visitor), null, null);
							tvTip.setText(R.string.tip_no_visitor);
						}
				}
			}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				// TODO Auto-generated method stub
				showMsg("加载失败");
				if(isLoadMore)
					mPullRefreshListView.onRefreshComplete();
				else
					closeDialog(mpDialog);
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
	
	/**
	 * Layout动画
	 * 
	 * @return
	 */
	protected LayoutAnimationController getAnimationController() {
		int duration=300;
		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(duration);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				-1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(duration);
		set.addAnimation(animation);

		LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
		controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
		return controller;
	}

	
}
