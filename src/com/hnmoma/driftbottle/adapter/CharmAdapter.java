package com.hnmoma.driftbottle.adapter;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.model.StoreModel;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CharmAdapter extends BaseAdapter {

	ImageLoader imageLoader;
	DisplayImageOptions options;
	private LayoutInflater inflater;
	private int Displaywidth;
	
	private LinkedList<StoreModel> mInfos;
	
	public CharmAdapter(Context context, ImageLoader imageLoader, DisplayImageOptions options) {
		this.imageLoader = imageLoader;
		this.options = options;
		this.Displaywidth = (((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth()-MoMaUtil.dip2px(context, 40))/3;
		
		mInfos = new LinkedList<StoreModel>();
	}

	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return mInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if(convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.listitem_charm, null);
			holder = new Holder();
			holder.iv_head = (ImageView) convertView.findViewById(R.id.iv_head);
			holder.tv_mz = (TextView) convertView.findViewById(R.id.tv_mz);
			holder.tv_jg = (TextView) convertView.findViewById(R.id.tv_jg);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		StoreModel dj = mInfos.get(position);
		LayoutParams params = holder.iv_head.getLayoutParams();
		params.width = Displaywidth;
		params.height = Displaywidth;
		holder.iv_head.setLayoutParams(params);
		imageLoader.displayImage(dj.getShortPic(), holder.iv_head, options);
		holder.tv_mz.setText(dj.getCsName());
		holder.tv_jg.setText(dj.getPrice()+"个礼券");
		
		return convertView;
	}
	
	public void addItemLast(List<StoreModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<StoreModel> datas) {
		for (int i = datas.size()-1; i>=0; i--) {
			StoreModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}
	
	public void reset(List<StoreModel> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(StoreModel bottle) {
		mInfos.remove(bottle);
	}

	static class Holder {
		ImageView iv_head;
		TextView tv_mz;
		TextView tv_jg;
	}
	
}
