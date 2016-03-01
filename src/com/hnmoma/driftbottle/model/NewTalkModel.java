package com.hnmoma.driftbottle.model;

import java.io.Serializable;

public class NewTalkModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String code;
	String msg;
	TalkInfo talkInfo;
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
	public TalkInfo getTalkInfo() {
		return talkInfo;
	}
	public void setTalkInfo(TalkInfo talkInfo) {
		this.talkInfo = talkInfo;
	}
	
	
	

}
