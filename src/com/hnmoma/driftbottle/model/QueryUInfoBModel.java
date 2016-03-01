package com.hnmoma.driftbottle.model;

import java.util.List;

public class QueryUInfoBModel{
	private String code;
	private String msg;
	private VzoneUserInfo userInfo;
	private List<VzoneGiftModel> giftList; //礼物集合
	private List<VzoneBadgeModel>  badgeList;	//徽章集合
	private List<PhotoWallModel> picList;//相册集合
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public VzoneUserInfo getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(VzoneUserInfo userInfo) {
		this.userInfo = userInfo;
	}
	public List<VzoneGiftModel> getGiftList() {
		return giftList;
	}
	public void setGiftList(List<VzoneGiftModel> giftList) {
		this.giftList = giftList;
	}
	public List<VzoneBadgeModel> getBadgeList() {
		return badgeList;
	}
	public void setBadgeList(List<VzoneBadgeModel> badgeList) {
		this.badgeList = badgeList;
	}
	public List<PhotoWallModel> getPicList() {
		return picList;
	}
	public void setPicList(List<PhotoWallModel> picList) {
		this.picList = picList;
	}
	
	
}
