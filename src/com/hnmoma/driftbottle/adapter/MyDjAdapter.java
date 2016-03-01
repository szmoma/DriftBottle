package com.hnmoma.driftbottle.adapter;

import java.util.ArrayList;
import java.util.Date;
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

public class MyDjAdapter extends BaseAdapter {

	protected ImageLoader imageLoader;
	DisplayImageOptions options;
	Context context;
	private List<PropsModel> mList;
	
	public MyDjAdapter(ImageLoader imageLoader, DisplayImageOptions options, Context context) {
		this.imageLoader = imageLoader;
		this.options = options;
		this.context = context;
		mList = new ArrayList<PropsModel>();
	}

	@Override
	public int getCount() {
		if (mList == null) {
			return 0;
		} else {
			return mList.size();
		}
	}

	@Override
	public PropsModel getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.griditem_personality, parent, false);
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
			if(propsModel.getUseType() == 0) {
				Date nowTime = new Date();
				int  margin = (int) ((propsModel.getEndTime().getTime() - nowTime.getTime()) / (1000*60*60*24));
				
				holder.tv_deadline.setText("还剩" + margin+"天");
			} else {// 1
				holder.tv_deadline.setText("还剩" + propsModel.getNum()+"个");
			}
			
		}
		return convertView;
	}

	private class ViewHolder {
		ImageView iv_head;
		TextView tv_name;
		TextView tv_deadline;
	}
	
	public void addItemLast(List<PropsModel> propertyList) {
		this.mList.addAll(propertyList);
	}

}
