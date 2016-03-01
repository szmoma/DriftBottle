package com.hnmoma.driftbottle;

import java.util.Random;
import java.util.UUID;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.Status;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.GameMsgUpdate;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.CustomDialog;
import com.hnmoma.driftbottle.custom.CustomDialog.CustomDialogClickListener;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.model.GameMsgModel;
import com.hnmoma.driftbottle.model.GameOpponentModel;
import com.hnmoma.driftbottle.model.QueryGameInfoModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;

/**
 * 游戏 出拳
 */
public class Game_Cq_Yz extends BaseActivity implements OnClickListener{
	
	GameOpponentModel gom;
	
	Random random = new Random();
	RadioGroup rg_money,rg_01,rg_02,rg_03;
	
	ImageButton bt_left;
	ImageButton bt_right;
	
	GameOpponentModel model;
	
	private int no1=-1,no2=-1,no3=-1;//第一、二、三局的值
	
	private boolean isfromChat;//是否从ChatActivity跳转过来的
	private String deleteIdentifier;//如果该页面是从ChatActivity过来的，deleteIdentifier表示消息的id
	
	private Dialog dialog;
	private  boolean isThrowable = true; //在当前页面，只能提交一次

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fishing_yxpz_cq);
		
		Intent intent = getIntent();
		if(intent!=null){
			model = (GameOpponentModel) intent.getSerializableExtra("GameOpponentModel");
			isfromChat = intent.getBooleanExtra("fromChat", false);
			deleteIdentifier = intent.getStringExtra("deleteIdentifier");
		}
		
		initview();
		initDate();
		setListener();
	}
	
	@Override
	public void onActivityReenter(int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityReenter(resultCode, data);
	}
	
	public void initview() {
		rg_money = (RadioGroup) findViewById(R.id.rg_money);
		rg_01 = (RadioGroup) findViewById(R.id.rg_01);
		rg_02 = (RadioGroup) findViewById(R.id.rg_02);
		rg_03 = (RadioGroup) findViewById(R.id.rg_03);
		
		bt_left = (ImageButton) findViewById(R.id.ib_jx);
		bt_right = (ImageButton) findViewById(R.id.bt_ok);
		bt_left.setOnClickListener(this);
		bt_right.setOnClickListener(this);
//		findViewById(R.id.bt_close).setOnClickListener(this);
	}

	private void initDate(){
		int money = model.getMoney();
		int checkId = getIdByMoney(money);
		rg_money.check(checkId);
	}
	
	private int getIdByMoney(int money){
		int id = 0;
		
		switch (money) {
		case 50:
			id = R.id.rb_11;
			break;
		case 100:
			id = R.id.rb_12;
			break;
		case 500:
			id = R.id.rb_13;
			break;
		case 1000:
			id = R.id.rb_14;
			break;
		}
		
		return id;
	}
	
	@Override
	public void onBackPressed() {
		CustomDialog customDialog = new CustomDialog();
		dialog = customDialog.showSelectDialog(this, "温馨提示", "放弃对战吗？", new CustomDialogClickListener() {
			
			@Override
			public void confirm() {
				// TODO Auto-generated method stub
				dialog.dismiss();
				finish();
				setResult(Activity.RESULT_CANCELED);
			}
			
			@Override
			public void cancel() {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
	}
	
	/**
	 * 每一注的结果
	 */
	int[] flag = new int[3];
	int [] array01 = new int[3];
	int [] array02 = new int[3];
	
	//012,输平赢 
	int result;
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
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
			case R.id.bt_ok:
				String str = model.getContent();
				try{
					array01 = MoMaUtil.cq_change2Array(str);
					
					array02[0] = no1;
					array02[1] = no2;
					array02[2] = no3;
					
					flag[0] = compare(array01[0], array02[0]);
					flag[1] = compare(array01[1], array02[1]);
					flag[2] = compare(array01[2], array02[2]);
					
					 int total = flag[0] + flag[1] + flag[2];
					 if(total > 3){//赢
						 result = 2;
					 }else if(total < 3){//输
						 result = 0;
					 }else{
						 result = 1;
					 }
					
					//扔之前检查内容是否合法
					if(check()){
						checkMyMoney();
					}
				}catch(Exception e){
					showMsg("解析异常");
					
				}
				break;
			case R.id.bt_close:
					finish();
					setResult(RESULT_CANCELED);
				break;
		}
	}
	
	/**
	 * t ta
	 * w wo
	 * 
	 * 0,1,2 石头，剪刀，布
	 * 
	 * @return 相对自己而言，012,输平赢
	 */
	private int compare(int t, int w){
		int result = 0;
		
		switch(t){
			case 0:
				if(w==0){
					result = 1;
				}else if(w==1){
					result = 0;
				}else{
					result = 2;
				}
				break;
			case 1:
				if(w==0){
					result = 2;
				}else if(w==1){
					result = 1;
				}else{
					result = 0;
				}
				break;
			case 2:
				if(w==0){
					result = 0;
				}else if(w==1){
					result = 2;
				}else{
					result = 1;
				}
				break;
		}
		return result;
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
		result = isThrowable;
		return result;
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
	 * 检查我的扇贝，如果缓存扇贝不够，则从服务器上更新资源，获取用户的最新扇贝。如果扇贝足够，则提交，否则不允许提交
	 */
	private void checkMyMoney(){
		int myMoney = MyApplication.getApp().getSpUtil().getMyMoney();
		if(myMoney<model.getMoney()){
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
							if(myMoney>=model.getMoney())
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
						showMsg("重试或联系客户");
					}
					
				}

				@Override
				public void onFailure(int statusCode,	Header[] headers,	byte[] responseBody, Throwable error) {
					// TODO Auto-generated method stub
					MoMaLog.e("debug", error.getMessage());
					showMsg("重试或联系客户");
				}});
			
		}else	{						
			doSubmit();
		}
	}
	/**
	 * result true，提交没问题
	 * result false，提交有问题
	 */
	public void doSubmit(){
		//格式：发起方_接收方
		final String cnt =model.getContent()+ "_"+( no1+"|"+no2+"|"+no3);
		isThrowable = false;
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("bottleId", model.getBottleId());
		jo.addProperty("content", cnt);
		jo.addProperty("result", result);
		
		BottleRestClient.post("gameResultNotice", this, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				super.onStart();
				showDialog("正在连接对方...", "连接失败", 15*1000);
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				if (mpDialog != null && mpDialog.isShowing()) {
					mpDialog.cancel();
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					BaseModel baseModel = gson.fromJson(str, BaseModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
						if("0".equals(baseModel.getCode())){
							//更新缓存：扇贝
							int money = MyApplication.getApp().getSpUtil().getMyMoney();
							money = money - model.getMoney();
							MyApplication.getApp().getSpUtil().setMyMoney(money);
							GameMsgUpdate gameMsgUpdate = new GameMsgUpdate();
							GameMsgModel gameMsgModel = new GameMsgModel();
							gameMsgModel.setBottleId(model.getBottleId());
							gameMsgModel.setMoney(model.getMoney());
							gameMsgModel.setUserId(model.getUserId());
							gameMsgModel.setNickName(model.getNickName());
							gameMsgModel.setHeadImg(model.getHeadImg());
							gameMsgModel.setContent(model.getContent());
							gameMsgModel.setResult(String.valueOf(result));
							gameMsgUpdate.updatePlayGame(gameMsgModel);

							if(isfromChat){
								//返回挑战结果
								Intent data = new Intent();
								data.putExtra("position", model.getPosition());
								data.putExtra("result", result);
								data.putExtra("bottleId", model.getBottleId());
								data.putExtra("eachGameMoney", model.getMoney());
								data.putExtra("eachGameInfo", cnt);
								data.putExtra("toUsername",model.getUserId() );
								data.putExtra("deleteIdentifier", deleteIdentifier);
								setResult(Activity.RESULT_OK, data);
							}else{
								Intent data = new Intent();
								data.putExtra("position", model.getPosition());
								data.putExtra("result", String.valueOf(result));
								data.putExtra("content", cnt);
								setResult(Activity.RESULT_OK,data);
							}
							
							Intent intent = new Intent(Game_Cq_Yz.this, Game_Cq_Activity.class);
							intent.putExtra("array01", array01);
							intent.putExtra("array02", array02);
							intent.putExtra("flag", flag);
							intent.putExtra("money", model.getMoney());
							if(isfromChat)
								intent.putExtra("fromChat", isfromChat);
							GameOpponentModel gom = new GameOpponentModel();
							gom.setHeadImg(model.getHeadImg());
							gom.setNickName(model.getNickName());
							gom.setUserId(model.getUserId());
							gom.setBottleId(model.getBottleId());
							
							intent.putExtra("GameOpponentModel", gom);
							startActivity(intent);
							closeDialog(mpDialog);
							finish();
						}else if("100028".equals(baseModel.getCode())){//已经完成挑战了
							new CustomDialog().showSelectDialog(Game_Cq_Yz.this, "温馨提示", "已挑战,放弃吗？", new CustomDialogClickListener() {
								
								@Override
								public void confirm() {
//									queryAndUpdateMessage(model.getBottleId(),model.getPosition());
									closeDialog(mpDialog);
									finish();
								}
								@Override
								public void cancel() {
									// TODO Auto-generated method stub
//									finish();
								}
							});
							
						}else{
							showMsg(baseModel.getMsg());
						}
					}else{
						showMsg("连接失败,请重试");
					}
				}else{
					showMsg("连接失败,请重试");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("连接失败,请检查网络，或联系客服");
			}
        });
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
	private void setListener() {
		// TODO Auto-generated method stub
		
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
	 * 清除RadioGroup下所有的RadioButton状态。因为RadioButton的亲爹是LinearLayout，LinearLayout的亲爹是RadioGroup，RadioGroup无法控制孙子辈的RadioBbutton
	 * @param rg
	 */
	public void clearRadioButtonState(RadioGroup rg){
		((RadioButton)rg.findViewById(R.id.rb_bu)).setChecked(false);
		((RadioButton)rg.findViewById(R.id.rb_st)).setChecked(false);
		((RadioButton)rg.findViewById(R.id.rb_jd)).setChecked(false);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//防止窗体泄露
		if(dialog!=null){
			dialog.dismiss();
			dialog = null;
		}
	}
	
	/**
	 * 更新消息
	 * @param message
	 */
	protected void queryAndUpdateMessage(final String bottleId,final int position) {
		// TODO Auto-generated method stub
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("bottleId", bottleId);
		
		BottleRestClient.post("queryGameByBottleId", this, jo, new AsyncHttpResponseHandler(){

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {
				// TODO Auto-generated method stub
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					QueryGameInfoModel gameInfoModel = gson.fromJson(str, QueryGameInfoModel.class);
					if(gameInfoModel != null && !TextUtils.isEmpty(gameInfoModel.getCode())){
						if("0".equals(gameInfoModel.getCode())){
							GameMsgModel gameInfo = gameInfoModel.getGameResultInfo();
							if(gameInfo==null)
								return ;
							EMConversation conversation = EMChatManager.getInstance().getConversation(gameInfo.getUserId());
							if(conversation==null)
								return ;
							EMMessage message = findMessageByBottleId(bottleId, conversation);
							if(message==null)
								return ;
							message.setAttribute("isGame_image", true);//
							if(gameInfo.getState()==1){//完成未查看
								message.setAttribute("isBeReceived", true);	//是否对方已经接受了应战
							}else if(gameInfo.getState()==2){//已查看
								message.setAttribute("isBeReceived", true);	//是否对方已经接受了应战
								message.setAttribute("isHaveLooked", true);//游戏发起者是否查看了游戏结果
							}else if(gameInfo.getState()==3){//拒绝
								message.setAttribute("isRefuse", true);
							}
							message.setAttribute("eachGameMoney", String.valueOf(gameInfo.getMoney())); //游戏金额
							message.setAttribute("eachGameInfo", gameInfo.getContent()); //每局信息
							message.setAttribute("gameResult", gameInfo.getResult()); //游戏结果
							message.status = Status.SUCCESS;
							message.setUnread(false);
							
							conversation.removeMessage(message.getMsgId());
//							message.setMsgId(UUID.randomUUID().toString().replace('_', ' '));
							EMChatManager.getInstance().importMessage(message, false);//向数据库中导入数据，但是不加入内存中
							conversation.addMessage(message);
							
							Intent data = new Intent();
							data.putExtra("position", model.getPosition());
							data.putExtra("isFinish", true);
							setResult(Activity.RESULT_OK,data);
							
							if(gameInfo.getState()==1){
								GameOpponentModel model = new GameOpponentModel();
								model.setBottleId(bottleId);
								model.setMoney(gameInfo.getMoney());
								model.setContent(gameInfo.getContent());
								model.setHeadImg(gameInfo.getHeadImg());
								model.setUserId(gameInfo.getUserId());
								model.setNickName(gameInfo.getNickName());
								
								Intent intent = new Intent(Game_Cq_Yz.this, Game_Cq_Activity.class);
								intent.putExtra("money", model.getMoney());
								intent.putExtra("GameOpponentModel", model);
								intent.putExtra("bUId", gameInfo.getbUId());
								startActivity(intent);
							}
							finish();
						}else{
							showMsg(gameInfoModel.getMsg());
						}
					}else{
						showMsg("加载失败...");
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				// TODO Auto-generated method stub
				
			}});
	}
	
	/**
	 * 通过瓶子的id，查询消息（这条消息，有可能来自聊天中的）
	 * @param bottleId 瓶子的id
	 * @param conversation 与好友的会话
	 * @return  如果找到消息，则返回消息内容，否则返回null
	 */
	private EMMessage findMessageByBottleId(String bottleId ,EMConversation conversation){
		EMMessage tmp = null;
		for(int i=conversation.getMsgCount()-1;i>=0;i--){
			EMMessage msg = conversation.getMessage(i);
			if(msg!=null&&!TextUtils.isEmpty( msg.getStringAttribute("gameBottleId",""))&& msg.getStringAttribute("gameBottleId","").equalsIgnoreCase(bottleId)){
				tmp = msg;
				break;
			}
		}
		return tmp;
	}
}

