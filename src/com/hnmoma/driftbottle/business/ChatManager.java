package com.hnmoma.driftbottle.business;

import java.util.Date;
import java.util.List;

import org.apache.http.Header;

import android.content.Context;

import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.model.Attachment;
import com.hnmoma.driftbottle.model.Bottle;
import com.hnmoma.driftbottle.model.BottleDao;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.Chat;
import com.hnmoma.driftbottle.model.ChatModel;
import com.hnmoma.driftbottle.model.DaoSession;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import de.greenrobot.dao.query.QueryBuilder;

public class ChatManager {

	Context globalContext;
	private static volatile ChatManager INSTANCE;
	private static Object INSTANCE_LOCK = new Object();
	
	public static ChatManager getInstance(Context paramContext){
	    if (INSTANCE == null)
	      synchronized (INSTANCE_LOCK){
	    	  INSTANCE = new ChatManager();
	    	  INSTANCE.init(paramContext);
	      }
	    return INSTANCE;
	  }

	  private void init(Context paramContext) {
	    this.globalContext = paramContext;
	  }
	  
	  public boolean saveReceiveMessage(boolean readed, BmobMsg bmobMsg) {
		  boolean result = false;
		  
		  if(bmobMsg!=null){
			  String userId = UserManager.getInstance(globalContext).getCurrentUserId();
			  //先保存对话消息，再保存最近对话
			  result = saveChat(readed, bmobMsg, userId);
		  }
		  
		  return result;
	  }
	  
	  private boolean saveChat(boolean readed, BmobMsg ncm, String userId){
		  boolean result = false;
		  
			DaoSession daoSession = MyApplication.getApp().getDaoSession();
			BottleModel bottleModel = ncm.getChatModel().getBottleInfo();
			List<ChatModel> chats = ncm.getChatModel().getChatList();
			
			if(bottleModel!=null){
				Bottle bottle = new Bottle();
				bottle.setBottleId(bottleModel.getBottleId());
				bottle.setBottleType(bottleModel.getBottleType());
				bottle.setContent(bottleModel.getContent());
				bottle.setContentType(bottleModel.getContentType());
				bottle.setBottleSort(bottleModel.getBottleSort());
				bottle.setCreateTime(bottleModel.getCreateTime());
				bottle.setGenerateTime(bottleModel.getCreateTime());
				bottle.setRemark(bottleModel.getRemark());
				bottle.setHasAnswer(false);
				
				UserInfoModel userInfo = bottleModel.getUserInfo();
				bottle.setUserId(userInfo.getUserId());
				bottle.setCity(userInfo.getCity());
				bottle.setHeadImg(userInfo.getHeadImg());
				bottle.setIdentityType(userInfo.getIdentityType());
				bottle.setNickName(userInfo.getNickName());
				bottle.setProvince(userInfo.getProvince());
				bottle.setUbId(ncm.getChatModel().getUbId());
				
				// 非文字瓶属性
				if(!bottleModel.getContentType().equals("5000")) {	//非文本消息：图片贺卡、视频、语音、web
					bottle.setUrl(bottleModel.getUrl());
					bottle.setShortPic(bottleModel.getShortPic());
					bottle.setPicNum(bottleModel.getPicNum());
					bottle.setIsRedirect(bottleModel.getIsRedirect());
					bottle.setRedirectUrl(bottleModel.getRedirectUrl());
					//是否需要把属性取出来，进行保存
					
				}
				
				bottle.setBelongto(userId);
				
				//我的瓶子列表，显示回信人信息
				int size = chats.size();
				if(size > 0){
					ChatModel lastChatModel = chats.get(size-1);
		        	bottle.setMessage(lastChatModel.getContent());
		        	bottle.setMessageType(lastChatModel.getContentType());
		        	
		        	bottle.setRecent_userId(lastChatModel.getUserId());
					bottle.setRecent_headImg(lastChatModel.getHeadImg());
					bottle.setRecent_nickName(lastChatModel.getNickName());
					bottle.setRecent_identityType(lastChatModel.getIdentityType());
					bottle.setRecent_province(lastChatModel.getProvince());
					bottle.setRecent_city(lastChatModel.getCity());
				}
				
				if(readed){
					bottle.setMsgCount(0);
				}else{
					int msgCount = ncm.getChatModel().getChatList().size();
					bottle.setMsgCount(msgCount);
				}
				
				long bottleIdPk;
				try{
					bottleIdPk = daoSession.insert(bottle);
				}catch(Exception e){
					return false;
				}
				// 附件
				// 如果内容类型不为普通文字瓶子就要保存新的附件信息
				if (!bottleModel.getContentType().equals("5000")) {
					if (bottleModel.getSubList() != null && bottleModel.getSubList().size() != 0) {
						for(int i = 0; i < bottleModel.getSubList().size(); i++) {
							Attachment att = bottleModel.getSubList().get(i);
							att.setAttachmentType(0);
							att.setBottleIdPk(bottleIdPk);
							daoSession.insert(att);
						}
					}
				}
				
				//对话，第一条消息，显示发送的瓶子内容
				Chat fchat = new Chat();
				fchat.setChatTime(bottleModel.getCreateTime());
				fchat.setContent(bottleModel.getContent());
				fchat.setContentType(bottleModel.getContentType());
				fchat.setHeadImg(userInfo.getHeadImg());
				fchat.setNickName(userInfo.getNickName());
				fchat.setIdentityType(userInfo.getIdentityType());
				fchat.setProvince(userInfo.getProvince());
				fchat.setCity(userInfo.getCity());
				fchat.setUserId(userInfo.getUserId());
				fchat.setBottleIdPk(bottleIdPk);
				fchat.setIsContent(true);
				daoSession.insert(fchat);
	        	
	        	for(ChatModel chatModel : chats){
	        		Chat chat = new Chat();
					chat.setChatTime(chatModel.getChatTime());
					chat.setContent(chatModel.getContent());
					chat.setContentType(chatModel.getContentType());
					chat.setHeadImg(chatModel.getHeadImg());
					chat.setNickName(chatModel.getNickName());
					chat.setUserId(chatModel.getUserId());
					chat.setBottleIdPk(bottleIdPk);
					chat.setIsContent(false);
					chat.setIsComMsg(true);
					daoSession.insert(chat);  
				}	
	        	
	        	result = true;
			}else{
				BottleDao bottleDao = daoSession.getBottleDao();
				QueryBuilder<Bottle> qb = bottleDao.queryBuilder();
				qb.where(BottleDao.Properties.UbId.eq(ncm.getChatModel().getUbId()), BottleDao.Properties.Belongto.eq(userId));
				List<Bottle> list = qb.list();
				Bottle bottle = null;
				if(list!=null && list.size()!=0){
					bottle = list.get(0);
				}
				
				if(bottle == null){
					deleteBottle(ncm.getChatModel().getUbId(), userId);
					result = false;
				}else{
					if(readed){
						bottle.setMsgCount(0);
					}else{
						int msgCount = bottle.getMsgCount();
						msgCount = msgCount + ncm.getChatModel().getChatList().size();
						bottle.setMsgCount(msgCount);
					}
					bottle.setCreateTime(new Date());
					//我的瓶子列表，显示回信人信息
					int size = chats.size();
					if(size > 0){
						ChatModel lastChatModel = chats.get(size-1);
			        	bottle.setMessage(lastChatModel.getContent());
			        	bottle.setMessageType(lastChatModel.getContentType());
					}
					long bottleIdPk = daoSession.insertOrReplace(bottle);
					
		        	for(ChatModel chatModel : chats){
		        		Chat chat = new Chat();
						chat.setChatTime(chatModel.getChatTime());
						chat.setContent(chatModel.getContent());
						chat.setContentType(chatModel.getContentType());
						chat.setHeadImg(chatModel.getHeadImg());
						chat.setNickName(chatModel.getNickName());
						chat.setUserId(chatModel.getUserId());
						chat.setBottleIdPk(bottleIdPk);
						chat.setIsContent(false);
						chat.setIsComMsg(true);
						daoSession.insert(chat); 
					}
		        	
		        	result = true;
				}
			}
			return result;
		}
	  
	//删除瓶子对话
  	private void deleteBottle(String ubid, String userId){
  		JsonObject jo = new JsonObject();
  		jo.addProperty("userId", userId);
  		jo.addProperty("ubId", ubid);
  		
  		BottleRestClient.post("deleteChat", globalContext, jo, new AsyncHttpResponseHandler(){
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
  			}

  			@Override
  			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
  			}
          });
  	}
  	
  	/**
  	 * 检查是否有未读消息
  	 * @param paramBoolean
  	 * @param bmobMsg
  	 */
  	public boolean hasUnReadMsg() {
  		boolean result = false;
		String userId = UserManager.getInstance(globalContext).getCurrentUserId();
		DaoSession daoSession = MyApplication.getApp().getDaoSession();
		BottleDao bottleDao = daoSession.getBottleDao();
		QueryBuilder<Bottle> qb = bottleDao.queryBuilder();
		qb.where(BottleDao.Properties.MsgCount.gt(0), BottleDao.Properties.Belongto.eq(userId));
		List<Bottle> list = qb.list();
		if(list!=null && list.size()!=0){
			result = true;
		}
		return result;
	}
  	
  	/**
  	 * 保存瓶子
  	 * @return
  	 */
  	public Bottle saveBottle(BottleModel bottleModel, String userId) {
		DaoSession daoSession = MyApplication.getApp().getDaoSession();
	
		Bottle bottle = new Bottle();
		bottle.setBottleId(bottleModel.getBottleId());
		bottle.setBottleType(bottleModel.getBottleType());
		bottle.setContent(bottleModel.getContent());
		bottle.setContentType(bottleModel.getContentType());
	
		// 非文字瓶属性
		if (!bottleModel.getContentType().equals("5000")) {
			bottle.setUrl(bottleModel.getUrl());
			bottle.setShortPic(bottleModel.getShortPic());
			bottle.setPicNum(bottleModel.getPicNum());
			bottle.setIsRedirect(bottleModel.getIsRedirect());
			bottle.setRedirectUrl(bottleModel.getRedirectUrl());
		}
		
		//任务瓶子
		bottle.setState(bottleModel.getState());
	
		bottle.setBottleSort(bottleModel.getBottleSort());
		bottle.setCreateTime(bottleModel.getCreateTime());
		bottle.setGenerateTime(bottleModel.getCreateTime());
		bottle.setRemark(bottleModel.getRemark());
		bottle.setMsgCount(0);
		bottle.setUbId(bottleModel.getUbId());
		bottle.setHasAnswer(bottleModel.getHasAnswer());
	
		UserInfoModel userInfo = bottleModel.getUserInfo();
		bottle.setUserId(userInfo.getUserId());
		bottle.setCity(userInfo.getCity());
		bottle.setDescript(userInfo.getDescript());
		bottle.setHeadImg(userInfo.getHeadImg());
		bottle.setIdentityType(userInfo.getIdentityType());
		bottle.setNickName(userInfo.getNickName());
		bottle.setProvince(userInfo.getProvince());
	
		bottle.setBelongto(userId);
		
		//最近对话列表
		bottle.setMessageType(bottle.getContentType());
		bottle.setMessage(bottle.getContent());
		
		bottle.setRecent_userId(bottle.getUserId());
		bottle.setRecent_headImg(bottle.getHeadImg());
		bottle.setRecent_nickName(bottle.getNickName());
		bottle.setRecent_identityType(bottle.getIdentityType());
		bottle.setRecent_province(bottle.getProvince());
		bottle.setRecent_city(bottle.getCity());
	
		long bottleIdPk = daoSession.insert(bottle);
		bottleModel.setBottleIdPk(bottleIdPk);
	
		// 附件
		// 如果内容类型不为普通文字瓶子就要保存新的附件信息
		if (!bottleModel.getContentType().equals("5000")) {
			if (bottleModel.getSubList() != null && bottleModel.getSubList().size() != 0) {
				for (int i = 0; i < bottleModel.getSubList().size(); i++) {
					Attachment att = bottleModel.getSubList().get(i);
					att.setAttachmentType(0);
					att.setBottleIdPk(bottleIdPk);
					daoSession.insert(att);
				}
			}
		}
	
		// 对话
		Chat chat = new Chat();
		chat.setChatTime(bottleModel.getCreateTime());
		chat.setContent(bottleModel.getContent());
		chat.setContentType(bottleModel.getContentType());
		chat.setHeadImg(userInfo.getHeadImg());
		chat.setNickName(userInfo.getNickName());
		chat.setIdentityType(userInfo.getIdentityType());
		chat.setProvince(userInfo.getProvince());
		chat.setCity(userInfo.getCity());
		chat.setUserId(userInfo.getUserId());
		chat.setBottleIdPk(bottleIdPk);
		chat.setIsContent(true);
		chat.setIsComMsg(true);
		daoSession.insert(chat);
		
		return bottle;
	}
}
