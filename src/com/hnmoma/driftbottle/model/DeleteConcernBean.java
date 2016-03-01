package com.hnmoma.driftbottle.model;

import java.util.List;

import com.google.gson.Gson;

public class DeleteConcernBean {

	/**
	 * 
	 */

	List<String> attentionIdList;
	String delType;
	String userId;
	public List<String> getAttentionIdList() {
		return attentionIdList;
	}
	public void setAttentionIdList(List<String> attentionIdList) {
		this.attentionIdList = attentionIdList;
	}
	public String getDelType() {
		return delType;
	}
	public void setDelType(String delType) {
		this.delType = delType;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getJsonString() {
		Gson gson = new Gson();
		return gson.toJson(this, this.getClass());
	}
	
}
