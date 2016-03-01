package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class BottleModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long bottleIdPk;
	private String bottleId;//瓶子的id
	private String content;//瓶子的内容
	private String remark;//备注
	private String contentType;//内容类别
	private String bottleType;//瓶子类型
	private String ubId;
	private String bottleSort;
	private String sysType;
	private Date createTime;//Date
	private boolean hasAnswer;
	//状态，0为可用，1为对方删除瓶子了无法联系
	private int state;
	private int isBack; //是否可以回复，0表示不可以，1表示可以
	private int fromOther; // 1 别人的沙滩
	
	
	public int getFromOther() {
		return fromOther;
	}
	public void setFromOther(int fromOther) {
		this.fromOther = fromOther;
	}
	public int getIsBack() {
		return isBack;
	}
	public void setIsBack(int isBack) {
		this.isBack = isBack;
	}
	/**
	 * 奖励数
	 */
	private int awardNum;
	/**
	 * 奖励类别
	 */
	private String awardType;
	/**
	 * 瓶子破碎时间(只有阅后即焚瓶才有)
	 */
	private String bottleTime;
	/**
	 * 赌注（只有游戏瓶子才有）
	 */
	private int money;
	
	//user
	private UserInfoModel userInfo;
	
	//非文字瓶属性
	private String url;
	private String shortPic;
	private int picNum;//TODO int
	private int isRedirect;
	private String redirectUrl;
	private List<Attachment> subList;
	
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
	public String getBottleType() {
		return bottleType;
	}
	public void setBottleType(String bottleType) {
		this.bottleType = bottleType;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public UserInfoModel getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(UserInfoModel userInfo) {
		this.userInfo = userInfo;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getUbId() {
		return ubId;
	}
	public void setUbId(String ubId) {
		this.ubId = ubId;
	}
	public boolean getHasAnswer() {
		return hasAnswer;
	}
	public void setHasAnswer(boolean hasAnswer) {
		this.hasAnswer = hasAnswer;
	}
	public String getBottleSort() {
		return bottleSort;
	}
	public void setBottleSort(String bottleSort) {
		this.bottleSort = bottleSort;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public Long getBottleIdPk() {
		return bottleIdPk;
	}
	public void setBottleIdPk(Long bottleIdPk) {
		this.bottleIdPk = bottleIdPk;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getShortPic() {
		return shortPic;
	}
	public void setShortPic(String shortPic) {
		this.shortPic = shortPic;
	}
	public int getIsRedirect() {
		return isRedirect;
	}
	public void setIsRedirect(int isRedirect) {
		this.isRedirect = isRedirect;
	}
	public List<Attachment> getSubList() {
		return subList;
	}
	public void setSubList(List<Attachment> subList) {
		this.subList = subList;
	}
	public int getPicNum() {
		return picNum;
	}
	public void setPicNum(int picNum) {
		this.picNum = picNum;
	}
	public String getRedirectUrl() {
		return redirectUrl;
	}
	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
	public int getAwardNum() {
		return awardNum;
	}
	public void setAwardNum(int awardNum) {
		this.awardNum = awardNum;
	}
	public String getAwardType() {
		return awardType;
	}
	public void setAwardType(String awardType) {
		this.awardType = awardType;
	}
	public String getBottleTime() {
		return bottleTime;
	}
	public void setBottleTime(String bottleTime) {
		this.bottleTime = bottleTime;
	}
	public int getMoney() {
		return money;
	}
	public void setMoney(int money) {
		this.money = money;
	}
	
	public String getSysType() {
		return sysType;
	}
	public void setSysType(String sysType) {
		this.sysType = sysType;
	}
}