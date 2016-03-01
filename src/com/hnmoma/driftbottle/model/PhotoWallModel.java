package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.Date;

public class PhotoWallModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6463375645868281256L;
	/**
	 * 
	 */
	private String picId;
	private String picUrl;
	private String shortPic;
	private int loveNum;
	Date addTime;
	/**
	 * 图片状态(0待审核，1审核通过，2非法图片）
	 */
	private int state;
	/**
	 * 0没赞，1已赞
	 */
	private int isLove;
	
	public Date getAddTime() {
		return addTime;
	}
	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}
	public String getPicId() {
		return picId;
	}
	public void setPicId(String picId) {
		this.picId = picId;
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
	public int getLoveNum() {
		return loveNum;
	}
	public void setLoveNum(int loveNum) {
		this.loveNum = loveNum;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getIsLove() {
		return isLove;
	}
	public void setIsLove(int isLove) {
		this.isLove = isLove;
	}
}
