package com.hnmoma.driftbottle;

import com.hnmoma.driftbottle.adapter.DqAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
/**
 * 用户区域
 * @author Administrator
 *
 */
public class InfomationActivity extends BaseActivity {

	ListView lv;
	DqAdapter dqadt;
	String[] categories;
	String resultString;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resultString = "";
		setContentView(R.layout.activity_infomation);
		lv = (ListView)findViewById(R.id.lv);
		String category = getIntent().getStringExtra("category");
		if("province".equals(category)) {	//省份
			categories = getResources().getStringArray(R.array.province);
			lv.setOnItemClickListener(new MyItemClickListener1());
		} else if("job".equals(category)) {
			categories = getResources().getStringArray(R.array.job); //工作
			lv.setOnItemClickListener(new MyItemClickListener2());
		}
		dqadt = new DqAdapter(this, categories);
		lv.setAdapter(dqadt);
	}
	
	
	class MyItemClickListener1 implements OnItemClickListener{
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
			resultString = dqadt.getItem(position);
			String[] province = getResources().getStringArray(getResouceId(position));
			dqadt = new DqAdapter(InfomationActivity.this, province);
			lv.setAdapter(dqadt);
			lv.setOnItemClickListener(new MyItemClickListener2());
		}
	}
	
	class MyItemClickListener2 implements OnItemClickListener{
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
			if(TextUtils.isEmpty(resultString)) {
				resultString =dqadt.getItem(position);
			} else {
				resultString = resultString + "-" + dqadt.getItem(position);
			}
			
			Intent intent = new Intent();
			intent.putExtra("resultString", resultString);
			setResult(Activity.RESULT_OK, intent);
			InfomationActivity.this.finish();
		}
	}
	
	public int getResouceId(int position){
		int result = 0;
		switch(position){
			case 0:
				result = R.array.北京;
				break;
			case 1:
				result = R.array.上海;
				break;
			case 2:
				result = R.array.天津;
				break;
			case 3:
				result = R.array.重庆;
				break;
			case 4:
				result = R.array.黑龙江;
				break;
			case 5:
				result = R.array.吉林;
				break;
			case 6:
				result = R.array.辽宁;
				break;
			case 7:
				result = R.array.内蒙古;
				break;
			case 8:
				result = R.array.河北;
				break;
			case 9:
				result = R.array.山西;
				break;
			case 10:
				result = R.array.陕西;
				break;
			case 11:
				result = R.array.山东;
				break;
			case 12:
				result = R.array.新疆;
				break;
			case 13:
				result = R.array.西藏;
				break;
			case 14:
				result = R.array.青海;
				break;
			case 15:
				result = R.array.甘肃;
				break;
			case 16:
				result = R.array.宁夏;
				break;
			case 17:
				result = R.array.河南;
				break;
			case 18:
				result = R.array.江苏;
				break;
			case 19:
				result = R.array.湖北;
				break;
			case 20:
				result = R.array.浙江;
				break;
			case 21:
				result = R.array.安徽;
				break;
			case 22:
				result = R.array.福建;
				break;
			case 23:
				result = R.array.江西;
				break;
			case 24:
				result = R.array.湖南;
				break;
			case 25:
				result = R.array.贵州;
				break;
			case 26:
				result = R.array.四川;
				break;
			case 27:
				result = R.array.广东;
				break;
			case 28:
				result = R.array.云南;
				break;
			case 29:
				result = R.array.广西;
				break;
			case 30:
				result = R.array.海南;
				break;
			case 31:
				result = R.array.香港;
				break;
			case 32:
				result = R.array.澳门;
				break;
			case 33:
				result = R.array.台湾;
				break;
		}
		return result;
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_back:
			this.finish();
			break;

		default:
			break;
		}
	}
	
}
