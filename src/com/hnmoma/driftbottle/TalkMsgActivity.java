package com.hnmoma.driftbottle;

import org.apache.http.Header;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hnmoma.driftbottle.adapter.TalkMsgAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.model.MsgModel;
import com.hnmoma.driftbottle.model.SpaceMsgModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 动态页面
 *
 */
public class TalkMsgActivity extends BaseActivity {
	private final int FRESH  =2; //刷新ListView
	private final int MORE = 1; //加载更多
	private final int FINISH = 0; //没有更多了
	
	private  final int  PAGENUM = 15; //每页的条数
	private boolean isLastItem;
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	String userId;
	String msgId;
	
	int itemPosition;
	
	PullToRefreshListView list;
	TalkMsgAdapter adapter;
	
	private View noMsgView;//没有消息时，显示的控件
	private TextView tvNoMsg;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.arg1) {
			case FRESH:
				adapter.notifyDataSetChanged();
				if(adapter.getCount()==0){
					list.setVisibility(View.GONE);
					noMsgView.setVisibility(View.VISIBLE);
				}else{
					list.setVisibility(View.VISIBLE);
					noMsgView.setVisibility(View.GONE);
				}
				break;
			case  MORE:
				list.getLoadingLayoutProxy().setRefreshingLabel("正在加载");
				list.getLoadingLayoutProxy().setPullLabel("上拉加载更多");
				list.getLoadingLayoutProxy().setReleaseLabel("释放开始加载");
				pickUpData(msgId, userId, true);
				break;
			case FINISH:
				showMsg("没有更多");
				list.getLoadingLayoutProxy().setPullLabel("没有更多");
				list.onRefreshComplete();
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
		
		userId = UserManager.getInstance(this).getCurrentUserId();
		msgId = "0";
		
		setContentView(R.layout.activity_talkmsg);
		noMsgView = findViewById(R.id.no_message);
		tvNoMsg =(TextView) noMsgView.findViewById(R.id.tv_no_message);
		tvNoMsg.setText(getResources().getString(R.string.tip_no_new)); 
		list = (PullToRefreshListView) findViewById(R.id.list);
		adapter = new TalkMsgAdapter(this, imageLoader, options);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> av, View view, int position, long id) {
				itemPosition = position -1;
				MsgModel msg = adapter.getItem(itemPosition);
//				showMsg(msg.getReContent());
				Intent reply = new Intent(TalkMsgActivity.this, BottleDetailActivity.class);
				reply.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				reply.putExtra("bottleId", msg.getBottleId());
				reply.putExtra("toUserId", "");
				reply.putExtra("reId", "");
				startActivityForResult(reply, 500);
			}
		});
		
		list.getRefreshableView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> av, View view, final int position, long id) {
				
				String[] strarray  = new String[] {"删除消息", "取消"};
				new AlertDialog.Builder(TalkMsgActivity.this)
								.setTitle("选择")
								.setItems(strarray, new DialogInterface.OnClickListener() {
									     @Override
									     public void onClick(DialogInterface dialog, int which) {
									    	 if(which == 0){
									    		dialog.dismiss();
									    		deleteMsg(adapter.getItem(position-1), true);
									    	 }else if(which == 1){
									    		dialog.dismiss();
									    	 }
									     }
								 })
								.show();
				return true;
			}
		});
		
		list.setMode(Mode.PULL_FROM_END);//底部刷新

		// Add an end-of-list listener
		list.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {
				//加载更多
				Message msg = Message.obtain();
				if(isLastItem){
					msg.arg1 = FINISH;
				}else{
					msg.arg1 = MORE;
				}
				mHandler.sendMessage(msg);
			}
		});
		
		pickUpData(msgId,userId,false);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == 500) {
			if(resultCode == RESULT_CANCELED)  {
				boolean isDelete = data.getBooleanExtra("DeleteDone", false);
				if(isDelete) {
					deleteMsg(adapter.getItem(itemPosition), false);
				}
			}
		}
	}
	
	protected void deleteMsg(final MsgModel item, final boolean isPrompt) {
		JsonObject jo = new JsonObject();
		jo.addProperty("msgId", item.getMsgId());
		BottleRestClient.post("delBottleMsg", this, jo, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				showDialog("正在删除....", "删除失败", 15*1000);
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				
				if (!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					BaseModel model = gson.fromJson(str, BaseModel.class);
					
					if (model != null && !TextUtils.isEmpty(model.getCode())) {
						if ("0".equals(model.getCode())) {
							if(isPrompt) {
								showMsg("删除成功");
							} 
							adapter.remove(item);
							
							Message msg = Message.obtain();
							msg.arg1 = FRESH;
							mHandler.sendMessage(msg);
						} else {
							showMsg(model.getMsg());
						}
					} else {
						showMsg("服务器繁忙");
					}
				} else {
					showMsg("服务器繁忙");
				}
			}
			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				super.onFinish();
				closeDialog(mpDialog);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				closeDialog(mpDialog);
			}
		});
	}
	String tip = "";
	/**
	 * 
	 * @param id 消息id
	 * @param userId 用户id
	 * @param isLoadMore 是否加载更多
	 */
	private void pickUpData(String id,String userId,final boolean isLoadMore){
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", userId);
		jo.addProperty("msgId", msgId);
		jo.addProperty("pageNum", PAGENUM);
		BottleRestClient.post("queryBottleMoving", this, jo, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				super.onStart();
				if(isLoadMore )
					showDialog("努力加载...", "加载失败...", 15*1000);
			}
	
			@Override
			public void onFinish() {
				super.onFinish();
				if(isLoadMore )
					closeDialog(mpDialog);
			}
	
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				
				if (!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					SpaceMsgModel model = gson.fromJson(str, SpaceMsgModel.class);
					
					if (model != null && !TextUtils.isEmpty(model.getCode())) {
						if ("0".equals(model.getCode())) {
							if(!isLoadMore) {
								adapter.reset(model.getMsgList());
							} else {
								if(model.getMsgList() != null&& model.getMsgList().size() >0)
									adapter.addItemLast(model.getMsgList());
							}
							msgId = model.getMsgList().get(model.getMsgList().size()-1).getMsgId();
							if(model.getIsMore() == 1) {
								isLastItem = false;
							}else {
								isLastItem = true;
							}
							Message msg = Message.obtain();
							msg.arg1 = FRESH;
							mHandler.sendMessage(msg);
						} else {
							Message msg = Message.obtain();
							msg.arg1 = FRESH;
							mHandler.sendMessage(msg);
						}
					} else {
						showMsg("服务器繁忙");
						setResult(RESULT_CANCELED);
						Message msg = Message.obtain();
						msg.arg1 = FRESH;
						mHandler.sendMessage(msg);
					}
				} else {
					showMsg("服务器繁忙");
					setResult(RESULT_CANCELED);
					Message msg = Message.obtain();
					msg.arg1 = FRESH;
					mHandler.sendMessage(msg);
				}
			}
	
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				super.onFinish();
				closeDialog(mpDialog);
			}
		});
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_back:
				this.finish();
				break;
//			case R.id.bt_new	:
				// 刷新
//				msgId = "0";
//				refresh = true;
//				pickUpData();
//				break;
		}
	}
}
