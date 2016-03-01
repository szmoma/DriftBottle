package com.hnmoma.driftbottle.model;


public class OrderInfo {
	
	private int money;
	private String desc;
	private String customInfo;
	private String payType; //充值类别
	private String useType;//使用类别,1000表示充值，1001表示开通会员
	
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	public int getMoney() {
		return money;
	}
	public void setMoney(int money) {
		this.money = money;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getCustomInfo() {
		return customInfo;
	}
	public void setCustomInfo(String customInfo) {
		this.customInfo = customInfo;
	}
	public String getUseType() {
		return useType;
	}
	public void setUseType(String useType) {
		this.useType = useType;
	}
	
}