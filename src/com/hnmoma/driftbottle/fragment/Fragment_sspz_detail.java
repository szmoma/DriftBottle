package com.hnmoma.driftbottle.fragment;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.easemob.chat.EMMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hnmoma.driftbottle.FishingBottleActivity;
import com.hnmoma.driftbottle.ImageFrameShowActivity;
import com.hnmoma.driftbottle.LoginActivity;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.SingleImageFrameShowActivity;
import com.hnmoma.driftbottle.VzoneActivity;
import com.hnmoma.driftbottle.WebFrameWithCacheActivity;
import com.hnmoma.driftbottle.adapter.CommentAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.itfc.BottleVoicePlayClickListener;
import com.hnmoma.driftbottle.model.ActionNumModel;
import com.hnmoma.driftbottle.model.Attachment;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.CommentMainModel;
import com.hnmoma.driftbottle.model.CommentModel;
import com.hnmoma.driftbottle.model.DealBottleModel;
import com.hnmoma.driftbottle.model.PickBottleModel;
import com.hnmoma.driftbottle.model.ReviewListModel;
import com.hnmoma.driftbottle.model.SinaTopicModel;
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
public class Fragment_sspz_detail extends BaseFragment implements OnClickListener, PlatformActionListener, ShareContentCustomizeCallback, Callback{
	private final int MORE = 1;	//加载更多的标志
	private final int FINISH = 0; //没有更多的内容标志
	private final int FRESH = 2;//刷新ListView
	
	RelativeLayout fl_tip;
	CircularImage iv_head;
	ImageView iv;
	TextView tv_name;
	TextView tv_sf;
	TextView tv_dq;
	TextView tv_cnt;
	TextView tv_da;
	TextView tvConstellation; //星座
	
	TextView tv_pl;
	TextView tv_dz;

	PullToRefreshListView mPullRefreshListView;

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
	private  String   reviewID; //评论的最后一条记录
	String userId;
	
	LinearLayout menu_view;//底部导航栏
	RelativeLayout comment_view;
	EditText et_comments;
	ImageButton bt_send;
	InputMethodManager imm;
	ImageButton bt_return;
	String commentsContentn;//评论的内容
	View footView;
	String parentId;
	String toUserId;
	
	private boolean isLast;//是否是最后一项
	
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
				queryCommentsData(reviewID,true);
			case FRESH:
				adapter.notifyDataSetChanged();
				if(commentModels==null||commentModels.isEmpty()){
					mPullRefreshListView.setMode(Mode.DISABLED);
				}else{
					mPullRefreshListView.setMode(Mode.PULL_FROM_END);
				}
				break;
			default:
				break;
			}
		};
	};
	
	public static BaseFragment newInstance(PickBottleModel model) {
		Fragment_sspz_detail fragment = new Fragment_sspz_detail();
		Bundle bundle = new Bundle();
		bundle.putSerializable("PickBottleModel", model);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_sspz_detail, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.view_center);
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
		//review
		menu_view = (LinearLayout) findViewById(R.id.menu_view);
		comment_view = (RelativeLayout) findViewById(R.id.comment_view);
		bt_send = (ImageButton) findViewById(R.id.bt_send);
		bt_send.setOnClickListener(this);
		footView = findViewById(R.id.footview);
		et_comments = (EditText) findViewById(R.id.et_comments);
		bt_return = (ImageButton) findViewById(R.id.bt_return);
		bt_return.setOnClickListener(this);
		
		Button bt_back = (Button) findViewById(R.id.bt_back);
		bt_back.setOnClickListener(this);
		View bt_pl = findViewById(R.id.bt_pl);
		bt_pl.setOnClickListener(this);
		View bt_dz = findViewById(R.id.bt_dz);
		bt_dz.setOnClickListener(this);
		View bt_fx = findViewById(R.id.bt_fx);
		bt_fx.setVisibility(View.GONE);
		bt_fx.setOnClickListener(this);
		
		Bundle bundle = getArguments();
		model = (PickBottleModel) bundle.getSerializable("PickBottleModel");
		bottleModel = model.getBottleInfo();
		commentModels = model.getCommentModels();// 评论集合
		actionNumModel = model.getActionNum();
		
		tv_pl = (TextView) findViewById(R.id.tv_pl);
		tv_dz = (TextView) findViewById(R.id.tv_dz);
		
		initData();
		initContentType();
		
		mPullRefreshListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Object obj = parent.getItemAtPosition(position) ;
				ReviewListModel model = null;
				if(obj instanceof ReviewListModel){
					model = (ReviewListModel) obj;
				}else {
					return ;
				}
				parentId = model.getParentId();
				toUserId = model.getUserId();
				if(toUserId.equals(UserManager.getInstance(act).getCurrentUserId())) {
					showMsg("不能回复自己");
					return;
				}
				menu_view.setVisibility(View.GONE);
				comment_view.setVisibility(View.VISIBLE);
				et_comments.setText("");
				et_comments.setHint("回复 " + model.getNickName());
				et_comments.requestFocus();
				imm.showSoftInputFromInputMethod(et_comments.getWindowToken(), 0);
			}
		});
	}
	
	

	private void initData() {
		userId = bottleModel.getUserInfo().getUserId();
		headImg = bottleModel.getUserInfo().getHeadImg();
		nickName = bottleModel.getUserInfo().getNickName();
		idtype = bottleModel.getUserInfo().getIdentityType();
		province = bottleModel.getUserInfo().getProvince();
		constellation = bottleModel.getUserInfo().getConstell();
		province = TextUtils.isEmpty(province) ? "" : province;
		city = bottleModel.getUserInfo().getCity();
		city = TextUtils.isEmpty(city) ? "" : city;
		cnt = bottleModel.getContent();
		cnt = cnt == null ? "" : cnt;
		age = bottleModel.getUserInfo().getAge()+"";
		isVip = bottleModel.getUserInfo().getIsVIP();
		menu_view.setVisibility(View.VISIBLE);
		
		if(commentModels == null || actionNumModel == null) {
			
		} else {
			tv_pl.setText("评论("+model.getActionNum().getReviewNum()+")");
			MoMaLog.e("评论数", model.getActionNum().getReviewNum() +"");
			int supportNum = model.getActionNum().getSupportNum();
			if(MyApplication.getApp().getSpUtil().getSupport()){
				supportNum += 1;
				tv_dz.setTextColor(getResources().getColor(R.color.dz_press));
				tv_dz.setCompoundDrawablesWithIntrinsicBounds(null, getActivity().getResources().getDrawable(R.drawable.read_sc_p), null, null);
			}
			tv_dz.setText("点赞("+supportNum+")");
		}
		
	}

	@SuppressLint("NewApi")
	private void initBottleHead(View view) {
		iv_head = (CircularImage) view.findViewById(R.id.iv_userhead);
		tv_name = (TextView) view.findViewById(R.id.tv_name);
		tv_sf = (TextView) view.findViewById(R.id.tv_sf);
		tv_dq = (TextView) view.findViewById(R.id.tv_dq);
		tvConstellation = (TextView) view.findViewById(R.id.tv_time);
		
		//ll_action = (LinearLayout) view.findViewById(R.id.ll_action);
		tv_sc = (TextView) view.findViewById(R.id.tv_sc);
		tv_rd = (TextView) view.findViewById(R.id.tv_rd);
		ImageView iv_vip = (ImageView) view.findViewById(R.id.iv_vip);
		
		if(isVip == 0) {
			iv_vip.setBackgroundResource(R.drawable.ic_vip_not);
			tv_name.setTextColor(getResources().getColor(R.color.bottlecontent_username));
		} else {
			iv_vip.setBackgroundResource(R.drawable.ic_vip);
			tv_name.setTextColor(getResources().getColor(R.color.username_vip));
		}

		imageLoader.displayImage(headImg, iv_head, options);
		iv_head.setOnClickListener(this);
		tv_name.setText(nickName);
		String[] identity = new String[2];
		if (TextUtils.isEmpty(idtype)) {
			identity[0] = "男";
			identity[1] = "m";
		} else {
			identity = getIdentityByCode(idtype);
		}
		Drawable drawableBgSex = null	;//性别背景
		Drawable drawable;
		if("m".equalsIgnoreCase(identity[1])){
			drawableBgSex = getActivity().getResources().getDrawable(R.drawable.manbg);
			drawable = getResources().getDrawable(R.drawable.icon_male_32);
		}else if("f".equalsIgnoreCase(identity[1])){
			drawableBgSex = getActivity().getResources().getDrawable(R.drawable.womanbg);
			drawable = getResources().getDrawable(R.drawable.icon_female_32);
		}else {
			drawableBgSex = getActivity().getResources().getDrawable(R.drawable.womanbg);
			drawable = getResources().getDrawable(R.drawable.icon_female_32);
		}
		if(android.os.Build.VERSION.SDK_INT >=android.os.Build.VERSION_CODES.JELLY_BEAN)
			tv_sf.setBackground(drawableBgSex);
		else
			tv_sf.setBackgroundDrawable(drawableBgSex);
		tv_sf.setText(age);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),drawable.getMinimumHeight());
		tv_sf.setCompoundDrawables(drawable, null, null, null);
		tv_sf.setPadding(MoMaUtil.dip2px(act, 5), 0, MoMaUtil.dip2px(act, 5), 0);
		if (constellation != null) {
			tvConstellation.setText(constellation);
		} 
		tv_dq.setText(province + " " + city);
	}

	String headImg = null;
	String nickName = null;
	String idtype = null;
	String constellation = null; //星座字符串
	String province = null;
	String city = null;
	String cnt = null;
	String age = null;
	int isVip = 0;
	/*
	 * 根据瓶子的不同类型加载不同的布局
	 */
	private void initContentType() {
		LayoutInflater inflater = LayoutInflater.from(act);
		View view = null;

		if (bottleModel.getContentType().equals("5001")) {
			view = inflater.inflate(R.layout.frament_frame_image, null);

			iv = (ImageView) view.findViewById(R.id.iv);
			tv_cnt = (TextView) view.findViewById(R.id.tv_cnt);
			imageLoader.displayImage(bottleModel.getShortPic(), iv, options);
			tv_cnt.setText(cnt);
			iv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					int num = bottleModel.getPicNum();
					String url = bottleModel.getUrl();
					// 是否跳转(0表示不跳转，1表示跳转)
					List<Attachment> subList;

						if (num > 1) {
							subList = bottleModel.getSubList();
							Intent intent = new Intent(act, ImageFrameShowActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
							intent.putExtra("imageUrls", (Serializable) subList);
							startActivity(intent);
						} else {
							Intent intent = new Intent(act, SingleImageFrameShowActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
							intent.putExtra("imageUrl", url);
							startActivity(intent);
						}
					
				}
			});
		}else if (bottleModel.getContentType().equals("5005")) {//web
			view = inflater.inflate(R.layout.frament_frame_image, null);

			iv = (ImageView) view.findViewById(R.id.iv);
			tv_cnt = (TextView) view.findViewById(R.id.tv_cnt);
			imageLoader.displayImage(bottleModel.getShortPic(), iv, options);
			tv_cnt.setText(cnt);
			iv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String redirectUrl = bottleModel.getRedirectUrl();

					Intent intent = new Intent(act, WebFrameWithCacheActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("webUrl", redirectUrl);
					startActivity(intent);
				}
			});
		}else if (bottleModel.getContentType().equals("5004")) {
			view = inflater.inflate(R.layout.frament_frame_voice, null);
			iv = (ImageView) view.findViewById(R.id.iv);
			tv_cnt = (TextView) view.findViewById(R.id.tv_cnt);
			if (!TextUtils.isEmpty(cnt)) {
				tv_cnt.setVisibility(View.VISIBLE);
				tv_cnt.setText(cnt);
			}

			ImageButton ib_01 =  (ImageButton) view.findViewById(R.id.ib_01);
			ProgressBar spinner = (ProgressBar)view.findViewById(R.id.loading);
			
			EMMessage msg = EMMessage.createSendMessage(EMMessage.Type.TXT);
			msg.setMsgId("10000");
			
			ib_01.setOnClickListener(new BottleVoicePlayClickListener(act, msg, bottleModel, ib_01, spinner));
		} else {
			view = inflater.inflate(R.layout.frament_frame_text, null);

			tv_cnt = (TextView) view.findViewById(R.id.tv_cnt);
			tv_cnt.setText(cnt);
		}

		// init bottleHead
		initBottleHead(view);
		mPullRefreshListView.getRefreshableView().addHeaderView(view, null, false);
		//初始化评论
		adapter = new CommentAdapter(imageLoader, options,bottleModel.getBottleId());
		
		if(commentModels != null) {
			View tip = inflater.inflate(R.layout.comment_gap_tip, null);
			fl_tip = (RelativeLayout) tip.findViewById(R.id.rl_tip);
			if(model.getActionNum().getReviewNum() > 0)
				fl_tip.setVisibility(View.VISIBLE);
			else
				fl_tip.setVisibility(View.GONE);
			mPullRefreshListView.getRefreshableView().addHeaderView(tip);
			
			mPullRefreshListView.setAdapter(adapter);
			adapter.addItemLast(commentModels);//判断评论数据
			Message msg = Message.obtain();
			msg.what = FRESH;
			mHandler.sendMessage(msg);
			
			reviewID = commentModels.get(commentModels.size()-1).getReviewId()== null? "0" : commentModels.get(commentModels.size()-1).getReviewId();
			
			CommentModel recomment = MyApplication.getApp().getSpUtil().getBottleComment();// 已评论的评论
			if(recomment != null && !TextUtils.isEmpty(recomment.getUserId())) {
				tv_pl.setText("评论("+(model.getActionNum().getReviewNum() + 1)+")");
				MoMaLog.e("评论数", model.getActionNum().getReviewNum() +"");
				fl_tip.setVisibility(View.VISIBLE);
			}
			tv_dz.setText("点赞("+model.getActionNum().getSupportNum()+")");
		} else {
			reviewID = "0";
			View tip = inflater.inflate(R.layout.comment_gap_tip, null);
			fl_tip = (RelativeLayout) tip.findViewById(R.id.rl_tip);
			TextView reviewNum = (TextView) tip.findViewById(R.id.tv_tip);
			reviewNum.setText("评论");
			if(model.getActionNum().getReviewNum() > 0)
				fl_tip.setVisibility(View.VISIBLE);
			else
				fl_tip.setVisibility(View.GONE);
			
			mPullRefreshListView.getRefreshableView().addHeaderView(tip);
			mPullRefreshListView.setAdapter(adapter);
			
			CommentModel recomment = MyApplication.getApp().getSpUtil().getBottleComment();// 已评论的评论
			if(recomment != null && !TextUtils.isEmpty(recomment.getUserId())) {
				fl_tip.setVisibility(View.VISIBLE);
				tv_pl.setText("评论("+(model.getActionNum().getReviewNum() + 1)+")");
				MoMaLog.e("评论数", model.getActionNum().getReviewNum() +"");
			}
			queryCommentsData(reviewID, false);
		}
		
	}
	/**
	 * 查询评论内容
	 * @param reviewId 最后一条评论的ID
	 * @param isLoadMore 是否加载更多。如果isLoadMore是true，则加载更多
	 */
	public void queryCommentsData(String reviewId,final boolean isLoadMore) {
		JsonObject jo = new JsonObject();
		jo.addProperty("bottleId", bottleModel.getBottleId());
		jo.addProperty("reviewId", reviewId);
		jo.addProperty("pageNum", PAGE_NUM);
		BottleRestClient.post("queryReview", getActivity(), jo,new AsyncHttpResponseHandler() {
					public void onStart() {
						super.onStart();
					}

					@Override
					public void onFinish() {
						super.onFinish();
						if(isLoadMore) 
							mPullRefreshListView.onRefreshComplete();
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,byte[] responseBody) {
						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							CommentMainModel model = gson.fromJson(str,CommentMainModel.class);
							if (model != null && !TextUtils.isEmpty(model.getCode())) {
								if ("0".equals(model.getCode())) {
									if(commentModels==null)
										commentModels = new LinkedList<CommentModel>();
									commentModels.addAll(model.getReviewList());
									if(isLoadMore){
										adapter.addItemLast(model.getReviewList());//判断评论数据
									}else{
										adapter.reset(model.getReviewList());//判断评论数据
									}
									reviewID = model.getReviewList().get(model.getReviewList().size()-1).getReviewId();
									
									if("0".equals(model.getIsMore())) { //最后一条
										isLast = true;
										if(isLoadMore)
											mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多");
									}else
										isLast = false;
									if(isLoadMore)
										mPullRefreshListView.onRefreshComplete();
									Message msg = Message.obtain();
									msg.what = FRESH;
									mHandler.sendMessage(msg);
								}else {
									if(isLoadMore){
										mPullRefreshListView.getLoadingLayoutProxy().setPullLabel("没有更多");
										mPullRefreshListView.onRefreshComplete();
									}
									showMsg("没有评论了");
									Message msg = Message.obtain();
									msg.what = FRESH;
									mHandler.sendMessage(msg);
								}
							}
						} else {
							showMsg("服务器繁忙");
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						if(isLoadMore)
							mPullRefreshListView.onRefreshComplete();
					}
				});
	}
	/**
	 * 按钮状态 0.播放状态 1.暂停状态
	 */
	int recorder_flag;
	Handler handler = new Handler();
	ImageButton ib_01;

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
	/**
	 * 把评论添加到评论列表中
	 * @param commentModel
	 */
	public void transfermsg(CommentModel commentModel) {
		this.commentModel = commentModel;
		List<CommentModel> tmp = new ArrayList<CommentModel>();
		tmp.add(commentModel);
		adapter.addItemTop(tmp);
		
		
		if(commentModels==null)
			commentModels = new LinkedList<CommentModel>();
		if(commentModels.isEmpty()){
			commentModels.add(commentModel);
		}else{
			commentModels.add(0, commentModel);
		}
		
		Message msg = Message.obtain();
		msg.what = FRESH;
		mHandler.sendMessage(msg);
	}

	@Override
	public boolean onBackPressed() {
		FishingBottleActivity fba = (FishingBottleActivity) act;
		BaseFragment bf = Fragment_sspz.newInstance(model);
		fba.changeContent(model, bf);
		return true;
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.bt_back:
				FishingBottleActivity fba = (FishingBottleActivity) act;
				BaseFragment bf = Fragment_sspz.newInstance(model);
				fba.changeContent(model, bf);
				imm.hideSoftInputFromWindow(et_comments.getWindowToken(), 0);
				break;
			case R.id.bt_pl:
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
				doSupport();
				break;
			case R.id.bt_fx:
				getSinaTopic();
				showShare(true, null);
				break;
			case R.id.iv_userhead:
				//TODO 进入用户空间
				Intent intent = new Intent(getActivity(), VzoneActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", userId);
				if(userId.equals(UserManager.getInstance(getActivity()).getCurrentUserId())){
					intent.putExtra("visitUserId", userId);
					intent.putExtra("identityflag", 0);
				}else{
					intent.putExtra("identityflag", 1);
					intent.putExtra("visitUserId", UserManager.getInstance(getActivity()).getCurrentUserId());
					
				}
				startActivity(intent);
				break;
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
		
		//对于主题，用户只允许评论一次
		if(commentModels!=null)
			for(CommentModel review :commentModels){
				if(UserManager.getInstance(getActivity()).getCurrentUserId().equals(review.getUserId())){
					showMsg("已经评论过了");
					return false;
				}
			}
				
		return true;
	}
	
	
	
	
	// 使用快捷分享完成分享（请务必仔细阅读位于SDK解压目录下Docs文件夹中OnekeyShare类的JavaDoc）
		private void showShare(boolean silent, String platform) {
			OnekeyShare oks = new OnekeyShare();
			oks.disableSSOWhenAuthorize();
			oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
			oks.setAddress("12345678901");
			oks.setTitle(MyConstants.SHARE_TITLE);
			oks.setTitleUrl(MyConstants.SHARE_URL + bottleModel.getBottleId());
			oks.setText(bottleModel.getContent());
			oks.setUrl(MyConstants.SHARE_URL + bottleModel.getBottleId());
			oks.setComment("分享");
			oks.setSilent(silent);
			oks.setSite(getString(R.string.app_name));
			oks.setSiteUrl(MyConstants.MYWEB_URL);
			oks.setCallback(this);
			oks.setShareContentCustomizeCallback(this);
			oks.show(act);
			
		}

		String sinaTopic;
		String sinaImageUrl;
		final String WebSite = " http://www.52plp.com ";
		private void getSinaTopic() {
			//获取新浪话题
			JsonObject obj = new JsonObject();
			obj.addProperty("bottleId", bottleModel.getBottleId());
			BottleRestClient.post("querySinaShare", act, obj,  new AsyncHttpResponseHandler(){

				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					String response = new String(responseBody);
					Gson gson = new Gson();
					SinaTopicModel baseModel = gson.fromJson(response, SinaTopicModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode()) ) {
						if("0".equals(baseModel.getCode())) {
							sinaTopic = baseModel.getTopicContent();
							sinaImageUrl = baseModel.getUrl();
//							showMsg(sinaImageUrl);
//							showMsg(sinaTopic);
							if(TextUtils.isEmpty(sinaImageUrl)) {
								sinaTopic = "";
							} else {
								sinaTopic = "#" +sinaTopic + "#";
							}
						}else{
							showMsg(baseModel.getMsg());
						}
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					sinaTopic = "";
					sinaImageUrl = "http://52plp.com/share/sina.jpg";
					//shareToWeibo(paramsToShare);
				}
				
				@Override
				public void onFinish() {
					super.onFinish();
				}
			});
	}
		// 分享到微博
		public void shareToWeibo(ShareParams paramsToShare) {
			//贺卡瓶子
			if(bottleModel.getContentType().equals("5002")) {
				if(bottleModel.getIsRedirect() == 1 ) {
					paramsToShare.setText(bottleModel.getContent()+ bottleModel.getRedirectUrl() +" "+" #漂流瓶子# " + sinaTopic);
				} else {
					paramsToShare.setText(bottleModel.getContent()+ MyConstants.SHARE_URL + bottleModel.getBottleId() +" "+" #漂流瓶子# " + sinaTopic);
				}
				paramsToShare.setImageUrl(bottleModel.getShortPic());
				return;
			}
			
			String sysType = bottleModel.getSysType();
			if("13".equals(sysType)) {
				paramsToShare.setText("女神出没，请注意！" + WebSite +" "+" #漂流瓶子# " + sinaTopic);
			}  else if("11".equals(sysType)) {
				      if (bottleModel.getContent().length() > 100 || TextUtils.isEmpty(bottleModel.getContent()))  {
				    	  paramsToShare.setText("哈哈哈我也是微醺~ " + WebSite +" "+"#漂流瓶子#" + sinaTopic);
				      } else {
				    	  paramsToShare.setText(bottleModel.getContent() + WebSite +" "+"#漂流瓶子#" + sinaTopic);
				      }
				} else if("2".equals(sysType) || "7".equals(sysType)) {
					paramsToShare.setText("一语中的 " + WebSite +" "+"#漂流瓶子#" + sinaTopic);
				} else if("9".equals(sysType)) {
					
					if("5000".equals(bottleModel.getContentType())) {
						paramsToShare.setText("朕已笑死，有事烧纸 " + WebSite +" "+"#漂流瓶子#" + sinaTopic);
					} else if("5001".equals(bottleModel.getContentType())) {
						if (bottleModel.getContent().length() >100 || TextUtils.isEmpty(bottleModel.getContent()))  {
					    	  paramsToShare.setText("啥也不说了，戳图！ " + WebSite +" "+"#漂流瓶子#" + sinaTopic);
					      } else {
					    	  paramsToShare.setText(bottleModel.getContent() + WebSite +" "+"#漂流瓶子#" + sinaTopic);
					      }
					}
				} else if("10".equals(sysType)) {
					paramsToShare.setText("这题不会，帮我看看？ " + WebSite +" "+"#漂流瓶子#" + sinaTopic);
				} 
			paramsToShare.setImageUrl((sinaImageUrl));
			
		}
		
		// 分享到微信朋友圈
		public void shareToWeChatFriend(ShareParams paramsToShare) {
			// TODO Auto-generated method stub
			String sysType = bottleModel.getSysType();
			paramsToShare.setShareType(Platform.SHARE_WEBPAGE);
			if("13".equals(sysType)) {
				paramsToShare.setTitle("这个妹纸是我的菜");
				paramsToShare.setImageUrl(bottleModel.getShortPic());
			}  else if("11".equals(sysType)) {
				      if (bottleModel.getContent().length() > 25 || TextUtils.isEmpty(bottleModel.getContent()))  {
				    	  paramsToShare.setTitle("朕已笑死，有事烧纸 ");
				    	  paramsToShare.setImageUrl(bottleModel.getShortPic());
				      } else {
				    	  paramsToShare.setTitle(bottleModel.getContent());
				    	  paramsToShare.setImageUrl(bottleModel.getShortPic());
				      }
				} else if("2".equals(sysType) || "7".equals(sysType)) {
					paramsToShare.setTitle("说得真好， " + bottleModel.getContent());
					paramsToShare.setImageUrl("http://52plp.com/share/share"+sysType+".jpg");
				} else if("9".equals(sysType)) {
					if("5000".equals(bottleModel.getContentType())) {
						paramsToShare.setTitle("笑到飙泪，" + bottleModel.getContent());
						paramsToShare.setImageUrl("http://52plp.com/share/share9.jpg");
					} else if("5001".equals(bottleModel.getContentType())) {
						paramsToShare.setTitle("哈哈哈，真是醉了… " + bottleModel.getContent());
						paramsToShare.setImageUrl(bottleModel.getShortPic());
					}
				} else if("10".equals(sysType)) {
					paramsToShare.setTitle("你知道答案吗？" );
					paramsToShare.setImageUrl("http://52plp.com/share/share10.jpg");
				} else if("12".equals(sysType)) {// 贺卡
					if (bottleModel.getContent().length() > 25 || TextUtils.isEmpty(bottleModel.getContent()))  {
						paramsToShare.setTitle(bottleModel.getContent().substring(0, 25)+ "...");
					}  else {
						paramsToShare.setTitle(bottleModel.getContent());
					}
					paramsToShare.setImageUrl(bottleModel.getShortPic());
					if(bottleModel.getIsRedirect() == 1 ) {
						paramsToShare.setUrl(bottleModel.getRedirectUrl());
						paramsToShare.setTitleUrl(bottleModel.getRedirectUrl());
					}
				}
		}
		
		// 分享到其他平台
		public void shareToOthers(ShareParams paramsToShare) {
			// TODO Auto-generated method stub
			String sysType = bottleModel.getSysType();
			paramsToShare.setShareType(Platform.SHARE_WEBPAGE);
			if("13".equals(sysType)) {
				paramsToShare.setTitle("女神出没!");
				paramsToShare.setImageUrl(bottleModel.getShortPic());
			}  else if("11".equals(sysType)) {
				    	  paramsToShare.setTitle("画面太美");
				    	  paramsToShare.setImageUrl(bottleModel.getShortPic());
				} else if("2".equals(sysType) || "7".equals(sysType)) {
					paramsToShare.setTitle("你同意不？ ");
					paramsToShare.setImageUrl("http://52plp.com/share/share"+sysType+".jpg");
				} else if("9".equals(sysType)) {
					if("5000".equals(bottleModel.getContentType())) {
						paramsToShare.setTitle("转发测笑点");
						paramsToShare.setImageUrl("http://52plp.com/share/share9.jpg");
					} else if("5001".equals(bottleModel.getContentType())) {
						paramsToShare.setTitle("笑shi我了");
						paramsToShare.setImageUrl(bottleModel.getShortPic());
					}
				} else if("10".equals(sysType)) {
					paramsToShare.setTitle("脑洞大开" );
					paramsToShare.setImageUrl("http://52plp.com/share/share10.jpg");
				} else if("12".equals(sysType)) {
					paramsToShare.setTitle(bottleModel.getContent());
					paramsToShare.setImageUrl(bottleModel.getShortPic());
					if(bottleModel.getIsRedirect() == 1 ) {
						paramsToShare.setTitleUrl(bottleModel.getRedirectUrl());
						paramsToShare.setUrl(bottleModel.getRedirectUrl());
					}
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
			switch (msg.arg1) {
			case 1:
				// closeDialog();
				showMsg("分享成功");
				break;
			case 2:
				// closeDialog();
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
			//TODO  根据不同的平台
			if(SinaWeibo.NAME.equals(platform.getName())){
				// 分享到新浪微博
				shareToWeibo(paramsToShare);
			}  else if(WechatMoments.NAME.equals(platform.getName())) {
				// 分享到微信朋友圈
					shareToWeChatFriend(paramsToShare);
//				}
			}  else {
				// 分享到其他平台
					shareToOthers(paramsToShare);
//				}
			}
		}

		@Override
		public void onCancel(Platform platform, int action) {
			Message msg = new Message();
			msg.arg1 = 3;
			msg.arg2 = action;
			msg.obj = platform;
			handler.sendMessage(msg);
		}

		@Override
		public void onComplete(Platform platform, int action, HashMap<String, Object> arg2) {
			Message msg = new Message();
			msg.arg1 = 1;
			msg.arg2 = action;
			msg.obj = platform;
			handler.sendMessage(msg);
		}

		@Override
		public void onError(Platform platform, int action, Throwable arg2) {
			Message msg = new Message();
			msg.arg1 = 2;
			msg.arg2 = action;
			msg.obj = platform;
			handler.sendMessage(msg);
		}
		
		boolean ifSupport = false;
		private void doSupport(){
			ifSupport = MyApplication.getApp().getSpUtil().getSupport();
			if(ifSupport){
				showMsg("已经点过了~");
			}else{
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
								tv_dz.setText("点赞("+(model.getActionNum().getSupportNum()+1)+")");
								tv_dz.setTextColor(getResources().getColor(R.color.dz_press));
								tv_dz.setCompoundDrawablesWithIntrinsicBounds(null, getActivity().getResources().getDrawable(R.drawable.read_sc_p), null, null);
							}

							@Override
							public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
							}
						});
			}
		}
		
		//提交评论
		public void doSubmit() {
			
				JsonObject jo = new JsonObject();
				jo.addProperty("userId", UserManager.getInstance(act).getCurrentUserId());
				jo.addProperty("bottleId", bottleModel.getBottleId());
				jo.addProperty("reContent", commentsContentn);
				jo.addProperty("come", "6000");
				String action= null;
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
											
											tv_pl.setText("评论("+(actionNumModel.getReviewNum()+1)+")");
											MoMaLog.e("评论数", (actionNumModel.getReviewNum()+1) +"");
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