package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.Date;

public class CommentModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String reviewId;
	private String userId;
	private String nickName;
	private String headImg;
	private String reContent;
	private String contentType;
	private Date reTime;
	private String favourNum;
	private String city;
	private int isVIP;
	
	public int getIsVIP() {
		return isVIP;
	}
	public void setIsVIP(int isVIP) {
		this.isVIP = isVIP;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public CommentModel(){
	}
	public CommentModel(String reviewId,
	 String userId,
	 String nickName,
	 String headImg,
	 String reContent,
	 String contentType,
	 Date reTime,
	 String favourNum) {
		this.reviewId = reviewId;
		this.userId = userId;
		this.nickName = nickName;
		this.headImg = headImg;
		this.reContent = reContent;
		this.contentType = contentType;
		this.reTime = reTime;
		this.favourNum = favourNum;
	}
	
	public String getReviewId() {
		return reviewId;
	}

	public void setReviewId(String reviewId) {
		this.reviewId = reviewId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getHeadImg() {
		return headImg;
	}

	public void setHeadImg(String headImg) {
		this.headImg = headImg;
	}

	public String getReContent() {
		return reContent;
	}

	public void setReContent(String reContent) {
		this.reContent = reContent;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getFavourNum() {
		return favourNum;
	}

	public void setFavourNum(String favourNum) {
		this.favourNum = favourNum;
	}

	public Date getReTime() {
		return reTime;
	}

	public void setReTime(Date reTime) {
		this.reTime = reTime;
	}
}