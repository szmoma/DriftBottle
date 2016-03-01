package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.Date;

public class MyGiftsModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String giftUId;
	private String userId;
	private int num;
	private Date giveTime;
	private int price;
	private int isBack;
	private int charm;
	private String giftName;
	private String picUrl;
	private String shortPic;
	private String nickName;
	private String headImg;
	private int isVIP;
	
	public int getIsVIP() {
		return isVIP;
	}
	public void setIsVIP(int isVIP) {
		this.isVIP = isVIP;
	}
	public String getGiftUId() {
		return giftUId;
	}
	public void setGiftUId(String giftUId) {
		this.giftUId = giftUId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public Date getGiveTime() {
		return giveTime;
	}
	public void setGiveTime(Date giveTime) {
		this.giveTime = giveTime;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public int getIsBack() {
		return isBack;
	}
	public void setIsBack(int isBack) {
		this.isBack = isBack;
	}
	public String getGiftName() {
		return giftName;
	}
	public void setGiftName(String giftName) {
		this.giftName = giftName;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public String getShortPic() {
		return shortPic;
	}
	public void setShortPic(String shortPic) {
		this.shortPic = shortPic;
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
	public int getCharm() {
		return charm;
	}
	public void setCharm(int charm) {
		this.charm = charm;
	}
}
