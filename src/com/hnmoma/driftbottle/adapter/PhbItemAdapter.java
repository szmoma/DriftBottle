package com.hnmoma.driftbottle.adapter;

import java.util.LinkedList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.model.TopUserModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PhbItemAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	int index;
	int type;
	private LinkedList<TopUserModel> mInfos;

	public PhbItemAdapter(ImageLoader imageLoader, DisplayImageOptions options, int index, int type) {
		this.options = options;
		this.imageLoader = imageLoader;
		mInfos = new LinkedList<TopUserModel>();
		this.index = index;
		this.type = type;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if (inflater == null) {
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.listitem_phb, null);
			holder = new Holder();
			holder.iv_head = (CircularImage) convertView.findViewById(R.id.iv_head);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_mc = (TextView) convertView.findViewById(R.id.tv_mc);
			holder.tv_01 = (TextView) convertView.findViewById(R.id.tv_01);
			holder.iv_vip = (ImageView) convertView.findViewById(R.id.iv_vip);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		TopUserModel tum = mInfos.get(position);
		imageLoader.displayImage(tum.getHeadImg(), holder.iv_head, options);
		holder.tv_mc.setText((position + 7) + "");

		if (!TextUtils.isEmpty(tum.getNickName())) {
			holder.tv_name.setText(tum.getNickName());
		} else {
			holder.tv_name.setText("");
		}

		String it = tum.getIdentityType();
		Drawable icon_sex = null; // 性别图片
		if (it.equals("2000") || it.equals("2001") || it.equals("2002")
				|| it.equals("2007")) {
			icon_sex = parent.getContext().getResources()
					.getDrawable(R.drawable.w_new);
		} else {
			icon_sex = parent.getContext().getResources()
					.getDrawable(R.drawable.m_new);
		}
		holder.tv_name.setCompoundDrawablesWithIntrinsicBounds(null, null,icon_sex, null);

		if(tum.getIsVIP() == 0) {
			holder.iv_vip.setVisibility(View.GONE);
			holder.tv_name.setTextColor(convertView.getContext().getResources().getColor(R.color.username_normal));
		} else {
			holder.iv_vip.setVisibility(View.VISIBLE);
			holder.tv_name.setTextColor(convertView.getContext().getResources().getColor(R.color.username_vip));
		}
		
		if(type == 0) {
			holder.tv_01.setVisibility(View.GONE);
		} else if (type == 1) {
			holder.tv_01.setVisibility(View.VISIBLE);
			holder.tv_01.setText("魅力：" + tum.getCharm());
		} else if(type == 3){
			holder.tv_01.setVisibility(View.VISIBLE);
			holder.tv_01.setText("财富：" + tum.getFortune());
		} else {
			holder.tv_01.setVisibility(View.VISIBLE);
			holder.tv_01.setText("战绩：" + tum.getScore().split("_")[0] + "胜");
		}

		return convertView;
	}

	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public TopUserModel getItem(int arg0) {
		if (arg0 < mInfos.size()) {
			return mInfos.get(arg0);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	public void addItemLast(List<TopUserModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<TopUserModel> datas) {
		for (int i = datas.size() - 1; i >= 0; i--) {
			TopUserModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}

	public void reset(List<TopUserModel> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(TopUserModel bottle) {
		mInfos.remove(bottle);
	}

	static class Holder {
		CircularImage iv_head;
		TextView tv_name;
		TextView tv_mc;
		TextView tv_01;
		ImageView iv_vip;
	}
}