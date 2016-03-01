package com.hnmoma.driftbottle.adapter;

import java.util.LinkedList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.model.PropsModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MyPropertyAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	
	private LinkedList<PropsModel> mInfos;
	
	public MyPropertyAdapter(ImageLoader imageLoader, DisplayImageOptions options) {
		this.options = options;
		this.imageLoader = imageLoader;
		mInfos = new LinkedList<PropsModel>();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.listitem_wddj, null);
			holder = new Holder();
			holder.iv_head = (ImageView) convertView.findViewById(R.id.iv_head);
			holder.tv_mz = (TextView) convertView.findViewById(R.id.tv_mz);
			holder.tv_jg = (TextView) convertView.findViewById(R.id.tv_jg);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
//		if(position == 0){
//			convertView.setPadding(0, 8, 0, 0);
//		}else if(position == getCount()-1){
//			convertView.setPadding(0, 0, 0, 8);
//		}else{
//			convertView.setPadding(0, 0, 0, 0);
//		}
		
		PropsModel dj = mInfos.get(position);
		imageLoader.displayImage(dj.getPic(), holder.iv_head, options);
		holder.tv_mz.setText(dj.getProName());
		
		if(dj.getUseType()==0){//包月
			holder.tv_jg.setText("包月¥"+dj.getPrice());
		}else{//按个计算
			holder.tv_jg.setText(dj.getPrice()+"个扇贝/个");
		}
		
		return convertView;
	}
	
	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public PropsModel getItem(int arg0) {
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

	public void addItemLast(List<PropsModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<PropsModel> datas) {
		for (int i = datas.size()-1; i>=0; i--) {
			PropsModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}
	
	public void reset(List<PropsModel> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(PropsModel bottle) {
		mInfos.remove(bottle);
	}

	static class Holder {
		ImageView iv_head;
		TextView tv_mz;
		TextView tv_jg;
	}
}