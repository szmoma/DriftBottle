package com.hnmoma.driftbottle.adapter;

import java.util.List;

import android.content.Context;
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

public class GridViewAdapter extends BaseAdapter {
	
	ImageLoader imageLoader;
	DisplayImageOptions options;
	private Context mContext;
	private List<PropsModel> mList;

	public GridViewAdapter(ImageLoader imageLoader, DisplayImageOptions options, Context mContext, List<PropsModel> mList) {
		super();
		this.imageLoader = imageLoader;
		this.options = options;
		this.mContext = mContext;
		this.mList = mList;
	}

	@Override
	public int getCount() {
		if (mList == null) {
			return 0;
		} else {
			return this.mList.size();
		}
	}

	@Override
	public PropsModel getItem(int position) {
		if (mList == null) {
			return null;
		} else {
			return this.mList.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(this.mContext).inflate(R.layout.griditem_personality, null, false);
			holder.iv_head = (ImageView) convertView.findViewById(R.id.iv_head);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_deadline = (TextView) convertView.findViewById(R.id.tv_deadline);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (this.mList != null) {
			PropsModel  propsModel = this.mList.get(position);
			imageLoader.displayImage(propsModel.getShortPic(), holder.iv_head, options);
			holder.tv_name.setText(propsModel.getProName());
			holder.tv_deadline.setText(propsModel.getPrice()+"扇贝/月");
		}
		return convertView;
	}

	private class ViewHolder {
		ImageView iv_head;
		TextView tv_name;
		TextView tv_deadline;
	}

}
