package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.List;

public class ChatMsgModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Long bottleIdPk;
	private List<Chat> chatList;
	
	public Long getBottleIdPk() {
		return bottleIdPk;
	}
	public void setBottleIdPk(Long bottleIdPk) {
		this.bottleIdPk = bottleIdPk;
	}
	public List<Chat> getChatList() {
		return chatList;
	}
	public void setChatList(List<Chat> chatList) {
		this.chatList = chatList;
	}
}