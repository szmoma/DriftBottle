package com.hnmoma.driftbottle.model;

public class UnionLoginModel {
	private String code;
	private String msg;
	private UserInfoModel userInfo;
	
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
	public UserInfoModel getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(UserInfoModel userInfo) {
		this.userInfo = userInfo;
	}
}