package com.hnmoma.driftbottle.model;

import java.util.List;

import com.google.gson.Gson;

public class DeleteBottlesBean{
	private String userId ;
	private List<String> ubIdList;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public List<String> getUbIdList() {
		return ubIdList;
	}
	public void setUbIdList(List<String> ubIdList) {
		this.ubIdList = ubIdList;
	}
	
	public String getJsonString() {
		Gson gson = new Gson();
		return gson.toJson(this, this.getClass());
	}
}
