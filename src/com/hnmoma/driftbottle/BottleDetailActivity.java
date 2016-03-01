package com.hnmoma.driftbottle;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.AvoidXfermode.Mode;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.adapter.TalkCommentAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.custom.CustomDialog;
import com.hnmoma.driftbottle.custom.CustomDialog.CustomDialogClickListener;
import com.hnmoma.driftbottle.custom.ListViewLoadMore;
import com.hnmoma.driftbottle.custom.ListViewLoadMore.IsLoadingListener;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.model.CourseItemModel;
import com.hnmoma.driftbottle.model.ReviewInfoModel;
import com.hnmoma.driftbottle.model.ReviewListModel;
import com.hnmoma.driftbottle.model.SingleTalkInfo;
import com.hnmoma.driftbottle.model.TalkListModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.sensitivewords.SensitivewordFilter;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.way.ui.emoji.EmojiTextView;
/**
 * 说说详情
 * @author yangsy
 *
 */
public class BottleDetailActivity extends BaseActivity {
	private final int FRESH = 1;//刷新ListView
	private final int INITCOUSR= 2;//初始化我的轨迹
	private final int INITBOTTLE=3;//初始化我的瓶子信息
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	TalkCommentAdapter adapter;
	LinkedList<ReviewListModel> mInfos;
	
	ImageButton btnDelete; //删除按钮
	ImageButton btnBack;	//返回按钮
	
	View contnet;//整个内容区域
	View viewComment;//评论区域
	View viewBottom;//底部导航区域
	
	CircularImage ivHead;//用户头像
	TextView tvName;//用户姓名
	ImageView ivVIP;//VIP的标志
	TextView tvZone;//省份
	TextView tvSex;//性别
	TextView tvConstell;//星座
	
	ImageView ivBottle;//瓶子中的图
	EmojiTextView etvTxt;//片瓶子中的文字
	
	LinearLayout llCours;//轨迹
	View viewCourse;//轨迹模块
	
	
	ListViewLoadMore mListView; //加载评论的控件
	
	TextView btnSupport; //点赞按钮
	TextView btnComment;//点击平路按钮
	
	LinearLayout viewNagtive; //顶部导航栏--点赞/评论操作
	RelativeLayout viewNagtiveSend;//底部导航栏--评论消息
	ImageButton btCancleSend;//取消发送
	ProgressBar pbSned; //发送消息的进度条
	EditText etComments; //可编辑发送的消息
	ImageButton btSend; //发送按钮
	
	String bottleId;
	String parentId;
	String toUserId;
	String reId;
	SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
	int identityflag = 1;// 默认1是他人说说
	boolean hasMore = false; //是否有更多
	InputMethodManager imm;
	
	private boolean isSupport;//是否赞过
	private int position;
	
	TalkListModel singleTalk;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FRESH:
				adapter.notifyDataSetChanged();
				if(adapter.getCount()==0){
					viewComment.setVisibility(View.GONE);
				}else{
					viewComment.setVisibility(View.VISIBLE);
				}
				break;
			case INITCOUSR:
				List<CourseItemModel> list = (List<CourseItemModel>) msg.obj;
				initCourseData(list);
				break;
			case INITBOTTLE:
				TalkListModel model = (TalkListModel) msg.obj;
				contnet.setVisibility(View.VISIBLE);
				initData(model);
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
				.showImageOnFail(R.drawable.defalutimg).cacheInMemory(true)
				.cacheOnDisc(true).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();
		
		bottleId = getIntent().getStringExtra("bottleId");
		position = getIntent().getIntExtra("position", 0);
		setupView();
		init();
		
		if(isSupport){
			btnSupport.setTextColor(getResources().getColor(R.color.dz_press));
			btnSupport.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.read_sc_p), null, null);
		}
		getTalkDetail(bottleId, false);
	}
	private void init() {
		// TODO Auto-generated method stub
		contnet.setVisibility(View.GONE);
		mInfos = new LinkedList<ReviewListModel>();
		adapter = new TalkCommentAdapter(this, imageLoader, options);
		mListView.setAdapter(adapter);
		
		imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (viewNagtive.getVisibility() == View.GONE&&viewNagtiveSend.getVisibility()==View.VISIBLE) {
			etComments.requestFocus();
			imm.showSoftInputFromInputMethod(etComments.getWindowToken(), 0);
		} 
		
		mListView.setLoading(false);//不需要上拉加载更多
		mListView.setOnItemLongClickListener( new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				final ReviewListModel modle = adapter.getItem(position);
				if(!UserManager.getInstance(BottleDetailActivity.this).getCurrentUserId().equals(modle.getUserId())&&identityflag==1){
					showMsg("只能删除自己的评论");
					return true;
				}
				new CustomDialog().showSelectDialog(
						BottleDetailActivity.this, "删除该条评论?",
						new CustomDialogClickListener() {
							@Override
							public void confirm() {
								deleteReview(modle.getReId(),bottleId);
							}
							@Override
							public void cancel() {
							}
				});
				return true;
			}
		});
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				ReviewListModel model = (ReviewListModel) parent.getItemAtPosition(position);
				parentId = model.getParentId();
				toUserId = model.getUserId();
				if(toUserId.equals(UserManager.getInstance(BottleDetailActivity.this).getCurrentUserId())) {
					showMsg("不能回复自己");
					return;
				}
				viewNagtive.setVisibility(View.GONE);
				viewNagtiveSend.setVisibility(View.VISIBLE);
				etComments.setText("");
				etComments.setHint("回复 " + model.getNickName());
				reId = model.getReId();
				etComments.requestFocus();
				imm.showSoftInputFromInputMethod(etComments.getWindowToken(), 0);
			}
		});
		
		mListView.setOnLoadingListener(new IsLoadingListener() {
			
			@Override
			public void onLoad() {
				// TODO Auto-generated method stub
				//加载更多
				
				
			}
		});
	}

	private void setupView() {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_detail_bottle);
		
		viewBottom = findViewById(R.id.menu_view);
		viewComment = findViewById(R.id.ll_comment);
		contnet = findViewById(R.id.content);
		
		btnBack = (ImageButton) findViewById(R.id.bt_back);
		btnDelete = (ImageButton) findViewById(R.id.bt_fx);
		
		ivHead = (CircularImage) findViewById(R.id.iv_userhead);
		tvName = (TextView) findViewById(R.id.tv_name);
		ivVIP =  (ImageView) findViewById(R.id.iv_vip);
		tvZone = (TextView) findViewById(R.id.tv_dq);
		tvSex = (TextView) findViewById(R.id.tv_sf);
		tvConstell = (TextView) findViewById(R.id.tv_time);
		
		ivBottle =  (ImageView) findViewById(R.id.iv);
		etvTxt = (EmojiTextView) findViewById(R.id.tv_content);
		
		llCours = (LinearLayout) findViewById(R.id.ll_gallery);
		viewCourse = findViewById(R.id.ll_course);
		
		mListView = (ListViewLoadMore) findViewById(R.id.lv_review);
		
		btnComment = (TextView) findViewById(R.id.tv_pl);
		btnSupport = (TextView) findViewById(R.id.tv_dz);
		viewNagtive = (LinearLayout) findViewById(R.id.ll_setting);
		viewNagtiveSend = (RelativeLayout) findViewById(R.id.rl_comment);
		etComments = (EditText) findViewById(R.id.et_comments);
		pbSned = (ProgressBar) findViewById(R.id.pb);
		btSend = (ImageButton) findViewById(R.id.bt_send);
		pbSned.setVisibility(View.GONE);
		viewNagtiveSend.setVisibility(View.GONE);
	}
	/**
	 * 
	 * @param bottleId 瓶子的id
	 * @param isLoadMore 是否加载更多
	 */
	private void getTalkDetail(final String bottleId, final boolean isLoadMore) {
		JsonObject jo = new JsonObject();
		jo.addProperty("bottleId", bottleId);

		BottleRestClient.post("queryBottleInfo", this, jo,new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							SingleTalkInfo model = gson.fromJson(str,SingleTalkInfo.class);
							if (model != null
									&& !TextUtils.isEmpty(model.getCode())) {
								if ("0".equals(model.getCode())) {
									if (!isLoadMore) {
										singleTalk = model.getTalkInfo();
										Message msg = Message.obtain();
										msg.what = INITBOTTLE;
										msg.obj = model.getTalkInfo();
										mHandler.sendMessage(msg);
									} else {
										adapter.addItemLast(model.getTalkInfo().getReviewLsit());
										Message msg = Message.obtain();
										msg.what = FRESH;
										mHandler.sendMessage(msg);
									}
									if (model.getIsMore() == 1) {
										hasMore = true;
									}else{
										hasMore = false;
									}
								} else {
									
									showMsg("瓶子已删除");
									Intent data = new Intent();
									data.putExtra("DeleteDone", true);
									data.putExtra("bottleId", bottleId);
									data.putExtra("position", position);
									setResult(RESULT_OK, data);
									//停顿1s后，结束当前页面
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									finish();
								}
							}
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						showMsg("加载失败");
					}
				});

	}

	protected void initData(final TalkListModel model) {
		identityflag = UserManager.getInstance(this).getCurrentUserId().equals(model.getUserId()) ? 0 : 1;
		if(identityflag == 1) {
			btnDelete.setVisibility(View.GONE);
		} else {
			btnDelete.setVisibility(View.VISIBLE);
		}
		
		if (model.getSupportNum() > 0) {
			btnSupport.setText("赞( " + model.getSupportNum() + " )");
		} else {
			btnSupport.setText("赞(0)");
		}
		if (model.getReviewNum() > 0) {
			btnComment.setText("评论( " + model.getReviewNum() + " )");
		} else {
			btnComment.setText("评论(0)");
		}
		
		tvName.setText(model.getNickName());
		imageLoader.displayImage(model.getHeadImg(), ivHead, options);
		String idtype = model.getIdentityType();
		String[] its = new String[2];
		if (TextUtils.isEmpty(idtype)) {
			its[0] = "男";
			its[1] = "m";
		} else {
			its = getIdentityByCode(idtype);
		}
		if(model.getIsVIP()==0)
			ivVIP.setImageResource(R.drawable.ic_vip_not);
		else
			ivVIP.setImageResource(R.drawable.ic_vip);
		tvSex.setText(String.valueOf(model.getAge()));
		Drawable drawable;
		if (its[1].equals("m")) {
			drawable = getResources().getDrawable(R.drawable.icon_male_32);
			tvSex.setBackgroundResource(R.drawable.manbg);
		} else {
			drawable = getResources().getDrawable(R.drawable.icon_female_32);
			tvSex.setBackgroundResource(R.drawable.womanbg);
		}
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tvSex.setCompoundDrawables(drawable, null, null, null);
		tvSex.setPadding(MoMaUtil.dip2px(this, 5), 0, MoMaUtil.dip2px(this, 5), 0);
		
		String province = TextUtils.isEmpty(model.getProvince()) ? ""
				: model.getProvince();
		String city = TextUtils.isEmpty(model.getCity()) ? "" : model
				.getCity();
		if(TextUtils.isEmpty(province) && TextUtils.isEmpty(city)) {
			tvZone.setVisibility(View.GONE);
		} else {
			tvZone.setText(province + " " + city);
		}
		

		if (TextUtils.isEmpty(model.getConstell())) {
			tvConstell.setVisibility(View.GONE);;
		} else {
			tvConstell.setText(model.getConstell());
		}
		// 文字
		if ("5000".equals(model.getContentType())) {
			ivBottle.setVisibility(View.GONE);
			etvTxt.setVisibility(View.VISIBLE);
			etvTxt.setText(model.getContent());
		} else {// 图文
			ivBottle.setVisibility(View.VISIBLE);
			etvTxt.setVisibility(View.VISIBLE);
			
			imageLoader.displayImage(model.getShortPic(), ivBottle, options);
			etvTxt.setText(model.getContent());
			ivBottle.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 打开大图
					String imageUrl = model.getUrl();
					if (TextUtils.isEmpty(imageUrl)) {
						showMsg("查看失败");
					} else {
						Intent intent = new Intent(BottleDetailActivity.this,SingleImageFrameShowActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("imageUrl", imageUrl);
						startActivity(intent);
					}
				}
			});

		}
		
		if(model.getReviewLsit()!=null)
			mInfos.addAll(model.getReviewLsit());
		adapter.addItemLast(mInfos);
		Message msg = Message.obtain();
		msg.what = FRESH;
		mHandler.sendMessage(msg);

		if (!TextUtils.isEmpty(toUserId)) {
			viewNagtive.setVisibility(View.GONE);
			viewNagtiveSend.setVisibility(View.VISIBLE);
			// et_comments.setFocusable(true);
			if ("-1".equals(toUserId)) {// 点击评论按钮时
				etComments.setHint("");
				parentId = "0";
				toUserId = "";
				reId = "";
			} else {// 点击具体评论时
				
			}
		}
		Message message = Message.obtain();
		message.what = INITCOUSR;
		message.obj = model.getUserList();
		mHandler.sendMessage(message);
	}

	protected void deleteReview(final String reId,final String bottleId) {
		JsonObject jo = new JsonObject();
		jo.addProperty("reId", reId);
		jo.addProperty("bottleId", bottleId);
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());

		BottleRestClient.post("delBottleReview", this, jo,
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {

						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							BaseModel model = gson.fromJson(str, BaseModel.class);
							if (model != null && !TextUtils.isEmpty(model.getCode())) {
								if ("0".equals(model.getCode())) {
									LinkedList<ReviewListModel> tempList = new LinkedList<ReviewListModel>();
									for (ReviewListModel reviewModel : mInfos) {
										if (reId.equals(reviewModel.getReId())) {
											tempList.add(reviewModel);
										} else if (reId.equals(reviewModel.getParentId())) {
											tempList.add(reviewModel);
										}
									}
									for (ReviewListModel delModel : tempList) {
										mInfos.remove(delModel);
									}
									if (mInfos.size() > 0) {
										btnComment.setText("评论( " + mInfos.size() + " )");
									} else {
										btnComment.setText("评论(0)");
									}
									adapter.reset(mInfos);
									Message msg = Message.obtain();
									msg.what = FRESH;
									mHandler.sendMessage(msg);
									
									commentNum-=1;
									Intent data = new Intent();
									data.putExtra("DeleteDone", true);
									data.putExtra("bottleId", bottleId);
									data.putExtra("position", position);
									setResult(RESULT_OK, data);
									
									showMsg("删除成功");
								} else {
									showMsg(model.getMsg());
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
	/*
	 * 获取性别
	 */
	private String[] getIdentityByCode(String code) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("2000", "小萝莉");
		map.put("2001", "花样少女");
		map.put("2002", "知性熟女");
		map.put("2003", "小正太");
		map.put("2004", "魅力少年");
		map.put("2005", "成熟男生");
		map.put("2006", "男");
		map.put("2007", "女");
		String flag = map.get(code);
		map = null;

		String[] strs = new String[2];
		strs[0] = flag;
		// m-lan f-nv
		if (code.equals("2000") || code.equals("2001") || code.equals("2002")
				|| code.equals("2007")) {
			strs[1] = "f";
		} else {
			strs[1] = "m";
		}

		return strs;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_back:
			Intent data = new Intent();
			data.putExtra("supportNum", supportNum);
			data.putExtra("commentNum", commentNum);
			data.putExtra("position", position);
			setResult(Activity.RESULT_OK, data);
			this.finish();
			break;
		case R.id.bt_dz:
			if(isSupport){
				showMsg("已经点赞");
			}else
				doSubmit(bottleId,true);
			break;
		case R.id.bt_pl:
			long currentTime = System.currentTimeMillis();
			if(identityflag == 1 && (currentTime - pauseTime)  < 1000*60) {
				showMsg(getResources().getString(R.string.comment_gap_tip));
				break;
			}
			viewNagtive.setVisibility(View.GONE);
			viewNagtiveSend.setVisibility(View.VISIBLE);
			etComments.requestFocus();
			etComments.setHint("");
			etComments.setText("");
			parentId = "0";
			toUserId = "";
			imm.showSoftInputFromInputMethod(etComments.getWindowToken(), 0);
			break;
		case R.id.bt_fx:
			String[] strarray  = new String[] {"删除瓶子", "取消"};
			new AlertDialog.Builder(this)
							.setTitle("选择")
							.setItems(strarray, new DialogInterface.OnClickListener() {
								     @Override
								     public void onClick(DialogInterface dialog, int which) {
								    	 if(which == 0){
								    		dialog.dismiss();
								    		doSubmit(bottleId,false);
								    	 }else if(which == 1){
								    		dialog.dismiss();
								    	 }
								     }
							    })
							.show();
			break;
		case R.id.bt_return:
			viewNagtive.setVisibility(View.VISIBLE);
			viewNagtiveSend.setVisibility(View.GONE);
			imm.hideSoftInputFromWindow(etComments.getWindowToken(), 0);
			break;
		case R.id.bt_send:
			// showMsg("review");
			String content = etComments.getText().toString().trim();
			if (!TextUtils.isEmpty(content) && check(content)) {
				addComment(bottleId,content);
			}
			break;

		case R.id.iv_commenthead:
			ReviewListModel model1 = adapter.getItem(mListView.getPositionForView(v));
			Intent intent = new Intent(BottleDetailActivity.this,VzoneActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			intent.putExtra("userId", model1.getUserId());
			intent.putExtra("visitUserId", UserManager.getInstance(this).getCurrentUserId());
			if (UserManager.getInstance(this).getCurrentUserId().equals(model1.getUserId())) {
				intent.putExtra("identityflag", 0);
			} else {
				intent.putExtra("identityflag", 1);
			}
			startActivity(intent);
			break;
		}
	}

	/**
	 * 评论检查 result true，内容没问题 result false，内容有问题
	 */
	public boolean check(String content) {
		boolean result = true;

		// 检查内容
		String str = content;

		// 检查网络
		if (!MoMaUtil.isNetworkAvailable(this)) {
			showMsg("当前网络不可用，请检查");
			return result = false;
		}

		// 敏感词处理
		SensitivewordFilter filter = MyApplication.getApp()
				.getSensitiveFilter();
		Set<String> set = filter.getSensitiveWord(str, 1);
		if (set.size() != 0) {
			showMsg("提交内容含有敏感词，请修正");
			return result = false;
		}

		return result;
	}

	int commentNum = 0;
	long pauseTime = 0;// 评论限时
	private void addComment(String bottleId,String content) {
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("bottleId", bottleId);
		jo.addProperty("reContent", content);
		jo.addProperty("come", "6000");
		jo.addProperty("parentId", parentId);
		jo.addProperty("toUserId", toUserId);

		BottleRestClient.post("addBottleReview", this, jo,	new AsyncHttpResponseHandler() {

					@Override
					public void onStart() {
						super.onStart();
						pbSned.setVisibility(View.VISIBLE);
						btSend.setVisibility(View.GONE);
					}

					@Override
					public void onFinish() {
						super.onFinish();
						pbSned.setVisibility(View.GONE);
						btSend.setVisibility(View.VISIBLE);
						imm.hideSoftInputFromWindow(etComments.getWindowToken(), 0);
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {

						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							ReviewInfoModel model = gson.fromJson(str, ReviewInfoModel.class);
							if (model != null && !TextUtils.isEmpty(model.getCode())) {
								if ("0".equals(model.getCode())) {
									showMsg("评论成功");
									pauseTime = System.currentTimeMillis();
									commentNum++;
									if(singleTalk!=null)
										singleTalk.setReviewNum(mInfos.size() + 1);
									btnComment.setText("评论( " + (singleTalk==null?1:singleTalk.getReviewNum()) + " )");
									viewNagtive.setVisibility(View.VISIBLE);
									viewNagtiveSend.setVisibility(View.GONE);
									if ("0".equals(parentId)) {
										mInfos.addFirst(model.getReviewInfo());
//										mInfos.addLast(model.getReviewInfo());
										adapter.reset(mInfos);
										Message msg = Message.obtain();
										msg.what = FRESH;
										mHandler.sendMessage(msg);
									} else {
										// 有评论的情况下
										int firstPosition = 0;
										boolean isFirst = true;
										for (ReviewListModel baseModel : mInfos) {
											if (parentId.equals(baseModel.getParentId())) {
												if (isFirst) {
													firstPosition = mInfos.indexOf(baseModel);
													isFirst = false;
												}
												firstPosition++;
											}
										}
										
										mInfos.add(firstPosition, model.getReviewInfo());
										adapter.reset(mInfos);
										Message msg = Message.obtain();
										msg.what = FRESH;
										mHandler.sendMessage(msg);
									}

								}
							}
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						showMsg("评论失败");
					}

				});
	}

	int supportNum = 0;

	// 删除说说或点赞
	public void doSubmit(final String bottleId ,final boolean support) {
		String submit = "";
		if (support) {
			submit = "loveBottle";
		} else {
			submit = "delBottle";
		}
		JsonObject jo = new JsonObject();
		jo.addProperty("bottleId", bottleId);
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUser().getUserId());

		BottleRestClient.post(submit, this, jo, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {

				String str = new String(responseBody);
				if (!TextUtils.isEmpty(str)) {
					Gson gson = new Gson();
					BaseModel model = gson.fromJson(str, BaseModel.class);
					if (model != null && !TextUtils.isEmpty(model.getCode())) {
						if ("0".equals(model.getCode())) {
							if (support) {
								showMsg("点赞成功");
								isSupport = true;
								if(singleTalk!=null)
									singleTalk.setSupportNum(singleTalk.getSupportNum() + 1);
								btnSupport.setText("赞( "+ singleTalk.getSupportNum() + " )");
								btnSupport.setTextColor(getResources().getColor(R.color.dz_press));
								btnSupport.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.read_sc_p), null, null);
								supportNum++;
							}else {
								showMsg("删除成功");
								Intent data = new Intent();
								data.putExtra("DeleteDone", true);
								data.putExtra("bottleId", bottleId);
								data.putExtra("position", position);
								setResult(Activity.RESULT_OK, data);
								finish();
							}
						} else if("100022".equals(model.getCode())){
							if(singleTalk!=null)
								singleTalk.setState("1000");//存储临时值，1000表示已经赞过
							isSupport = true;
							btnSupport.setTextColor(getResources().getColor(R.color.dz_press));
							btnSupport.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.read_sc_p), null, null);
							showMsg(model.getMsg());
						} else {
							showMsg(model.getMsg());
						}
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				showMsg("操作失败");
			}

		});
	}

	@Override
	public void onBackPressed() {
		Intent data = new Intent();
		data.putExtra("supportNum", supportNum);
		data.putExtra("commentNum", commentNum);
		data.putExtra("position", position);
		setResult(Activity.RESULT_OK, data);
		super.onBackPressed();
	}

	
	/**
	 * 初始化我的轨迹
	 * @param userList
	 */
	protected void initCourseData(List<CourseItemModel> userList) {
		if(userList == null || userList.size() == 0) {
			llCours.setVisibility(View.GONE);
			viewCourse.setVisibility(View.GONE);
			return ;
		} 
		llCours.setVisibility(View.VISIBLE);
		viewCourse.setVisibility(View.VISIBLE);
		for(int i=0; i < userList.size(); i++) {
			View course = getLayoutInflater().inflate(R.layout.view_mybottle_course, null);
			CircularImage iv_userhead = (CircularImage) course.findViewById(R.id.iv_userhead);
			TextView tv_name = (TextView) course.findViewById(R.id.tv_name);
			View view_gap = course.findViewById(R.id.view_gap);
			imageLoader.displayImage(userList.get(i).getHeadImg(), iv_userhead, options);
			final String userId = userList.get(i).getUserId();
			iv_userhead.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(BottleDetailActivity.this, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("userId", userId);
					if(userId.equals(UserManager.getInstance(BottleDetailActivity.this).getCurrentUserId())){
						intent.putExtra("identityflag", 0);
						intent.putExtra("visitUserId", userId);
					}else{
						intent.putExtra("identityflag", 1);
						intent.putExtra("visitUserId", UserManager.getInstance(BottleDetailActivity.this).getCurrentUserId());	//访问ID
					}
					startActivity(intent);
				}
			});
			tv_name.setText(userList.get(i).getNickName());
			if(userList.get(i).getIsVIP() == 0) {
				tv_name.setTextColor(getResources().getColor(R.color.bottlecontent_username));
			} else {
				tv_name.setTextColor(getResources().getColor(R.color.username_vip));
			}
			
			if(i == userList.size()-1) {
				view_gap.setVisibility(View.GONE);
			} else {
				view_gap.setVisibility(View.VISIBLE);
			}
			llCours.addView(course);
		}
	}
	
}
