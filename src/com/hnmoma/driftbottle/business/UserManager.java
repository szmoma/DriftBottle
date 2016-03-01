package com.hnmoma.driftbottle.business;

import java.util.List;

import android.content.Context;

import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.model.DaoSession;
import com.hnmoma.driftbottle.model.User;
import com.hnmoma.driftbottle.model.UserDao;
import com.hnmoma.driftbottle.model.UserInfoModel;

public class UserManager {

	public static final String COLUMN_NAME_CONTACTS = "contacts";
	public static final String COLUMN_NAME_BLACKLIST = "blacklist";
	Context context;
	private static volatile UserManager INSTANCE;
	private static Object INSTANCE_LOCK = new Object();
	
	public static UserManager getInstance(Context paramContext){
	    if (INSTANCE == null)
	      synchronized (INSTANCE_LOCK){
	        if (INSTANCE == null)
	        	INSTANCE = new UserManager();
	        	INSTANCE.init(paramContext);
	      }
	    return INSTANCE;
	}
	
	public void init(Context paramContext){
		this.context = paramContext;
//	    this.bmobPush = new BmobPushManager(this.context);
	}
	

	public User getCurrentUser(){
		User user = null;
//	    return (BmobChatUser)BmobUser.getCurrentUser(this.context, BmobChatUser.class);
		DaoSession daoSession = MyApplication.getApp().getDaoSession();
		UserDao userDao = daoSession.getUserDao();
		List<User> list = userDao.loadAll();
		if(list != null && list.size() != 0){
			user = list.get(0);
		}
		return user;
	}

	public String getCurrentUserName(){
		if (getCurrentUser() != null)
			return getCurrentUser().getNickName();
	    return "";
	}
	
	public String getCurrentUserId(){
	    if (getCurrentUser() != null)
	      return getCurrentUser().getUserId();
	    return "";
	}
	
	
	public void logout(){
		DaoSession daoSession = MyApplication.getApp().getDaoSession();
		UserDao userDao = daoSession.getUserDao();
		userDao.deleteAll();
		
		//此处，应该把用户的密码设置为null,删除所有的消息，这是不友好的
		
		
	}
	
	public void login(UserInfoModel ui){
		DaoSession daoSession = MyApplication.getApp().getDaoSession();
		UserDao userDao = daoSession.getUserDao();
		//删除所有的，退出登录
		userDao.deleteAll();
		//重新生成sp文件
		MyApplication.getApp().getSpUtil(true, ui.getUserId());
		
		User user = new User();
		user.setCity(ui.getCity());
		user.setDescript(ui.getDescript());
		user.setHeadImg(ui.getHeadImg());
		user.setIdentityType(ui.getIdentityType());
		user.setNickName(ui.getNickName());
		user.setProvince(ui.getProvince());
		user.setUserId(ui.getUserId());
		user.setAge(ui.getAge());
		user.setConstell(ui.getConstell());
		user.setHobby(ui.getHobby());
		user.setIsVIP(ui.getIsVIP());
		user.setTempHeadImg(ui.getTempHeadImg());
		userDao.insert(user);
	}

	public void register(){
	}
	/**
	 * 变更用户信息：会员信息发生变化时，直接更改数据库中的字段。
	 * @param isVip 如果isVip是true，需要变更的用户是VIP
	 */
	public User updateUserInfo(boolean isVip){
		DaoSession daoSession = MyApplication.getApp().getDaoSession();
		UserDao userDao = daoSession.getUserDao();
		User user = getCurrentUser();
		user.setIsVIP(isVip?1:0);
		userDao.update(user);
		return user;
	}
}
