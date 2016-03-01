package com.hnmoma.driftbottle.model;

import java.util.List;

public class UserTopBModel{
	private String code;
	private String msg;
	private List<TopUserModel> userList;
	
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
	public List<TopUserModel> getUserList() {
		return userList;
	}
	public void setUserList(List<TopUserModel> userList) {
		this.userList = userList;
	}
}
