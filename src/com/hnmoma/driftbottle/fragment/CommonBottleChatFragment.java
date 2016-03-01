package com.hnmoma.driftbottle.fragment;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hnmoma.driftbottle.ImageFrameShowActivity;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.SingleImageFrameShowActivity;
import com.hnmoma.driftbottle.WebFrameWithCacheActivity;
import com.hnmoma.driftbottle.itfc.FrameShowCallBack;
import com.hnmoma.driftbottle.model.Attachment;
import com.hnmoma.driftbottle.model.AttachmentDao;
import com.hnmoma.driftbottle.model.Bottle;
import com.hnmoma.driftbottle.model.Chat;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.way.ui.emoji.EmojiTextView;

import de.greenrobot.dao.query.QueryBuilder;

/**
 *
 */
public class CommonBottleChatFragment extends BaseFragment {

	ImageView iv_head;
	ImageView iv;
	// Button bt_ts;
	TextView tv_name;
	TextView tv_sf;
	TextView tv_dq;
	EmojiTextView tv_cnt;
	TextView tv_da;
	TextView tv_time;

	LinearLayout rl_c;
	// LinearLayout view_bottle_head;
	LinearLayout ll_action;
	TextView tv_sc;
	TextView tv_rd;

	SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");

	Chat chat;
	Bottle bottleModel;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("bottleModel", bottleModel);
		outState.putSerializable("chat", chat);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			bottleModel = (Bottle) savedInstanceState
					.getSerializable("bottleModel");
			chat = (Chat) savedInstanceState.getSerializable("chat");
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.system_bottle_base, null);
		// View view =initContentType();
		rl_c = (LinearLayout) view.findViewById(R.id.rl_c);
		return view;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FrameShowCallBack fsc = (FrameShowCallBack) act;
		Object[] objects = fsc.onFragmentInit(this);
		bottleModel = (Bottle) objects[1];
		chat = (Chat) objects[2];
		initContentType();
		initData();
	}

	String headImg = null;
	String nickName = null;
	String idtype = null;
	Date ChatTime = null;
	String province = null;
	String city = null;
	String cnt = null;

	public void initData() {
		headImg = chat.getHeadImg();
		nickName = chat.getNickName();
		idtype = chat.getIdentityType();
		ChatTime = chat.getChatTime();
		province = chat.getProvince();
		province = TextUtils.isEmpty(province) ? "" : province;
		city = chat.getCity();
		city = TextUtils.isEmpty(city) ? "" : city;
		// cnt = chat.getContent();
		// cnt=cnt==null?"":cnt;
		// bt_ts.setVisibility(View.GONE);

		imageLoader.displayImage(headImg, iv_head, options);
		tv_name.setText(nickName);

		String[] identity = new String[2];
		if (TextUtils.isEmpty(idtype)) {
			identity[0] = "男";
			identity[1] = "m";
		} else {
			identity = getIdentityByCode(idtype);
		}
		tv_sf.setText(identity[0]);
		Drawable drawable;
		if (identity[1].equals("m")) {
			drawable = getResources().getDrawable(R.drawable.icon_male_32);
		} else {
			drawable = getResources().getDrawable(R.drawable.icon_female_32);
		}
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		tv_sf.setCompoundDrawables(drawable, null, null, null);
		tv_sf.setPadding(MoMaUtil.dip2px(act, 5), 0, MoMaUtil.dip2px(act, 5), 0);
		// time
		if (ChatTime != null) {
			tv_time.setText(sdf.format(ChatTime));
		} else {
			tv_time.setText("");
		}
		tv_dq.setText(province + " " + city);

		// initContentType();
	}

	private void initContentType() {
		cnt = chat.getContent();
		cnt = cnt == null ? "" : cnt;
		LayoutInflater inflater = LayoutInflater.from(act);
		View view = null;

		if (bottleModel.getContentType().equals("5000")) {
			view = inflater.inflate(R.layout.frament_frame_text, null);

			// view_bottle_head = (LinearLayout)
			// view.findViewById(R.id.view_bottle_head);
			// view_bottle_head.setVisibility(View.GONE);
			tv_cnt = (EmojiTextView) view.findViewById(R.id.tv_cnt);
			tv_da = (TextView) view.findViewById(R.id.tv_da);

			tv_cnt.setText(cnt);

			if (bottleModel.getBottleType().equals("4002")) {
				tv_da.setVisibility(View.VISIBLE);

				if (bottleModel.getHasAnswer()) {
					tv_da.setText("答案：" + bottleModel.getRemark());
				} else {
					tv_da.setText("答案：分享后看答案");
				}
			}
		} else if (bottleModel.getContentType().equals("5001")) {
			view = inflater.inflate(R.layout.frament_frame_image, null);

			// view_bottle_head = (LinearLayout)
			// view.findViewById(R.id.view_bottle_head);
			// view_bottle_head.setVisibility(View.GONE);
			iv = (ImageView) view.findViewById(R.id.iv);
			tv_cnt = (EmojiTextView) view.findViewById(R.id.tv_cnt);
			imageLoader.displayImage(bottleModel.getShortPic(), iv, options);
			tv_cnt.setText(cnt);

			iv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					int num = bottleModel.getPicNum();
					String url = bottleModel.getUrl();
					String redirectUrl = bottleModel.getRedirectUrl();
					// 是否跳转(0表示不跳转，1表示跳转)
					int isRedirect = bottleModel.getIsRedirect();
					List<Attachment> subList;

					if (isRedirect == 0) {
						if (num > 1) {
							AttachmentDao targetDao = MyApplication.getApp()
									.getDaoSession().getAttachmentDao();
							QueryBuilder<Attachment> qb = targetDao
									.queryBuilder();
							qb.where(AttachmentDao.Properties.BottleIdPk
									.eq(bottleModel.getBottleIdPk()));
							subList = qb.list();

							Intent intent = new Intent(act,
									ImageFrameShowActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
							intent.putExtra("imageUrls", (Serializable) subList);
							startActivity(intent);
						} else {
							// subList = new ArrayList<Attachment>();
							//
							// Attachment att = new Attachment();
							// att.setAttachmentUrl(url);
							// att.setAttachmentType(0);
							//
							// subList.add(att);

							Intent intent = new Intent(act,
									SingleImageFrameShowActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
							intent.putExtra("imageUrl", url);
							startActivity(intent);
						}
					} else if (isRedirect == 1) {// 要跳转的主题贺卡
						Intent intent = new Intent(act,
								WebFrameWithCacheActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("webUrl", redirectUrl);
						startActivity(intent);
					}
				}
			});
		} else if (bottleModel.getContentType().equals("5002")) {
			// ll_action.setVisibility(View.GONE);

			view = inflater.inflate(R.layout.frament_frame_card, null);
			// view_bottle_head = (LinearLayout)
			// view.findViewById(R.id.view_bottle_head);
			// view_bottle_head.setVisibility(View.GONE);
			iv = (ImageView) view.findViewById(R.id.iv);
			tv_cnt = (EmojiTextView) view.findViewById(R.id.tv_cnt);
			imageLoader.displayImage(bottleModel.getShortPic(), iv, options);
			tv_cnt.setText(cnt);

			iv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					int num = bottleModel.getPicNum();
					String url = bottleModel.getUrl();
					String redirectUrl = bottleModel.getRedirectUrl();
					// 是否跳转(0表示不跳转，1表示跳转)
					int isRedirect = bottleModel.getIsRedirect();
					List<Attachment> subList;

					if (isRedirect == 0) {
						if (num > 1) {
							AttachmentDao targetDao = MyApplication.getApp()
									.getDaoSession().getAttachmentDao();
							QueryBuilder<Attachment> qb = targetDao
									.queryBuilder();
							qb.where(AttachmentDao.Properties.BottleIdPk
									.eq(bottleModel.getBottleIdPk()));
							subList = qb.list();

							Intent intent = new Intent(act,
									ImageFrameShowActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
							intent.putExtra("imageUrls", (Serializable) subList);
							startActivity(intent);
						} else {
							subList = new ArrayList<Attachment>();

							Attachment att = new Attachment();
							att.setAttachmentUrl(url);
							att.setAttachmentType(0);

							subList.add(att);

							Intent intent = new Intent(act,
									SingleImageFrameShowActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
							intent.putExtra("imageUrl", (Serializable) subList);
							startActivity(intent);
						}
					} else if (isRedirect == 1) {// 要跳转的主题贺卡
						Intent intent = new Intent(act,
								WebFrameWithCacheActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("webUrl", redirectUrl);
						startActivity(intent);
					}
				}
			});
		} else if (bottleModel.getContentType().equals("5004")) {
			view = inflater.inflate(R.layout.frament_frame_voice, null);

			// view_bottle_head = (LinearLayout)
			// view.findViewById(R.id.view_bottle_head);
			// view_bottle_head.setVisibility(View.GONE);
			// view_bottle_head = (LinearLayout)
			// view.findViewById(R.id.view_bottle_head);
			// view_bottle_head.setVisibility(View.GONE);
			iv = (ImageView) view.findViewById(R.id.iv);
			tv_cnt = (EmojiTextView) view.findViewById(R.id.tv_cnt);
			if (!TextUtils.isEmpty(cnt)) {
				tv_cnt.setVisibility(View.VISIBLE);
				tv_cnt.setText(cnt);
			}

			ib_01 = (ImageButton) view.findViewById(R.id.ib_01);
			ib_01.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (recorder_flag == 0) {
						startPlay();
					} else if (recorder_flag == 1) {
						stopPlay();
					}
				}
			});
		} else {
			view = inflater.inflate(R.layout.frament_frame_text, null);

			// view_bottle_head = (LinearLayout)
			// view.findViewById(R.id.view_bottle_head);
			// view_bottle_head.setVisibility(View.GONE);
			tv_cnt = (EmojiTextView) view.findViewById(R.id.tv_cnt);
			tv_da = (TextView) view.findViewById(R.id.tv_da);

			tv_cnt.setText(cnt);

			if (bottleModel.getBottleType().equals("4002")) {
				tv_da.setVisibility(View.VISIBLE);

				if (bottleModel.getHasAnswer()) {
					tv_da.setText("答案：" + bottleModel.getRemark());
				} else {
					tv_da.setText("答案：分享后看答案");
				}
			}
		}
		initBottleHead(view);
		rl_c.addView(view);
	}

	private void initBottleHead(View view) {
		iv_head = (ImageView) view.findViewById(R.id.iv_userhead);
		tv_name = (TextView) view.findViewById(R.id.tv_name);
		tv_sf = (TextView) view.findViewById(R.id.tv_sf);
		tv_dq = (TextView) view.findViewById(R.id.tv_dq);

		tv_time = (TextView) view.findViewById(R.id.tv_time);
		// rl_c = (LinearLayout) view.findViewById(R.id.rl_c);

		// ll_action = (LinearLayout) view.findViewById(R.id.ll_action);
		// tv_sc = (TextView) view.findViewById(R.id.tv_sc);
		// tv_rd = (TextView) view.findViewById(R.id.tv_rd);

		// bt_ts = (Button) view.findViewById(R.id.bt_ts);
		// bt_ts.setOnClickListener(new OnClickListener() {
		// public void onClick(View v) {
		// Intent intent = new Intent(act, TousuActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		// TousuModel tm = new TousuModel();
		// tm.setBottleId(bottleModel.getBottleId());
		// tm.setUserId(UserManager.getInstance(act).getCurrentUserId());
		// tm.setbUserId(bottleModel.getUserId());
		// tm.setReportType(0);
		// intent.putExtra("tousuModel", tm);
		// startActivityForResult(intent, 1000);
		// }
		// });
	}

	// AsyncMfLoader asyncMfLoader;
	// SpeechMgr sm;
	/**
	 * 按钮状态 0.播放状态 1.暂停状态
	 */
	int recorder_flag;
	Handler handler = new Handler();
	ImageButton ib_01;

	private void stopPlay() {
		recorder_flag = 0;
		// if(sm!=null){
		// sm.stopPlay();
		// }
		ib_01.setBackgroundResource(R.drawable.selector_voice_start);
	}

	private void startPlay() {
		recorder_flag = 1;
		String url = bottleModel.getUrl();
		if (TextUtils.isEmpty(url)) {
			return;
		}
		// 音频处理，先下载，再播放
		// if(asyncMfLoader==null){
		// asyncMfLoader = new AsyncMfLoader(act);
		// }
		// asyncMfLoader.loadMfFile(act, url, new MfCallback() {
		// public void mfLoaded(File file, String imageUrl) {
		// Log.d("文件路径", file.getPath());
		// if(sm==null){
		// sm = SpeechMgr.getInstance();
		// }
		// sm.playSpeech(file.getPath(), new SpeexDecoderListener(){
		// @Override
		// public void decodedProgress(long decodeSize) {
		// // TODO Auto-generated method stub
		// }
		//
		// @Override
		// public void decodedFinish(boolean success) {
		// recorder_flag = 0;
		// Log.d("播放结束", "decodedFinish");
		// handler.post(new Runnable(){
		// public void run() {
		// ib_01.setBackgroundResource(R.drawable.selector_voice_start);
		// }
		// });
		// }
		//
		// });
		//
		// ib_01.setBackgroundResource(R.drawable.selector_voice_stop);
		// }
		// });
		// SpeechMgr sm = SpeechMgr.getInstance();
		// sm.playSpeech("/mnt/sdcard/DriftBottle/netmfs/mf.v", null);
	}

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

	public void openAnswer(String answer) {
		tv_da.setText("答案：" + answer);
	}

	@Override
	public void onPause() {
		// if(sm!=null){
		// sm.stopPlay();
		// }
		super.onPause();
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

	@Override
	public boolean onBackPressed() {
		// TODO Auto-generated method stub
		return false;
	}
}