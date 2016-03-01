package com.hnmoma.driftbottle.adapter;

import java.util.LinkedList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.model.VzoneBadgeModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class VzoneBadgeAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	
	private LinkedList<VzoneBadgeModel> mInfos;
	
	public VzoneBadgeAdapter(ImageLoader imageLoader, DisplayImageOptions options) {
		this.imageLoader = imageLoader;
		this.options = options;
		this.mInfos =new LinkedList<VzoneBadgeModel>();
		
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.griditem_vzonebadge, null);
			holder = new Holder();
			holder.iv_badge = (ImageView) convertView.findViewById(R.id.iv_badge);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		//PhotoWallModel visitor = mInfos.get(position);
		imageLoader.displayImage(mInfos.get(position).getShortPic(), holder.iv_badge, options);
		
//		if(!TextUtils.isEmpty(visitor.getNickName())){
//		}
		return convertView;
	}
	
	@Override
	public int getCount() {
		if(mInfos.size()>4){
			return 4;
		}else{
			return mInfos.size();
		}
	}

	@Override
	public VzoneBadgeModel getItem(int arg0) {
		if(arg0<mInfos.size()){
			return mInfos.get(arg0);
		}else{
			return null;
		}
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	public void addItemLast(List<VzoneBadgeModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<VzoneBadgeModel> datas) {
		for (int i = datas.size()-1; i>=0; i--) {
			VzoneBadgeModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}
	
	public void reset(List<VzoneBadgeModel> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(VzoneBadgeModel bottle) {
		mInfos.remove(bottle);
	}

	static class Holder {
		ImageView iv_badge;
	}
}