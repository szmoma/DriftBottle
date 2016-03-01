package com.hnmoma.driftbottle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.Button;

import com.hnmoma.driftbottle.business.UserManager;
import com.hnmoma.driftbottle.fragment.PhbPageItem;
import com.viewpagerindicator.TabPageIndicator;
/**
 * hottop榜
 * @author Administrator
 *
 */
public class PhbActivity extends BaseActivity{
	TabPageIndicator indicator;
	TabPageIndicatorAdapter adapter;
	ViewPager pager;
	
	Button btn_mlsc;
//	Button btn_mycf;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//setContentView(R.layout.activity_phb);
		initView();
	}
	
	private void initView(){
		setContentView(R.layout.activity_phb);
		btn_mlsc = (Button) findViewById(R.id.btn_mlsc);
//		btn_mycf = (Button) findViewById(R.id.btn_mycf);
		
		adapter = new TabPageIndicatorAdapter(getSupportFragmentManager());
		pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        
        //实例化TabPageIndicator然后设置ViewPager与之关联
        indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        indicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
//				if (position == 0) {
//					btn_mlsc.setSelected(true);
//					btn_mycf.setSelected(false);
//				} 
//				else if (position == 1) {
//					btn_mlsc.setSelected(false);
//					btn_mycf.setSelected(true);
//				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_back:
				this.finish();
				break;
			case R.id.btn_mlsc:
				// indicator.setCurrentItem(0);
				pager.setCurrentItem(0);
				break;
//			case R.id.btn_mycf:
//				// indicator.setCurrentItem(1);
//				pager.setCurrentItem(1);
//				break;
			case R.id.bt_mlsc:
				if (UserManager.getInstance(this).getCurrentUser() == null) {
					Intent intent1 = new Intent(this, LoginActivity.class);
					intent1.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivity(intent1);
				}  else {
					Intent intent2 = new Intent(this, MlscActivity.class);
					intent2.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					startActivity(intent2);
				}
				break;
		}
	}
	
	class TabPageIndicatorAdapter extends FragmentPagerAdapter {
		
		 String [] titles = new String[] {"热榜"};// "魅力榜","财富榜"
		 PhbPageItem fragment [] = new PhbPageItem[1];

		 public TabPageIndicatorAdapter(FragmentManager fm) {
	         super(fm);
	     }
		 
		 public void updateItem(){
//			 if(fragment[1]!=null){
//				 fragment[1].updateItem();
//			 }
		 }

	     @Override
	     public Fragment getItem(int position) {
	    	 fragment[position] = PhbPageItem.newInstance(position);
	    	 btn_mlsc.setSelected(true);
//			 btn_mycf.setSelected(false);
	         return fragment[position];
	     }

	     @Override
	     public CharSequence getPageTitle(int position) {
	         return titles[position];
	     }

	     @Override
	     public int getCount() {
	         return titles.length;
	     }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
//    	if(requestCode == 400){
//			if(resultCode == Activity.RESULT_OK){
//				int resultMoney = data.getIntExtra("resultMoney", 0);
//				if(resultMoney!=0){
//					tv_ye.setText("扇贝余额：" + resultMoney);
//					indicator.setCurrentItem(1);
//					//刷新我的道具列表
//					adapter.updateItem();
//				}
//			}
//		}
	}
	
	public void reflash(){
	}
}
