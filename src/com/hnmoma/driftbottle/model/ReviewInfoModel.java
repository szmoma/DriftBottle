package com.hnmoma.driftbottle.model;

import java.io.Serializable;

public class ReviewInfoModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String code;
	String msg;
	ReviewListModel reviewInfo;
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
	public ReviewListModel getReviewInfo() {
		return reviewInfo;
	}
	public void setReviewInfo(ReviewListModel reviewInfo) {
		this.reviewInfo = reviewInfo;
	}
	
	

}
