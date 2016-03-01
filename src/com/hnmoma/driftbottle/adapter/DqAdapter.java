package com.hnmoma.driftbottle.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;

public class DqAdapter extends ArrayAdapter<String> {

	private LayoutInflater inflater;

	public DqAdapter(Activity activity, String[] dq) {
		super(activity, 0, dq);
		inflater = activity.getLayoutInflater();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_dq, null);
			holder = new Holder();
			holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		// 获取item对应第几条（position）的Model实体
		String dq = getItem(position);
		holder.tv_title.setText(dq);

		return convertView;
	}

	class Holder {
		TextView tv_title;
	}
}