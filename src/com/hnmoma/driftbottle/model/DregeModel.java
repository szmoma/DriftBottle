package com.hnmoma.driftbottle.model;

import java.io.Serializable;

public class DregeModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String code;
	String msg;
	int netNum;
	int isVIP;
	
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
	public int getNetNum() {
		return netNum;
	}
	public void setNetNum(int netNum) {
		this.netNum = netNum;
	}
	public int getIsVIP() {
		return isVIP;
	}
	public void setIsVIP(int isVIP) {
		this.isVIP = isVIP;
	}
	
	
}
