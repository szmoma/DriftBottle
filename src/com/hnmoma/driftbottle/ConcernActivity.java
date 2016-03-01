package com.hnmoma.driftbottle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hnmoma.driftbottle.adapter.ConcernAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.AttentionModel;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.model.ConcernModel;
import com.hnmoma.driftbottle.model.DeleteConcernBean;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 标记界面
 *<br/> yangsy于6月15日修改
 *
 */
public class ConcernActivity extends BaseActivity {

	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	private final int MORE = 1;	//加载更多的标志
	private final int FINISH = 0; //没有更多的内容标志
	private final int FRESH = 2;//刷新ListView
	
	TextView tv_no_message;
	View view_no_message;
	LinearLayout ll_mbmg;
	Button my_bottle_mng;
	PullToRefreshListView list;
	ConcernAdapter adapter;
	
	private static int PAGENUM = 8;
	String attentionId;
	boolean isLastItem; //是否是最后
	
//	List<AttentionModel> user;
	List<AttentionModel> delUsers;
	boolean isMulChoice = false;
	public HashMap<Integer, Boolean> ischeck;
	
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
		
		setContentView(R.layout.activity_concern);
		init();
		pickUpData(attentionId,false);
	}
	
	private void init() {
		view_no_message = findViewById(R.id.no_message);
		tv_no_message = (TextView) findViewById(R.id.tv_no_message);
		ll_mbmg = (LinearLayout) findViewById(R.id.ll_mbmg);
		my_bottle_mng = (Button) findViewById(R.id.my_bottle_mng);
		list = (PullToRefreshListView) findViewById(R.id.list);
		ListView actualListView = list.getRefreshableView();
//		registerForContextMenu(actualListView);
		ischeck = new HashMap<Integer, Boolean>();
		delUsers = new LinkedList<AttentionModel>();
		attentionId = "0";
		
		adapter = new ConcernAdapter(this,imageLoader, options, ischeck);
		actualListView.setAdapter(adapter);
		
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
				if(isMulChoice) {
					changeDelUser(position-1);
					return;
				} else {
					AttentionModel model = adapter.getItem(position - 1);
					Intent intent = new Intent(ConcernActivity.this, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("identityflag", 1);
					intent.putExtra("userId", model.getUserId());	//被访问者的ID
					intent.putExtra("visitUserId", UserManager.getInstance(ConcernActivity.this).getCurrentUserId());	//访问ID
					startActivity(intent);
				}
			}
		});
		//setOnItemLongClickListener
		actualListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				isMulChoice = true;
				adapter.setDelMode(isMulChoice);
				ll_mbmg.setVisibility(View.VISIBLE);
				changeDelUser(arg2-1);
				return true;
			}
		});
		
		list.setMode(Mode.PULL_FROM_END);//加载更逗
		list.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				if(!isLastItem){
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
				showMsg("没有更多了");
				list.getLoadingLayoutProxy().setPullLabel("没有更多了");
				list.onRefreshComplete();
				break;
			case MORE:	//刷新
				pickUpData(attentionId,true);
				break;
			case FRESH:
				adapter.notifyDataSetChanged();
				
				if(adapter.getCount()==0){
					list.setVisibility(View.GONE);
					view_no_message.setVisibility(View.VISIBLE);
					tv_no_message.setText(getResources().getString(R.string.tip_no_concernman));
				}else{
					view_no_message.setVisibility(View.GONE);
					list.setVisibility(View.VISIBLE);
				}
				break;
			default:
				break;
			}
		};
	};
	
	
	/**
	 * 
	 * @param attentionId 最后一个关注着的id
	 * @param isLoadMore 是否加载更多
	 */
	private void pickUpData(String id ,final boolean isLoadMore) {
		JsonObject jo = new JsonObject();
		jo.addProperty("attentionId", id);
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("pageNum", PAGENUM);
		
		BottleRestClient.post("queryAttention", this, jo, new AsyncHttpResponseHandler() {

			@Override
			public void onStart() {
				super.onStart();
				if(isLoadMore)
					showDialog("努力加载...", "记载失败...", 15*1000);
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				if(!isLoadMore)
					list.onRefreshComplete();
				else
					closeDialog(mpDialog);
			}
			
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					ConcernModel concernModel = gson.fromJson(str, ConcernModel.class);
					
					if("0".equals(concernModel.getCode())) {
						adapter.addItemLast(concernModel.getAttentionList());
						attentionId = adapter.getItem(adapter.getCount()-1).getAttentionId();
						if(concernModel.getIsMore() == 1) { //还有更多的时候
							isLastItem = true;
						} else {
							isLastItem = false;
							if(isLoadMore)
							list.getLoadingLayoutProxy().setPullLabel("没有更多了");
						}
						
						refresh();
						if(isLoadMore) {
							list.onRefreshComplete();
						}
					} else if ("1000".equals(concernModel.getCode())){
						isLastItem = false;
						list.getLoadingLayoutProxy().setPullLabel("没有更多了");
						list.onRefreshComplete();
						
						Message msg = Message.obtain();
						msg.arg1 = FRESH;
						mHandler.sendMessage(msg);
					}else{
						showMsg(concernModel.getMsg());
						isLastItem = false;
						if(isLoadMore){
							list.getLoadingLayoutProxy().setPullLabel("没有更多了");
							list.onRefreshComplete();
						}
						Message msg = Message.obtain();
						msg.arg1 = FRESH;
						mHandler.sendMessage(msg);
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				showMsg("加载失败");
				if(!isLastItem)
					list.onRefreshComplete();
			}
			});

		
	}

	private void refresh() {
		for(int i=0; i< adapter.getCount(); i++) {
			ischeck.put(i, false);
		}
		Message msg = Message.obtain();
		msg.arg1 = FRESH;
		mHandler.sendMessage(msg);
	}
	
	// 增删用户
	public void changeDelUser(int position){
		if(position < 0) {
			return;
		}
		if(delUsers.contains(adapter.getItem(position))) {
			delUsers.remove(adapter.getItem(position));
			ischeck.put(position, false);
		} else {
			delUsers.add(adapter.getItem(position));
			ischeck.put(position, true);
		}
				
		Message msg = Message.obtain();
		msg.arg1 = FRESH;
		mHandler.sendMessage(msg);
	}
	
	public void cancelMng(){
//		my_bottle_mng.setBackgroundResource(R.drawable.title_btn_mng);
//		my_bottle_mng.setText("");
		ll_mbmg.setVisibility(View.GONE);
		isMulChoice = false;
		adapter.setDelMode(isMulChoice);
//		ischeck.clear();
		delUsers.clear();
		refresh();
	}
	
	private void showDelDialog(final boolean delall) {
		String content;
		if(delall) {
			content = "确定要清空标记的人";
		} else {
			content = "确定要删除标记的人";
		}
		new AlertDialog.Builder(this)
			.setTitle("温馨提示")
			.setMessage(content)
			.setPositiveButton("确定", new DialogInterface.OnClickListener() { 
                    public void onClick(DialogInterface dialog, int which) { 
                    	
                    	if(delall) {
                    		adapter.clear();
                    		deleteSelectUser("1001");
                    	} else {
                    		if(delUsers.size() == adapter.getCount()) {//TODO
                    			adapter.clear();
                        		deleteSelectUser("1001");
                    		} else {
                    			for(AttentionModel model : delUsers) {
                					adapter.remove(model);
                				}
                				attentionId = adapter.getItem(adapter.getCount()-1).getAttentionId();
                				deleteSelectUser("1000");
                    		}
            			}
        				cancelMng();
                    } 
                })
             .setNegativeButton("取消", new DialogInterface.OnClickListener() { 
                    public void onClick(DialogInterface dialog, int which) { 
                    } 
             })
			.show();
	}

	protected void deleteSelectUser(String type) {// 1000 删除不定条数； 1001 删除全部
		if("1000".equals(type) && (delUsers == null || delUsers.size() == 0)) {
			return;
		}
		
		DeleteConcernBean dcb = new DeleteConcernBean();
		List<String>  attentionIdList =new LinkedList<String>();
		if("1000".equals(type)) {
			for(int i=0; i < delUsers.size(); i++) {
				attentionIdList.add(delUsers.get(i).getAttentionId());
			}
		}
		dcb.setUserId(UserManager.getInstance(this).getCurrentUserId());
		dcb.setDelType(type);
		dcb.setAttentionIdList(attentionIdList);
		
		BottleRestClient.post("delAttention", this, dcb.getJsonString(), new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {
				String res = new String(responseBody);
				if(!TextUtils.isEmpty(res)) {
					Gson gson = new Gson();
					BaseModel model = gson.fromJson(res, BaseModel.class);
					
					if("0".equals(model.getCode())) {
						showMsg("删除成功");
						
						Message msg = Message.obtain();
						msg.arg1 = FRESH;
						mHandler.sendMessage(msg);
					} else {
						showMsg(model.getMsg());
					}
				}
//						user.addAll(concernModel.getAttentionList());
				
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				showMsg("删除失败");
			}
			
		});

		
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.my_bottle_back:
			this.finish();
			break;
		
		case R.id.my_bottle_mng:
			//多选删除
			showDelDialog(true);
			break;
		case R.id.btn_cancel:
			// 取消删除
			cancelMng();
			break;
		case R.id.btn_del:
			// 删除
			showDelDialog(false);
			break;
		
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(isMulChoice){
			cancelMng();
		}else{
			super.onBackPressed();
		}
		
	}
	
	
}
