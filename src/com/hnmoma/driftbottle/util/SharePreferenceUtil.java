package com.hnmoma.driftbottle.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.renren.Renren;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;

import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.model.CommentModel;
import com.hnmoma.driftbottle.model.ScreenInfo;

@SuppressLint("CommitPrefEdits")
public class SharePreferenceUtil {
	
	private SharedPreferences mSharedPreferences;
	private static SharedPreferences.Editor editor;

	public SharePreferenceUtil(Context context, String name) {
		mSharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		editor = mSharedPreferences.edit();
	}
//	private String SHARED_KEY_NOTIFY = "shared_key_notify";
//	private String SHARED_KEY_VOICE = "shared_key_sound";
//	private String SHARED_KEY_VIBRATE = "shared_key_vibrate";
	
	private final String LOCKSCREENPWD="lockScreenPwd";	//描述手势密码的字段
	private final String LOCKSCREENTOGGLE = "LockScreenToggle";	//设置手势开关
	
	private final String SUPORTIDEA = "supportIdea";//支持某种观点 
	
	private final String PIC_NUM_LIMIT = "pic";//照片的上限数
	private final String ATTENTION_NUM_LIMIT="attention";	//关注上限制
	private final String VISITOR_NUM_LIMIT = "visitor";	//访客上限数
	private final String CATCH_NUM_LIMIT="catch"; //捞网上限数
	private final String UPPERLIMIT ="updateNumLimt";	//最后已从从服务器上更新照片上限、关注上限、访客上限、捞网上限的时间
	private static final String NEWMSG = "msg_record"; //新消息的标记
	
//	// 是否允许推送通知
//	public boolean isAllowPushNotify() {
//		return mSharedPreferences.getBoolean(SHARED_KEY_NOTIFY, true);
//	}
//
//	public void setPushNotifyEnable(boolean isChecked) {
//		editor.putBoolean(SHARED_KEY_NOTIFY, isChecked);
//		editor.commit();
//	}

//	// 允许声音
//	public boolean isAllowVoice() {
//		return mSharedPreferences.getBoolean(SHARED_KEY_VOICE, true);
//	}
//
//	public void setAllowVoiceEnable(boolean isChecked) {
//		editor.putBoolean(SHARED_KEY_VOICE, isChecked);
//		editor.commit();
//	}

	public String getClientid() {
		return mSharedPreferences.getString("clientId", "");
	}
	
	public void setClientId(String cid) {
		editor.putString("clientId", cid);
		editor.commit();
	}

	public boolean getClientIdBound() {
		return mSharedPreferences.getBoolean("clientIdBound", false);
	}

	public void setClientIdBound(boolean bound) {
		editor.putBoolean("clientIdBound", bound);
		editor.commit();
	}
	
	//================系统消息处理====================
	public void setNoticeMsg(String[] msg) {
		if(msg!=null && msg.length!=0){
			String strs = new String();
			for(String str : msg){
				strs += str + "&&";
			}
			
			String[] oldmsg = getNoticeMsg();
			if(oldmsg!=null && oldmsg.length!=0){
				String old_strs = new String();
				for(String str_old : oldmsg){
					old_strs += str_old + "&&";
				}
				
				strs += old_strs;
			}
			
			editor.putString("noticeMsg", strs);
			editor.commit();
		}else{
			editor.putString("noticeMsg", "");
			editor.commit();
		}
	}
	
	public String[] getNoticeMsg() {
		String [] result = null;
		String msg = mSharedPreferences.getString("noticeMsg", "");
		if(!TextUtils.isEmpty(msg)){
			result = msg.split("&&");
		}
		
		return result;
	}
	/**
	 * 
	 * @param showBottles 沙滩和海里的瓶子数量
	 * @return
	 */
	public int getBottles(int showBottles){
		/**
		 * 如果沙滩和海里的瓶子总数大于5，则不需要生成新的瓶子;<br/>
		 * 如果瓶子总数不大于0，说明没有瓶子，则不需要生成瓶子;<br/>
		 */
		if(showBottles>=5)
			return 0;
		
		int sum = getOldBottle();//瓶子的总数
		if(sum==0){ //第一次是时候，需要生成瓶子
			long oldTime =getGenerateOldTime();
			long cot = System.currentTimeMillis() - oldTime;
			if(cot>=10*60*1000){ //间隔时间大于10Min时，生成一批瓶子
				setGenerateOldTime();
				sum = 10;
				/**
				 * 可见瓶子是0时，如果瓶子总数存在5个以上的瓶子，只需要生成5个瓶子；如果瓶子总数不大于5，则把后台的瓶子全部可见
				 */
				if(showBottles==0){	//如果可见的瓶子数量是0,且瓶子总数大于5，则生成5个可见的瓶子
					setOldBottle(5);
					return 5;
				}else{
					/**
					 * 可见瓶子的范围是(0,5)时候，随机生成瓶子
					 */
					int num = 5-showBottles; //至多补充的瓶子数量
					if(sum>=num){
						setOldBottle(sum-num);
					}else{
						setOldBottle(0);
						num=sum;
					}
					return num;
				}
			}else{
				return 0;
			}
		}else{
			/**
			 * 可见瓶子是0时，如果瓶子总数存在5个以上的瓶子，只需要生成5个瓶子；如果瓶子总数不大于5，则把后台的瓶子全部可见
			 */
			if(showBottles==0){	//如果可见的瓶子数量是0,且瓶子总数大于5，则生成5个可见的瓶子
				if(sum<=5){
					setOldBottle(0);
					return sum;
				}else{//大于5个时候，返回5个
					setOldBottle(sum-5); 
					return 5;
				}
			}else{
				/**
				 * 可见瓶子的范围是(0,5)时候，随机生成瓶子
				 */
				int num = 5-showBottles; //至多补充的瓶子数量
				if(sum>=num){
					setOldBottle(sum-num);
				}else{
					setOldBottle(0);
					num=sum;
				}
				return num;
			}
			
		}
		
	}
	
	//控制在0-5间,如果没用完，就留到下次
	//瓶子一个小时刷新一次，间隔大于等于60小时直接5个瓶子。间隔小于5分钟不刷新，大于5小于30（1-3），大于30小于60（2-4）
	public int getGenerateBottles(int oldBottle){
		int result = 0;
		
		//获取当前时间,判断是否连续退出和进入问题
		long currentTime = System.currentTimeMillis();
		long oldTime = getGenerateOldTime();
		long cot = currentTime - oldTime;
		
		if(oldBottle>=5){
      		//保存最后一次生成的时间
			setGenerateOldTime();
			return 5;
		}
		
      //初次使用直接返回5个瓶子
        if(oldTime == -1){
			//保存最后一次生成的时间
			setGenerateOldTime();
			return 5;
		}
      	
      	Random random = new Random();
        if(cot >= 1000*60*30){//30
        	result = 5;
        }else if(cot >= 1000*60*15){// 15
			result = 3+random.nextInt(1);
        }else if(cot >= 1000*60*3){// 3
			result = 2+random.nextInt(1);
        }
        
        result += oldBottle;
        if(result > 5){
        	result = 5;
        }
        
		if(result>0){
			//保存最后一次生成的时间
			setGenerateOldTime();
		}
		
		return result;
	}
	
	public long getGenerateOldTime(){
		return mSharedPreferences.getLong("generateOldTime", 0L);
	}
	public void setGenerateOldTime(){
		editor.putLong("generateOldTime", System.currentTimeMillis()).commit();
	}
	
	public int getYkPickUpNum() {
		int result = 0;
		
		//获取系统时间
		Time time = new Time(TimeZone.getDefault().getID());       
        time.setToNow();      
        int year = time.year;      
        int month = time.month + 1;      
        int day = time.monthDay; 
        String nowStr = ""+year+month+day;
		String oldStr = mSharedPreferences.getString("yKpickUpDay", "");
		if(!oldStr.equals(nowStr)){
			editor.putString("yKpickUpDay", nowStr);
			editor.putInt("yKpickUpNum", MyConstants.YK_MAX_PICKUP_NUM);
			editor.commit();
			
			result = MyConstants.YK_MAX_PICKUP_NUM;
		}else{
			result = mSharedPreferences.getInt("yKpickUpNum", MyConstants.YK_MAX_PICKUP_NUM);
		}
		
		return result;
	}
	
	/**
	 * 设置没捡玩的瓶子数量
	 */
	public synchronized void  setOldBottle(int oldBottle){
		editor.putInt("oldBottle", oldBottle).commit();
	}
	
	public synchronized int getOldBottle(){
		return mSharedPreferences.getInt("oldBottle", 0);
	}
	
	/**
	 * 是否能扔瓶子
	 * @return
	 */
	public boolean  canThrow() {
//		int result = 0;
		//获取系统时间
//		Time time = new Time(TimeZone.getDefault().getID());       
//        time.setToNow();      
//        int year = time.year;      
//        int month = time.month;      
//        int day = time.monthDay; 
//        String nowStr = ""+year+month+day;
//		String oldStr = mSharedPreferences.getString("throwDay_", "");
//		if(!oldStr.equals(nowStr)){
//			editor.putString("throwDay_", nowStr);
//			editor.putInt("throwNum_", MyConstants.MAX_THROW_NUM);
//			editor.commit();
//			
//			result = MyConstants.MAX_THROW_NUM;
//		}else{
//			result = mSharedPreferences.getInt("throwNum_", MyConstants.MAX_THROW_NUM);
//		}
		
	   long currentTime = System.currentTimeMillis();
	   long lastThrowTime = 0L;
	   try {
	  		//将最后一次扔瓶子的时间转化为日历
	  	  		 lastThrowTime = mSharedPreferences.getLong("lastThrowTime_", 0L);
			} catch (ClassCastException e) { 
				// TODO: handle exception
				//版本升级问题，兼容上一版本的“lastThrowTime_”值是字符类型，保存自定义的时间字符串，最好格式化字符串类型的时间
				Time time = new Time(TimeZone.getDefault().getID());       
		        time.setToNow();      
		        int year = time.year;      
		        int month = time.month + 1;      
		        int day = time.monthDay; 
		        
		        String tmpTime = ""+year+month+day;
				tmpTime = mSharedPreferences.getString("lastThrowTime_", tmpTime).trim();
				try {
					//lastThrowTime_值不是一个标准的时间格式，把它转化成yyyyMMdd格式
					tmpTime = MoMaUtil.formatTimeString(tmpTime);
					String pattern = "yyyyMMdd";
					SimpleDateFormat sdf = new SimpleDateFormat(pattern);
					Date date = sdf.parse(tmpTime.toString());
					lastThrowTime = date.getTime();
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					lastThrowTime = 0L;
				}
			}
 		
	   boolean isThrowed = false;
	   if((currentTime - lastThrowTime) >= 30*1000) {
		 isThrowed = true;
	   }
		return isThrowed;
	 }
	
	/**
	 *设置上次丢的时间
	 * @return
	 */
	public void setLastThrowTime() {
		//获取系统时间
//		Time time = new Time(TimeZone.getDefault().getID());       
//        time.setToNow();      
//        int year = time.year;      
//        int month = time.month;      
//        int day = time.monthDay; 
//        String nowStr = ""+year+month+day;
        long lastThrowTime = System.currentTimeMillis();
        
        editor.putLong("lastThrowTime_", lastThrowTime);
//		editor.putInt("throwNum_", throwNum);
//		editor.putString("throwDay_", nowStr);
		editor.commit();
	}
	
	//===================================================
	//================热榜数据缓存====================
		public void setHotTopCache(int index, String str) {
			editor.putString("HotTopCache_"+index, str);
			editor.commit();
		}
		
		public String getHotTopCache(int index) {
			return mSharedPreferences.getString("HotTopCache_"+index, "");
		}
	
	//===================================================
	//================排行榜数据缓存====================
	public void setPhbCache(int index, int position, String str) {
		editor.putString("phbCache_"+index + position, str);
		editor.commit();
	}
	
	public String getPhbCache(int index, int position) {
		return mSharedPreferences.getString("phbCache_"+index+ position, "");
	}
	
	//================礼物数据缓存====================
	public void setGiftCache(String str) {
		editor.putString("giftCache", str);
		editor.commit();
	}
		
	public String getGiftCache() {
		return mSharedPreferences.getString("giftCache", "");
	}
	
	//================魅力商城数据缓存====================
		public void setCharmCache(String str) {
			editor.putString("charmCache", str);
			editor.commit();
		}
			
		public String getCharmCache() {
			return mSharedPreferences.getString("charmCache", "");
		}
		
	//================道具商城数据缓存====================
//	public void setDjscCache(int index, String str) {
//		editor.putString("djscCache_"+index, str);
//		editor.commit();
//	}
//	
//	public String getDjscCache(int index) {
//		return mSharedPreferences.getString("djscCache_"+index, "");
//	}

	//================闪屏数据缓存====================	
	public void saveSplashData(ScreenInfo mScreenInfo) {
		if(TextUtils.isEmpty(mScreenInfo.getVersion())){
			editor.putString("splash_version", mScreenInfo.getVersion());
		}
		if(mScreenInfo.getStartTime() != null) {
			editor.putLong("splash_startTime", mScreenInfo.getStartTime().getTime());
		}
		if(mScreenInfo.getEndTime() != null) {
			editor.putLong("splash_endTime", mScreenInfo.getEndTime().getTime());
		}
		if(mScreenInfo.getManNum() >= 0 ) {
			editor.putInt("splash_manNum", mScreenInfo.getManNum());
		}
		if(mScreenInfo.getWomanNum() >= 0 ) {
			editor.putInt("splash_womanNum", mScreenInfo.getWomanNum());
		}
	}
	
	public ScreenInfo getSplashData() {
		ScreenInfo info = new ScreenInfo();
		info.setEndTime(new Date(mSharedPreferences.getLong("splash_endTime", 0)));
		info.setStartTime(new Date(mSharedPreferences.getLong("splash_startTime", 0)));
		info.setVersion(mSharedPreferences.getString("splash_version", "0"));
		info.setWomanNum(mSharedPreferences.getInt("splash_womanNum", 0));
		info.setManNum(mSharedPreferences.getInt("splash_manNum", 0));
		return info;
	}
	
	public String getSplashVersion() {
		return mSharedPreferences.getString("splash_version", "0");
	}
		
	public void clearApplicatioinCache() {
		editor.putString("userHeadImg", "");
		editor.putString("HotTopCache_"+0, "");
		editor.putString("HotTopCache_"+1, "");
		for(int position = 0; position < 4; position++) {
			editor.putString("phbCache_"+0+position, "");
			editor.putString("phbCache_"+1+position, "");
		}
		editor.putString("giftCache", "");
		editor.putString("djscCache_"+0, "");
		editor.putString("djscCache_"+1, "");
		editor.putInt("myMoney_", 0);
		editor.putString("charmCache", "");
		editor.putString("gagTime_", ""); //禁言
		editor.commit();
		/************此处存在SharSDK存在NullPointerException，需要做特殊处理---yangsy于2015-6-15修改***********/
		try {
			//清除微博等绑定关系
			Platform plat = ShareSDK.getPlatform(MyApplication.getApp(), SinaWeibo.NAME);
			if (plat!=null && plat.isValid()) {
				plat.removeAccount();
			}
			
			//清除QQ绑定关系
			Platform qqplat = ShareSDK.getPlatform(MyApplication.getApp(), QQ.NAME);
			if (qqplat!=null && qqplat.isValid()) {
				qqplat.removeAccount();
			}
			
			//清除renren绑定关系
			Platform rrplat = ShareSDK.getPlatform(MyApplication.getApp(), Renren.NAME);
			if (rrplat!=null && rrplat.isValid()) {
				rrplat.removeAccount();
			}
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			MoMaLog.e("debug", "ShareSDK空指向异常");
			ShareSDK.initSDK(MyApplication.getApp());
			for(Platform platform:ShareSDK.getPlatformList()){
				platform.removeAccount();
			}
			
		}
	}
	/**
	 * 是否删除账号信息以及缓存
	 * @param isAll 如果是，则三处所有的
	 */
	public void clearApplicatioinCache(boolean isAll) {
		clearApplicatioinCache();
		if(isAll){
			editor.putString(LOCKSCREENPWD, "");
			editor.putBoolean(LOCKSCREENTOGGLE, false);
			editor.putLong("lastThrowTime_", 0L);
			editor.putLong("lastSpqUpdateTime_", 0L);
			editor.putInt(PIC_NUM_LIMIT, 20);
			editor.putInt(ATTENTION_NUM_LIMIT, 10);
			editor.putInt(VISITOR_NUM_LIMIT, 15);
			editor.putInt(CATCH_NUM_LIMIT, 20);
			editor.commit();
		}
		
	}
	
	public void dealYkPickUpNum() {
		//获取系统时间
		Time time = new Time(TimeZone.getDefault().getID());       
        time.setToNow();      
        int year = time.year;      
        int month = time.month + 1;      
        int day = time.monthDay; 
        String nowStr = ""+year+month+day;
        
        int times = getYkPickUpNum();
        if(times>0){
        	times = times-1;
        }
		
		editor.putInt("yKpickUpNum", times);
		editor.putString("yKpickUpDay", nowStr);
		editor.commit();
	}
	
	public void setYkPickUpNum(int pickUpNum) {
		//获取系统时间
		Time time = new Time(TimeZone.getDefault().getID());       
        time.setToNow();      
        int year = time.year;      
        int month = time.month + 1;      
        int day = time.monthDay; 
        String nowStr = ""+year+month+day;
        
		editor.putInt("yKpickUpNum", pickUpNum);
		editor.putString("yKpickUpDay", nowStr);
		editor.commit();
	}
	
	public void setPickUpNum(int pickUpNum) {
		//获取系统时间
		Time time = new Time(TimeZone.getDefault().getID());       
        time.setToNow();      
        int year = time.year;      
        int month = time.month + 1;      
        int day = time.monthDay; 
        String nowStr = ""+year+month+day;
		
		editor.putInt("pickUpNum_", pickUpNum);
		editor.putString("pickUpDay_", nowStr);
		editor.commit();
	}
	
	//============================闪金任务======================================
	 public long getLastDateById(String taskId){
    	long lastDate = mSharedPreferences.getLong("sjrw_"+taskId, 0);
    	return lastDate;
	 }
	    
    public void setLastDateById(String taskId){
		editor.putLong("sjrw_"+taskId, System.currentTimeMillis());
		editor.commit();
    }
    
    public String getLastVersion(String taskId){
    	String ver = mSharedPreferences.getString("sjrw_version_"+taskId, "0");
    	return ver;
	 }
    
    public void setLastVersion(String taskId, String version){
		editor.putString("sjrw_version_"+taskId, version);
		editor.commit();
    }
	    
    //================是否当天的第一次扔瓶子===========
  	public boolean isFirstTimeThrow() {
  		//获取系统时间
//  		Time time = new Time(TimeZone.getDefault().getID());       
//        time.setToNow();      
//        int year = time.year;      
//        int month = time.month + 1;      
//        int day = time.monthDay; 
//        String nowStr = ""+year+month+day;
//  	 String oldStr = mSharedPreferences.getString("lastThrowTime_", "");
  		
  		long lastThrowTime = 0;
  		try {
  		//将最后一次扔瓶子的时间转化为日历
  	  		 lastThrowTime = mSharedPreferences.getLong("lastThrowTime_", 0L);
		} catch (ClassCastException e) { 
			// TODO: handle exception
			//版本升级问题，兼容上一版本的“lastThrowTime_”值是字符类型，保存自定义的时间字符串，最好格式化字符串类型的时间
			Time time = new Time(TimeZone.getDefault().getID());       
	        time.setToNow();      
	        int year = time.year;      
	        int month = time.month + 1;      
	        int day = time.monthDay; 
	        
	        String tmpTime = ""+year+month+day;
			tmpTime = mSharedPreferences.getString("lastThrowTime_", tmpTime).trim();
			try {
				//lastThrowTime_值不是一个标准的时间格式，把它转化成yyyyMMdd格式
				tmpTime = MoMaUtil.formatTimeString(tmpTime);
				String pattern = "yyyyMMdd";
				SimpleDateFormat sdf = new SimpleDateFormat(pattern);
				Date date = sdf.parse(tmpTime.toString());
				lastThrowTime = date.getTime();
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				lastThrowTime = 0L;
			}
		}
  		
  		Calendar lastCal = Calendar.getInstance();
  		lastCal.setTime(new Date(lastThrowTime));
  		MoMaLog.e("debug","year="+ lastCal.get(Calendar.YEAR)+",month="+lastCal.get(Calendar.MONTH)+",day="+lastCal.get(Calendar.DATE));
  		//获得当前时间的
  		Calendar currentCal = Calendar.getInstance();
  		MoMaLog.e("debug", "last="+lastCal+",date="+lastCal.get(Calendar.DATE));
  		MoMaLog.e("debug", "curr="+currentCal+"date="+currentCal.get(Calendar.DATE));
  		if(lastCal.get(Calendar.YEAR)>currentCal.get(Calendar.YEAR))
  			return false;
  		else if(lastCal.get(Calendar.YEAR)==currentCal.get(Calendar.YEAR)){
  			if(lastCal.get(Calendar.MONTH)>currentCal.get(Calendar.MONTH))
  				return false;
  			else if (lastCal.get(Calendar.MONTH)==currentCal.get(Calendar.MONTH)) {
				if(lastCal.get(Calendar.DATE)<currentCal.get(Calendar.DATE))
					return true;
				else
					return false;
			}else
				return true;
  		}else{
  			return true;
  		}
  		
  	}
  	
  	public String getDeviceId() {
		return mSharedPreferences.getString("deviceId", "");
	}

	public void setDeviceId(String deviceId) {
		editor.putString("deviceId", deviceId);
		editor.commit();
	}
	
	//=================禁言处理====================
	public void setGagTime(String date) {
		editor.putString("gagTime_", date);
		editor.commit();
	}
	
	public String getGagTime() {
		return mSharedPreferences.getString("gagTime_", "");
	}
	
	//================我的扇贝====================
	public void setMyMoney(int money) {
		editor.putInt("myMoney_", money);
		editor.commit();
	}
	
	public int getMyMoney() {
		return mSharedPreferences.getInt("myMoney_", 0);
	}
	
	public long getSpqOkTime() {
		long oldTime = mSharedPreferences.getLong("lastSpqUpdateTime_", 0L);
		return Math.abs(System.currentTimeMillis()-oldTime);
	}
	
	public void updateSpqRefreshTime() {
		//获取系统时间
		editor.putLong("lastSpqUpdateTime_",  System.currentTimeMillis()).commit();
	}
	/*
	 * 用户对一个瓶子第一次评论的时间
	 */
	public void updateCommentTime(long time) {
		editor.putLong("firstCommentTime", time);
		editor.commit();
	}
	
	/**
	 * 用户对一个瓶子前后评论时间的间隔
	 * @return
	 */
	public long getGapTime() {
		long currentTime = System.currentTimeMillis();
		long oldTime = mSharedPreferences.getLong("firstCommentTime", 0);
		long gap = Math.abs(oldTime - currentTime);
		return gap;
	}
	
	/*
	 * 保存讨论瓶子的评论内容，只保存一条
	 */
	public void saveBottleComment(CommentModel commentModel){
		editor.putString("commentModelNickName", commentModel.getNickName());
		editor.putString("commentModelCity", commentModel.getCity());
		editor.putString("commentModelReContent", commentModel.getReContent());
		editor.putString("commentModelUserId", commentModel.getUserId());
		editor.putString("commentModelFavourNum", commentModel.getFavourNum());
		editor.putString("commentModelHeadImg", commentModel.getHeadImg());
		editor.putLong("commentModelReTime", commentModel.getReTime().getTime());
		
		editor.commit();
	}
	
	/*
	 * 获取讨论瓶子的评论内容
	 */
	public CommentModel getBottleComment() {
		CommentModel comment = new CommentModel();
		comment.setUserId(mSharedPreferences.getString("commentModelUserId", ""));
		comment.setNickName(mSharedPreferences.getString("commentModelNickName", ""));
		comment.setCity(mSharedPreferences.getString("commentModelCity", ""));
		comment.setReContent(mSharedPreferences.getString("commentModelReContent", ""));
		comment.setFavourNum(mSharedPreferences.getString("commentModelFavourNum", ""));
		comment.setHeadImg(mSharedPreferences.getString("commentModelHeadImg", ""));
		comment.setReTime(new Date(mSharedPreferences.getLong("commentModelReTime", 0)));
		return comment;
	}
	
	public void delBottleComment() {
		editor.putString("commentModelNickName", "");
		editor.putString("commentModelCity", "");
		editor.putString("commentModelReContent", "");
		editor.putString("commentModelUserId", "");
		editor.putString("commentModelFavourNum", "");
		editor.putString("commentModelHeadImg", "");
		editor.putLong("commentModelReTime", 0);
		editor.commit();
	}
	
	public void updateBottleID(String bottleID){
		editor.putString("bottleID", bottleID);
		editor.commit();
	}
	//刷瓶器保存
	/**
	 * 获取刷屏器的缓存
	 * @return 如果返回值是0，表示没有刷频器；如果1表示有刷频器
	 */
	public int getBrush(){
		int hasBrush = mSharedPreferences.getInt("hasBrush", 0);
		return hasBrush;
	}
	
	public void setBrush(int hasBrush) {
		editor.putInt("hasBrush", hasBrush);
		editor.commit();
	}
	
	public String getBottleID(){
		String bottleID = mSharedPreferences.getString("bottleID", "");
		return bottleID;
	}
	// 保存用户是否更改了性别
//	public void setGenerChanged(String userId, boolean isChanged) {
//		editor.putBoolean("generChanged", isChanged);
//		editor.putString(userId, userId);
//		editor.commit();
//	}
//	
//	public boolean getGenerChanged() {
//		boolean isChanged = mSharedPreferences.getBoolean("generChanged", false);
//		return isChanged;
//	}
	
	public String getUserId(String userId) {
		String userid = mSharedPreferences.getString(userId, "");
		return userid;
	}
	
	public String getUserHeadImage() {
		return mSharedPreferences.getString("userHeadImg", "");
	}
	
	public void setUserHeadImage(String userHeadImg) {
		editor.putString("userHeadImg", userHeadImg);
		editor.commit();
	}
	
	/**
	 * 丢瓶子提示，false为不显示
	 * @return
	 */
	public boolean getThrowHintFlag(){
		return mSharedPreferences.getBoolean("throwHintFlag", true);
	}
	
	public void setThrowHintFlag(boolean flag){
		editor.putBoolean("throwHintFlag", flag);
		editor.commit();
	}
	//对方是否已经回复
	public boolean getBackedByUserId(String toChatUsername) {
		return mSharedPreferences.getBoolean("Backed_" + toChatUsername, false);
	}
	//设置对方回复
	public void setBackedByUserId(String toChatUsername, boolean backed) {
		if(backed){
			editor.putBoolean("Backed_" + toChatUsername, backed);
			editor.commit();
		}else{
			editor.remove("Backed_" + toChatUsername);
			editor.commit();
		}
	}
	/**
	 * 获得用户的手势密码
	 * @return 返回手势密码的字符串。如果不存在，则返回空格字符。
	 */
	public String getLockScreenPwd(){
		return mSharedPreferences.getString(LOCKSCREENPWD, "");
	}
	/**
	 * 设置用户的手势密码
	 * @param pwd 手势密码字符串
	 */
	public void setLockScreenPwd(String pwd){
		editor.putString(LOCKSCREENPWD, pwd).commit();
	}
	/**
	 * 获取用户的手势锁的配置
	 * @return 如果用户启用手势锁，则返回true，否则返回false
	 */
	public boolean  isScreenLocked() {
		
		return mSharedPreferences.getBoolean(LOCKSCREENTOGGLE, false);
	}
	/**
	 * 设置用户的手势锁
	 * @param isOn 如果isOn是true，则启用手势锁，否则就是关闭手势锁
	 */
	public void setScreenLocked(boolean isOn) {
		editor.putBoolean(LOCKSCREENTOGGLE, isOn).commit();
	}
	/**
	 * 支持某种观点
	 * @return 如果支持某种观点，则返回true，否则返回true
	 */
	public boolean  getSupport() {
		return mSharedPreferences.getBoolean(SUPORTIDEA, false);
	}
	/**
	 * 设置某种观点，如果isSupport是true，表示支持，否则表示反对
	 * @param isSupport
	 */
	public void setSupport(boolean isSupport) {
		editor.putBoolean(SUPORTIDEA, isSupport).commit();
	}
	
	/**
	 * 获取捞网次数的上限值。如果返回Integer.MAX_VALUE，表示无限制
	 * @return int
	 */
	public int getCatchNumLimit(){
		return mSharedPreferences.getInt(CATCH_NUM_LIMIT, 20);
	}
	/**
	 * 设置捞网次数的上限值
	 * @param max
	 */
	public void setCatchNumLimit(int max){
		editor.putInt(CATCH_NUM_LIMIT, max).commit();
	}
	
	/**
	 * 每使用一次捞网，上限数自动减1
	 */
	public void autoDecrementCatchNum(){
		int num = getCatchNumLimit();
		num -=1;
		setCatchNumLimit(num);
	}
	
	/**
	 * 获取访客的上限度。如果获取到的值是Integer.MAX_VALUE，表示无限制
	 * @return int
	 */
	public int getVisitorNumLimit(){
		return mSharedPreferences.getInt(VISITOR_NUM_LIMIT, 15);
	}
	/**
	 * 设置访客的上限值
	 * @param max 最大上限值
	 */
	public void setVisitorNumLimit(int max){
		editor.putInt(VISITOR_NUM_LIMIT, max).commit();
	}
	
	/**
	 * 获取关注的上限值。如果获取到的值是Integer.MAX_VALUE，则表示无限制
	 * @return int
	 */
	public int getAttentionNumLimit(){
		return mSharedPreferences.getInt(ATTENTION_NUM_LIMIT, 10);
	}
	/**
	 * 设置关注的上限值
	 * @param max 最大上限值
	 */
	public void setAttentionNumLimit(int max){
		editor.putInt(ATTENTION_NUM_LIMIT, max).commit();
	}
	
	
	
	/**
	 * 获取相册中照片的上限数。如果获取到的值是Integer.MAX_VALUE，则表示无限制。
	 * @return  int
	 */
	public int getPicNumLimit(){
		return mSharedPreferences.getInt(PIC_NUM_LIMIT, 20);
	}
	/**
	 * 设置相册中照片的上限数
	 * @param num 最大上限值
	 */
	public void setPicNumLimit(int max){
		editor.putInt(PIC_NUM_LIMIT, max).commit();
	}
	
	/**
	 * 设置默认的上限访问
	 * @param defalutNumOfpic 相册中，照片数量上限默认值。无上限用Integer.MAX_VALUE表示
	 * @param defalutNumAttention 关注上限默认值，无上限用Integer.MAX_VALUE表示
	 * @param defaultNumVistor 访客上限默认值，无上限用Integer.MAX_VALUE表示
	 * @param defaultNumCatch 捞网上限默认值，无上限用Integer.MAX_VALUE表示
	 */
	public void defaultNumOfUpperLimit(int defalutNumOfpic,int defalutNumAttention,int defaultNumVistor,int defaultNumCatch){
		editor.putInt(PIC_NUM_LIMIT, defalutNumOfpic);
		editor.putInt(ATTENTION_NUM_LIMIT, defalutNumAttention);
		editor.putInt(VISITOR_NUM_LIMIT, defaultNumVistor);
		editor.putInt(CATCH_NUM_LIMIT, defaultNumCatch);
		editor.putLong(UPPERLIMIT, System.currentTimeMillis());
		editor.commit();
	}
	
	/**
	 * 设置最后一次从服务器上更新缓存的时间
	 * @param time
	 */
	public void setTimeOfUpperLimit(long time){
		editor.putLong(UPPERLIMIT, time).commit();
	}
	/**
	 * 获取最后一次更新缓存的时间
	 * @return
	 */
	public long getTimeOfUpperLimit(){
		return mSharedPreferences.getLong(UPPERLIMIT, 0L);
	}
	/**
	 * 是否有必要更新缓存（访客、相册、关注、捞网），默认请款下，每天更新一次
	 * @return
	 */
//	public boolean isCanUpdateUpperLimit(){
//		long time = getTimeOfUpperLimit()	;
//		if(time==0L)
//			return true;
//		
//		Calendar lastCal = Calendar.getInstance();
//  		lastCal.setTime(new Date(time));
//  		//获得当前时间的
//  		Calendar currentCal = Calendar.getInstance();
//  		if(lastCal.get(Calendar.YEAR)>currentCal.get(Calendar.YEAR))
//  			return false;
//  		else if(lastCal.get(Calendar.YEAR)==currentCal.get(Calendar.YEAR)){
//  			if(lastCal.get(Calendar.MONTH)>currentCal.get(Calendar.MONTH))
//  				return false;
//  			else if (lastCal.get(Calendar.MONTH)==currentCal.get(Calendar.MONTH)) {
//				if(lastCal.get(Calendar.DATE)<currentCal.get(Calendar.DATE))
//					return true;
//				else
//					return false;
//			}else
//				return true;
//  		}else{
//  			return true;
//  		}
//	}
	/**
	 * 新消息记录
	 * @param isFlag 如果isFlag的值是true，表示有新消息
	 */
	public void setMsgRecord(boolean isFlag) {
		// TODO Auto-generated method stub
		editor.putBoolean(NEWMSG, isFlag).commit();
	}
	
	public boolean getMsgRecord(){
		return mSharedPreferences.getBoolean(NEWMSG, false);
	}
	
}
