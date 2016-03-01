package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.List;

public class CommentMainModel implements Serializable {
	private String code;
	private String msg;
	private String isMore;
	private List<CommentModel> reviewList;

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

	public List<CommentModel> getReviewList() {
		return reviewList;
	}

	public void setReviewList(List<CommentModel> reviewList) {
		this.reviewList = reviewList;
	}

}
