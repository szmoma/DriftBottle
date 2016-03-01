package com.hnmoma.driftbottle.adapter;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.util.DateUtils;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.model.ChangeList;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MyGiftAdapter extends BaseAdapter {

	ImageLoader imageLoader;
	DisplayImageOptions options;
	private LayoutInflater inflater;
	Context context;
	LinkedList<ChangeList> mInfos;
	public MyGiftAdapter(ImageLoader imageLoader, DisplayImageOptions options, Context context) {
		this.imageLoader = imageLoader;
		this.options = options;
		this.context = context;
		
		mInfos = new LinkedList<ChangeList>();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mInfos.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if(convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.listitem_mygift, null);
			holder = new Holder();
			holder.iv_pic = (ImageView) convertView.findViewById(R.id.iv_pic);
			holder.tv_mz = (TextView) convertView.findViewById(R.id.tv_giftname);
			holder.tv_jg = (TextView) convertView.findViewById(R.id.tv_result);
			holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
			holder.intervalline = convertView.findViewById(R.id.intervalline);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		ChangeList dj = mInfos.get(position);
		imageLoader.displayImage(dj.getShortPic(), holder.iv_pic, options);
		holder.tv_mz.setText(dj.getCsName());
		if("0".equals(dj.getState())) {
			holder.tv_jg.setText(context.getResources().getString(R.string.tv_converting));
			holder.tv_jg.setTextColor(context.getResources().getColor(R.color.order_color));
		} else if("1".equals(dj.getState())) {
			holder.tv_jg.setText(context.getResources().getString(R.string.tv_converted));
		}  else {
			holder.tv_jg.setText(context.getResources().getString(R.string.tv_convert_failure));
		}
		
		holder.tv_date.setText(DateUtils.getTimestampString(dj.getChangeTime()));
		
//		if(position == mInfos.size() - 1) {
//			holder.intervalline.setVisibility(View.INVISIBLE);
//		}
		return convertView;
	}

	

	static class Holder {
		ImageView iv_pic;
		TextView tv_mz;
		TextView tv_jg;
		TextView tv_date;
		View intervalline;
	}
	
	public void addItemLast(List<ChangeList> datas) {
		mInfos.addAll(datas);
	}
	
	public void reset(List<ChangeList> datas){
		mInfos.clear();
		mInfos.addAll(datas);
	}

}
