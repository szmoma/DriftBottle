package com.hnmoma.driftbottle.adapter;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.model.GameMsgModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class GameMsgAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	Activity context;
	private LinkedList<GameMsgModel> mInfos;
	
	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
	boolean isMySelf;
	
	public GameMsgAdapter(ImageLoader imageLoader, DisplayImageOptions options, Activity context) {
		this.options = options;
		this.imageLoader = imageLoader;
		mInfos = new LinkedList<GameMsgModel>();
		this.context = context;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.listitem_game, null);
			holder = new Holder();
			holder.iv_head = (CircularImage) convertView.findViewById(R.id.iv_head);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);
			
			holder.view_1 = (TextView) convertView.findViewById(R.id.view_1);
			holder.view_2 = (LinearLayout) convertView.findViewById(R.id.view_2);
			holder.view_3 = (Button) convertView.findViewById(R.id.view_3);
			
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
		
		GameMsgModel mm = mInfos.get(position);
		imageLoader.displayImage(mm.getHeadImg(), holder.iv_head, options);
		holder.tv_name.setText(mm.getNickName());
		holder.tv_desc.setText(mm.getMoney()+"扇贝");
		
		int isHost = mm.getIsHost();
		int state = mm.getState();
		String result = mm.getResult();
		
		if(isHost == 1){//对方是发起方
			if(state == 0){//显示应战，拒绝按钮
				holder.view_2.setVisibility(View.VISIBLE);
				
				holder.view_1.setVisibility(View.GONE);
				holder.view_3.setVisibility(View.GONE);
			}else if(state == 1 || state == 2){//完成未查看//已查看,显示结果
				holder.view_1.setVisibility(View.VISIBLE);
				
				holder.view_3.setVisibility(View.GONE);
				holder.view_2.setVisibility(View.GONE);
				
				if("0".equals(result)){
					holder.view_1.setText("败北");
				}else if("1".equals(result)){
					holder.view_1.setText("平手");
				}else if("2".equals(result)){
					holder.view_1.setText("胜利");
				}
			}else if(state == 3){//拒绝
				holder.view_1.setVisibility(View.VISIBLE);
				
				holder.view_3.setVisibility(View.GONE);
				holder.view_2.setVisibility(View.GONE);
				
				holder.view_1.setText("拒绝");
			}
		}else{//对方是接收方
			if(state == 0){//显示应战，拒绝按钮
				holder.view_1.setVisibility(View.VISIBLE);
				
				holder.view_3.setVisibility(View.GONE);
				holder.view_2.setVisibility(View.GONE);
				
				holder.view_1.setText("等待应战");
			}else if(state == 1){//完成未查看，显示查看按钮
				holder.view_3.setVisibility(View.VISIBLE);
				
				holder.view_1.setVisibility(View.GONE);
				holder.view_2.setVisibility(View.GONE);
			}else if(state == 2){//已查看,显示结果
				holder.view_1.setVisibility(View.VISIBLE);
				
				holder.view_3.setVisibility(View.GONE);
				holder.view_2.setVisibility(View.GONE);
				
				if(result.equals("0")){
					holder.view_1.setText("败北");
				}else if(result.equals("1")){
					holder.view_1.setText("平手");
				}else if(result.equals("2")){
					holder.view_1.setText("胜利");
				}
			}else if(state == 3){//拒绝
				holder.view_1.setVisibility(View.VISIBLE);
				
				holder.view_3.setVisibility(View.GONE);
				holder.view_2.setVisibility(View.GONE);
				
				holder.view_1.setText("拒绝");
			}
		}
		
		return convertView;
	}
	
	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public GameMsgModel getItem(int arg0) {
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

	public void addItemLast(List<GameMsgModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<GameMsgModel> datas) {
		for (int i = datas.size()-1; i>=0; i--) {
			GameMsgModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}
	
	public void reset(List<GameMsgModel> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(GameMsgModel bottle) {
		mInfos.remove(bottle);
	}

	static class Holder {
		CircularImage iv_head;
		TextView tv_name;
		TextView tv_desc;
		
		TextView view_1;
		LinearLayout view_2;
		Button view_3;
	}
}