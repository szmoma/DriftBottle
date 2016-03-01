package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.LinkedList;

public class TalkListModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String userId; //用户Id
	String bottleId;//瓶子id
	String content;//瓶子内容
	String contentType; //内容类别
	String shortPic; //缩略图
	String url; //图片地址
	String state;
	int supportNum; //赞数
	int reviewNum; //评论数
	Long createTime;
	String nickName;
	String headImg;
	int isVIP;
	String constell;
	String identityType;
	String city;
	String province;
	String age;
	LinkedList<CourseItemModel> userList; //用户列表
	LinkedList<ReviewListModel> reviewLsit;
	
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getConstell() {
		return constell;
	}
	public void setConstell(String constell) {
		this.constell = constell;
	}
	public String getIdentityType() {
		return identityType;
	}
	public void setIdentityType(String identityType) {
		this.identityType = identityType;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
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
	public String getBottleId() {
		return bottleId;
	}
	public void setBottleId(String bottleId) {
		this.bottleId = bottleId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getShortPic() {
		return shortPic;
	}
	public void setShortPic(String shortPic) {
		this.shortPic = shortPic;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public int getSupportNum() {
		return supportNum;
	}
	public void setSupportNum(int supportNum) {
		this.supportNum = supportNum;
	}
	public int getReviewNum() {
		return reviewNum;
	}
	public void setReviewNum(int reviewNum) {
		this.reviewNum = reviewNum;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	public LinkedList<ReviewListModel> getReviewLsit() {
		return reviewLsit;
	}
	public void setReviewLsit(LinkedList<ReviewListModel> reviewLsit) {
		this.reviewLsit = reviewLsit;
	}
	public int getIsVIP() {
		return isVIP;
	}
	public void setIsVIP(int isVIP) {
		this.isVIP = isVIP;
	}
	public LinkedList<CourseItemModel> getUserList() {
		return userList;
	}
	public void setUserList(LinkedList<CourseItemModel> userList) {
		this.userList = userList;
	}
	
}
