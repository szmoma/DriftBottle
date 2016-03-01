package com.hnmoma.driftbottle;

import java.util.List;
import java.util.UUID;

import org.apache.http.Header;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.Status;
import com.easemob.exceptions.EaseMobException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.adapter.GameMsgAdapter;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.model.GameMsgModel;
import com.hnmoma.driftbottle.model.GameOpponentModel;
import com.hnmoma.driftbottle.model.QueryGameMsgBModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 游戏消息页面
 * @author Administrator
 *
 */
public class GameMsgActivity extends BaseActivity{
	public final static int UPDATELISTVIEW = 1;//更新listView页面
	
	private int comeFromTS; //是否从通知栏来的消息
	
	ListView mListView;
	GameMsgAdapter gameMsgAdapter;
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	boolean isloading;
	boolean hasMore = true;
	static final int PAGENUM = 15; 
	
	String bUId = "0";
	
	ProgressBar pb;
	private TextView tvGameResult;//战绩结果
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UPDATELISTVIEW:
				 gameMsgAdapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		};
	};
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("comeFromTS", comeFromTS);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)
		.showImageForEmptyUri(R.drawable.defalutimg)
		.showImageOnFail(R.drawable.defalutimg)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
//		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.bitmapConfig(Bitmap.Config.RGB_565)
//		.displayer(new RoundedBitmapDisplayer(2))
		.build();
		
		if (savedInstanceState != null){
			comeFromTS = savedInstanceState.getInt("comeFromTS");
		}
		
		if(getIntent() != null){
			comeFromTS = getIntent().getIntExtra("comeFromTS", 0);
		}
		
		setContentView(R.layout.activity_game_msg);
		mListView = (ListView) findViewById(R.id.lv_gift);
		pb = (ProgressBar) findViewById(R.id.pb);
		tvGameResult = (TextView) findViewById(R.id.tv_result);
		
		gameMsgAdapter = new GameMsgAdapter(imageLoader, options, this);
		mListView.setAdapter(gameMsgAdapter);
		
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
					if(hasMore && !isloading){
						isloading = true;
						pickDatas();
					 }
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
		
		pickDatas();
	}
	
	private void pickDatas(){
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("bUId", bUId);
		jo.addProperty("pageNum", PAGENUM);
		BottleRestClient.post("queryGameNotice", this, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				super.onStart();
				pb.setVisibility(View.VISIBLE);
				if(!isloading){
					findViewById(R.id.rl_game_result).setVisibility(View.GONE);
					mListView.setVisibility(View.GONE);
				}
			}
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				if(!isloading){
					findViewById(R.id.rl_game_result).setVisibility(View.VISIBLE);
					mListView.setVisibility(View.VISIBLE);
				}

				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					QueryGameMsgBModel baseModel = gson.fromJson(str, QueryGameMsgBModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
						if("0".equals(baseModel.getCode())){
							List<GameMsgModel> msgs = baseModel.getGameResultList();
							
							if(msgs != null && msgs.size() != 0){
								hasMore = baseModel.getIsMore()==0 ? false:true;
								bUId = msgs.get(msgs.size()-1).getbUId();
								
								String record = baseModel.getGameRecord();
								String[] records = record.split("_");//胜_平_负
								if(records.length==3){
									tvGameResult.setText(records[0]+"胜"+records[1]+"平"+records[2]+"负");
								}else if(records.length==2){
									tvGameResult.setText(records[0]+"胜"+records[1]+"平0负");
								}else if(records.length==1){
									tvGameResult.setText(records[0]+"胜0平0负");
								}else{
									findViewById(R.id.rl_game_result).setVisibility(View.GONE);
								}
								 findViewById(R.id.no_message).setVisibility(View.GONE);
								 mListView.setVisibility(View.VISIBLE);
								 findViewById(R.id.rl_game_result).setVisibility(View.VISIBLE);
								 
								gameMsgAdapter.addItemLast(msgs);
								
								Message msg = Message.obtain();
								msg.what  =UPDATELISTVIEW;
								handler.sendMessage(msg);
								
							}else{
								findViewById(R.id.rl_game_result).setVisibility(View.GONE);
								mListView.setVisibility(View.GONE);
								View view = findViewById(R.id.no_message);
								view.setVisibility(View.VISIBLE);
								TextView tvNomessage = (TextView) view.findViewById(R.id.tv_no_message);
								tvNomessage.setText(getResources().getString(R.string.no_game_result));
								tvNomessage.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.game_result), null, null);
							}
							
						}else{
							showMsg(baseModel.getMsg());
						}
					}else{
						showMsg("查询失败");
					}
				}else{
					showMsg("查询失败");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("查询失败");
				if(!isloading){
					findViewById(R.id.rl_game_result).setVisibility(View.VISIBLE);
					mListView.setVisibility(View.VISIBLE);
				}
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				pb.setVisibility(View.GONE);
				isloading = false;
				if(!isloading){
					findViewById(R.id.rl_game_result).setVisibility(View.VISIBLE);
					mListView.setVisibility(View.VISIBLE);
				}
			}
        });
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_back:
				if(comeFromTS==1){
					//从通知栏来的消息
					Intent intent = new Intent(this, MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivity(intent);
				}else{
					this.finish();
				}
				break;
			case R.id.bt_jj://拒绝
				int position = mListView.getPositionForView(v);
				doReject(position);
				break;
			case R.id.bt_yz://应战
				position = mListView.getPositionForView(v);
				doYz(position);
				break;
			case R.id.view_3://查看
				position = mListView.getPositionForView(v);
				doCheckOut(position);
				break;
			case R.id.iv_head:
				int pos = mListView.getPositionForView(v);
				GameMsgModel gmm = gameMsgAdapter.getItem(pos);
				Intent intent = new Intent(GameMsgActivity.this, VzoneActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("identityflag", 1);
				intent.putExtra("userId", gmm.getUserId());
				intent.putExtra("visitUserId", UserManager.getInstance(GameMsgActivity.this).getCurrentUserId());
				startActivity(intent);
				break;
		}
	}
	
	/**
	 * 游戏应战
	 * @param position
	 */
	private void doYz(int position){
		GameMsgModel gm = gameMsgAdapter.getItem(position);
		Intent intent = new Intent(this, Game_Cq_Yz.class);
		GameOpponentModel gom = new GameOpponentModel();
		gom.setHeadImg(gm.getHeadImg());
		gom.setNickName(gm.getNickName());
		gom.setUserId(gm.getUserId());
		gom.setPosition(position);
		gom.setMoney(gm.getMoney());
		gom.setBottleId(gm.getBottleId());
		gom.setContent(gm.getContent());
		intent.putExtra("GameOpponentModel", gom);
		startActivityForResult(intent, 100);
	}
	/**
	 * 拒接游戏
	 * @param position
	 */
	private void doReject(final int position){
		final GameMsgModel gm = gameMsgAdapter.getItem(position);
		
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("bUId", gm.getbUId());
		jo.addProperty("state", "2");
		
		BottleRestClient.post("updateGameState", this, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				super.onStart();
				showDialog("正在拒绝...", "拒绝失败...", 15*1000);
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
					BaseModel baseModel = gson.fromJson(str, BaseModel.class);
					if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
						if("0".equals(baseModel.getCode())){
							gameMsgAdapter.getItem(position).setState(3);
							Message msg = Message.obtain();
							msg.what  =UPDATELISTVIEW;
							handler.sendMessage(msg);
							updateRefuseGame(gm);
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
	
	/**
	 * 每一注的结果
	 */
	int[] flag = new int[3];
	int [] array01 = new int[3];
	int [] array02 = new int[3];
	/**
	 * 查看游戏
	 * @param position
	 */
	private void doCheckOut(final int position){
		final GameMsgModel gm = gameMsgAdapter.getItem(position);
		
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("bUId", gm.getbUId());
		jo.addProperty("state", "1");
		
		BottleRestClient.post("updateGameState", this, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				super.onStart();
				showDialog("努力加载...", "加载失败...", 15*1000);
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
							String gstr = gm.getContent();
							int isHost = gm.getIsHost();
							String [] jg = gstr.split("_");
							if(jg!=null&& jg.length==2){
								if(isHost == 1){//对方是发起方
									array01 = MoMaUtil.cq_change2Array(jg[0]);
									array02 = MoMaUtil.cq_change2Array(jg[1]);
								}else{
									array01 = MoMaUtil.cq_change2Array(jg[1]);
									array02 = MoMaUtil.cq_change2Array(jg[0]);
								}
							
							
								flag[0] = compare(array01[0], array02[0]);
								flag[1] = compare(array01[1], array02[1]);
								flag[2] = compare(array01[2], array02[2]);
							
								Intent intent = new Intent(GameMsgActivity.this, Game_Cq_Activity.class);
								intent.putExtra("array01", array01);
								intent.putExtra("array02", array02);
								intent.putExtra("flag", flag);
								intent.putExtra("money", gm.getMoney());
							
								GameOpponentModel gom = new GameOpponentModel();
								gom.setHeadImg(gm.getHeadImg());
								gom.setNickName(gm.getNickName());
								gom.setUserId(gm.getUserId());
								intent.putExtra("GameOpponentModel", gom);
								startActivity(intent);
								
								updateLookGame(gm,flag);
							}else{
								showMsg("系统异常");
							}
							
							//查看成功的第一件事是更新adapter，然后跳转
							gameMsgAdapter.getItem(position).setState(2);
							Message msg = Message.obtain();
							msg.what = UPDATELISTVIEW;
							handler.sendMessageDelayed(msg, 500);
						}else{
							showMsg(baseModel.getMsg());
						}
					}else{
						showMsg("获取失败,请重试");
					}
				}else{
					showMsg("获取失败,请重试");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showMsg("获取失败,请检查网络，或联系客服");
			}
        });
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
	
	@Override
	public void onBackPressed() {
		if(comeFromTS==1){
			//从通知栏来的消息
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			startActivity(intent);
		}else{
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 100){//接收挑战
			if(resultCode == RESULT_OK){
				if(data==null)
					return ;
				int position = data.getIntExtra("position", -1);
				String result = data.getStringExtra("result");
				String content = data.getStringExtra("content");
				if(position==-1||TextUtils.isEmpty(result))
					return ;
				GameMsgModel gmm = gameMsgAdapter.getItem(position);
				if(gmm!=null){
					gmm.setState(2);
					gmm.setResult(result);
					gmm.setContent(content);
				}
				
				Message msg = Message.obtain();
				msg.what  =UPDATELISTVIEW;
				handler.sendMessage(msg);
			}
		}
	}
	
	/**
	 * 更新消息：查看
	 * @param model
	 * @param flag
	 */
	protected void updateLookGame(GameMsgModel model, int[] flag) {
		// TODO Auto-generated method stub
		if(flag==null||flag.length!=3)
			return ;
		if(model==null)
			return ;
		String bottleId = model.getBottleId();
		String toUserId = model.getUserId();//好友的id
		EMConversation conversation  = EMChatManager.getInstance().getConversation(toUserId);
		if(conversation==null)
			return ;
		EMMessage msg = findMessageByBottleId(bottleId, conversation);
		if(msg==null)
			return ;
		
		int total =  flag[0] + flag[1] + flag[2];
		int result = -1;
		if(total>3){//赢了
			 result = 2;
		}else if(total<3){	//输
			 result = 0;
		}else{	//平局
			 result = 1;
		}
		model.setResult(String.valueOf(result));
		
//		sendLookMessageOfCMD(model);
		updateMessageState(conversation,msg, model);
	}
	/**
	 * 更新的消息---拒绝
	 * @param model
	 */
	private void updateRefuseGame(GameMsgModel model) {
		// TODO Auto-generated method stub
		if(model==null)
			return ;
		String bottleId = model.getBottleId();
		String toUserId = model.getUserId();//好友的id
		EMConversation conversation  = EMChatManager.getInstance().getConversation(toUserId);
		if(conversation==null)
			return ;
		EMMessage msg = findMessageByBottleId(bottleId, conversation);
		if(msg==null)
			return ;
		sendRefuseMessageOfCMD(model);
		updateMessageRefuseState(conversation, msg, model);
	}

	private void sendRefuseMessageOfCMD(GameMsgModel model) {
		// TODO Auto-generated method stub
		if(model==null)
			return ;
		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.CMD);//创建一个发送消息：透传
		message.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, MyApplication.getApp().getSpUtil().getUserHeadImage());	//聊天的头像
		message.setAttribute("isGame_image", true);//
		message.setAttribute("isRefuse", true);//已经拒接
		message.setAttribute("toUsername", model.getUserId()); //被挑战着
		message.setAttribute("sendUserId", model.getUserId());//游戏挑战者
		message.setAttribute("gameBottleId", model.getBottleId()); //
		
		//发送给某人
		message.setReceipt(model.getUserId());
				
		String action = MyConstants.UPDATEGAMESTATUS;
		CmdMessageBody cmdBody=new CmdMessageBody(action);
		message.addBody(cmdBody);
		sendMessage(message);
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
	

	/**
	 * 需要更新的消息
	 * @param message
	 */
	private void updateMessageState(EMConversation conversation,EMMessage msg,GameMsgModel model){
		//更新本地的消息:游戏结果
		msg.setAttribute("isBeReceived", true);	//是否对方已经接受了应战
		msg.setAttribute("isHaveLooked", true);//游戏发起者是否查看了游戏结果
		msg.setAttribute("eachGameInfo", model.getContent()); //每局信息
		msg.setAttribute("gameResult", String.valueOf(model.getResult())); 
		msg.setMsgTime(System.currentTimeMillis());
		msg.status = EMMessage.Status.SUCCESS;
		msg.setUnread(false);
//		conversation.removeMessage(msg.getMsgId());
		EMChatManager.getInstance().importMessage(msg, false);//向数据库中导入数据，但是不加入内存中
//		conversation.addMessage(msg);
	}
	
	private void updateMessageRefuseState(EMConversation conversation,EMMessage msg, GameMsgModel model) {
		// TODO Auto-generated method stub
		msg.setAttribute("isRefuse", true);//已经拒接
		msg.setAttribute("eachGameInfo", model.getContent()); //每局信息
		msg.setAttribute("gameResult", String.valueOf(model.getResult())); 
		msg.setMsgTime(System.currentTimeMillis());
		msg.status = EMMessage.Status.SUCCESS;
		msg.setUnread(false);
//		conversation.removeMessage(msg.getMsgId());
		EMChatManager.getInstance().importMessage(msg, false);//向数据库中导入数据，但是不加入内存中
//		conversation.addMessage(msg);
		
	}
	
	private void sendMessage(EMMessage message) {
		// TODO Auto-generated method stub
		try {
			EMChatManager.getInstance().sendMessage(message);
		} catch (EaseMobException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}