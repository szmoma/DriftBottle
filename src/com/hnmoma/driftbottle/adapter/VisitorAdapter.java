package com.hnmoma.driftbottle.adapter;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.model.VisitorListModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class VisitorAdapter extends BaseAdapter {

	private int duration = 1000;
//	private Animation push_left_in, push_right_in;

	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	LinkedList<VisitorListModel> mInfos;

	SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");

	public VisitorAdapter(Context context,ImageLoader imageLoader, DisplayImageOptions options) {
		this.options = options;
		this.imageLoader = imageLoader;
		mInfos = new LinkedList<VisitorListModel>();
//		 push_left_in=AnimationUtils.loadAnimation(context, R.anim.item_push_left_in);  
//         push_right_in=AnimationUtils.loadAnimation(context, R.anim.item_push_right_in); 
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
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if (inflater == null) {
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.listitem_visitor, null);
			holder = new Holder();
			holder.iv_head = (CircularImage) convertView
					.findViewById(R.id.iv_head);
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			holder.tv_username = (TextView) convertView
					.findViewById(R.id.tv_username);
			holder.iv_vip = (ImageView) convertView.findViewById(R.id.iv_vip);
			
//			if (position % 2 == 0) {  
//                push_left_in.setDuration(duration);  
//                convertView.setAnimation(push_left_in);  
//            } else {  
//                push_right_in.setDuration(duration);  
//                convertView.setAnimation(push_right_in);  
//            }
			
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		VisitorListModel model = mInfos.get(position);
		if (model != null) {
			imageLoader.displayImage(model.getHeadImg(), holder.iv_head,
					options);
			holder.tv_username.setText(model.getNickName());
			if (model.getIsVIP() == 0) {
				holder.iv_vip.setVisibility(View.GONE);
				holder.tv_username.setTextColor(convertView.getContext()
						.getResources().getColor(R.color.username_normal));
			} else {
				holder.iv_vip.setVisibility(View.VISIBLE);
				holder.tv_username.setTextColor(convertView.getContext()
						.getResources().getColor(R.color.username_vip));
			}

			if (model.getVisitsTime() == null) {
				holder.tv_time.setText("");
			} else {
				holder.tv_time.setText(sdf.format(model.getVisitsTime()));
			}
		}

		return convertView;
	}

	public void addItemLast(List<VisitorListModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<VisitorListModel> datas) {
		if (datas == null) {
			return;
		}
		for (int i = datas.size() - 1; i >= 0; i--) {
			VisitorListModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}

	public void reset(List<VisitorListModel> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}

	public void remove(VisitorListModel bottle) {
		mInfos.remove(bottle);
	}

	static class Holder {
		CircularImage iv_head;
		TextView tv_username;
		TextView tv_time;
		ImageView iv_vip;
	}

}
