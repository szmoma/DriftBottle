package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.List;

public class PickBottleModel extends BaseModel implements Serializable{
	private BottleModel bottleInfo;
	private ActionNumModel actionNum;
	private List<CommentModel> reviewList;
	
	public BottleModel getBottleInfo() {
		if(bottleInfo == null) 
			bottleInfo = new BottleModel();
		return bottleInfo;
	}

	public void setBottleInfo(BottleModel bottleInfo) {
		this.bottleInfo = bottleInfo;
	}

	public ActionNumModel getActionNum() {
		return actionNum;
	}

	public void setActionNum(ActionNumModel actionNum) {
		this.actionNum = actionNum;
	}

	public List<CommentModel> getCommentModels() {
		return reviewList;
	}

	public void setCommentModels(List<CommentModel> reviewList) {
		this.reviewList = reviewList;
	}
	
}
