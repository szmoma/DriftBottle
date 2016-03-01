package com.hnmoma.driftbottle.adapter;

import java.util.LinkedList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.model.GiftsModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class LwscAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	
	private LinkedList<GiftsModel> mInfos;
	
	public LwscAdapter(ImageLoader imageLoader, DisplayImageOptions options) {
		this.options = options;
		this.imageLoader = imageLoader;
		mInfos = new LinkedList<GiftsModel>();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}//listitem_djsc
			convertView = inflater.inflate(R.layout.listitem_lwsc, null);
			holder = new Holder();
			holder.iv_head = (ImageView) convertView.findViewById(R.id.iv_head);
			holder.tv_mz = (TextView) convertView.findViewById(R.id.tv_mz);
			holder.tv_jg = (TextView) convertView.findViewById(R.id.tv_jg);
			holder.tv_charm = (TextView) convertView.findViewById(R.id.tv_charm);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		//第一行和最后一行，需要有间距，每一行有4个
		if(position/4==0){	//第一行
			convertView.setPadding(0, 10, 0, 0);
		}else if(position>=(getCount()/4+getCount()%4==0?0:1-1)*4){ //最后一行
			convertView.setPadding(0, 0, 0, 10);
		}else {
			convertView.setPadding(0, 0, 0, 0);
		}
		
		GiftsModel dj = mInfos.get(position);
		imageLoader.displayImage(dj.getShortPic(), holder.iv_head, options);
		holder.tv_mz.setText(dj.getGiftName());
		holder.tv_jg.setText(dj.getPrice()+"扇贝");
		holder.tv_charm.setText("魅力 +" + dj.getCharm());
		return convertView;
	}
	
	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public GiftsModel getItem(int arg0) {
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

	public void addItemLast(List<GiftsModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<GiftsModel> datas) {
		for (int i = datas.size()-1; i>=0; i--) {
			GiftsModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}
	
	public void reset(List<GiftsModel> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(GiftsModel bottle) {
		mInfos.remove(bottle);
	}

	static class Holder {
		ImageView iv_head;
		TextView tv_mz;
		TextView tv_jg;
		TextView tv_charm;
	}
}