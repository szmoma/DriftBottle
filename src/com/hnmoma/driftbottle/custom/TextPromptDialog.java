package com.hnmoma.driftbottle.custom;

import com.hnmoma.driftbottle.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TextPromptDialog extends Dialog {

	Context context;
	Button cancel;
	Button confirm;
	TextView tv_content;
	String content;
	public TextPromptDialog(Context context) {
		super(context);
		this.context = context;
		// TODO Auto-generated constructor stub
	}
	
	public TextPromptDialog(Context context, int theme, String content) {
		super(context, theme);
		this.context = context;
		this.content = content;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.dialog_prompt);
		this.setCancelable(true);
		this.setCanceledOnTouchOutside(false);
		
		cancel = (Button) findViewById(R.id.btn_cancel);
		confirm = (Button) findViewById(R.id.btn_confirm);
		tv_content = (TextView) findViewById(R.id.tv_content);
		tv_content.setText(content);
		cancel.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				TextPromptDialog.this.cancel();
			}
		});
		
		
	}
	
	
	
	public void setOnConfirmClick(Button.OnClickListener  click) {
			confirm.setOnClickListener(click);
	}
	
	
}
