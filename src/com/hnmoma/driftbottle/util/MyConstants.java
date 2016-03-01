package com.hnmoma.driftbottle.util;


public final class MyConstants {
	
//	public static final String APP_ID = "wx91d10becb0283201";
	public static final String APP_ID = "wx04b7204f4d0f264f";
	
	public static final String QQURI = "com.tencent.mobileqq.activity.JumpActivity";
	public static final int QQREQUESTCODE = 5000;
	public static final String SHARE_TYPE_GIF = "image/gif";
	
	public static final String WX_ACTION = "wx_action";
	
	public static final String ACCESS_TYPE = "access_type";
    public static final int ACCESS_TYPE_MAIN = 0;
    public static final int ACCESS_TYPE_IMAGE_CAPTURE = 1;
    public static final int ACCESS_TYPE_IMAGE_PICK = 2;
    public static final int ACCESS_TYPE_WEIXIN = 3;
    
    //最大文件数
    public static final int MAX_UPLOADCACHE = 50;
    
    public static final String SP_MOODEMOJI_TYPE = "MoodEmojiType";
    
    /** 官方网站 */
	public static final String WEBSITE_ADDR = "http://www.52plp.com";
	/** 官方新浪微博 */
	public static final String SDK_SINAWEIBO_UID = "2736205540";
	/** 官方腾讯微博 */
	public static final String SDK_TENCENTWEIBO_UID = "f4show";
	
	public static final String SHARE_URL = "http://www.52plp.com/BottleOss/bottle/share?bottleId=";
	public static final String MYWEB_URL = "http://www.52plp.com";
	public static final String BOTTLE01_URL = "http://bottlefile.oss-cn-hangzhou.aliyuncs.com/256.png";
	public static final String SHARE_TITLE = "漂流瓶子分享";
	
	//广告地址
	public static final String ADS_URL = "http://www.52plp.com/BottleOss/recomm/recomm.html";
	
	//最大捞瓶子数
    public static final int MAX_PICKUP_NUM = 10;
    public static final int YK_MAX_PICKUP_NUM = 3;
    public static final int MAX_THROW_NUM = 5;
    
    //小八的起始Y位置
    public static final int XIAOBA_Y = 200;
    
    public static final String DB_NAME = "driftBottle_store";  
    
    public static final String MSG_BROADCAST_ACTION = "com.hnmoma.driftbottle.msgreceiver";//消息广播action
    /**
     * 用户信息改变广播
     */
    public static final String USERINFOCHANGE_BROADCAST_ACTION = "com.hnmoma.driftbottle.userinfochangereceiver";
    
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_LOCATION = 3;
    public static final int TYPE_VOICE = 4;
    
    public static final String HELPURL = "http://www.52plp.com/BottleOss/mbhelp.htm";
	
	private MyConstants() {
	}

	public static class Config {
		public static final boolean DEVELOPER_MODE = false;
	}
	
	public static final String GROUP_USERNAME = "item_groups";
	public static final String NEW_FRIENDS_USERNAME = "item_new_friends";
	public static final String MESSAGE_ATTR_IS_VOICE_CALL = "is_voice_call";
	/**
	 * 默认0是普通对话类型
	 * 1为瓶子消息类型
	 * 2为瓶子通知类型
	 */
	public static final String MESSAGE_ATTR_VIEWTYPE = "viewType";
	/**
	 * 0是普通捡起的瓶子
	 * 1定向道具瓶子
	 */
	public static final String MESSAGE_ATTR_BOTTLETYPE = "bottleType";
	public static final String MESSAGE_ATTR_HEADIMG = "msg_headImg";
	
	public static final String MESSAGE_ATTR_BOTTLEMSG = "bottle_msg";
	public static final String MESSAGE_ATTR_STRANGER = "stranger";
	
	public static final String MESSAGE_ATTR_LISTTYPE = "list_type";
	/**
	 * 标记该消息是否是打招呼
	 */
	public static final String MESSAGE_ATTR_ISSAYHELLO = "isSayHello";
	public static final String ISChat_SAYHELLO = "isChat_SayHello";
	public static final String MSG_CONTENT = "msg_content"; 
	
	/**
	 * 默认0是非通知消息
	 * 1为开始建立联系
	 * 2为断开联系
	 * 3为等待回复
	 */
	public static final String MESSAGE_ATTR_NOTICETYPE = "noticeType";
	public static final String MESSAGE_ATTR_BOTTLE = "msg_bottle";
	public static final String HELLOMSG_ATTR_COUNT = "hellomsg_count";
	/**
	 * bottleActionType
	 */
	public static final String MESSAGE_ACTION_DELBOTTLE = "action_delbottle";
	/**
	 * 空间消息通知
	 */
	public static final String MESSAGE_ACTION_VZONETYPE = "action_vzone_newmsg";

	public static final String UPDATEGAMESTATUS = "action_game_answer";	//更新聊天中的游戏互动状态
	
	public static final String MESSAGE_ACTION_BOTTLE="action_bottle_newmsg"; //瓶子消息---动态

}