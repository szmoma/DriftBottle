package com.hnmoma.driftbottle.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.MyBottleActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.SysChatActivity;
import com.hnmoma.driftbottle.adapter.MyBottleAdapter;
import com.hnmoma.driftbottle.business.ChatManager;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.Bottle;
import com.hnmoma.driftbottle.model.BottleDao;
import com.hnmoma.driftbottle.model.DaoSession;
import com.hnmoma.driftbottle.model.DeleteBottlesBean;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.dao.query.QueryBuilder;


public class MyBottlePageItem extends BaseFragment implements OnClickListener{
	
	ListView lv;
	MyBottleAdapter adapter;
	
	LinearLayout ll_mbmg;
	Button bt_cancel;
	Button bt_del;
	
	boolean isloading;
	boolean hasMore = true;
	static final int PAGENUM = 20; 
	int pageIndex = 0;
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	Handler myHandler = new Handler();
	
	public static boolean [] showMng = new boolean[]{false, false};
	
	int index;
	
	public static MyBottlePageItem newInstance(int index) {
		MyBottlePageItem mbpi = new MyBottlePageItem();
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
//		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.bitmapConfig(Bitmap.Config.RGB_565)
//		.displayer(new RoundedBitmapDisplayer(2))
		.build();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View contextView = inflater.inflate(R.layout.fragment_mybottlepi, container, false);
		
		ll_mbmg = (LinearLayout)contextView.findViewById(R.id.ll_mbmg);
		bt_cancel = (Button)contextView.findViewById(R.id.bt_cancel);
		bt_del = (Button)contextView.findViewById(R.id.bt_del);
		bt_cancel.setOnClickListener(this);
		bt_del.setOnClickListener(this);
		
		
		lv = (ListView) contextView.findViewById(R.id.my_bottle_list);
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				if(showMng[index]){
					CheckBox cb = (CheckBox) view.findViewById(R.id.cb_check);
					cb.toggle();
					adapter.getItem(position).setMngIsChecked(cb.isChecked());
					adapter.notifyDataSetChanged();
				}else{
					Intent intent = new Intent(act, SysChatActivity.class);
					Bottle bottle = adapter.getItem(position);
					
					intent.putExtra("bottleModel", bottle);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivityForResult(intent, 100);
					
//					if(bottle.getMsgCount()!=0){
//						new Thread(new Runnable(){
//							public void run() {
//									DaoSession daoSession = MyApplication.getApp().getDaoSession();
//									bottle.setMsgCount(0);
//									daoSession.update(bottle);
//									
//									myHandler.postDelayed(new Runnable(){
//										public void run() {
//											adapter.notifyDataSetChanged();
//											MyApplication.getApp().getNotificationManager().cancel(0);
//										}
//									}, 500);
//							}
//						}).start();
//					}
				}
			}
		});
		
		lv.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
					if(hasMore && !isloading){
						isloading = true;
						pickDatas();
					 }
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
		
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id) {
				if(adapter!=null && adapter.getCount()!=0){
					showMng(position);
				}
				return true;
			}
		});
		
		return contextView;
	}
	
	public void cancelMng(){
		//ViewPager不可滚动
		MyBottleActivity ma = (MyBottleActivity) act;
		ma.pager.setScrollable(true);
//		ma.bottle_friend.setClickable(true);
//		ma.bottle_collect.setClickable(true);
		ll_mbmg.setVisibility(View.GONE);
		showMng[index] = false;
		adapter.notifyDataSetChanged();
	}
	
	public  void showMng(int position){
		//ViewPager不可滚动
		MyBottleActivity ma = (MyBottleActivity) act;
		ma.pager.setScrollable(false);
//		ma.bottle_friend.setClickable(false);
//		ma.bottle_collect.setClickable(false);
		
		ll_mbmg.setVisibility(View.VISIBLE);
		adapter.resetCheck(position);
		showMng[index] = true;
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//新消息过来不改变编辑模式
		if(showMng[index]){
			showMng(-1);
		}
		
		updateItem();
	}
	
	int totalPage;
	private int getTotalPage(){
		DaoSession daoSession = MyApplication.getApp().getDaoSession();
		BottleDao bottleDao = daoSession.getBottleDao();
		QueryBuilder<Bottle> qb = bottleDao.queryBuilder();
		if(UserManager.getInstance(act).getCurrentUser()==null){
			qb.where(BottleDao.Properties.Belongto.eq("0000000000"));
		}else{
			qb.where(BottleDao.Properties.Belongto.eq(UserManager.getInstance(act).getCurrentUserId()));
		}
		long count = qb.count();
		totalPage = (int) Math.ceil((float)count/PAGENUM);
//		System.out.println("totalPage>"+totalPage);
		return totalPage;
	}
	
	public class MyTask extends AsyncTask<Void, List<Bottle>, List<Bottle>> {
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected List<Bottle> doInBackground(Void... params) {
			DaoSession daoSession = MyApplication.getApp().getDaoSession();
			BottleDao bottleDao = daoSession.getBottleDao();
			
			QueryBuilder<Bottle> qb = bottleDao.queryBuilder();
			if(UserManager.getInstance(act).getCurrentUser()==null){
				qb.where(BottleDao.Properties.Belongto.eq("0000000000"));
			}else{
				qb.where(BottleDao.Properties.Belongto.eq(UserManager.getInstance(act).getCurrentUserId()));
			}
			
			if (index == 0) {
				qb.where(BottleDao.Properties.BottleSort.eq("3001"));
			}else{
				qb.where(BottleDao.Properties.BottleSort.eq("3000"));
			}
			
			qb.orderDesc(BottleDao.Properties.CreateTime);
			qb.limit(PAGENUM).offset(PAGENUM * pageIndex);
			
			return qb.list();
		}

		@Override
		protected void onPostExecute(List<Bottle> list) {
			super.onPostExecute(list);
			isloading = false;
			
			if(list != null && list.size() != 0){
				pageIndex ++ ;
				adapter.addItemLast(list);
				adapter.notifyDataSetChanged();
			}
			
			hasMore = pageIndex < totalPage;
		}
	}
	
	private void pickDatas(){
		MyTask myTask = new MyTask();
		myTask.execute();
	}
	

	@Override
	public boolean onBackPressed() {
		return false;
	}
	
	//删除瓶子对话
	public void deleteBottles(List<String> ubidList){
		if(UserManager.getInstance(act).getCurrentUser()==null){
			return;
		}
		
		DeleteBottlesBean dbb = new DeleteBottlesBean();
		dbb.setUserId(UserManager.getInstance(act).getCurrentUserId());
		dbb.setUbIdList(ubidList);
		String str = dbb.getJsonString();
		if(!TextUtils.isEmpty(str)){
			BottleRestClient.post("deleteChatList", act, str, new AsyncHttpResponseHandler(){
				@Override
				public void onStart() {
					super.onStart();
				}
				
				@Override
				public void onFinish() {
					super.onFinish();
				}

				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				}
	        });
		}
	}

	public void updateItem(){
		pageIndex = 0;
		isloading = false;
		hasMore = true;
		adapter = null;
		adapter = new MyBottleAdapter(imageLoader, options, index);
		lv.setAdapter(adapter);
		
		//获取总页数
		getTotalPage();
		if(hasMore && !isloading){
			isloading = true;
			pickDatas();
		 }
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_cancel:
			cancelMng();
			break;
		case R.id.bt_del:
			showDelDialog();
			break;
		}
	}
	
	private void showDelDialog() {
		new AlertDialog.Builder(act)
			.setTitle("温馨提示")
			.setMessage("瓶子删除后将不会收到后续的回应")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() { 
                    public void onClick(DialogInterface dialog, int which) { 
                    	
                    	List<Bottle> dBottles = adapter.delCheck();
                    	List<String> ubidList = new ArrayList<String>();
                    	
            			if(dBottles != null && dBottles.size() != 0){
            				DaoSession daoSession = MyApplication.getApp().getDaoSession();
            				for(Bottle bottle : dBottles){
            					daoSession.delete(bottle);
            					ubidList.add(bottle.getUbId());
            				}
            				
            				//服务器批量删除
            				deleteBottles(ubidList);
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
	
	/**
	 * 消息内容变了，同步变化
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 100){//未登录状态下进入个人中心
			if(resultCode == Activity.RESULT_OK){
				updateItem();
			}
		}
	}
	
	@Override
	public void onResume() {
    	super.onResume();
    	//挂起了，并且有新数据
    	if(MyApplication.isPending){
    		if(ChatManager.getInstance(act).hasUnReadMsg()){
    			if (index == 0) {
    				updateItem();
    			}
    		}
    	}
	}
}