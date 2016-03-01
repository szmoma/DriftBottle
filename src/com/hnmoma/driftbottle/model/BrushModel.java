package com.hnmoma.driftbottle.model;

import java.io.Serializable;

public class BrushModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String code;
	private String msg;
	private int hasBrush;
	
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
	public int getHasBrush() {
		return hasBrush;
	}
	public void setHasBrush(int hasBrush) {
		this.hasBrush = hasBrush;
	}
	
	
}
