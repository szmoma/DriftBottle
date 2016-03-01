package com.hnmoma.driftbottle.model;

import java.util.List;

public class NewChatModel extends BaseModel {
	
	private String ubId;
	private List<ChatModel> chatList;
	private BottleModel bottleInfo;
	
	public String getUbId() {
		return ubId;
	}
	public void setUbId(String ubId) {
		this.ubId = ubId;
	}
	public List<ChatModel> getChatList() {
		return chatList;
	}
	public void setChatList(List<ChatModel> chatList) {
		this.chatList = chatList;
	}
	public BottleModel getBottleInfo() {
		return bottleInfo;
	}
	public void setBottleInfo(BottleModel bottleInfo) {
		this.bottleInfo = bottleInfo;
	}
}