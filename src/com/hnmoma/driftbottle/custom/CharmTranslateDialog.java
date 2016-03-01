package com.hnmoma.driftbottle.custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hnmoma.driftbottle.R;

public class CharmTranslateDialog extends Dialog {

	Context context;
	EditText charmValue;
	Button cancel;
	Button confirm;
	String inputValue;
	
	public CharmTranslateDialog(Context context) {
		super(context);
		this.context = context;
		// TODO Auto-generated constructor stub
	}
	
	public CharmTranslateDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.dialog_charmtranslate);
		this.setCancelable(true);
		this.setCanceledOnTouchOutside(false);
		
		charmValue = (EditText) findViewById(R.id.et_charmvalue);
		cancel = (Button) findViewById(R.id.btn_cancel);
		confirm = (Button) findViewById(R.id.btn_confirm);
		
		cancel.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				CharmTranslateDialog.this.cancel();
			}
		});
		
		charmValue.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				inputValue =  charmValue.getText().toString().trim();
			}
		});
		
	}
	
	public void setOnConfirmClick(Button.OnClickListener  click) {
			confirm.setOnClickListener(click);
	}
	
	public String getValue() {
		return inputValue;
	}
	
}
