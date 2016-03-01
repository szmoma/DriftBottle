package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.List;

public class StoreModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1010L;
	String csId;
	String csName;
	String shortPic;
	int price;
	
	String descript;
	List<String> picUrl;
	String remark;
	String picNum;
	int type;
	int changeNum;
	
	public String getCsId() {
		return csId;
	}
	public void setCsId(String csId) {
		this.csId = csId;
	}
	public String getCsName() {
		return csName;
	}
	public void setCsName(String csName) {
		this.csName = csName;
	}
	public String getShortPic() {
		return shortPic;
	}
	public void setShortPic(String shortPic) {
		this.shortPic = shortPic;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public String getDescript() {
		return descript;
	}
	public void setDescript(String descript) {
		this.descript = descript;
	}
	
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getChangeNum() {
		return changeNum;
	}
	public void setChangeNum(int changeNum) {
		this.changeNum = changeNum;
	}
	
	public List<String> getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(List<String> picUrl) {
		this.picUrl = picUrl;
	}
	public String getPicNum() {
		return picNum;
	}
	public void setPicNum(String picNum) {
		this.picNum = picNum;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	
}
