package com.hnmoma.driftbottle.util.download;

import java.io.IOException;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.download.DownloadListener.Data;

public class DownloadManager extends Handler {

	private DownloadThread downloadThread;
	private Downloader downloader;
	private DownloadListener listener;

	private static final int MSG_PROGRESS = 1;
	private static final int MSG_DOWNLOAD_COMPLETE = 2;
	private static final int MSG_DOWNLOAD_CANCELLED = 3;
	private static final int MSG_ERROR = 4;

	public DownloadManager(Downloader downloader, DownloadListener listener) {
		this.downloader = downloader;
		this.listener = listener;
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
		case MSG_PROGRESS: {
			DownloadListener.Data data = (Data) msg.obj;
			if (listener != null) {
				listener.onProgress(data);
			}
			break;
		}
		case MSG_DOWNLOAD_COMPLETE: {
			if (listener != null) {
				listener.onCompleted();
			}
			break;
		}
		case MSG_DOWNLOAD_CANCELLED: {
			break;
		}
		case MSG_ERROR: {
			Exception e = (Exception) msg.obj;
			if (listener != null) {
				listener.onError(e);
			}
			break;
		}
		default:
			return;
		}
	}

	public void download() {
		downloadThread = new DownloadThread();
		downloadThread.start();
	}

	private class DownloadThread extends Thread implements DownloadListener {

		private static final String TAG = "DownloadThread";

		public void run() {
			try {
				downloader.setProgressListener(this);
				downloader.download();
				sendMessage(MSG_DOWNLOAD_COMPLETE);
			} catch (InterruptedException e) {
				sendMessage(MSG_DOWNLOAD_CANCELLED);
			} catch (IOException e) {
				MoMaLog.e(TAG, e.getMessage() + ": " + Log.getStackTraceString(e));
				Message message = new Message();
				message.what = MSG_ERROR;
				message.obj = e;
				sendMessage(message);
			}
		}

		private void sendMessage(int messageType) {
			Message message = new Message();
			message.what = messageType;
			DownloadManager.this.sendMessage(message);
		}

		private void sendMessage(Message msg){
			DownloadManager.this.sendMessage(msg);
		}
		@Override
		public void onProgress(Data data) {
			// TODO
			Message message = new Message();
			message.what = MSG_PROGRESS;
			message.obj =data;
			DownloadManager.this.sendMessage(message);
		}

		@Override
		public void onError(Exception e) {
			// 在这里没有任何用，异常被捕获了
			// do nothing
		}

		@Override
		public void onCompleted() {
			// do nothing
		}
	}
}
