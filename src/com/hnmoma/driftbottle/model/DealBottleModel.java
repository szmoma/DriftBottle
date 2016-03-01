package com.hnmoma.driftbottle.model;

import java.util.Date;

public class DealBottleModel extends BaseModel {
	
	private Date dealTime;

	public Date getDealTime() {
		return dealTime;
	}

	public void setDealTime(Date dealTime) {
		this.dealTime = dealTime;
	}
}