package com.hnmoma.driftbottle.adapter;

import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.model.VzoneGiftModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class VzoneGiftAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	
	private LinkedList<VzoneGiftModel> mInfos;
	
	public VzoneGiftAdapter(ImageLoader imageLoader, DisplayImageOptions options) {
		
		this.imageLoader = imageLoader;
		this.options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.vzone_emptygift)
		.showImageForEmptyUri(R.drawable.vzone_emptygift)
		.showImageOnFail(R.drawable.vzone_emptygift)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
//		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.bitmapConfig(Bitmap.Config.RGB_565)
//		.displayer(new RoundedBitmapDisplayer(2))
		.build();
		mInfos = new LinkedList<VzoneGiftModel>();
		
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.griditem_vzonegift, null);
			holder = new Holder();
			holder.iv_head = (ImageView) convertView.findViewById(R.id.iv_head);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		VzoneGiftModel model = mInfos.get(position);
		imageLoader.displayImage(model.getShortPic(), holder.iv_head, options);
		holder.tv_name.setVisibility(View.GONE);
//		holder.tv_name.setText(visitor.getGiftName());
		return convertView;
	}
	
	@Override
	public int getCount() {
		if(mInfos.size() >= 4){
			return 4;
		}
		return mInfos.size();
		
	}

	@Override
	public VzoneGiftModel getItem(int arg0) {
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

	public void addItemLast(List<VzoneGiftModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<VzoneGiftModel> datas) {
		for (int i = datas.size()-1; i>=0; i--) {
			VzoneGiftModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}
	
	public void reset(List<VzoneGiftModel> datas) {
		if(mInfos!=null)
			
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(VzoneGiftModel bottle) {
		mInfos.remove(bottle);
	}

	static class Holder {
		ImageView iv_head;
		TextView tv_name;
	}
}