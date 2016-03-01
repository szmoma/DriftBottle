package com.hnmoma.driftbottle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.http.Header;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.LocationMessageBody;
import com.easemob.chat.NormalFileMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VideoMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.EMLog;
import com.easemob.util.PathUtil;
import com.easemob.util.VoiceRecorder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.adapter.MessageAdapter;
import com.hnmoma.driftbottle.business.MyNotificationManager;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.PasteEditText;
import com.hnmoma.driftbottle.custom.TextPromptDialog;
import com.hnmoma.driftbottle.itfc.BottleVoicePlayClickListener;
import com.hnmoma.driftbottle.itfc.VoicePlayClickListener;
import com.hnmoma.driftbottle.model.BaseModel;
import com.hnmoma.driftbottle.model.GameOpponentModel;
import com.hnmoma.driftbottle.model.QueryUserInfoModel;
import com.hnmoma.driftbottle.model.Stranger;
import com.hnmoma.driftbottle.model.User;
import com.hnmoma.driftbottle.model.UserInfoModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.hnmoma.driftbottle.util.BitmapUtil;
import com.hnmoma.driftbottle.util.ImageUtils;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.MyConstants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.way.ui.emoji.EmojiKeyboard;

/**
 * 聊天页面，由于后台使用了AsyncTask异步下载音频文件，尽量指定屏幕的方向，否则会出现异常：非法持有对象
 * 
 */
public class ChatActivity extends BaseActivity implements OnClickListener{

	private static final int REQUEST_CODE_EMPTY_HISTORY = 2;	//	请求清空历史消息
	public static final int REQUEST_CODE_CONTEXT_MENU = 3;	//请求复制消息
	private static final int REQUEST_CODE_MAP = 4;	//请求地理位置
	public static final int REQUEST_CODE_TEXT = 5;	//发送文本消息
	public static final int REQUEST_CODE_VOICE = 6;	//发送语音
	public static final int REQUEST_CODE_PICTURE = 7;	//发送图片
	public static final int REQUEST_CODE_LOCATION = 8;	//发送地理位置
	public static final int REQUEST_CODE_NET_DISK = 9;	
	public static final int REQUEST_CODE_FILE = 10;	//发送文件
	public static final int REQUEST_CODE_COPY_AND_PASTE = 11;
	public static final int REQUEST_CODE_PICK_VIDEO = 12;
	public static final int REQUEST_CODE_DOWNLOAD_VIDEO = 13;
	public static final int REQUEST_CODE_VIDEO = 14;
	public static final int REQUEST_CODE_DOWNLOAD_VOICE = 15;
	public static final int REQUEST_CODE_SELECT_USER_CARD = 16;
	public static final int REQUEST_CODE_SEND_USER_CARD = 17;
	public static final int REQUEST_CODE_CAMERA = 18;	//发送照相机的图片
	public static final int REQUEST_CODE_LOCAL = 19;	//发送本地图片
	public static final int REQUEST_CODE_CLICK_DESTORY_IMG = 20;
	public static final int REQUEST_CODE_GROUP_DETAIL = 21;
	public static final int REQUEST_CODE_SELECT_VIDEO = 23;	//发送本地视频
	public static final int REQUEST_CODE_SELECT_FILE = 24;	//本地文件
	public static final int REQUEST_CODE_ADD_TO_BLACKLIST = 25;	//移入黑名单
	public static final int REQUEST_CODE_SEND_GIFT = 27;	//送礼物
	public static final int  REQUEST_CODE_PLAY_GAME = 26;	//游戏互动：挑战者向被挑战者发送消息
	public static final int  REQUEST_CODE_PLAYING_GAME = 28;  //游戏互动：被挑战者接收挑战
	public static final int REQUEST_CODE_PLAYED_GAME = 29;	//挑战者查看游戏信息
	
	public static final int RESULT_CODE_COPY = 1;
	public static final int RESULT_CODE_DELETE = 2;
	public static final int RESULT_CODE_FORWARD = 3;
	public static final int RESULT_CODE_OPEN = 4;
	public static final int RESULT_CODE_DWONLOAD = 5;
	public static final int RESULT_CODE_TO_CLOUD = 6;
	public static final int RESULT_CODE_EXIT_GROUP = 7;
	
	
	public static final int CHATTYPE_SINGLE = 1;
	public static final int CHATTYPE_GROUP = 2;

	public static final String COPY_IMAGE = "EASEMOBIMG";
	
	public static ChatActivity activityInstance = null;
	//录音
	private View recordingContainer;//录音的容器
	private ImageView micImage;	//录音的动画图
	private TextView recordingHint; //录影提示
	
	private ListView listView;
	
	private PasteEditText mEditTextContent;
	
	private View buttonSetModeKeyboard; //软键盘
	private View buttonSetModeVoice;
	private View buttonSend;
	private View buttonPressToSpeak;
	private EmojiKeyboard mFaceRoot;// 表情父容器
	private LinearLayout expressionContainer;
	private LinearLayout llContainerBtn;	//容器，包含的按钮是：图片、照相、礼物、游戏、文件、位置、视频
	private View more;
//	private int position;
	private ClipboardManager clipboard;
	private InputMethodManager manager;
//	private List<String> reslist;
	private Drawable[] micImages;
	private int chatType;
	private EMConversation conversation;
	private NewMessageBroadcastReceiver receiver;
//	public static ChatActivity activityInstance = null;
	// 给谁发送消息
	private VoiceRecorder voiceRecorder;
	private MessageAdapter adapter;
	private File cameraFile;
	static int resendPos;

//	private GroupListener groupListener;

	private ImageView iv_emoticons_normal;
	private ImageView iv_emoticons_checked;
	private RelativeLayout edittext_layout;
	private ProgressBar loadmorePB;
	private boolean isloading;
	private final int pagesize = 20;
	private boolean haveMoreData = true;
	private Button btnMore;
	
	private View bar_bottom;
	private View ll_more;
	private SwipeRefreshLayout swipeRefreshLayout;

	private Handler micImageHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			// 切换msg切换图片
			if(msg.what>0&&msg.what<micImages.length)
				micImage.setImageDrawable(micImages[msg.what]);
		}
	};
	private EMGroup group;
	
	private String myHeadImg;
	
	private Stranger mStranger;//与自己会话的人
	private String toChatUserId; //与自己聊天人的Id

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		initView();
		setUpView();
		activityInstance = this;
	}

	/**
	 * initView
	 */
	protected void initView() {
		recordingContainer = findViewById(R.id.recording_container);
		micImage = (ImageView) findViewById(R.id.mic_image);
		recordingHint = (TextView) findViewById(R.id.recording_hint);
		listView = (ListView) findViewById(R.id.list);
		mEditTextContent = (PasteEditText) findViewById(R.id.et_sendmessage);
		buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
		edittext_layout = (RelativeLayout) findViewById(R.id.edittext_layout);
		buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
		buttonSend = findViewById(R.id.btn_send);
		buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
//		expressionViewpager = (ViewPager) findViewById(R.id.vPager);
		bar_bottom = findViewById(R.id.bar_bottom);
		ll_more = findViewById(R.id.ll_more);
		
		mFaceRoot = (EmojiKeyboard) findViewById(R.id.face_ll);
		
		mFaceRoot.setEventListener(new com.way.ui.emoji.EmojiKeyboard.EventListener() {
			
			@Override
			public void onEmojiSelected(String res) {
				// TODO Auto-generated method stub
				EmojiKeyboard.input(mEditTextContent, res);
			}
			
			@Override
			public void onBackspace() {
				// TODO Auto-generated method stub
				EmojiKeyboard.backspace(mEditTextContent);
			}
		});
		
		expressionContainer = (LinearLayout) findViewById(R.id.ll_face_container);
		llContainerBtn = (LinearLayout) findViewById(R.id.ll_btn_container);
		iv_emoticons_normal = (ImageView) findViewById(R.id.iv_emoticons_normal);
		iv_emoticons_checked = (ImageView) findViewById(R.id.iv_emoticons_checked);
		loadmorePB = (ProgressBar) findViewById(R.id.pb_load_more);
		btnMore = (Button) findViewById(R.id.btn_more);
		iv_emoticons_normal.setVisibility(View.VISIBLE);
		iv_emoticons_checked.setVisibility(View.INVISIBLE);
		more = findViewById(R.id.more);
		edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);

		// 动画资源文件,用于录制语音时
		micImages = new Drawable[] { getResources().getDrawable(R.drawable.record_animate_01),
				getResources().getDrawable(R.drawable.record_animate_02), getResources().getDrawable(R.drawable.record_animate_03),
				getResources().getDrawable(R.drawable.record_animate_04), getResources().getDrawable(R.drawable.record_animate_05),
				getResources().getDrawable(R.drawable.record_animate_06), getResources().getDrawable(R.drawable.record_animate_07),
				getResources().getDrawable(R.drawable.record_animate_08), getResources().getDrawable(R.drawable.record_animate_09),
				getResources().getDrawable(R.drawable.record_animate_10), getResources().getDrawable(R.drawable.record_animate_11),
				getResources().getDrawable(R.drawable.record_animate_12), getResources().getDrawable(R.drawable.record_animate_13),
				getResources().getDrawable(R.drawable.record_animate_14)};

		edittext_layout.requestFocus();
		voiceRecorder = new VoiceRecorder(micImageHandler);
		buttonPressToSpeak.setOnTouchListener(new PressToSpeakListen());
		mEditTextContent.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_active);
				} else {
					edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);
				}

			}
		});
		mEditTextContent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_active);
				more.setVisibility(View.GONE);
				iv_emoticons_normal.setVisibility(View.VISIBLE);
				iv_emoticons_checked.setVisibility(View.INVISIBLE);
				expressionContainer.setVisibility(View.GONE);
				llContainerBtn.setVisibility(View.GONE);
			}
		});
		// 监听文字框
		mEditTextContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!TextUtils.isEmpty(s)) {
					btnMore.setVisibility(View.GONE);
					buttonSend.setVisibility(View.VISIBLE);
				} else {
					btnMore.setVisibility(View.VISIBLE);
					buttonSend.setVisibility(View.GONE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		
		 swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.chat_swipe_layout);

		 swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
		                 android.R.color.holo_orange_light, android.R.color.holo_red_light);
		 swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

	         @Override
	         public void onRefresh() {
	                 new Handler().postDelayed(new Runnable() {

	                         @Override
	                         public void run() {
	                                 if (listView.getFirstVisiblePosition() == 0 && !isloading && haveMoreData) {
	                                         List<EMMessage> messages;
	                                         try {
                                                 if (chatType == CHATTYPE_SINGLE){
                                                     messages = conversation.loadMoreMsgFromDB(adapter.getItem(0).getMsgId(), pagesize);
                                                 }
                                                 else{
                                                     messages = conversation.loadMoreGroupMsgFromDB(adapter.getItem(0).getMsgId(), pagesize);
                                                 }
	                                         } catch (Exception e1) {
                                                 swipeRefreshLayout.setRefreshing(false);
                                                 return;
	                                         }
	                                         
	                                         if (messages.size() > 0) {
                                                 adapter.refreshSeekTo((messages.size() - 1));
                                                 if (messages.size() != pagesize){
                                                     haveMoreData = false;
                                                 }
	                                         } else {
	                                             haveMoreData = false;
	                                         }
	                                         
	                                         isloading = false;

	                                 }else{
	                                	 showMsg("没有更多了");
	                                 }
	                                 swipeRefreshLayout.setRefreshing(false);
	                         }
	                 }, 1000);
	         }
	 });

	}

	private void setUpView() {
//		activityInstance = this;
		iv_emoticons_normal.setOnClickListener(this);
		iv_emoticons_checked.setOnClickListener(this);
		clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "driftbottle");
		// 判断单聊还是群聊
		chatType = getIntent().getIntExtra("chatType", CHATTYPE_SINGLE);

		if (chatType == CHATTYPE_SINGLE) { // 单聊
			toChatUserId = getIntent().getStringExtra("userId");
			conversation = EMChatManager.getInstance().getConversation(toChatUserId);
			
			mStranger = MyApplication.getApp().getDaoSession().getStrangerDao().load(toChatUserId);
			if(mStranger!=null && !TextUtils.isEmpty(mStranger.getNickName())){
				for(int i = conversation.getMsgCount()-1;i>=0;i--){
					if(2!=conversation.getMessage(i).getIntAttribute(MyConstants.MESSAGE_ATTR_NOTICETYPE, -1)){
						mStranger.setState(1);
						MyApplication.getApp().getDaoSession().insertOrReplace(mStranger); 
						break;
					}else{
						mStranger.setState(2);
						MyApplication.getApp().getDaoSession().insertOrReplace(mStranger); 
						break;
					}
				}
				
				((TextView) findViewById(R.id.name)).setText(mStranger.getNickName());
			}else{
				JsonObject jo = new JsonObject();
				jo.addProperty("id", toChatUserId);
				BottleRestClient.post("queryUserInfo", this, jo, new AsyncHttpResponseHandler (){
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
							QueryUserInfoModel userModel = gson.fromJson(str, QueryUserInfoModel.class);
							 
							 if(userModel!=null&&"0".equals(userModel.getCode())){
								 UserInfoModel userObj = userModel.getUserInfo();
								 if(userObj!=null){
									 Stranger stranger = new Stranger();
									stranger.setUserId(userObj.getUserId());
									stranger.setCity(userObj.getCity());
									stranger.setDescript(userObj.getDescript());
									try {
										stranger.setIdentityType(userObj.getIdentityType());
									} catch (Exception e) {
										// TODO: handle exception
									}
									stranger.setHeadImg(userObj.getHeadImg());
									stranger.setNickName(userObj.getNickName());
									stranger.setProvince(userObj.getProvince());
									stranger.setIsVIP(userObj.getIsVIP());
									
									for(int i=conversation.getMsgCount()-1;i>=0;i--){
										if(2!=conversation.getMessage(i).getIntAttribute(MyConstants.MESSAGE_ATTR_NOTICETYPE, -1)){
											stranger.setState(1);
											MyApplication.getApp().getDaoSession().insertOrReplace(stranger); 
											break;
										}else{
											stranger.setState(2);
											MyApplication.getApp().getDaoSession().insertOrReplace(stranger); 
											break;
										}
									}
									
									mStranger = stranger;
								 }
							 }
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					}
		        });
			
				String nickName = getIntent().getStringExtra("nickName");
				if(TextUtils.isEmpty(nickName)) {
					((TextView) findViewById(R.id.name)).setText("");
				} else {
					((TextView) findViewById(R.id.name)).setText(nickName);
				}
			}
		} else {
			// 群聊
			findViewById(R.id.container_to_group).setVisibility(View.VISIBLE);
			toChatUserId = getIntent().getStringExtra("groupId");
			conversation = EMChatManager.getInstance().getConversation(toChatUserId);
			group = EMGroupManager.getInstance().getGroup(toChatUserId);
			((TextView) findViewById(R.id.name)).setText(group.getGroupName());
		}
		
		//如果是从游戏页面过来的，无论连接状态是什么，可以聊天
		if(getIntent().getBooleanExtra("isConnOfGame", false)){
			if(mStranger!=null&&mStranger.getState()!=1){
				mStranger.setState(1);
				MyApplication.getApp().getDaoSession().getStrangerDao().update(mStranger);
			}
			
			if(conversation.getLastMessage()!=null&&2==conversation.getLastMessage().getIntAttribute(MyConstants.MESSAGE_ATTR_NOTICETYPE, -1)){
				//如果最后一条消息是是删除瓶子的消息，则去掉最后一条消息
				conversation.removeMessage(conversation.getLastMessage().getMsgId());
			}
		}
		
		//判断是否系统通知
		if(toChatUserId.equals("99999910")){
			bar_bottom.setVisibility(View.GONE);
			ll_more.setVisibility(View.GONE);
			((TextView) findViewById(R.id.name)).setText("系统消息");
			
		}
		
		conversation.resetUnreadMsgCount(); // 把此会话的未读数置为0
		
		adapter = new MessageAdapter(this, this.toChatUserId, chatType);
		// 显示消息
		listView.setAdapter(adapter);
		listView.setOnScrollListener(new ListScrollListener());
		int count = listView.getCount();
		if (count > 0) {
			listView.setSelection(count - 1);
		}
		
		if(mStranger!=null)
			adapter.setmStrangerHeadUrl(mStranger.getHeadImg());

		listView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hideKeyboard();
				more.setVisibility(View.GONE);
				iv_emoticons_normal.setVisibility(View.VISIBLE);
				iv_emoticons_checked.setVisibility(View.INVISIBLE);
				expressionContainer.setVisibility(View.GONE);
				llContainerBtn.setVisibility(View.GONE);
				return false;
			}
		});
		
		adapter.refreshSelectLast();
		// 注册接收消息广播
		receiver = new NewMessageBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
		// 设置广播的优先级别大于Mainacitivity,这样如果消息来的时候正好在chat页面，直接显示消息，而不是提示消息未读
		intentFilter.setPriority(5);
		registerReceiver(receiver, intentFilter);

		// 注册一个ack回执消息的BroadcastReceiver
		IntentFilter ackMessageIntentFilter = new IntentFilter(EMChatManager.getInstance().getAckMessageBroadcastAction());
		ackMessageIntentFilter.setPriority(5);
		registerReceiver(ackMessageReceiver, ackMessageIntentFilter);

		// 注册一个消息送达的BroadcastReceiver
		IntentFilter deliveryAckMessageIntentFilter = new IntentFilter(EMChatManager.getInstance().getDeliveryAckMessageBroadcastAction());
		deliveryAckMessageIntentFilter.setPriority(5);
		registerReceiver(deliveryAckMessageReceiver, deliveryAckMessageIntentFilter);
		
		// 注册一个cmd消息的BroadcastReceiver
		IntentFilter cmdIntentFilter = new IntentFilter(EMChatManager.getInstance().getCmdMessageBroadcastAction());
		cmdIntentFilter.setPriority(5);
		registerReceiver(cmdMessageReceiver, cmdIntentFilter);
		
		// 监听当前会话的群聊解散被T事件
//		groupListener = new GroupListener();
//		EMGroupManager.getInstance().addGroupChangeListener(groupListener);

		// show forward message if the message is not null
		String forward_msg_id = getIntent().getStringExtra("forward_msg_id");
		if (forward_msg_id != null) {
			// 显示发送要转发的消息
			forwardMessage(forward_msg_id);
		}
		if(UserManager.getInstance(this).getCurrentUser()!=null){
			myHeadImg = UserManager.getInstance(this).getCurrentUser().getHeadImg();
		}
		
		myHeadImg = (myHeadImg ==null?"":myHeadImg);
	}
	
	/**
	 * cmd消息BroadcastReceiver
	 */
	private BroadcastReceiver cmdMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//获取cmd message对象
//			String msgId = intent.getStringExtra("msgid");
			EMMessage message = intent.getParcelableExtra("message");
			//获取消息body
			CmdMessageBody cmdMsgBody = (CmdMessageBody) message.getBody();
			String action = cmdMsgBody.action;//获取自定义action
//			//获取扩展属性
			if(MyConstants.MESSAGE_ACTION_VZONETYPE.equals(action)){
				//全局的已经处理数据，这里只需要刷新
				adapter.refresh();
				abortBroadcast();
			}else if(MyConstants.UPDATEGAMESTATUS.equals(action)){
				//更新游戏的状态
				adapter.refresh();
				abortBroadcast();
			}else if(MyConstants.MESSAGE_ATTR_ISSAYHELLO.equals(action)){
				adapter.refresh();
			}
		}
	};

	/**
	 * 转发消息
	 * 
	 * @param forward_msg_id
	 */
	protected void forwardMessage(String forward_msg_id) {
		EMMessage forward_msg = EMChatManager.getInstance().getMessage(forward_msg_id);
		EMMessage.Type type = forward_msg.getType();
		switch (type) {
		case TXT:
			// 获取消息内容，发送消息
			String content = ((TextMessageBody) forward_msg.getBody()).getMessage();
			sendText(content);
			break;
		case IMAGE:
			// 发送图片
			if(forward_msg.getBooleanAttribute("isGift_image",false)){
				Bitmap gift = BitmapFactory.decodeResource(getResources(), R.drawable.default_image); 
				File file =new File(MyApplication.mAppPath+"pictures","gift.png");
				BitmapUtil.writeBitmap(file, gift,false);
				ImageMessageBody body = new ImageMessageBody(file);
				forward_msg.addBody(body);
				conversation.addMessage(forward_msg);
				
				listView.setAdapter(adapter);
				adapter.refresh();
				listView.setSelection(listView.getCount() - 1);
			}else if(forward_msg.getBooleanAttribute("isGame_image",false)){
				sendMessageOfGame(forward_msg);
			}else{
				String filePath = ((ImageMessageBody) forward_msg.getBody()).getLocalUrl();
				if (filePath != null) {
					File file = new File(filePath);
					if (!file.exists()) {
						// 不存在大图发送缩略图
						filePath = ImageUtils.getThumbnailImagePath(filePath);
					}
					sendPicture(filePath);
				}
			}
			
			break;
		default:
			break;
		}
	}

	/**
	 * onActivityResult
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CODE_EXIT_GROUP) {
			setResult(RESULT_OK);
			finish();
			return;
		}
		if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
			switch (resultCode) {
			case RESULT_CODE_COPY: // 复制消息
				EMMessage copyMsg = ((EMMessage) adapter.getItem(data.getIntExtra("position", -1)));
				if (copyMsg.getType() == EMMessage.Type.IMAGE) {
					ImageMessageBody imageBody = (ImageMessageBody) copyMsg.getBody();
					// 加上一个特定前缀，粘贴时知道这是要粘贴一个图片
					clipboard.setText(COPY_IMAGE + imageBody.getLocalUrl());
				} else {
					// clipboard.setText(SmileUtils.getSmiledText(ChatActivity.this,
					// ((TextMessageBody) copyMsg.getBody()).getMessage()));
					clipboard.setText(((TextMessageBody) copyMsg.getBody()).getMessage());
				}
				break;
			case RESULT_CODE_DELETE: // 删除消息
				EMMessage deleteMsg = (EMMessage) adapter.getItem(data.getIntExtra("position", -1));
				conversation.removeMessage(deleteMsg.getMsgId());
				adapter.refresh();
				listView.setSelection(data.getIntExtra("position", adapter.getCount()) - 1);
				break;

//			case RESULT_CODE_FORWARD: // 转发消息
//				EMMessage forwardMsg = (EMMessage) adapter.getItem(data.getIntExtra("position", 0));
//				Intent intent = new Intent(this, ForwardMessageActivity.class);
//				intent.putExtra("forward_msg_id", forwardMsg.getMsgId());
//				startActivity(intent);
//
//				break;

			default:
				break;
			}
		}
		if (resultCode == RESULT_OK) { // 清空消息
			if (requestCode == REQUEST_CODE_EMPTY_HISTORY) {
				// 清空会话
				EMChatManager.getInstance().clearConversation(toChatUserId);
				adapter.refresh();
			} else if (requestCode == REQUEST_CODE_CAMERA) { // 发送照片
				if (cameraFile != null && cameraFile.exists())
					sendPicture(cameraFile.getAbsolutePath());
			} else if (requestCode == REQUEST_CODE_SELECT_VIDEO) { // 发送本地选择的视频
				
				int duration = data.getIntExtra("dur", 0);
				String videoPath = data.getStringExtra("path");
				File file = new File(PathUtil.getInstance().getImagePath(), "thvideo" + System.currentTimeMillis());
				Bitmap bitmap = null;
				FileOutputStream fos = null;
				try {
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}
					bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, 3);
					if (bitmap == null) {
						EMLog.d("chatactivity", "problem load video thumbnail bitmap,use default icon");
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.app_panel_video_icon);
					}
					fos = new FileOutputStream(file);

					bitmap.compress(CompressFormat.JPEG, 100, fos);

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						fos = null;
					}
					if (bitmap != null) {
						bitmap.recycle();
						bitmap = null;
					}

				}
				sendVideo(videoPath, file.getAbsolutePath(), duration / 1000);

			} else if (requestCode == REQUEST_CODE_LOCAL) { // 发送本地图片
				if (data != null) {
					Uri selectedImage = data.getData();
					if (selectedImage != null) {
						sendPicByUri(selectedImage);
					}
				}
			} else if (requestCode == REQUEST_CODE_SELECT_FILE) { // 发送选择的文件
				if (data != null) {
					Uri uri = data.getData();
					if (uri != null) {
						sendFile(uri);
					}
				}

			} else if (requestCode == REQUEST_CODE_MAP) { // 地图
				double latitude = data.getDoubleExtra("latitude", 0);
				double longitude = data.getDoubleExtra("longitude", 0);
				String locationAddress = data.getStringExtra("address");
				if (locationAddress != null && !locationAddress.equals("")) {
					more(more);
					sendLocationMsg(latitude, longitude, "", locationAddress);
				} else {
					showMsg("无法获取到您的位置信息!");
				}
				// 重发消息
			} else if (requestCode == REQUEST_CODE_TEXT) {
				resendMessage();
			} else if (requestCode == REQUEST_CODE_VOICE) {
				resendMessage();
			} else if (requestCode == REQUEST_CODE_PICTURE) {
				resendMessage();
			} else if (requestCode == REQUEST_CODE_LOCATION) {
				resendMessage();
			} else if (requestCode == REQUEST_CODE_VIDEO || requestCode == REQUEST_CODE_FILE) {
				resendMessage();
			} else if (requestCode == REQUEST_CODE_COPY_AND_PASTE) {
				// 粘贴
				if (!TextUtils.isEmpty(clipboard.getText())) {
					String pasteText = clipboard.getText().toString();
					if (pasteText.startsWith(COPY_IMAGE)) {
						// 把图片前缀去掉，还原成正常的path
						sendPicture(pasteText.replace(COPY_IMAGE, ""));
					}

				}
			} else if (requestCode == REQUEST_CODE_ADD_TO_BLACKLIST) { // 移入黑名单
				EMMessage deleteMsg = (EMMessage) adapter.getItem(data.getIntExtra("position", -1));
				addUserToBlacklist(deleteMsg.getFrom());
			} else if (conversation.getMsgCount() > 0) {
				adapter.refresh();
				setResult(RESULT_OK);
			} else if (requestCode == REQUEST_CODE_GROUP_DETAIL) {
				adapter.refresh();
			}
		}
		if(requestCode==REQUEST_CODE_SEND_GIFT&&resultCode==RESULT_OK){
			if(data!=null){
				if(data.getBooleanExtra("repeat", false)){
					int positon = data.getIntExtra("position", -1);
					if(positon!=-1){	//消息重发
						EMMessage message = conversation.getMessage(positon);
						sendMessageOfGift(message);
						return ;
					}
				}
				Bundle extras = data.getExtras();
				if(extras!=null){
					sendMessageOfGift(extras);
				}
			}
		}else if(requestCode==REQUEST_CODE_PLAY_GAME&&resultCode==RESULT_OK){
			if(data!=null){
				if(data.getBooleanExtra("repeat", false)){
					int positon = data.getIntExtra("position", -1);
					if(positon!=-1){	//消息重发
						EMMessage message = conversation.getMessage(positon);
						sendMessageOfGame(message);
						return ;
					}
				}
				Bundle extras = data.getExtras();
				if(extras!=null){
					sendMessageOfGame(extras);
				}
			}
		}else if(requestCode==REQUEST_CODE_PLAYING_GAME&&resultCode==RESULT_OK){
			if(data!=null){
				if(data.getBooleanExtra("repeat", false)){
					int positon = data.getIntExtra("position", -1);
					if(positon!=-1){	//消息重发
						EMMessage message = conversation.getMessage(positon);
						sendMessageOfGame(message);
						return ;
					}
				}
				if(data.getBooleanExtra("isFinish", false)){
					int positon = data.getIntExtra("position", -1);
					refreshUI();
					listView.setSelection(positon);
					return ;
				}
				sendMessageOfGame(data);
			}
		}else if(requestCode==REQUEST_CODE_PLAYED_GAME&&resultCode==RESULT_OK){ 
			if(data!=null){
				if(data.getBooleanExtra("repeat", false)){
					int positon = data.getIntExtra("position", -1);
					if(positon!=-1){	//消息重发
						EMMessage message = conversation.getMessage(positon);
						sendMessageOfGame(message);
						return ;
					}
				}
				updateMessageOfGame(data);
			}
			
		}
	}

	/**
	 * 消息图标点击事件
	 * 
	 * @param view
	 */
	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.btn_send) {// 点击发送按钮(发文字和表情)
			
			String s = mEditTextContent.getText().toString();
			sendText(s);
		} else if (id == R.id.btn_take_picture) {
			//判断对方是否已经回应
			boolean backed = MyApplication.getApp().getSpUtil().getBackedByUserId(toChatUserId);
			if(!backed){
				//取出最后一条消息,如果该条消息不是普通对话类型，则不允许发图片
				EMMessage em = conversation.getLastMessage();
				if(em.direct == EMMessage.Direct.RECEIVE&&em.getIntAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 0)==0){
					MyApplication.getApp().getSpUtil().setBackedByUserId(toChatUserId, true);
					backed = true;
				}
//				List<EMMessage> allMsgs = conversation.getAllMessages();
//				for(EMMessage em : allMsgs){
//					if(em.direct == EMMessage.Direct.RECEIVE&&em.getIntAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 0)==0){
//						MyApplication.getApp().getSpUtil().setBackedByUserId(toChatUserId, true);
//						backed = true;
//						break;
//					}
//				}
			}
			
			if(backed){
				selectPicFromCamera();// 点击照相图标
			}else{
				showMsg("对方回复后才能发图片哦");
			}
		} else if (id == R.id.btn_picture) {	//发送图片
			//判断对方是否已经回应
			boolean backed = MyApplication.getApp().getSpUtil().getBackedByUserId(toChatUserId);
			if(!backed){
				for(int i=conversation.getMsgCount()-1;i>=0;i--){
					EMMessage em = conversation.getMessage(i);
					if(em.direct == EMMessage.Direct.RECEIVE&&em.getIntAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 0)==0){
						MyApplication.getApp().getSpUtil().setBackedByUserId(toChatUserId, true);
						backed = true;
						break;
					}
				}
			}
			
			if(backed){
				selectPicFromLocal(); // 点击图片图标
			}else{
				showMsg("对方回复后才能发图片哦");
			}
		} else if (id == R.id.btn_location) { // 发送位置
//			startActivityForResult(new Intent(this, BaiduMapActivity.class), REQUEST_CODE_MAP);
		} else if (id == R.id.iv_emoticons_normal) { // 点击显示表情框
			more.setVisibility(View.VISIBLE);
			iv_emoticons_normal.setVisibility(View.INVISIBLE);
			iv_emoticons_checked.setVisibility(View.VISIBLE);
			llContainerBtn.setVisibility(View.GONE);
			expressionContainer.setVisibility(View.VISIBLE);
			hideKeyboard();
		} else if (id == R.id.iv_emoticons_checked) { // 点击隐藏表情框
			iv_emoticons_normal.setVisibility(View.VISIBLE);
			iv_emoticons_checked.setVisibility(View.INVISIBLE);
			llContainerBtn.setVisibility(View.VISIBLE);
			expressionContainer.setVisibility(View.GONE);
			more.setVisibility(View.GONE);

		} else if (id == R.id.btn_video) { // 点击摄像图标
			Intent intent = new Intent(ChatActivity.this, ImageGridActivity.class);
			startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
		} else if (id == R.id.btn_file) { // 点击文件图标
			selectFileFromLocal();
		}
//		else if (id == R.id.btn_voice_call) { //点击语音电话图标
//			if(!EMChatManager.getInstance().isConnected())
//				Toast.makeText(this, "尚未连接至服务器，请稍后重试", 0).show();
//			else
//				startActivity(new Intent(ChatActivity.this, VoiceCallActivity.class).
//						putExtra("username", toChatUsername).
//						putExtra("isComingCall", false));
//		}
		else if(id == R.id.iv_userhead){
			int position = listView.getPositionForView(view);
			EMMessage em = adapter.getItem(position);
			String userId = em.getFrom();
			String visitUserId = UserManager.getInstance(this).getCurrentUserId();
			
			if(userId.equals("99999910")) {
				return;
			} else if(userId.equals(visitUserId)){
				Intent intent = new Intent(this, VzoneActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("userId", visitUserId);
				intent.putExtra("visitUserId", userId);
				intent.putExtra("identityflag", 0);
				startActivity(intent);
			}else{
				Intent intent = new Intent(this, VzoneActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				intent.putExtra("identityflag", 1);
				intent.putExtra("userId", userId);
				intent.putExtra("visitUserId", visitUserId);
				startActivity(intent);
			}
		}else if(id==R.id.btn_gift){	//礼物
			String visitUserId = UserManager.getInstance(this).getCurrentUserId();	//送礼物的对象
			Intent intent = new Intent(this, LwscActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			intent.putExtra("userId", toChatUserId);
			intent.putExtra("visitUserId", visitUserId);
			intent.putExtra("fromChat", true);
			startActivityForResult(intent, REQUEST_CODE_SEND_GIFT);
		}else if(id==R.id.btn_game){//游戏互动
			String visitUserId = UserManager.getInstance(this).getCurrentUserId();	//送礼物的对象
			Intent intent = new Intent(this, Game_cq_tz.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			intent.putExtra("userId", toChatUserId);
			intent.putExtra("visitUserId", visitUserId);
			intent.putExtra("fromChat", true);
			startActivityForResult(intent,  REQUEST_CODE_PLAY_GAME);
		}
	}
	
	

	/**
	 * 照相获取图片
	 */
	public void selectPicFromCamera() {
//		if(!hasCamera){
//			showMsg("没有相机的权限,请授权!");
//			return ;
//		}
		if (!MoMaUtil.isExitsSdcard()) {
			showMsg("SD卡不存在，不能拍照");
			return;
		}

		cameraFile = new File(PathUtil.getInstance().getImagePath(), UserManager.getInstance(this).getCurrentUserId()
				+ System.currentTimeMillis() + ".jpg");
		cameraFile.getParentFile().mkdirs();
		startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
				REQUEST_CODE_CAMERA);
	}

	/**
	 * 选择文件
	 */
	private void selectFileFromLocal() {
		Intent intent = null;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);

		} else {
			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
	}

	/**
	 * 从图库获取图片
	 */
	public void selectPicFromLocal() {
		Intent intent;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");

		} else {
			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		startActivityForResult(intent, REQUEST_CODE_LOCAL);
	}

	/**
	 * 发送文本消息
	 * 
	 * @param content
	 *            message content
	 * @param isResend
	 *            boolean resend
	 */
	private void sendText(String content) {
		
		if(check()){
			return;
		}

		if (content.trim().length() > 0) {
			EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
			message.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, MyApplication.getApp().getSpUtil().getUserHeadImage());
			
			// 如果是群聊，设置chattype,默认是单聊
			if (chatType == CHATTYPE_GROUP)
				message.setChatType(ChatType.GroupChat);
			TextMessageBody txtBody = new TextMessageBody(content.trim());
			// 设置消息body
			message.addBody(txtBody);
			// 设置要发给谁,用户username或者群聊groupid
			message.setReceipt(toChatUserId);
			// 把messgage加到conversation中
			conversation.addMessage(message);
			// 通知adapter有消息变动，adapter会根据加入的这条message显示消息和调用sdk的发送方法
			adapter.refresh();
			listView.setSelection(listView.getCount() - 1);
			mEditTextContent.setText("");
			
			setResult(RESULT_OK);

		}
	}

	/**
	 * 发送语音
	 * 
	 * @param filePath
	 * @param fileName
	 * @param length
	 * @param isResend
	 */
	private void sendVoice(String filePath, String fileName, String length, boolean isResend) {
		if(check()){
			return;
		}
		
		if (!(new File(filePath).exists())) {
			return;
		}
		try {
			final EMMessage message = EMMessage.createSendMessage(EMMessage.Type.VOICE);
			message.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, MyApplication.getApp().getSpUtil().getUserHeadImage());
			// 如果是群聊，设置chattype,默认是单聊
			if (chatType == CHATTYPE_GROUP)
				message.setChatType(ChatType.GroupChat);
			message.setReceipt(toChatUserId);
			int len = Integer.parseInt(length);
			VoiceMessageBody body = new VoiceMessageBody(new File(filePath), len);
			message.addBody(body);

			conversation.addMessage(message);
			adapter.refresh();
			listView.setSelection(listView.getCount() - 1);
			setResult(RESULT_OK);
			// send file
			// sendVoiceSub(filePath, fileName, message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送图片
	 * 
	 * @param filePath
	 */
	private void sendPicture(final String filePath) {
		
		if(check()){
			return ;
		}
		
		// create and add image message in view
		final EMMessage message = EMMessage.createSendMessage(EMMessage.Type.IMAGE);
		message.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, MyApplication.getApp().getSpUtil().getUserHeadImage());
		// 如果是群聊，设置chattype,默认是单聊
		if (chatType == CHATTYPE_GROUP)
			message.setChatType(ChatType.GroupChat);

		message.setReceipt(toChatUserId);
		ImageMessageBody body = new ImageMessageBody(new File(filePath));
		// 默认超过100k的图片会压缩后发给对方，可以设置成发送原图
//		 body.setSendOriginalImage(true);
		message.addBody(body);
		conversation.addMessage(message);

		listView.setAdapter(adapter);
		adapter.refresh();
		listView.setSelection(listView.getCount() - 1);
		setResult(RESULT_OK);
		// more(more);
	}

	/**
	 * 发送视频消息
	 */
	private void sendVideo(final String filePath, final String thumbPath, final int length) {
		if(check()){
			return ;
		}
		
		final File videoFile = new File(filePath);
		if (!videoFile.exists()) {
			return;
		}
		try {
			EMMessage message = EMMessage.createSendMessage(EMMessage.Type.VIDEO);
			message.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, MyApplication.getApp().getSpUtil().getUserHeadImage());
			// 如果是群聊，设置chattype,默认是单聊
			if (chatType == CHATTYPE_GROUP)
				message.setChatType(ChatType.GroupChat);
			message.setReceipt(toChatUserId);
			VideoMessageBody body = new VideoMessageBody(videoFile, thumbPath, length, videoFile.length());
			message.addBody(body);
			conversation.addMessage(message);
			listView.setAdapter(adapter);
			adapter.refresh();
			listView.setSelection(listView.getCount() - 1);
			setResult(RESULT_OK);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 根据图库图片uri发送图片
	 * 
	 * @param selectedImage
	 */
	private void sendPicByUri(Uri selectedImage) {
		if(check()){
			return ;
		}
		
		// String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(selectedImage, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex("_data");
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			cursor = null;

			if (picturePath == null || picturePath.equals("null")) {
				showMsg(getResources().getString(R.string.not_found_pic));
				return;
			}
			sendPicture(picturePath);
		} else {
			File file = new File(selectedImage.getPath());
			if (!file.exists()) {
				showMsg(getResources().getString(R.string.not_found_pic));
				return;

			}
			sendPicture(file.getAbsolutePath());
		}

	}

	/**
	 * 发送位置信息
	 * 
	 * @param latitude
	 * @param longitude
	 * @param imagePath
	 * @param locationAddress
	 */
	private void sendLocationMsg(double latitude, double longitude, String imagePath, String locationAddress) {
		if(check()){
			return ;
		}
		
		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.LOCATION);
		message.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, MyApplication.getApp().getSpUtil().getUserHeadImage());
		// 如果是群聊，设置chattype,默认是单聊
		if (chatType == CHATTYPE_GROUP)
			message.setChatType(ChatType.GroupChat);
		LocationMessageBody locBody = new LocationMessageBody(locationAddress, latitude, longitude);
		message.addBody(locBody);
		message.setReceipt(toChatUserId);
		conversation.addMessage(message);
		listView.setAdapter(adapter);
		refreshUI();
		listView.setSelection(listView.getCount() - 1);
		setResult(RESULT_OK);

	}

	/**
	 * 发送文件
	 * 
	 * @param uri
	 */
	private void sendFile(Uri uri) {
		if(check()){
			return ;
		}
		
		String filePath = null;
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
			Cursor cursor = null;

			try {
				cursor = getContentResolver().query(uri, projection, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					filePath = cursor.getString(column_index);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			filePath = uri.getPath();
		}
		File file = new File(filePath);
		if (file == null || !file.exists()) {
			Toast.makeText(getApplicationContext(), "文件不存在", 0).show();
			return;
		}
		if (file.length() > 10 * 1024 * 1024) {
			Toast.makeText(getApplicationContext(), "文件不能大于10M", 0).show();
			return;
		}

		// 创建一个文件消息
		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.FILE);
		message.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, MyApplication.getApp().getSpUtil().getUserHeadImage());
		// 如果是群聊，设置chattype,默认是单聊
		if (chatType == CHATTYPE_GROUP)
			message.setChatType(ChatType.GroupChat);

		message.setReceipt(toChatUserId);
		// add message body
		NormalFileMessageBody body = new NormalFileMessageBody(new File(filePath));
		message.addBody(body);

		conversation.addMessage(message);
		listView.setAdapter(adapter);
		adapter.refresh();
		listView.setSelection(listView.getCount() - 1);
		setResult(RESULT_OK);
	}

	/**
	 * 重发消息
	 */
	private void resendMessage() {
		EMMessage msg = null;
		msg = conversation.getMessage(resendPos);
		// msg.setBackSend(true);
		msg.status = EMMessage.Status.CREATE;

		adapter.refresh();
		listView.setSelection(resendPos);
	}

	/**
	 * 显示语音图标按钮
	 * 
	 * @param view
	 */
	public void setModeVoice(View view) {
		hideKeyboard();
		edittext_layout.setVisibility(View.GONE);
		more.setVisibility(View.GONE);
		view.setVisibility(View.GONE);
		buttonSetModeKeyboard.setVisibility(View.VISIBLE);
		buttonSend.setVisibility(View.GONE);
		btnMore.setVisibility(View.VISIBLE);
		buttonPressToSpeak.setVisibility(View.VISIBLE);
		iv_emoticons_normal.setVisibility(View.VISIBLE);
		iv_emoticons_checked.setVisibility(View.INVISIBLE);
		llContainerBtn.setVisibility(View.VISIBLE);
		expressionContainer.setVisibility(View.GONE);

	}

	/**
	 * 显示键盘图标
	 * 
	 * @param view
	 */
	public void setModeKeyboard(View view) {
		// mEditTextContent.setOnFocusChangeListener(new OnFocusChangeListener()
		// {
		// @Override
		// public void onFocusChange(View v, boolean hasFocus) {
		// if(hasFocus){
		// getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		// }
		// }
		// });
		edittext_layout.setVisibility(View.VISIBLE);
		more.setVisibility(View.GONE);
		view.setVisibility(View.GONE);
		buttonSetModeVoice.setVisibility(View.VISIBLE);
		// mEditTextContent.setVisibility(View.VISIBLE);
		mEditTextContent.requestFocus();
		// buttonSend.setVisibility(View.VISIBLE);
		buttonPressToSpeak.setVisibility(View.GONE);
		if (TextUtils.isEmpty(mEditTextContent.getText())) {
			btnMore.setVisibility(View.VISIBLE);
			buttonSend.setVisibility(View.GONE);
		} else {
			btnMore.setVisibility(View.GONE);
			buttonSend.setVisibility(View.VISIBLE);
		}

	}

	/**
	 * 点击清空聊天记录
	 * 
	 * @param view
	 */
	public void emptyHistory(View view) {
		startActivityForResult(
				new Intent(this, AlertDialog.class).putExtra("titleIsCancel", true).putExtra("msg", "是否清空所有聊天记录").putExtra("cancel", true),
				REQUEST_CODE_EMPTY_HISTORY);
	}

	/**
	 * 点击进入群组详情
	 * 
	 * @param view
	 */
	public void toGroupDetails(View view) {
//		startActivityForResult((new Intent(this, GroupDetailsActivity.class).putExtra("groupId", toChatUsername)),
//				REQUEST_CODE_GROUP_DETAIL);
	}

	/**
	 * 显示或隐藏图标按钮页
	 * 
	 * @param view
	 */
	public void more(View view) {
		if (more.getVisibility() == View.GONE) {
			hideKeyboard();
			more.setVisibility(View.VISIBLE);
			llContainerBtn.setVisibility(View.VISIBLE);
			expressionContainer.setVisibility(View.GONE);
		} else {
			//优先更多容器
			if(llContainerBtn.getVisibility()==View.GONE){
				if(expressionContainer.getVisibility() == View.VISIBLE){
					iv_emoticons_normal.setVisibility(View.VISIBLE);
					iv_emoticons_checked.setVisibility(View.INVISIBLE);
				}
				llContainerBtn.setVisibility(View.VISIBLE);
				expressionContainer.setVisibility(View.GONE);
			}else{
				if(expressionContainer.getVisibility() == View.GONE){
					more.setVisibility(View.GONE);
				}else{
					expressionContainer.setVisibility(View.GONE);
					llContainerBtn.setVisibility(View.VISIBLE);
					iv_emoticons_normal.setVisibility(View.VISIBLE);
					iv_emoticons_checked.setVisibility(View.INVISIBLE);
				}
				
			}

		}

	}

	/**
	 * 点击文字输入框
	 * 
	 * @param v
	 */
	public void editClick(View v) {
		listView.setSelection(listView.getCount() - 1);
		if (more.getVisibility() == View.VISIBLE) {
			more.setVisibility(View.GONE);
			iv_emoticons_normal.setVisibility(View.VISIBLE);
			iv_emoticons_checked.setVisibility(View.INVISIBLE);
		}

	}

	/**
	 * 消息广播接收者
	 * 
	 */
	private class NewMessageBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String username = intent.getStringExtra("from");
			String msgid = intent.getStringExtra("msgid");
			// 收到这个广播的时候，message已经在db和内存里了，可以通过id获取mesage对象
			EMMessage message = EMChatManager.getInstance().getMessage(msgid);
			if(message==null)
				return ;
			// 如果是群聊消息，获取到group id
			if (message.getChatType() == ChatType.GroupChat) {
				username = message.getTo();
			}
			if (!username.equals(toChatUserId)) {
				// 消息不是发给当前会话，return
				return;
			}
			
			if (message.getIntAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 0) == 1){
//				判断瓶子类型：0为捡起的瓶子，1为定向道具瓶子
				if (message.getIntAttribute(MyConstants.MESSAGE_ATTR_BOTTLETYPE, 0) == 0){
					//瓶子消息保存后加一条系统消息（对方收藏了瓶子，已建立连接/对方删除了瓶子无法建立联系）
					if(mStranger!=null){
						mStranger= MyApplication.getApp().getDaoSession().getStrangerDao().load(toChatUserId);
						if(mStranger!=null&&mStranger.getState()!=1)
							mStranger.setState(1);
					}
				}
			}
			
			
//			//如果是瓶子，就把陌生人对象和瓶子消息对象拿出来保存
//			if (message.getIntAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 0)==1){
//				Gson gson = new Gson();
//				String str_bottlemsg = message.getStringAttribute(MyConstants.MESSAGE_ATTR_BOTTLEMSG, "");
//				BottleMsg bottleMsg = gson.fromJson(str_bottlemsg, BottleMsg.class);
//				//
//				String str_stranger = message.getStringAttribute(MyConstants.MESSAGE_ATTR_STRANGER, "");
//				Stranger stranger = gson.fromJson(str_stranger, Stranger.class);
//				
//				
//				//添加瓶子消息
//				BottleMsgManager.getInstance(ChatActivity.this).insertBottleMsg(bottleMsg);
//				//添加陌生人消息
//				BottleMsgManager.getInstance(ChatActivity.this).insertOrReplaceStranger(stranger);
//				
//				//瓶子消息保存后加一条系统消息（对方收藏了瓶子，已建立连接/对方删除了瓶子无法建立联系）
//				BottleMsgManager.getInstance(ChatActivity.this).addMsgNotice(1, username);
//			}
			
			// conversation =
			// EMChatManager.getInstance().getConversation(toChatUsername);
			// 通知adapter有新消息，更新ui
			MyNotificationManager.getInstance(ChatActivity.this).cancleMsgNotice();
			
			adapter.refresh();
			listView.setSelection(listView.getCount() - 1);
			// 记得把广播给终结掉
			abortBroadcast();
		}
	}

	/**
	 * 消息回执BroadcastReceiver
	 */
	private BroadcastReceiver ackMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String msgid = intent.getStringExtra("msgid");
			String from = intent.getStringExtra("from");
			EMConversation conversation = EMChatManager.getInstance().getConversation(from);
			if (conversation != null) {
				// 把message设为已读
				EMMessage msg = conversation.getMessage(msgid);
				if (msg != null) {
					msg.isAcked = true;
				}
			}
			abortBroadcast();
			refreshUI();
		}
	};
	
	/**
	 * 消息送达BroadcastReceiver
	 */
	private BroadcastReceiver deliveryAckMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String msgid = intent.getStringExtra("msgid");
			String from = intent.getStringExtra("from");
			EMConversation conversation = EMChatManager.getInstance().getConversation(from);
			if (conversation != null) {
				// 把message设为已读
				EMMessage msg = conversation.getMessage(msgid);
				if (msg != null) {
					msg.isDelivered = true;
				}
			}
			abortBroadcast();
			refreshUI();
		}
	};
	private PowerManager.WakeLock wakeLock;

	/**
	 * 按住说话listener
	 * 
	 */
	class PressToSpeakListen implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (!MoMaUtil.isExitsSdcard()) {
					showMsg("发送语音需要sdcard支持!");
					return false;
				}
				try {
					v.setPressed(true);
					wakeLock.acquire();
					if (VoicePlayClickListener.isPlaying)
						VoicePlayClickListener.currentPlayListener.stopPlayVoice();
					recordingContainer.setVisibility(View.VISIBLE);
					recordingHint.setText(getString(R.string.move_up_to_cancel));
					recordingHint.setBackgroundColor(Color.TRANSPARENT);
					voiceRecorder.startRecording(null, toChatUserId, getApplicationContext());
				} catch (Exception e) {
					e.printStackTrace();
					v.setPressed(false);
					if (wakeLock.isHeld())
						wakeLock.release();
					if(voiceRecorder != null)
						voiceRecorder.discardRecording();
					recordingContainer.setVisibility(View.INVISIBLE);
					showMsg(getResources().getString( R.string.recoding_fail));
					return false;
				}

				return true;
			case MotionEvent.ACTION_MOVE: {
				if (event.getY() < 0) {
					recordingHint.setText(getString(R.string.release_to_cancel));
					recordingHint.setBackgroundResource(R.drawable.recording_text_hint_bg);
				} else {
					recordingHint.setText(getString(R.string.move_up_to_cancel));
					recordingHint.setBackgroundColor(Color.TRANSPARENT);
				}
				return true;
			}
			case MotionEvent.ACTION_UP:
				v.setPressed(false);
				recordingContainer.setVisibility(View.INVISIBLE);
				if (wakeLock.isHeld())
					wakeLock.release();
				if (event.getY() < 0) {
					// discard the recorded audio.
					voiceRecorder.discardRecording();

				} else {
					// stop recording and send voice file
//					if(!hasRecordAudio){
//						return true;
//					}
					try {
						int length = voiceRecorder.stopRecoding();
						
						if (length > 0) {
							sendVoice(voiceRecorder.getVoiceFilePath(), voiceRecorder.getVoiceFileName(toChatUserId),
									Integer.toString(length), false);
						} else if(length==EMError.INVALID_FILE){
							showMsg("没有录音全权限");
						}else {
							showMsg("录音时间太短");
						}
					} catch (Exception e) {
						e.printStackTrace();
						showMsg("发送失败，请检测服务器是否连接");
					}

				}
				return true;
//			default:
//				recordingContainer.setVisibility(View.INVISIBLE);
//				if(voiceRecorder != null)
//					voiceRecorder.discardRecording();
//				return false;
			}
			return false;
		}
	}

//	/**
//	 * 获取表情的gridview的子view
//	 * 
//	 * @param i
//	 * @return
//	 */
//	private View getGridChildView(int i) {
//		View view = View.inflate(this, R.layout.expression_gridview, null);
//		ExpandGridView gv = (ExpandGridView) view.findViewById(R.id.gridview);
//		List<String> list = new ArrayList<String>();
//		if (i == 1) {
//			List<String> list1 = reslist.subList(0, 20);
//			list.addAll(list1);
//		} else if (i == 2) {
//			list.addAll(reslist.subList(20, reslist.size()));
//		}
//		list.add("delete_expression");
//		final ExpressionAdapter expressionAdapter = new ExpressionAdapter(this, 1, list);
//		gv.setAdapter(expressionAdapter);
//		gv.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				String filename = expressionAdapter.getItem(position);
//				try {
//					// 文字输入框可见时，才可输入表情
//					// 按住说话可见，不让输入表情
//					if (buttonSetModeKeyboard.getVisibility() != View.VISIBLE) {
//
//						if (filename != "delete_expression") { // 不是删除键，显示表情
//							// 这里用的反射，所以混淆的时候不要混淆SmileUtils这个类
//							Class clz = Class.forName("com.easemob.chatuidemo.utils.SmileUtils");
//							Field field = clz.getField(filename);
//							mEditTextContent.append(SmileUtils.getSmiledText(ChatActivity.this, (String) field.get(null)));
//						} else { // 删除文字或者表情
//							if (!TextUtils.isEmpty(mEditTextContent.getText())) {
//
//								int selectionStart = mEditTextContent.getSelectionStart();// 获取光标的位置
//								if (selectionStart > 0) {
//									String body = mEditTextContent.getText().toString();
//									String tempStr = body.substring(0, selectionStart);
//									int i = tempStr.lastIndexOf("[");// 获取最后一个表情的位置
//									if (i != -1) {
//										CharSequence cs = tempStr.substring(i, selectionStart);
//										if (SmileUtils.containsKey(cs.toString()))
//											mEditTextContent.getEditableText().delete(i, selectionStart);
//										else
//											mEditTextContent.getEditableText().delete(selectionStart - 1, selectionStart);
//									} else {
//										mEditTextContent.getEditableText().delete(selectionStart - 1, selectionStart);
//									}
//								}
//							}
//
//						}
//					}
//				} catch (Exception e) {
//				}
//
//			}
//		});
//		return view;
//	}
//
//	public List<String> getExpressionRes(int getSum) {
//		List<String> reslist = new ArrayList<String>();
//		for (int x = 1; x <= getSum; x++) {
//			String filename = "ee_" + x;
//
//			reslist.add(filename);
//
//		}
//		return reslist;
//
//	}
	
	protected void onStop() {
		// unregister this event listener when this activity enters the
		// background
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		EMGroupManager.getInstance().removeGroupChangeListener(groupListener);
		// 注销广播
		try {
			unregisterReceiver(receiver);
			receiver = null;
			
			unregisterReceiver(ackMessageReceiver);
			ackMessageReceiver = null;
			
			unregisterReceiver(deliveryAckMessageReceiver);
			deliveryAckMessageReceiver = null;
			
			unregisterReceiver(cmdMessageReceiver);
			cmdMessageReceiver = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		activityInstance = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		adapter.refresh();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (wakeLock.isHeld())
			wakeLock.release();
		if (VoicePlayClickListener.isPlaying && VoicePlayClickListener.currentPlayListener != null) {
			// 停止语音播放
			VoicePlayClickListener.currentPlayListener.stopPlayVoice();
		}
		
		if (BottleVoicePlayClickListener.isPlaying && BottleVoicePlayClickListener.currentPlayListener != null) {
			// 停止瓶子语音播放
			BottleVoicePlayClickListener.currentPlayListener.stopPlayVoice();
		}
		
		try {
			// 停止录音
			if (voiceRecorder.isRecording()) {
				voiceRecorder.discardRecording();
				recordingContainer.setVisibility(View.INVISIBLE);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 隐藏软键盘
	 */
	private void hideKeyboard() {
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null)
				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * 加入到黑名单
	 * 
	 * @param username
	 */
	private void addUserToBlacklist(final String username) {
		final ProgressDialog pd = new ProgressDialog(this);
		pd.setMessage(getString(R.string.Is_moved_into_blacklist));
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		new Thread(new Runnable() {
			public void run() {
				try {
					EMContactManager.getInstance().addUserToBlackList(username, false);
					runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							showMsg(getResources().getString(R.string.Move_into_blacklist_success));
						}
					});
				} catch (EaseMobException e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							showMsg(getResources().getString(R.string.Move_into_blacklist_failure));
						}
					});
				}
			}
		}).start();
		
		try {
			EMContactManager.getInstance().addUserToBlackList(username, true);
			showMsg( username+getResources().getString(R.string.blacklist_sucessed));
		} catch (EaseMobException e) {
			e.printStackTrace();
			showMsg( getResources().getString(R.string.blacklist_failure));
		}
	}

	/**
	 * 返回
	 * 
	 * @param view
	 */
	public void back(View view) {
		finish();
	}
	/**
	 * 聊天titlebar 更多按钮相应
	 * @param view
	 */
	public void moreFn(View view) {
		showPickDialog();
	}
	
	/**
	 * 选择提示对话框
	 */
	boolean inBlackList = false;
	private void showPickDialog() {
		String[] strarray;
		
		//获取黑名单用户的usernames
		List<String> blackList = EMContactManager.getInstance().getBlackListUsernames();
		if(blackList.contains(toChatUserId)){
			inBlackList = true;
			strarray = new String[] {"举报", "移出拉黑", "取消"};
		}else{
			inBlackList = false;
			strarray = new String[] {"举报", "拉黑", "取消"};
		}
		
		new AlertDialog.Builder(this)
			.setTitle("更多")
			.setItems(strarray, new DialogInterface.OnClickListener() {
				     @Override
				     public void onClick(DialogInterface dialog, int which) {
				    	 if(which == 0){
				    		dialog.dismiss();
				    		jubao(null);
				    	 }else if(which == 1){
				    		dialog.dismiss();
				    		if(inBlackList){
				    			try {
									EMContactManager.getInstance().deleteUserFromBlackList(toChatUserId);
									showMsg("已移出黑名单");
								} catch (EaseMobException e) {
								}
				    		}else{
				    			//第二个参数如果为true，则把用户加入到黑名单后双方发消息时对方都收不到；false,则
				    			//我能给黑名单的中用户发消息，但是对方发给我时我是收不到的
				    			try {
									EMContactManager.getInstance().addUserToBlackList(toChatUserId, false);
									showMsg("已成功将对方加入黑名单，你不会再收到对方的消息");
								} catch (EaseMobException e) {
								}
				    		}
				    	 }else if(which == 2){
				    		 dialog.dismiss();
				    	 }
				     }
			    })
			.show();
	}
	
	TextPromptDialog dialogToBuy;
	public void jubao(View view) {
		if(dialogToBuy==null){
			final TextPromptDialog dialogToBuy = new TextPromptDialog(this, R.style.style_dialog_ballon, "将最近的聊天内容作为附件，提交到举报后台供管理员调查核实。确定举报对方吗？");
			dialogToBuy.show();
			// 设置宽度
			WindowManager windowManager = getWindowManager();
			Display display = windowManager.getDefaultDisplay();
			WindowManager.LayoutParams lp = dialogToBuy.getWindow().getAttributes();
			lp.width = (int) (display.getWidth()); 
			dialogToBuy.getWindow().setAttributes(lp);
			
			dialogToBuy.setOnConfirmClick(new Button.OnClickListener() {
				
				@Override
				public void onClick(View v) {

					dialogToBuy.dismiss();
					//感谢你的举报，管理员将对举报进行核实，根据实际情况做相应处理
					String content = getJuBaoContent();
					if(!TextUtils.isEmpty(content)){
						JsonObject jo = new JsonObject();
						jo.addProperty("userId", UserManager.getInstance(ChatActivity.this).getCurrentUserId());
						jo.addProperty("bUserId", toChatUserId);
						jo.addProperty("reportType", "0");
						jo.addProperty("content", content);
						
						BottleRestClient.post("addNewReport", ChatActivity.this, jo, new AsyncHttpResponseHandler(){
							@Override
							public void onStart() {
								super.onStart();
								showDialog("举报中...", "举报失败...", 15*1000);
							}
							
							@Override
							public void onFinish() {
								super.onFinish();
								closeDialog(mpDialog);
							}
			
							@Override
							public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
								showMsg("感谢你的举报，管理员将对举报进行核实，根据实际情况做相应处理");
							}
			
							@Override
							public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
								showMsg("举报失败");
								closeDialog(mpDialog);
							}
				        });
					}else{
						showMsg("感谢你的举报，管理员将对举报进行核实，根据实际情况做相应处理");
					}
				
				}
			});
			
		}else{
			dialogToBuy.show();
		}
	}
	/**
	 * 举报
	 * @return
	 */
	private String getJuBaoContent(){
		List<EMMessage> msgs = conversation.getAllMessages();
		StringBuffer jbcontent = new StringBuffer();
		String who;
		String content;
		
		for(EMMessage msg : msgs){
			content = "";
			if (msg.getIntAttribute(MyConstants.MESSAGE_ATTR_VIEWTYPE, 0)==0){
				who = msg.direct == EMMessage.Direct.RECEIVE ? "被举报人：":"举报人：";
				
				switch (msg.getType()) {
				case IMAGE:
					ImageMessageBody mb = (ImageMessageBody) msg.getBody();
					content = "<img>"+mb.getRemoteUrl()+"</img>";
					break;
				case VOICE:
					VoiceMessageBody vb = (VoiceMessageBody) msg.getBody();
					content = "<audio>"+vb.getRemoteUrl()+"</audio>";
					break;
				case TXT:
					TextMessageBody tb = (TextMessageBody) msg.getBody();
					content = tb.getMessage();
					break;
				}
				
				jbcontent.append(who);
				jbcontent.append(content);
				jbcontent.append("\n");
			}
		}
		return jbcontent.toString();
	}

	/**
	 * 覆盖手机返回键
	 */
	@Override
	public void onBackPressed() {
		if (more.getVisibility() == View.VISIBLE) {
			if(llContainerBtn.getVisibility()!=View.GONE||expressionContainer.getVisibility()==View.GONE){
				super.onBackPressed();
			}
			more.setVisibility(View.GONE);
			
			iv_emoticons_normal.setVisibility(View.VISIBLE);
			iv_emoticons_checked.setVisibility(View.INVISIBLE);
		} else {
			super.onBackPressed();
		}
	}

	/**
	 * listview滑动监听listener
	 * 
	 */
	private class ListScrollListener implements OnScrollListener {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE:
				/*if (view.getFirstVisiblePosition() == 0 && !isloading && haveMoreData) {
					loadmorePB.setVisibility(View.VISIBLE);
					// sdk初始化加载的聊天记录为20条，到顶时去db里获取更多
					List<EMMessage> messages;
					try {
						// 获取更多messges，调用此方法的时候从db获取的messages
						// sdk会自动存入到此conversation中
						if (chatType == CHATTYPE_SINGLE)
							messages = conversation.loadMoreMsgFromDB(adapter.getItem(0).getMsgId(), pagesize);
						else
							messages = conversation.loadMoreGroupMsgFromDB(adapter.getItem(0).getMsgId(), pagesize);
					} catch (Exception e1) {
						loadmorePB.setVisibility(View.GONE);
						return;
					}
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
					}
					if (messages.size() != 0) {
						// 刷新ui
						adapter.notifyDataSetChanged();
						listView.setSelection(messages.size() - 1);
						if (messages.size() != pagesize)
							haveMoreData = false;
					} else {
						haveMoreData = false;
					}
					loadmorePB.setVisibility(View.GONE);
					isloading = false;

				}*/
				break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		// 点击notification bar进入聊天页面，保证只有一个聊天页面
		String username = intent.getStringExtra("userId");
		if (toChatUserId.equals(username))
			super.onNewIntent(intent);
		else {
			finish();
			startActivity(intent);
		}

	}

	public String getToChatUsername(){
		return toChatUserId;
	}
	
	/**
	 * 检查是当前用户是否可以发消息
	 * @return 如果用户被禁言、单向连通，则返回true，否则返回false
	 */
	private boolean check(){
		String gagTime = MyApplication.getApp().getSpUtil().getGagTime();
		if(!TextUtils.isEmpty(gagTime)){
			MoMaLog.e("禁言时间", gagTime);
			Date dateFrom = new Date(gagTime);
			Date dateTo = new Date(System.currentTimeMillis());//获取当前时间
			int day = MoMaUtil.getGapCount(dateTo, dateFrom);
			
			if(day>0){
				showMsg("经举报并核实，您的言论存在多次违规已被禁言,离解禁还有"+day+"天");
				return true;
			}else{
				MyApplication.getApp().getSpUtil().setGagTime("");
			}
		}
		
		if(mStranger!=null){
			if(mStranger.getState()==1){
				return false;
			}else if(mStranger.getState()==2){
				showMsg("对方删除了瓶子，无法回复");
				return true;
			}else {
				showMsg("等待对方回复");
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 发送礼物：从礼物商城回调的结果，再一次发送给环信
	 * @param extras 礼物商城回调的结果，包括礼物信息，诸如礼物名称、礼物数量、礼物价格、魅力值、礼物接收者，礼物发送者等
	 */
	@TargetApi(value = android.os.Build.VERSION_CODES.HONEYCOMB)
	private void sendMessageOfGift(Bundle extras){
		String giftName =null;//礼物名称
		String thumbUrl = null; //礼物图片的略缩图
		String picUrl = null;//礼物图片
		String fromUserId = null; //送礼物的对象
		String toUserId = null; //礼物接收者
		if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB_MR1){
			giftName = extras.getString("giftName","");
			 thumbUrl= extras.getString("thumbUrl",""); 
			 picUrl = extras.getString("giftUrl","");	
			 fromUserId = extras.getString("userId","");	
			 toUserId = extras.getString("toUserId","");
		}else{
			giftName = (String) extras.get("giftName");
			 thumbUrl= (String) extras.get("thumbUrl"); 
			 picUrl = (String) extras.get("giftUrl");	
			 fromUserId = (String) extras.get("userId");	
			 toUserId = (String) extras.get("toUserId");
		}
		 
		int num = extras.getInt("num", -1);	//礼物数量
		int type = extras.getInt("type",0);	//类型：0表示礼物，1表示道具
		int charm = extras.getInt("charm",-1);//魅力值
		int price = extras.getInt("price",-1);//礼物价格
		
		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.IMAGE);//创建一个发送消息:图片
		message.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, MyApplication.getApp().getSpUtil().getUserHeadImage());	//聊天的头像
		
		// 默认是单聊,群聊不支持送礼物
		if (chatType == CHATTYPE_GROUP){
			showMsg(getResources().getString(R.string.no_support_group));
			return ;
		}
		
		if(type!=0){
			showMsg(getResources().getString(R.string.not_support_property));
			return ;
		}
		if(TextUtils.isEmpty(toUserId)||TextUtils.isEmpty(giftName)||TextUtils.isEmpty(picUrl)||charm<0||price<0){
			showMsg(getResources().getString(R.string.error_platform));
			return ;
		}
		
		if(num<=0){
			showMsg(getResources().getString(R.string.error_num));
			return ;
		}
			
		if(TextUtils.isEmpty(fromUserId)){
			User currentUser = UserManager.getInstance(this).getCurrentUser();
			if(currentUser==null){
				showMsg(getResources().getString(R.string.error_platform));
			}
			fromUserId = currentUser.getUserId();
		}
			
		message.setAttribute("isGift_image", true);	
		message.setAttribute("thumbUrl", thumbUrl);
		message.setAttribute("giftImg",picUrl );
		message.setAttribute("giftName",giftName );
		message.setAttribute("giftPrice",String.valueOf(price*num) );
		message.setAttribute("giftCharm",String.valueOf(charm*num ));
		message.setReceipt(toUserId);
		
		Bitmap gift = BitmapFactory.decodeResource(getResources(), R.drawable.defalutimg); 
		File file =new File(MyApplication.mAppPath+"pictures","gift.png");
		BitmapUtil.writeBitmap(file, gift,false);
		ImageMessageBody body = new ImageMessageBody(file);
		message.addBody(body);
		conversation.addMessage(message);
		
		listView.setAdapter(adapter);
		adapter.refresh();
		listView.setSelection(listView.getCount() - 1);
	}
	/**
	 * 消息重发
	 * @param message 消息实体
	 */
	private void sendMessageOfGift(EMMessage message) {
		// TODO Auto-generated method stub
		try {
			EMChatManager.getInstance().sendMessage(message);
		} catch (EaseMobException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		listView.setAdapter(adapter);
		adapter.refresh();
		listView.setSelection(listView.getCount() - 1);
	}
	
	/**
	 * 添加与好友游戏的操作：挑战者向被挑战者发送的消息
	 * @param extras
	 */
	@TargetApi(value = android.os.Build.VERSION_CODES.HONEYCOMB)
	private void sendMessageOfGame(Bundle extras){
		if(check()){
			return ;
		}
		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.IMAGE);//创建一个发送消息:图片
		
		// 默认是单聊,群聊不能游戏
		if (chatType == CHATTYPE_GROUP){
			showMsg(getResources().getString(R.string.no_support_group));
			return ;
		}
		
		int money = extras.getInt("eachGameMoney",-1);//
		String bottleId = null;
		String toUserId = null;
		String eachGameInfo = null;
		String fromUserId = null;
		if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB_MR1){
			bottleId = extras.getString("bottleId","");
			 toUserId= extras.getString("toUserId","");
			 eachGameInfo = extras.getString("eachGameInfo","");
			 fromUserId = extras.getString("fromUserId","");
		}else{
			bottleId = (String) extras.get("bottleId");
			toUserId = (String) extras.get("toUserId");
			eachGameInfo = (String) extras.get("eachGameInfo");
			fromUserId = (String) extras.get("fromUserId");
		}
		
		if(TextUtils.isEmpty(bottleId)||TextUtils.isEmpty(toUserId)||TextUtils.isEmpty(fromUserId)||TextUtils.isEmpty(eachGameInfo)||money<0){
			showMsg(getResources().getString(R.string.error_platform));
			return ;
		}
			
		message.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, MyApplication.getApp().getSpUtil().getUserHeadImage());	//聊天的头像
		message.setAttribute("isGame_image", true);//
		message.setAttribute("isBeReceived", false);	//是否对方已经接受了应战
		message.setAttribute("deleteIdentifier", UUID.randomUUID().toString()); //唯一标识(用来B应答游戏之后，A收到应答消息，删除A前面发起的游戏消息)
		message.setAttribute("isHaveLooked", false);//游戏发起者是否查看了游戏结果
		message.setAttribute("eachGameMoney", String.valueOf(money)); //游戏金额
		message.setAttribute("eachGameInfo", eachGameInfo); //每局信息
		message.setAttribute("toUsername", toUserId); //被挑战着
		message.setAttribute("sendUserId", fromUserId);//游戏发起者
		message.setAttribute("gameBottleId", bottleId); //
		
		message.setReceipt(toUserId);
		
		Bitmap game = BitmapFactory.decodeResource(getResources(), R.drawable.chat_game); 
		File file =new File(MyApplication.mAppPath+"pictures","game.png");
		BitmapUtil.writeBitmap(file, game,false);
		ImageMessageBody body = new ImageMessageBody(file);
		
		message.addBody(body);
		conversation.addMessage(message);
		
		conversation.getAllMessages();
		sendMessageOfGame(message);
	}
	
	/**
	 * 添加与好友游戏的操作：被接收后，向挑战者发送的消息----透传消息
	 * @param data
	 */
	private void sendMessageOfGame(Intent data){
		if(check()){
			return ;
		}
		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.CMD);//创建一个发送消息：透传
		
		// 默认是单聊,群聊不能游戏
		if (chatType == CHATTYPE_GROUP){
			showMsg(getResources().getString(R.string.no_support_group));
			return ;
		}
		
		int result = data.getIntExtra("result",5);	//012,输平赢 
		String bottleId = data.getStringExtra("bottleId");	
		int money = data.getIntExtra("eachGameMoney",-1);
		String eachGameInfo = data.getStringExtra("eachGameInfo");
		String toUserId = data.getStringExtra("toUsername");
		
		if(TextUtils.isEmpty(bottleId)||TextUtils.isEmpty(toUserId)||TextUtils.isEmpty(eachGameInfo)||money<0||result<0){
			showMsg(getResources().getString(R.string.error_platform));
			return ;
		}
		
		//自定义扩展
		message.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, MyApplication.getApp().getSpUtil().getUserHeadImage());	//聊天的头像
		message.setAttribute("isGame_image", true);//
		message.setAttribute("isBeReceived", true);	//是否对方已经接受了应战
		message.setAttribute("isHaveLooked", false);//游戏发起者是否查看了游戏结果
		message.setAttribute("eachGameMoney", String.valueOf(money)); //游戏金额
		message.setAttribute("eachGameInfo", eachGameInfo); //每局信息
		message.setAttribute("toUsername", toUserId); //被挑战着
		message.setAttribute("sendUserId", toUserId);//游戏挑战者
		message.setAttribute("gameBottleId", bottleId); //
		message.setAttribute("gameResult", String.valueOf(result)); //游戏结果
		message.setAttribute("deleteIdentifier", UUID.randomUUID().toString()); 
		
		//发送给某人
		message.setReceipt(toUserId);
		
		//action可以自定义，在广播接收时可以收到
		String action = MyConstants.UPDATEGAMESTATUS;
		CmdMessageBody cmdBody=new CmdMessageBody(action);
		message.addBody(cmdBody);
		
		try {
			EMChatManager.getInstance().sendMessage(message);
		} catch (EaseMobException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateGameStatus(message);
	}
	
	/**
	 * 添加与好友游戏的操作：挑战者查看游戏结果
	 * @param data
	 */
	private void updateMessageOfGame(Intent data){
		GameOpponentModel model = (GameOpponentModel) data.getSerializableExtra("GameOpponentModel");
		String bottleId = model.getBottleId();
		String bUId = data.getStringExtra("bUId");
		int result = data.getIntExtra("result", 0);
		if(TextUtils.isEmpty(bottleId)){
			showMsg(getResources().getString(R.string.error_platform));
			return ;
		}
		EMMessage msg = getMessageById(bottleId);
		if(msg==null)
			return ;
		msg.setAttribute(MyConstants.MESSAGE_ATTR_HEADIMG, MyApplication.getApp().getSpUtil().getUserHeadImage());	//聊天的头像
		msg.setAttribute("isHaveLooked", true);//游戏发起者是否查看了游戏结果
		msg.setAttribute("gameResult", String.valueOf(result));
		
		conversation.removeMessage(msg.getMsgId());
		EMChatManager.getInstance().importMessage(msg, false);//向数据库中导入数据，但是不加入内存中
		conversation.addMessage(msg);
		listView.setAdapter(adapter);
		adapter.refresh();
		listView.setSelection(listView.getCount() - 1);
		
		if(TextUtils.isEmpty(bUId))
			return ;
		updateGameByUID(bUId,msg);
	}
	/**
	 * 更新本地游戏状态
	 * @param bId
	 */
	public void updateGameStatus(EMMessage message){
		String bottleId = message.getStringAttribute("gameBottleId", "");
		boolean isBeReceived = message.getBooleanAttribute("isBeReceived", false);
		boolean isHaveLooked = message.getBooleanAttribute("isHaveLooked", false);
		String money = message.getStringAttribute("eachGameMoney","0");
		String eachGameInfo = message.getStringAttribute("eachGameInfo", ""); 
		String gameResult = message.getStringAttribute("gameResult","0");
		
		EMMessage msg = getMessageById(bottleId);
		if(msg==null)
			return ;
		
		msg.setAttribute("isBeReceived", isBeReceived);	//是否对方已经接受了应战
		msg.setAttribute("isHaveLooked", isHaveLooked);//游戏发起者是否查看了游戏结果
		msg.setAttribute("eachGameMoney", money); //游戏金额
		msg.setAttribute("eachGameInfo", eachGameInfo); //每局信息
		msg.setAttribute("gameResult", gameResult); 
		msg.setMsgTime(message.getMsgTime());
		msg.status = EMMessage.Status.SUCCESS;
		msg.setUnread(false);
		
		conversation.removeMessage(msg.getMsgId());
		EMChatManager.getInstance().importMessage(msg, false);//向数据库中导入数据，但是不加入内存中
		conversation.addMessage(msg);
		listView.setAdapter(adapter);
		adapter.refresh();
		listView.setSelection(listView.getCount() - 1);
	}
	/**
	 * 根据瓶子的ID，获取会话中的对象
	 * @param bottleId 瓶子ID
	 * @return 如果没有找到符合的对象，则返回null
	 */
	private EMMessage getMessageById(String bottleId) {
		// TODO Auto-generated method stub
		List<EMMessage> messages = conversation.getAllMessages();
		String tmpId = null;
		EMMessage msg = null;
		for(EMMessage tmpMsg:messages){
			tmpId = tmpMsg.getStringAttribute("gameBottleId","");
			if(!TextUtils.isEmpty(tmpId)&&tmpId.equalsIgnoreCase(bottleId)){
				//更新msg
				msg = tmpMsg;
				break;
			}
		}
		return msg;
	}

	public void sendMessageOfGame(EMMessage message){
		try {
			EMChatManager.getInstance().sendMessage(message);
		} catch (EaseMobException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		listView.setAdapter(adapter);
		adapter.refresh();
		listView.setSelection(listView.getCount() - 1);
	}
	
	
	/**
	 * 通过游戏的bUid更新游戏的状态
	 * @param bUid
	 */
	private void updateGameByUID(String bUid,final EMMessage msg){
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("bUId", bUid);
		jo.addProperty("state", "1");
		
		BottleRestClient.post("updateGameState", this, jo, new AsyncHttpResponseHandler(){

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {
				// TODO Auto-generated method stub
				Gson gson =new Gson();
				String str= new String(responseBody);
				BaseModel baseModel = gson.fromJson(str, BaseModel.class);
				if(baseModel != null && !TextUtils.isEmpty(baseModel.getCode())){
					if(!"0".equals(baseModel.getCode())){
						//如果更新失败，重新更新UI
						msg.setAttribute("isHaveLooked", true);
						updateGameStatus(msg);
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				// TODO Auto-generated method stub
				
			}});
		}
	
	public ListView getListView() {
		return listView;
	}

	private void refreshUI() {
	    if(adapter == null){
            return;
        }
	    
		runOnUiThread(new Runnable() {
			public void run() {
				adapter.refresh();
				
			}
		});
	}
}
