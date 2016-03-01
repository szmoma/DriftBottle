package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.List;

public class VisistorModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1111052966260479889L;
	
	String code;
	String msg;
	String isMore;
	List<VisitorListModel> visitorList;
	
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
	public String getIsMore() {
		return isMore;
	}
	public void setIsMore(String isMore) {
		this.isMore = isMore;
	}
	public List<VisitorListModel> getVisitorList() {
		return visitorList;
	}
	public void setVisitorList(List<VisitorListModel> visitorList) {
		this.visitorList = visitorList;
	}
	

}
