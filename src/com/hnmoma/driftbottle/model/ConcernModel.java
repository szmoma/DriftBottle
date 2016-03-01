package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.List;

public class ConcernModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String code;
	String msg;
	int isMore;
	List<AttentionModel> attentionList;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getIsMore() {
		return isMore;
	}
	public void setIsMore(int isMore) {
		this.isMore = isMore;
	}
	public List<AttentionModel> getAttentionList() {
		return attentionList;
	}
	public void setAttentionList(List<AttentionModel> attentionList) {
		this.attentionList = attentionList;
	}

	
}
