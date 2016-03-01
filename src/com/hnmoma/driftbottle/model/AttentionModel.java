package com.hnmoma.driftbottle.model;

import java.io.Serializable;

public class AttentionModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String attentionId;
	String userId;
	String nickName;
	String headImg;
	String identityType;
	String age;
	String descript;
	Long attentionTime;
	int isVIP;
	
	public int getIsVIP() {
		return isVIP;
	}
	public void setIsVIP(int isVIP) {
		this.isVIP = isVIP;
	}
	public Long getAttentionTime() {
		return attentionTime;
	}
	public void setAttentionTime(Long attentionTime) {
		this.attentionTime = attentionTime;
	}
	public String getAttentionId() {
		return attentionId;
	}
	public void setAttentionId(String attentionId) {
		this.attentionId = attentionId;
	}
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
	public String getHeadImg() {
		return headImg;
	}
	public void setHeadImg(String headImg) {
		this.headImg = headImg;
	}
	public String getIdentityType() {
		return identityType;
	}
	public void setIdentityType(String identityType) {
		this.identityType = identityType;
	}
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public String getDescript() {
		return descript;
	}
	public void setDescript(String descript) {
		this.descript = descript;
	}
	
	
}
