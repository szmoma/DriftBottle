package com.hnmoma.driftbottle.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.model.SjrwModel;

public class SjrwAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;

	private LinkedList<SjrwModel> mInfos;
	SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
	String version;
	
	public SjrwAdapter(Context ctx, String version) {
		long currentTime = System.currentTimeMillis();
		mInfos = new LinkedList<SjrwModel>();
		this.version = version;
		
		//分享到新浪微博
		SjrwModel sjm = new SjrwModel();
		sjm.setTaskId("stask1000");
		sjm.setIconId(R.drawable.sinaweibo);
		sjm.setTaskName("分享到新浪微博");
		sjm.setTaskDesc("领取200扇贝（每周一次）");
		sjm.setPrice(200);
		
		long oldTime = MyApplication.getApp().getSpUtil().getLastDateById("stask1000");
		long cot = currentTime - oldTime;
		if(cot >= 1000*60*60*24*7){
			sjm.setCanClick(true);
        }else{
        	sjm.setCanClick(false);
        }
		mInfos.add(sjm);
		
		//分享到微信朋友圈
		sjm = new SjrwModel();
		sjm.setTaskId("stask1001");
		sjm.setIconId(R.drawable.pengyq);
		sjm.setTaskName("分享到微信朋友圈");
		sjm.setTaskDesc("领取200扇贝（每周一次）");
		sjm.setPrice(200);
		
		oldTime = MyApplication.getApp().getSpUtil().getLastDateById("stask1001");
		cot = currentTime - oldTime;
		if(cot >= 1000*60*60*24*7){
			sjm.setCanClick(true);
        }else{
        	sjm.setCanClick(false);
        }
		mInfos.add(sjm);
		
		//关注官方微信
		sjm = new SjrwModel();
		sjm.setTaskId("stask1003");
		sjm.setIconId(R.drawable.wechat);
		sjm.setTaskName("关注官方微信");
		sjm.setTaskDesc("领取200扇贝");
		sjm.setPrice(200);
		
		oldTime = MyApplication.getApp().getSpUtil().getLastDateById("stask1003");
		if(oldTime==0){
			sjm.setCanClick(true);
        }else{
        	sjm.setCanClick(false);
        }
		mInfos.add(sjm);
		
		
		//====================特权处理==========================//
		String channelId="";
		try {
			ApplicationInfo appInfo = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
			String str =  appInfo.metaData.getString("UMENG_CHANNEL");
			channelId = str==null?"":str;
		} catch (NameNotFoundException e) {
		}
		
		boolean channelFlag = false;
		if(MyApplication.TQ_CHANNELS.length!=0){
			for(String channel : MyApplication.TQ_CHANNELS){
				if(channel.equals(channelId)){
					channelFlag = true;
					break;
				}
			}
		}
		
		boolean dateFlag = false;
		if(!TextUtils.isEmpty(MyApplication.TQ_ENDDATE)){
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date now = new Date();
			try {
				Date end = format.parse(MyApplication.TQ_ENDDATE);
				dateFlag = now.before(end);
			} catch (Exception e) {
				dateFlag = false;
			}
		}
		
		if(channelFlag && dateFlag){
			//独家特权
			sjm = new SjrwModel();
			sjm.setTaskId("stask1004");
			sjm.setIconId(R.drawable.tequan);
			sjm.setTaskName("乐商店独家特权");
			sjm.setTaskDesc("随机领取30—300扇贝奖励");
			sjm.setPrice(200);
			
			String ver = MyApplication.getApp().getSpUtil().getLastVersion("stask1004");
			if(ver.equals(version)){
				sjm.setCanClick(false);
	        }else{
	        	sjm.setCanClick(true);
	        }
			mInfos.add(sjm);
		}
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
			convertView = inflater.inflate(R.layout.listitem_sjrw, null);
			holder = new Holder();
			holder.iv_head = (ImageView) convertView.findViewById(R.id.iv_head);
			holder.tv_bt = (Button) convertView.findViewById(R.id.tv_bt);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);
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
		
		SjrwModel sjm = mInfos.get(position);
		holder.tv_name.setText(sjm.getTaskName());
		holder.tv_desc.setText(sjm.getTaskDesc());
		holder.iv_head.setBackgroundResource(sjm.getIconId());
		
		if(sjm.isCanClick()){
			holder.tv_bt.setSelected(false);
			holder.tv_bt.setText("立即领取");
        }else{
        	holder.tv_bt.setSelected(true);
        	holder.tv_bt.setText("已经领取");
        	holder.tv_bt.setBackgroundResource(R.drawable.selector_sjrw_done);
        }
		
		return convertView;
	}
	
	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public SjrwModel getItem(int arg0) {
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

	public void addItemLast(List<SjrwModel> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<SjrwModel> datas) {
		for (int i = datas.size()-1; i>=0; i--) {
			SjrwModel info = datas.get(i);
			mInfos.addFirst(info);
		}
	}
	
	public void reset(List<SjrwModel> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(SjrwModel bottle) {
		mInfos.remove(bottle);
	}

	static class Holder {
		ImageView iv_head;
		Button tv_bt;
		TextView tv_name;
		TextView tv_desc;
	}
}