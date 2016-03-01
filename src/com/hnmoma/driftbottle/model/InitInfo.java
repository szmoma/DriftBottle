package com.hnmoma.driftbottle.model;

public class InitInfo{
	private String thawTime;
	private int hasBrush;
	private VersionInfo versionInfo;
	private KeyWordInfo keyWordInfo;
	private ScreenInfo screenInfo;
	private int isVIP;
	
	public ScreenInfo getScreenInfo() {
		return screenInfo;
	}
	public void setScreenInfo(ScreenInfo screenInfo) {
		this.screenInfo = screenInfo;
	}
	public String getThawTime() {
		return thawTime;
	}
	public void setThawTime(String thawTime) {
		this.thawTime = thawTime;
	}
	
	public int getHasBrush() {
		return hasBrush;
	}
	public void setHasBrush(int hasBrush) {
		this.hasBrush = hasBrush;
	}
	public VersionInfo getVersionInfo() {
		return versionInfo;
	}
	public void setVersionInfo(VersionInfo versionInfo) {
		this.versionInfo = versionInfo;
	}
	public KeyWordInfo getKeyWordInfo() {
		return keyWordInfo;
	}
	public void setKeyWordInfo(KeyWordInfo keyWordInfo) {
		this.keyWordInfo = keyWordInfo;
	}
	public int getIsVIP() {
		return isVIP;
	}
	public void setIsVIP(int isVIP) {
		this.isVIP = isVIP;
	}
	
	
}