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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.easemob.chat.EMContact;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.adapter.AsyncStrangerLoader.StrangerCallback;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.Stranger;
import com.hnmoma.driftbottle.util.MyConstants;
import com.hnmoma.driftbottle.util.SmileUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.way.ui.emoji.EmojiTextView;

/**
 * 显示所有聊天记录adpater
 * 
 */
public class ChatAllHistoryAdapter extends ArrayAdapter<EMConversation> {

	private LayoutInflater inflater;
	
	ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;

	AsyncStrangerLoader asyncStrangerLoader;
	private ListView listView;
	
	public HashMap<Integer, Boolean> ischeck;
	boolean isMulChoice = false;
	SimpleDateFormat sdf;
//	String checkedName;
//	boolean isSelected = false;
	
	public ChatAllHistoryAdapter(Context context, ListView listView, int textViewResourceId, List<EMConversation> objects,HashMap<Integer, Boolean> ischeck) {
		super(context, textViewResourceId, objects);
		inflater = LayoutInflater.from(context);
//		daoSession = MyApplication.getApp().getDaoSession();
		 this.listView = listView;
		 this.ischeck = ischeck;
		asyncStrangerLoader = new AsyncStrangerLoader(context);
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)//avatar_default
		.showImageForEmptyUri(R.drawable.defalutimg)
		.showImageOnFail(R.drawable.defalutimg)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
//		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.bitmapConfig(Bitmap.Config.RGB_565)
//		.displayer(new RoundedBitmapDisplayer(2))
		.build();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		  ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.row_chat_history, parent, false);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.unreadLabel = (TextView) convertView.findViewById(R.id.unread_msg_number);
			holder.message = (EmojiTextView) convertView.findViewById(R.id.message);
			holder.time = (TextView) convertView.findViewById(R.id.time);
			holder.avatar = (CircularImage) convertView.findViewById(R.id.avatar);
			holder.msgState = convertView.findViewById(R.id.msg_state);
			holder.list_item_layout = (RelativeLayout) convertView.findViewById(R.id.list_item_layout);
			holder.cb_check = (CheckBox) convertView.findViewById(R.id.cb_check);
			holder.ll_checkbg = (RelativeLayout) convertView.findViewById(R.id.ll_checkbg);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}	

		// 获取与此用户/群组的会话
		EMConversation conversation = getItem(position);
		// 获取用户username或者群组groupid
		final String userId = conversation.getUserName();
		List<EMGroup> groups = EMGroupManager.getInstance().getAllGroups();
		EMContact contact = null;
		boolean isGroup = false;
		for (EMGroup group : groups) {
			if (group.getGroupId().equals(userId)) {
				isGroup = true;
				contact = group;
				break;
			}
		}
			if(isMulChoice){
				holder.ll_checkbg.setVisibility(View.VISIBLE);
			} else {
				holder.ll_checkbg.setVisibility(View.INVISIBLE);
			}
			holder.cb_check.setChecked(ischeck.get(position));
			
			// Load the image and set it on the ImageView
			holder.avatar.setTag(userId+"_head");
			holder.name.setTag(userId+"_name");
			
			String headImg = "";
			String nickName = "";
			if("99999912".equals(conversation.getUserName())) {
				holder.name.setText("游戏战绩");
				holder.name.setTextColor(getContext().getResources().getColor(R.color.msg_game));
				holder.avatar.setImageResource(R.drawable.msg_game);
			} else if("99999910".equals(conversation.getUserName())) {
				holder.name.setText("系统消息");
				holder.name.setTextColor(getContext().getResources().getColor(R.color.msg_system_notice));
				holder.avatar.setImageResource(R.drawable.msg_notice);
			} else if("99999999".equals(conversation.getUserName())) {
				holder.name.setText("礼物盒子");
				holder.name.setTextColor(getContext().getResources().getColor(R.color.msg_gift));
				holder.avatar.setImageResource(R.drawable.msg_gift);
			}else if(conversation.getUserName().equals("99999923")) {
				//TODO 打招呼的item
				holder.name.setText("打招呼列表");
				holder.name.setTextColor(getContext().getResources().getColor(R.color.msg_sayhello));
				holder.avatar.setImageResource(R.drawable.msg_sayhello);
			}  else {
				// 延遲加載圖片 ： imageUrl 是 圖片的http鏈接地址，後面是回调函數
				Stranger stranger = asyncStrangerLoader.loadStranger(userId, new StrangerCallback() {
		            public void strangerLoaded(Stranger stranger) {
		            	if(stranger!=null){
		            		ImageView avatar = (ImageView) listView.findViewWithTag(stranger.getUserId()+"_head");
		            		TextView name = (TextView) listView.findViewWithTag(stranger.getUserId()+"_name");
		            		
		            		if (avatar != null) { // 防止图片url获取不到图片是，占位图片不见了的情况
		            			imageLoader.displayImage(stranger.getHeadImg(), avatar, options);
			                }
		            		
		            		if (name != null) { // 防止图片url获取不到图片是，占位图片不见了的情况
		            			name.setText(stranger.getNickName());
			                }
		            	}
		            }
		        });
				
			if (stranger == null) {
				holder.name.setText("");
				imageLoader.displayImage(headImg, holder.avatar, options);
			}else{
				if(!TextUtils.isEmpty(stranger.getNickName())){
					nickName = stranger.getNickName();
				}
				if(!TextUtils.isEmpty(stranger.getHeadImg())){
					headImg = stranger.getHeadImg();
				}
			}
			
			holder.name.setText(nickName);
			holder.name.setTextColor(Color.parseColor("#333333"));
			imageLoader.displayImage(headImg, holder.avatar, options);
		}

		if (conversation.getUnreadMsgCount() > 0 ) {
			// 显示与此用户的消息未读数
			holder.unreadLabel.setVisibility(View.VISIBLE);
			holder.unreadLabel.setText(String.valueOf(conversation.getUnreadMsgCount()));
		} else if("99999923".equals(conversation.getUserName())) {
			// 打招呼的列表
			int unreadcount = conversation.getLastMessage().getIntAttribute(MyConstants.HELLOMSG_ATTR_COUNT, 0);
			if(unreadcount > 0) {
				holder.unreadLabel.setVisibility(View.VISIBLE);
				holder.unreadLabel.setText(String.valueOf(unreadcount));
			} else {
				holder.unreadLabel.setVisibility(View.GONE);
			}
		} else  {
			holder.unreadLabel.setVisibility(View.GONE);
		}
		
		if (conversation.getMsgCount() != 0) {
			// 把最后一条消息的内容作为item的message内容
			EMMessage lastMessage = conversation.getLastMessage();
			// TODO  判断消息的内容类型
			if (lastMessage.getIntAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 0)==1){
				String strBottle = lastMessage.getStringAttribute(MyConstants.MESSAGE_ATTR_BOTTLE, "");
				Gson gson = new Gson();
				BottleModel bottleModel = null;
				try {
					bottleModel = gson.fromJson(strBottle, BottleModel.class);
				} catch (JsonSyntaxException e) {
					// TODO Auto-generated catch block
					try {
						JSONObject obj = new JSONObject(strBottle);
						if(bottleModel==null)
							bottleModel = new BottleModel();
						bottleModel.setBottleId(obj.getString("bottleId"));
						bottleModel.setBottleIdPk(obj.getLong("bottleIdPk"));
						bottleModel.setContent(obj.getString("content"));
						bottleModel.setRemark(obj.getString("remark"));
						bottleModel.setContentType(obj.getString("contentType"));
						bottleModel.setBottleType(obj.getString("bottleType"));
						bottleModel.setUbId(obj.getString("ubId"));
						bottleModel.setBottleSort(obj.getString("bottleSort"));
						bottleModel.setSysType(obj.getString("sysType"));
						bottleModel.setCreateTime(new Date(obj.getString("createTime"))); //createTime:May 21, 2015 10:51:36
						bottleModel.setHasAnswer(obj.getBoolean("hasAnswer"));
						bottleModel.setState(obj.getInt("state"));
						bottleModel.setIsBack(obj.getInt("isBack"));
						bottleModel.setFromOther(obj.getInt("fromOther"));
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				String msgType = bottleModel.getContentType();
				if(!TextUtils.isEmpty(msgType)){
					String str = "";
					if(msgType.equals("5001")){
						str = "[图片] ";
					}else if(msgType.equals("5002")){
						str = "[贺卡] ";
					}else if(msgType.equals("5003")){
						str = "[视频] ";
					}else if(msgType.equals("5004")){
						str = "[语音] ";
					}
					holder.message.setText(str + bottleModel.getContent().trim());
				}	
			}else{
				if(lastMessage.getBooleanAttribute(MyConstants.MESSAGE_ATTR_ISSAYHELLO,false)){
					Stranger sg = MyApplication.getApp().getDaoSession().getStrangerDao().load(conversation.getUserName());
					if(sg!=null){
						if(lastMessage.direct==EMMessage.Direct.RECEIVE){
							holder.message.setText(Html.fromHtml("<i>"+sg.getNickName()+"<i/>向你打招呼"));
						}else{
							holder.message.setText(Html.fromHtml("你向<i>"+sg.getNickName()+"</i>打招呼"));
						}
					}else{
						holder.message.setText("打招呼");
					}
				}else{
					holder.message.setText(SmileUtils.getSmiledText(getContext(), getMessageDigest(lastMessage, (this.getContext()))), BufferType.SPANNABLE);
				}
			}
			Date msgTime = new Date(lastMessage.getMsgTime());
			Date now = new Date();
			if(msgTime.getDay() != now.getDay() || msgTime.getMonth() != now.getMonth()) {
				sdf = new SimpleDateFormat("MM月dd日 HH:mm");
			} else {
				sdf = new SimpleDateFormat("HH:mm");
			}
			holder.time.setText(sdf.format(new Date(lastMessage.getMsgTime())));
			if (lastMessage.direct == EMMessage.Direct.SEND && lastMessage.status == EMMessage.Status.FAIL) {
				holder.msgState.setVisibility(View.VISIBLE);
			} else {
				holder.msgState.setVisibility(View.GONE);
			}
		}
		
		return convertView;
	}
	
	
	/**
	 * 根据消息内容和消息类型获取消息内容提示
	 * 
	 * @param message
	 * @param context
	 * @return
	 */
	private String getMessageDigest(EMMessage message, Context context) {
		String digest = "";
		switch (message.getType()) {
		case LOCATION: // 位置消息
			if (message.direct == EMMessage.Direct.RECEIVE) {
				// 从sdk中提到了ui中，使用更简单不犯错的获取string的方法
				// digest = EasyUtils.getAppResourceString(context,
				// "location_recv");
				digest = getStrng(context, R.string.location_recv);
				digest = String.format(digest, message.getFrom());
				return digest;
			} else {
				// digest = EasyUtils.getAppResourceString(context,
				// "location_prefix");
				digest = getStrng(context, R.string.location_prefix);
			}
			break;
		case IMAGE: // 图片消息
			if(message.getBooleanAttribute("isGift_image", false)){
				digest = "[礼物]";
			}else if(message.getBooleanAttribute("isGame_image", false)){
				digest = "[划拳]";
			}else{
//				ImageMessageBody imageBody = (ImageMessageBody) message.getBody();
				digest = getStrng(context, R.string.picture) ;//+ imageBody.getFileName();
			}
			
			break;
		case VOICE:// 语音消息
			digest = getStrng(context, R.string.voice);
			break;
		case VIDEO: // 视频消息
			digest = getStrng(context, R.string.video);
			break;
		case TXT: // 文本消息
			if(!message.getBooleanAttribute(MyConstants.MESSAGE_ATTR_IS_VOICE_CALL,false)){
				TextMessageBody txtBody = (TextMessageBody) message.getBody();
				digest = txtBody.getMessage();
			}else{
				TextMessageBody txtBody = (TextMessageBody) message.getBody();
				digest = getStrng(context, R.string.voice_call) + txtBody.getMessage();
			}
			break;
		case FILE: // 普通文件消息
			digest = getStrng(context, R.string.file);
			break;
		default:
			System.err.println("error, unknow type");
			return "";
		}

		return digest;
	}
	
	private static class ViewHolder {
		/** 和谁的聊天记录 */
		TextView name;
		/** 消息未读数 */
		TextView unreadLabel;
		/** 最后一条消息的内容 */
		EmojiTextView message;
		/** 最后一条消息的时间 */
		TextView time;
		/** 用户头像 */
		CircularImage avatar;
		/** 最后一条消息的发送状态 */
		View msgState;
		/** 整个list中每一行总布局 */
		RelativeLayout list_item_layout;
		
		CheckBox cb_check;
		RelativeLayout ll_checkbg;
	}

	String getStrng(Context context, int resId) {
		return context.getResources().getString(resId);
	}

	public void setDelMode(boolean isMulChoice) {
		this.isMulChoice = isMulChoice;
	}
	
}
