package com.hnmoma.driftbottle.business;

import android.text.TextUtils;

import com.hnmoma.driftbottle.model.GiftInfo;
import com.hnmoma.driftbottle.model.NewChatModel;
import com.hnmoma.driftbottle.model.NoticeModel;
import com.hnmoma.driftbottle.model.PayInfo;

public class BmobMsg {
	
	NewChatModel chatModel;
	//支付
	PayInfo payInfo;
	//礼物
	GiftInfo giftInfo;
	
	//系统通知
	private String content;
	
	//
	private String type;
	
	public static BmobMsg createReceiveMsg(NoticeModel noticeModel) {
		 BmobMsg bm = null;
	
		if(noticeModel.getType().equals("1000")){	 //瓶子消息推送
//			List<NewChatModel> newChats = noticeModel.getNewList();
//			if(newChats!=null && newChats.size()!=0){
//				bm = new BmobMsg();
//				bm.setType(noticeModel.getType());
//				bm.setChatModel(newChats.get(0));
//			}
		}else if(noticeModel.getType().equals("1001")){	//充值结果通知
			PayInfo pi = noticeModel.getPayInfo();
			if(pi!=null){
				bm = new BmobMsg();
				bm.setType(noticeModel.getType());
				bm.setPayInfo(pi);
			}
		}else if(noticeModel.getType().equals("1002")){	//礼物消息
			GiftInfo gi = noticeModel.getGiftInfo();
			if(gi!=null){
				bm = new BmobMsg();
				bm.setType(noticeModel.getType());
				bm.setGiftInfo(gi);
			}
		}else if(noticeModel.getType().equals("1003")){	//系统通知
			String msg = noticeModel.getContent();
			if(!TextUtils.isEmpty(msg)){
				bm = new BmobMsg();
				bm.setType(noticeModel.getType());
				bm.setContent(msg);
			}
		}
	  return bm;
	}

	public NewChatModel getChatModel() {
		return chatModel;
	}

	public void setChatModel(NewChatModel chatModel) {
		this.chatModel = chatModel;
	}

	public PayInfo getPayInfo() {
		return payInfo;
	}

	public void setPayInfo(PayInfo payInfo) {
		this.payInfo = payInfo;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public GiftInfo getGiftInfo() {
		return giftInfo;
	}

	public void setGiftInfo(GiftInfo giftInfo) {
		this.giftInfo = giftInfo;
	}
}