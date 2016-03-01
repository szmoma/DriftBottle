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
import com.hnmoma.driftbottle.model.PhotoWallModel;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class VzonePhotoAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	
	private LinkedList<PhotoWallModel> mInfos;
	
	private int identityflag ; //身份标志，0表示自己，1表示其他人
	
	public VzonePhotoAdapter(ImageLoader imageLoader, int identityflag) {
		this.imageLoader = imageLoader;
		mInfos = new LinkedList<PhotoWallModel>();
		this.identityflag  = identityflag;
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)
		.showImageForEmptyUri(R.drawable.defalutimg)
		.showImageOnFail(R.drawable.defalutimg)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
//		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.bitmapConfig(Bitmap.Config.RGB_565)
//		.displayer(new RoundedBitmapDisplayer(2))
		.build();
	}
	
	public VzonePhotoAdapter(ImageLoader imageLoader) {
		this.imageLoader = imageLoader;
		mInfos = new LinkedList<PhotoWallModel>();
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)
		.showImageForEmptyUri(R.drawable.defalutimg)
		.showImageOnFail(R.drawable.defalutimg)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
//		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.bitmapConfig(Bitmap.Config.RGB_565)
//		.displayer(new RoundedBitmapDisplayer(2))
		.build();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.griditem_vzonephoto, null);
			holder = new Holder();
			holder.iv_photo = (ImageView) convertView.findViewById(R.id.iv_photo);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		PhotoWallModel visitor = mInfos.get(position);
		imageLoader.displayImage(visitor.getPicUrl(), holder.iv_photo, options);
		
		//第一行和最后一行，需要有间距，每一行有4个
//		if(position/4==0){	//第一行
//			convertView.setPadding(0, 10, 0, 0);
//		}else if(position>=(getCount()/4+getCount()%4==0?0:1-1)*4){ //最后一行
//			convertView.setPadding(0, 0, 0, 10);
//		}else {
//			convertView.setPadding(0, 0, 0, 0);
//		}
		return convertView;
	}
	
	@Override
	public int getCount() {
			return mInfos.size();
	}

	@Override
	public PhotoWallModel getItem(int arg0) {
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

	public void addItemLast(List<PhotoWallModel> datas) {
		if(mInfos==null){
			mInfos = new LinkedList<PhotoWallModel>();
		}
		mInfos.addAll(datas);
	}

	public void addItemTop(List<PhotoWallModel> datas) {
		for (int i = datas.size()-1; i>=0; i--) {
			PhotoWallModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}
	
	public void reset(List<PhotoWallModel> datas) {
		if(mInfos==null){
			mInfos = new LinkedList<PhotoWallModel>();
		}else
			mInfos.clear();
		mInfos.addAll(datas);
//		if(identityflag==0){
//			PhotoWallModel firstObj = new PhotoWallModel();
//			firstObj.setPicId("0000");
//			mInfos.addFirst(firstObj);
//		}
		
	}
	
	public void remove(PhotoWallModel bottle) {
		if(mInfos==null)
			return;
		mInfos.remove(bottle);
	}

	public void addItem2Top(PhotoWallModel data) {
		if(mInfos==null)
			mInfos = new LinkedList<PhotoWallModel>();
		if(mInfos.isEmpty())
			mInfos.add(data);
		else
			mInfos.add(0, data);
	}
	
	static class Holder {
		ImageView iv_photo;
		TextView tv_name;
	}

}
