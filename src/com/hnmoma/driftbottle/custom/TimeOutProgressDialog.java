package com.hnmoma.driftbottle.custom;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.hnmoma.driftbottle.R;

public class TimeOutProgressDialog extends ProgressDialog {
	
	OnTimeOutListener onTimeOutListener;
	 private long mTimeOut = 0;// 默认timeOut为0即无限大
	 private Timer mTimer = null;// 定时器
	 private boolean isExit; //默认情况下，允许点击back键退出
	 
	 private Handler mHandler = new Handler(){
	        @Override
	        public void handleMessage(Message msg) {
	            if(onTimeOutListener != null){
	            	onTimeOutListener.onTimeOut();
	            }
	        }
	    };
	    
	    private TextView tv_dialog;
	    
		@Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        isExit = true;
	        setContentView(R.layout.dialog_loading);
	        tv_dialog = (TextView) findViewById(R.id.tv_dialog);
	    }

	public TimeOutProgressDialog(Context context) {
		super(context, R.style.MyDialog);
		setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		setIndeterminate(true);// 设置进度条是否为不明确
		setCancelable(isExit);// 设置进度条是否可以按退回键取消
	}
	
	public void setContent(String msg){
		tv_dialog.setText(msg);
	}
	/**
	 * 如果isExit的值是true，则允许返回键关闭对话框
	 * @param isExit
	 */
	public void setExit(boolean isExit) {
		this.isExit = isExit;
		setCancelable(isExit);// 设置进度条是否可以按退回键取消
	}

	public void setTimeOut(long t, OnTimeOutListener timeOutListener) {
        mTimeOut = t;
        if (timeOutListener != null) {
        	this.onTimeOutListener = timeOutListener;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mTimeOut != 0) {
            mTimer = new Timer();
            TimerTask timerTast = new TimerTask() {
                @Override
                public void run() {
                    Message msg = mHandler.obtainMessage();
                    mHandler.sendMessage(msg);
                }
            };
            mTimer.schedule(timerTast, mTimeOut);
        }

    }
    
	 public interface OnTimeOutListener {
		 abstract public void onTimeOut();
	 }
	 
	 @Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		dismiss();
	}
	 
}