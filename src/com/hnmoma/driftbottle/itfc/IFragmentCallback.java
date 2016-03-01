package com.hnmoma.driftbottle.itfc;

import android.os.Bundle;
/**
 * Fragment的回调参数
 * @author Administrator
 *
 */
public interface IFragmentCallback {
	/**
	 * 回调函数
	 * @param arg 返回的结果
	 * @param tag 需要显示哪个fragment
	 */
	public void startDetailFragment(Bundle arg,String tag);
}
