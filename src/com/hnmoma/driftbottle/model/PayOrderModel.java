package com.hnmoma.driftbottle.model;


public class PayOrderModel extends BaseModel {
	
	private String orderId;
	private WXPayModel wxInfo;
	
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public WXPayModel getWxInfo() {
		return wxInfo;
	}

	public void setWxInfo(WXPayModel wxInfo) {
		this.wxInfo = wxInfo;
	}
	
	
}