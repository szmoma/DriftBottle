package com.hnmoma.driftbottle;

import java.util.Random;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.BaseModelEx;
import com.hnmoma.driftbottle.model.GameOpponentModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;

/**
 * 游戏瓶子，再次挑战出拳界面
 * @author MOMA
 *
 */
public class Game_cq_tz extends BaseActivity implements OnClickListener {
	
	Random random = new Random();
	RadioGroup rg_money,rg_01,rg_02,rg_03;
	
	GameOpponentModel model;
	
	private int domoney;	//提交的价格
	private int no1=-1,no2=-1,no3=-1;//第一、二、三局的值
	
	private  boolean isFromChat;	//是否从聊天页面到达此页面，如果该值是true，则需要返回值
	private String  toUserId;//被挑战对象
	private String  fromUserId;	//挑战对象
	private boolean isThrowable = true;//扔瓶子的状态,默认情况下是可仍的；扔过一次后，就不允许再扔

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.throw_yxpz);
		
		Intent intent = getIntent();
		if(intent!=null){
			model = (GameOpponentModel) intent.getSerializableExtra("GameOpponentModel");
			toUserId = intent.getStringExtra("userId");
			fromUserId = intent.getStringExtra("visitUserId");
			isFromChat = intent.getBooleanExtra("fromChat", false);
		}
		
		rg_money = (RadioGroup) findViewById(R.id.rg_money);
		rg_01 = (RadioGroup) findViewById(R.id.rg_01);
		rg_02 = (RadioGroup) findViewById(R.id.rg_02);
		rg_03 = (RadioGroup) findViewById(R.id.rg_03);
		
		//RadioGroup默认，并默认提交值
		rg_money.check(R.id.rb_12);
		String str_money = ((RadioButton)rg_money.findViewById(R.id.rb_12)).getText().toString();//带有单位的额度
		domoney = transMoney(str_money);
				
		setLinstener();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_close:
				this.finish();
				break;
			case R.id.bt_ok:
				//扔之前检查内容是否合法
				if(check()){
					checkMyMoney();
				}
				break;
			case R.id.ib_jx:
				clearRadioButtonState(rg_01);
				clearRadioButtonState(rg_02);
				clearRadioButtonState(rg_03);
				
				int checkId = getRandomId();
				((RadioButton)rg_01.findViewById(checkId)).setChecked(true);
				no1 = getNumById(checkId);
				
				checkId = getRandomId();
				((RadioButton)rg_02.findViewById(checkId)).setChecked(true);
				no2 = getNumById(checkId);
				
				checkId = getRandomId();
				((RadioButton)rg_03.findViewById(checkId)).setChecked(true);
				no3 = getNumById(checkId);
				
				break;
		}
	}
	/**
	 * 检查我的扇贝，如果缓存扇贝不够，则从服务器上更新资源，获取用户的最新扇贝。如果扇贝足够，则提交，否则不允许提交
	 */
	private void checkMyMoney(){
		int myMoney = MyApplication.getApp().getSpUtil().getMyMoney();
		if(myMoney<domoney){
			//重新查询用户的扇贝
			JsonObject jo = new JsonObject();
			jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
			BottleRestClient.post("queryShanbei", this, jo, new AsyncHttpResponseHandler(){

				@Override
				public void onSuccess(int statusCode,
						Header[] headers,
						byte[] responseBody) {
					// TODO Auto-generated method stub
					String res= new String(responseBody);
					try {
						JSONObject obj = new JSONObject(res);
						if("0".equals(obj.getString("code"))){
							String str_money = obj.getString("shanbei");
							if(str_money==null)
								return ;
							if(!MoMaUtil.isDigist(str_money)){
								MoMaLog.e("debug", "不是数据类型的字符串");
								str_money="0";
							}
							int myMoney = Integer.valueOf(str_money);
							MyApplication.getApp().getSpUtil().setMyMoney(myMoney);
							if(myMoney>=domoney)
								doSubmit();
							else{
								showMsg("亲，扇贝不足");
							}
							
						}else{
							showMsg(res);
						}
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						showMsg("系统错误");
					}
					
				}

				@Override
				public void onFailure(int statusCode,
						Header[] headers,
						byte[] responseBody, Throwable error) {
					// TODO Auto-generated method stub
					showMsg(new String(responseBody));
				}});
			
		}else	{						
			doSubmit();
		}
	}
	
	private int getRandomId(){
		int checkId = R.id.rb_st;
		
		int checkNo = random.nextInt(3);
		switch (checkNo) {
		case 0:
			checkId = R.id.rb_st;
			break;
		case 1:
			checkId = R.id.rb_jd;
			break;
		case 2:
			checkId = R.id.rb_bu;
			break;
		}
		
		return checkId;
	}
	
	/**
	 * result true，内容没问题
	 * result false，内容有问题
	 */
	public boolean check(){
		boolean result = true;
		
		if(UserManager.getInstance(this).getCurrentUser()==null){
			Intent intent = new Intent(this, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			startActivityForResult(intent, 400);
			showMsg(getResources().getString(R.string.unlogin_msg));
			return result = false;
		}
		
		
		//检查网络
		if(!MoMaUtil.isNetworkAvailable(this)){
			showMsg("当前网络不可用，请检查");
			return result = false;
		}
		
		//检查3局是否出拳
		if(no1<0 || no2<0 || no3<0){
			showMsg("请先选择出拳");
			return result = false;
		}
		
		if(domoney==-1){
			showMsg("请先选择额度");
			return false;
		}
		result = isThrowable;
		return result;
	}
	
	private int getNumById(int id){
		int num = 0;
		
		switch (id) {
		case R.id.rb_st:
			num = 0;
			break;
		case R.id.rb_jd:
			num = 1;
			break;
		case R.id.rb_bu:
			num = 2;
			break;
		}
		
		return num;
	}
	
	
	
	/**
	 * result true，提交没问题
	 * result false，提交有问题
	 */
	public void doSubmit(){
		isThrowable = false;
		final String cnt = no1+"|"+no2+"|"+no3;
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("content", cnt);
		jo.addProperty("money", domoney);
		jo.addProperty("bottleType", "4009");
		jo.addProperty("isFirst", "1");
		if(isFromChat){
			jo.addProperty("toUserId", toUserId);
			jo.addProperty("comeType", 1001);
		}else{
			if(model!=null)
				jo.addProperty("toUserId", model.getUserId());
			jo.addProperty("comeType", 1000);
		}
		
		jo.addProperty("come", "6000");
		
		BottleRestClient.post("throwGameBottle", this, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				super.onStart();
				showDialog("正在下战书...", "挑战失败", 15*100);
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				closeDialog(mpDialog);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					BaseModelEx baseModel = gson.fromJson(str, BaseModelEx.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
						if("0".equals(baseModel.getCode())){
							//更新缓存：扇贝
							int money = MyApplication.getApp().getSpUtil().getMyMoney();
							money = money - domoney;
							MyApplication.getApp().getSpUtil().setMyMoney(money);
							
							if(isFromChat){
								Intent intent = new Intent();
								Bundle extras = new Bundle();
								extras.putString("toUserId", toUserId);
								extras.putString("fromUserId", fromUserId);
								extras.putString("eachGameInfo",cnt );
								extras.putInt("eachGameMoney",domoney );
								extras.putString("bottleId", baseModel.getBottleId());
								intent.putExtras(extras);
								setResult(Activity.RESULT_OK, intent);
							}
							closeDialog(mpDialog);
							Game_cq_tz.this.finish();
						}else{
							showMsg(baseModel.getMsg());
						}
					}else{
						showMsg("提交失败,请重试");
					}
				}else{
					showMsg("提交失败,请重试");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("提交失败,请检查网络，或联系客服");
			}
        });
	}
	
	private void setLinstener() {
		// TODO Auto-generated method stub
		rg_money.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				String str_money =((RadioButton) group.findViewById(checkedId)).getText().toString();
				
				domoney = transMoney(str_money);
			}
		});
		
		rg_01.findViewById(R.id.rb_st).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_01);
				((RadioButton)rg_01.findViewById(R.id.rb_st)).setChecked(true);
				no1 = getNumById(R.id.rb_st);
			}
		});
		
		rg_01.findViewById(R.id.rb_jd).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_01);
				((RadioButton)rg_01.findViewById(R.id.rb_jd)).setChecked(true);
				no1 = getNumById(R.id.rb_jd);
			}
		});
		
		rg_01.findViewById(R.id.rb_bu).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_01);
				((RadioButton)rg_01.findViewById(R.id.rb_bu)).setChecked(true);
				no1 = getNumById(R.id.rb_bu);
			}
		});
		
		rg_02.findViewById(R.id.rb_st).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_02);
				((RadioButton)rg_02.findViewById(R.id.rb_st)).setChecked(true);
				no2 = getNumById(R.id.rb_st);
			}
		});
		
		rg_02.findViewById(R.id.rb_jd).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_02);
				((RadioButton)rg_02.findViewById(R.id.rb_jd)).setChecked(true);
				no2 = getNumById(R.id.rb_jd);
			}
		});
		
		rg_02.findViewById(R.id.rb_bu).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_02);
				((RadioButton)rg_02.findViewById(R.id.rb_bu)).setChecked(true);
				no2 = getNumById(R.id.rb_bu);
			}
		});
		
		rg_03.findViewById(R.id.rb_st).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_03);
				((RadioButton)rg_03.findViewById(R.id.rb_st)).setChecked(true);
				no3 = getNumById(R.id.rb_st);
			}
		});
		
		rg_03.findViewById(R.id.rb_jd).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_03);
				((RadioButton)rg_03.findViewById(R.id.rb_jd)).setChecked(true);
				no3 = getNumById(R.id.rb_jd);
			}
		});
		
		rg_03.findViewById(R.id.rb_bu).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearRadioButtonState(rg_03);
				((RadioButton)rg_03.findViewById(R.id.rb_bu)).setChecked(true);
				no3 = getNumById(R.id.rb_bu);
			}
		});
	}
	
	/**
	 * 把带有单位的额度转化为数字。仅仅去数字部分
	 * @param str 需要转化的额度字符串，格式是：数字+单位，例如“50扇贝”
	 * @return 如果转化成功，则返回一个非负数，否则就返回-1
	 */
	private int transMoney(String str) {
		// TODO Auto-generated method stub
		if(TextUtils.isEmpty(str))
			return - 1 ;
		str = str.trim();
		str = str.substring(0, str.length()-2);	//去掉最后两个汉字
		if(TextUtils.isDigitsOnly(str))
			return Integer.valueOf(str);
		return -1;
	}
	
	/**
	 * 清除RadioGroup下所有的RadioButton状态。因为RadioButton的亲爹是LinearLayout，LinearLayout的亲爹是RadioGroup，RadioGroup无法控制孙子辈的RadioBbutton
	 * @param rg
	 */
	public void clearRadioButtonState(RadioGroup rg){
		((RadioButton)rg.findViewById(R.id.rb_bu)).setChecked(false);
		((RadioButton)rg.findViewById(R.id.rb_st)).setChecked(false);
		((RadioButton)rg.findViewById(R.id.rb_jd)).setChecked(false);
	}
}