package com.hnmoma.driftbottle.business;

import java.util.List;
import java.util.UUID;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.view.TextureView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Direct;
import com.easemob.chat.TextMessageBody;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.model.BottleMsgAttach;
import com.hnmoma.driftbottle.model.BottleMsgAttachDao;
import com.hnmoma.driftbottle.model.DaoSession;
import com.hnmoma.driftbottle.model.QueryUserInfoModel;
import com.hnmoma.driftbottle.model.Stranger;
import com.hnmoma.driftbottle.model.StrangerDao;
import com.hnmoma.driftbottle.model.User;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;

import de.greenrobot.dao.query.QueryBuilder;

public class BottleMsgManager {

	Context globalContext;
	private static volatile BottleMsgManager INSTANCE;
	private static Object INSTANCE_LOCK = new Object();
	
	public static BottleMsgManager getInstance(Context paramContext){
	    if (INSTANCE == null)
	      synchronized (INSTANCE_LOCK){
	    	  INSTANCE = new BottleMsgManager();
	    	  INSTANCE.init(paramContext);
	      }
	    return INSTANCE;
	  }

	  private void init(Context paramContext) {
	    this.globalContext = paramContext;
	  }
	  
	public void updateBottleMsg(BottleMsgAttach bottleMsgAttach) {
		DaoSession daoSession = MyApplication.getApp().getDaoSession();
		BottleMsgAttachDao bottleMsgAttachDao = daoSession.getBottleMsgAttachDao();
		bottleMsgAttachDao.update(bottleMsgAttach);
	}
	
	public void addMsgNotice(final int state, final String userId,String headUrl){
		Stranger mStranger = MyApplication.getApp().getDaoSession().getStrangerDao().load(userId);
		if(mStranger==null){
			JsonObject jo = new JsonObject();
			jo.addProperty("id", userId);
			BottleRestClient.post("queryUserInfo", MyApplication.getApp(), jo, new AsyncHttpResponseHandler (){
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
					String str = new String(responseBody);
					if(!TextUtils.isEmpty(str)){
						Gson gson = new Gson();
						QueryUserInfoModel userModel = gson.fromJson(str, QueryUserInfoModel.class);
						 
						 if(userModel!=null&&"0".equals(userModel.getCode())){
							 UserInfoModel userObj = userModel.getUserInfo();
							 if(userObj!=null){
								 Stranger model = new Stranger();
									model.setUserId( userObj.getUserId());
									model.setNickName(userObj.getNickName());
									try {
										model.setIdentityType(userObj.getIdentityType());
									} catch (Exception e) {
										// TODO: handle exception
									}
									model.setHeadImg(userObj.getHeadImg());
									model.setProvince(userObj.getProvince());
									model.setCity( userObj.getCity());
									if(state==1){
										model.setState(1);
									}else if(state == 2){
										model.setState(2);
									}else if(state == 3){
										model.setState(0);
									}
									//更新数据库
									BottleMsgManager.getInstance( MyApplication.getApp()).insertOrReplaceStranger(model);
									saveNoticeMessage(state,userId);
							 }
						 }
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				}
	        });
		}else{
			if(state==1){
				mStranger.setState(1);
			}else if(state == 2){
				mStranger.setState(2);
			}else if(state == 3){
				mStranger.setState(0);
			}
			if(!TextUtils.isEmpty(headUrl)&&!headUrl.equals(mStranger.getHeadImg()))
				mStranger.setHeadImg(headUrl);
			//更新数据库
			BottleMsgManager.getInstance( MyApplication.getApp()).insertOrReplaceStranger(mStranger);
			saveNoticeMessage(state,userId);
		}
	}
	
	private void saveNoticeMessage(int noticeType,String userId) {
		// TODO Auto-generated method stub
		String noticeMsg = "";
		if(noticeType == 1){
			noticeMsg = "对方收藏了瓶子，已建立连接！";
		}else if(noticeType == 2){
			noticeMsg = "对方删除了瓶子，无法收到您的回复！";
		}else if(noticeType == 3){
			noticeMsg = "等待对方回复";
		}
		
		EMMessage msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
		msg.setAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 2);
		//如果为消息通知类型，设置是开始建立连接还是对方删除瓶子无法建立联系
		msg.setAttribute(MyConstants.MESSAGE_ATTR_NOTICETYPE, noticeType);
		TextMessageBody body = new TextMessageBody(noticeMsg);
		msg.addBody(body);
		msg.setChatType(ChatType.Chat);
		msg.setMsgId(getId());

		
		msg.setTo(UserManager.getInstance(globalContext).getCurrentUserId());
		msg.setFrom(userId);
		msg.setMsgTime(System.currentTimeMillis());
		
		//这个方法false是导到数据库，不保存到内存。true保存到内存，但会响铃加两条消息的字样
//		EMChatManager.getInstance().importMessage(msg, true);
		
		//这个方法false是不响铃。true响铃加两条消息的字样
		EMChatManager.getInstance().saveMessage(msg, false);
//		EMChatManager.getInstance().getConversation(userId).resetUnsetMsgCount();
	}
	
	
	public synchronized static String getId() {
		String id = UUID.randomUUID().toString();
		return id.replace('_', ' ');
	}
	
//	/**
//	 * 告诉对方瓶子已经删除
//	 */
//	public void delMsgNotice(int noticeType, String userId){
//		EMMessage msg = EMMessage.createSendMessage(EMMessage.Type.TXT);
//		msg.setAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 2);
//		//如果为消息通知类型，设置是开始建立连接还是对方删除瓶子无法建立联系
//		msg.setAttribute(MyConstants.MESSAGE_ATTR_NOTICETYPE, noticeType);
//		
//		String noticeMsg;
//		if(noticeType == 1){
//			noticeMsg = "对方收藏了瓶子，已建立连接！";
//		}else{
//			noticeMsg = "对方删除了瓶子，无法收到您的回复！";
//		}
//		
//		TextMessageBody body = new TextMessageBody(noticeMsg);
//		msg.addBody(body);
//		
//		msg.setTo(userId);
//		msg.setFrom(UserManager.getInstance(globalContext).getCurrentUserId());
//		msg.setMsgTime(System.currentTimeMillis());
//		
//		EMChatManager.getInstance().saveMessage(msg, true);
////		EMChatManager.getInstance().importMessage(msg, false);
//		EMChatManager.getInstance().getConversation(userId).resetUnsetMsgCount();
//	}

	public BottleMsgAttach getBottleMsgAttachById(String msgId) {
		BottleMsgAttach bottleMsgAttach = null;
		DaoSession daoSession = MyApplication.getApp().getDaoSession();
		BottleMsgAttachDao bottleMsgAttachDao = daoSession.getBottleMsgAttachDao();
		
		QueryBuilder<BottleMsgAttach> qb = bottleMsgAttachDao.queryBuilder();
		qb.where(BottleMsgAttachDao.Properties.MsgId.eq(msgId));
		
		List<BottleMsgAttach> list = qb.list();
		if(list!=null && list.size()!=0){
			bottleMsgAttach = list.get(0);
		}
		
		return bottleMsgAttach;
	}

	public void addBottleMsgAttach(BottleMsgAttach bottleMsgAttach) {
		DaoSession daoSession = MyApplication.getApp().getDaoSession();
		BottleMsgAttachDao bottleMsgAttachDao = daoSession.getBottleMsgAttachDao();
		bottleMsgAttachDao.insert(bottleMsgAttach);
	}
	
	public Stranger getStrangerById(String userId){
	  	Stranger stranger = null;
		DaoSession daoSession = MyApplication.getApp().getDaoSession();
		StrangerDao bottleMsgDao = daoSession.getStrangerDao();
		
		QueryBuilder<Stranger> qb = bottleMsgDao.queryBuilder();
		qb.where(StrangerDao.Properties.UserId.eq(userId));
		
		List<Stranger> list = qb.list();
		if(list!=null && list.size()!=0){
			stranger = list.get(0);
		}
		
		return stranger;
  }
	
	public Stranger insertOrReplaceStranger(UserInfoModel uim){
		Stranger stranger = null;
		if(uim != null){
			stranger = new Stranger();
			stranger.setUserId(uim.getUserId());
			stranger.setCity(uim.getCity());
			stranger.setDescript(uim.getDescript());
			stranger.setHeadImg(uim.getHeadImg());
			try {
				stranger.setIdentityType(uim.getIdentityType());
			} catch (Exception e) {
				// TODO: handle exception
			}
			stranger.setNickName(uim.getNickName());
			stranger.setProvince(uim.getProvince());
			DaoSession daoSession = MyApplication.getApp().getDaoSession();
			daoSession.insertOrReplace(stranger);
		}
		return stranger;
	}
	/**
	 * 插入或更新陌生人的信息
	 * @param user
	 */
	public void insertOrReplaceStranger(User user){
		if(user != null){
			Stranger stranger = new Stranger();
			stranger.setUserId(user.getUserId());
			stranger.setCity(user.getCity());
			stranger.setDescript(user.getDescript());
			stranger.setHeadImg(user.getHeadImg());
			try {
				stranger.setIdentityType(user.getIdentityType());
			} catch (Exception e) {
				// TODO: handle exception
			}
			stranger.setNickName(user.getNickName());
			stranger.setProvince(user.getProvince());
			
			DaoSession daoSession = MyApplication.getApp().getDaoSession();
			daoSession.insertOrReplace(stranger);
		}
	}
	/**
	 * 插入或者更新陌生人的信息
	 * @param stranger
	 */
	public void insertOrReplaceStranger(Stranger stranger){
		if(stranger != null){
			DaoSession daoSession = MyApplication.getApp().getDaoSession();
			daoSession.insertOrReplace(stranger);
		}
	}
	/**
	 * 根据用户ID，删除用户
	 * @param userName
	 */
	public void delStrangerById(String userName) {
		DaoSession daoSession = MyApplication.getApp().getDaoSession();
		daoSession.getStrangerDao().deleteByKey(userName);
	}
	/**
	 * 更新聊天UI中的游戏状态
	 * @param message 消息
	 */
	public void updateGameMessageOfChat(EMMessage message,String toUserId){
		if(message==null)
			return ;
		//获取与userId的会话
		EMConversation conversation = EMChatManager.getInstance().getConversation(toUserId);
		if(!message.getBooleanAttribute("isGame_image", false))
			return ;
		String bottleId = message.getStringAttribute("gameBottleId","");
		
		//获取此conversation当前内存所有的message
		List<EMMessage> messages = conversation.getAllMessages();
		
		if(TextUtils.isEmpty(bottleId))
			return ;
		if(messages==null||messages.isEmpty())
			return ;
		EMMessage msg = null;
		String tmpId = null;
		for(int i=messages.size()-1;i>=0;i--){
			EMMessage tmpMsg = messages.get(i);
			tmpId = tmpMsg.getStringAttribute("gameBottleId","");
			if(!TextUtils.isEmpty(tmpId)&&tmpId.equalsIgnoreCase(bottleId)){
				//更新msg
				msg = tmpMsg;
				break;
			}
		}
		
		if(msg==null)
			return ;
		
		//消息无状态，更新所有的扩展
		boolean isBeReceived = message.getBooleanAttribute("isBeReceived", false);
		boolean isHaveLooked = message.getBooleanAttribute("isHaveLooked", false);
		boolean isRefuse = message.getBooleanAttribute("isRefuse",false);
		if(isRefuse){
			msg.setAttribute("isRefuse", true);
		}else{
			String money = message.getStringAttribute("eachGameMoney","0");
			String eachGameInfo = message.getStringAttribute("eachGameInfo", ""); 
			String gameResult = message.getStringAttribute("gameResult","0");
			msg.setAttribute("eachGameMoney", money); //游戏金额
			msg.setAttribute("eachGameInfo", eachGameInfo); //每局信息
			msg.setAttribute("gameResult", gameResult); 
		}
		msg.setAttribute("isBeReceived", isBeReceived);	//是否对方已经接受了应战
		msg.setAttribute("isHaveLooked", isHaveLooked);//游戏发起者是否查看了游戏结果
		msg.setMsgTime(System.currentTimeMillis());
		
		conversation.removeMessage(msg.getMsgId());
		EMChatManager.getInstance().importMessage(msg, false);//向数据库中导入数据，但是不加入内存中
		conversation.addMessage(msg);
	}
	//把cmd消息，转化为Txt
	public void insertMessageOfHello(EMMessage message) {
		// TODO Auto-generated method stub
		/**对于接收方来说，只要接收到打招呼，状态就是可以连通的**/
		String fromUserId = message.getFrom();
		Stranger mStranger = MyApplication.getApp().getDaoSession().getStrangerDao().load(fromUserId);
		if(mStranger==null){
			MyApplication.getApp().queryUserInfo(fromUserId,1);
		}else{
			boolean isChange = false;
			if(mStranger.getState()!=1){
				mStranger.setState(1);
				isChange = true;
			}
			
			if(!TextUtils.isEmpty(message.getStringAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, null))&&
					!message.getStringAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, null).equals(mStranger.getHeadImg())){
				mStranger.setHeadImg(message.getStringAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, ""));
				isChange = true;
			}
			if(isChange){
				MyApplication.getApp().getDaoSession().getStrangerDao().update(mStranger);
			}
		}
		
		EMMessage msg = EMMessage.createSendMessage(EMMessage.Type.TXT);
		String headUrl = message.getStringAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, "");
		if(!TextUtils.isEmpty(headUrl)){
			msg.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, headUrl);
		}
		msg.setAttribute(MyConstants.MESSAGE_ATTR_ISSAYHELLO, true);
		msg.setAttribute(MyConstants.MESSAGE_ATTR_NOTICETYPE, 0);
		
		TextMessageBody body = new TextMessageBody( message.getStringAttribute(MyConstants.MSG_CONTENT, ""));
		msg.addBody(body);
		msg.setFrom(message.getFrom());
		msg.setTo(message.getTo());
		msg.setReceipt(message.getTo());
		msg.setUnread(true);
		msg.setChatType(ChatType.Chat);
		msg.direct = EMMessage.Direct.RECEIVE;
		msg.setMsgTime(message.getMsgTime());
		String id = UUID.randomUUID().toString();
		id = id.replace('_', ' ');
		msg.setMsgId(id);
		EMChatManager.getInstance().saveMessage(msg);
	}
	
}
