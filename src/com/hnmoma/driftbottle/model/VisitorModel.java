package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.Date;

public class VisitorModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userId;
	private String nickName;
	private String identityType;
	private String headImg;
	private Date visitsTime;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
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
