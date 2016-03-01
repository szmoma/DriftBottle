package com.hnmoma.driftbottle.model;

import java.io.Serializable;


public class PayInfo implements Serializable{
	
	private String orderId;
	private int mdou;
	private int status;
	private String remark;
	
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public int getMdou() {
		return mdou;
	}
	public void setMdou(int mdou) {
		this.mdou = mdou;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
}