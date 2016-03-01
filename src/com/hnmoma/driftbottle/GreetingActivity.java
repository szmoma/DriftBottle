package com.hnmoma.driftbottle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.hnmoma.driftbottle.adapter.AsyncStrangerLoader;
import com.hnmoma.driftbottle.adapter.GreetingAdapter;
import com.hnmoma.driftbottle.adapter.AsyncStrangerLoader.StrangerCallback;
import com.hnmoma.driftbottle.business.BottleMsgManager;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.model.Stranger;
import com.hnmoma.driftbottle.util.MyConstants;
/**
 * 招呼列表
 *
 */
public class GreetingActivity extends BaseActivity {
	private final int FRESH = 1;
	
	List<EMConversation> conversations;
	List<String> delUsers;
	boolean isMulChoice = false;
	public HashMap<Integer, Boolean> ischeck;
	
	ListView listview;
	GreetingAdapter adapter;
	private LinearLayout ll_mbmg;
	Button btn_cancel;
	Button btn_del;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FRESH:
				adapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_greeting);
		
		loadConversationsWithRecentChat();
		ischeck = new HashMap<Integer, Boolean>();
		delUsers = new ArrayList<String>();
		for (int i = 0; i < conversations.size(); i++) {
			ischeck.put(i, false);
		}
		ll_mbmg = (LinearLayout) findViewById(R.id.ll_mbmg);
		
		listview = (ListView) findViewById(R.id.list);
		adapter = new GreetingAdapter(this, listview, conversations, ischeck);
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(isMulChoice) {
					changeDelUser(position);
					return;
				}
				EMConversation conversation = adapter.getItem(position);
				AsyncStrangerLoader asyncStrangerLoader = new AsyncStrangerLoader(GreetingActivity.this);
				
				Stranger stranger = asyncStrangerLoader.loadStranger(conversation.getUserName(),new StrangerCallback() {
					
					@Override
					public void strangerLoaded(Stranger stranger) {
						// TODO Auto-generated method stub
						if(stranger.getState()!=1){
							stranger.setState(1);
							BottleMsgManager.getInstance(GreetingActivity.this).insertOrReplaceStranger(stranger);
						}
					}
				});
				if(stranger!=null){
					if(stranger.getState()!=1){
						stranger.setState(1);
						BottleMsgManager.getInstance(GreetingActivity.this).insertOrReplaceStranger(stranger);
					}
				}
//				conversation.resetUnreadMsgCount();
				Intent intent = new Intent(GreetingActivity.this, ChatActivity.class);
				intent.putExtra("userId", conversation.getUserName());
				startActivity(intent);
				
				conversation.resetUnreadMsgCount();
				
			}
			
		});
		
		// 注册一个cmd消息的BroadcastReceiver
//		IntentFilter cmdIntentFilter = new IntentFilter(EMChatManager.getInstance().getCmdMessageBroadcastAction());
//		cmdIntentFilter.setPriority(6);
//		registerReceiver(cmdMessageReceiver, cmdIntentFilter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Message msg = Message.obtain();
		msg.what = FRESH;
		mHandler.sendMessage(msg);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		unregisterReceiver(cmdMessageReceiver);
//		cmdMessageReceiver = null;
	}
	
	
	// load conversation
	private void loadConversationsWithRecentChat() {
		// 获取所有会话，包括陌生人
		Hashtable<String, EMConversation> conversationTable = EMChatManager.getInstance().getAllConversations();
		conversations = new ArrayList<EMConversation>();
				
		//过滤掉messages seize为0的conversation
		for(EMConversation conversation : conversationTable.values()){
			if(conversation.getAllMessages().size() != 0) {
				if(conversation.getLastMessage().getBooleanAttribute(MyConstants.MESSAGE_ATTR_ISSAYHELLO, false) && conversation.getMsgCount() == 1) {
					if(conversation.getLastMessage().getFrom().equals(UserManager.getInstance(this).getCurrentUserId())) {
//						EMChatManager.getInstance().deleteConversation(conversation.getUserName());// 删除自己打出的招呼
					} else {
						conversations.add(conversation);// 收到的招呼列表
					}
				} else {
					//conversation contains multiple messages or conversation has no attribute 
					List<EMMessage> msgs = conversation.getAllMessages();
					int getHellototal = 0;
					for(EMMessage msg : msgs) {
						if(msg.getBooleanAttribute(MyConstants.MESSAGE_ATTR_ISSAYHELLO, false)) {
							if(msg.getTo().equals(UserManager.getInstance(this).getCurrentUserId())) {
								getHellototal += 1;
							}
						}
					}
					if(getHellototal == msgs.size()) {
						conversations.add(conversation);
					} 
				}
			}
		}
		
		sortConversationByLastChatTime(conversations);
	}

	/**
	 * 根据最后一条消息的时间排序
	 * 
	 * @param usernames
	 */
	private void sortConversationByLastChatTime(List<EMConversation> conversationList) {
		Collections.sort(conversationList, new Comparator<EMConversation>() {
			@Override
			public int compare(final EMConversation con1, final EMConversation con2) {

				EMMessage con2LastMessage = con2.getLastMessage();
				EMMessage con1LastMessage = con1.getLastMessage();
				if (con2LastMessage!=null&&con1LastMessage!=null&&con2LastMessage.getMsgTime() == con1LastMessage.getMsgTime()) {
					return 0;
				} else if (con2LastMessage!=null&&con1LastMessage!=null&&con2LastMessage.getMsgTime() > con1LastMessage.getMsgTime()) {
					return 1;
				} else {
					return -1;
				}
			}

		});
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_cancel:
			cancelMng();
			break;
		case R.id.btn_del:
			showDelDialog();
			break;
		case R.id.hellolist_back:
			this.finish();
			break;
		case R.id.hellolist_delete:
			showMng(-1);
			break;
		}
	}
	// show delete pattern
	public void showMng(int position){
//		ma.bottle_friend.setClickable(false);
		ll_mbmg.setVisibility(View.VISIBLE);
		isMulChoice = true;
		adapter.setDelMode(isMulChoice);
		changeDelUser(position);
	}
	
	// delete conversation
	public void changeDelUser(int position){
		if(position < 0) {
			return;
		}
		String name = adapter.getItem(position).getUserName();
		if(delUsers.contains(name)) {
			ischeck.put(position, false);
			delUsers.remove(name);
		} else {
			ischeck.put(position, true);
			delUsers.add(name);
		}
		Message msg = Message.obtain();
		msg.what = FRESH;
		mHandler.sendMessage(msg);
	}
	
	// cancel delete 
	public void cancelMng(){
		//ViewPager不可滚动
		ll_mbmg.setVisibility(View.GONE);
		isMulChoice = false;
		adapter.setDelMode(isMulChoice);
		delUsers.clear();
		for (int i = 0; i < conversations.size(); i++) {
			ischeck.put(i, false);
		}
		Message msg = Message.obtain();
		msg.what = FRESH;
		mHandler.sendMessage(msg);
	}
	
	// show dialog for delete  confirm 
		private void showDelDialog() {
			new AlertDialog.Builder(this)
				.setTitle("温馨提示")
				.setMessage("删除后将不会出现在该列表")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() { 
	                    public void onClick(DialogInterface dialog, int which) { 
	                    	
	            			if(delUsers != null  && delUsers.size() != 0) {
	            				for(String name : delUsers) {
	            				
		            				EMConversation tobeDeleteCons = EMChatManager.getInstance().getConversation(name);
		            				BottleMsgManager.getInstance(GreetingActivity.this).delStrangerById(name);//
		            				// 删除此会话
		            				EMChatManager.getInstance().deleteConversation(name);
		            				conversations.remove(tobeDeleteCons);
		            				MyApplication.getApp().getSpUtil().setBackedByUserId(name, false);
		            				}
	            			}
	            			
	        				cancelMng();
	                    } 
	                })
	             .setNegativeButton("取消", new DialogInterface.OnClickListener() { 
	                    public void onClick(DialogInterface dialog, int which) { 
	                    } 
	             })
				.show();
		}
		
//		private BroadcastReceiver cmdMessageReceiver = new BroadcastReceiver() {
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				//获取cmd message对象
////				String msgId = intent.getStringExtra("msgid");
//				EMMessage message = intent.getParcelableExtra("message");
//				//获取消息body
//				CmdMessageBody cmdMsgBody = (CmdMessageBody) message.getBody();
//				String action = cmdMsgBody.action;//获取自定义action
////				//获取扩展属性
//				if(MyConstants.MESSAGE_ATTR_ISSAYHELLO.equals(action)){
//					adapter.notifyDataSetInvalidated();
//					abortBroadcast();
//				}
//			}
//		};
	
}
