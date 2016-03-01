package com.hnmoma.driftbottle.adapter;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.ThrowDaxiePingActivity;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.model.MyGiftsModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class GiftAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	Activity context;
	private LinkedList<MyGiftsModel> mInfos;
	
	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
	boolean isMySelf;
	
	public GiftAdapter(ImageLoader imageLoader, DisplayImageOptions options, boolean isMySelf, Activity context) {
		this.options = options;
		this.imageLoader = imageLoader;
		mInfos = new LinkedList<MyGiftsModel>();
		this.isMySelf = isMySelf;
		this.context = context;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.listitem_gift, null);
			holder = new Holder();
			holder.iv_head = (CircularImage) convertView.findViewById(R.id.iv_head);
			holder.iv_giftpic = (ImageView) convertView.findViewById(R.id.iv_giftpic);
			holder.tv_username = (TextView) convertView.findViewById(R.id.tv_username);
			holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
			holder.tv_giftname = (TextView) convertView.findViewById(R.id.tv_giftname);
			holder.tv_giftprice = (TextView) convertView.findViewById(R.id.tv_giftprice);
			holder.tv_xg = (TextView) convertView.findViewById(R.id.tv_xg);
			holder.bt_dx = (Button) convertView.findViewById(R.id.bt_dx);
			holder.iv_vip = (ImageView) convertView.findViewById(R.id.iv_vip);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
//		if(position == 0){
//			convertView.setPadding(0, 8, 0, 0);
//		}else if(position == getCount()-1){
//			convertView.setPadding(0, 0, 0, 8);
//		}else{
//			convertView.setPadding(0, 0, 0, 0);
//		}
		
		MyGiftsModel mm = mInfos.get(position);
		imageLoader.displayImage(mm.getHeadImg(), holder.iv_head, options);
		imageLoader.displayImage(mm.getPicUrl(), holder.iv_giftpic, options);
		holder.tv_username.setText(mm.getNickName());
		
		if(mm.getGiveTime()!=null){
			holder.tv_date.setText(sdf.format(mm.getGiveTime()));
		}else{
			holder.tv_date.setText("");
		}
		
		holder.tv_giftname.setText(mm.getGiftName());
		holder.tv_giftprice.setText("价格："+mm.getPrice());
		holder.tv_xg.setText("魅力+"+mm.getCharm());
		
		if(isMySelf){
			holder.bt_dx.setVisibility(View.VISIBLE);
			holder.bt_dx.setOnClickListener(new DXClickListener(position));
		}else{
			holder.bt_dx.setVisibility(View.INVISIBLE);
		}
		
		if(mm.getIsBack() == 0){
			holder.bt_dx.setEnabled(true);
			holder.bt_dx.setText("答谢");
			holder.bt_dx.setBackgroundResource(R.drawable.selector_bluebt);
		}else{
			holder.bt_dx.setEnabled(false);
			holder.bt_dx.setText("已答谢");
			holder.bt_dx.setBackgroundResource(R.drawable.emptybt);
		}
		
		if(mm.getIsVIP() == 0) {
			holder.iv_vip.setVisibility(View.GONE);
			holder.tv_username.setTextColor(convertView.getContext().getResources().getColor(R.color.username_normal));
		} else {
			holder.iv_vip.setVisibility(View.VISIBLE);
			holder.tv_username.setTextColor(convertView.getContext().getResources().getColor(R.color.username_vip));
		}
		
		return convertView;
	}
	// 答谢瓶子的回调
	class DXClickListener implements Button.OnClickListener{
		int position;
		DXClickListener(int position) {
			this.position = position;
		}
		
		@Override
		public void onClick(View arg0) {
			MyGiftsModel mm = mInfos.get(position);
			Intent intent = new Intent(context, ThrowDaxiePingActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			intent.putExtra("userId", UserManager.getInstance(context).getCurrentUserId());
			intent.putExtra("position", position);
			intent.putExtra("myGiftsModel", mm);
			context.startActivityForResult(intent, 500);
		}
		
	}
	
	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public MyGiftsModel getItem(int arg0) {
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

	public void addItemLast(List<MyGiftsModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<MyGiftsModel> datas) {
		for (int i = datas.size()-1; i>=0; i--) {
			MyGiftsModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}
	
	public void reset(List<MyGiftsModel> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(MyGiftsModel bottle) {
		mInfos.remove(bottle);
	}

	static class Holder {
		CircularImage iv_head;
		ImageView iv_giftpic;
		TextView tv_username;
		TextView tv_giftprice;
		TextView tv_date;
		TextView tv_giftname;
		TextView tv_xg;
		Button bt_dx;
		ImageView iv_vip;
	}
}