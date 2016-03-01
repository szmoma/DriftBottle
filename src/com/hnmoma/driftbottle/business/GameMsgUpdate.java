package com.hnmoma.driftbottle.business;

import java.util.UUID;

import android.text.TextUtils;

import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.exceptions.EaseMobException;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.model.GameMsgModel;
import com.hnmoma.driftbottle.util.MyConstants;

public class GameMsgUpdate {
	/**
	 * 用户挑战后，需要去会话查询一下
	 * @param model
	 */
	public void updatePlayGame(GameMsgModel model){
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
		sendPlayMessageOfCMD(model);
		updateMessageState(conversation,msg, model);
	}
	/**
	 * 更新消息：查看
	 * @param model
	 * @param flag
	 */
	public void updateLookGame(GameMsgModel model, int[] flag) {
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
	public void updateRefuseGame(GameMsgModel model) {
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

	public void sendRefuseMessageOfCMD(GameMsgModel model) {
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
	public EMMessage findMessageByBottleId(String bottleId ,EMConversation conversation){
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
	
	public void sendPlayMessageOfCMD(GameMsgModel model){
		if(model==null)
			return ;
		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.CMD);//创建一个发送消息：透传
		message.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, MyApplication.getApp().getSpUtil().getUserHeadImage());	//聊天的头像
		message.setAttribute("isGame_image", true);//
		message.setAttribute("isBeReceived", true);	//是否对方已经接受了应战
		message.setAttribute("eachGameMoney", String.valueOf(model.getMoney())); //游戏金额
		message.setAttribute("eachGameInfo", model.getContent()); //每局信息
		message.setAttribute("toUsername", model.getUserId()); //被挑战着
		message.setAttribute("sendUserId", model.getUserId());//游戏挑战者
		message.setAttribute("gameBottleId", model.getBottleId()); //
		message.setAttribute("gameResult", String.valueOf(model.getResult())); //游戏结果
		message.setAttribute("deleteIdentifier", UUID.randomUUID().toString()); 
		//发送给某人
		message.setReceipt(model.getUserId());
				
		String action = MyConstants.UPDATEGAMESTATUS;
		CmdMessageBody cmdBody=new CmdMessageBody(action);
		message.addBody(cmdBody);
		sendMessage(message);
	}
	
//	public void sendLookMessageOfCMD(GameMsgModel model){
//		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.CMD);//创建一个发送消息：透传
//		message.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, MyApplication.getApp().getSpUtil().getUserHeadImage());	//聊天的头像
//		message.setAttribute("isGame_image", true);//
//		message.setAttribute("isBeReceived", true);	//是否对方已经接受了应战
//		message.setAttribute("isHaveLooked", true);//游戏发起者是否查看了游戏结果
//		message.setAttribute("eachGameMoney", String.valueOf(model.getMoney())); //游戏金额
//		message.setAttribute("eachGameInfo", model.getContent()); //每局信息
//		message.setAttribute("toUsername", model.getUserId()); //被挑战着
//		message.setAttribute("sendUserId", model.getUserId());//游戏挑战者
//		message.setAttribute("gameBottleId", model.getBottleId()); //
//		message.setAttribute("gameResult", String.valueOf(model.getResult())); //游戏结果
//		message.setAttribute("deleteIdentifier", UUID.randomUUID().toString()); 
//		//发送给某人
//		message.setReceipt(model.getUserId());
//		String action = MyConstants.UPDATEGAMESTATUS;
//		CmdMessageBody cmdBody=new CmdMessageBody(action);
//		message.addBody(cmdBody);
//		sendMessage(message);
//	}
	/**
	 * 需要更新的消息
	 * @param message
	 */
	public void updateMessageState(EMConversation conversation,EMMessage msg,GameMsgModel model){
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
	
	public void updateMessageRefuseState(EMConversation conversation,EMMessage msg, GameMsgModel model) {
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
	
	public void sendMessage(EMMessage message) {
		// TODO Auto-generated method stub
		try {
			EMChatManager.getInstance().sendMessage(message);
		} catch (EaseMobException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
