package com.hnmoma.driftbottle.model;

import java.io.Serializable;

public class SingleTalkInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String code;
	String msg;
	int  isMore;
	TalkListModel bottleInfo;
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
	public TalkListModel getTalkInfo() {
		return bottleInfo;
	}
	public void setTalkInfo(TalkListModel bottleInfo) {
		this.bottleInfo = bottleInfo;
	}
	
	
	

}
