package com.hnmoma.driftbottle.custom;  
  
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
  
public class Text {  
    private Paint paint;  
    private String content;//文字内容   
    private float x;//x坐标   
    private float y;//y坐标   
    private float stepX;//移动步长   
    private float contentWidth;//文字宽度   
    private int width;//屏幕宽度
    private int count = 1;//gunjichi
    private MyTextView father;
    
    public Text(MyTextView father, String content, float x, float y, float stepX, int width) {  
    	this.father = father;
        this.x = x;  
        this.y = y;  
        this.stepX = stepX;  
        this.width = width;
        this.content = content;  
        //画笔参数设置   
        paint = new Paint();  
        paint.setColor(Color.WHITE);  
        paint.setTextSize(25f);  
        paint.setAntiAlias(true);
        this.contentWidth = paint.measureText(content);  
    }  
  
    public void move() {  
        x -= stepX;  
        if(x < - contentWidth){//移出屏幕后,从右侧进入   
            x = width;//屏幕宽度,真实情况下应该动态获取,不能写死  
            count ++;
            if(count > 1){
            	father.destory();
            }
        }
    }  
  
    public void draw(Canvas canvas) {  
        canvas.drawText(content, x, y, paint);  
    }  
}  