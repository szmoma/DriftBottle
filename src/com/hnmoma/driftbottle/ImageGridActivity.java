package com.hnmoma.driftbottle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.hnmoma.driftbottle.fragment.ImageGridFragment;
/**
 * <p>2015-4-22 yangsy修改Fragment的提交方式。修改理由如下：<br/>
 * 如果commit()方法在Activity的onSaveInstanceState()之后调用，会出错。把commit()方法替换为commitAllowingStateLoss(),效果一样。但commitAllowingStateLoss()方法需要支持包或sdk不低于11<br/>
 * 解决方案的地址：http://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-h</p>
 */
public class ImageGridActivity extends FragmentActivity {

	private static final String TAG = "ImageGridActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        if (BuildConfig.DEBUG) {
//            Utils.enableStrictMode();
//        }
        super.onCreate(savedInstanceState);

        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, new ImageGridFragment(), TAG);
//            ft.commit();
             ft.commitAllowingStateLoss();
        }
    }
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		
		
		
		
	}
	
	
}
