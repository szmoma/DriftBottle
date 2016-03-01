package com.hnmoma.driftbottle.adapter;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.model.MyBottleModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.way.ui.emoji.EmojiTextView;

public class TalkAdapter extends BaseAdapter {
//	private int duration=1000;  
//	private Animation slideTopToBottom,slideBottomToTop; 
	
	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	Activity act;
	
	private LinkedList<MyBottleModel> mInfos;
	int identityflag;
	SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日  HH:mm");
	
	public TalkAdapter(Activity act, ImageLoader imageLoader, int identityflag) {
		this.act = act;
		this.imageLoader = imageLoader;
		this.identityflag = identityflag;
		mInfos = new LinkedList<MyBottleModel>();
		
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.defalutimg)
				.showImageForEmptyUri(R.drawable.defalutimg)
				.showImageOnFail(R.drawable.defalutimg).cacheInMemory(true)
				.cacheOnDisc(true).considerExifParams(true)
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new RoundedBitmapDisplayer(5)).build();
//		slideTopToBottom=AnimationUtils.loadAnimation(act, R.anim.slide_top_to_bottom);  
//        slideBottomToTop=AnimationUtils.loadAnimation(act, R.anim.slide_bottom_to_top);  
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		
		if (convertView == null) {
			if (inflater == null) {
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.listitem_talk, null);
			
			holder = new Holder();
			
			holder.iv_head = (CircularImage) convertView.findViewById(R.id.iv_head);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			holder.btnDelete = (ImageButton) convertView.findViewById(R.id.bt_delete);
			holder.ll_all = (LinearLayout) convertView.findViewById(R.id.ll_all);
			holder.tv_content = (EmojiTextView) convertView.findViewById(R.id.tv_content);
			holder.iv_pic = (ImageView) convertView.findViewById(R.id.iv_pic);
			holder.tv_number = (TextView) convertView.findViewById(R.id.tv_number);
			holder.tvUserNumber = (TextView) convertView.findViewById(R.id.tv_useNum);
			
//			if(position==0){ 
//				slideBottomToTop.setDuration(duration); 
//            	convertView.setAnimation(slideBottomToTop); 
//			}else{ 
//				slideTopToBottom.setDuration(duration); 
//            	convertView.setAnimation(slideTopToBottom); 
//			}
			convertView.setTag(holder);
			
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		MyBottleModel model = mInfos.get(position);
		imageLoader.displayImage(model.getHeadImg(), holder.iv_head, options);
		holder.tv_name.setText(model.getNickName());
		holder.tv_time.setText(sdf.format(model.getCreateTime()));
		if (identityflag == 1) {
			holder.btnDelete.setVisibility(View.GONE);
		} else {
			holder.btnDelete.setVisibility(View.VISIBLE);
		}
		
		if (TextUtils.isEmpty(model.getContent())) {
			holder.tv_content.setVisibility(View.GONE);
		} else {
			holder.tv_content.setVisibility(View.VISIBLE);
			holder.tv_content.setText(model.getContent());
		}
		
		if (TextUtils.isEmpty(model.getShortPic())) {
			holder.ll_all.setBackgroundColor(act.getResources().getColor(R.color.transparent));
			holder.iv_pic.setVisibility(View.GONE);
		} else {
			holder.ll_all.setBackgroundColor(act.getResources().getColor(R.color.talkitem_bg_pic));
			holder.iv_pic.setVisibility(View.VISIBLE);
			imageLoader.displayImage(model.getShortPic(), holder.iv_pic, options);
		}
		
		holder.tv_number.setText("" + model.getReviewNum());// 评论
		Drawable drawable = act.getResources().getDrawable(R.drawable.talk_reviewnum);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		holder.tv_number.setCompoundDrawables(drawable, null, null, null);
		
		holder.tvUserNumber.setText("" + model.getUseNum());
		Drawable drawable2 = act.getResources().getDrawable(R.drawable.talk_usenum);
		drawable2.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		holder.tvUserNumber.setCompoundDrawables(drawable2, null, null, null);
		
		return convertView;
	}

	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public MyBottleModel getItem(int arg0) {
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

	public void addItemLast(List<MyBottleModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<MyBottleModel> datas) {
		for (int i = datas.size() - 1; i >= 0; i--) {
			MyBottleModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}

	public void reset(List<MyBottleModel> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}

	public void remove(MyBottleModel bottle) {
		mInfos.remove(bottle);
	}
	
	/**
	 * 把数组中的对象替换掉
	 * @param bottle
	 */
	public void replace(MyBottleModel bottle){
		if(bottle==null)
			return ;
		for(MyBottleModel model:mInfos){
			if(bottle.getBottleId().equals(model.getBottleId())){
				model = bottle;
			}
			
		}
	}

	static class Holder {
		CircularImage iv_head;
		TextView tv_name;
		TextView tv_time;
		ImageButton btnDelete;
		LinearLayout ll_all;
		EmojiTextView tv_content;
		ImageView iv_pic;
//		Button btn_support;
//		Button btn_comment;
		TextView tv_number;
		TextView tvUserNumber; //瓶子被捡到的数量
	}

}
