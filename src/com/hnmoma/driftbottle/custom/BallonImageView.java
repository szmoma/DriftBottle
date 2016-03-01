package com.hnmoma.driftbottle.custom;

import com.hnmoma.driftbottle.util.MoMaUtil;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
/*
 * 气球的移动
 */
public class BallonImageView extends ImageView{
	
	Handler handler = new Handler();
    class MyThread implements Runnable{
		@Override
		public void run() {
			move();
            handler.postDelayed(mt, 100);
		}
    }
    MyThread mt = new MyThread();
    private int moveY = MoMaUtil.dip2px(this.getContext(), 50), 
    leftMargin = MoMaUtil.dip2px(this.getContext(), 30), right,
    moveX = leftMargin;
    RelativeLayout.LayoutParams lp;
    /**
     * 0为从左往右
     * 1为从右往左
     */
    int flag;

	public BallonImageView(Context context) {
		super(context);
	}
	
	public BallonImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void init(int width){
		right = width - leftMargin*2;
	}
	
	private void move(){
		if(moveX >= right){
			flag = 1;
		}else if(moveX <= leftMargin){
			flag = 0;
		}
		
		if(flag==0){
			moveX += 1;
		}else{
			moveX -= 1;
		}
		
//		moveY++;
		
		this.layout(moveX, moveY, moveX+this.getWidth(), moveY+this.getHeight());
		if(lp == null){
			lp = (RelativeLayout.LayoutParams) this.getLayoutParams();
		}
		lp.leftMargin = moveX;
	}
	
	public void start() {  
        handler.post(mt);
    }  
	
	public void onPause(){
		handler.removeCallbacks(mt);
	}
	
	public void onResume(){
		handler.post(mt);
	}
	
	public void onDestroy(){
		handler.removeCallbacks(mt);
		handler = null;
		mt = null;
		lp = null;
	}
}
