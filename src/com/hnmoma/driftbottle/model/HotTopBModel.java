package com.hnmoma.driftbottle.model;

import java.util.List;

public class HotTopBModel {

	String code;
	String msg;
	
	List<TopUserModel> fortuneList;
	List<TopUserModel> charmList;
	List<TopUserModel> gamewinList;
	List<TopUserModel> newpList;
	
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
	public List<TopUserModel> getFortuneList() {
		return fortuneList;
	}
	public void setFortuneList(List<TopUserModel> fortuneList) {
		this.fortuneList = fortuneList;
	}
	public List<TopUserModel> getCharmList() {
		return charmList;
	}
	public void setCharmList(List<TopUserModel> charmList) {
		this.charmList = charmList;
	}
	public List<TopUserModel> getGamewinList() {
		return gamewinList;
	}
	public void setGamewinList(List<TopUserModel> gamewinList) {
		this.gamewinList = gamewinList;
	}
	public List<TopUserModel> getNewpList() {
		return newpList;
	}
	public void setNewpList(List<TopUserModel> newpList) {
		this.newpList = newpList;
	}
	
	
}
