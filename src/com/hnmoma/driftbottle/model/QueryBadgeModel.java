package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.List;

public class QueryBadgeModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String code;
	String msg;
	List<BadgeListModel>  badgeList;
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
	public List<BadgeListModel> getBadgeList() {
		return badgeList;
	}
	public void setBadgeList(List<BadgeListModel> badgeList) {
		this.badgeList = badgeList;
	}
	
	
	
	
}
