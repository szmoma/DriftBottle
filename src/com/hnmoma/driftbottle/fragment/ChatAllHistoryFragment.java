package com.hnmoma.driftbottle.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import android.app.AlertDialog;
import android.app.ApplicationErrorReport.AnrInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.hnmoma.driftbottle.ChatActivity;
import com.hnmoma.driftbottle.GameMsgActivity;
import com.hnmoma.driftbottle.GiftActivity;
import com.hnmoma.driftbottle.GreetingActivity;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.MyBottleActivity;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.adapter.ChatAllHistoryAdapter;
import com.hnmoma.driftbottle.business.BottleMsgManager;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MyConstants;

/**
 * 显示所有会话记录，比较简单的实现，更好的可能是把陌生人存入本地，这样取到的聊天记录是可控的
 * 
 */
public class ChatAllHistoryFragment extends BaseFragment implements OnClickListener {
	private InputMethodManager inputMethodManager;
	private ListView listView;
	private ChatAllHistoryAdapter adapter;
	public RelativeLayout errorItem;
	public TextView errorText;
	private boolean hidden;
//	private List<EMGroup> groups;
	private LinearLayout ll_mbmg;
	Button btn_cancel;
	Button btn_del;
	
	List<EMConversation> conversations;
	List<String> delUsers;
	boolean isMulChoice = false;
	public HashMap<Integer, Boolean> ischeck;
	private View viewNoMsg;//没有消息时，显示的空间
	private TextView tvNoMsg;//没有消息时，显示的文字
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_conversation_history, container, false);
	}
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		errorItem = (RelativeLayout) getView().findViewById(R.id.rl_error_item);
		errorText = (TextView) errorItem.findViewById(R.id.tv_connect_errormsg);
		btn_cancel = (Button) getView().findViewById(R.id.btn_cancel);
		btn_del = (Button) getView().findViewById(R.id.btn_del);
		viewNoMsg= getView().findViewById(R.id.no_message);
		tvNoMsg = ((TextView)getView().findViewById(R.id.tv_no_message));
		tvNoMsg.setText(getResources().getString(R.string.tip_no_message));
		btn_cancel.setOnClickListener(this);
		btn_del.setOnClickListener(this);
		conversations = loadConversationsWithRecentChat();
		
		ischeck = new HashMap<Integer, Boolean>();
		for (int i = 0; i < conversations.size(); i++) {
			ischeck.put(i, false);
		}
		// contact list
		listView = (ListView) getView().findViewById(R.id.list);
		adapter = new ChatAllHistoryAdapter(getActivity(),listView, 1, conversations, ischeck);
		
		ll_mbmg = (LinearLayout) getView().findViewById(R.id.ll_mbmg);
//		groups = EMGroupManager.getInstance().getAllGroups();
		delUsers = new ArrayList<String>();
		// 设置adapter
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(isMulChoice) {
					changeDelUser(position);
					return;
				}
				EMConversation conversation = adapter.getItem(position);
				String username = conversation.getUserName();
				if(username.equals("99999999")){// 查看收到的礼物
					Intent intent = new Intent(getActivity(), GiftActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					String str = UserManager.getInstance(getActivity()).getCurrentUserId();
					intent.putExtra("userId", str);
					intent.putExtra("visitUserId", str);
					startActivity(intent);
					
					// 把此会话的未读数置为0
					conversation.resetUnsetMsgCount();
				}else if(username.equals("99999912")){//游戏消息
					Intent intent = new Intent(getActivity(), GameMsgActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					String str = UserManager.getInstance(getActivity()).getCurrentUserId();
					intent.putExtra("userId", str);
					intent.putExtra("visitUserId", str);
					startActivity(intent);
					
					// 把此会话的未读数置为0
					conversation.resetUnsetMsgCount();
				} else if(username.equals("99999923")) {
					//TODO  点击进入打招呼列表
					Intent HelloIntent = new Intent(getActivity(), GreetingActivity.class);
					startActivity(HelloIntent);
					
				} else{
					if (username.equals(UserManager.getInstance(getActivity()).getCurrentUserId())){
						showMsg("不能和自己聊天");
					}else {
						// 进入聊天页面
						Intent intent = new Intent(getActivity(), ChatActivity.class);
						intent.putExtra("userId", username);
						startActivity(intent);
					}
				}
			}
		});
		// 注册上下文菜单
		//registerForContextMenu(listView);

		listView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// 隐藏软键盘
				if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
					if (getActivity().getCurrentFocus() != null)
						inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
				}
				return false;
			}
		});
		
		// 长按删除
		listView.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id) {
				if(adapter!=null && adapter.getCount()!=0){
					showMng(position);
				}
				return true;
			}
			
		});
		
//		// 搜索框
//		query = (EditText) getView().findViewById(R.id.query);
//		// 搜索框中清除button
//		clearSearch = (ImageButton) getView().findViewById(R.id.search_clear);
//		query.addTextChangedListener(new TextWatcher() {
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//				adapter.getFilter().filter(s);
//				if (s.length() > 0) {
//					clearSearch.setVisibility(View.VISIBLE);
//				} else {
//					clearSearch.setVisibility(View.INVISIBLE);
//				}
//			}
//
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			}
//
//			public void afterTextChanged(Editable s) {
//			}
//		});
//		clearSearch.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				query.getText().clear();
//
//			}
//		});

	}
	
	public void showMng(int position){
		//ViewPager不可滚动
		MyBottleActivity ma = (MyBottleActivity) act;
		ma.pager.setScrollable(false);
//		ma.bottle_collect.setClickable(false);
//		ma.bottle_friend.setClickable(false);
		ll_mbmg.setVisibility(View.VISIBLE);
		isMulChoice = true;
		adapter.setDelMode(isMulChoice);
		changeDelUser(position);
	}
	// 增删用户
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
		adapter.notifyDataSetChanged();
		
	}
	// cancel delete 
	public void cancelMng(){
		//ViewPager不可滚动
		MyBottleActivity ma = (MyBottleActivity) act;
		ma.pager.setScrollable(true);
//		ma.bottle_friend.setClickable(true);
//		ma.bottle_collect.setClickable(true);
		ll_mbmg.setVisibility(View.GONE);
		isMulChoice = false;
		adapter.setDelMode(isMulChoice);
		delUsers.clear();
		for (int i = 0; i < conversations.size(); i++) {
			ischeck.put(i, false);
		}
		adapter.notifyDataSetChanged();
	}
	
	// show dialog for  confirm
	private void showDelDialog() {
		new AlertDialog.Builder(act)
			.setTitle("温馨提示")
			.setMessage("消息删除后将不会收到后续的回应")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() { 
                    public void onClick(DialogInterface dialog, int which) { 
                    	
            			if(delUsers != null  && delUsers.size() != 0) {
            				for(String name : delUsers) {
            					if("99999923".equals(name)) {
            						for(EMConversation con : HelloList) {
            							EMChatManager.getInstance().deleteConversation(con.getUserName());
            						}
            						//
            						HelloList.clear();
            						conversations.remove(HelloCon);
            					}  else {
            						EMConversation tobeDeleteCons = EMChatManager.getInstance().getConversation(name);
    	            				BottleMsgManager.getInstance(getActivity()).delStrangerById(name);//TODO ?
    	            				//删除了瓶子，告诉对方瓶子已经删除
    	            				int noticeType = tobeDeleteCons.getLastMessage().getIntAttribute(MyConstants.MESSAGE_ATTR_NOTICETYPE, 0);
    	            				if(noticeType != 2){
    	            					delUserBottle(name);
    	            				}
    	            				// 删除此会话
    	            				EMChatManager.getInstance().deleteConversation(name);
    	            				conversations.remove(tobeDeleteCons);
    	            				MyApplication.getApp().getSpUtil().setBackedByUserId(name, false);
    	            				
    	            				
    	            				
    	            				}
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
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.delete_message) {
			EMConversation tobeDeleteCons = adapter.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
			BottleMsgManager.getInstance(getActivity()).delStrangerById(tobeDeleteCons.getUserName());
			//删除了瓶子，告诉对方瓶子已经删除
			
			int noticeType = tobeDeleteCons.getLastMessage().getIntAttribute(MyConstants.MESSAGE_ATTR_NOTICETYPE, 0);
			if(noticeType != 2){
				delUserBottle(tobeDeleteCons.getUserName());
			}
			
			// 删除此会话
			EMChatManager.getInstance().deleteConversation(tobeDeleteCons.getUserName(),tobeDeleteCons.isGroup());
			
//			InviteMessgeDao inviteMessgeDao = new InviteMessgeDao(getActivity());
//			inviteMessgeDao.deleteMessage(tobeDeleteCons.getUserName());
			adapter.remove(tobeDeleteCons);
			adapter.notifyDataSetChanged();

			// 更新消息未读数
//			((MainActivity) getActivity()).updateUnreadLabel();
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	/**
	 * 告诉对方删除瓶子
	 */
	private void delUserBottle(String userName){
		EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
		CmdMessageBody cmdBody = new CmdMessageBody(MyConstants.MESSAGE_ACTION_DELBOTTLE);
		cmdMsg.addBody(cmdBody); 
		cmdMsg.setReceipt(userName);
//		cmdMsg.setAttribute("a", "a");//支持自定义扩展
		EMChatManager.getInstance().sendMessage(cmdMsg, new EMCallBack(){
			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProgress(int arg0, String arg1) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
			}});
	}
	

	/**
	 * 刷新页面
	 */
	public void refresh() {
		conversations.clear();
		conversations.addAll(loadConversationsWithRecentChat());
		
		conversations = loadConversationsWithRecentChat();
		for (int i = 0; i < conversations.size(); i++) {
			ischeck.put(i, false);
		}
		if(delUsers != null && delUsers.size() > 0) {
			for(String name : delUsers) {
				EMConversation selecCon = EMChatManager.getInstance().getConversation(name);
				ischeck.put(conversations.indexOf(selecCon), true); 
			}
		}
		if(adapter == null){
			adapter = new ChatAllHistoryAdapter(getActivity(),listView, R.layout.row_chat_history, conversations, ischeck);
			adapter.setDelMode(isMulChoice);
			listView.setAdapter(adapter);
		}
		adapter.notifyDataSetChanged();
		
	}

	/**
	 * 获取所有会话
	 * @param context
	 * @return
	 */
	List<EMConversation> HelloList;
	List<EMConversation> DelList;
	EMConversation HelloCon; //打招呼列表
	private List<EMConversation> loadConversationsWithRecentChat() {
		List<EMConversation> tempconversations = new ArrayList<EMConversation>();
		if(EMChatManager.getInstance().isConnected()){ //连接到环信服务器
			if(!EMChat.getInstance().isLoggedIn()){	//账号是否登录了
				return tempconversations;
			}
		}else{
			return tempconversations;
		}
		if(UserManager.getInstance(getActivity()).getCurrentUser()==null){
			return tempconversations;
		}
		showDialog("努力加载...", "加载失败...", 20*1000);
		try {
			// 从本地数据库加载聊天记录到内存的操作
			EMChatManager.getInstance().loadAllConversations();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MoMaLog.e("debug", "从本地数据库加载聊天记录到内存的操作:失败，用户没有登陆");
			return tempconversations;
		}catch (Exception e) {
			MoMaLog.e("debug", "从本地数据库加载聊天记录到内存的操作:失败，用户没有登陆");
			return tempconversations;
		}
		//获取内存中所有会话,调用getAllConversations()方法之前，必须调用loadAllConversations();
		Hashtable<String, EMConversation> conversationTable = EMChatManager.getInstance().getAllConversations();
		
		HelloList = new ArrayList<EMConversation>();
		DelList = new ArrayList<EMConversation>();
		
		//过滤掉messages seize为0的conversation
		for(EMConversation conversation : conversationTable.values()){
			if(conversation.getAllMessages().size() != 0) {
				//如果最后一条记录是打招呼
				if(conversation.getLastMessage().getBooleanAttribute(MyConstants.MESSAGE_ATTR_ISSAYHELLO, false) && conversation.getMsgCount() == 1) {
					if(conversation.getLastMessage().getFrom().equals(UserManager.getInstance(getActivity()).getCurrentUserId())) {
						DelList.add(conversation);
					} else {
						HelloList.add(conversation);// 收到的招呼列表
					}
				} else {
					List<EMMessage> msgs = conversation.getAllMessages();
					int getHellototal = 0;
					int sayHelloTotal = 0;
					for(EMMessage msg : msgs) {// 优化
						if(msg.getBooleanAttribute(MyConstants.MESSAGE_ATTR_ISSAYHELLO, false)) {
							if(msg.getTo().equals(UserManager.getInstance(act).getCurrentUserId())) {
								getHellototal += 1;
							} else {
								sayHelloTotal += 1;
							}
						} else {
							break;
						}
					}
					if(getHellototal == msgs.size()) {
						HelloList.add(conversation);
					} else if(sayHelloTotal == msgs.size()){
						DelList.add(conversation);
					} else {
						tempconversations.add(conversation);
					}
				}
			}
		}
		
		sortConversationByLastChatTime(HelloList);
		if(HelloList.size() > 0) {
			long time = HelloList.get(0).getLastMessage().getMsgTime();
			EMMessage EMMsg  = EMMessage.createSendMessage(EMMessage.Type.TXT);
			EMMsg.setAttribute(MyConstants.MESSAGE_ATTR_LISTTYPE, "1");
			EMMsg.setAttribute(MyConstants.MESSAGE_ATTR_NOTICETYPE, 2);// 设置不给对方发送删除消息
			EMMsg.setAttribute(MyConstants.HELLOMSG_ATTR_COUNT, getUnreadCount(HelloList));
			TextMessageBody txtBody = new TextMessageBody("有人给你打招呼了~~~");
			EMMsg.addBody(txtBody);
			EMMsg.setReceipt("99999923");
			EMMsg.setMsgTime(time);
			EMMsg.setUnread(false);
			HelloCon = new EMConversation("99999923");
			HelloCon.addMessage(EMMsg);
			tempconversations.add(HelloCon);
		}
		// 删除自己打的招呼
		for(int i=0; i< DelList.size(); i++) {
			EMChatManager.getInstance().deleteConversation(DelList.get(i).getUserName());
		}
		DelList.clear();
		sortConversationByLastChatTime(tempconversations);
		closeDialog(mpDialog);
		return tempconversations;
	}

	public  int getUnreadCount(List<EMConversation> conversations) {
		int total = 0;
		for(EMConversation con : conversations) {
			total += con.getUnreadMsgCount();
		}
		return total;
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
				if (con2LastMessage.getMsgTime() == con1LastMessage.getMsgTime()) {
					return 0;
				} else if (con2LastMessage.getMsgTime() > con1LastMessage.getMsgTime()) {
					return 1;
				} else {
					return -1;
				}
			}

		});
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if (!hidden) {
			refresh();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!hidden&&isVisible()) {
			refresh();
		}
	}

	@Override
	public boolean onBackPressed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_cancel:
			cancelMng();
			break;
		case R.id.btn_del:
			showDelDialog();
			break;
		}
	}
	
	

}
