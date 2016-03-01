package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.List;

public class TalkModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String code;
	String msg;
	int isMore;
	List<MyBottleModel> bottleList;
	
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
	public List<MyBottleModel> getTalkList() {
		return bottleList;
	}
	public void setTalkList(List<MyBottleModel> bottleList) {
		this.bottleList = bottleList;
	}
	
	

}
