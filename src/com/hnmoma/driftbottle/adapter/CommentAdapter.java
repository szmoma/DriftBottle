package com.hnmoma.driftbottle.adapter;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hnmoma.driftbottle.LoginActivity;
import com.hnmoma.driftbottle.VzoneActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.model.CommentModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/*
 * 系统瓶子 评论的adapter 
 */
public class CommentAdapter extends BaseAdapter implements Serializable {

	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	String bottleId;
	private LinkedList<CommentModel> mInfos;
	
	SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");

	public CommentAdapter(ImageLoader imageLoader, DisplayImageOptions options, String bottleId) {
		this.options = options;
		this.imageLoader = imageLoader;
		this.bottleId = bottleId;
		mInfos = new LinkedList<CommentModel>();
	}
	
	public View getView(int position, View convertView, final ViewGroup parent) {
		Holder holder;
		final Context mContext = parent.getContext();
		if (convertView == null) {
			if (inflater == null) {
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.listview_comment_item, null);
			holder = new Holder();
			holder.iv_head = (CircularImage) convertView.findViewById(R.id.iv_head);
			holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_content = (TextView) convertView.findViewById(R.id.tv_cnt);
			holder.iv_vip = (ImageView) convertView.findViewById(R.id.iv_vip);
			holder.view_gap = convertView.findViewById(R.id.view_gap);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		final CommentModel cm = mInfos.get(position);
		
//		if(position == mInfos.size()) {
//			holder.view_gap.setVisibility(View.GONE);
//		} else {
//			holder.view_gap.setVisibility(View.VISIBLE);
//		}
		imageLoader.displayImage(cm.getHeadImg(), holder.iv_head, options);
		
		holder.tv_name.setText(cm.getNickName());
		if (cm.getReTime() != null) {
			holder.tv_date.setText(sdf.format(cm.getReTime()));
		} else {
			holder.tv_date.setText("");
		}
		
		holder.iv_head.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (UserManager.getInstance(mContext).getCurrentUser() == null) {
					Intent  intent = new Intent(mContext, LoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					mContext.startActivity(intent);
					return;
				}
				if(cm.getUserId().equals(UserManager.getInstance(mContext).getCurrentUserId())){
					Intent intent = new Intent(mContext, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("userId", cm.getUserId());
					intent.putExtra("identityflag", 0);
					intent.putExtra("visitUserId",  UserManager.getInstance(mContext).getCurrentUserId());
					mContext.startActivity(intent);
				} else {
					Intent intent = new Intent(mContext, VzoneActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					intent.putExtra("userId", cm.getUserId());
					intent.putExtra("identityflag", 1);
					intent.putExtra("visitUserId", UserManager.getInstance(mContext).getCurrentUserId());
					mContext.startActivity(intent);
				}
				
			}
		});
		holder.tv_content .setText(cm.getReContent());
		if(cm.getIsVIP() == 0) {
			holder.iv_vip.setVisibility(View.GONE);
			holder.tv_name.setTextColor(convertView.getContext().getResources().getColor(R.color.bottlecontent_username));
		} else {
			holder.iv_vip.setVisibility(View.VISIBLE);
			holder.tv_name.setTextColor(convertView.getContext().getResources().getColor(R.color.username_vip));
		}
		return convertView;
	}

	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public CommentModel getItem(int arg0) {
		if (arg0 < mInfos.size()) {
			return mInfos.get(arg0);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	public void addItemLast(List<CommentModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<CommentModel> datas) {
		for (int i = datas.size() - 1; i >= 0; i--) {
			CommentModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}

	public void reset(List<CommentModel> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}

	public void remove(CommentModel bottle) {
		mInfos.remove(bottle);
	}

	static class Holder {
		CircularImage iv_head;
		TextView tv_name;
		TextView tv_date;
		TextView tv_content;
		ImageView iv_vip;
		View view_gap;
	}
}