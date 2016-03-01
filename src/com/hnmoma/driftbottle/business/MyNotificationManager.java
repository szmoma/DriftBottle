package com.hnmoma.driftbottle.business;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.hnmoma.driftbottle.GiftActivity;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.MyBottleActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.RechargeActivity;

public class MyNotificationManager {

	Context globalContext;
	private static volatile MyNotificationManager INSTANCE;
	private static Object INSTANCE_LOCK = new Object();
	
	public static MyNotificationManager getInstance(Context paramContext){
	    if (INSTANCE == null)
	      synchronized (INSTANCE_LOCK){
	    	  INSTANCE = new MyNotificationManager();
	    	  INSTANCE.init(paramContext);
	      }
	    return INSTANCE;
	  }

	  private void init(Context paramContext) {
	    this.globalContext = paramContext;
	  }
	  
	  static long startTime;
		/**
		 * 消息通知可能有多种情况
		 * 1.打开某个activity
		 * 2.打开某个网页
		 * 
		 * 如果修改请记得还有登陆LoginActivity
		 * 
		 * msg内容提示
		 * flag 哪个模块的通知，1我的瓶子，2充值，3送礼通知, 4打招呼
		 *
		 */
		public void newMsgNotice(String msg, int flag){
			
			long now = System.currentTimeMillis();
			if(now - startTime < 2000){
				startTime = now;
				return;
			}
			
			//获取到系统的notificationManager  
			NotificationManager notificationManager =  MyApplication.getApp().getNotificationManager();
			Notification notification = new Notification(R.drawable.ic_launcher, "漂流瓶子新消息", now);  
			
//			notification.defaults = Notification.DEFAULT_ALL;
			notification.flags |= Notification.FLAG_AUTO_CANCEL; 
			
			//如果设置开启铃声则用系统设置，关闭者只闪灯
			boolean ntcSoundOpend = MyApplication.getApp().getSysSpUtil().isNtcSoundOpend();
			if(ntcSoundOpend){
				notification.defaults = Notification.DEFAULT_ALL;
			}else{
				//LED 灯闪烁 
				notification.defaults = Notification.DEFAULT_LIGHTS;
				notification.ledARGB = 0xff00ff00;
				notification.ledOnMS = 300;
				notification.ledOffMS = 1000;
				notification.flags |= Notification.FLAG_SHOW_LIGHTS;
				
//				notification.sound = null;  
//				notification.defaults |= Notification.DEFAULT_VIBRATE;
			}
			
			switch(flag){
				case 0:
					 //设置用户点击notification的动作   
			         // pendingIntent 延期的意图   
			         Intent it = new Intent(globalContext, MyBottleActivity.class); 
			         it.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			         PendingIntent pendingIntent  = PendingIntent.getActivity(globalContext, 0, it, 0);  
			         notification.contentIntent = pendingIntent;  
					break;
				case 1:
					 //充值成功后跳转到充值中心
			         it = new Intent(globalContext, RechargeActivity.class); 
			         it.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			         pendingIntent  = PendingIntent.getActivity(globalContext, 0, it, 0);  
			         notification.contentIntent = pendingIntent;  
					break;
				case 2:
					 //gift
			         it = new Intent(globalContext, GiftActivity.class); 
			         it.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			         it.putExtra("userId", UserManager.getInstance(globalContext).getCurrentUserId());
			         it.putExtra("visitUserId", UserManager.getInstance(globalContext).getCurrentUserId());
			         //来自于推送
			         it.putExtra("comeFromTS", 1);
			         pendingIntent  = PendingIntent.getActivity(globalContext, 0, it, 0);  
			         notification.contentIntent = pendingIntent;  
					break;
				case 3:
					 //系统消息
			         it = new Intent(globalContext, MyBottleActivity.class); 
			         it.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			         pendingIntent  = PendingIntent.getActivity(globalContext, 0, it, 0);  
			         notification.contentIntent = pendingIntent;  
					break;
			}
	          
	         //自定义界面   
	         RemoteViews rv = new RemoteViews(globalContext.getPackageName(), R.layout.new_msg_notification);  
	         rv.setTextViewText(R.id.notification_title, "漂流瓶子");  
	         rv.setTextViewText(R.id.notification_name, msg);  
	         rv.setImageViewResource(R.id.notification_icon, R.drawable.ic_launcher);
	         notification.contentView = rv;  
	           
	         //把定义的notification 传递给 notificationmanager   
	         notificationManager.notify(0, notification);  
	         
	         startTime = System.currentTimeMillis();
		}
		
		public void cancleMsgNotice(){
			MyApplication.getApp().getNotificationManager().cancel(0);
		}
}
