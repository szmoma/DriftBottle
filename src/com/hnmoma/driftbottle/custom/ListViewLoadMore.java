package com.hnmoma.driftbottle.custom;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.hnmoma.driftbottle.R;
/**
 * listView是否记载更多，可以嵌套在ScrollView中使用
 * @author yangsy
 *
 */
public class ListViewLoadMore extends ListView implements OnScrollListener {

	View footView;
	int lastItem; //// 最后一项
	int totalItemCount; //// 此刻一共有多少项
	boolean isLoading=false;
	IsLoadingListener isLoadingListener;

	public ListViewLoadMore(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public ListViewLoadMore(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public ListViewLoadMore(Context context) {
		super(context);
		initView(context);
	}

	/**
	 * 初始化footView
	 * 
	 * @param context
	 */
	void initView(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		footView = inflater.inflate(R.layout.foot_layout, null);
		addFooterView(footView);
		footView.findViewById(R.id.foot_layout).setVisibility(View.GONE);
		setOnScrollListener(this);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

		if (lastItem == totalItemCount && scrollState == SCROLL_STATE_IDLE) {
			if (isLoading) {
				footView.findViewById(R.id.foot_layout).setVisibility(View.VISIBLE);
				isLoadingListener.onLoad();
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		lastItem = firstVisibleItem + visibleItemCount;
		this.totalItemCount = totalItemCount;
	}
	
	public void setOnLoadingListener(IsLoadingListener isLoadingListener){
		this.isLoadingListener=isLoadingListener;
	}
	
	public interface IsLoadingListener{
		public void onLoad();
	}
	
	public void complateLoad(){
		isLoading=false;
		footView.findViewById(R.id.foot_layout).setVisibility(View.GONE);
	}
	public void setLoading(boolean isLoading){
		this.isLoading = isLoading;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE>>2, MeasureSpec.AT_MOST);
		
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

}
