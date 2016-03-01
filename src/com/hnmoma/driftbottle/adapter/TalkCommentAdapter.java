package com.hnmoma.driftbottle.adapter;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.hnmoma.driftbottle.VzoneActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.model.ReviewListModel;
import com.hnmoma.driftbottle.util.SmileUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class TalkCommentAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	LinkedList<ReviewListModel> mInfos;
	Activity act;
	
	SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
	
	public TalkCommentAdapter(Activity act, ImageLoader imageLoader, DisplayImageOptions options) {
		this.act = act;
		this.imageLoader = imageLoader;
		this.options = options;
		this.mInfos = new LinkedList<ReviewListModel>();
	}
	
	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public ReviewListModel getItem(int position) {
		return mInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if (inflater == null) {
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.listitem_talkcomment, null);
			holder = new Holder();
			holder.tv_username = (TextView) convertView.findViewById(R.id.tv_username);
			holder.tv_reply = (TextView) convertView.findViewById(R.id.tv_reply);
			holder.tv_friend = (TextView) convertView.findViewById(R.id.tv_friend);
			holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
			holder.tv_time =(TextView) convertView.findViewById(R.id.tv_time);
			holder.iv_head = (CircularImage) convertView.findViewById(R.id.iv_commenthead);
			holder.iv_vip = (ImageView) convertView.findViewById(R.id.iv_vip);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		ReviewListModel model = mInfos.get(position);
		
		imageLoader.displayImage(model.getHeadImg(), holder.iv_head, options);
		holder.tv_time.setText(sdf.format(model.getCreateTime()));
		holder.tv_username.setText(model.getNickName());

		if(model.getIsVIP() == 0) {
			holder.iv_vip.setVisibility(View.GONE);
			holder.tv_username.setTextColor(act.getResources().getColor(R.color.bottlecontent_username));
		} else {
			holder.iv_vip.setVisibility(View.VISIBLE);
			holder.tv_username.setTextColor(act.getResources().getColor(R.color.username_vip));
		}
//		holder.tv_username.setOnClickListener(new Button.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				String userId = mInfos.get(position).getUserId();
//				Intent reply = new Intent(act, OtherVzoneActivity.class);
//				reply.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//				reply.putExtra("userId", userId);
//				act.startActivity(reply);
//			}
//			
//		});
		if(TextUtils.isEmpty(model.getToUserId())) {
			holder.tv_reply.setVisibility(View.GONE);
			holder.tv_friend.setVisibility(View.GONE);
			holder.tv_content.setText(model.getReContent());
		} else {
			holder.tv_reply.setVisibility(View.VISIBLE);
			holder.tv_friend.setVisibility(View.VISIBLE);
			holder.tv_friend.setText(model.getToNickName());
			holder.tv_friend.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View v) {
					String userId = mInfos.get(position).getToUserId();
					Intent reply = new Intent(act, VzoneActivity.class);
					reply.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					reply.putExtra("userId", userId);
					act.startActivity(reply);
				}
				
			});
			holder.tv_content.setText(": " + SmileUtils.getSmiledText(act, model.getReContent()),BufferType.SPANNABLE);
		}
	
		return convertView;
	}
	
	public void addItemLast(List<ReviewListModel> datas) {
		if(datas!=null&&datas.size()!=0) {
			mInfos.addAll(datas);
		}
	}
	
	public void addItem2Top(ReviewListModel data) {
		mInfos.add(1, data);
	}

	public void addItemTop(List<ReviewListModel> datas) {
		if(datas!=null&&datas.size()!=0) {
			for (int i = datas.size()-1; i>=0; i--) {
				ReviewListModel info = datas.get(i);
				mInfos.addFirst(info);
			}
		}
	}
	
	public void reset(List<ReviewListModel> datas) {
		mInfos.clear();
		if(datas!=null&&datas.size()!=0){
			mInfos.addAll(datas);
		}
	}
	
	public void remove(ReviewListModel bottle) {
		mInfos.remove(bottle);
	}

	
	static class Holder {
		CircularImage iv_head;
		TextView tv_time;
		TextView tv_username;
		TextView tv_reply;
		TextView tv_friend;
		TextView tv_content;
		ImageView iv_vip;
	}

}
