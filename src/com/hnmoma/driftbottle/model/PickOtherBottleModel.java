package com.hnmoma.driftbottle.model;

import java.io.Serializable;

public class PickOtherBottleModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String code;
	String msg;
	OtherBottleModel  bottleInfo;
	
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
	public OtherBottleModel getBottleInfo() {
		return bottleInfo;
	}
	public void setBottleInfo(OtherBottleModel bottleInfo) {
		this.bottleInfo = bottleInfo;
	}
	
	
}
