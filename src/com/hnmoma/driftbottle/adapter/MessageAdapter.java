/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hnmoma.driftbottle.adapter;
/**
 * 消息列表的adapter
 */
import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Type;
import com.easemob.chat.FileMessageBody;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.util.DateUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.ChatActivity;
import com.hnmoma.driftbottle.ContextMenu;
import com.hnmoma.driftbottle.Game_Cq_Activity;
import com.hnmoma.driftbottle.Game_Cq_Yz;
import com.hnmoma.driftbottle.GiftBoxActivity;
import com.hnmoma.driftbottle.GiftWallActivity;
import com.hnmoma.driftbottle.MyAlertDialog;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.ShowBigImage;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.ChatBottle;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.custom.NoticeMsg;
import com.hnmoma.driftbottle.itfc.VoicePlayClickListener;
import com.hnmoma.driftbottle.model.GameMsgModel;
import com.hnmoma.driftbottle.model.GameOpponentModel;
import com.hnmoma.driftbottle.model.QueryGameInfoModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.task.LoadImageTask;
import com.hnmoma.driftbottle.util.ImageCache;
import com.hnmoma.driftbottle.util.ImageUtils;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.hnmoma.driftbottle.util.SmileUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MessageAdapter extends BaseAdapter {
	//文本消息
	private static final int MESSAGE_TYPE_RECV_TXT = 0;
	private static final int MESSAGE_TYPE_SENT_TXT = 1;
	//位置
	private static final int MESSAGE_TYPE_SENT_LOCATION = 3;
	private static final int MESSAGE_TYPE_RECV_LOCATION = 4;
	//图片
	private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
	private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
	//语音
	private static final int MESSAGE_TYPE_SENT_VOICE = 6;
	private static final int MESSAGE_TYPE_RECV_VOICE = 7;
	//视频
	private static final int MESSAGE_TYPE_SENT_VIDEO = 8;
	private static final int MESSAGE_TYPE_RECV_VIDEO = 9;
	//文件
	private static final int MESSAGE_TYPE_SENT_FILE = 10;
	private static final int MESSAGE_TYPE_RECV_FILE = 11;
	//电话
	private static final int MESSAGE_TYPE_SENT_VOICE_CALL = 12;
	private static final int MESSAGE_TYPE_RECV_VOICE_CALL = 13;
	//瓶子消息类型
	private static final int MESSAGE_TYPE_BOTTLE = 14;
	//瓶子通知类型
	private static final int MESSAGE_TYPE_NOTICE = 15;

	public static final String IMAGE_DIR = "chat/image/";
//	public static final String VOICE_DIR = "chat/audio/";
//	public static final String VIDEO_DIR = "chat/video";

	private String username;
	private LayoutInflater inflater;
	private Activity activity;
	
	private static final int HANDLER_MESSAGE_REFRESH_LIST = 0;
	private static final int HANDLER_MESSAGE_SELECT_LAST = 1;
	private static final int HANDLER_MESSAGE_SEEK_TO = 2;
	
	// reference to conversation object in chatsdk
	private EMConversation conversation;
	EMMessage[] messages = null;
	
	private Activity context;

	private Map<String, Timer> timers = new Hashtable<String, Timer>();
	ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	
	private Toast toast;
	private String mStrangerHeadUrl;//好友头像地址

	public MessageAdapter(Activity context, String username, int chatType) {
		this.username = username;
		this.context = context;
		inflater = LayoutInflater.from(context);
		activity = (Activity) context;
		this.conversation = EMChatManager.getInstance().getConversation(username);
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)
		.showImageForEmptyUri(R.drawable.defalutimg)
		.showImageOnFail(R.drawable.defalutimg)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
//		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.bitmapConfig(Bitmap.Config.RGB_565)
//		.displayer(new RoundedBitmapDisplayer(3))
		.build();
	}

	/**
	 * 获取item数
	 */
	public int getCount() {
//		return conversation.getMsgCount();
		return messages==null?0:messages.length;
	}

	public EMMessage getItem(int position) {
//		return conversation.getMessage(position);
		if (messages != null && position < messages.length) {
			return messages[position];
		}
		return null;
	}
	
	Handler handler = new Handler() {
		private void refreshList() {
			// UI线程不能直接使用conversation.getAllMessages()
			// 否则在UI刷新过程中，如果收到新的消息，会导致并发问题
			messages = (EMMessage[]) conversation.getAllMessages().toArray(new EMMessage[conversation.getAllMessages().size()]);
			for (int i = 0; i < messages.length; i++) {
				// getMessage will set message as read status
				conversation.getMessage(i);
			}
			notifyDataSetChanged();
		}
		
		@Override
		public void handleMessage(android.os.Message message) {
			switch (message.what) {
			case HANDLER_MESSAGE_REFRESH_LIST:
				refreshList();
				break;
			case HANDLER_MESSAGE_SELECT_LAST:
				if (activity instanceof ChatActivity) {
					ListView listView = ((ChatActivity)activity).getListView();
					if (messages.length > 0) {
						listView.setSelection(messages.length - 1);
					}
				}
				break;
			case HANDLER_MESSAGE_SEEK_TO:
				int position = message.arg1;
				if (activity instanceof ChatActivity) {
					ListView listView = ((ChatActivity)activity).getListView();
					listView.setSelection(position);
				}
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 刷新页面
	 */
	public void refresh() {
		if (handler.hasMessages(HANDLER_MESSAGE_REFRESH_LIST)) {
			return;
		}
		android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST);
		handler.sendMessage(msg);
	}
	
	/**
	 * 刷新页面, 选择最后一个
	 */
	public void refreshSelectLast() {
		handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
		handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_SELECT_LAST));
	}
	
	/**
	 * 刷新页面, 选择Position
	 */
	public void refreshSeekTo(int position) {
		handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
		android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_SEEK_TO);
		msg.arg1 = position;
		handler.sendMessage(msg);
	}
	
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 获取item类型
	 */
	public int getItemViewType(int position) {
		EMMessage message = conversation.getMessage(position);
		if(message==null)
			return -1;
		if (message.getIntAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 0) == 2){
			return MESSAGE_TYPE_BOTTLE;
		}else if (message.getIntAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 0) == 1){
			return MESSAGE_TYPE_NOTICE;
		}else{
			if (message.getType() == EMMessage.Type.TXT) {
				if (!message.getBooleanAttribute(MyConstants.MESSAGE_ATTR_IS_VOICE_CALL, false))
					return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_TXT : MESSAGE_TYPE_SENT_TXT;
				return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE_CALL : MESSAGE_TYPE_SENT_VOICE_CALL;
			}
			if (message.getType() == EMMessage.Type.IMAGE) {
				return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_IMAGE : MESSAGE_TYPE_SENT_IMAGE;

			}
			if (message.getType() == EMMessage.Type.LOCATION) {
				return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_LOCATION : MESSAGE_TYPE_SENT_LOCATION;
			}
			if (message.getType() == EMMessage.Type.VOICE) {
				return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE : MESSAGE_TYPE_SENT_VOICE;
			}
			if (message.getType() == EMMessage.Type.VIDEO) {
				return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO : MESSAGE_TYPE_SENT_VIDEO;
			}
			if (message.getType() == EMMessage.Type.FILE) {
				return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_FILE : MESSAGE_TYPE_SENT_FILE;
			}

			return -1;// invalid
		}
	}

	public int getViewTypeCount() {
		return 16;
	}

	private View createViewByMessage(EMMessage message, int position) {
		switch (message.getType()) {
//		case LOCATION:
//			return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_location, null) : inflater.inflate(
//					R.layout.row_sent_location, null);
		case IMAGE:
			return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_picture, null) : inflater.inflate(
					R.layout.row_sent_picture, null);

		case VOICE:
			return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_voice, null) : inflater.inflate(
					R.layout.row_sent_voice, null);
		case VIDEO:
			return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_video, null) : inflater.inflate(
					R.layout.row_sent_video, null);
//		case FILE:
//				return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_file, null) : inflater.inflate(
//						R.layout.row_sent_file, null);
		default:
//			// 语音电话
//			if (message.getBooleanAttribute(MyConstants.MESSAGE_ATTR_IS_VOICE_CALL, false))
//				return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_voice_call, null) : inflater
//						.inflate(R.layout.row_sent_voice_call, null);
			return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_message, null) : inflater.inflate(
					R.layout.row_sent_message, null);
		}
	}

	
	public View getView(final int position, View convertView, ViewGroup parent) {
		final EMMessage message = getItem(position);
		if (message.getIntAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 0)==1){
			convertView = new ChatBottle(context, message);//聊天界面的瓶子
		}else if(message.getIntAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 0)==2){
			convertView = new NoticeMsg(context, message);// 通知信息
		}else{
			ChatType chatType = message.getChatType();
			final ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = createViewByMessage(message, position);	//获得视图
				if (message.getType() == EMMessage.Type.IMAGE) {
					holder.tvGiftName = (TextView) convertView.findViewById(R.id.tv_chat_gift_name);
					holder.tvGiftPrice = (TextView) convertView.findViewById(R.id.tv_chat_gift_price);
					holder.tvGiftReward = (TextView) convertView.findViewById(R.id.tv_chat_gift_reward);
					holder.ivGiftImage = (ImageView) convertView.findViewById(R.id.iv_chat_gift_pic);
					holder.tvGameStatus = (TextView) convertView.findViewById(R.id.tv_status);
					holder.ivGameImage = (ImageView) convertView.findViewById(R.id.iv_game);
					holder.iv = ((ImageButton) convertView.findViewById(R.id.iv_sendPicture));
					holder.head_iv = (CircularImage) convertView.findViewById(R.id.iv_userhead);
					holder.tv = (TextView) convertView.findViewById(R.id.percentage);
					holder.pb = (ProgressBar) convertView.findViewById(R.id.progressBar);
					holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
					holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
				} else if (message.getType() == EMMessage.Type.TXT) {
					try {
						holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
						holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
						holder.head_iv = (CircularImage) convertView.findViewById(R.id.iv_userhead);
						// 这里是文字内容
						holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
						holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
					} catch (Exception e) {
					}

//					// 语音通话
//					if (message.getBooleanAttribute(MyConstants.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
//						holder.iv = (ImageView) convertView.findViewById(R.id.iv_call_icon);
//						holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
//					}

				} else if (message.getType() == EMMessage.Type.VOICE) {
					try {
						holder.iv = ((ImageButton) convertView.findViewById(R.id.iv_voice));
						holder.head_iv = (CircularImage) convertView.findViewById(R.id.iv_userhead);
						holder.tv = (TextView) convertView.findViewById(R.id.tv_length);
						holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
						holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
						holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
						holder.iv_read_status = (ImageView) convertView.findViewById(R.id.iv_unread_voice);
					} catch (Exception e) {
					}
				} 
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			//独立存储的头像和昵称
			String headImg = message.getStringAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, "");
			if(message.direct == EMMessage.Direct.SEND){
				//自己的头像:未审核时候，用未审核的头像。一般情况下，从消息中获取到的头像是审核后的头像
				if(TextUtils.isEmpty(headImg)){
					String tmp = UserManager.getInstance(activity).getCurrentUser().getHeadImg();//审核后的头像
					String tmp2 = UserManager.getInstance(activity).getCurrentUser().getTempHeadImg();//未审核是的头像
					//获取审核后的头像
					headImg = tmp==null?tmp2:tmp;
				}else{
					if( UserManager.getInstance(activity).getCurrentUser()!=null){
						//如果协议中的头像地址与本地缓存的地址不一样，优先使用本地缓存的头像
						String tmp = UserManager.getInstance(activity).getCurrentUser().getHeadImg();
						String tmp2 = UserManager.getInstance(activity).getCurrentUser().getTempHeadImg();
						if(TextUtils.isEmpty(tmp)){
							if(!TextUtils.isEmpty(tmp2))
								if(!tmp2.equals(headImg))
									headImg = tmp2;
						}else{
							if(!tmp.equals(headImg))
								headImg = tmp;
						}
					}
				}
			}else{
				//统一好友头像
				if(TextUtils.isEmpty(headImg)){
					if(!TextUtils.isEmpty(mStrangerHeadUrl))
						headImg = mStrangerHeadUrl;
				}else{
					if(!TextUtils.isEmpty(mStrangerHeadUrl)&&!mStrangerHeadUrl.equals(headImg))
						headImg =mStrangerHeadUrl;
				}
			}
			if(holder.head_iv!=null)
				imageLoader.displayImage(headImg, holder.head_iv, options);
			
			// 群聊时，显示接收的消息的发送人的名称
			if (chatType == ChatType.GroupChat && message.direct == EMMessage.Direct.RECEIVE)
				holder.tv_userId.setText(message.getFrom());
			
			// 如果是发送的消息并且不是群聊消息，显示已读textview
			if (chatType != ChatType.GroupChat&&message.direct == EMMessage.Direct.SEND) {
				holder.tv_ack = (TextView) convertView.findViewById(R.id.tv_ack);//已读
				holder.tv_delivered = (TextView) convertView.findViewById(R.id.tv_delivered);// 送达
				if (holder.tv_ack != null) {
					if (message.isAcked) {
						if (holder.tv_delivered != null) {
							holder.tv_delivered.setVisibility(View.GONE);
						}
						holder.tv_ack.setVisibility(View.VISIBLE);
					} else {
						holder.tv_ack.setVisibility(View.GONE);
						// check and display msg delivered ack status
						if (holder.tv_delivered != null) {
							if (message.isDelivered) {
								holder.tv_delivered.setVisibility(View.VISIBLE);
							} else {
								holder.tv_delivered.setVisibility(View.GONE);
							}
						}
					}
				}
			} else{
				// 如果是文本或者地图消息并且不是group messgae，显示的时候给对方发送已读回执
				if ((message.getType() == Type.TXT || message.getType() == Type.LOCATION) && !message.isAcked && chatType != ChatType.GroupChat) {
					// 不是语音通话记录
					try {
						EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
						// 发送已读回执
						message.isAcked = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			switch (message.getType()) {
			case IMAGE: // 图片
				handleImageMessage(message, holder, position, convertView);
				break;
			case TXT: // 文本
				if (!message.getBooleanAttribute(MyConstants.MESSAGE_ATTR_IS_VOICE_CALL, false))
					handleTextMessage(message, holder, position);
				else
					// 语音电话
					handleVoiceCallMessage(message, holder, position);
				break;
//			case LOCATION: // 位置
//				handleLocationMessage(message, holder, position, convertView);
//				break;
			case VOICE: // 语音
				handleVoiceMessage(message, holder, position, convertView);
				break;
//			case VIDEO: // 视频
//				handleVideoMessage(message, holder, position, convertView);
//				break;
//			case FILE: // 一般文件
//				handleFileMessage(message, holder, position, convertView);
//				break;
			default:
				// not supported
			}

			if (message.direct == EMMessage.Direct.SEND) {
				View statusView = convertView.findViewById(R.id.msg_status);
				if(statusView!=null)
				// 重发按钮点击事件
				statusView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 显示重发消息的自定义alertdialog
						Intent intent = new Intent(activity, MyAlertDialog.class);
						intent.putExtra("msg", activity.getString(R.string.confirm_resend));
						intent.putExtra("title", activity.getString(R.string.resend));
						intent.putExtra("cancel", true);
						intent.putExtra("position", position);
						if (message.getType() == EMMessage.Type.TXT)
							activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_TEXT);
						else if (message.getType() == EMMessage.Type.VOICE)
							activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_VOICE);
						else if (message.getType() == EMMessage.Type.IMAGE){
							if(message.getBooleanAttribute("isGift_image",false)){
								activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_SEND_GIFT);
							}else if(message.getBooleanAttribute("isGame_image",false)){
								if(message.direct==EMMessage.Direct.SEND)
									activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_PLAY_GAME);
								else
									activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_PLAYING_GAME);
							}else 
								activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_PICTURE);
						}
						else if (message.getType() == EMMessage.Type.LOCATION)
							activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_LOCATION);
						else if (message.getType() == EMMessage.Type.FILE)
							activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_FILE);
						else if (message.getType() == EMMessage.Type.VIDEO)
							activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_VIDEO);

					}
				});

			}
//			else {
				// 长按头像，移入黑名单
//				holder.head_iv.setOnLongClickListener(new OnLongClickListener() {
//
//					@Override
//					public boolean onLongClick(View v) {
//						Intent intent = new Intent(activity, AlertDialog.class);
//						intent.putExtra("msg", "移入到黑名单？");
//						intent.putExtra("cancel", true);
//						intent.putExtra("position", position);
//						activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_ADD_TO_BLACKLIST);
//						return true;
//					}
//				});
//			}
			
			TextView timestamp = (TextView) convertView.findViewById(R.id.timestamp);
			if (position == 0) {
				timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
				timestamp.setVisibility(View.VISIBLE);
			} else {
				// 两条消息时间离得如果稍长，显示时间
				if (DateUtils.isCloseEnough(message.getMsgTime(), conversation.getMessage(position - 1).getMsgTime())) {
					timestamp.setVisibility(View.GONE);
				} else {
					timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
					timestamp.setVisibility(View.VISIBLE);
				}
			}
			if(message.direct==EMMessage.Direct.SEND&&message.getBooleanAttribute("isGame_image",false))
				if(!message.getBooleanAttribute("isBeReceived",false)){
					TextView gameNotice = (TextView) convertView.findViewById(R.id.gameNotice);
					if(gameNotice!=null){
						gameNotice.setVisibility(View.VISIBLE);
						String str_money = message.getStringAttribute("eachGameMoney", "");
						String str_msg = "系统代收"+str_money+"扇贝,超过24小时无人应战,扇贝会全额返还";
						gameNotice.setText(str_msg);
					}
				}
		}
		
		return convertView;
	}

	/**
	 * 文本消息
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 */
	private void handleTextMessage(@ NonNull EMMessage message, @NonNull ViewHolder holder, final int position) {
		//设置背景
		if(message.getBooleanAttribute(MyConstants.MESSAGE_ATTR_ISSAYHELLO, false)){
			if(message.direct==EMMessage.Direct.RECEIVE){
				holder.tv.setBackgroundResource(R.drawable.bg_hello_left);
			}else{
				holder.tv.setBackgroundResource(R.drawable.bg_hello_rigth);
			}
		}else{
			if(message.direct==EMMessage.Direct.RECEIVE){
				holder.tv.setBackgroundResource(R.drawable.chatfrom_bg);
			}else{
				holder.tv.setBackgroundResource(R.drawable.chatto_bg);
			}
		}
		TextMessageBody txtBody = (TextMessageBody) message.getBody();
		Spannable span = SmileUtils.getSmiledText(context, txtBody.getMessage());
		// 设置内容
		holder.tv.setText(span, BufferType.SPANNABLE);
		// 设置长按事件监听
		holder.tv.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				activity.startActivityForResult(
						(new Intent(activity, ContextMenu.class)).putExtra("position", position).putExtra("type",
								EMMessage.Type.TXT.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
				return true;
			}
		});

		if (message.direct == EMMessage.Direct.SEND) {
			switch (message.status) {
			case SUCCESS: // 发送成功
				if(holder.pb!=null&&holder.staus_iv!=null){
					holder.pb.setVisibility(View.GONE);
					holder.staus_iv.setVisibility(View.GONE);
				}
				break;
			case FAIL: // 发送失败
				if(holder.pb!=null&&holder.staus_iv!=null){
					holder.pb.setVisibility(View.GONE);
					holder.staus_iv.setVisibility(View.VISIBLE);
				}
				break;
			case INPROGRESS: // 发送中
				if(holder.pb!=null&&holder.staus_iv!=null){
					holder.pb.setVisibility(View.VISIBLE);
					holder.staus_iv.setVisibility(View.GONE);
				}
				break;
			default:
				// 发送消息
				sendMsgInBackground(message, holder);
			}
		}
	}

	/**
	 * 语音通话记录
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 */
	private void handleVoiceCallMessage(EMMessage message, ViewHolder holder, final int position) {
		TextMessageBody txtBody = (TextMessageBody) message.getBody();
		holder.tv.setText(txtBody.getMessage());

	}

	/**
	 * 图片消息
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 * @param convertView
	 */
	private void handleImageMessage(@NonNull final EMMessage message, @NonNull final ViewHolder holder, final int position, View convertView) {
		if(holder==null||message==null||convertView ==null)
			return ;
		if(holder.pb!=null)
			holder.pb.setTag(position);
		//注册监听事件
		registerImage(message, holder,position);

		// 接收方向的消息
		if (message.direct == EMMessage.Direct.RECEIVE) {
			updatePicture(message,holder,convertView);
			return ;
		}
		
		// 发送的消息
		if(message.getBooleanAttribute("isGift_image",false)){
			if(holder.tvGiftName==null||holder.tvGiftPrice==null||holder.tvGiftReward==null)
				return ;
			try {
				//显示礼物
				convertView.findViewById(R.id.rl_gift).setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.rl_game).setVisibility(View.GONE);
				convertView.findViewById(R.id.gameNotice).setVisibility(View.GONE);
				holder.iv.setVisibility(View.GONE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(convertView.findViewById(R.id.rl_gift)!=null)
					convertView.findViewById(R.id.rl_gift).setVisibility(View.VISIBLE);
			}
			
			String remoteUrl = message.getStringAttribute("giftImg", "");
			imageLoader.displayImage(remoteUrl, holder.ivGiftImage, options);
			holder.tvGiftName.setText(message.getStringAttribute("giftName", ""));
			holder.tvGiftPrice.setText("价格："+message.getStringAttribute("giftPrice", "0"));
			holder.tvGiftReward.setText("魅力值：+"+message.getStringAttribute("giftCharm", "0"));
		}else if(message.getBooleanAttribute("isGame_image",false)){
			if(holder.ivGameImage==null||holder.tvGameStatus==null)
				return ;
			//显示游戏
			try{
				convertView.findViewById(R.id.rl_gift).setVisibility(View.GONE);
				convertView.findViewById(R.id.rl_game).setVisibility(View.VISIBLE);
				holder.iv.setVisibility(View.GONE);
			}catch(Exception e){
				if(convertView.findViewById(R.id.rl_game)!=null)
					convertView.findViewById(R.id.rl_game).setVisibility(View.VISIBLE);
			}
			
			holder.ivGameImage.setImageResource(R.drawable.chat_game);
			int result = -1;
			String str_result = message.getStringAttribute("gameResult", "");
			if(MoMaUtil.isDigist(str_result))
				result = Integer.valueOf(str_result);
			
			boolean isBeReceived = message.getBooleanAttribute("isBeReceived", false);
			boolean isHaveLooked = message.getBooleanAttribute("isHaveLooked", false);
			boolean isRefuse = message.getBooleanAttribute("isRefuse",false);
			if(isRefuse){
				holder.tvGameStatus.setText("拒绝");
			}else{
				if(isBeReceived){
					if(result==2)
						holder.tvGameStatus.setText("战绩：你赢了");
					else if(result==1)
						holder.tvGameStatus.setText("战绩：平局");
					else if(result==0)
						holder.tvGameStatus.setText("战绩：败北");
					
					if(!isHaveLooked)
						holder.tvGameStatus.setText("已应战，点击查看结果");
				}else
					holder.tvGameStatus.setText("等待应战……");
			}
		}else{
			try {
				convertView.findViewById(R.id.gameNotice).setVisibility(View.GONE);
				convertView.findViewById(R.id.rl_gift).setVisibility(View.GONE);
				convertView.findViewById(R.id.rl_game).setVisibility(View.GONE);
				holder.iv.setVisibility(View.VISIBLE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				holder.iv.setVisibility(View.VISIBLE);
			}
			
			ImageMessageBody imgBody = (ImageMessageBody) message.getBody();
			String filePath = imgBody.getLocalUrl();
			if (filePath != null) {
				if(new File(filePath).exists())
					showImageView(ImageUtils.getThumbnailImagePath(filePath), holder.iv, filePath, null, message);
				else 
					showImageView(ImageUtils.getThumbnailImagePath(filePath), holder.iv, filePath, IMAGE_DIR, message);
			}
		}
		
		switch (message.status) {
		case SUCCESS:
			holder.pb.setVisibility(View.GONE);
			holder.tv.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.GONE);
			break;
		case FAIL:
			holder.pb.setVisibility(View.GONE);
			holder.tv.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.VISIBLE);
			if(message.getBooleanAttribute("isGame_image",false)&&holder.tvGameStatus==null){
				holder.tvGameStatus.setText("发送失败");
			}
			break;
		case INPROGRESS:
			holder.staus_iv.setVisibility(View.GONE);
			holder.pb.setVisibility(View.VISIBLE);
			holder.tv.setVisibility(View.VISIBLE);
			if (timers.containsKey(message.getMsgId()))
				return;
			// set a timer
			final Timer timer = new Timer();
			timers.put(message.getMsgId(), timer);
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							holder.pb.setVisibility(View.VISIBLE);
							holder.tv.setVisibility(View.VISIBLE);
							holder.tv.setText(message.progress + "%");
							if (message.status == EMMessage.Status.SUCCESS) {
								holder.pb.setVisibility(View.GONE);
								holder.tv.setVisibility(View.GONE);
								// message.setSendingStatus(Message.SENDING_STATUS_SUCCESS);
								timer.cancel();
							} else if (message.status == EMMessage.Status.FAIL) {
								holder.pb.setVisibility(View.GONE);
								holder.tv.setVisibility(View.GONE);
								// message.setSendingStatus(Message.SENDING_STATUS_FAIL);
								// message.setProgress(0);
								holder.staus_iv.setVisibility(View.VISIBLE);
								showTost(activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast));
								timer.cancel();
							}

						}
					});

				}
			}, 0, 500);
			break;
		default:
			sendPictureMessage(message, holder);
		}
	}
	

	/**
	 * 语音消息
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 * @param convertView
	 */
	private void handleVoiceMessage(@NonNull final EMMessage message, @NonNull final ViewHolder holder, final int position, View convertView) {
		VoiceMessageBody voiceBody = (VoiceMessageBody) message.getBody();
		holder.tv.setText(voiceBody.getLength() + "\"");
		convertView.findViewById(R.id.ll_content).setOnClickListener(new VoicePlayClickListener(message, holder.iv, holder.iv_read_status, this, activity, username) );
		convertView.findViewById(R.id.ll_content).setOnLongClickListener(new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			activity.startActivityForResult(
					(new Intent(activity, ContextMenu.class)).putExtra("position", position).putExtra("type",
							EMMessage.Type.VOICE.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
			return true;
		}
	});
		
//		holder.iv.setOnClickListener(new VoicePlayClickListener(message, holder.iv, holder.iv_read_status, this, activity, username));
//		holder.iv.setOnLongClickListener(new OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				activity.startActivityForResult(
//						(new Intent(activity, ContextMenu.class)).putExtra("position", position).putExtra("type",
//								EMMessage.Type.VOICE.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
//				return true;
//			}
//		});

		if (message.direct == EMMessage.Direct.RECEIVE) {
			if (message.isAcked) {
				// 隐藏语音未读标志
				holder.iv_read_status.setVisibility(View.INVISIBLE);
			} else {
				holder.iv_read_status.setVisibility(View.VISIBLE);
			}
			if (message.status == EMMessage.Status.INPROGRESS) {
				holder.pb.setVisibility(View.VISIBLE);
				((FileMessageBody) message.getBody()).setDownloadCallback(new EMCallBack() {

					@Override
					public void onSuccess() {
						activity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								holder.pb.setVisibility(View.INVISIBLE);
								notifyDataSetChanged();
							}
						});

					}

					@Override
					public void onProgress(int progress, String status) {
					}

					@Override
					public void onError(int code, String message) {
						activity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								holder.pb.setVisibility(View.INVISIBLE);
							}
						});

					}
				});

			} else {
				holder.pb.setVisibility(View.INVISIBLE);

			}
			return;
		}

		// until here, deal with send voice msg
		switch (message.status) {
		case SUCCESS:
			holder.pb.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.GONE);
			break;
		case FAIL:
			holder.pb.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.VISIBLE);
			break;
		case INPROGRESS:
			holder.pb.setVisibility(View.VISIBLE);
			holder.staus_iv.setVisibility(View.GONE);
			break;
		default:
			sendMsgInBackground(message, holder);
		}
	}



	/**
	 * 发送消息
	 * 
	 * @param message
	 * @param holder
	 * @param position
	 */
	public void sendMsgInBackground(final EMMessage message, final ViewHolder holder) {
		if(message==null)
			return ;
		holder.staus_iv.setVisibility(View.GONE);
		holder.pb.setVisibility(View.VISIBLE);

		// 调用sdk发送异步发送方法
		EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

			@Override
			public void onSuccess() {
				updateSendedView(message, holder);
			}

			@Override
			public void onError(int code, String error) {
				updateSendedView(message, holder);
			}

			@Override
			public void onProgress(int progress, String status) {
			}

		});

	}

	/*
	 * chat sdk will automatic download thumbnail image for the image message we
	 * need to register callback show the download progress
	 */
	private void showDownloadImageProgress(final EMMessage message, final ViewHolder holder) {
		// final ImageMessageBody msgbody = (ImageMessageBody)
		// message.getBody();
		final FileMessageBody msgbody = (FileMessageBody) message.getBody();
		holder.pb.setVisibility(View.VISIBLE);
		holder.tv.setVisibility(View.VISIBLE);

		msgbody.setDownloadCallback(new EMCallBack() {

			@Override
			public void onSuccess() {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// message.setBackReceive(false);
						if (message.getType() == EMMessage.Type.IMAGE) {
							holder.pb.setVisibility(View.GONE);
							holder.tv.setVisibility(View.GONE);
						}
						notifyDataSetChanged();
					}
				});
			}

			@Override
			public void onError(int code, String message) {

			}

			@Override
			public void onProgress(final int progress, String status) {
				if (message.getType() == EMMessage.Type.IMAGE) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							holder.tv.setText(progress + "%");

						}
					});
				}

			}

		});
	}

	/*
	 * send message with new sdk
	 */
	private void sendPictureMessage(final EMMessage message, final ViewHolder holder) {
		try {
			// before send, update ui
			holder.staus_iv.setVisibility(View.GONE);
			holder.pb.setVisibility(View.VISIBLE);
			holder.tv.setVisibility(View.VISIBLE);
			holder.tv.setText("0%");
			// if (chatType == ChatActivity.CHATTYPE_SINGLE) {
			EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

				@Override
				public void onSuccess() {
					MoMaLog.d("debug", "send image message successfully");
					activity.runOnUiThread(new Runnable() {
						public void run() {
							// send success
							holder.pb.setVisibility(View.GONE);
							holder.tv.setVisibility(View.GONE);
						}
					});
				}

				@Override
				public void onError(int code, String error) {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							holder.pb.setVisibility(View.GONE);
							holder.tv.setVisibility(View.GONE);
							// message.setSendingStatus(Message.SENDING_STATUS_FAIL);
							holder.staus_iv.setVisibility(View.VISIBLE);
							showTost(activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast));
						}
					});
				}

				@Override
				public void onProgress(final int progress, String status) {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							holder.tv.setText(progress + "%");
						}
					});
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新ui上消息发送状态
	 * 
	 * @param message
	 * @param holder
	 */
	private void updateSendedView(final EMMessage message, final ViewHolder holder) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// send success
				if (message.getType() == EMMessage.Type.VIDEO) {
					holder.tv.setVisibility(View.GONE);
				}
				if (message.status == EMMessage.Status.SUCCESS) {
					// if (message.getType() == EMMessage.Type.FILE) {
					// holder.pb.setVisibility(View.INVISIBLE);
					// holder.staus_iv.setVisibility(View.INVISIBLE);
					// } else {
					// holder.pb.setVisibility(View.GONE);
					// holder.staus_iv.setVisibility(View.GONE);
					// }

				} else if (message.status == EMMessage.Status.FAIL) {
					// if (message.getType() == EMMessage.Type.FILE) {
					// holder.pb.setVisibility(View.INVISIBLE);
					// } else {
					// holder.pb.setVisibility(View.GONE);
					// }
					// holder.staus_iv.setVisibility(View.VISIBLE);
					showTost(activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast));
				}

				notifyDataSetChanged();
			}
		});
	}

	/**
	 * load image into image view
	 * 
	 * @param thumbernailPath
	 * @param iv
	 * @param position
	 * @return the image exists or not
	 */
	private boolean showImageView(@NonNull String thumbernailPath,@NonNull final ImageView iv, @NonNull final String localFullSizePath, final String remoteDir,
			@NonNull final EMMessage message) {
		Bitmap bitmap = ImageCache.getInstance().get(thumbernailPath);
		if (bitmap != null) {
			// thumbnail image is already loaded, reuse the drawable
			iv.setImageBitmap(bitmap);
			iv.setClickable(true);
			iv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					MoMaLog.d("debug","image view on click");
					Intent intent = new Intent(activity, ShowBigImage.class);
					File file = new File(localFullSizePath);
					if (file.exists()) {
						Uri uri = Uri.fromFile(file);
						intent.putExtra("uri", uri);
						System.err.println("here need to check why download everytime");
					} else {
						// The local full size pic does not exist yet.
						// ShowBigImage needs to download it from the server
						// first
						// intent.putExtra("", message.get);
						ImageMessageBody body = (ImageMessageBody) message.getBody();
						intent.putExtra("secret", body.getSecret());
						intent.putExtra("remotepath", remoteDir);
					}
					if (message != null && message.direct == EMMessage.Direct.RECEIVE && !message.isAcked
							&& message.getChatType() != ChatType.GroupChat) {
						try {
							EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
							message.isAcked = true;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					activity.startActivity(intent);
				}
			});
			return true;
		} else {
			if(TextUtils.isEmpty(thumbernailPath)&&!TextUtils.isEmpty(remoteDir)){
				thumbernailPath = remoteDir;
			}
			new LoadImageTask().execute(thumbernailPath, localFullSizePath, remoteDir, message.getChatType(), iv, activity, message);
			return true;
		}

	}

	public  class ViewHolder {
		ImageButton iv;
		TextView tv;
		ProgressBar pb;
		ImageView staus_iv;
		CircularImage head_iv;
		TextView tv_userId;
		ImageView playBtn;
		TextView timeLength;
		TextView size;
		LinearLayout container_status_btn;
		LinearLayout ll_container;
		ImageView iv_read_status;
		// 显示已读回执状态
		TextView tv_ack;
//		 显示送达回执状态
		TextView tv_delivered;

		TextView tv_file_name;
		TextView tv_file_size;
		TextView tv_file_download_state;
		
		ImageView ivGiftImage;//礼物图片
		TextView tvGiftName;//礼物名称
		TextView tvGiftPrice;	//礼物价格
		TextView tvGiftReward;//礼物奖励
		
		ImageView ivGameImage;	//游戏背景图
		TextView tvGameStatus;//游戏状态
		
	}
	
	/**
	 * 更新图片消息状态：接收成功
	 * @param message
	 * @param holder
	 */
	private void updatePicture(@NonNull EMMessage message, @NonNull final ViewHolder holder, @NonNull View view) {
		// TODO Auto-generated method stub
		if(holder.pb!=null&&holder.tv!=null){
			holder.pb.setVisibility(View.GONE);
			holder.tv.setVisibility(View.GONE);
		}
		if(message.getBooleanAttribute("isGift_image",false)){	//礼物
			View tmp = view.findViewById(R.id.rl_gift);
			try {
				view.findViewById(R.id.rl_game).setVisibility(View.GONE);
				tmp.setVisibility(View.VISIBLE);
				holder.iv.setVisibility(View.GONE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(tmp!=null)
					tmp.setVisibility(View.VISIBLE);
			}
			
			String remoteUrl = message.getStringAttribute("giftImg", ""); //礼物图片
			imageLoader.displayImage(remoteUrl, holder.ivGiftImage, options);
			holder.tvGiftName.setText(message.getStringAttribute("giftName", ""));	//礼物名称
			holder.tvGiftPrice.setText("价格："+message.getStringAttribute("giftPrice", "0"));	//礼物价格
			holder.tvGiftReward.setText("魅力：+"+message.getStringAttribute("giftCharm", "0"));	//魅力值
		}else if(message.getBooleanAttribute("isGame_image",false)){	//游戏
			try {
				view.findViewById(R.id.rl_game).setVisibility(View.VISIBLE);
				view.findViewById(R.id.rl_gift).setVisibility(View.GONE);
				holder.iv.setVisibility(View.GONE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			holder.ivGameImage.setImageResource(R.drawable.chat_game);
			boolean isRefuse = message.getBooleanAttribute("isRefuse",false);//是否被拒绝
			if(isRefuse){
				holder.tvGameStatus.setText("已拒绝");
			}else{
				//更具消息的状态，动态更新游戏状态
				if(message.getBooleanAttribute("isBeReceived",false)){ //已经接收挑战
					int result = -1;
					String str_result = message.getStringAttribute("gameResult", "");
					if(MoMaUtil.isDigist(str_result))
						result = Integer.valueOf(str_result);
					
					if(result==0)
						holder.tvGameStatus.setText("战绩：败北");
					else if(result==2)
						holder.tvGameStatus.setText("战绩：你赢了");
					else if(result==1)
						holder.tvGameStatus.setText("战绩：平局");
				}else{		//未接受挑战
					long startTime = message.getMsgTime(); //创建消息的时间
					long endTime = System.currentTimeMillis();
					long intervalTime =1*24*60*60*1000;//消息的有效期：1天
					//如果startTime与endTime之间的时间差大于或等于intervalTime，则重新请求服务器
					if(Math.abs(endTime-startTime)>=intervalTime){
						holder.tvGameStatus.setText("游戏已经过期");
						holder.ivGameImage.setClickable(false);
					}else{
						holder.tvGameStatus.setText("点击应战");
					}
				}
			}
		}else{
			try {
				view.findViewById(R.id.rl_game).setVisibility(View.GONE);
				view.findViewById(R.id.rl_gift).setVisibility(View.GONE);
				holder.iv.setVisibility(View.VISIBLE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				holder.iv.setVisibility(View.VISIBLE);
			}
			if (message.status == EMMessage.Status.INPROGRESS) {
				holder.iv.setImageResource(R.drawable.defalutimg);
				showDownloadImageProgress(message, holder);
				// downloadImage(message, holder);
			} else {
				holder.iv.setImageResource(R.drawable.defalutimg);
				ImageMessageBody imgBody = (ImageMessageBody) message.getBody();
				if(imgBody==null)
					return ;
				if (imgBody.getLocalUrl() != null) {
					// String filePath = imgBody.getLocalUrl();
					String remotePath = imgBody.getRemoteUrl();
					String filePath = ImageUtils.getImagePath(remotePath);
					String thumbRemoteUrl = imgBody.getThumbnailUrl();
					String thumbnailPath = ImageUtils.getThumbnailImagePath(thumbRemoteUrl);
					showImageView(thumbnailPath, holder.iv, filePath, imgBody.getRemoteUrl(), message);
				}
			}
			
		}
	}

	/**
	 * 注册图片的监听事件
	 * @param message 消息
	 * @param holder 控件容器
	 * @param position
	 */
	private void registerImage(final EMMessage message, final ViewHolder holder,final int position) {
		// TODO Auto-generated method stub
		if(position<0)
			return ;
		if(message.getBooleanAttribute("isGift_image",false)){
//			if(holder.ivGiftImage==null||holder.tvGiftName== null||holder.tvGiftPrice==null||holder.tvGiftReward==null)
//				return ;
//			MyOnClickListener listener = new MyOnClickListener(message.direct==EMMessage.Direct.RECEIVE);
//			holder.ivGiftImage.setOnClickListener(listener);
//			holder.tvGiftName.setOnClickListener(listener);
//			holder.tvGiftPrice.setOnClickListener(listener);
//			holder.tvGiftReward.setOnClickListener(listener);
		}else if(message.getBooleanAttribute("isGame_image",false)){
			if(holder.ivGameImage== null)
				return ;
			holder.ivGameImage.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(message.direct == EMMessage.Direct.RECEIVE){
						//如果用户已经玩了该次游戏，则屏蔽点击事件；
						//如果用户没有玩过该次游戏，则进入游戏
						boolean isBeReceived = message.getBooleanAttribute("isBeReceived",false);
						if(!isBeReceived){
							
							String bottleId= message.getStringAttribute("gameBottleId", "");
							String str_money = message.getStringAttribute("eachGameMoney", "");
							if(!MoMaUtil.isDigist(str_money)){
								str_money="0";
							}
							final int money = Integer.valueOf(str_money);
							String result = message.getStringAttribute("eachGameInfo", "");
							String toUser = message.getStringAttribute("sendUserId", ""); //游戏挑战者
							
							GameOpponentModel model = new GameOpponentModel(); //游戏挑战信息
							model.setBottleId(bottleId);
							model.setMoney(money);
							model.setContent(result);
							model.setHeadImg(message.getStringAttribute(MyConstants.MESSAGE_ATTR_HEADIMG,""));
							model.setUserId(toUser);
							model.setNickName(message.getFrom());
							
							final Intent intent = new Intent(activity, Game_Cq_Yz.class);
							intent.putExtra("GameOpponentModel", model);
							intent.putExtra("fromChat", true);
							//此处需要查询用户的扇贝
							int myMoney = MyApplication.getApp().getSpUtil().getMyMoney();
							if(myMoney<money){
								//重新查询用户的扇贝
								JsonObject jo = new JsonObject();
								jo.addProperty("userId", UserManager.getInstance(activity).getCurrentUserId());
								BottleRestClient.post("queryShanbei", activity, jo, new AsyncHttpResponseHandler(){

									@Override
									public void onSuccess(int statusCode,
											Header[] headers,
											byte[] responseBody) {
										// TODO Auto-generated method stub
										String res= new String(responseBody);
										try {
											JSONObject obj = new JSONObject(res);
											if("0".equals(obj.getString("code"))){
												String str_money = obj.getString("shanbei");
												if(str_money==null)
													return ;
												if(!MoMaUtil.isDigist(str_money)){
													MoMaLog.e("debug", "不是数字类型的字符串");
													str_money="0";
												}
												int myMoney = Integer.valueOf(str_money);
												MyApplication.getApp().getSpUtil().setMyMoney(myMoney);
												if(myMoney>=money)
													activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_PLAYING_GAME);
												else{
													showTost("亲，扇贝不足");
												}
												
											}else{
												showTost(res);
											}
											
										} catch (JSONException e) {
											// TODO Auto-generated catch block
											showTost("重试或联系客服");
										}
										
									}

									@Override
									public void onFailure(int statusCode,Header[] headers,byte[] responseBody, Throwable error) {
										// TODO Auto-generated method stub
										showTost("重试或联系客服");
									}});
								
							}else	{						
								activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_PLAYING_GAME);
							}
						}else{
							showTost("游戏已玩过");
						}
						
					}else{
						boolean isBeReceived = message.getBooleanAttribute("isBeReceived", false);
						boolean isHaveLooked = message.getBooleanAttribute("isHaveLooked",false);
						boolean isRefuse = message.getBooleanAttribute("isRefuse",false);
						if(isRefuse)
							return ;
						if(!isBeReceived){
							return ;
						}
						
						if(isHaveLooked){
							return ;
						}
						
						String bottleId= message.getStringAttribute("gameBottleId", "");
						queryGameByBottleId(UserManager.getInstance(activity).getCurrentUserId(), bottleId,holder);
						return ;
					}
				}
			});
			
		}else{
			if(holder.iv==null)
				return ;
			holder.iv.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					activity.startActivityForResult(
							(new Intent(activity, ContextMenu.class)).putExtra("position", position).putExtra("type",
									EMMessage.Type.IMAGE.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
					return true;
				}
			});
		}
	}
	
	class MyOnClickListener implements OnClickListener{
		private boolean  isReceive;
		public MyOnClickListener(boolean isReceive){
			this.isReceive = isReceive;
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String userId = UserManager.getInstance(activity).getCurrentUserId(); //当前账户
			if(isReceive){
				MoMaLog.e("debug", "查看收礼记录");
				Intent intent = new Intent(activity, GiftWallActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", userId);
				intent.putExtra("visitUserId", userId);
				activity.startActivity(intent);
				
			}else{
				MoMaLog.e("debug", "查看送礼记录");
				Intent intent = new Intent(activity, GiftBoxActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", userId);
				intent.putExtra("itemIndex", 1);
				activity.startActivity(intent);
			}
			
		}
		
	}
	/**
	 * 根据游戏瓶子的ID，获取游戏的详细信息
	 * @param userId
	 * @param bottleId
	 */
	private void queryGameByBottleId(String userId,final String bottleId,final ViewHolder holder){
		
		JsonObject jo = new JsonObject();
		jo.addProperty("userId",userId);
		jo.addProperty("bottleId", bottleId);
		
		BottleRestClient.post("queryGameByBottleId", activity, jo, new AsyncHttpResponseHandler(){
			@Override
			public void onStart() {
				super.onStart();
				
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String str = new String(responseBody);
				if(!TextUtils.isEmpty(str)){
					Gson gson = new Gson();
					QueryGameInfoModel gameInfoModel = gson.fromJson(str, QueryGameInfoModel.class);
					if(gameInfoModel != null && !TextUtils.isEmpty(gameInfoModel.getCode())){
						if("0".equals(gameInfoModel.getCode())){
							GameMsgModel gameInfo = gameInfoModel.getGameResultInfo();
							if(gameInfo.getState()==1){ //完成未查看 
								GameOpponentModel model = new GameOpponentModel();
								model.setBottleId(bottleId);
								model.setMoney(gameInfo.getMoney());
								model.setContent(gameInfo.getContent());
								model.setHeadImg(gameInfo.getHeadImg());
								model.setUserId(gameInfo.getUserId());
								model.setNickName(gameInfo.getNickName());
								
								Intent intent = new Intent(activity, Game_Cq_Activity.class);
								intent.putExtra("money", model.getMoney());
								intent.putExtra("GameOpponentModel", model);
								intent.putExtra("fromChat", true);
								intent.putExtra("bUId", gameInfo.getbUId());
								activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_PLAYED_GAME);
							}else{
								holder.ivGameImage.setClickable(false);
								if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
									holder.ivGameImage.setAlpha(0.5f);
								else{
									//设置多层样式
									holder.ivGameImage.setDrawingCacheEnabled(true);
									Drawable drawable = holder.ivGameImage.getDrawable();
									drawable.setColorFilter(Color.GRAY,Mode.MULTIPLY);
									holder.ivGameImage.setImageDrawable(drawable);
								}
							}
							
						}else{
							showTost(gameInfoModel.getMsg());
						}
					}else{
						showTost("提交失败,请重试");
					}
				}else{
					showTost("提交失败,请重试");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				showTost("提交失败,请检查网络，或联系客服");
			}
        });
	}
	private void  showTost(String msg){
		if(toast == null){
			toast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
		}else{
			toast.setText(msg);
		}
		toast.show();
	}

	public void setmStrangerHeadUrl(String mStrangerHeadUrl) {
		this.mStrangerHeadUrl = mStrangerHeadUrl;
	}

}