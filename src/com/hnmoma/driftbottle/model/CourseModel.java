package com.hnmoma.driftbottle.model;

import java.io.Serializable;
import java.util.List;

public class CourseModel extends BaseModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<CourseItemModel>  userList;
	
	public List<CourseItemModel> getUserList() {
		return userList;
	}
	public void setUserList(List<CourseItemModel> userList) {
		this.userList = userList;
	}
	
	
}
