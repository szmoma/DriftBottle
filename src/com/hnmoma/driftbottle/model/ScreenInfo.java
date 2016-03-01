package com.hnmoma.driftbottle.model;

import java.util.Date;

public class ScreenInfo {

	private String version;
	private Date startTime;
	private Date endTime;
	private String gifUrl;
	private String backgroundUrl;
	private String descript;
	private int manNum;
	private int womanNum;
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public String getGifUrl() {
		return gifUrl;
	}
	public void setGifUrl(String gifUrl) {
		this.gifUrl = gifUrl;
	}
	public String getBackgroundUrl() {
		return backgroundUrl;
	}
	public void setBackgroundUrl(String backgroundUrl) {
		this.backgroundUrl = backgroundUrl;
	}
	public String getDescript() {
		return descript;
	}
	public void setDescript(String descript) {
		this.descript = descript;
	}
	public int getManNum() {
		return manNum;
	}
	public void setManNum(int manNum) {
		this.manNum = manNum;
	}
	public int getWomanNum() {
		return womanNum;
	}
	public void setWomanNum(int womanNum) {
		this.womanNum = womanNum;
	}
	
	
}
