package com.hnmoma.driftbottle.model;

import java.util.List;

public class QueryGiftModel {

	String code;
	String msg;
	String isMore;
	List<ChangeList> changeList;
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
	public String getIsMore() {
		return isMore;
	}
	public void setIsMore(String isMore) {
		this.isMore = isMore;
	}
	public List<ChangeList> getChangeList() {
		return changeList;
	}
	public void setChangeList(List<ChangeList> changeList) {
		this.changeList = changeList;
	}
	
	
}
