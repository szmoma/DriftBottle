package com.hnmoma.driftbottle.model;

import java.util.List;

public class QueryGiftLogBModel{
	private String code;
	private String msg;
	private int isMore;
	private List<MyGiftsModel> logList;
	
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
	public int getIsMore() {
		return isMore;
	}
	public void setIsMore(int isMore) {
		this.isMore = isMore;
	}
	public List<MyGiftsModel> getLogList() {
		return logList;
	}
	public void setLogList(List<MyGiftsModel> logList) {
		this.logList = logList;
	}
}
