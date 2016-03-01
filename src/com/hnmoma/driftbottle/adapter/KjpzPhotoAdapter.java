package com.hnmoma.driftbottle.adapter;

import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.model.Attachment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class KjpzPhotoAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	
	int width;
	
	private LinkedList<Attachment> mInfos;
	
	public KjpzPhotoAdapter(ImageLoader imageLoader, int width) {
		this.imageLoader = imageLoader;
		mInfos = new LinkedList<Attachment>();
		this.width = width;
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.card_kj_def)
		.showImageForEmptyUri(R.drawable.card_kj_def)
		.showImageOnFail(R.drawable.card_kj_def)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.displayer(new RoundedBitmapDisplayer(5))
		.build();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.griditem_kjpzphoto, null);
			holder = new Holder();
			
			holder.iv_head = (ImageView) convertView.findViewById(R.id.iv_head);
			
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(width, width);
			holder.iv_head.setLayoutParams(param);
			
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		Attachment dj = mInfos.get(position);
		imageLoader.displayImage(dj.getAttachmentUrl(), holder.iv_head, options);
		
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
	public Attachment getItem(int arg0) {
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

	public void addItemLast(List<Attachment> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<Attachment> datas) {
		for (int i = datas.size()-1; i>=0; i--) {
			Attachment info = datas.get(i);
			mInfos.addFirst(info);
		}
	}
	
	public void reset(List<Attachment> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(Attachment bottle) {
		mInfos.remove(bottle);
	}

	static class Holder {
		ImageView iv_head;
		TextView tv_mz;
		TextView tv_jg;
	}
}