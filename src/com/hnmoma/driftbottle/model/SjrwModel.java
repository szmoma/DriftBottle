package com.hnmoma.driftbottle.model;

import java.io.Serializable;

public class SjrwModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String taskId;
	private int iconId;
	private int price;
	private long lastTime;
	private String taskName;
	private String taskDesc;
	private boolean canClick;
	
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public int getIconId() {
		return iconId;
	}
	public void setIconId(int iconId) {
		this.iconId = iconId;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public long getLastTime() {
		return lastTime;
	}
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public String getTaskDesc() {
		return taskDesc;
	}
	public void setTaskDesc(String taskDesc) {
		this.taskDesc = taskDesc;
	}
	public boolean isCanClick() {
		return canClick;
	}
	public void setCanClick(boolean canClick) {
		this.canClick = canClick;
	}
}
