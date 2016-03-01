package com.hnmoma.driftbottle.model;

import java.io.Serializable;


public class VzoneGift implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String giftId;
	private String picUrl;
	private String shortPic;
	
	public String getGiftId() {
		return giftId;
	}
	public void setGiftId(String giftId) {
		this.giftId = giftId;
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
}