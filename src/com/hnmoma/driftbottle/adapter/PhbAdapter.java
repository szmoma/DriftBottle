package com.hnmoma.driftbottle.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hnmoma.driftbottle.HotTopItemActivity;
import com.hnmoma.driftbottle.LoginActivity;
import com.hnmoma.driftbottle.VzoneActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.TopUserModel;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PhbAdapter extends BaseAdapter {

	Context context;
//	String currentUserId;
	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	int index;
	int Displaywidth;
//	private LinkedList<TopUserModel> mInfos;
	private Map<Integer, List<TopUserModel>> datas;
	private  String TopName[] = {"鲜肉榜", "魅力榜", "财富榜", "拳王榜"};//"富豪榜", "女神榜", "拳王榜", "新人榜"
	private int[] TopNamePic = {R.drawable.fortuneleft, R.drawable.charmleft, R.drawable.gamewinleft, R.drawable.newpleft};
	
	public PhbAdapter(Context context, ImageLoader imageLoader, DisplayImageOptions options,
			int index) {
		this.context = context;
		this.options = options;
		this.imageLoader = imageLoader;
//		mInfos = new LinkedList<TopUserModel>();
		datas = new HashMap<Integer, List<TopUserModel>>();
		this.index = index;
		this.Displaywidth = (((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth()-MoMaUtil.dip2px(context, 50))/4;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if (inflater == null) {
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.listitem_hottop, null);
			holder = new Holder();
			holder.rl_more =  (RelativeLayout) convertView.findViewById(R.id.rl_more);
			holder.tv_name = new TextView[5];
			holder.rl_layout = new RelativeLayout[5];
			holder.iv_head = new ImageView[5];
			holder.iv_vip = new ImageView[5];
			
			holder.tv_topname = (TextView) convertView.findViewById(R.id.tv_topname);
			holder.ll_item = (LinearLayout) convertView.findViewById(R.id.ll_item);
			
			holder.rl_layout[0] = (RelativeLayout) convertView.findViewById(R.id.rl_01);
			holder.iv_head[0] = (ImageView) convertView.findViewById(R.id.iv_01);
			holder.tv_name[0] = (TextView) convertView.findViewById(R.id.tvname_01);
			holder.iv_vip[0] = (ImageView) convertView.findViewById(R.id.iv_vip01);
			
			holder.rl_layout[1] = (RelativeLayout) convertView.findViewById(R.id.rl_02);
			holder.iv_head[1] = (ImageView) convertView.findViewById(R.id.iv_02);
			holder.tv_name[1] = (TextView) convertView.findViewById(R.id.tvname_02);
			holder.iv_vip[1] = (ImageView) convertView.findViewById(R.id.iv_vip02);
//			holder.tv_number[1] = (TextView) convertView.findViewById(R.id.tvbg_02);
			
			holder.rl_layout[2] = (RelativeLayout) convertView.findViewById(R.id.rl_03);
			holder.iv_head[2] = (ImageView) convertView.findViewById(R.id.iv_03);
			holder.tv_name[2] = (TextView) convertView.findViewById(R.id.tvname_03);
			holder.iv_vip[2] = (ImageView) convertView.findViewById(R.id.iv_vip03);
//			holder.tv_number[2] = (TextView) convertView.findViewById(R.id.tvbg_03);
			
			holder.rl_layout[3] = (RelativeLayout) convertView.findViewById(R.id.rl_04);
			holder.iv_head[3] = (ImageView) convertView.findViewById(R.id.iv_04);
			holder.tv_name[3] = (TextView) convertView.findViewById(R.id.tvname_04);
			holder.iv_vip[3] = (ImageView) convertView.findViewById(R.id.iv_vip04);
//			holder.tv_number[3] = (TextView) convertView.findViewById(R.id.tvbg_04);
			
			holder.rl_layout[4] = (RelativeLayout) convertView.findViewById(R.id.rl_05);
			holder.iv_head[4] = (ImageView) convertView.findViewById(R.id.iv_05);
			holder.tv_name[4] = (TextView) convertView.findViewById(R.id.tvname_05);
			holder.iv_vip[4] = (ImageView) convertView.findViewById(R.id.iv_vip05);
//			holder.tv_number[4] = (TextView) convertView.findViewById(R.id.tvbg_05);
			
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		List<TopUserModel> tum = datas.get(position);
		if(tum != null && tum.size() > 0) {
			holder.ll_item.setVisibility(View.VISIBLE);
			holder.rl_more.setOnClickListener(new MyClickListener(position));
			holder.tv_topname.setVisibility(View.VISIBLE);
			holder.tv_topname.setText(TopName[position]);
			Drawable drawable = context.getResources().getDrawable(TopNamePic[position]);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			holder.tv_topname.setCompoundDrawables(drawable,null,null,null);
//			holder.tv_topname.setPadding(MoMaUtil.dip2px(context, 10), 0, 0, 0);
			setItemData(holder, position);
		} else {
//			holder.tv_topname.setVisibility(View.GONE);
			holder.ll_item.setVisibility(View.GONE);
		}
		
//		if (index == 0) {
//			holder.tv_01.setText("魅力值：" + tum.getCharm());
//		} else {
//			holder.tv_01.setText("财富值：" + tum.getFortune());
//		}

		return convertView;
	}
	
	
	@SuppressWarnings("deprecation")
	private void setItemData(Holder holder, int item) {
		List<TopUserModel> mInfos = datas.get(item);
		
		for(int i= 0; i < mInfos.size(); i++) {
			holder.rl_layout[i].setVisibility(View.VISIBLE);
			LayoutParams params = holder.iv_head[i].getLayoutParams();
			if( i== 0) {
				params.height = Displaywidth*2;
				params.width = Displaywidth * 2;
			} else {
				params.height = Displaywidth;
				params.width = Displaywidth;
			}
			holder.iv_head[i].setLayoutParams(params);
			imageLoader.displayImage(mInfos.get(i).getHeadImg(), holder.iv_head[i], options);
			holder.iv_head[i].setOnClickListener(new MyClickListener(item));
			
			holder.tv_name[i].setText(mInfos.get(i).getNickName());
			if(mInfos.get(i).getIsVIP() == 0) {
				holder.iv_vip[i].setVisibility(View.GONE);
				holder.tv_name[i].setTextColor(context.getResources().getColor(R.color.username_normal));
			} else {
				holder.iv_vip[i].setVisibility(View.VISIBLE);
				holder.tv_name[i].setTextColor(context.getResources().getColor(R.color.username_vip));
			}
			String idtype = mInfos.get(i).getIdentityType();
			String [] its = new String[2];
			if(TextUtils.isEmpty(idtype)){
				its[0] = "男";
				its[1] = "m";
			}else{
				its = getIdentityByCode(idtype);
			}
			Drawable drawable;
			if(its[1].equals("m")){
				drawable= context.getResources().getDrawable(R.drawable.m_new);
			}else{
				drawable= context.getResources().getDrawable(R.drawable.w_new);
			}
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			holder.tv_name[i].setCompoundDrawables(null,null,drawable,null);
			holder.tv_name[i].setPadding(0, 0, MoMaUtil.dip2px(context, 5), 0);
			
			if(i == 4) {
				break;
			}
		}

		if(mInfos.size() < 5) {
			int last =  mInfos.size();
			while (last < 5) {
				holder.rl_layout[last].setVisibility(View.GONE);
				last++;
			}
		}
	}
	
	class MyClickListener implements OnClickListener {

		int item;
		
		public MyClickListener(int item) {
			this.item = item;
		}
		
		@Override
		public void onClick(View v) {
			
			if(UserManager.getInstance(context).getCurrentUser() == null) {
				Intent intent = new Intent(context, LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				context.startActivity(intent);
				return;
			}
			
			String currentUserId = UserManager.getInstance(context).getCurrentUserId();
			
			switch (v.getId()) {
			case R.id.iv_01:
				String userID0 = datas.get(item).get(0).getUserId();
				if(userID0.equals(currentUserId)) {
					Intent intent = new Intent(context, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("userId", userID0);
					intent.putExtra("identityflag", 0);
					intent.putExtra("visitUserId", currentUserId);
					context.startActivity(intent);
				} else {
					Intent intent = new Intent(context, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("identityflag", 1);
					intent.putExtra("userId", userID0);
					intent.putExtra("visitUserId", currentUserId);
					context.startActivity(intent);
				}
				break;
			case R.id.iv_02:
				String userID1 = datas.get(item).get(1).getUserId();
				if(userID1.equals(currentUserId)) {
					Intent intent = new Intent(context, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("userId", userID1);
					intent.putExtra("identityflag", 0);
					intent.putExtra("visitUserId", currentUserId);
					context.startActivity(intent);
				} else {
					Intent intent = new Intent(context, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("identityflag", 1);
					intent.putExtra("userId", userID1);
					intent.putExtra("visitUserId", currentUserId);
					context.startActivity(intent);
				}
				break;
			case R.id.iv_03:
				String userID2 = datas.get(item).get(2).getUserId();
				if(userID2.equals(currentUserId)) {
					Intent intent = new Intent(context, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("userId", userID2);
					intent.putExtra("identityflag", 0);
					intent.putExtra("visitUserId", currentUserId);
					context.startActivity(intent);
				} else {
					Intent intent = new Intent(context, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("identityflag", 1);
					intent.putExtra("userId", userID2);
					intent.putExtra("visitUserId", currentUserId);
					context.startActivity(intent);
				}
				break;
			case R.id.iv_04:
				String userID3 = datas.get(item).get(3).getUserId();
				if(userID3.equals(currentUserId)) {
					Intent intent = new Intent(context, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("userId", userID3);
					intent.putExtra("identityflag", 0);
					intent.putExtra("visitUserId", currentUserId);
					context.startActivity(intent);
				} else {
					Intent intent = new Intent(context, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("identityflag", 1);
					intent.putExtra("userId", userID3);
					intent.putExtra("visitUserId", currentUserId);
					context.startActivity(intent);
				}
				break;
			case R.id.iv_05:
				String userID4 = datas.get(item).get(4).getUserId();
				if(userID4.equals(currentUserId)) {
					Intent intent = new Intent(context, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("userId", userID4);
					intent.putExtra("identityflag", 0);
					intent.putExtra("visitUserId", currentUserId);
					context.startActivity(intent);
				} else {
					Intent intent = new Intent(context, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("identityflag", 1);
					intent.putExtra("userId", userID4);
					intent.putExtra("visitUserId", currentUserId);
					context.startActivity(intent);
				}
				break;
			case R.id.rl_more:
				List<TopUserModel> list = datas.get(item);
				Intent HotItem = new Intent(context, HotTopItemActivity.class);
				HotItem.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				HotItem.putExtra("item", 0);// 0：总榜 
				HotItem.putExtra("position", item);
				context.startActivity(HotItem);
				break;
			}
		}
		
	}
	

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public List<TopUserModel> getItem(int arg0) {
		if (arg0 < datas.size()) {
			return datas.get(arg0);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	public void addItemLast(Map<Integer, List<TopUserModel>> datas) {
		this.datas.putAll(datas);
	}


	public void reset(Map<Integer, List<TopUserModel>> datas) {
		this.datas.clear();
		this.datas.putAll(datas);
	}
	
	/*
	 * 获取性别
	 */
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

	static class Holder {
		TextView tv_topname;
		LinearLayout ll_item;//整个布局
		RelativeLayout[] rl_layout;// 每个item的集合
		ImageView[] iv_head;
		TextView[] tv_name;
		ImageView[] iv_vip;
		RelativeLayout rl_more;
	}
}