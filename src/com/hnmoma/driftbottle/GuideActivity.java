package com.hnmoma.driftbottle;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;



/**
 * 显示顺序：<br/>
 * 1、显示捡瓶子，以及操作提示<br/>
 * 2、显示捡起瓶子的卡片 3、关闭捡瓶子的动画和操作，显示气球，以及操作提示<br/>
 * 4、显示刷瓶子以及操作，关闭上一个页面 5、显示底部导航条以及每个操作
 */
public class GuideActivity extends BaseActivity {
	private final int START = 0; //显示第一页
	private final int PIKCOUT = 1; //显示第二页：捞取
	private final int OTHERBEACH = 2; //显示第三页：显示其他人的沙滩
	private final int BALLON = 3;	// 第四页：显示气球
	private final int PROPER = 4;	//第四页：显示刷频器
	private final int USER = 5; //第四页：显示用户头像
	private final int OK = 6;//显示结束按钮
	
	private int nextStep = -1;
	
	private Timer mTimer;
	private TimerTask mTimerTask;
	
  
    private static int delay = 500;  //500ms   
    private static int period = 2000;  //  默认情况，10ms切换一个场景
	
	private View start,end;
	private ImageView ivPickBottle; //显示捡瓶子的页面
	private ImageView ivOtherBeach; //显示其他人的沙滩页面
	
	private ImageView ivBalloon; //气球
	private ImageView ivProp; //道具
	private ImageView ivCenter; //用户中心
	private ImageView ivCenterTip;//用户中心的提示
	private ImageView ivOK;
	private ImageView ivBallonNormal;
	private ImageView ivPropNormal;
	private ImageView ivUser;
	
	private boolean isPause = false;//timer是否暂停
	private long waitTime = 800L;
	private boolean[] showflag;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		start = findViewById(R.id.guide_start);
		end = findViewById(R.id.guide_end);
		ivPickBottle = (ImageView) findViewById(R.id.guide_midle);
		ivOtherBeach = (ImageView) findViewById(R.id.iv_other_beach);
		ivBalloon = (ImageView) findViewById(R.id.iv_balloon);
		
		ivProp = (ImageView) findViewById(R.id.iv_prop);
		ivCenter = (ImageView) findViewById(R.id.main_kj_new);
		ivCenterTip = (ImageView) findViewById(R.id.iv_center_tip);
		ivOK = (ImageView) findViewById(R.id.iv_ok);
		ivBallonNormal = (ImageView) findViewById(R.id.iv_balloon_p);
		ivPropNormal = (ImageView) findViewById(R.id.iv_prop_n);
		ivUser =  (ImageView) findViewById(R.id.iv_user_n);
		
		start.setVisibility(View.VISIBLE);
		ivPickBottle.setVisibility(View.GONE);
		end.setVisibility(View.GONE);
		ivBallonNormal.setVisibility(View.GONE);
		ivPropNormal.setVisibility(View.GONE);
		
		ivBalloon.setVisibility(View.VISIBLE);
		ivProp.setVisibility(View.GONE);
		ivCenter.setVisibility(View.GONE);
		ivCenterTip.setVisibility(View.GONE);
		ivUser.setVisibility(View.GONE);
		
		showflag = new boolean[7];
		startTimer();
		initGuide();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stopTimer();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		// 屏蔽返回键
//		 super.onBackPressed();
	}

	private void initGuide() {
		// TODO Auto-generated method stub
		findViewById(R.id.btn_start).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//显示捡瓶子的页面
				Message msg = Message.obtain();
				nextStep = PIKCOUT;
				msg.what = nextStep;
				mHandler.sendMessage(msg);
				isPause =true;
				
			}
		});
		ivPickBottle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				nextStep = OTHERBEACH;
				msg.what = nextStep;
				mHandler.sendMessage(msg);
				isPause =true;
			}
		});
		
		ivOtherBeach.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				nextStep = BALLON;
				msg.what = nextStep;
				mHandler.sendMessage(msg);
				isPause =true;
			}
		});
		
		ivBalloon.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				nextStep = PROPER;
				msg.what = nextStep;
				mHandler.sendMessage(msg);
				isPause =true;
			}
		});
		
		ivProp.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Message msg = Message.obtain();
				nextStep = USER;
				msg.what = nextStep;
				mHandler.sendMessage(msg);
				isPause =true;
			}
		});
		
		ivCenter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				nextStep = OK;
				msg.what = nextStep;
				mHandler.sendMessage(msg);
				isPause =true;
			}
		});
		
		findViewById(R.id.iv_ok).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(GuideActivity.this,MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(intent);
				MyApplication.getApp().getSysSpUtil().setIsFirstTime(false);
				finish();
			}
		});
		end.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(nextStep+1>=showflag.length)
					return ;
				Message msg = Message.obtain();
				if(showflag[nextStep]){
					nextStep +=1;
				}else{
					for(int i=nextStep;i>=0;i--){
						if(showflag[i]){
							nextStep= i+1;
							break;
						}
					}
				}
				
				msg.what = nextStep;
				if(nextStep==OK)
					mHandler.sendMessageDelayed(msg, waitTime);
				else
					mHandler.sendMessageDelayed(msg, waitTime);
				isPause =true;
			}
		});
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case START:
				start.setVisibility(View.VISIBLE);
				ivPickBottle.setVisibility(View.GONE);
				end.setVisibility(View.GONE);
				showflag[0]= true;
				break;
			case PIKCOUT:
				ivPickBottle.setVisibility(View.VISIBLE);
				start.setVisibility(View.GONE);
				showflag[1]= true;
				break;
			case OTHERBEACH:
				ivPickBottle.setVisibility(View.GONE);
				start.setVisibility(View.GONE);
				ivOtherBeach.setVisibility(View.VISIBLE);
				showflag[2]= true;
				break;
			case BALLON:
				ivOtherBeach.setVisibility(View.GONE);
				end.setVisibility(View.VISIBLE);
				ivBalloon.setImageResource(R.drawable.ic_guide_balloon_p);
				ivBalloon.setVisibility(View.VISIBLE);
				showflag[3]= true;
				break;
			case PROPER:
				ivBallonNormal.setVisibility(View.VISIBLE);
				ivBalloon.setVisibility(View.GONE);;
				ivProp.setImageResource(R.drawable.ic_guide_prop_p);
				ivProp.setVisibility(View.VISIBLE);
				showflag[4]= true;
				break;
			case USER:
				ivPropNormal.setVisibility(View.VISIBLE);
				ivProp.setVisibility(View.GONE);
				ivCenter.setImageResource(R.drawable.ic_guide_center);
				ivCenterTip.setImageResource(R.drawable.ic_guide_center_tip);
				ivCenter.setVisibility(View.VISIBLE);
				ivCenterTip.setVisibility(View.VISIBLE);
				showflag[5]= true;
				break;
			case OK:
				ivUser.setVisibility(View.VISIBLE);
				ivCenterTip.setVisibility(View.GONE);
				ivCenter.setVisibility(View.GONE);
				ivOK.setVisibility(View.VISIBLE);
				showflag[6]= true;
				stopTimer();
				break;
			default:
				break;
			}
		}
	};
	
	private void startTimer(){  
        if (mTimer == null) {  
            mTimer = new Timer();  
        }  
  
        if (mTimerTask == null) {  
            mTimerTask = new TimerTask() {  
                @Override  
                public void run() {  
                	Message message = Message.obtain();
        			message.what = nextStep;
        			nextStep+=1;
        			if(nextStep==START){
        				mHandler.sendMessageDelayed(message, waitTime);
        			}else
        				mHandler.sendMessage(message);
        			while(isPause){
        				 try {  
                             Thread.sleep(period*6);  
                         } catch (InterruptedException e) {  
                         }
        			}
                }  
            };  
        }  
  
        if(this.mTimer != null && this.mTimerTask != null )  
            mTimer.schedule(mTimerTask, delay, period);  
    }  
  
    private void stopTimer(){  
        if (mTimer != null) {  
            mTimer.cancel();  
            mTimer = null;  
        }  
  
        if (mTimerTask != null) {  
            mTimerTask.cancel();  
            mTimerTask = null;  
        }      
    }  
	
}
