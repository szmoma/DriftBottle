package com.hnmoma.driftbottle.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.SysChatActivity;
import com.hnmoma.driftbottle.fragment.BaseFragment;
import com.hnmoma.driftbottle.model.Bottle;
import com.hnmoma.driftbottle.model.Chat;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.way.ui.emoji.EmojiTextView;

/**
 * http://blog.csdn.net/lllkey/article/details/9093107
 * @author MOMA_PC
 *
 */
public class ChatAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	Bottle bottleModel;
	
	SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
	
	private LinkedList<Chat> mInfos;
	
	SysChatActivity chatActivity;
	
	public BaseFragment contentFragment;
	
	public static interface IMsgViewType{
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
		int IMVT_MSG_COMMON_BOTTLE = 2;
		int IMVT_MSG_TASK_BOTTLE = 3;
//		int IMVT_MSG_CardFrame = 4;
//		int IMVT_MSG_VideoFrame = 5;
//		int IMVT_MSG_VoiceFrame = 6;
	}

	public ChatAdapter(ImageLoader imageLoader, DisplayImageOptions options, Bottle bottleModel, SysChatActivity chatActivity) {
		this.options = options;
		this.imageLoader = imageLoader;
		mInfos = new LinkedList<Chat>();
		this.bottleModel = bottleModel;
		this.chatActivity = chatActivity;
	}
	
	public int getItemViewType(int position) {
		Chat entity = mInfos.get(position);
	 	if(entity.getIsContent()){
	 		//套框架
			if(bottleModel.getBottleType().equals("4000")){
				return IMsgViewType.IMVT_MSG_COMMON_BOTTLE;
			}else if(bottleModel.getBottleType().equals("4001")){
				return IMsgViewType.IMVT_MSG_COMMON_BOTTLE;
			}else if(bottleModel.getBottleType().equals("4002")){
				return IMsgViewType.IMVT_MSG_COMMON_BOTTLE;
			}else {
				return IMsgViewType.IMVT_MSG_TASK_BOTTLE;
			}
	 	}else{
	 		if (entity.getIsComMsg()){
		 		return IMsgViewType.IMVT_COM_MSG;
		 	}else{
		 		return IMsgViewType.IMVT_TO_MSG;
		 	}
	 	}
	}


	public int getViewTypeCount() {
		return 4;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
	    ViewHolder2 holder2 = null;  
	    Chat chat = mInfos.get(position);
	    int type = getItemViewType(position);  
	    if (convertView == null) {  
	    	if(inflater==null){
				inflater = LayoutInflater.from(parent.getContext());
			}
            // 按当前所需的样式，确定new的布局  
            switch (type) {  
            case IMsgViewType.IMVT_MSG_COMMON_BOTTLE:  
            	convertView = inflater.inflate(R.layout.commonbottle_chat, null);
                break;  
            case IMsgViewType.IMVT_MSG_TASK_BOTTLE:  
            	convertView = inflater.inflate(R.layout.taskbottle_chat, null);
                break;  
            case IMsgViewType.IMVT_COM_MSG:  
            	convertView = inflater.inflate(R.layout.chatting_item_msg_text_left, null);
            	holder2 = new ViewHolder2();
            	holder2.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
            	holder2.tvContent = (EmojiTextView) convertView.findViewById(R.id.tv_chatcontent);
            	holder2.iv_userhead = (ImageView) convertView.findViewById(R.id.iv_userhead);
                convertView.setTag(holder2);  
                break;  
            case IMsgViewType.IMVT_TO_MSG:  
            	convertView = inflater.inflate(R.layout.chatting_item_msg_text_right, null);
            	holder2 = new ViewHolder2();
            	holder2.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
            	holder2.tvContent = (EmojiTextView) convertView.findViewById(R.id.tv_chatcontent);
            	holder2.iv_userhead = (ImageView) convertView.findViewById(R.id.iv_userhead);
                convertView.setTag(holder2);  
                break;  
            }  
  
        } else {  
            switch (type) {  
	            case IMsgViewType.IMVT_COM_MSG:  
	                holder2 = (ViewHolder2) convertView.getTag();  
	                break;  
	            case IMsgViewType.IMVT_TO_MSG:  
	                holder2 = (ViewHolder2) convertView.getTag();  
	                break;  
            }  
        }  
        // 设置资源  
        switch (type) {  
        case IMsgViewType.IMVT_COM_MSG:  
        	String contentType = chat.getContentType();
        	if(!TextUtils.isEmpty(contentType) && contentType.equals("5000")){
        		String cnt = chat.getContent();
        	    cnt = cnt==null?"":cnt;
        	    Date ChatTime = chat.getChatTime();
        	    
        	    cnt = chat.getContent();
        	    cnt = cnt==null?"":cnt;
        	    holder2.tvContent.setText(cnt);
        	    
        	    imageLoader.displayImage(chat.getHeadImg(), holder2.iv_userhead, options);
        	    
        	    if(position>1){
        	    	Chat preChat = mInfos.get(position-1);
        	    	Date preChatTime = preChat.getChatTime();
        	    	long preTime = preChatTime.getTime();
        	    	
            	    if(ChatTime != null){
            	    	long minc = ChatTime.getTime() - preTime;
            	    	
            	    	if(minc >= 1000*60){
            	    		holder2.tvSendTime.setVisibility(View.VISIBLE);
            	    		holder2.tvSendTime.setText(sdf.format(ChatTime));
            	    	}else{
            	    		holder2.tvSendTime.setVisibility(View.GONE);
            	    	}
            	    }else{
            	    	holder2.tvSendTime.setText("");
            	    }
        	    }else{
        	    	if(ChatTime!=null){
        	    		holder2.tvSendTime.setVisibility(View.VISIBLE);
            	    	holder2.tvSendTime.setText(sdf.format(ChatTime));
            	    }else{
            	    	holder2.tvSendTime.setVisibility(View.GONE);
            	    }
        	    }
        	}else{
        		holder2.tvContent.setText("版本过低无法显示，请升级到最新版");
        	}
            break;  
        case IMsgViewType.IMVT_TO_MSG: 
        	contentType = chat.getContentType();
        	if(!TextUtils.isEmpty(contentType) && contentType.equals("5000")){
        		String cnt = chat.getContent();
        	    cnt = cnt==null?"":cnt;
        	    holder2.tvContent.setText(cnt);
        	    
        	    Date ChatTime = chat.getChatTime();
        	    
        	    imageLoader.displayImage(chat.getHeadImg(), holder2.iv_userhead, options);
        	    
        	    if(position>1){
        	    	Chat preChat = mInfos.get(position-1);
        	    	Date preChatTime = preChat.getChatTime();
        	    	long preTime = preChatTime.getTime();
        	    	
            	    if(ChatTime != null){
            	    	long minc = ChatTime.getTime() - preTime;
            	    	
            	    	if(minc >= 1000*60){
            	    		holder2.tvSendTime.setVisibility(View.VISIBLE);
            	    		holder2.tvSendTime.setText(sdf.format(ChatTime));
            	    	}else{
            	    		holder2.tvSendTime.setVisibility(View.GONE);
            	    	}
            	    }else{
            	    	holder2.tvSendTime.setText("");
            	    }
        	    }else{
        	    	if(ChatTime!=null){
        	    		holder2.tvSendTime.setVisibility(View.VISIBLE);
            	    	holder2.tvSendTime.setText(sdf.format(ChatTime));
            	    }else{
            	    	holder2.tvSendTime.setVisibility(View.GONE);
            	    }
        	    }
        	    
//        	    ChatTime = chat.getChatTime();
//        	    if(ChatTime!=null){
//        	    	holder2.tvSendTime.setText(sdf.format(ChatTime));
//        	    }else{
//        	    	holder2.tvSendTime.setText("");
//        	    }
        	}else{
        		holder2.tvContent.setText("版本过低无法显示，请升级到最新版");
        	}
            break;  
        }  
  
        return convertView;  
    }  
	
//	/**
//	 * 数据填充界面
//	 * contentType 5000文字，5001图片，5002语音，5003视频
//	 */
//	private void initView(){
//		//套框架
//		if(bottleModel.getContentType().equals("5000")){
//			contentFragment = TextFrameFragment.newInstance(bottleModel);
//		}else if(bottleModel.getContentType().equals("5001")){
//			contentFragment = ImageFrameFragment.newInstance(bottleModel);
//		}else if(bottleModel.getContentType().equals("5002")){
//			contentFragment = VoiceFrameFragment.newInstance(bottleModel);
//		}else if(bottleModel.getContentType().equals("5003")){
//			
//		}
//		
//		chatActivity.getSupportFragmentManager()
//		.beginTransaction()
//		.replace(R.id.fl_content, contentFragment)
//		.commit();
//	}
	
	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public Chat getItem(int arg0) {
		return mInfos.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	public void addItemLast(List<Chat> datas) {
		mInfos.addAll(datas);
	}

	public void addItemTop(List<Chat> datas) {
		for(Chat chat : datas){
			mInfos.addFirst(chat);
		}
	}
	
	public String getLast10Items(){
		StringBuffer chatContens = new StringBuffer();
		int size = mInfos.size();
		
		int count = 0;
		for(int i = size -1; i>=0; i--){
			Chat chat = mInfos.get(i);
			
			if(chat.getIsContent()){
				chatContens.append("瓶子内容:" + chat.getContent() +";\n");
		 	}else{
		 		if (chat.getIsComMsg()){
		 			chatContens.append("被举报人:" + chat.getContent() +";\n");
			 	}else{
			 		chatContens.append("举报人:" + chat.getContent() +";\n");
			 	}
		 	}
			
			count ++;
			if(count>9){
				break;
			}
		}
		return chatContens.toString();
	}
	
	public void addOneItem2Top(Chat chat) {
		mInfos.addFirst(chat);
	}
	
	public void addOneItem2Foot(Chat chat) {
		mInfos.addLast(chat);
	}
	
	public void reset(List<Chat> datas) {
		mInfos.clear();
		mInfos.addAll(datas);
	}
	
	public void remove(Chat bottle) {
		mInfos.remove(bottle);
	}
	
    public class ViewHolder2 {  
    	public TextView tvSendTime;
	    public EmojiTextView tvContent;
	    public ImageView iv_userhead;
    }  
}