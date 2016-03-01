package com.hnmoma.driftbottle.adapter;

import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.model.BadgeListModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class BadgeAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	
	private LinkedList<BadgeListModel> mInfos;
	
	public BadgeAdapter(ImageLoader imageLoader) {
		this.imageLoader = imageLoader;
		mInfos = new LinkedList<BadgeListModel>();
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)
		.showImageForEmptyUri(R.drawable.defalutimg)
		.showImageOnFail(R.drawable.defalutimg)
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
			convertView = inflater.inflate(R.layout.griditem_badge, null);
			holder = new Holder();
			holder.iv_badge = (ImageView) convertView.findViewById(R.id.iv_badge);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		BadgeListModel model = mInfos.get(position);
		if("1".equals(model.getIsLight())) {
			imageLoader.displayImage(model.getPicUrl(), holder.iv_badge, options);
		} else {
			imageLoader.displayImage(model.getGayPicUrl(), holder.iv_badge, options);
		}
		return convertView;
	}
	
	@Override
	public int getCount() {
			return mInfos.size();
	}

	@Override
	public BadgeListModel getItem(int arg0) {
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

	public void addItemLast(List<BadgeListModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<BadgeListModel> datas) {
		for (int i = datas.size()-1; i>=0; i--) {
			BadgeListModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}
	
	public void reset(List<BadgeListModel> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(BadgeListModel bottle) {
		mInfos.remove(bottle);
	}

	static class Holder {
		ImageView iv_badge;
	}
}

