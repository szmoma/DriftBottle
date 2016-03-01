package com.hnmoma.driftbottle;

import org.apache.http.Header;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.model.TousuModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.loopj.android.http.AsyncHttpResponseHandler;
/**
 * 举报页面
 *
 */
public class VzoneTousuActivity extends BaseActivity {
	
	RadioGroup rg_reason;
	TousuModel tousuModel;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("tousuModel", tousuModel);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null){
			tousuModel = (TousuModel) savedInstanceState.getSerializable("tousuModel");
		}
		
		Intent intent = getIntent();
		if(intent != null){
			tousuModel = (TousuModel) intent.getSerializableExtra("tousuModel");
		}
		
		if(tousuModel==null){
			this.finish();
			return;
		}
		
		if(!TextUtils.isEmpty(tousuModel.getContent())){
			MoMaLog.e("举报内容：", tousuModel.getContent());
		}
		
		setContentView(R.layout.activity_tousu);
		initView();
	}
	
	private void initView(){
		rg_reason = (RadioGroup)findViewById(R.id.rg_reason);
//		rg_reason.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			
//			@Override
//			public void onCheckedChanged(RadioGroup arg0, int arg1) {
//				String msg = ((RadioButton)arg0.findViewById(arg1)).getText().toString();
//				showMsg(msg);
//			}
//		});
	}
	
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.bt_back:
				onBackPressed();
				break;
			case R.id.bt_ok:
				String resultString = getCheckStringById(rg_reason.getCheckedRadioButtonId());
				// showMsg(resultString);
				MoMaLog.d("举报内容", resultString);
				
				if(TextUtils.isEmpty(resultString)){
					showMsg("请选择举报模块");
					return;
				}
				
				JsonObject jo = new JsonObject();
				jo.addProperty("userId", tousuModel.getUserId());
				jo.addProperty("type", resultString);
				jo.addProperty("bUserId", tousuModel.getbUserId());
				jo.addProperty("reportType", tousuModel.getReportType());
				jo.addProperty("content", tousuModel.getContent());
				jo.addProperty("id", tousuModel.getId());
				
				BottleRestClient.post("addNewReport", this, jo, new AsyncHttpResponseHandler(){
					@Override
					public void onStart() {
						super.onStart();
					}
					
					@Override
					public void onFinish() {
						super.onFinish();
					}
	
					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
						showMsg("举报成功");
						setResult(RESULT_OK);
						VzoneTousuActivity.this.finish();
					}
	
					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
						Log.d("举报失败：", error.getMessage());
						showMsg("举报失败");
					}
		        });
					
				break;
		}
	}
	
	public String getCheckStringById(int checkedId) {
		if(checkedId==-1){
			return "";
		}
		
		String resultString = "";
		
		switch (checkedId) {
		case R.id.rb_11:
			resultString = "1005";
			break;
		case R.id.rb_12:
			resultString = "1006";
			break;
		case R.id.rb_13:
			resultString = "1007";
			break;
		
		}
		
		return resultString;
	}

//	@Override
//	public void onCheckedChanged(RadioGroup group, int checkedId) {
//		switch (group.getCheckedRadioButtonId()) {
//		case R.id.rb_11:
//			resultString = "1001";
//			break;
//		case R.id.rb_12:
//			resultString = "1002";
//			break;
//		case R.id.rb_13:
//			resultString = "1003";
//			break;
//		case R.id.rb_14:
//			resultString = "1004";
//			break;
//		}
//	}
}