package com.hnmoma.driftbottle.custom;

import android.app.Activity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.hnmoma.driftbottle.R;

public class NoticeMsg extends LinearLayout {
	protected Activity context;
	protected EMMessage message;
	
	TextView tv_msg;

	public NoticeMsg(Activity context) {
		super(context);
		this.context = context;
	}

	public NoticeMsg(Activity context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public NoticeMsg(Activity context, EMMessage message) {
		super(context);
		this.context = context;
		this.message = message;
		
		initView();
		initData();
	}
	
	private void initView(){
		LayoutInflater.from(context).inflate(R.layout.view_noticemsg, this);
		tv_msg = (TextView) findViewById(R.id.tv_msg);
	}
	
	public void initData(){
		TextMessageBody txtBody = (TextMessageBody) message.getBody();
		String msg = txtBody.getMessage();
		if(!TextUtils.isEmpty(msg)){
			tv_msg.setText(msg);
		}
	}
}