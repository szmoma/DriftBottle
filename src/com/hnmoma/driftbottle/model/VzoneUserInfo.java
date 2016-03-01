package com.hnmoma.driftbottle.model;

import java.io.Serializable;

public class VzoneUserInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userId; //用户id
	private String province;	//省
	private String city;	//城市
	private String nickName;//昵称
	private String age;	//年纪
	private int pointNum;//	积分
	private int isVIP;//	是否为vip,0表示否，1表示是
	private int loveNum	;//爱心数
	private int fansNum	;//粉丝数
	private String constell;	//星座
	private String identityType; //用户身份类型
	private String descript; //个人描述
	private String headImg;	//头像
	private int visitsNum;	//访问量
	private int charm; //魅力值
	private int fortune;	//财富值
	private String score;	//胜-负-平
	private int picNum; //照片数量
	private int msgNum;	//未读消息
	private int giftNum;		//礼物数量
	
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public String getConstell() {
		return constell;
	}
	public void setConstell(String constell) {
		this.constell = constell;
	}
	public int getPicNum() {
		return picNum;
	}
	public void setPicNum(int picNum) {
		this.picNum = picNum;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getIdentityType() {
		return identityType;
	}
	public void setIdentityType(String identityType) {
		this.identityType = identityType;
	}
	public String getDescript() {
		return descript;
	}
	public void setDescript(String descript) {
		this.descript = descript;
	}
	public String getHeadImg() {
		return headImg;
	}
	public void setHeadImg(String headImg) {
		this.headImg = headImg;
	}
	public int getVisitsNum() {
		return visitsNum;
	}
	public void setVisitsNum(int visitsNum) {
		this.visitsNum = visitsNum;
	}
	
	public int getGiftNum() {
		return giftNum;
	}
	public void setGiftNum(int giftNum) {
		this.giftNum = giftNum;
	}
	
	public int getCharm() {
		return charm;
	}
	public void setCharm(int charm) {
		this.charm = charm;
	}
	public int getFortune() {
		return fortune;
	}
	public void setFortune(int fortune) {
		this.fortune = fortune;
	}
	public int getMsgNum() {
		return msgNum;
	}
	public void setMsgNum(int msgNum) {
		this.msgNum = msgNum;
	}
	public int getPointNum() {
		return pointNum;
	}
	public void setPointNum(int pointNum) {
		this.pointNum = pointNum;
	}
	public int getIsVIP() {
		return isVIP;
	}
	public void setIsVIP(int isVIP) {
		this.isVIP = isVIP;
	}
	public int getLoveNum() {
		return loveNum;
	}
	public void setLoveNum(int loveNum) {
		this.loveNum = loveNum;
	}
	public int getFansNum() {
		return fansNum;
	}
	public void setFansNum(int fansNum) {
		this.fansNum = fansNum;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	
	
}
