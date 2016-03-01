//package com.hnmoma.driftbottle.business;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import android.content.Context;
//import android.view.Gravity;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.View.OnKeyListener;
//import android.view.ViewGroup.LayoutParams;
//import android.view.ext.SatelliteMenu;
//import android.widget.PopupWindow;
//import android.widget.TextView;
//
//import com.hnmoma.driftbottle.R;
//import com.hnmoma.driftbottle.util.MoMaUtil;
//
//public class QiPaoManager {
//
//	Context globalContext;
//	PopupWindow pw = null;
//	TextView qp_cnt;
//	List<String> msgList = new ArrayList<String>();
//	private static volatile QiPaoManager INSTANCE;
//	private static Object INSTANCE_LOCK = new Object();
//	
//	public static QiPaoManager getInstance(Context paramContext){
//	    if (INSTANCE == null)
//	      synchronized (INSTANCE_LOCK){
//	    	  INSTANCE = new QiPaoManager();
//	    	  INSTANCE.init(paramContext);
//	      }
//	    return INSTANCE;
//	  }
//
//	  private void init(Context paramContext) {
//	    this.globalContext = paramContext;
//	    LayoutInflater factory = LayoutInflater.from(globalContext);
//		View view = factory.inflate(R.layout.view_qipao, null);
//		view.findViewById(R.id.qp_btn).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				nextMsgQiPao();
//			}
//		});
//		view.setFocusable(true); // 这个很重要  
//		view.setFocusableInTouchMode(true);
//		
//		qp_cnt = (TextView) view.findViewById(R.id.qp_cnt);
//		
//		pw = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);  
//		pw.setFocusable(true);  
//		pw.setOutsideTouchable(true);  
////		pw.setBackgroundDrawable(new BitmapDrawable()); 
//		
//		// 重写onKeyListener  
////		view.setOnKeyListener(new OnKeyListener() {  
////		    @Override  
////		    public boolean onKey(View v, int keyCode, KeyEvent event) {  
////		        if (keyCode == KeyEvent.KEYCODE_BACK) {  
////		        	nextMsgQiPao();
////		            return true;  
////		        }  
////		        return false;  
////		    }  
////		});  
//	  }
//	  
//	  public void showMsgQiPao(SatelliteMenu menu, String[] msgs){
//		  	for(String s : msgs){
//				msgList.add(s);
//			}
//		  	
//		  	if(!pw.isShowing()){  
//				pw.showAtLocation(menu.getXiaoBa(), Gravity.BOTTOM|Gravity.LEFT, MoMaUtil.dip2px(globalContext, 50), MoMaUtil.dip2px(globalContext, 60) + pw.getHeight());
//			}
//			
//			nextMsgQiPao();
//		  
////			LayoutInflater factory = LayoutInflater.from(this);
////			final View textEntryView = factory.inflate(R.layout.view_qipao, null);
////			pw = new PopupWindow(textEntryView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
////	        
////			pw.setFocusable(true);  
////			pw.setOutsideTouchable(false);  
////			pw.setBackgroundDrawable(new BitmapDrawable());  
////	          
//////	        int[] location = new int[2];  
//////	        menu.getXiaoBa().getLocationOnScreen(location);  
//////	        pw.showAtLocation(menu, Gravity.NO_GRAVITY, location[0]+100, location[1]-pw.getHeight());  
////////	        pw.showAtLocation(menu, Gravity.NO_GRAVITY, location[0]+menu.getXiaoBa().getWidth(), location[1]);  
////////	        pw.showAsDropDown(menu.getXiaoBa());
////			
//////			pw.update(); 
//////			pw.setBackgroundDrawable(new BitmapDrawable()); 
////			pw.showAtLocation(menu.getXiaoBa(), Gravity.BOTTOM|Gravity.LEFT, MoMaUtil.dip2px(this, 50), MoMaUtil.dip2px(this, 60) + pw.getHeight()); //设置layout在PopupWindow中显示的位置 
//		}
//		
//		/**
//		 * 下一条信息
//		 * @param msg
//		 */
//		public void nextMsgQiPao(){
//			if(msgList.size()!=0){
//				qp_cnt.setText(msgList.get(0));
//				msgList.remove(0);
//			}else{
//				closeMsgQiPao();
//			}
//		}
//		
//		/**
//		 * 关闭信息气泡
//		 * @param msg
//		 */
//		public void closeMsgQiPao(){
//			pw.dismiss();  
//			qp_cnt.setText("");
//		}
//}
