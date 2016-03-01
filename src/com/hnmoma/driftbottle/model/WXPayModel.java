package com.hnmoma.driftbottle.model;

import java.io.Serializable;

public class WXPayModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String appid;
	private String noncestr;
	private String ipackage;
	private String partnerid;
	private String prepayid;
	private String sign;
	private String timestamp;
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getNoncestr() {
		return noncestr;
	}
	public void setNoncestr(String noncestr) {
		this.noncestr = noncestr;
	}
	public String getIpackage() {
		return ipackage;
	}
	public void setIpackage(String ipackage) {
		this.ipackage = ipackage;
	}
	public String getPartnerid() {
		return partnerid;
	}
	public void setPartnerid(String partnerid) {
		this.partnerid = partnerid;
	}
	public String getPrepayid() {
		return prepayid;
	}
	public void setPrepayid(String prepayid) {
		this.prepayid = prepayid;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	
}
