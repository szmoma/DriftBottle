package com.hnmoma.driftbottle;

import org.apache.http.Header;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hnmoma.driftbottle.adapter.TalkAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.model.MyBottleModel;
import com.hnmoma.driftbottle.model.TalkModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 说说列表/我的瓶子
 * @author Administrator
 *
 */
public class TalkActivity extends BaseActivity {
	private final int MORE = 1;	//加载更多的标志
	private final int FINISH = 0; //没有更多的内容标志
	private final int FRESH =2;//刷新listview
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	String userId;
	int identityflag;// 0 表示自己的 1 表示别人的
	String bId; //当bottleId为0，则获取最新的说说数据
	
	EditText et_comments;
	
	ImageView btn_write_talk;
	
	private PullToRefreshListView mPullRefreshListView;
	private TalkAdapter talkAdapter;
	private boolean isLast; //说说的条数，是否是最后一条
	
	ImageView newMsg;
	int position = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_talk);
		identityflag = getIntent().getIntExtra("identityflag", 1);
		userId = getIntent().getStringExtra("userId");
		isLast = false;
		bId = "0";
		setupView();
		pickUpData(bId,false);
		init();
		
		// 注册一个cmd消息的BroadcastReceiver
		IntentFilter cmdIntentFilter = new IntentFilter(EMChatManager.getInstance().getCmdMessageBroadcastAction());
		cmdIntentFilter.setPriority(6);
		registerReceiver(cmdMessageReceiver, cmdIntentFilter);
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			unregisterReceiver(cmdMessageReceiver);
			cmdMessageReceiver = null;
			closeDialog(mpDialog);
		} catch (Exception e) {
		}
	}
	

	private void init() {
		// TODO Auto-generated method stub
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)
		.showImageForEmptyUri(R.drawable.defalutimg)
		.showImageOnFail(R.drawable.defalutimg)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
		
		//标题
		((TextView)findViewById(R.id.tv_title)).setText(getResources().getString(R.string.mybottle));
		
		Drawable noBottle = getResources().getDrawable(R.drawable.talk_no_bottle);
		noBottle.setBounds(0, 0, noBottle.getMinimumWidth(), noBottle.getMinimumHeight());
		((TextView)findViewById(R.id.tv_no_message)).setCompoundDrawables(null, noBottle, null, null);
		((TextView)findViewById(R.id.tv_no_message)).setText(getResources().getString(R.string.tip_no_Talks));
		
		registerForContextMenu(mPullRefreshListView);
		talkAdapter = new TalkAdapter(this, imageLoader, identityflag);
		mPullRefreshListView.setAdapter(talkAdapter);
		
		mPullRefreshListView.setMode(Mode.PULL_FROM_END);//底部刷新
		
		if(identityflag == 1) {
			btn_write_talk.setVisibility(View.GONE);
		} else {
			btn_write_talk.setVisibility(View.VISIBLE);
		}
		updateUnreadHead(MyApplication.getApp().getSpUtil().getMsgRecord());
	}

	private void setupView() {
		// TODO Auto-generated method stub
		btn_write_talk = (ImageView) findViewById(R.id.btn_ok);
		et_comments = (EditText) findViewById(R.id.et_comments);
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_talk);
		newMsg = (ImageView) findViewById(R.id.main_kj_new);
		
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
				mHandler.sendMessage(msg);
			}
		});
	}
	
	private Handler mHandler = new Handler()	{
		public void handleMessage(android.os.Message msg) {
			switch (msg.arg1) {
			case FINISH://
				showMsg("没有更多");
				mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多");
				mPullRefreshListView.onRefreshComplete();
				break;
			case MORE:	//加载更多
				mPullRefreshListView.getLoadingLayoutProxy().setRefreshingLabel("正在加载");
				mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("上拉加载更多");
				mPullRefreshListView.getLoadingLayoutProxy().setReleaseLabel("释放开始加载");
				pickUpData(bId,true);
				break;
			case FRESH:
				talkAdapter.notifyDataSetChanged();
				if(talkAdapter.getCount()==0){
					mPullRefreshListView.setVisibility(View.GONE);
					findViewById(R.id.no_message).setVisibility(View.VISIBLE);
				}else{
					mPullRefreshListView.setVisibility(View.VISIBLE);
					findViewById(R.id.no_message).setVisibility(View.GONE);
				}
				break;
			default:
				break;
			}
		};
	};
	
	String tip = "";
	/**
	 * 从服务器刷新数据
	 * @param id 最后一个瓶子的id
	 * @param isLoadMore 是否记载更多
	 */
	private void pickUpData(final String id,final boolean isLoadMore){
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", userId);
		jo.addProperty("bId", id);
//		jo.addProperty("isSelf", identityflag);
		jo.addProperty("pageNum", 10);
		
		BottleRestClient.post("queryMyBottle", this, jo, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				super.onStart();
				if(!isLoadMore)
					showDialog("努力加载...", "加载失败...", 15*1000);
			}
	
			@Override
			public void onFinish() {
				super.onFinish();
				if(!isLoadMore)
					closeDialog(mpDialog);
			}
	
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				
				if (!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					TalkModel model = gson.fromJson(str, TalkModel.class);
					if (model != null && !TextUtils.isEmpty(model.getCode())) {
						if ("0".equals(model.getCode())) {
							if(isLoadMore) {
								talkAdapter.addItemLast(model.getTalkList());
							} else {
								talkAdapter.reset(model.getTalkList());
							}
							
							bId = model.getTalkList().get(model.getTalkList().size()-1).getbId();
							if(model.getIsMore() == 1) {
								isLast = false;
							}else {
								isLast = true;
								mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多了");
								if(!TextUtils.isEmpty(tip))
									showMsg(tip);
							}
							
							if(!isLoadMore){
								mPullRefreshListView.getRefreshableView().setSelection(mPullRefreshListView.getRefreshableView().getFirstVisiblePosition());
							}
							
							} else if("1000".equals(model.getCode())){
							isLast = true;
							if(isLoadMore)
								mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多了");
						}else{
							if(isLoadMore)
								mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多了");
							isLast = true;
							showMsg(model.getMsg());
						}
						mPullRefreshListView.onRefreshComplete();
						Message msg = Message.obtain();
						msg.arg1 = FRESH;
						mHandler.sendMessage(msg);
					} else {
						showMsg("服务器忙");
						setResult(RESULT_CANCELED);
						Message msg = Message.obtain();
						msg.arg1 = FRESH;
						mHandler.sendMessage(msg);
					}
				} else {
					showMsg("服务器忙");
					setResult(RESULT_CANCELED);
					Message msg = Message.obtain();
					msg.arg1 = FRESH;
					mHandler.sendMessage(msg);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				if(!isLoadMore)
					closeDialog(mpDialog);
				showMsg("加载失败");
			}
		});
	}
	
	
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.btn_back:
			this.finish();
			break;
		case R.id.btn_ok:
			//写说说
			luanchAnnounceTalk();
			break;
		case R.id.bt_delete:
			final MyBottleModel model1  = talkAdapter.getItem(mPullRefreshListView.getRefreshableView().getPositionForView(v)-1);
			String[] strarray  = new String[] {"删除", "取消"};
			
			new AlertDialog.Builder(this)
				.setTitle("选择")
				.setItems(strarray, new DialogInterface.OnClickListener() {
					     @Override
					     public void onClick(DialogInterface dialog, int which) {
					    	 if(which == 0){
					    		dialog.dismiss();
					    		deleteTalk(model1);
					    	 }else if(which == 1){
					    		dialog.dismiss();
					    	 }
					     }
				    })
				.show();
			
			break;
//		case R.id.btn_support:
//			MyBottleModel model2  = talkAdapter.getItem(mPullRefreshListView.getRefreshableView().getPositionForView(v)-1);
//			if("1000".equals(model2.getUseNum())){
//				showMsg("已经赞过");
//			}else
//				doSupport(model2);
//			break;
		case R.id.tv_number:
		case R.id.iv_head:
		case R.id.rl_01:
		case R.id.ll_all:
			// 点击内容，进入说说详情
			position = mPullRefreshListView.getRefreshableView().getPositionForView(v)-1;
			MyBottleModel model4  = talkAdapter.getItem(position);
			
			Intent reply = new Intent(this, BottleDetailActivity.class);
			reply.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			reply.putExtra("bottleId", model4.getBottleId());
			reply.putExtra("position", position);
			startActivityForResult(reply, 200);
			break;
		case R.id.iv_pic:
			MyBottleModel model5  = talkAdapter.getItem(mPullRefreshListView.getRefreshableView().getPositionForView(v)-1);
			String imageUrl = model5.getUrl();
			if(TextUtils.isEmpty(imageUrl)) {
				showMsg("查看失败");
			} else {
				Intent intent = new Intent(this, SingleImageFrameShowActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("imageUrl", imageUrl);
				startActivity(intent);
			}
			break;
		}
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 100) {
			if(resultCode == Activity.RESULT_OK) {
				// 发表说说后回来,无论ListView是否绑定数据，重新去刷新数据，talkId重置为0
				bId = "0";
				pickUpData(bId,false);
				isLast = false;
			}
		} else if(requestCode == 200) {
				// 进入说说详情后回来
			if(resultCode == Activity.RESULT_OK) {
				if(data==null)	
					return ;
				String talkId = data.getStringExtra("bottleId");
				int position = data.getIntExtra("position", -1);
				if(data.getBooleanExtra("DeleteDone", false)){
					Log.e("debug","tId="+talkId+",tId2="+talkAdapter.getItem(position).getBottleId());
					if(position!=-1&&!TextUtils.isEmpty(talkId)&&talkId.equals(talkAdapter.getItem(position).getBottleId())){
						talkAdapter.remove(talkAdapter.getItem(position));
						bId = "0";
					}
				}else{
					int supportNum = data.getIntExtra("supportNum", -1);
					int commentNum = data.getIntExtra("commentNum", -1);
					if(supportNum==-1&&commentNum==-1)
						return ;
					if(supportNum!=-1){
						talkAdapter.getItem(position).setUseNum(supportNum + talkAdapter.getItem(position).getUseNum());
					}
					if(commentNum!=-1){
						talkAdapter.getItem(position).setReviewNum(commentNum + talkAdapter.getItem(position).getReviewNum());
					}
				}
				Message msg = Message.obtain();
				msg.arg1 = FRESH;
				mHandler.sendMessage(msg);
			}
		}
	}
	
	@Override
	protected void onPause() {
		System.gc();
		super.onPause();
	}
	/**
	 * 删除说说
	 * @param model
	 */
	public void deleteTalk(@NonNull final MyBottleModel model) {
		if(model==null)
			return ;
		String mBId = model.getBottleId();//瓶子的id
		JsonObject jo = new JsonObject();
		jo.addProperty("bottleId", mBId);
		MoMaLog.e("debug","isnull:"+(UserManager.getInstance(this)==null?true:false));
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		
		BottleRestClient.post("delBottle", this, jo, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {
				
				String str = new String(responseBody);
				if (!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					BaseModel baseModel = gson.fromJson(str, BaseModel.class);
					if (baseModel != null && !TextUtils.isEmpty(baseModel.getCode())) {
						if ("0".equals(baseModel.getCode())) {
							if(bId.equals(model.getbId())){
								bId = "0";
							}
							talkAdapter.remove(model);
							Message msg = Message.obtain();
							msg.arg1 = FRESH;
							mHandler.sendMessage(msg);
							showMsg("删除成功");
						} else {
							showMsg(baseModel.getMsg());
						}
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				showMsg("删除失败");
			}
			
		});
	}
	
	public void doSupport(final MyBottleModel model) {
		String talkId = model.getBottleId();
		JsonObject jo = new JsonObject();
		jo.addProperty("talkId", talkId);
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		
		BottleRestClient.post("loveTalk", this, jo, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {
				
				String str = new String(responseBody);
				if (!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					BaseModel baseModel = gson.fromJson(str, BaseModel.class);
					if (baseModel != null && !TextUtils.isEmpty(baseModel.getCode())) {
						if ("0".equals(baseModel.getCode())) {
							showMsg("点赞成功");
//							model.setState("1000");//存储临时值，1000表示已经赞过
							model.setUseNum(model.getUseNum()+1);
							talkAdapter.replace(model);
							Message msg = Message.obtain();
							msg.arg1 = FRESH;
							mHandler.sendMessage(msg);
							
						} else if("100022".equals(baseModel.getCode())){
//							model.setState("1000");//存储临时值，1000表示已经赞过
							talkAdapter.replace(model);
							Message msg = Message.obtain();
							msg.arg1 = FRESH;
							mHandler.sendMessage(msg);
							showMsg(baseModel.getMsg());
						}else {
							showMsg(baseModel.getMsg());
						}
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				showMsg("点赞失败");
			}
			
		});
	}
	
	/**
	 * 启动我的瓶子消息的页面
	 */
	private void luanchAnnounceTalk(){
		updateUnreadHead(false);
		//清除所有新消息的标志
		MyApplication.getApp().getSpUtil().setMsgRecord(false);
		Intent sendMsg = new Intent(this, TalkMsgActivity.class);
		sendMsg.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//		startActivityForResult(sendMsg, 100);
		startActivity(sendMsg);
	}
	
	/**
	 * cmd消息BroadcastReceiver
	 */
	private BroadcastReceiver cmdMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//获取cmd message对象
//			String msgId = intent.getStringExtra("msgid");
			EMMessage message = intent.getParcelableExtra("message");
			//获取消息body
			CmdMessageBody cmdMsgBody = (CmdMessageBody) message.getBody();
			String aciton = cmdMsgBody.action;//获取自定义action
//			//获取扩展属性
//			String attr=message.getStringAttribute("a");
			if(aciton.equals(MyConstants.MESSAGE_ACTION_BOTTLE)) {
				//瓶子动态
				updateUnreadHead(true);
				abortBroadcast();
			}else if(MyConstants.MESSAGE_ACTION_BOTTLE.equals(aciton)){
				updateUnreadHead(true);
				MyApplication.getApp().getSpUtil().setMsgRecord(true);
				abortBroadcast();
			}
		}
	};
	
	/**
	 * 获取空间消息未读数
	 */
	public void updateUnreadHead(boolean isShow) {
		if(isShow) {
			newMsg.setVisibility(View.VISIBLE);
		} else {
			newMsg.setVisibility(View.GONE);
		}
	}
}
