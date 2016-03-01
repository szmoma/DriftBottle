//package com.hnmoma.driftbottle;
//
//import android.app.NotificationManager;
//import android.os.Handler;
//
//import com.hnmoma.driftbottle.business.EventListener;
//import com.hnmoma.driftbottle.model.ChatMsgModel;
//
//public abstract class BaseWithNoticeActivity extends BaseActivity implements EventListener{
//	
////	/**
////	 * 广播接收者，接收GetMsgService发送过来的消息
////	 */
////	private BroadcastReceiver msgReceiver = new BroadcastReceiver() {
////		public void onReceive(Context context, Intent intent) {
////			ChatMsgModel cmm = (ChatMsgModel) intent.getSerializableExtra("ChatMsgModel");
////			dealMsg(cmm);// 把收到的消息传递给子类
////		}
////	};
//
////	/**
////	 * 抽象方法，用于子类处理消息，
////	 * 
////	 * @param msg
////	 *            传递给子类的消息对象
////	 */
////	public abstract void dealMsg(ChatMsgModel cmm);
//
////	@Override
////	public void onStart() {// 在start方法中注册广播接收者
////		super.onStart();
////		IntentFilter intentFilter = new IntentFilter();
////		intentFilter.addAction(MyConstants.MSG_BROADCAST_ACTION);
////		getApplicationContext().registerReceiver(msgReceiver, intentFilter);// 注册接受消息广播
////
////	}
////	
////	@Override
////	protected void onDestroy() {
////		super.onDestroy();
////		getApplicationContext().unregisterReceiver(msgReceiver);// 注销接受消息广播
////	}
//	
////	Handler myHandler = new Handler();
////	MyThread myThread = new MyThread();
////	NotificationManager manger;
////	
////	class MyThread implements Runnable{
////		public void run() {
////			if(manger==null){
////				manger = (NotificationManager)BaseWithNoticeActivity.this.getSystemService(NOTIFICATION_SERVICE);
////			}
////			manger.cancel(0);
////		}
////	};
////	public void cancelNotice(){
////		myHandler.postDelayed(myThread, 1000);
////	}
//}
