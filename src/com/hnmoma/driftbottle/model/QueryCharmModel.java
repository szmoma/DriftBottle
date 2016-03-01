package com.hnmoma.driftbottle.model;

import java.util.List;

public class QueryCharmModel {

	List<StoreModel> storeList;
	String msg;
	String code;
	public List<StoreModel> getStoreList() {
		return storeList;
	}
	public void setStoreList(List<StoreModel> storeList) {
		this.storeList = storeList;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
}
