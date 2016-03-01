package com.hnmoma.driftbottle;

import java.util.List;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.adapter.PhbHeadAdapter;
import com.hnmoma.driftbottle.adapter.PhbItemAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.TopUserModel;
import com.hnmoma.driftbottle.model.UserTopBModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class HotTopItemActivity extends BaseActivity implements OnClickListener {
	private final int FRESH = 1; //刷新整个页面
	private final int FRESH2 = 2;//刷新局部
	TextView tv_name;
	ListView mylv;
	PhbItemAdapter adapter;

	boolean isloading;
	boolean hasMore = true;

	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;

	int type;// 1：魅力值 2：成绩 3：财富值
	int index;// 0：周榜   1：总榜
	int position;

	PhbHeadAdapter phbHeadAdapter;
	GridView gv_phbhead;

	String visitUserId;

	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FRESH:
				phbHeadAdapter.notifyDataSetChanged();
				adapter.notifyDataSetChanged();
				break;
			case FRESH2:
				phbHeadAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("item", index);
		outState.putSerializable("position", position);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			index = savedInstanceState.getInt("item");
			position = savedInstanceState.getInt("position");
		}

		index = getIntent().getIntExtra("item", 0);
		position = getIntent().getIntExtra("position", 0);

		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.defalutimg)
				.showImageForEmptyUri(R.drawable.defalutimg)
				.showImageOnFail(R.drawable.defalutimg).cacheInMemory(true)
				.cacheOnDisc(true).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();

		init();

	}

	private void init() {
		setContentView(R.layout.activity_hottopitem);
//		View contextView = this.getLayoutInflater().inflate(
//				R.layout.activity_hottopitem, null);
		tv_name = (TextView)findViewById(R.id.tv_name);

		mylv = (ListView) findViewById(R.id.mylv);
		View head = this.getLayoutInflater().inflate(R.layout.phbhead, null);
		gv_phbhead = (GridView) head.findViewById(R.id.gv_user);

		gv_phbhead.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View view, int position,
					long id) {
				if (UserManager.getInstance(HotTopItemActivity.this).getCurrentUser() == null) {
					Intent intent = new Intent(HotTopItemActivity.this, LoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivityForResult(intent, 400);
				} else {
					TopUserModel tum = (TopUserModel) av.getItemAtPosition(position);

					if (tum.getUserId().equals(visitUserId)) {
						Intent intent = new Intent(HotTopItemActivity.this, VzoneActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("identityflag", 0);
						intent.putExtra("visitUserId", visitUserId);
						intent.putExtra("userId", visitUserId);
						startActivity(intent);
					} else {
						Intent intent = new Intent(HotTopItemActivity.this, VzoneActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("identityflag", 1);
						intent.putExtra("userId", tum.getUserId());
						intent.putExtra("visitUserId", visitUserId);
						startActivity(intent);
					}
				}
			}
		});

		setActivityTitle();
		phbHeadAdapter = new PhbHeadAdapter(this, imageLoader, index, type);
		gv_phbhead.setAdapter(phbHeadAdapter);

		mylv.addHeaderView(head, null, false);
		mylv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View view, int position,
					long id) {
				if (UserManager.getInstance(HotTopItemActivity.this)
						.getCurrentUser() == null) {
					Intent intent = new Intent(HotTopItemActivity.this,
							LoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivityForResult(intent, 400);
				} else {
					TopUserModel tum = (TopUserModel) av.getItemAtPosition(position);

					if (tum.getUserId().equals(visitUserId)) {
						Intent intent = new Intent(HotTopItemActivity.this, VzoneActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("userId", visitUserId);
						intent.putExtra("identityflag", 0);
						intent.putExtra("visitUserId", visitUserId);
						startActivity(intent);
					} else {
						Intent intent = new Intent(HotTopItemActivity.this, VzoneActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("identityflag", 1);
						intent.putExtra("userId", tum.getUserId());
						intent.putExtra("visitUserId", visitUserId);
						startActivity(intent);
					}
				}
			}
		});

		if (UserManager.getInstance(this).getCurrentUser() != null) {
			visitUserId = UserManager.getInstance(this).getCurrentUserId();
		}

		adapter = new PhbItemAdapter(imageLoader, options, index, type);
		mylv.setAdapter(adapter);

		String str = MyApplication.getApp().getSpUtil().getPhbCache(index,position);
		if (TextUtils.isEmpty(str)) {
			MoMaLog.e("没有排行缓存重新请求", "没有排行缓存重新请求");
			pickDatas();
		} else {
			Gson gson = new Gson();
			UserTopBModel baseModel = gson.fromJson(str, UserTopBModel.class);
			if (baseModel != null && baseModel.getUserList() != null
					&& baseModel.getUserList().size() != 0) {
				List<TopUserModel> userList = baseModel.getUserList();
				Message msg = Message.obtain();
				if(userList.size() > 6) {
					phbHeadAdapter.addItemLast(userList.subList(0, 6));
					adapter.addItemLast(userList.subList(6, userList.size()));
					msg.what = FRESH;
				} else {
					phbHeadAdapter.addItemLast(userList);
					msg.what = FRESH2;
				}
				mHandler.sendMessage(msg);
				MoMaLog.e("有排行缓存重用", "有排行缓存重用");
			} else {
				MoMaLog.e("排行缓存损坏，重新请求", "排行缓存损坏，重新请求");
				pickDatas();
			}
		}

	}

	private void setActivityTitle() {
		StringBuilder sb = new StringBuilder();// "鲜肉榜", "魅力榜", "财富榜", "拳王榜"
		if (position == 0) {
			sb.append("鲜肉榜");
			type = 0;// 1：魅力值 2：成绩 3：财富值
		} else if (position == 1) {
			sb.append("魅力榜");
			type = 1;
		} else if (position == 2) {
			sb.append("财富榜");
			type = 3;
		} else if (position == 3) {
			sb.append("拳王榜");
			type = 2;
		}
//		if (index == 0) {
//			sb.append("/周榜");
//		} else {
//			sb.append("/总榜");
//		}
		tv_name.setText(sb);
	}

	private void pickDatas() {
		JsonObject jo2 = new JsonObject();
		jo2.addProperty("topNum", "20");
//		if (index == 1) {
		jo2.addProperty("qtype", "hot1000");
//		} else {
//			jo2.addProperty("qtype", "hot1001");
//		}
		if(position == 0) {
			jo2.addProperty("type", "top1003");
		} else if(position == 1) {
			jo2.addProperty("type", "top1000");
		} else if(position == 2) {
			jo2.addProperty("type", "top1001");
		} else {
			jo2.addProperty("type", "top1002");
		}
		
		// 查询道具
		BottleRestClient.post("userTop", this, jo2,	new AsyncHttpResponseHandler() {
					@Override
					public void onStart() {
					// TODO Auto-generated method stub
					super.onStart();
						showDialog("正在提交...","提交失败...",15*1000);
					}
				
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							UserTopBModel baseModel = gson.fromJson(str,
									UserTopBModel.class);
							if (baseModel != null
									&& !TextUtils.isEmpty(baseModel.getCode())) {
								if ("0".equals(baseModel.getCode())) {
									if (baseModel.getUserList() != null
											&& baseModel.getUserList().size() > 0) {
										List<TopUserModel> userList = baseModel.getUserList();
										Message msg= Message.obtain();
										if (userList.size() > 6) {
											phbHeadAdapter.addItemLast(userList.subList(0, 6));
											adapter.addItemLast(userList.subList(6, userList.size()));
											msg.what = FRESH;
										} else {
											phbHeadAdapter.addItemLast(userList);
											msg.what = FRESH2;
										}
										mHandler.sendMessage(msg);
										MyApplication.getApp().getSpUtil().setPhbCache(index, position, str);
									}
								} else {
									showMsg(baseModel.getMsg());
								}
							} else {
								showMsg("加载失败");
							}
						} else {
							showMsg("加载失败");
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						showMsg("加载失败");
					}

					@Override
					public void onFinish() {
						super.onFinish();
						closeDialog(mpDialog);
					}
				});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_grade_detail:
			if (UserManager.getInstance(this).getCurrentUser() == null) {
				Intent intent1 = new Intent(this, LoginActivity.class);
				intent1.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(intent1);
			} else {
//				Intent intent2 = new Intent(this, MlscActivity.class);
//				intent2.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//				startActivity(intent2);
				// TODO  进入我的等级详情
				Intent intent = new Intent(HotTopItemActivity.this,GradeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", UserManager.getInstance(getApplicationContext()).getCurrentUserId());
				startActivity(intent);
			}
			break;
		case R.id.bt_back:
			this.finish();
			break;
		}
	}

	public void updateItem() {
		pickDatas();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 400) {// 未登录状态下进入个人中心
			if (resultCode == Activity.RESULT_OK) {
				// phba.reflash();
				if (UserManager.getInstance(this).getCurrentUser() != null) {
					visitUserId = UserManager.getInstance(this)
							.getCurrentUserId();
				}
			}
		}
	}
}
