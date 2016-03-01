package com.hnmoma.driftbottle.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.hnmoma.driftbottle.model.MsgModel;
import com.hnmoma.driftbottle.util.SmileUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.way.ui.emoji.EmojiTextView;

public class TalkMsgAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
	Activity act;
	
	private LinkedList<MsgModel> mInfos;
	
	public TalkMsgAdapter(Activity act, ImageLoader imageLoader, DisplayImageOptions options) {
		this.act = act;
		this.options = options;
		this.imageLoader = imageLoader;
		mInfos = new LinkedList<MsgModel>();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.listitem_talkmsg, null);
			holder = new Holder();
			holder.iv_head = (CircularImage) convertView.findViewById(R.id.iv_head);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			holder.iv_pic = (ImageView) convertView.findViewById(R.id.iv_pic);
			holder.tv_usedcontent = (EmojiTextView) convertView.findViewById(R.id.tv_usedcontent);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		final MsgModel dj = mInfos.get(position);
		imageLoader.displayImage(dj.getHeadImg(), holder.iv_head, options);
		holder.iv_head.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent reply = new Intent(act, VzoneActivity.class);
				reply.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				reply.putExtra("userId", dj.getUserId());
				act.startActivity(reply);
			}
		});
		holder.tv_name.setText(dj.getNickName());
		if("1000".equals(dj.getAction())) {
			holder.tv_content.setText("赞了你");
		} else {
			holder.tv_content.setText(dj.getReContent());
		}
		holder.tv_time.setText(sdf.format(new Date(dj.getCreateTime())));
		
		if("5001".equals(dj.getBottleContentType())) {
			holder.tv_usedcontent.setVisibility(View.GONE);
			
			if(TextUtils.isEmpty(dj.getShortPic())){
				holder.iv_pic.setVisibility(View.GONE);
			}else{
				holder.iv_pic.setVisibility(View.VISIBLE);
				imageLoader.displayImage(dj.getShortPic(), holder.iv_pic, options);
			}
		} else if("5000".equals(dj.getBottleContentType())) {
			holder.tv_usedcontent.setVisibility(View.VISIBLE);
			holder.iv_pic.setVisibility(View.GONE);
			if(TextUtils.isEmpty(dj.getBottleContent()))
				holder.tv_usedcontent.setText("");
			else
				holder.tv_usedcontent.setText(SmileUtils.getSmiledText(act, dj.getBottleContent()),BufferType.SPANNABLE);
		} 
		return convertView;
	}
	
	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public MsgModel getItem(int arg0) {
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

	public void addItemLast(List<MsgModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<MsgModel> datas) {
		for (int i = datas.size()-1; i>=0; i--) {
			MsgModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}
	
	public void reset(List<MsgModel> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(MsgModel bottle) {
		mInfos.remove(bottle);
	}

	static class Holder {
		CircularImage iv_head;
		TextView tv_name;
		TextView tv_content;
		TextView tv_time;
		ImageView 	iv_pic;
		EmojiTextView tv_usedcontent;
	}

}
