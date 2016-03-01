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
package com.hnmoma.driftbottle.itfc;

import java.io.File;
import java.util.UUID;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.easemob.chat.EMMessage;
import com.hnmoma.driftbottle.R;
import com.hnmoma.driftbottle.business.BottleMsgManager;
import com.hnmoma.driftbottle.model.BottleModel;
import com.hnmoma.driftbottle.model.BottleMsgAttach;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class BottleVoicePlayClickListener implements View.OnClickListener {

	EMMessage message;
	ImageButton voiceIconView;

	private AnimationDrawable voiceAnimation = null;
	MediaPlayer mediaPlayer = null;
	ProgressBar spinner;
	Activity activity;

	public static boolean isPlaying = false;
	public static BottleVoicePlayClickListener currentPlayListener = null;
	static EMMessage currentMessage = null;

	BottleModel bottleModel;
	BottleMsgAttach bottleMsgAttach;

	/**
	 * 
	 * @param message
	 * @param v
	 * @param iv_read_status
	 * @param context
	 * @param activity
	 * @param user
	 * @param chatType
	 */
	public BottleVoicePlayClickListener(Activity activity, EMMessage message, BottleModel bottleModel, ImageButton v, ProgressBar spinner) {
		this.message = message;
		this.spinner = spinner;
		this.voiceIconView = v;
		this.activity = activity;
		this.bottleModel = bottleModel;
		
		//捡瓶子阅读界面
		if(message.getMsgId().equals("10000")){
			//瓶子消息
			bottleMsgAttach = new BottleMsgAttach();
			bottleMsgAttach.setMsgId(message.getMsgId());
			bottleMsgAttach.setMessage(bottleModel.getContent());
			bottleMsgAttach.setDownloaded(false);
			//文件保存路径
			String fileName = UUID.randomUUID().toString()+".amr";
			File cacheDir = StorageUtils.getOwnCacheDirectory(activity, "driftbottle/voice");// 获取到缓存的目录地址
			bottleMsgAttach.setLocalUrl(cacheDir.getPath()+"/"+fileName);
			bottleMsgAttach.setRemoteUrl(bottleModel.getUrl());
		}else{
			bottleMsgAttach = BottleMsgManager.getInstance(activity).getBottleMsgAttachById(message.getMsgId());
			if(bottleMsgAttach==null){
				saveBottleMsgAttach();
			}
		}
	}

	public void stopPlayVoice() {
		voiceAnimation.stop();
		voiceIconView.setImageResource(R.drawable.chatfrom_voice_playing);
		
		// stop play voice
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		isPlaying = false;
	}

	public void playVoice(String filePath) {
		if (!(new File(filePath).exists())) {
			return;
		}
		AudioManager audioManager = (AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);
		mediaPlayer = new MediaPlayer();
		if (!audioManager.isWiredHeadsetOn()  ){
			audioManager.setMode(AudioManager.MODE_NORMAL);
			audioManager.setSpeakerphoneOn(true);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			
		}else{
			audioManager.setSpeakerphoneOn(false);//关闭扬声器
			//把声音设定成Earpiece（听筒）出来，设定为正在通话中
			 audioManager.setMode(AudioManager.MODE_IN_CALL);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
					stopPlayVoice(); // stop animation
				}

			});
			isPlaying = true;
			currentPlayListener = this;
			currentMessage = message;
			mediaPlayer.start();
			showAnimation();
		} catch (Exception e) {
		}
	}

	// show the voice playing animation
	private void showAnimation() {
		// play voice, and start animation
		voiceIconView.setImageResource(R.anim.voice_from_icon);
		voiceAnimation = (AnimationDrawable) voiceIconView.getDrawable();
		voiceAnimation.start();
	}

	@Override
	public void onClick(View v) {

		if (isPlaying) {
			currentPlayListener.stopPlayVoice();
			if (currentMessage != null && currentMessage.hashCode() == message.hashCode()) {
				currentMessage = null;
				return;
			}
		}
			
			if(bottleMsgAttach.getDownloaded()){
//				File file = new File(bottleMsg.getLocalUrl());
				File file = new File(bottleMsgAttach.getLocalUrl());
				if (file.exists() && file.isFile()){
//					playVoice(bottleMsg.getLocalUrl());
					playVoice(bottleMsgAttach.getLocalUrl());
				}else{
					System.err.println("file not exist");
				}
				
			}else{
				//下载文件
				AsyncHttpClient client = new AsyncHttpClient();
				client.get(activity,bottleMsgAttach.getRemoteUrl(), new AsyncHttpResponseHandler(){
					@Override
					public void onStart() {
						// TODO Auto-generated method stub
						super.onStart();
						File file = new File(bottleMsgAttach.getLocalUrl());
						MoMaUtil.delOutTimeFile(file.getParent(), 1000*60*60*24*10);
					}
					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
						// TODO Auto-generated method stub
						spinner.setVisibility(View.GONE);
						voiceIconView.setVisibility(View.VISIBLE);
						bottleMsgAttach.setDownloaded(true);
						bottleMsgAttach.setMessage(new String(responseBody));;
						//捡瓶子阅读界面
						if(!message.getMsgId().equals("10000")){
							BottleMsgManager.getInstance(activity).updateBottleMsg(bottleMsgAttach);	
						}
						MoMaUtil.writeFile(bottleMsgAttach.getLocalUrl(), responseBody);
						playVoice(bottleMsgAttach.getLocalUrl());
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,	byte[] responseBody, Throwable error) {
						// TODO Auto-generated method stub
						spinner.setVisibility(View.GONE);
						voiceIconView.setVisibility(View.VISIBLE);
//						MoMaLog.d("address", bottleMsgAttach.getRemoteUrl()+" "+bottleMsgAttach.getLocalUrl());
						Toast.makeText(activity, "语音获取失败: "+new String(responseBody), Toast.LENGTH_SHORT).show();
					}
					
				});
				
				spinner.setVisibility(View.VISIBLE);
				voiceIconView.setVisibility(View.GONE);
			}
	}
	
	private void saveBottleMsgAttach(){
		//添加瓶子消息
		if(bottleModel.getContentType().equals("5000")){//文字
			//瓶子消息
			bottleMsgAttach = new BottleMsgAttach();
			bottleMsgAttach.setMsgId(message.getMsgId());
			bottleMsgAttach.setMessage(bottleModel.getContent());
		}else if(bottleModel.getContentType().equals("5001")){//图片
			//瓶子消息
			bottleMsgAttach = new BottleMsgAttach();
			bottleMsgAttach.setMsgId(message.getMsgId());
			bottleMsgAttach.setMessage(bottleModel.getContent());
			
			bottleMsgAttach.setThumbUrl(bottleModel.getShortPic());
			bottleMsgAttach.setRemoteUrl(bottleModel.getUrl());
			
		}else if(bottleModel.getContentType().equals("5002")){//贺卡
			//瓶子消息
			bottleMsgAttach = new BottleMsgAttach();
			bottleMsgAttach.setMsgId(message.getMsgId());
			bottleMsgAttach.setMessage(bottleModel.getContent());
			
			bottleMsgAttach.setThumbUrl(bottleModel.getShortPic());
			bottleMsgAttach.setRemoteUrl(bottleModel.getRedirectUrl());
		}else if(bottleModel.getContentType().equals("5003")){//视频
		}else if(bottleModel.getContentType().equals("5004")){//语音
			//瓶子消息
			bottleMsgAttach = new BottleMsgAttach();
			bottleMsgAttach.setMsgId(message.getMsgId());
			bottleMsgAttach.setMessage(bottleModel.getContent());
			bottleMsgAttach.setDownloaded(false);
			
			//文件保存路径
			String fileName = UUID.randomUUID().toString()+".amr";
			File cacheDir = StorageUtils.getOwnCacheDirectory(activity, "driftbottle/voice");// 获取到缓存的目录地址
			
			bottleMsgAttach.setLocalUrl(cacheDir.getPath()+"/"+fileName);
			bottleMsgAttach.setRemoteUrl(bottleModel.getUrl());
		}
		
		BottleMsgManager.getInstance(activity).addBottleMsgAttach(bottleMsgAttach);
	}

	interface OnVoiceStopListener {
		void onStop();
		void onStart();
	}
}