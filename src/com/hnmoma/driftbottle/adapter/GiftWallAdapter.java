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
import com.hnmoma.driftbottle.model.MyGiftsModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class GiftWallAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	
	private LinkedList<MyGiftsModel> mInfos;
	boolean isMySelf;
	
	public GiftWallAdapter(ImageLoader imageLoader, DisplayImageOptions options,  boolean isMySelf) {
		this.options = options;
		this.imageLoader = imageLoader;
		mInfos = new LinkedList<MyGiftsModel>();
		this.isMySelf = isMySelf;
	}

	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public MyGiftsModel getItem(int position) {
		return mInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Holder holder;
		if (convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.griditem_giftwall, null);
			holder = new Holder();
			holder.iv_head = (ImageView) convertView.findViewById(R.id.iv_head);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_charm = (TextView) convertView.findViewById(R.id.tv_charm);
			holder.tv_diamond = (TextView) convertView.findViewById(R.id.tv_diamond);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		MyGiftsModel mm = mInfos.get(position);
		imageLoader.displayImage(mm.getPicUrl(), holder.iv_head, options);
		holder.tv_charm.setText("魅力+" + mm.getCharm());
		holder.tv_diamond.setText("价格: " + mm.getPrice());
		holder.tv_name.setText(mm.getGiftName());
		
		//第一行和最后一行，需要有间距，每一行有4个
		if(position/4==0){	//第一行
			convertView.setPadding(0, 10, 0, 0);
		}else if(position>=(getCount()/4+getCount()%4==0?0:1-1)*4){ //最后一行
			convertView.setPadding(0, 0, 0, 10);
		}else {
			convertView.setPadding(0, 0, 0, 0);
		}
				
		return convertView;
	}

	public void addItemLast(List<MyGiftsModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<MyGiftsModel> datas) {
		for (int i = datas.size()-1; i>=0; i--) {
			MyGiftsModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}
	
	public void reset(List<MyGiftsModel> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(MyGiftsModel bottle) {
		mInfos.remove(bottle);
	}

	static class Holder {
		ImageView iv_head;
		TextView tv_name;
		TextView tv_diamond;
		TextView tv_charm;
	}
	
}
