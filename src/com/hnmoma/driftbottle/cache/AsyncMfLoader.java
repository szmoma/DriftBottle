package com.hnmoma.driftbottle.cache;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;
import com.hnmoma.driftbottle.util.ThreadPoolUtils;

public class AsyncMfLoader {
	private static final String LOGTAG = MoMaUtil.makeLogTag(AsyncMfLoader.class);
	private FileCacheManager imageCacheManager;
	
	public AsyncMfLoader(Context context) {
        /*FTU*/
         imageCacheManager = FileCacheManager.getImageCacheService(context, FileCacheManager.MODE_FIXED_TIMED_USED, "time");
         imageCacheManager.setDelay_millisecond(1 * 24 * 60 * 60 * 1000);
	}

	public void loadMfFile(final Context context, final String url, final MfCallback mfCallback) {
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				mfCallback.mfLoaded((File) message.obj, url);
			}
		};

		ThreadPoolUtils.execute(new Runnable() {
			public void run() {
				File file = loadImageFromCacher(url);
				
//				File drawable = new File(ResourceManager.SdcardPath+"/MagicFace/mymfs/20130510122444.mf");
				
				if(file!=null && file.exists()){
					Message message = handler.obtainMessage(0, file);
					handler.sendMessage(message);
				}
			}
		});
	}
	
	public File loadImageFromCacher(String imageUrl) {
		File mf = null ;
		try {
			mf = imageCacheManager.downlaodMf(new URL(imageUrl));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			MoMaLog.e(LOGTAG, e.getMessage());
		}
		return mf;
	}
	
	public interface MfCallback {
		public void mfLoaded(File mfFile, String mfUrl);
	}
}
