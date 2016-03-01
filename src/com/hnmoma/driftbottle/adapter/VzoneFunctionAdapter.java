package com.hnmoma.driftbottle.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.nostra13.universalimageloader.core.ImageLoader;

public class VzoneFunctionAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	ImageLoader imageLoader;
//	DisplayImageOptions options;
	
	private String[] mInfos;
	private int[] mPictues = {R.drawable.vzone_00, R.drawable.vzone_01,R.drawable.vzone_02,
												R.drawable.vzone_06, R.drawable.vzone_04, R.drawable.vzone_05};//R.drawable.vzone_03
	public VzoneFunctionAdapter(ImageLoader imageLoader, String[] mInfos) {
		this.imageLoader = imageLoader;
		this.mInfos = mInfos;
		
//		options = new DisplayImageOptions.Builder()
//		.showImageOnLoading(R.drawable.defalutimg)
//		.showImageForEmptyUri(R.drawable.defalutimg)
//		.showImageOnFail(R.drawable.defalutimg)
//		.cacheInMemory(true)
//		.cacheOnDisc(true)
//		.considerExifParams(true)
//		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
//		.bitmapConfig(Bitmap.Config.RGB_565)
//		.displayer(new RoundedBitmapDisplayer(5))
//		.build();
		
		
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.griditem_vzonefunction, null);
			holder = new Holder();
			holder.iv_function = (CircularImage) convertView.findViewById(R.id.iv_function);
			holder.tv_detail = (TextView) convertView.findViewById(R.id.tv_detail);
			holder.iv_new = (ImageView) convertView.findViewById(R.id.iv_new);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
//		PhotoWallModel dj = mInfos.get(position);
//		if(dj.getPicId().equals("0000")){
//			holder.iv_function.setImageResource(R.drawable.selector_pw_btn);
//		}else{
//			imageLoader.displayImage(dj.getShortPic(), holder.iv_function, options);
//		}
//		imageLoader.displayImage("", holder.iv_function, options);
//		if(position < 3) {
		holder.iv_function.setImageResource(mPictues[position]);
//		} else {
//			holder.iv_function.setImageResource(R.drawable.vzone_03);
//		}
//		holder.iv_function.setImageResource(resId)
		holder.tv_detail.setText(mInfos[position]);
		holder.iv_new.setVisibility(View.GONE);
		// 设置tv_detail 的值 和  iv_new 是否显示
//		if(dj.getState()==2){
//			holder.tv_detail.setVisibility(View.VISIBLE);
//		}else{
//			holder.tv_detail.setVisibility(View.GONE);
//		}
		
		
		return convertView;
	}
	
	@Override
	public int getCount() {
			return mInfos.length;
	}

	@Override
	public String getItem(int arg0) {
		return mInfos[arg0];
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

//	public void addItemLast(String[] datas) {
//		mInfos.addAll(datas);
//	}
//	
//	public void addItem2Top(PhotoWallModel data) {
//		mInfos.add(1, data);
//	}
//
//	public void addItemTop(List<PhotoWallModel> datas) {
//		for (int i = datas.size()-1; i>=0; i--) {
//			PhotoWallModel info = datas.get(i);
//			mInfos.addFirst(info);
//		}
//	}
//	
//	public void reset(List<PhotoWallModel> datas) {
//		mInfos.clear();
//		PhotoWallModel vpm = new PhotoWallModel();
//		vpm.setPicId("0000");
//		mInfos.add(vpm);
//		
//		if(datas!=null&&datas.size()!=0){
//			mInfos.addAll(datas);
//		}
//	}
//	
//	public void remove(PhotoWallModel bottle) {
//		mInfos.remove(bottle);
//	}

	static class Holder {
		CircularImage iv_function;
		ImageView iv_new;
		TextView tv_detail;
	}
}