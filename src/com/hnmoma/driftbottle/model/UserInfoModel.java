package com.hnmoma.driftbottle.model;

import java.io.Serializable;

public class UserInfoModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userId;
	/**
	 * 魅力值
	 */
	private String charm;
	/**
	 * 财富值
	 */
	private String fortune;
	/**
	 * 年龄
	 */
	private int age;
	/**
	 * 星座
	 */
	private String constell;
	private String province;
	private String city;
	private String nickName;
	private String identityType;
	private String descript;
	private String headImg;
	private String hxUserName;
	private String hxPassword;
	private String job;
	private String hobby;
	private String birthday;
	private int isNew;	//是否是第一次登陆
	
	private int isVIP;
	private String tempHeadImg;//未审核的头像
	
	
	public int getIsVIP() {
		return isVIP;
	}
	public void setIsVIP(int isVIP) {
		this.isVIP = isVIP;
	}
	public String getJob() {
		return job;
	}
	public void setJob(String job) {
		this.job = job;
	}
	public String getHobby() {
		return hobby;
	}
	public void setHobby(String hobby) {
		this.hobby = hobby;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
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
	public String getHxUserName() {
		return hxUserName;
	}
	public void setHxUserName(String hxUserName) {
		this.hxUserName = hxUserName;
	}
	public String getHxPassword() {
		return hxPassword;
	}
	public void setHxPassword(String hxPassword) {
		this.hxPassword = hxPassword;
	}
	public String getCharm() {
		return charm;
	}
	public void setCharm(String charm) {
		this.charm = charm;
	}
	public String getFortune() {
		return fortune;
	}
	public void setFortune(String fortune) {
		this.fortune = fortune;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getConstell() {
		return constell;
	}
	public void setConstell(String constell) {
		this.constell = constell;
	}
	public int getIsNew() {
		return isNew;
	}
	public void setIsNew(int isNew) {
		this.isNew = isNew;
	}
	public String getTempHeadImg() {
		return tempHeadImg;
	}
	public void setTempHeadImg(String tempHeadImg) {
		this.tempHeadImg = tempHeadImg;
	}
	
	
	
}