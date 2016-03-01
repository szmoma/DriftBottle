package com.hnmoma.driftbottle;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hnmoma.driftbottle.adapter.TalkCommentAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.custom.CustomDialog;
import com.hnmoma.driftbottle.custom.CustomDialog.CustomDialogClickListener;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.model.ReviewInfoModel;
import com.hnmoma.driftbottle.model.ReviewListModel;
import com.hnmoma.driftbottle.model.SingleTalkInfo;
import com.hnmoma.driftbottle.model.TalkListModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.sensitivewords.SensitivewordFilter;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.way.ui.emoji.EmojiTextView;
/**
 * 说说详情
 * @author Administrator
 *
 */
public class TalkDetailActivity extends BaseActivity implements  OnItemClickListener, OnItemLongClickListener {
	private final int FRESH = 1;//刷新ListView
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	TalkListModel singleTalk;

	TextView btn_support;
	TextView btn_review;
	Button btn_delete;
	ImageButton bt_send;
	LinearLayout ll_setting;
	RelativeLayout rl_review;
	ProgressBar pb;
	EditText et_comments;
//	TextView tv_tip;
	RelativeLayout rl_tip;
	ImageView iv_vip;

	PullToRefreshListView mPullRefreshListView;
	TalkCommentAdapter adapter;
	LinkedList<ReviewListModel> mInfos;

	String bottleId;
	String parentId;
	String toUserId;
	String reId;
	SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
	int identityflag = 1;// 默认1是他人说说
	boolean hasMore = false;
	boolean isloading = true;
	InputMethodManager imm;
	
	private boolean isSupport;//是否赞过
	private int position;
	
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
				.showImageOnFail(R.drawable.defalutimg).cacheInMemory(true)
				.cacheOnDisc(true).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();
		imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		bottleId = getIntent().getStringExtra("bottleId");
		toUserId = getIntent().getStringExtra("toUserId");
		parentId = getIntent().getStringExtra("parentId");
		isSupport = "1000".equals(getIntent().getStringExtra("isSupport"))?true:false;
		position = getIntent().getIntExtra("position", 0);

		setContentView(R.layout.activity_talkdetail);
		btn_delete = (Button) findViewById(R.id.bt_fx);
		btn_review = (TextView) findViewById(R.id.tv_pl);
		btn_support = (TextView) findViewById(R.id.tv_dz);
		bt_send = (ImageButton) findViewById(R.id.bt_send);
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_review);
		ll_setting = (LinearLayout) findViewById(R.id.ll_setting);
		rl_review = (RelativeLayout) findViewById(R.id.rl_comment);
		et_comments = (EditText) findViewById(R.id.et_comments);
		pb = (ProgressBar) findViewById(R.id.pb);
		pb.setVisibility(View.GONE);
		
		mPullRefreshListView.setMode(Mode.DISABLED);//暂时不用刷新机制
		
		if(isSupport){
			btn_support.setTextColor(getResources().getColor(R.color.dz_press));
			btn_support.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.read_sc_p), null, null);
		}
		getTalkDetail(bottleId, true);
	}

	private void getTalkDetail(final String talkId2, final boolean isFirst) {
		JsonObject jo = new JsonObject();
		jo.addProperty("bottleId", talkId2);

		BottleRestClient.post("queryBottleInfo", this, jo,
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {

						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							SingleTalkInfo model = gson.fromJson(str,
									SingleTalkInfo.class);
							if (model != null
									&& !TextUtils.isEmpty(model.getCode())) {
								if ("0".equals(model.getCode())) {
									if (isFirst) {
										singleTalk = model.getTalkInfo();
										initData();
									} else {
										adapter.addItemLast(model.getTalkInfo().getReviewLsit());
										Message msg = Message.obtain();
										msg.what = FRESH;
										mHandler.sendMessage(msg);
									}
									if (model.getIsMore() == 1) {
										hasMore = true;
									}
									isloading = false;
								} else {
									showMsg("瓶子已删除");
									Intent data = new Intent();
									data.putExtra("DeleteDone", true);
									data.putExtra("bottleId", talkId2);
									data.putExtra("position", position);
									setResult(RESULT_OK, data);
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
	

	protected void initData() {
		identityflag = UserManager.getInstance(this).getCurrentUserId().equals(singleTalk.getUserId()) ? 0 : 1;
		if(identityflag == 1) {
			btn_delete.setVisibility(View.GONE);
		} else {
			btn_delete.setVisibility(View.VISIBLE);
		}
		
		if (singleTalk.getReviewLsit() == null
				|| singleTalk.getReviewLsit().size() == 0) {
			mInfos = new LinkedList<ReviewListModel>();
		} else {
			mInfos = singleTalk.getReviewLsit();
		}
		adapter = new TalkCommentAdapter(this, imageLoader, options);
		View v = initHead();
		mPullRefreshListView.getRefreshableView().addHeaderView(v);
		adapter.addItemLast(mInfos);
		mPullRefreshListView.setAdapter(adapter);
		mPullRefreshListView.getRefreshableView().setOnItemClickListener(this);


		if (!TextUtils.isEmpty(toUserId)) {
			ll_setting.setVisibility(View.GONE);
			rl_review.setVisibility(View.VISIBLE);
			// et_comments.setFocusable(true);
			if ("-1".equals(toUserId)) {// 点击评论按钮时
				et_comments.setHint("");
				parentId = "0";
				toUserId = "";
				reId = "";
			} else {// 点击具体评论时
				
			}
		}
		

		if (ll_setting.getVisibility() == View.GONE) {
			et_comments.requestFocus();
			imm.showSoftInputFromInputMethod(et_comments.getWindowToken(), 0);
		} else {
			imm.hideSoftInputFromWindow(et_comments.getWindowToken(), 0);
		}
		mPullRefreshListView.getRefreshableView().setOnItemLongClickListener(this);
	}

	protected void deleteReview(final String reId) {
		JsonObject jo = new JsonObject();
		jo.addProperty("reId", reId);
		jo.addProperty("bottleId", singleTalk.getBottleId());
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
										btn_review.setText("评论( " + mInfos.size() + " )");
										rl_tip.setVisibility(View.VISIBLE);
									} else {
										btn_review.setText("评论(0)");
										rl_tip.setVisibility(View.GONE);
									}
									adapter.reset(mInfos);
									Message msg = Message.obtain();
									msg.what = FRESH;
									mHandler.sendMessage(msg);
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

	private View initHead() {
		View head = LayoutInflater.from(this).inflate(R.layout.talkdetail_head,null);
		
		CircularImage iv_head = (CircularImage) head.findViewById(R.id.iv_userhead);
		TextView tv_name = (TextView) head.findViewById(R.id.tv_name);
		EmojiTextView tv_content = (EmojiTextView) head
				.findViewById(R.id.tv_content);
//		RelativeLayout rl_head = (RelativeLayout) head
//				.findViewById(R.id.rl_head);
		TextView tv_gj = (TextView) head.findViewById(R.id.tv_dq);
		TextView tv_sf = (TextView) head.findViewById(R.id.tv_sf);
		TextView tv_xz = (TextView) head.findViewById(R.id.tv_time);
		LinearLayout fl_all = (LinearLayout) head.findViewById(R.id.fl_all);
//		tv_tip = (TextView) head.findViewById(R.id.tv_tip);
		iv_vip = (ImageView) head.findViewById(R.id.iv_vip);
		iv_vip.setVisibility(View.GONE);
		rl_tip = (RelativeLayout) head.findViewById(R.id.rl_tip);
//		rl_head.setBackgroundResource(R.drawable.mb_titletop);
		if (singleTalk.getSupportNum() > 0) {
			btn_support.setText("赞( " + singleTalk.getSupportNum() + " )");
		} else {
			btn_support.setText("赞(0)");
		}
		if (singleTalk.getReviewNum() > 0) {
			rl_tip.setVisibility(View.VISIBLE);
			btn_review.setText("评论( " + singleTalk.getReviewNum() + " )");
		} else {
			rl_tip.setVisibility(View.GONE);
			btn_review.setText("评论(0)");
		}
		tv_name.setText(singleTalk.getNickName());
		imageLoader.displayImage(singleTalk.getHeadImg(), iv_head, options);

		String idtype = singleTalk.getIdentityType();
		String[] its = new String[2];
		if (TextUtils.isEmpty(idtype)) {
			its[0] = "男";
			its[1] = "m";
		} else {
			its = getIdentityByCode(idtype);
		}

		if (TextUtils.isEmpty(singleTalk.getAge())) {
			tv_sf.setText("");
		} else {
			tv_sf.setText(singleTalk.getAge() + "");
		}
		// tv_sf.setText(its[0]);
		Drawable drawable;
		if (its[1].equals("m")) {
			drawable = getResources().getDrawable(R.drawable.icon_male_32);
			tv_sf.setBackgroundResource(R.drawable.manbg);
		} else {
			drawable = getResources().getDrawable(R.drawable.icon_female_32);
			tv_sf.setBackgroundResource(R.drawable.womanbg);
		}
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv_sf.setCompoundDrawables(drawable, null, null, null);
		tv_sf.setPadding(MoMaUtil.dip2px(this, 5), 0, MoMaUtil.dip2px(this, 5), 0);
		
		String province = TextUtils.isEmpty(singleTalk.getProvince()) ? ""
				: singleTalk.getProvince();
		String city = TextUtils.isEmpty(singleTalk.getCity()) ? "" : singleTalk
				.getCity();
		if(TextUtils.isEmpty(province) && TextUtils.isEmpty(city)) {
			tv_gj.setVisibility(View.GONE);
		} else {
			tv_gj.setText(province + " " + city);
		}
		

		if (TextUtils.isEmpty(singleTalk.getConstell())) {
			tv_xz.setText("");
		} else {
			tv_xz.setText(singleTalk.getConstell());
		}
		// 文字
		if ("5000".equals(singleTalk.getContentType())) {
			fl_all.setVisibility(View.GONE);
			tv_content.setVisibility(View.VISIBLE);
			tv_content.setText(singleTalk.getContent());
		} else {// 图文
			fl_all.setVisibility(View.VISIBLE);
			tv_content.setVisibility(View.GONE);
			// View content =
			// LayoutInflater.from(this).inflate(R.layout.frament_frame_image,
			// null);
			// LinearLayout view_bottle_head = (LinearLayout)
			// content.findViewById(R.id.view_bottle_head);
			ImageView iv = (ImageView) head.findViewById(R.id.iv);
			TextView tv_cnt = (TextView) head.findViewById(R.id.tv_cnt);
			imageLoader.displayImage(singleTalk.getShortPic(), iv, options);
			tv_cnt.setText(singleTalk.getContent());
			// view_bottle_head.setVisibility(View.GONE);
			iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 打开大图
					String imageUrl = singleTalk.getUrl();
					if (TextUtils.isEmpty(imageUrl)) {
						showMsg("查看失败");
					} else {
						Intent intent = new Intent(TalkDetailActivity.this,
								SingleImageFrameShowActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("imageUrl", imageUrl);
						startActivity(intent);
					}
				}
			});

		}
		
		return head;
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
				doSubmit(singleTalk.getBottleId(),true);
			break;
		case R.id.bt_pl:
			long currentTime = System.currentTimeMillis();
			if(identityflag == 1 && (currentTime - pauseTime)  < 1000*60) {
				showMsg(getResources().getString(R.string.comment_gap_tip));
				break;
			}
			ll_setting.setVisibility(View.GONE);
			rl_review.setVisibility(View.VISIBLE);
			et_comments.requestFocus();
			et_comments.setHint("");
			et_comments.setText("");
			parentId = "0";
			toUserId = "";
			imm.showSoftInputFromInputMethod(et_comments.getWindowToken(), 0);
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
								    		doSubmit(singleTalk.getBottleId(),false);
								    	 }else if(which == 1){
								    		dialog.dismiss();
								    	 }
								     }
							    })
							.show();
			break;
		case R.id.bt_return:
			ll_setting.setVisibility(View.VISIBLE);
			rl_review.setVisibility(View.GONE);
			imm.hideSoftInputFromWindow(et_comments.getWindowToken(), 0);
			break;
		case R.id.bt_send:
			// showMsg("review");
			String content = et_comments.getText().toString().trim();
			if (!TextUtils.isEmpty(content) && check(content)) {
				addComment(content);
			}
			break;

		case R.id.iv_commenthead:
			ReviewListModel model1 = adapter.getItem(mPullRefreshListView
					.getRefreshableView().getPositionForView(v) - 2);
			if (model1.getUserId().equals(
					UserManager.getInstance(this).getCurrentUserId())) {
				Intent intent = new Intent(TalkDetailActivity.this,VzoneActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", model1.getUserId());
				intent.putExtra("identityflag", 0);
				intent.putExtra("visitUserId", UserManager.getInstance(this)
						.getCurrentUserId());
				startActivity(intent);
			} else {
				Intent intent = new Intent(TalkDetailActivity.this,
						VzoneActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("identityflag", 1);
				intent.putExtra("userId", model1.getUserId());
				intent.putExtra("visitUserId", UserManager.getInstance(this)
						.getCurrentUserId());
				startActivity(intent);
			}
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
	private void addComment(String content) {
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("bottleId", singleTalk.getBottleId());
		jo.addProperty("reContent", content);
		jo.addProperty("come", "6000");
		jo.addProperty("parentId", parentId);
		jo.addProperty("toUserId", toUserId);

		BottleRestClient.post("addBottleReview", this, jo,	new AsyncHttpResponseHandler() {

					@Override
					public void onStart() {
						super.onStart();
						pb.setVisibility(View.VISIBLE);
						bt_send.setVisibility(View.GONE);
					}

					@Override
					public void onFinish() {
						super.onFinish();
						pb.setVisibility(View.GONE);
						bt_send.setVisibility(View.VISIBLE);
						imm.hideSoftInputFromWindow(et_comments.getWindowToken(), 0);
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
									rl_tip.setVisibility(View.VISIBLE);
									singleTalk.setReviewNum(mInfos.size() + 1);
									btn_review.setText("评论( " + singleTalk.getReviewNum() + " )");
									ll_setting.setVisibility(View.VISIBLE);
									rl_review.setVisibility(View.GONE);
									if ("0".equals(parentId)) {
										mInfos.addFirst(model.getReviewInfo());
//										mInfos.addLast(model.getReviewInfo());
										adapter.reset(mInfos);
										Message msg = Message.obtain();
										msg.what = FRESH;
										mHandler.sendMessage(msg);
										// singleTalk.getReviewLsit().addLast(model.getReviewInfo());
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
	public void doSubmit(@NonNull final String bottleId ,final boolean support) {
		if(TextUtils.isEmpty(bottleId)){
			showMsg("警告：提交失败");
			return ;
		}
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
								singleTalk.setSupportNum(singleTalk.getSupportNum() + 1);
								btn_support.setText("赞( "+ singleTalk.getSupportNum() + " )");
								btn_support.setTextColor(getResources().getColor(R.color.dz_press));
								btn_support.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.read_sc_p), null, null);
								supportNum++;
							}else {
								showMsg("删除成功");
								Intent data = new Intent();
								data.putExtra("talkId", bottleId);
								data.putExtra("position", position);
								setResult(Activity.RESULT_OK, data);
								TalkDetailActivity.this.finish();
							}
						} else if("100022".equals(model.getCode())){
							singleTalk.setState("1000");//存储临时值，1000表示已经赞过
							isSupport = true;
							btn_support.setTextColor(getResources().getColor(R.color.dz_press));
							btn_support.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.read_sc_p), null, null);
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ReviewListModel model = (ReviewListModel) parent.getItemAtPosition(position);
		if(model ==null)
			return ;
		
		parentId = model.getParentId();
		toUserId = model.getUserId();
		if(toUserId.equals(UserManager.getInstance(this).getCurrentUserId())) {
			showMsg("不能回复自己");
			return;
		}
		ll_setting.setVisibility(View.GONE);
		rl_review.setVisibility(View.VISIBLE);
		et_comments.setText("");
		et_comments.setHint("回复 " + model.getNickName());
		reId = model.getReId();
		et_comments.requestFocus();
		imm.showSoftInputFromInputMethod(et_comments.getWindowToken(), 0);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		ReviewListModel model = (ReviewListModel) parent.getItemAtPosition(position);
		if(model==null){
			return true;
		}
		if (!model.getUserId().equals(UserManager.getInstance(TalkDetailActivity.this).getCurrentUserId())&&identityflag==1){
			showMsg("只能删除自己的评论");
			return true;
		}
		final String resId = model.getReId();
		new CustomDialog().showSelectDialog(
				TalkDetailActivity.this, "删除该条评论?",
				new CustomDialogClickListener() {
					@Override
					public void confirm() {
						deleteReview(resId);
					}
					@Override
					public void cancel() {
					}
		});
		return true;
		}
}
