package com.hnmoma.driftbottle.fragment;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hnmoma.driftbottle.ImageFrameShowActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.SingleImageFrameShowActivity;
import com.hnmoma.driftbottle.WebFrameWithCacheActivity;
import com.hnmoma.driftbottle.itfc.FrameShowCallBack;
import com.hnmoma.driftbottle.model.ActionNumModel;
import com.hnmoma.driftbottle.model.Attachment;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.util.MoMaUtil;

/**
 *
 */
public class ImageFrameFragment extends BaseFragment {
	
	ImageView iv_head;
	ImageView iv;
	TextView tv_name;
	TextView tv_sf;
	TextView tv_dq;
	TextView tv_cnt;
	TextView tv_da;
	TextView tv_time;
	
	LinearLayout ll_action;
	TextView tv_sc;
	TextView tv_rd;
	
	SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
	
	BottleModel bm;
	ActionNumModel actionNumModel;
	
//	public static ImageFrameFragment newInstance(BottleModel bm) {
//		ImageFrameFragment tff = new ImageFrameFragment();
//		Bundle args = new Bundle();
//		args.putSerializable("bottleModel", bm);
//		tff.setArguments(args);
//		return tff;
//	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("bottleModel", bm);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null){
			bm = (BottleModel) savedInstanceState.getSerializable("bottleModel");
		}
		
//		Bundle bundle = getArguments();
//		if(bundle != null){
//			bm = (BottleModel) bundle.getSerializable("bottleModel");
//		}
		
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frament_imageframe, null);
		
		iv_head = (ImageView) view.findViewById(R.id.iv_userhead);
		iv = (ImageView) view.findViewById(R.id.iv);
		tv_name = (TextView) view.findViewById(R.id.tv_name);
		tv_sf = (TextView) view.findViewById(R.id.tv_sf);
		tv_dq = (TextView) view.findViewById(R.id.tv_dq);
		tv_cnt = (TextView) view.findViewById(R.id.tv_cnt);
		tv_da = (TextView) view.findViewById(R.id.tv_da);
		tv_time = (TextView) view.findViewById(R.id.tv_time);
		ll_action = (LinearLayout) view.findViewById(R.id.ll_action);
		tv_sc = (TextView) view.findViewById(R.id.tv_sc);
		tv_rd = (TextView) view.findViewById(R.id.tv_rd);
		
		iv.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int num = bm.getPicNum();
				String url = bm.getUrl();
				String redirectUrl = bm.getRedirectUrl();
				//是否跳转(0表示不跳转，1表示跳转)
				int isRedirect = bm.getIsRedirect();
				List<Attachment> subList;
				
				if(isRedirect == 0){
					if(num>1){
						subList = bm.getSubList();
						Intent intent = new Intent(act, ImageFrameShowActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("imageUrls", (Serializable)subList);
						startActivity(intent);
					}else{
						subList = new ArrayList<Attachment>();
						
						Attachment att = new Attachment();
						att.setAttachmentUrl(url);
						att.setAttachmentType(0);
						
						subList.add(att);
						
						Intent intent = new Intent(act, SingleImageFrameShowActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						intent.putExtra("imageUrls", (Serializable)subList);
						startActivity(intent);
					}
				}else if(isRedirect == 1){//要跳转的主题贺卡
					Intent intent = new Intent(act, WebFrameWithCacheActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("webUrl", redirectUrl);
					startActivity(intent);
				}
			}
		});
		
		return view;
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FrameShowCallBack fsc = (FrameShowCallBack) act;
		Object [] objects = fsc.onFragmentInit(this);
		bm = (BottleModel) objects[0];
		actionNumModel = (ActionNumModel) objects[1];
		initData();
	}
	
	public void initData(){
		imageLoader.displayImage(bm.getUserInfo().getHeadImg(), iv_head, options);
		tv_name.setText(bm.getUserInfo().getNickName());
		
		imageLoader.displayImage(bm.getShortPic(), iv, options);
		
		String [] identity = new String[2];
		if(TextUtils.isEmpty(bm.getUserInfo().getIdentityType())){
			identity[0] = "男";
			identity[1] = "m";
		}else{
			identity = getIdentityByCode(bm.getUserInfo().getIdentityType());
		}
		tv_sf.setText(identity[0]);
		Drawable drawable;
		if(identity[1].equals("m")){
			drawable= getResources().getDrawable(R.drawable.icon_male_32);
		}else{
			drawable= getResources().getDrawable(R.drawable.icon_female_32);
		}
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv_sf.setCompoundDrawables(drawable,null,null,null);
		tv_sf.setPadding(MoMaUtil.dip2px(act, 5), 0, MoMaUtil.dip2px(act, 5), 0);
		//time
		Date ChatTime = bm.getCreateTime();
	    if(ChatTime!=null){
	    	tv_time.setText(sdf.format(ChatTime));
	    }else{
	    	tv_time.setText("");
	    }
		
		
		String province = TextUtils.isEmpty(bm.getUserInfo().getProvince())?"":bm.getUserInfo().getProvince();
		String city = TextUtils.isEmpty(bm.getUserInfo().getCity())?"":bm.getUserInfo().getCity();
		tv_dq.setText(province+" "+city);
		
		String cnt = bm.getContent();
		cnt=cnt==null?"":cnt;
		tv_cnt.setText(cnt);
		
		if(bm.getBottleType().equals("4002")){
			tv_da.setVisibility(View.VISIBLE);
			
			if(bm.getHasAnswer()){
				tv_da.setText("答案："+bm.getRemark());
			}else{
				tv_da.setText("答案：分享后看答案");
			}
		}
		
		if (bm.getBottleSort().equals("3000")) {
			ll_action.setVisibility(View.VISIBLE);
		} else if (bm.getBottleSort().equals("3001")){
			ll_action.setVisibility(View.GONE);
		}
		
		if(actionNumModel!=null){
			tv_sc.setText("收藏("+actionNumModel.getSupportNum()+")");
			tv_rd.setText("扔掉("+actionNumModel.getDisSupportNum()+")");
		}
	}
	
	private String[] getIdentityByCode(String code){
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
		strs[0]=flag;
		//m-lan f-nv
		if(code.equals("2000") || code.equals("2001") || code.equals("2002") || code.equals("2007")){
			strs[1]="f";
		}else{
			strs[1]="m";
		}
		
		return strs;
	}
	
	public void openAnswer(String answer) {
		tv_da.setText("答案："+answer);
	}

	@Override
	public boolean onBackPressed() {
		// TODO Auto-generated method stub
		return false;
	}
}
