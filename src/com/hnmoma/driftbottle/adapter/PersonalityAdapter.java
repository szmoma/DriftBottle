package com.hnmoma.driftbottle.adapter;

import java.util.HashMap;
import java.util.List;

import com.hnmoma.driftbottle.DjDetailActivity;
import com.hnmoma.driftbottle.PropertyStoreActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.custom.CustomPersonGridView;
import com.hnmoma.driftbottle.model.PropsModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PersonalityAdapter extends BaseAdapter {

	ImageLoader imageLoader;
	DisplayImageOptions options;
	LayoutInflater inflater;
	Context mContext;
	private HashMap<Integer, List<PropsModel>> mList; 
	//定义两个int常量标记不同的Item视图
//	public static final int ITEM_TEXT = 0;
//	public static final int ITEM_IMAGE = 1;
//	String[] titles = new String[] {"VIP道具", "聊天气泡", "主页特效"};
//	List<PropsModel> propertyList;
	
	public PersonalityAdapter(ImageLoader imageLoader, DisplayImageOptions options, Context context) {
		super(); 
		this.imageLoader = imageLoader;
		this.options = options;
		mContext = context;
		inflater = LayoutInflater.from(mContext);
		this.mList =new HashMap<Integer, List<PropsModel>>(); ;
	}

	@Override
	public int getCount() {
		if (mList == null) { 
			return 0; 
		} else { 
			return this.mList.size(); 
		} 
	}

	@Override
	public Object getItem(int position) {
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
			convertView =inflater.inflate(R.layout.listview_item_personality, parent, false); 
			holder.tv_title = (TextView) convertView.findViewById(R.id.listview_item_title); 
			holder.gridView = (CustomPersonGridView) convertView.findViewById(R.id.listview_item_gridview); 
			convertView.setTag(holder); 
		} else { 
			holder = (ViewHolder) convertView.getTag(); 
		} 

		if (this.mList != null) { 
			if (holder.tv_title != null) { 
				holder.tv_title.setText("VIP道具");
			} 
			if (holder.gridView != null) { 
				final List<PropsModel> arrayListForEveryGridView = this.mList.get(1); 
				GridViewAdapter gridViewAdapter=new GridViewAdapter(imageLoader, options, mContext, arrayListForEveryGridView); 
				holder.gridView.setAdapter(gridViewAdapter); 
				holder.gridView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Intent intent = new Intent(mContext, DjDetailActivity.class);
						intent.putExtra("propsModel", arrayListForEveryGridView.get(position));
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						mContext.startActivity(intent);
						((Activity)mContext).startActivityForResult(intent, PropertyStoreActivity.REQ_CODE_BUY);
					}
				});
			} 
		} 
		return convertView;
	}

	//各个布局的控件资源
	class ViewHolder{
		TextView tv_title; 
		CustomPersonGridView gridView;
	}

	public void addItemLast(List<PropsModel> propertyList2) {
		this.mList.put(1, propertyList2);
	}

}
