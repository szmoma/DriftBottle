package com.hnmoma.driftbottle;

import org.apache.http.Header;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.ChangeGiftModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
/**
 * 虚拟商品（Q币)：姓名，手机号
 * 实物商品：姓名、手机号、地址必填
 * 手机充值卡：运营商，姓名，手机号
 * @author Administrator
 *
 */
public class OrderActivity extends BaseActivity {

	EditText name;
	EditText phone;
	EditText address;
	Button btn_confirm;
	
	String userId;
	String csId;
	
	private RadioGroup rgOper;
	private String operType;
	
	private String username , userphone, useraddress ;
	int type;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order);
		setupView();
		
		userId = UserManager.getInstance(this).getCurrentUserId();
		csId = getIntent().getStringExtra("csId");
		type = getIntent().getIntExtra("type", 1);
		
		init(type);
		setLinstener();
	}

	/**
	 * 初始化
	 * @param type 商品类型：0表示虚拟商品，1表示实品,2表示直充<br/>
	 */
	private void init(int type) {
		// TODO Auto-generated method stub
		name.setVisibility(View.VISIBLE);
		phone.setVisibility(View.VISIBLE);
		
		
		if(type == 0) {
			if("cs1004".equalsIgnoreCase(csId)||"cs1005".equalsIgnoreCase(csId)){
				findViewById(R.id.rl_addr).setVisibility(View.GONE);
				findViewById(R.id.ll_oper).setVisibility(View.VISIBLE);
			}else{
				findViewById(R.id.rl_addr).setVisibility(View.GONE);
				findViewById(R.id.ll_oper).setVisibility(View.GONE);
				address.setText("虚拟商品不需填写此项");
			}
		} else if(type == 1)  {
			findViewById(R.id.rl_addr).setVisibility(View.VISIBLE);
			findViewById(R.id.ll_oper).setVisibility(View.GONE);
			
			address.setText("");
		 } else{
			 findViewById(R.id.rl_addr).setVisibility(View.VISIBLE);
			 findViewById(R.id.ll_oper).setVisibility(View.GONE);
				
			 name.setText("直充商品不需填写此项");
		  	 phone.setText("直充商品不需填写此项");
		  	
		 }
	}


	private void setupView() {
		// TODO Auto-generated method stub
		name = (EditText) findViewById(R.id.et_name);
		phone = (EditText) findViewById(R.id.et_phone);
		address = (EditText) findViewById(R.id.et_address);
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
		rgOper = (RadioGroup) findViewById(R.id.rg_oper);
		
		//设置默认值
		rgOper.check(R.id.oper_mobile);
		operType = ((RadioButton)rgOper.findViewById(R.id.oper_mobile)).getText().toString();
	}


	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_back:
			this.finish();
			break;

		case R.id.btn_cancel:
			name.setText("");
			phone.setText("");

			init(type);
			
			break;
		case R.id.btn_confirm:
			username = name.getText().toString().trim();
			userphone = phone.getText().toString().trim();
			
			useraddress = "";
			if("cs1004".trim().equalsIgnoreCase(csId.trim())||"cs1005".trim().equalsIgnoreCase(csId.trim())){
				useraddress = operType;
			}else
				useraddress = address.getText().toString().trim();
			
			if(type==0){
				if("cs1004".equalsIgnoreCase(csId)||"cs1005".equalsIgnoreCase(csId)){
					if(TextUtils.isEmpty(useraddress)){
						showCenterToast(getResources().getString(R.string.ask_error_addr),500);
						rgOper.setFocusable(true);
						return;
					}
					if(TextUtils.isEmpty(username)){
						showCenterToast(getResources().getString(R.string.ask_error_name),500);
						name.setFocusable(true);
						name.setFocusableInTouchMode(true);
						return;
					}
					
					if(TextUtils.isEmpty(userphone)){
						showCenterToast(getResources().getString(R.string.ask_error_tel),500);
						phone.setFocusable(true);
						phone.setFocusableInTouchMode(true);
						return;
					}
				}
				if(TextUtils.isEmpty(username)){
					showCenterToast(getResources().getString(R.string.ask_error_name),500);
					name.setFocusable(true);
					name.setFocusableInTouchMode(true);
					return;
				}
				
				if(TextUtils.isEmpty(userphone)){
					showCenterToast(getResources().getString(R.string.ask_error_tel),500);
					phone.setFocusable(true);
					phone.setFocusableInTouchMode(true);
					return;
				}
				//doSubmit(username, userphone, useraddress);
				showDialog(getResources().getString(R.string.ask_title), getResources().getString(R.string.virtual_shop));
			}else if(type==1){
				if(TextUtils.isEmpty(useraddress)){
					showCenterToast(getResources().getString(R.string.ask_error_addr),500);
					address.setFocusable(true);
					address.setFocusableInTouchMode(true);
					return;
				}
				
				if(TextUtils.isEmpty(userphone)){
					showCenterToast(getResources().getString(R.string.ask_error_tel),500);
					phone.setFocusable(true);
					phone.setFocusableInTouchMode(true);
					return;
				}
				
				if(TextUtils.isEmpty(username)){
					showCenterToast(getResources().getString(R.string.ask_error_name),500);
					name.setFocusable(true);
					name.setFocusableInTouchMode(true);
					return;
				}
				showDialog(getResources().getString(R.string.ask_title), getResources().getString(R.string.real_shop));
			}else {
				if(TextUtils.isEmpty(useraddress)||TextUtils.isEmpty(useraddress)||TextUtils.isEmpty(useraddress)){
					showCenterToast("输入的信息不能为空",500);
					return;
				}
				//doSubmit(username, userphone, useraddress);
				showDialog(getResources().getString(R.string.ask_title), getResources().getString(R.string.virtual_shop));
			}
			break;
		}
	}
	/**
	 * 显示居中的Toast
	 * @param msg 需要显示的内容
	 * @param time 显示的时间
	 */
	private void showCenterToast(String msg,int time) {
		// TODO Auto-generated method stub
		Toast toast = Toast.makeText(this, msg, time);
		toast.setGravity(Gravity.CENTER, 0, -20);
		toast.show();
	}
	
	/**
	 * 把用户名、电话以及地址提交给服务器
	 * @param username 用户名
	 * @param userphone 电话号码
	 * @param useraddress 地址
	 */
	private void doSubmit(final String username, final String userphone, final String useraddress) {
		if(!MoMaUtil.isPhone(userphone)){
				showMsg("请认真填写电话号码");
				return ;
		}
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", userId);
		jo.addProperty("csId", csId);
		jo.addProperty("deviceId", getDeviceId());
		jo.addProperty("name", username);
		jo.addProperty("phone", userphone);
		jo.addProperty("address", useraddress);
		BottleRestClient.post("changeGift", getBaseContext(), jo, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers,byte[] responseBody) {
					String str = new String(responseBody);
					if (!TextUtils.isEmpty(str)) {
						Gson gson = new Gson();
						ChangeGiftModel baseModel = gson.fromJson(str, ChangeGiftModel.class);
						if (baseModel != null && !TextUtils.isEmpty(baseModel.getCode())) {
							if ("0".equals(baseModel.getCode())) {
								//提示用户兑换成功
//								if(type == 1) {//type 商品类型：0表示虚拟商品，1表示实品,2表示直充
//									showCenterToast(getResources().getString(R.string.real_shop),Toast.LENGTH_LONG);
//								} else {
//									showCenterToast(getResources().getString(R.string.virtual_shop),Toast.LENGTH_LONG);
//								}
								showMsg("提交成功");
								Intent intent = new Intent();
						    	intent.putExtra("remaining", baseModel.getGiftMoney());
						    	setResult(RESULT_OK, intent);
								OrderActivity.this.finish();
							} else {
								showMsg("提交失败！");
							}
						} else {
							showMsg("提交失败！");
						}
						btn_confirm.setClickable(true);
					}
				}
				@Override
				public void onFailure(int statusCode, Header[] headers,
						byte[] responseBody, Throwable error) {
					showMsg("提交失败！");
					btn_confirm.setClickable(true);
				}

			});
		
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	private void setLinstener() {
		// TODO Auto-generated method stub
		rgOper.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				int id = group.getCheckedRadioButtonId();
				RadioButton rb = (RadioButton) group.findViewById(id);
				operType = rb.getText().toString();
			}
		});
	}
	/**
	 * 显示对话框
	 * @param title 显示对话框的标题
	 * @param message 显示对话框的内容
	 */
	private void showDialog(final String title,final String message){
		 		new AlertDialog.Builder(this)
		        .setIcon(R.drawable.ic_launcher)
		        .setTitle(title)
		        .setMessage(message)
		        .setPositiveButton(getResources().getString(R.string.reWrite),
		                  new DialogInterface.OnClickListener() {
		             public void onClick(DialogInterface dialog,
		            		 int whichButton) {
		                  dialog.dismiss();
		             }
		        })
		        .setNegativeButton(this.getResources().getString(R.string.submit), new DialogInterface.OnClickListener() {
		             public void onClick(DialogInterface dialog, int whichButton) {
		                  doSubmit(username, userphone, useraddress);
		                  dialog.dismiss();
		             }
		        })
		        .create()
		        .show();
	}
	
	
}
