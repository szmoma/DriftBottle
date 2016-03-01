package com.hnmoma.driftbottle.model;

import java.io.Serializable;

public class GameOpponentModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String  userId;
	String  nickName;
	String  headImg;
	
	int position;
	int money;
	String bottleId;
	String content;
	
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
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public int getMoney() {
		return money;
	}
	public void setMoney(int money) {
		this.money = money;
	}
	public String getBottleId() {
		return bottleId;
	}
	public void setBottleId(String bottleId) {
		this.bottleId = bottleId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}