package com.hnmoma.driftbottle.model;

import java.io.Serializable;

public class MsgModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String userId;
	String msgId;
	String nickName;
	String headImg;
	long createTime;
	String action;
	String bottleId;
	String bottleContent;
	String bottleContentType;
	String shortPic;
	String reContent;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getMsgId() {
		return msgId;
	}
	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getHeadImg() {
		return headImg;
	}
	public void setHeadImg(String headImg) {
		this.headImg = headImg;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	
	public String getBottleId() {
		return bottleId;
	}
	public void setBottleId(String bottleId) {
		this.bottleId = bottleId;
	}
	public String getBottleContent() {
		return bottleContent;
	}
	public void setBottleContent(String bottleContent) {
		this.bottleContent = bottleContent;
	}
	public String getBottleContentType() {
		return bottleContentType;
	}
	public void setBottleContentType(String bottleContentType) {
		this.bottleContentType = bottleContentType;
	}
	public String getShortPic() {
		return shortPic;
	}
	public void setShortPic(String shortPic) {
		this.shortPic = shortPic;
	}
	public String getReContent() {
		return reContent;
	}
	public void setReContent(String reContent) {
		this.reContent = reContent;
	}
	
	

}
