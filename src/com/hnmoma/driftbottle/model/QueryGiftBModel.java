package com.hnmoma.driftbottle.model;

import java.util.List;

public class QueryGiftBModel{
	private String code;
	private String msg;
	private String uuid;
	private List<GiftsModel> giftList;
	
	
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
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public List<GiftsModel> getGiftList() {
		return giftList;
	}
	public void setGiftList(List<GiftsModel> giftList) {
		this.giftList = giftList;
	}
}
