package com.hnmoma.driftbottle.model;

public class AddPhotoBModel{
	private String code;
	private String msg;
	private PhotoWallModel picInfo;
	
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
	public PhotoWallModel getPicInfo() {
		return picInfo;
	}
	public void setPicInfo(PhotoWallModel picInfo) {
		this.picInfo = picInfo;
	}
}
