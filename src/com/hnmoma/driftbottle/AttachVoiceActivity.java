package com.hnmoma.driftbottle;

import java.io.File;

import pl.droidsonroids.gif.GifImageView;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.util.VoiceRecorder;
import com.hnmoma.driftbottle.custom.TextProgressBar;
import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;

public class AttachVoiceActivity extends BaseActivity{
	private static final String LOGTAG = MoMaUtil.makeLogTag(AttachVoiceActivity.class);
	
	TextView tv_title;
	
	protected GifImageView gifview;
	TextProgressBar spinner;
	
	ImageButton ib_01;
	Button bt_right;
	Button bt_cxlz;
	TextView tv_state;
	ImageView iv_voicesb;
	ProgressBar pb;
	
	long start;
	long end;
	
	int length;
	
	File mSampleFile;
	String filePath;
	/**
	 * 是否新附件
	 */
	boolean ifnewAttach;
	
	/**
	 * 按钮状态
	 * 0.录制状态
	 * 1.播放状态
	 * 2.暂停状态
	 */
	int recorder_flag;
	
	private Drawable[] micImages;
	AudioManager audioManager ;
	private PowerManager.WakeLock wakeLock;
	private VoiceRecorder voiceRecorder;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("filePath", filePath);
		outState.putBoolean("ifnewAttach", ifnewAttach);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null){
			filePath = savedInstanceState.getString("filePath");
			ifnewAttach = savedInstanceState.getBoolean("ifnewAttach");
		}
		
		Intent intent = getIntent();
		if(intent != null){
			filePath = intent.getStringExtra("filePath");
			ifnewAttach = intent.getBooleanExtra("ifnewAttach", true);
		}
		audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
		initView();
		initData();
	}
	
	private void initData(){
		wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "driftbottle");
		
        if(ifnewAttach){
        	bt_right.setBackgroundResource(R.drawable.title_btn_ok);
        }else {
        	bt_right.setBackgroundResource(R.drawable.title_btn_mng);
        	bt_cxlz.setVisibility(View.VISIBLE);
        	
        	recorder_flag = 1;
			ib_01.setBackgroundResource(R.drawable.selector_voice_play);
			tv_state.setText("点击播放");
        }
	}
	
	/**
	 * 0正常态
	 * 1录音中
	 * 
	 */
	boolean isRecording;
	
	private void initView(){
		setContentView(R.layout.activity_attach_voice);
		iv_voicesb =  (ImageView) findViewById(R.id.iv_voicesb);
		ib_01 =  (ImageButton) findViewById(R.id.ib_01);
		bt_cxlz =  (Button) findViewById(R.id.bt_cxlz);
		bt_right =  (Button) findViewById(R.id.bt_right);
		tv_state =  (TextView) findViewById(R.id.tv_state);
		pb =  (ProgressBar) findViewById(R.id.pb);
		pb.setMax(60000);
		
		// 动画资源文件,用于录制语音时
		micImages = new Drawable[] { getResources().getDrawable(R.drawable.record_animate_01),
				getResources().getDrawable(R.drawable.record_animate_02), getResources().getDrawable(R.drawable.record_animate_03),
				getResources().getDrawable(R.drawable.record_animate_04), getResources().getDrawable(R.drawable.record_animate_05),
				getResources().getDrawable(R.drawable.record_animate_06), getResources().getDrawable(R.drawable.record_animate_07),
				getResources().getDrawable(R.drawable.record_animate_08), getResources().getDrawable(R.drawable.record_animate_09),
				getResources().getDrawable(R.drawable.record_animate_10), getResources().getDrawable(R.drawable.record_animate_11),
				getResources().getDrawable(R.drawable.record_animate_12), getResources().getDrawable(R.drawable.record_animate_13),
				getResources().getDrawable(R.drawable.record_animate_14), };
		
		voiceRecorder = new VoiceRecorder(micImageHandler);
		
		ib_01.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if(recorder_flag==0){
					if (!MoMaUtil.isExitsSdcard()) {
						showMsg("语音需要sdcard支持！");
						return true;
					}
					//此处判断，是否有录音权限 voiceRecorder.stopRecoding()返回的值是0，此处延迟处理
					
					startRecording();
					isRecording = true;
					return true;
				}
				return false;
			}
		});
		
		ib_01.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(recorder_flag==1){
					startPlaying();
				}else if(recorder_flag==2){
					stopPlaying();
				}
			}
		});
		
		ib_01.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					if(isRecording && recorder_flag == 0){
						isRecording = false;
						MoMaLog.d(LOGTAG, "录制完毕");
						stopRecording();
						handler.removeCallbacks(myThread);
						pb.setProgress(0);
					}
					break;
				}
				return false;
			}
		});
	}
	
	private Handler micImageHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			// 切换msg切换图片
			iv_voicesb.setImageDrawable(micImages[msg.what]);
		}
	};
	
	public void startPlaying(){
		if(!TextUtils.isEmpty(filePath)){//为空说明还未编码完成
			recorder_flag = 2;
			tv_state.setText("点击停止");
			ib_01.setBackgroundResource(R.drawable.selector_record_stop);
			
			mediaPlayer = new MediaPlayer();
////			if (EMChatManager.getInstance().getChatOptions().getUseSpeaker()){
//			audioManager.setMode(AudioManager.MODE_NORMAL);
//			audioManager.setSpeakerphoneOn(true);
//			mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
////			}else{
////				audioManager.setSpeakerphoneOn(false);//关闭扬声器
////				//把声音设定成Earpiece（听筒）出来，设定为正在通话中
////				 audioManager.setMode(AudioManager.MODE_IN_CALL);
////				mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
////			}
			
			if (!audioManager.isWiredHeadsetOn()){
				audioManager.setMode(AudioManager.MODE_NORMAL);
				audioManager.setSpeakerphoneOn(true);
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			}else{
				audioManager.setSpeakerphoneOn(false);//关闭扬声器
				//把声音设定成Earpiece（听筒）出来，设定为正在通话中
				 audioManager.setMode(AudioManager.MODE_IN_CALL);
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
			}
			
			try {
				mediaPlayer.setDataSource(filePath);
				mediaPlayer.prepare();
				mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						// TODO Auto-generated method stub
						mediaPlayer.release();
						mediaPlayer = null;
						stopPlaying();
					}

				});
				isPlaying = true;
				mediaPlayer.start();
			} catch (Exception e) {
			}
			
			//开始录音动画
			MoMaLog.d(LOGTAG, "startPlaying");
		}
	}
	
	
	MediaPlayer mediaPlayer = null;
	public boolean isPlaying = false;
	
	public void stopPlaying(){
		recorder_flag = 1;
		ib_01.setBackgroundResource(R.drawable.selector_voice_play);
		tv_state.setText("点击播放");
		
		// stop play voice
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		isPlaying = false;
		MoMaLog.d(LOGTAG, "stopPlaying");
	}
	
	private void startRecording() {
		start = System.currentTimeMillis();
		handler.post(myThread);
		try {
			
			wakeLock.acquire();
			if (isPlaying)
				stopPlaying();
			voiceRecorder.startRecording(null, "bottle_voice", getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
			if (wakeLock.isHeld())
				wakeLock.release();
			if(voiceRecorder != null)
				voiceRecorder.discardRecording();
			showMsg(getResources().getString(R.string.recoding_fail));
		}
	}
	
	private void stopRecording() {
		end = System.currentTimeMillis();
		
		if (wakeLock.isHeld())
			wakeLock.release();
			// stop recording and send voice file
			try {
				length = voiceRecorder.stopRecoding();
				if (length > 0) {
					recorder_flag = 1;
					ib_01.setBackgroundResource(R.drawable.selector_voice_play);
					filePath = voiceRecorder.getVoiceFilePath();
					bt_cxlz.setVisibility(View.VISIBLE);
					tv_state.setText("点击播放");
					
				}else if(length==EMError.INVALID_FILE){
					showMsg("没有录音权限");
				} else {
					showMsg("录音时间太短");
					recorder_flag = 0;
				}
			} catch (Exception e) {
				e.printStackTrace();
				showMsg("发送失败，请检测服务器是否连接");
			}
	}
	
	
	Handler handler = new Handler();
	MyThread myThread = new MyThread();
	class MyThread extends Thread{
		public void run() {
			end = System.currentTimeMillis();
			long temp = end - start;
			
			pb.setProgress((int) temp);
			
			if(temp >= 60000){
				stopRecording();
				handler.removeCallbacks(myThread);
			}else{
				handler.postDelayed(myThread, 1000);
			}
		}
	}
	
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.bt_back:
				onBackPressed();
				break;
			case R.id.bt_right:
				if(ifnewAttach){
					if(TextUtils.isEmpty(filePath)){
						setResult(RESULT_CANCELED);
					}else{
						Intent intent = new Intent();
						intent.putExtra("filePath", filePath);
						intent.putExtra("fileLength", length);
			        	setResult(RESULT_OK, intent);
					}
					this.finish();
		        }else {
		        	setResult(RESULT_FIRST_USER);
		        	this.finish();
		        }
				break;
			case R.id.bt_cxlz:	//播放按钮
				if (isPlaying) {
					// 停止语音播放
					stopPlaying();
				}
				
				ifnewAttach = true;
				bt_right.setBackgroundResource(R.drawable.title_btn_ok);
				
				recorder_flag=0;
				length=0;
				File file = new File(filePath);
				file.deleteOnExit();
				
				filePath = "";
				tv_state.setText("长按录音");
				ib_01.setBackgroundResource(R.drawable.selector_voice_record);
				
				bt_cxlz.setVisibility(View.GONE);
				break;
		}
	}
	
	@Override
    protected void onResume() {
		super.onResume();
	}
	
	@Override
    protected void onPause() {
		super.onPause();
		
		if (wakeLock.isHeld())
			wakeLock.release();
		if (isPlaying) {
			// 停止语音播放
			stopPlaying();
		}
		
		try {
			// 停止录音
			if (voiceRecorder.isRecording()) {
				handler.removeCallbacks(myThread);
				pb.setProgress(0);
				voiceRecorder.discardRecording();
			}
		} catch (Exception e) {
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}