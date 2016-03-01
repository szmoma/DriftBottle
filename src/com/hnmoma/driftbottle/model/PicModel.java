package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.List;

public class PicModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5338350457502429139L;
	/**
	 * 
	 */
	String code;
	String msg;
	String isMore;
	List<PhotoWallModel> picList;
	int picNum;//已经上传的相册总数
	
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
	public List<PhotoWallModel> getPicList() {
		return picList;
	}
	public void setPicList(List<PhotoWallModel> picList) {
		this.picList = picList;
	}
	public int getPicNum() {
		return picNum;
	}
	public void setPicNum(int picNum) {
		this.picNum = picNum;
	}

}
