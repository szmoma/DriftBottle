package com.hnmoma.driftbottle.fragment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.hnmoma.driftbottle.ImageFrameShowActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.SingleImageFrameShowActivity;
import com.hnmoma.driftbottle.TalkDetailActivity;
import com.hnmoma.driftbottle.WebFrameWithCacheActivity;
import com.hnmoma.driftbottle.model.ActionNumModel;
import com.hnmoma.driftbottle.model.Attachment;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.CommentModel;
import com.hnmoma.driftbottle.model.PickBottleModel;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.SmileUtils;
import com.way.ui.emoji.EmojiTextView;

public class Fragment_talk extends BaseFragment implements OnClickListener{
	
	PickBottleModel model;
	
	BottleModel bottleModel;
	ActionNumModel actionNumModel;
	List<CommentModel>  commentModels;
	
	Button bt_01;
	Button bt_02;
	
	ImageView iv_userhead;
	TextView tv_name;
	TextView tv_sf;
	TextView tv_xz;
	TextView tv_gj;
	ImageView iv_bottletype;
	
	View view_one;
	TextView view_two;
	
	ImageView iv;
	EmojiTextView tv_desc;

	public static BaseFragment newInstance(PickBottleModel model) {
		Fragment_talk fragment = new Fragment_talk();
		Bundle bundle = new Bundle();
		bundle.putSerializable("PickBottleModel", model);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fishing_plpz, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Bundle bundle = getArguments();
		model = (PickBottleModel) bundle.getSerializable("PickBottleModel");
		
		bt_01 = (Button) findViewById(R.id.bt_01);
		bt_02 = (Button) findViewById(R.id.bt_02);
		
		bt_01.setOnClickListener(this);
		bt_02.setOnClickListener(this);
		
		view_one = findViewById(R.id.view_one);
		view_two = (TextView) findViewById(R.id.view_two);
		
		
		iv_userhead = (ImageView) findViewById(R.id.iv_userhead);
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_sf = (TextView) findViewById(R.id.tv_sf);
		tv_xz = (TextView) findViewById(R.id.tv_xz);
		tv_gj = (TextView) findViewById(R.id.tv_gj);
		
		iv = (ImageView) findViewById(R.id.iv);
		tv_desc = (EmojiTextView) findViewById(R.id.tv_desc);
		
		initDate();
	}
	
	private void initDate(){
		bottleModel = model.getBottleInfo();
		actionNumModel = model.getActionNum();
		commentModels = model.getCommentModels();
		
		UserInfoModel userInfo = bottleModel.getUserInfo();
		
		String headImg = userInfo.getHeadImg();
		String nickName = userInfo.getNickName();
//		idtype = bottleModel.getUserInfo().getIdentityType();
//		ChatTime = bottleModel.getCreateTime();
//		province = bottleModel.getUserInfo().getProvince();
//		province = TextUtils.isEmpty(province) ? "" : province;
//		city = bottleModel.getUserInfo().getCity();
//		city = TextUtils.isEmpty(city) ? "" : city;
//		cnt = bottleModel.getContent();
//		cnt = cnt == null ? "" : cnt;
		
		imageLoader.displayImage(headImg, iv_userhead, options);
		tv_name.setText(nickName);
		String province = bottleModel.getUserInfo().getProvince();
		province = TextUtils.isEmpty(province) ? "" : province;
		String city = bottleModel.getUserInfo().getCity();
		city = TextUtils.isEmpty(city) ? "" : city;
		
		tv_gj.setText(city);
		
		tv_sf.setText(userInfo.getAge()+"");
		
		String[] identity = new String[2];
		String idtype = userInfo.getIdentityType();
		if (TextUtils.isEmpty(idtype)) {
			identity[0] = "男";
			identity[1] = "m";
		} else {
			identity = getIdentityByCode(idtype);
		}
		
		Drawable drawable;
		if (identity[1].equals("m")) {
			tv_sf.setBackgroundResource(R.drawable.bg_sex_man);
			drawable = getResources().getDrawable(R.drawable.icon_male_32);
		} else {
			tv_sf.setBackgroundResource(R.drawable.bg_sex_female);
			drawable = getResources().getDrawable(R.drawable.icon_female_32);
		}
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv_sf.setCompoundDrawables(drawable, null, null, null);
		tv_sf.setPadding(MoMaUtil.dip2px(act, 5), 0, MoMaUtil.dip2px(act, 5), 0);
		
		//bottle content
		String cnt = bottleModel.getContent();
		cnt = cnt == null ? "" : cnt;
		
		if (bottleModel.getContentType().equals("5000")) {//文字
			view_one.setVisibility(View.GONE);
			view_two.setVisibility(View.VISIBLE);
			
			view_two.setText(cnt);
		} else if (bottleModel.getContentType().equals("5001")) {//图片
			view_one.setVisibility(View.VISIBLE);
			view_two.setVisibility(View.GONE);
			
			imageLoader.displayImage(bottleModel.getShortPic(), iv, options);
			tv_desc.setText(SmileUtils.getSmiledText(act, cnt),BufferType.SPANNABLE);
			
			iv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					int num = bottleModel.getPicNum();
					String url = bottleModel.getUrl();
//					String redirectUrl = bottleModel.getRedirectUrl();
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
		} else if (bottleModel.getContentType().equals("5003")) {//视频
		} else if (bottleModel.getContentType().equals("5004")) {//语音
		} else if (bottleModel.getContentType().equals("5005")) {//web
			view_one.setVisibility(View.VISIBLE);
			view_two.setVisibility(View.GONE);
			
			final String redirectUrl = bottleModel.getRedirectUrl();
			imageLoader.displayImage(bottleModel.getShortPic(), iv, options);
			tv_desc.setText(SmileUtils.getSmiledText(act, cnt),BufferType.SPANNABLE);
			iv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(act, WebFrameWithCacheActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("webUrl", redirectUrl);
					startActivity(intent);
				}
			});
		}
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
		if (code.equals("2000") || code.equals("2001") || code.equals("2002") || code.equals("2007")) {
			strs[1] = "f";
		} else {
			strs[1] = "m";
		}

		return strs;
	}

	@Override
	public boolean onBackPressed() {
		act.finish();
		return true;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.bt_01:
				act.finish();
				break;
			case R.id.bt_02:
				Intent talk = new Intent(act, TalkDetailActivity.class);
				talk.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				talk.putExtra("talkId", bottleModel.getBottleId());
				talk.putExtra("toUserId", "");
				talk.putExtra("parentId", "");
				startActivity(talk);
				break;
		}
	}
}
