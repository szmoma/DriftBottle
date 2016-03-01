package com.hnmoma.driftbottle.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hnmoma.driftbottle.FishingBottleActivity;
import com.hnmoma.driftbottle.LoginActivity;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.adapter.CommentAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.itfc.BottleVoicePlayClickListener;
import com.hnmoma.driftbottle.model.ActionNumModel;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.CommentMainModel;
import com.hnmoma.driftbottle.model.CommentModel;
import com.hnmoma.driftbottle.model.DealBottleModel;
import com.hnmoma.driftbottle.model.PickBottleModel;
import com.hnmoma.driftbottle.model.User;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.sensitivewords.SensitivewordFilter;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;

/**
 *系统瓶子打开读取界面（包括瓶子内容和评论）
 */
public class Fragment_wdpz_detail extends BaseFragment implements OnClickListener, PlatformActionListener, ShareContentCustomizeCallback, Callback{
	private final int FINISH = 0;
	private final int MORE = 1;
	private final int FRESH = 2;
	
	RelativeLayout fl_tip;
	CircularImage iv_head;
	ImageView iv;
	Button bt_ts;
	TextView tv_name;
	TextView tv_sf;
	TextView tv_dq;
	TextView tv_cnt;
	TextView tv_da;
	TextView tv_time;

	TextView tv_sc;
	TextView tv_rd;
	
	SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");

	PickBottleModel model;
	BottleModel bottleModel;
	ActionNumModel actionNumModel;
	List<CommentModel> commentModels;

	CommentAdapter adapter;
	CommentModel commentModel;
	
	private static final int  PAGE_NUM = 10;
	private  String   reviewID;
	
	LinearLayout menu_view;
	RelativeLayout comment_view;
	EditText et_comments;
	ImageButton bt_send;
	InputMethodManager imm;
	ImageButton bt_return;
	String commentsContentn;
	View footView;
	
	TextView tv_pl;
	TextView tv_dz;
	
	PullToRefreshListView mPullRefreshListView;
	
	private boolean isLast;//是否是最后一项
	String parentId;
	String toUserId;
	
	private Handler mHandler = new Handler()	{
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FINISH://
				mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多");
				mPullRefreshListView.onRefreshComplete();
				break;
			case MORE:	//加载更多
				mPullRefreshListView.getLoadingLayoutProxy().setRefreshingLabel("正在加载");
				mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("上拉加载更多");
				mPullRefreshListView.getLoadingLayoutProxy().setReleaseLabel("释放开始加载");
				commentsData(reviewID,true);
				break;
			case FRESH:
				adapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		};
	};
	
	public static BaseFragment newInstance(PickBottleModel model) {
		Fragment_wdpz_detail fragment = new Fragment_wdpz_detail();
		Bundle bundle = new Bundle();
		bundle.putSerializable("PickBottleModel", model);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
		parentId = "0";
		toUserId = "";
		return inflater.inflate(R.layout.frament_plpz_detailnt, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.view_center);
		tv_pl = (TextView) findViewById(R.id.tv_pl);
		tv_dz = (TextView) findViewById(R.id.tv_dz);
		menu_view = (LinearLayout) findViewById(R.id.menu_view);
		comment_view = (RelativeLayout) findViewById(R.id.comment_view);
		bt_send = (ImageButton) findViewById(R.id.bt_send);
		footView = findViewById(R.id.footview);
		et_comments = (EditText) findViewById(R.id.et_comments);
		bt_return = (ImageButton) findViewById(R.id.bt_return);
		
		mPullRefreshListView.setMode(Mode.PULL_FROM_END);//底部刷新
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				if(isLast)
					msg.what = FINISH;
				else
					msg.what = MORE;
				mHandler.sendMessage(msg);
			}
		});
		
		bt_send.setOnClickListener(this);
		bt_return.setOnClickListener(this);
		findViewById(R.id.bt_back).setOnClickListener(this);
		findViewById(R.id.bt_pl).setOnClickListener(this);
		findViewById(R.id.bt_dz).setOnClickListener(this);
		findViewById(R.id.bt_fx).setOnClickListener(this);
		
		Bundle bundle = getArguments();
		model = (PickBottleModel) bundle.getSerializable("PickBottleModel");
		if(model!=null){
			bottleModel = model.getBottleInfo();
			commentModels = model.getCommentModels();
			actionNumModel = model.getActionNum();
		}else{
			bottleModel = new BottleModel();
			commentModels = new LinkedList<CommentModel>();
			actionNumModel = new ActionNumModel();
		}
		tv_pl.setText("评论("+actionNumModel.getReviewNum()+")");
		int supportNum = model.getActionNum().getSupportNum();
		if(MyApplication.getApp().getSpUtil().getSupport()){
			supportNum += 1;
			tv_dz.setTextColor(getResources().getColor(R.color.dz_press));
			tv_dz.setCompoundDrawablesWithIntrinsicBounds(null, getActivity().getResources().getDrawable(R.drawable.read_sc_p), null, null);
		}
		tv_dz.setText("点赞("+supportNum+")");
		initContentType();
	}

	/*
	 * 根据瓶子的不同类型加载不同的布局
	 */
	private void initContentType() {
		LayoutInflater inflater = LayoutInflater.from(act);
		View headView = inflater.inflate(R.layout.frament_wdpz_head, null);
		String cnt = bottleModel.getContent();
		cnt = (cnt == null ) ? "" : cnt;
		tv_cnt = (TextView) headView.findViewById(R.id.tv_cnt); //问题
		tv_cnt.setText(cnt);
		tv_da = (TextView) headView.findViewById(R.id.tv_da);	//答案
		tv_da.setText(getResources().getString(R.string.tip_answer));
		
		//添加头部
		mPullRefreshListView.getRefreshableView().addHeaderView(headView, null, false);
		
		//初始化评论
		adapter = new CommentAdapter(imageLoader, options,bottleModel.getBottleId());

		if (bottleModel.getBottleSort().equals("3000")) {
			if(commentModels != null) {
				//添加评论标签
				View tip = inflater.inflate(R.layout.comment_gap_tip, null);
				fl_tip = (RelativeLayout) tip.findViewById(R.id.rl_tip);
				TextView reviewNum = (TextView) tip.findViewById(R.id.tv_tip);
				reviewNum.setText("评论");
				mPullRefreshListView.getRefreshableView().addHeaderView(tip);
				
				mPullRefreshListView.setAdapter(adapter);
				adapter.addItemLast(commentModels);//判断评论数据
				reviewID = commentModels.get(commentModels.size()-1).getReviewId();
				
				CommentModel comment = MyApplication.getApp().getSpUtil().getBottleComment();// 已评论的评论
				if(comment != null && !TextUtils.isEmpty(comment.getUserId())) {
					tv_pl.setText("评论("+(model.getActionNum().getReviewNum() + 1)+")");
				}
			} else {
				View tip = inflater.inflate(R.layout.comment_gap_tip, null);
				fl_tip = (RelativeLayout) tip.findViewById(R.id.rl_tip);
				TextView reviewNum = (TextView) tip.findViewById(R.id.tv_tip);
				reviewNum.setText("评论");
				mPullRefreshListView.getRefreshableView().addHeaderView(tip);
				mPullRefreshListView.setAdapter(adapter);
				
				CommentModel comment = MyApplication.getApp().getSpUtil().getBottleComment();// 已评论的评论
				if(comment != null && !TextUtils.isEmpty(comment.getUserId())) {
					fl_tip.setVisibility(View.VISIBLE);
					tv_pl.setText("评论("+(model.getActionNum().getReviewNum() + 1)+")");
				} else {
					fl_tip.setVisibility(View.GONE);
				}
			}
			Message msg = Message.obtain();
			msg.what = FRESH;
			mHandler.sendMessage(msg);
		} else if (bottleModel.getBottleSort().equals("3001")) {
			mPullRefreshListView.setAdapter(adapter);
		}
	}
	
	
	/**
	 * 查询评论内容
	 * @param reviewId 最后一条评论的ID
	 * @param isLoadMore 是否加载更多
	 */
	public void commentsData(String reviewId,final boolean isLoadMore) {
		JsonObject jo = new JsonObject();
		jo.addProperty("bottleId", bottleModel.getBottleId());
		jo.addProperty("reviewId", reviewId);
		jo.addProperty("pageNum", PAGE_NUM);
		BottleRestClient.post("queryReview", getActivity(), jo,new AsyncHttpResponseHandler() {
					public void onStart() {
						super.onStart();
						//loadMoreView.setVisibility(View.VISIBLE);
						if(!isLoadMore)
							 showDialog("努力加载...", "加载失败...", 15*1000);
					}

					@Override
					public void onFinish() {
						super.onFinish();
						if(isLoadMore)
							mPullRefreshListView.onRefreshComplete();
						else
							closeDialog(mpDialog);
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							CommentMainModel model = gson.fromJson(str,CommentMainModel.class);
							if (model != null && !TextUtils.isEmpty(model.getCode())) {
								if ("0".equals(model.getCode())) {
									if(commentModels==null)
										commentModels = new LinkedList<CommentModel>();
									
									if(isLoadMore)
										adapter.addItemLast(model.getReviewList());//判断评论数据
									else
										adapter.reset(model.getReviewList());
									
									
									reviewID = model.getReviewList().get(model.getReviewList().size()-1).getReviewId();
									
									if("0".equals(model.getIsMore())) {
										isLast = true;
										if(isLoadMore)
											mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多");
									}else
										isLast = false;
									mPullRefreshListView.onRefreshComplete();
									Message msg = Message.obtain();
									msg.what = FRESH;
									mHandler.sendMessage(msg);
								}else{
									isLast = true;
									if(isLoadMore){
										mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多");
										mPullRefreshListView.onRefreshComplete();
									}
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
					public void onFailure(int statusCode, Header[] headers,byte[] responseBody, Throwable error) {
						if(isLoadMore)
							mPullRefreshListView.onRefreshComplete();
					}
				});
	}

	//AsyncMfLoader asyncMfLoader;
	//SpeechMgr sm;
	/**
	 * 按钮状态 0.播放状态 1.暂停状态
	 */
	int recorder_flag;
	Handler handler = new Handler(this);
	ImageButton ib_01;

	


	@Override
	public void onPause() {
		super.onPause();
		
		if (BottleVoicePlayClickListener.isPlaying && BottleVoicePlayClickListener.currentPlayListener != null) {
			// 停止瓶子语音播放
			BottleVoicePlayClickListener.currentPlayListener.stopPlayVoice();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1000) {
			if (resultCode == Activity.RESULT_OK) {
				act.setResult(Activity.RESULT_OK);
				act.finish();
			}
		}
	}

	public void transfermsg(CommentModel commentModel) {
		if (commentModel != null) {
			this.commentModel = commentModel;
			commentModels = new ArrayList<CommentModel>();
			commentModels.add(commentModel);
			adapter.addItemTop(commentModels);
			Message msg = Message.obtain();
			msg.what = FRESH;
			mHandler.sendMessage(msg);
		}
	}

	@Override
	public boolean onBackPressed() {
		FishingBottleActivity fba = (FishingBottleActivity) act;
		BaseFragment bf = Fragment_wdpz.newInstance(model);
		fba.changeContent(model, bf);
//		act.finish();
		return true;
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.bt_back:
				FishingBottleActivity fba = (FishingBottleActivity) act;
				BaseFragment bf = Fragment_wdpz.newInstance(model);
				fba.changeContent(model, bf);
//				act.finish();
//				showMsg("退出");
				break;
			case R.id.bt_pl:
//				showMsg("评论");
				if (UserManager.getInstance(act).getCurrentUser() == null) {
					Intent  intent = new Intent(act, LoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivity(intent);
					break;
				}
				if (menu_view.getVisibility() == 0) {
					menu_view.setVisibility(8);
					comment_view.setVisibility(0);
					imm.showSoftInputFromInputMethod(et_comments.getWindowToken(), 0);
				}
				parentId = "0";
				toUserId = "";
				break;
			case R.id.bt_return:
//				showMsg("返回");
				if (bt_return.getVisibility() == 0) {
					menu_view.setVisibility(0);
					comment_view.setVisibility(8);
					imm.hideSoftInputFromWindow(et_comments.getWindowToken(), 0);
				}
				break;
			case R.id.bt_send:
				view.setClickable(false);
				commentsContentn = et_comments.getText().toString().trim();
				if(check(commentsContentn)){
					doSubmit();
				}
				break;
			case R.id.bt_dz:
				//点赞
//				showMsg("点赞");
				doSupport();
				break;
			case R.id.bt_fx:
//				showMsg("分享");
				showShare(true, null);
				break;
		}
	}
	
	boolean ifSupport = false;
	private void doSupport(){
		ifSupport = MyApplication.getApp().getSpUtil().getSupport();
		if(ifSupport){
			showMsg("已经点过了~");
		}else{
			tv_dz.setText("已赞");
			
			ifSupport = true;
			MyApplication.getApp().getSpUtil().setSupport(true);

			JsonObject jo = new JsonObject();
			jo.addProperty("userId", UserManager.getInstance(act).getCurrentUserId());
			jo.addProperty("bottleId", bottleModel.getBottleId());
			
			String action = null;
			if(bottleModel.getFromOther()==1){
				action = "loveBottle";
			}else{
				action ="addAction";
				jo.addProperty("type", "8000");
			}
			BottleRestClient.post(action, act, jo, new AsyncHttpResponseHandler() {
						@Override
						public void onStart() {
							super.onStart();
						}

						public void onFinish() {
							super.onFinish();
						}

						@Override
						public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
							tv_dz.setText("赞( " + (actionNumModel.getDisSupportNum()+1) + " )");
							tv_dz.setTextColor(getResources().getColor(R.color.dz_press));
							tv_dz.setCompoundDrawablesWithIntrinsicBounds(null, getActivity().getResources().getDrawable(R.drawable.read_sc_p), null, null);
						}

						@Override
						public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
						}
					});
		}
	}
	
	
	/**
	 * 检查可以发送返回true
	 * @return
	 */
	private boolean check(String content){
		
		if (UserManager.getInstance(act).getCurrentUser() == null) {
			showMsg(getResources().getString(R.string.unlogin_msg));
			bt_send.setClickable(true);
			return false;
		}
		
		String gagTime = MyApplication.getApp().getSpUtil().getGagTime();
		if(!TextUtils.isEmpty(gagTime)){
			MoMaLog.e("禁言时间", gagTime);
			Date dateFrom = new Date(gagTime);
			Date dateTo = new Date(System.currentTimeMillis());//获取当前时间
			int day = MoMaUtil.getGapCount(dateTo, dateFrom);
			if(day>0){
				showMsg("经举报并核实，您的言论存在多次违规已被禁言,离解禁还有"+day+"天");
				bt_send.setClickable(true);
				return false;
			}else{
				MyApplication.getApp().getSpUtil().setGagTime("");
			}
		}
		
		if (commentsContentn.length() == 0) {
			showMsg("发送内容不能为空");
			bt_send.setClickable(true);
			return false;
		}
		
		//敏感词处理
		String dest = "";
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(content);
        dest = m.replaceAll("");
		
		SensitivewordFilter filter = MyApplication.getApp().getSensitiveFilter();
		Set<String> set = filter.getSensitiveWord(dest, 1);
		if(set.size()!=0){
			showMsg("提交内容含有敏感词，请修正");
			bt_send.setClickable(true);
			return false;
		}
		
		final long GAP_TIME = 1000*60*2;
		long mGapTime = MyApplication.getApp().getSpUtil().getGapTime();
		String bottleID = MyApplication.getApp().getSpUtil().getBottleID();
		if(bottleID.equals(bottleModel.getBottleId())){
			if(mGapTime < GAP_TIME ){
				showMsg(getResources().getString(R.string.comment_gap_tip));
				bt_send.setClickable(true);
				return false;
			}
		}
		
		return true;
	}
	
	// 使用快捷分享完成分享（请务必仔细阅读位于SDK解压目录下Docs文件夹中OnekeyShare类的JavaDoc）
		private void showShare(boolean silent, String platform) { 
			if (bottleModel.getContentType().equals("5000")) {	//文字
				textFrameShare(silent, platform);
			} else if (bottleModel.getContentType().equals("5001")) {
			} else if (bottleModel.getContentType().equals("5002")) {	//贺卡
				voiceFrameShare(silent, platform);
			} else if (bottleModel.getContentType().equals("5003")) {
			}
		}

		private void textFrameShare(boolean silent, String platform) {
			final OnekeyShare oks = new OnekeyShare();
			// oks.setNotification(R.drawable.ic_launcher,
			// getString(R.string.app_name));
			// address是接收人地址，仅在信息和邮件使用，否则可以不提供
			oks.setAddress("12345678901");
			// title标题，在印象笔记、邮箱、信息、微信（包括好友和朋友圈）、人人网和QQ空间使用，否则可以不提供
			oks.setTitle(MyConstants.SHARE_TITLE);
			// titleUrl是标题的网络链接，仅在人人网和QQ空间使用，否则可以不提供
			oks.setTitleUrl(MyConstants.SHARE_URL + bottleModel.getBottleId());
			// text是分享文本，所有平台都需要这个字段
			String cnt = bottleModel.getContent();
			if (!TextUtils.isEmpty(cnt)) {
				if (cnt.length() > 100) {
					cnt = cnt.substring(0, 100) + "...";
				}
			}
			oks.setText(cnt + " " + MyConstants.SHARE_URL + bottleModel.getBottleId() + " （来自#漂流瓶子#）");
			// imagePath是本地的图片路径，除Linked-In外的所有平台都支持这个字段
			// oks.setImagePath("/sdcard/pic.jpg");
			// imageUrl是图片的网络路径，新浪微博、人人网、QQ空间和Linked-In支持此字段
			oks.setImageUrl(MyConstants.BOTTLE01_URL);
			// url仅在微信（包括好友和朋友圈）中使用，否则可以不提供
			oks.setUrl(MyConstants.SHARE_URL + bottleModel.getBottleId());
			// filePath是待分享应用程序的本地路劲，仅在微信好友和Dropbox中使用，否则可以不提供
			// oks.setFilePath(MainActivity.TEST_IMAGE);
			// comment是我对这条分享的评论，仅在人人网和QQ空间使用，否则可以不提供
			oks.setComment("分享");
			// site是分享此内容的网站名称，仅在QQ空间使用，否则可以不提供
			oks.setSite(getString(R.string.app_name));
			oks.setSiteUrl(MyConstants.MYWEB_URL);
			// foursquare分享时的地方描述
			// foursquare分享时的地方名
			// oks.setVenueName("Share SDK");
			// oks.setVenueDescription("This is a beautiful place!");
			// 分享地纬度，新浪微博、腾讯微博和foursquare支持此字段
			// oks.setLatitude(23.056081f);
			// oks.setLongitude(113.385708f);
			oks.setSilent(silent);
			if (platform != null) {
				oks.setPlatform(platform);
			}

			// 去除注释，可令编辑页面显示为Dialog模式
			// oks.setDialogMode();

			// 去除注释，则快捷分享的操作结果将通过OneKeyShareCallback回调
			oks.setShareContentCustomizeCallback(this);
			oks.setCallback(this);

			// 去除注释，演示在九宫格设置自定义的图标
			// Bitmap logo = BitmapFactory.decodeResource(getResources(),
			// R.drawable.ic_launcher);
			// String label = getResources().getString(R.string.app_name);
			// OnClickListener listener = new OnClickListener() {
			// public void onClick(View v) {
			// String text = "Customer Logo -- Share SDK " +
			// ShareSDK.getSDKVersionName();
			// Toast.makeText(ChatActivity.this, text, Toast.LENGTH_SHORT).show();
			// oks.finish();
			// }
			// };
			// oks.setCustomerLogo(logo, label, listener);

			oks.show(act);
		}

		private void imageFrameShare(boolean silent, String platform) {
		}

		private void voiceFrameShare(boolean silent, String platform) {
			final OnekeyShare oks = new OnekeyShare();
			// address是接收人地址，仅在信息和邮件使用，否则可以不提供
			oks.setAddress("12345678901");
			// title标题，在印象笔记、邮箱、信息、微信（包括好友和朋友圈）、人人网和QQ空间使用，否则可以不提供
			oks.setTitle(MyConstants.SHARE_TITLE);
			// titleUrl是标题的网络链接，仅在人人网和QQ空间使用，否则可以不提供
			oks.setTitleUrl(bottleModel.getRedirectUrl());
			// text是分享文本，所有平台都需要这个字段
			String cnt = bottleModel.getContent();
			if (!TextUtils.isEmpty(cnt)) {
				if (cnt.length() > 100) {
					cnt = cnt.substring(0, 100) + "...";
				}
			}
			oks.setText(cnt + " " + bottleModel.getRedirectUrl() + " （来自#漂流瓶子#）");
			// oks.setImagePath("/sdcard/pic.jpg");
			// imageUrl是图片的网络路径，新浪微博、人人网、QQ空间和Linked-In支持此字段
			oks.setImageUrl(bottleModel.getShortPic());
			// url仅在微信（包括好友和朋友圈）中使用，否则可以不提供
			oks.setUrl(bottleModel.getRedirectUrl());
			// comment是我对这条分享的评论，仅在人人网和QQ空间使用，否则可以不提供
			oks.setComment("分享");
			// site是分享此内容的网站名称，仅在QQ空间使用，否则可以不提供
			oks.setSite(getString(R.string.app_name));
			oks.setSiteUrl(MyConstants.MYWEB_URL);
			oks.setSilent(silent);
			if (platform != null) {
				oks.setPlatform(platform);
			}

			// 去除注释，则快捷分享的操作结果将通过OneKeyShareCallback回调
			oks.setShareContentCustomizeCallback(this);
			oks.setCallback(this);
			oks.show(act);
		}

		/** 处理操作结果 */
		@Override
		public boolean handleMessage(Message msg) {
			MoMaLog.e("debug","action="+msg.arg1);
			switch (msg.arg1) {
			case 1:
				bottleModel.setHasAnswer(true);
				tv_da.setText("答案：" + bottleModel.getRemark());
				showMsg("分享成功");
				break;
			case 2:
				showMsg("分享失败");
				break;
			case 3:
				// closeDialog();
				showMsg("用户取消");
				break;
			}
			return false;
		}

		@Override
		public void onShare(Platform platform, ShareParams paramsToShare) {
			MoMaLog.e("debug","oper="+"onShare");
			if (Wechat.NAME.equals(platform.getName()) || WechatMoments.NAME.equals(platform.getName())) {
				String title = paramsToShare.getText();
				if (!TextUtils.isEmpty(title)) {
					title = title.substring(0, 10);
					paramsToShare.setTitle(title);
				}
			}
			bottleModel.setHasAnswer(true);
			tv_da.setText("答案：" + bottleModel.getRemark());
		}

		@Override
		public void onCancel(Platform platform, int action) {
			MoMaLog.e("debug","oper="+"onCancel");
			Message msg = Message.obtain();
			msg.arg1 = 3;
			msg.arg2 = action;
			msg.obj = platform;
			handler.sendMessage(msg);
		}

		@Override
		public void onComplete(Platform platform, int action, HashMap<String, Object> arg2) {
			MoMaLog.e("debug","oper="+"onComplete");
			Message msg = Message.obtain();
			msg.arg1 = 1;
			msg.arg2 = action;
			msg.obj = platform;
			handler.sendMessage(msg);
		}

		@Override
		public void onError(Platform platform, int action, Throwable arg2) {
			MoMaLog.e("debug","oper="+"onError");
			Message msg = Message.obtain();
			msg.arg1 = 2;
			msg.arg2 = action;
			msg.obj = platform;
			handler.sendMessage(msg);
		}
		
		//提交评论
		public void doSubmit() {
			
				JsonObject jo = new JsonObject();
				jo.addProperty("userId", UserManager.getInstance(act).getCurrentUserId());
				jo.addProperty("bottleId", bottleModel.getBottleId());
				jo.addProperty("reContent", commentsContentn);
				jo.addProperty("come", "6000");
				String action = null;
				if(bottleModel.getFromOther()==1){
					action= "addBottleReview";
					jo.addProperty("parentId", parentId);
					jo.addProperty("toUserId", toUserId);
				}else{
					action ="addReview";
					jo.addProperty("contentType", "5000");
				}
				

				BottleRestClient.post(action, act, jo, new AsyncHttpResponseHandler() {
							@Override
							public void onStart() {
								super.onStart();
								footView.setVisibility(View.VISIBLE);
								bt_send.setVisibility(View.GONE);
							}

							public void onFinish() {
								super.onFinish();
								footView.setVisibility(View.GONE);
								bt_send.setVisibility(View.VISIBLE);
								bt_send.setClickable(true);
								menu_view.setVisibility(View.VISIBLE);
								comment_view.setVisibility(View.GONE);
							}

							@Override
							public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

								String str = new String(responseBody);
								if (!TextUtils.isEmpty(str)) {
									Gson gson = new Gson();
									DealBottleModel model = gson.fromJson(str, DealBottleModel.class);
									if (model != null && !TextUtils.isEmpty(model.getCode())) {
										if ("0".equals(model.getCode())) {
											User user = UserManager.getInstance(act).getCurrentUser();
											CommentModel commentModel = new CommentModel();
											commentModel.setNickName(user.getNickName());
											commentModel.setReContent(commentsContentn);
											commentModel.setFavourNum("0");
											commentModel.setHeadImg(user.getHeadImg());
											commentModel.setReTime(new java.util.Date());
											commentModel.setUserId(user.getUserId());
											commentModel.setCity(user.getCity());
											transfermsg(commentModel);
											showMsg("评论成功");
											fl_tip.setVisibility(View.VISIBLE);
											MyApplication.getApp().getSpUtil().saveBottleComment(commentModel);
											MyApplication.getApp().getSpUtil().updateCommentTime(System.currentTimeMillis());
											MyApplication.getApp().getSpUtil().updateBottleID(bottleModel.getBottleId());
											et_comments.setText("");
										} else {
											showMsg(model.getMsg());
										}
										imm.hideSoftInputFromWindow(et_comments.getWindowToken(), 0);
									} else {
										showMsg("服务器繁忙");
									}
								} else {
									showMsg("服务器繁忙");
								}
							}

							@Override
							public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
								showMsg("发送失败");
							}
						});
		}

}