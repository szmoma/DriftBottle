package com.hnmoma.driftbottle.model;

import java.io.Serializable;

public class BadgeListModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String bId;
	String bName;
	String shortPic;
	String picUrl;
	String gayShortPic;
	String gayPicUrl;
	String descript;
	String isLight;
	int speed;
	String totalSpeed;
	
	public String getbId() {
		return bId;
	}
	public void setbId(String bId) {
		this.bId = bId;
	}
	public String getbName() {
		return bName;
	}
	public void setbName(String bName) {
		this.bName = bName;
	}
	public String getShortPic() {
		return shortPic;
	}
	public void setShortPic(String shortPic) {
		this.shortPic = shortPic;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public String getGayShortPic() {
		return gayShortPic;
	}
	public void setGayShortPic(String gayShortPic) {
		this.gayShortPic = gayShortPic;
	}
	public String getGayPicUrl() {
		return gayPicUrl;
	}
	public void setGayPicUrl(String gayPicUrl) {
		this.gayPicUrl = gayPicUrl;
	}
	public String getDescript() {
		return descript;
	}
	public void setDescript(String descript) {
		this.descript = descript;
	}
	public String getIsLight() {
		return isLight;
	}
	public void setIsLight(String isLight) {
		this.isLight = isLight;
	}
	public int getSpeed() {
		return speed;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	public String getTotalSpeed() {
		return totalSpeed;
	}
	public void setTotalSpeed(String totalSpeed) {
		this.totalSpeed = totalSpeed;
	}
	
	

}
