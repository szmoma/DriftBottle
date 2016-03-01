package com.hnmoma.driftbottle.adapter;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.model.AttentionModel;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ConcernAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	Context context;
	LinkedList<AttentionModel> mInfos;
	
	public HashMap<Integer, Boolean> ischeck;
	boolean isMulChoice = false;
	SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
	
	public ConcernAdapter(Context context,ImageLoader imageLoader, DisplayImageOptions options, HashMap<Integer, Boolean> ischeck) {
		this.context = context;
		this.imageLoader = imageLoader;
		this.options = options;
		this.ischeck = ischeck;
		mInfos = new LinkedList<AttentionModel>();
	}
	
	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public AttentionModel getItem(int arg0) {
		return mInfos.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.listitem_concern, null);
			holder = new Holder();
			holder.iv_head = (CircularImage) convertView.findViewById(R.id.iv_head);
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			holder.tv_username = (TextView) convertView.findViewById(R.id.tv_username);
			holder.tv_sf = (TextView) convertView.findViewById(R.id.tv_sf);
			holder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);
			holder.cb_check = (CheckBox) convertView.findViewById(R.id.cb_check);
			holder.ll_checkbg = (RelativeLayout) convertView.findViewById(R.id.ll_checkbg);
			holder.iv_vip = (ImageView) convertView.findViewById(R.id.iv_vip);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		if(isMulChoice){
			holder.ll_checkbg.setVisibility(View.VISIBLE);
			if(ischeck != null &&ischeck.size() > 0) {
				holder.cb_check.setChecked(ischeck.get(position));
			}
		} else {
			holder.ll_checkbg.setVisibility(View.GONE);
		}
		
		AttentionModel model = mInfos.get(position);
//		if(TextUtils.isEmpty(model.getHeadImg())) {
//			holder.iv_head.setImageResource(R.drawable.defalutimg);
//		} else {
		imageLoader.displayImage(model.getHeadImg(), holder.iv_head, options);	
//		}
		
		holder.tv_username.setText(model.getNickName());
		if(model.getAttentionTime() == null) {
			holder.tv_time.setText("");
		} else {
			holder.tv_time.setText(sdf.format(model.getAttentionTime()));
		}
		
		if(TextUtils.isEmpty(model.getDescript())) {
			holder.tv_desc.setText(context.getString(R.string.no_sign));
		} else {
			holder.tv_desc.setText(model.getDescript());
		}

		if(!TextUtils.isEmpty(model.getAge())) {
			holder.tv_sf.setText(model.getAge());
		}else{
			holder.tv_sf.setText("0");
		}
		Drawable drawable;
		String [] its = new String[2];
		if(TextUtils.isEmpty(model.getIdentityType())){
			its[0] = "男";
			its[1] = "m";
		}else{
			its = getIdentityByCode(model.getIdentityType());
		}
		if(its[1].equals("m")){
			drawable= context.getResources().getDrawable(R.drawable.icon_male_32);
			holder.tv_sf.setBackgroundResource(R.drawable.bg_sex_man);
		}else{
			drawable= context.getResources().getDrawable(R.drawable.icon_female_32);
			holder.tv_sf.setBackgroundResource(R.drawable.bg_sex_female);
		}
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		holder.tv_sf.setCompoundDrawables(drawable,null,null,null);
		holder.tv_sf.setPadding(MoMaUtil.dip2px(context, 5), 0, MoMaUtil.dip2px(context, 5), 0);
		
		if(model.getIsVIP() == 0) {
			holder.iv_vip.setVisibility(View.GONE);
			holder.tv_username.setTextColor(context.getResources().getColor(R.color.username_normal));
		} else {
			holder.iv_vip.setVisibility(View.VISIBLE);
			holder.tv_username.setTextColor(context.getResources().getColor(R.color.username_vip));
		}
		
		return convertView;
	}
	
	public void addItemLast(List<AttentionModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<AttentionModel> datas) {
		for (int i = datas.size()-1; i>=0; i--) {
			AttentionModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}
	
	public void reset(List<AttentionModel> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(AttentionModel bottle) {
		mInfos.remove(bottle);
	}
	
	static class Holder {
		CircularImage iv_head;
		TextView tv_username;
		TextView tv_time;
		TextView tv_sf;
		TextView tv_desc;
		CheckBox cb_check;
		RelativeLayout ll_checkbg;
		ImageView iv_vip;
	}
	
	public void setDelMode(boolean isMulChoice) {
		this.isMulChoice = isMulChoice;
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

	public void clear() {
		mInfos.clear();
	}
}
