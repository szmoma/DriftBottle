package com.hnmoma.driftbottle.custom;  
  
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
  
/** 
 * 自定义TextView,TextView自带了该功能,但功能有限,最重要的是TextView失去焦点的情况下走马灯效果会暂停！ 
 *  
 * @author administrator 
 *  
 */  
public class MyTextView extends TextView{  
    private Text text;  
    Handler handler = new Handler();
    class MyThread implements Runnable{
		@Override
		public void run() {
			 // 1.刷新   
            postInvalidate();  
            // 3.移動   
            text.move(); 
            handler.postDelayed(mt, 150);
		}
    }
    MyThread mt = new MyThread();
    
    int screenWidth;
  
    public MyTextView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    } 
    
    public void init(int width){
    	screenWidth = width;
    }
    
    public void setContent(String content){
    	this.setVisibility(View.VISIBLE);
    	text = new Text(this, content, screenWidth, 30, 10, screenWidth);  
    }
  
    public void startMove() {  
        handler.post(mt);
    }  
    
    public void stopMove() {  
    	handler.removeCallbacks(mt);
    }  
    
    public void destory() {  
    	stopMove();
    	this.setVisibility(View.GONE);
    } 
  
    @Override  
    protected void onDraw(Canvas canvas) { 
    	if(text!=null){
    		 // 背景色   
            canvas.drawColor(Color.TRANSPARENT);  
            // 绘制文字   
            text.draw(canvas);  
    	}
    }  
}  