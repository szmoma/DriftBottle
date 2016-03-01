package com.hnmoma.driftbottle.adapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.util.DateUtils;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.fragment.MyBottlePageItem;
import com.hnmoma.driftbottle.model.Bottle;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MyBottleAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	int index;
	
	private LinkedList<Bottle> mInfos;
	
	public MyBottleAdapter(ImageLoader imageLoader, DisplayImageOptions options, int index) {
		this.options = options;
		this.imageLoader = imageLoader;
		mInfos = new LinkedList<Bottle>();
		this.index = index;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.listitem_mybottle, null);
			holder = new Holder();
			holder.iv_head = (CircularImage) convertView.findViewById(R.id.iv_head);
			holder.tv_username = (TextView) convertView.findViewById(R.id.tv_username);
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			holder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);
			holder.tv_msgcnt = (TextView) convertView.findViewById(R.id.tv_msgcnt);
			holder.cb_check = (CheckBox) convertView.findViewById(R.id.cb_check);
			holder.ll_checkbg = (RelativeLayout) convertView.findViewById(R.id.ll_checkbg);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
//		if(position == 0){
//			convertView.setPadding(0, 8, 0, 0);
//		}else{
//			convertView.setPadding(0, 0, 0, 0);
//		}
		
		Bottle bottle = mInfos.get(position);
		
		//用户信息，如果最近的新字段没数据就用旧的
//		recent_userId，recent_nickName
		if(TextUtils.isEmpty(bottle.getRecent_userId())){
			imageLoader.displayImage(bottle.getHeadImg(), holder.iv_head, options);
			holder.tv_username.setText(bottle.getNickName());
			
//			String province = TextUtils.isEmpty(bottle.getProvince())?"":bottle.getProvince();
//			String city = TextUtils.isEmpty(bottle.getCity())?"未知城市":bottle.getCity();
			holder.tv_time.setText(DateUtils.getTimestampString(bottle.getCreateTime()));
		}else{
			imageLoader.displayImage(bottle.getRecent_headImg(), holder.iv_head, options);
			holder.tv_username.setText(bottle.getRecent_nickName());
			
//			String province = TextUtils.isEmpty(bottle.getRecent_province())?"":bottle.getRecent_province();
//			String city = TextUtils.isEmpty(bottle.getRecent_city())?"未知城市":bottle.getRecent_city();
			holder.tv_time.setText(DateUtils.getTimestampString(bottle.getCreateTime()));
		}
		
//		String cnt = bottle.getContent();
//		cnt = cnt==null?"":cnt;
//		holder.tv_desc.setText(cnt.trim());
		
		String msgType = bottle.getMessageType();
		String msg = bottle.getMessage();
		
		if(TextUtils.isEmpty(msg)){
			msg = bottle.getContent();
			msg = msg==null?"":msg;
		}
		
		if(!TextUtils.isEmpty(msgType)){
			String str = "";
			
			if(msgType.equals("5001")){
				str = "[图片] ";
			}else if(msgType.equals("5002")){
				str = "[贺卡] ";
			}else if(msgType.equals("5003")){
				str = "[视频] ";
			}else if(msgType.equals("5004")){
				str = "[语音] ";
			}
			
			holder.tv_desc.setText(str + msg.trim());
		}else{
			holder.tv_desc.setText(msg.trim());
		}
		
		if(bottle.getMsgCount()<=0){
			holder.tv_msgcnt.setVisibility(View.GONE);
		}else{
			holder.tv_msgcnt.setVisibility(View.VISIBLE);
			holder.tv_msgcnt.setText(""+bottle.getMsgCount());
		}
		
		//是否显示多选
		if(MyBottlePageItem.showMng[index]){
			holder.ll_checkbg.setVisibility(View.VISIBLE);
			boolean isChecked = false;
			if(bottle.getMngIsChecked()!=null){
				isChecked = bottle.getMngIsChecked();
			}
			holder.cb_check.setChecked(isChecked);
		}else{
			holder.ll_checkbg.setVisibility(View.GONE);
		}
		
		return convertView;
	}
	
	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public Bottle getItem(int arg0) {
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

	public void addItemTop(List<Bottle> datas) {
		for (int i = datas.size()-1; i>=0; i--) {
			Bottle info = datas.get(i);
			mInfos.addFirst(info);
		}
	}
	
	public void addItemLast(List<Bottle> datas) {
		mInfos.addAll(datas);
	}

	public void reset(List<Bottle> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(Bottle bottle) {
		mInfos.remove(bottle);
	}

	public void resetCheck(int position){
		for(int i = 0; i < mInfos.size(); i++){
			Bottle bottle = mInfos.get(i);
			
			if(i == position){
				bottle.setMngIsChecked(true);
			}else{
				bottle.setMngIsChecked(false);
			}
		}
	}
	
	public List<Bottle> delCheck(){
		List<Bottle> delBottle = new ArrayList<Bottle>();
		
		for(Bottle bottle : mInfos){
			if(bottle.getMngIsChecked()!=null && bottle.getMngIsChecked()){
				delBottle.add(bottle);
			}
		}
		
		if(delBottle.size()!=0){
			mInfos.removeAll(delBottle);
		}
		
		return delBottle;
	}
	
	static class Holder {
		CircularImage iv_head;
		TextView tv_username;
		TextView tv_time;
		TextView tv_desc;
		TextView tv_msgcnt;
		CheckBox cb_check;
		RelativeLayout ll_checkbg;
	}
}