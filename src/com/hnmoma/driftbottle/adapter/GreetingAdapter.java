package com.hnmoma.driftbottle.adapter;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.util.DateUtils;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.adapter.AsyncStrangerLoader.StrangerCallback;
import com.hnmoma.driftbottle.custom.CircularImage;
import com.hnmoma.driftbottle.model.Stranger;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MyConstants;
import com.hnmoma.driftbottle.util.SmileUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.way.ui.emoji.EmojiTextView;

public class GreetingAdapter extends BaseAdapter {

	List<EMConversation> conversations;
	
	Context context;
	ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	private LayoutInflater inflater;
	private ListView listView;
	AsyncStrangerLoader asyncStrangerLoader;
	
	boolean isMulChoice = false;
	public HashMap<Integer, Boolean> ischeck;
	
	public  GreetingAdapter(Context context, ListView listView, List<EMConversation> conversations, HashMap<Integer, Boolean> ischeck) {
		if(inflater == null) {
			inflater = LayoutInflater.from(context);
		}
		this.context = context;
		this.conversations = conversations;
		asyncStrangerLoader = new AsyncStrangerLoader(context);
		this.listView = listView;
		this.ischeck = ischeck;
		
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
	public int getCount() {
		return conversations.size();
	}

	@Override
	public EMConversation getItem(int position) {
		return conversations.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null) {
			convertView = inflater.inflate(R.layout.row_chat_history, null);
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
		if(isMulChoice){
			holder.ll_checkbg.setVisibility(View.VISIBLE);
		} else {
			holder.ll_checkbg.setVisibility(View.INVISIBLE);
		}
		holder.cb_check.setChecked(ischeck.get(position));
		
		EMConversation conversation = getItem(position);
		if(conversation==null){
			convertView.setVisibility(View.GONE);
			return convertView;
		}
		String headImg = "";
		String nickName = "";
		// Load the image and set it on the ImageView
		String username = conversation.getUserName();
		holder.avatar.setTag(username+"_head");
		holder.name.setTag(username+"_name");
		// 延遲加載圖片 ： imageUrl 是 圖片的http鏈接地址，後面是回调函數
		Stranger stranger = asyncStrangerLoader.loadStranger(username, new StrangerCallback() {
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
		imageLoader.displayImage(headImg, holder.avatar, options);

	if (conversation.getUnreadMsgCount() > 0) {
		// 显示与此用户的消息未读数
		holder.unreadLabel.setText(String.valueOf(conversation.getUnreadMsgCount()));
		holder.unreadLabel.setVisibility(View.VISIBLE);
	} else {
		holder.unreadLabel.setVisibility(View.GONE);
	}

		// 把最后一条消息的内容作为item的message内容
		EMMessage lastMessage = conversation.getLastMessage();
		// TODO  判断消息的内容类型
//		String strBottle = lastMessage.getStringAttribute(MyConstants.MESSAGE_ATTR_BOTTLE, "");
		holder.message.setText(SmileUtils.getSmiledText(context, getMessageDigest(lastMessage, context)), BufferType.SPANNABLE);
		
		holder.time.setText(DateUtils.getTimestampString(new Date(lastMessage==null?System.currentTimeMillis():lastMessage.getMsgTime())));
		if(holder.msgState!=null&&lastMessage!=null){
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
	private String getMessageDigest(@ NonNull EMMessage message, @NonNull Context context) {
		String digest = "";
		if(message==null)
			return digest;
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
			ImageMessageBody imageBody = (ImageMessageBody) message.getBody();
			digest = getStrng(context, R.string.picture) + imageBody.getFileName();
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
			MoMaLog.e("debug","error, unknow type");
			return "";
		}

		return digest;
	}
	
	String getStrng(Context context, int resId) {
		return context.getResources().getString(resId);
	}
	
	public void addAll(List<EMConversation> conversations) {
		this.conversations = conversations;
	}
	
	public void setDelMode(boolean isMulChoice) {
		this.isMulChoice = isMulChoice;
	}

	static class ViewHolder {
		
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
	
}
