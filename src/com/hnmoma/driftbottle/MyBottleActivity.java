package com.hnmoma.driftbottle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.ControlScrollViewPager;
import com.hnmoma.driftbottle.fragment.ChatAllHistoryFragment;
import com.hnmoma.driftbottle.fragment.MyBottlePageItem;
import com.hnmoma.driftbottle.util.MyConstants;
import com.viewpagerindicator.TabPageIndicator;
/**
 * 消息页面
 * @author Administrator
 *
 */
public class MyBottleActivity extends BaseActivity implements OnClickListener {

	TabPageIndicatorAdapter adapter;
	public ControlScrollViewPager pager;
	public TextView tv_title;
	private NewMessageBroadcastReceiver msgReceiver;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_mybottle);
		
		tv_title = (TextView) findViewById(R.id.tv_title);
		// ViewPager的adapter
		adapter = new TabPageIndicatorAdapter(getSupportFragmentManager());
		pager = (ControlScrollViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);

		// 实例化TabPageIndicator然后设置ViewPager与之关联
		TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(pager);
//		indicator.setOnPageChangeListener(new OnPageChangeListener() {
//
//			@Override
//			public void onPageSelected(int position) {
//				if (position == 0) {
//					bottle_friend.setSelected(true);
//					bottle_collect.setSelected(false);
//				} else if (position == 1) {
//					bottle_friend.setSelected(false);
//					bottle_collect.setSelected(true);
//				}
//			}
//
//			@Override
//			public void onPageScrolled(int position, float positionOffset,
//					int positionOffsetPixels) {
//			}
//
//			@Override
//			public void onPageScrollStateChanged(int state) {
//			}
//		});

		msgReceiver = new NewMessageBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(EMChatManager
				.getInstance().getNewMessageBroadcastAction());
		intentFilter.setPriority(4);
		registerReceiver(msgReceiver, intentFilter);

		// 注册一个cmd消息的BroadcastReceiver
		IntentFilter cmdIntentFilter = new IntentFilter(EMChatManager.getInstance().getCmdMessageBroadcastAction());
		intentFilter.setPriority(4);
		registerReceiver(cmdMessageReceiver, cmdIntentFilter);
	}

	/**
	 * cmd消息BroadcastReceiver
	 */
	private BroadcastReceiver cmdMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 获取cmd message对象
			//String msgId = intent.getStringExtra("msgid");
			EMMessage message = intent.getParcelableExtra("message");
			// 获取消息body
			CmdMessageBody cmdMsgBody = (CmdMessageBody) message.getBody();
			String aciton = cmdMsgBody.action;// 获取自定义action
			if (aciton.equals(MyConstants.MESSAGE_ACTION_DELBOTTLE)) {
				// 全局的已经处理数据，这里只需要刷新
				adapter.updateItem();
			}
		}
	};

	class TabPageIndicatorAdapter extends FragmentPagerAdapter {

		String[] titles = new String[] { "消息" };
		Fragment fragment[] = new Fragment[1];

		public TabPageIndicatorAdapter(FragmentManager fm) {
			super(fm);
		}

		public void updateItem() {
			if (fragment[0] != null) {
				ChatAllHistoryFragment mbpi = (ChatAllHistoryFragment) fragment[0];
				mbpi.refresh();
			}
		}

		@Override
		public Fragment getItem(int position) {
//			if (position == 0) {
				ChatAllHistoryFragment mbpi = new ChatAllHistoryFragment();
				fragment[position] = mbpi;
//			} else {
//				fragment[position] = MyBottlePageItem.newInstance(position);
//			}
//			bottle_friend.setSelected(true);
//			bottle_collect.setSelected(false);
			return fragment[position];
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}

		@Override
		public int getCount() {
			return titles.length;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.my_bottle_back:
			Intent intent = new Intent(MyBottleActivity.this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			intent.putExtra("flag", 0);
			startActivity(intent);
			MyBottlePageItem.showMng[0] = false;
			MyBottlePageItem.showMng[1] = false;
			
			this.finish();
			break;
		case R.id.my_bottle_mng:
			// TODO 我的瓶子 统一删除
//			int position = pager.getCurrentItem();
//			if(position == 1) {
//				MyBottlePageItem fragment =(MyBottlePageItem) getSupportFragmentManager().findFragmentByTag( 
//						"android:switcher:" + R.id.pager + ":" + position);
//				fragment.showMng( -1);
//			} else {
//				ChatAllHistoryFragment fragment =(ChatAllHistoryFragment) getSupportFragmentManager().findFragmentByTag( 
//						"android:switcher:" + R.id.pager + ":" + position);
//				fragment.showMng(-1);
//			}
			//进入关注
			if(UserManager.getInstance(this).getCurrentUser() == null) {
				intent = new Intent(this, LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivityForResult(intent, 700);
			} else {
				Intent careAbout = new Intent(MyBottleActivity.this, ConcernActivity.class);
				careAbout.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				careAbout.putExtra("flag", 0);
				startActivity(careAbout);
			}
			break;
//		case R.id.btn_mybottle_friend:
//			// indicator.setCurrentItem(0);
//			pager.setCurrentItem(0);
//			break;
//		case R.id.btn_mybottle_collect:
//			// indicator.setCurrentItem(1);
//			pager.setCurrentItem(1);
//			break;
		}
	}

	@Override
	public void onBackPressed() {
		// 从通知栏来的消息
		Intent intent = new Intent(MyBottleActivity.this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		intent.putExtra("flag", 0);
		startActivity(intent);

		MyBottlePageItem.showMng[0] = false;
		MyBottlePageItem.showMng[1] = false;

		this.finish();
	}

	/**
	 * 新消息广播接收者
	 * 
	 * 
	 */
	private class NewMessageBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			adapter.updateItem();
			// 注销广播，否则在ChatActivity中会收到这个广播
			// abortBroadcast();
		}
	}

	// @Override
	// protected void onPause() {
	// super.onPause();
	// GexinSdkMsgReceiver.ehList.remove(this);
	// }
	//
	// @Override
	// protected void onResume() {
	// super.onResume();
	// GexinSdkMsgReceiver.ehList.add(this);
	// }
	//
	// @Override
	// public boolean supportEventType(String type) {
	// if(type.equals(GexinSdkMsgReceiver.EVENT_CHAT)){
	// return true;
	// }
	// return false;
	// }
	//
	// @Override
	// public void onMessage(BmobMsg message) {
	// if(message.getType().equals(GexinSdkMsgReceiver.EVENT_OUTLINEMSG)){
	// boolean isAllow =
	// MyApplication.getApp().getSysSpUtil().isNtcSoundOpend();
	// if(isAllow){
	// if(MyApplication.getApp().getMediaPlayer()!=null){
	// MyApplication.getApp().getMediaPlayer().start();
	// }
	// }
	// adapter.updateItem();
	// }else if(message.getType().equals(GexinSdkMsgReceiver.EVENT_CHAT)){
	// //保存接收到的消息-并发送已读回执给对方
	// boolean saved = ChatManager.getInstance(this).saveReceiveMessage(false,
	// message);
	// if(saved){
	// // 声音提示
	// boolean isAllow =
	// MyApplication.getApp().getSysSpUtil().isNtcSoundOpend();
	// if(isAllow){
	// if(MyApplication.getApp().getMediaPlayer()!=null){
	// if(MyApplication.getApp().getMediaPlayer()!=null){
	// MyApplication.getApp().getMediaPlayer().start();
	// }
	// }
	// }
	// adapter.updateItem();
	// }
	// }
	// }
	//
	// @Override
	// public void onReaded(String paramString1, String paramString2) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onNetChange(boolean paramBoolean) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onOffline() {
	// // TODO Auto-generated method stub
	//
	// }

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 注销广播接收者
		try {
			unregisterReceiver(msgReceiver);
			msgReceiver = null;
		} catch (Exception e) {
		}
		try {
			unregisterReceiver(cmdMessageReceiver);
			cmdMessageReceiver = null;
		} catch (Exception e) {
		}
	}
}