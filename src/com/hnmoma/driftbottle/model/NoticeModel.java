package com.hnmoma.driftbottle.model;


public class NoticeModel {
	
	private String type;
	private String userId;
	private String content;
	
	//支付信息
	PayInfo payInfo;
	//礼物信息
	GiftInfo giftInfo;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public PayInfo getPayInfo() {
		return payInfo;
	}
	public void setPayInfo(PayInfo payInfo) {
		this.payInfo = payInfo;
	}
	public GiftInfo getGiftInfo() {
		return giftInfo;
	}
	public void setGiftInfo(GiftInfo giftInfo) {
		this.giftInfo = giftInfo;
	}
}