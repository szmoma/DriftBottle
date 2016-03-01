package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.Date;

public class VisitorListModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 96764257366115225L;

	String userId;
	String nickName;
	String identityType;
	String headImg;
	String id;
	Date visitsTime;
	int isVIP;
	
	public int getIsVIP() {
		return isVIP;
	}
	public void setIsVIP(int isVIP) {
		this.isVIP = isVIP;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getIdentityType() {
		return identityType;
	}
	public void setIdentityType(String identityType) {
		this.identityType = identityType;
	}
	public String getHeadImg() {
		return headImg;
	}
	public void setHeadImg(String headImg) {
		this.headImg = headImg;
	}
	public Date getVisitsTime() {
		return visitsTime;
	}
	public void setVisitsTime(Date visitsTime) {
		this.visitsTime = visitsTime;
	}
	
}
