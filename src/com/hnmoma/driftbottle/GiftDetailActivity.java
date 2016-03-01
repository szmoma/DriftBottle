package com.hnmoma.driftbottle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.custom.BuyDialog;
import com.hnmoma.driftbottle.model.ChangeGiftModel;
import com.hnmoma.driftbottle.model.StoreModel;
import com.hnmoma.driftbottle.net.BottleRestClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 礼品详情
 * @author Administrator
 *
 */
public class GiftDetailActivity extends BaseActivity {
		
    //轮播图图片数量  
    int image_count;  
    //自动轮播的时间间隔  
    private final static int TIME_INTERVAL = 4;  
    //自定义轮播图的资源  
    private List<String> imageUrls;  
    //放轮播图片的ImageView 的list  
    private List<ImageView> imageViewsList;  
    //放圆点的View的list  
    private List<View> dotViewsList;  
    //当前轮播页  
    private int currentItem  = 0; 
    
    protected ImageLoader imageLoader = ImageLoader.getInstance();
    DisplayImageOptions options;
    
    private ViewPager viewPager;
    TextView giftName;
    TextView descript;
    TextView price;
    TextView number;
    Button btn_exchange;
    //定时任务  
    private ScheduledExecutorService scheduledExecutorService;  
    StoreModel gift;
    int giftMoney;
    //Handler  
    private Handler handler = new Handler(){  
  
        @Override  
        public void handleMessage(Message msg) {  
            // TODO Auto-generated method stub  
            super.handleMessage(msg);  
            viewPager.setCurrentItem(currentItem);  
        }  
          
    };  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_giftdetail);
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.defalutimg)
		.showImageForEmptyUri(R.drawable.defalutimg)
		.showImageOnFail(R.drawable.defalutimg)
		.cacheInMemory(true)
		.cacheOnDisc(true).considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565).build();
		
		gift = (StoreModel) getIntent().getSerializableExtra("gift");
		giftMoney = getIntent().getIntExtra("giftMoney", 0);
		imageViewsList = new ArrayList<ImageView>();  
        dotViewsList = new ArrayList<View>();  
        imageUrls =gift.getPicUrl();
        image_count = Integer.parseInt(gift.getPicNum());
        
        btn_exchange = (Button) findViewById(R.id.btn_exchange);
        viewPager = (ViewPager) findViewById(R.id.viewPager); 
        giftName = (TextView) findViewById(R.id.tv_giftname);
        descript = (TextView) findViewById(R.id.tv_content);
        price = (TextView) findViewById(R.id.tv_price);
        number = (TextView) findViewById(R.id.tv_population);
        initView();
        startPlay();
	}
	
	
	/** 
     * 初始化Views等 
     */  
    private void initView(){  
        if(imageUrls == null || imageUrls.size() == 0)  
            return;  
          
        LinearLayout dotLayout = (LinearLayout)findViewById(R.id.dotLayout);  
        dotLayout.setVisibility(View.GONE);
        dotLayout.removeAllViews();  
          
        // 热点个数与图片个数相等  
        for (int i = 0; i < imageUrls.size(); i++) {  
            ImageView view =  new ImageView(this);  
            view.setTag(imageUrls.get(i));  
            view.setScaleType(ScaleType.FIT_XY);  
            imageViewsList.add(view);  
              
            View dotView =  new View(this);  
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(16,16);  
            params.leftMargin = 12;  
            params.rightMargin = 12;  
            dotLayout.addView(dotView, params);  
            dotViewsList.add(dotView);  
        }  
        
        giftName.setText(gift.getCsName());
        price.setText(gift.getPrice()+"个礼券");
        descript.setText(gift.getDescript());
        number.setText(gift.getChangeNum()+"人兑换");
        
        viewPager.setFocusable(true);  
        viewPager.setAdapter(new MyPagerAdapter());  
        viewPager.setOnPageChangeListener(new MyPageChangeListener());  
    }
	
    
    class MyPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return imageViewsList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}
    	
		@Override  
        public void destroyItem(View container, int position, Object object) {  
            ((ViewPager)container).removeView(imageViewsList.get(position));  
        }
		
		@Override  
        public Object instantiateItem(View container, int position) {  
            ImageView imageView = imageViewsList.get(position);  
            imageLoader.displayImage(imageView.getTag() + "", imageView, options);  
              
            ((ViewPager)container).addView(imageViewsList.get(position));  
            return imageViewsList.get(position);  
        }  
		
    }
		
    class MyPageChangeListener implements OnPageChangeListener{

    	boolean isAutoPlay = false;  
		@Override
		public void onPageScrollStateChanged(int arg0) {
			 switch (arg0) {  
	            case 1:// 手势滑动，空闲中  
	                isAutoPlay = false;  
	                break;  
	            case 2:// 界面切换中  
	                isAutoPlay = true;  
	                break;  
	            case 0:// 滑动结束，即切换完毕或者加载完毕  
	                // 当前为最后一张，此时从右向左滑，则切换到第一张  
	                if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1 && !isAutoPlay) {  
	                    viewPager.setCurrentItem(0);  
	                }  
	                // 当前为第一张，此时从左向右滑，则切换到最后一张  
	                else if (viewPager.getCurrentItem() == 0 && !isAutoPlay) {  
	                    viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);  
	                }  
	                break;  
	        }  
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageSelected(int pos) {
			currentItem = pos;  
            for(int i=0;i < dotViewsList.size();i++){  
                if(i == pos){  
                    ((View)dotViewsList.get(pos)).setBackgroundResource(R.drawable.dot_foucs);  
                }else {  
                    ((View)dotViewsList.get(i)).setBackgroundResource(R.drawable.dot_normal);  
                }  
            } 
			
		}
    	
    }
    
    public void onClick(View v) {
    	switch (v.getId()) {
		case R.id.bt_back:
			onBackPressed();
			this.finish();
			break;
		case R.id.btn_exchange:
//			if(gift.getType() == 0) {
//				final BuyDialog buyDialog = new BuyDialog(this, R.style.style_dialog_ballon);
//				buyDialog.show();
//				
//				WindowManager windowManager = getWindowManager();
//				Display display = windowManager.getDefaultDisplay();
//				WindowManager.LayoutParams lp = buyDialog.getWindow().getAttributes();
//				lp.width = (int)(display.getWidth()); //设置宽度
//				buyDialog.getWindow().setAttributes(lp);
//				
//				buyDialog.setContent(gift.getShortPic(), gift.getCsName()+"\n\n价格: "+ gift.getPrice()+"个礼券");
//				buyDialog.setOnSubmitClick(new OnClickListener() {
//					
//					@Override
//					public void onClick(View v) {
//						buyDialog.dismiss();
//						doSubmit();
//					}
//				});
			
//			} else  {
				if(giftMoney >= gift.getPrice()) {
					if(gift.getType() == 2 ) {
						final BuyDialog dialogToBuy = new BuyDialog(this, R.style.style_dialog_ballon, "确定");
						dialogToBuy.show();
						
						WindowManager windowManager = getWindowManager();
						Display display = windowManager.getDefaultDisplay();
						WindowManager.LayoutParams lp = dialogToBuy.getWindow().getAttributes();
						lp.width = (int)(display.getWidth()); //设置宽度
						dialogToBuy.getWindow().setAttributes(lp);
						
						dialogToBuy.setContent(gift.getShortPic(), getResources().getString(R.string.ask_submit));
						dialogToBuy.setOnSubmitClick(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								dialogToBuy.dismiss();
								btn_exchange.setClickable(false);
								showMsg(getResources().getString(R.string.tv_converting));
								doSubmit();
							}
						});
					} else {
						Intent intent = new Intent(this, OrderActivity.class);
						intent.putExtra("csId", gift.getCsId());
						intent.putExtra("type", gift.getType());
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
						startActivityForResult(intent, 1001);
					}
				}  else {
					Toast toast = Toast.makeText(this, getResources().getString(R.string.tip_charm_not_enough), Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
//			}
			break;
		}
    }
    
    private void doSubmit() {
		JsonObject jo = new JsonObject();
		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
		jo.addProperty("csId", gift.getCsId());
		jo.addProperty("deviceId", getDeviceId());
		BottleRestClient.post("changeGift", this, jo, new AsyncHttpResponseHandler() {
					@Override
					public void onStart() {
					// TODO Auto-generated method stub
					super.onStart();
					showDialog("正在提交...", "提交失败...", 15*1000);
					}
					@Override
					public void onSuccess(int statusCode, Header[] headers,byte[] responseBody) {
						String str = new String(responseBody);
						if (!TextUtils.isEmpty(str)) {
							Gson gson = new Gson();
							ChangeGiftModel baseModel = gson.fromJson(str, ChangeGiftModel.class);
							if (baseModel != null && !TextUtils.isEmpty(baseModel.getCode())) {
								if ("0".equals(baseModel.getCode())) {
										//Log.d("魅力商城返回了几条数据 ", baseModel.getChangeGift() + "条");
								    	giftMoney = Integer.parseInt(baseModel.getGiftMoney());
								    	showMsg(getResources().getString(R.string.tv_converted));
								}
							} else {
								showMsg(getResources().getString(R.string.tv_convert_failure));
							}
						} else {
							showMsg(getResources().getString(R.string.tv_convert_failure));
						}
						btn_exchange.setClickable(true);
					}
					@Override
					public void onFinish() {
					// TODO Auto-generated method stub
					super.onFinish();
						closeDialog(mpDialog);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						showMsg(getResources().getString(R.string.tv_convert_failure));
						btn_exchange.setClickable(true);
					}

				});
		
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultData, Intent data) {
    	
    	super.onActivityResult(requestCode, resultData, data);
    	if(requestCode == 1001) {
    		if(resultData == RESULT_OK) {
    			giftMoney = Integer.parseInt(data.getStringExtra("remaining"));
    		}
    	}
    	
    }
    
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	Intent intent = new Intent();
    	intent.putExtra("remaining", giftMoney);
    	setResult(RESULT_OK, intent);
    	super.onBackPressed();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	destoryBitmaps();
    }
    
//    private void doSubmit() {
//		JsonObject jo = new JsonObject();
//		jo.addProperty("userId", UserManager.getInstance(this).getCurrentUserId());
//		jo.addProperty("csId", gift.getCsId());
//		jo.addProperty("deviceId", MyApplication.deviceId);
//		jo.addProperty("name", "");
//		jo.addProperty("phone", "");
//		jo.addProperty("address", "");
//		BottleRestClient.post("changeGift",this, jo,new AsyncHttpResponseHandler() {
//					@Override
//					public void onSuccess(int statusCode, Header[] headers,byte[] responseBody) {
//						String str = new String(responseBody);
//						if (!TextUtils.isEmpty(str)) {
//							Gson gson = new Gson();
//							ChangeGiftModel baseModel = gson.fromJson(str,
//									ChangeGiftModel.class);
//							if (baseModel != null
//									&& !TextUtils.isEmpty(baseModel.getCode())) {
//								if ("0".equals(baseModel.getCode())) {
//										//Log.d("魅力商城返回了几条数据 ", baseModel.getChangeGift() + "条");
//										showMsg("兑换成功！");
//										
//								}
//							} else {
//								showMsg("兑换失败！");
//							}
//						} else {
//							showMsg("兑换失败！");
//						}
//					}
//
//					@Override
//					public void onFailure(int statusCode, Header[] headers,
//							byte[] responseBody, Throwable error) {
//						showMsg("兑换失败！");
//					}
//
//				});
//		
//	}
//    
    /** 
     * 开始轮播图切换 
     */  
    private void startPlay(){  
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();  
        scheduledExecutorService.scheduleAtFixedRate(new SlideShowTask(), 1, TIME_INTERVAL, TimeUnit.SECONDS);  
    }  
    /** 
     * 停止轮播图切换 
     */  
    private void stopPlay(){  
        scheduledExecutorService.shutdown();  
    }  
    
    /** 
     *执行轮播图切换任务 
     * 
     */  
    private class SlideShowTask implements Runnable{  
  
        @Override  
        public void run() {  
            synchronized (viewPager) {  
                currentItem = (currentItem+1)%imageViewsList.size();  
                handler.obtainMessage().sendToTarget();  
            }  
        }  
          
    }  
      
    /** 
     * 销毁ImageView资源，回收内存 
     *  
     */  
    private void destoryBitmaps() {  
  
        for (int i = 0; i < image_count; i++) {  
            ImageView imageView = imageViewsList.get(i);  
            Drawable drawable = imageView.getDrawable();  
            if (drawable != null) {  
                //解除drawable对view的引用  
                drawable.setCallback(null);  
            }  
        }  
    }  
	
}
