package com.hnmoma.driftbottle.cache;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;

import com.hnmoma.driftbottle.MyApplication;
import com.hnmoma.driftbottle.itfc.IDownload;
import com.hnmoma.driftbottle.util.FilesUtils;
import com.hnmoma.driftbottle.util.MoMaLog;

public class FileCacheManager {
	private DBClient dbClient;
	private static FileCacheManager instance = null;
	private Context mContext;
	private FilesUtils filesUtils;
	private int mode = 1;

	private int max_Count = 20;

	private long delay_Millisecond = 259200000L;

	private long max_Memory = 3145728L;
	public static final int MODE_LEAST_RECENTLY_USED = 0;
	public static final int MODE_FIXED_TIMED_USED = 1;
	public static final int MODE_FIXED_MEMORY_USED = 2;
	public static final int MODE_NO_CACHE_USED = 3;

	private FileCacheManager(Context context, int mode, String tag) {
		this.mode = mode;
		this.mContext = context;
		this.filesUtils = new FilesUtils(context);
		if (mode == 0)
			this.dbClient = new DBClient(this.mContext, "least_recently_used", tag);
		else if (mode == 1)
			this.dbClient = new DBClient(this.mContext, "fixed_timed_used", tag);
		else if (mode == 2)
			this.dbClient = new DBClient(this.mContext, "fixed_memory_used", tag);
	}

	public static synchronized FileCacheManager getImageCacheService(Context context, int mode, String tag) {
		if (instance == null) {
			instance = new FileCacheManager(context, mode, tag);
		}
		return instance;
	}

	public Bitmap downlaodImage(URL url) {
		Bitmap bitmap = null;
		CacheInfo cacheInfo = null;
		switch (this.mode) {
		case 0:
			cacheInfo = new LRU(url).execute();
			if (cacheInfo == null)
				break;
			bitmap = cacheInfo.getValue();

			break;
		case 1:
			cacheInfo = new FTU(url).execute();
			if (cacheInfo == null)
				break;
			bitmap = cacheInfo.getValue();

			break;
		case 2:
			cacheInfo = new FMU(url).execute();
			if (cacheInfo == null)
				break;
			bitmap = cacheInfo.getValue();

			break;
		case 3:
			cacheInfo = this.filesUtils.downloadImage(url);
			if (cacheInfo == null)
				break;
			bitmap = cacheInfo.getValue();
		}

		return bitmap;
	}
	
	public File downlaodMf(URL url) {
		File mf = null;
		CacheInfo cacheInfo = new MfFTU(url).execute();
		if(cacheInfo!=null){
			mf = cacheInfo.getFile();
		}
		return mf;
	}

	public void setMax_num(int max_num) {
		this.max_Count = max_num;
	}

	public void setDelay_millisecond(long delay_millisecond) {
		this.delay_Millisecond = delay_millisecond;
	}

	public void setMax_Memory(long max_Memory) {
		this.max_Memory = max_Memory;
	}

	private class FMU implements IDownload {
		URL url = null;

		FMU(URL url) {
			this.url = url;
		}

		public CacheInfo execute() {
			SQLiteDatabase db = FileCacheManager.this.dbClient.getSQLiteDatabase();
//			db.beginTransaction();
			CacheInfo cacheInfo = null;
			try {
				cacheInfo = FileCacheManager.this.dbClient.select(this.url.toString(), db);
				List cacheInfos = FileCacheManager.this.dbClient.selectAll(db);
				if (cacheInfo == null) {
					cacheInfo = FileCacheManager.this.filesUtils.downloadImage(this.url);
					if (cacheInfo == null) {
						return null;
					}
					if (cacheInfo.getFileSize() > FileCacheManager.this.max_Memory) {
						MoMaLog.i(FileCacheManager.this.mContext.getPackageName(),"the image resource"
										+ cacheInfo.getUrl().toString()
										+ " need more  storage than "
										+ FileCacheManager.this.max_Memory+ "B");
					} else {
						if ((cacheInfos != null) && (cacheInfos.size() > 0)) {
							long sumSize = 0L;
							while (cacheInfos.size() > 0) {
								int i = 0;
								for (int size = cacheInfos.size(); i < size; i++) {
									CacheInfo tempCache = (CacheInfo) cacheInfos.get(i);
									sumSize += tempCache.getFileSize();
								}
								if (sumSize + cacheInfo.getFileSize() <= FileCacheManager.this.max_Memory) {
									break;
								}
								CacheInfo deleteCacheInfo = maxSize(cacheInfos);
								if (!FileCacheManager.this.dbClient.delete(deleteCacheInfo.getUrl().toString()))
									continue;
								FileCacheManager.this.filesUtils.deleteImage(deleteCacheInfo.getFileName());
								cacheInfos.remove(deleteCacheInfo);
							}

						}

						if (FileCacheManager.this.dbClient.insert(cacheInfo)){
							FileCacheManager.this.filesUtils.saveImage(cacheInfo.getValue(),cacheInfo.getFileName());
//							db.setTransactionSuccessful(); // 设置事务处理成功
						}
					}
				} else {
					Bitmap bitmap = FileCacheManager.this.filesUtils.readImage(cacheInfo.getFileName());
					if (bitmap != null)
						cacheInfo.setValue(bitmap);
				}
			} finally {
//				db.endTransaction();
				db.close();
			}

			return cacheInfo;
		}

		private CacheInfo maxSize(List<CacheInfo> cacheInfos) {
			long max = ((CacheInfo) cacheInfos.get(0)).getFileSize();
			CacheInfo deleteCache = (CacheInfo) cacheInfos.get(0);
			int i = 0;
			for (int size = cacheInfos.size(); i < size; i++) {
				CacheInfo tempCache = (CacheInfo) cacheInfos.get(i);
				if (tempCache.getFileSize() > max) {
					deleteCache = tempCache;
				}
			}
			return deleteCache;
		}
	}

	private class FTU implements IDownload {
		URL url = null;

		FTU(URL url) {
			this.url = url;
		}

		public CacheInfo execute() {
			CacheInfo cacheInfo = null;
			SQLiteDatabase db = FileCacheManager.this.dbClient.getSQLiteDatabase();
			try {
				cacheInfo = FileCacheManager.this.dbClient.select(this.url.toString(), db);
				if (cacheInfo == null) {
					cacheInfo = FileCacheManager.this.filesUtils.downloadImage(this.url);
					if (cacheInfo == null){
						return null;
					}
					
					if (FileCacheManager.this.dbClient.insert(cacheInfo)){
						FileCacheManager.this.filesUtils.saveImage(cacheInfo.getValue(), cacheInfo.getFileName());
					}
				} else {
					FileCacheManager.this.dbClient.update(System.currentTimeMillis(),this.url.toString(), db);
					Bitmap bitmap = FileCacheManager.this.filesUtils.readImage(cacheInfo.getFileName());
					cacheInfo.setValue(bitmap);
				}
				
//				db.beginTransaction();
				
				List cacheInfos = FileCacheManager.this.dbClient.selectAll(db);
				if (cacheInfos != null) {
					int i = 0;
					for (int size = cacheInfos.size(); i < size; i++) {
						CacheInfo tempCache = (CacheInfo) cacheInfos.get(i);

						if ((tempCache.getCreatAt() + FileCacheManager.this.delay_Millisecond >= System.currentTimeMillis())
								|| (!FileCacheManager.this.dbClient.delete(tempCache.getUrl().toString())))
							continue;
						FileCacheManager.this.filesUtils.deleteImage(tempCache.getFileName());
					}
//					db.setTransactionSuccessful(); // 设置事务处理成功
				}

			} finally {
//				db.endTransaction();
			}

			return cacheInfo;
		}
	}

	private class LRU implements IDownload {
		URL url = null;

		LRU(URL url) {
			this.url = url;
		}

		public CacheInfo execute() {
			SQLiteDatabase db = FileCacheManager.this.dbClient
					.getSQLiteDatabase();
			db.beginTransaction();
			CacheInfo cacheInfo = null;
			try {
				cacheInfo = FileCacheManager.this.dbClient.select(
						this.url.toString(), db);
				List cacheInfos = FileCacheManager.this.dbClient.selectAll(db);
				if (cacheInfo != null) {
					Bitmap bitmap = FileCacheManager.this.filesUtils
							.readImage(cacheInfo.getFileName());
					cacheInfo.setValue(bitmap);
					int i = 0;
					for (int size = cacheInfos.size(); i < size; i++) {
						CacheInfo temp = (CacheInfo) cacheInfos.get(i);
						if (this.url.toString()
								.equals(temp.getUrl().toString()))
							FileCacheManager.this.dbClient.update(0,
									this.url.toString(), db);
						else
							FileCacheManager.this.dbClient.update(temp
									.getUsetimes() + 1, temp.getUrl()
									.toString(), db);
					}
				} else {
					cacheInfo = FileCacheManager.this.filesUtils
							.downloadImage(this.url);
					if (cacheInfo == null)
						return null;
					if ((cacheInfos != null)
							&& (cacheInfos.size() >= FileCacheManager.this.max_Count)) {
						int usetimes = 0;
						CacheInfo deletedCache = (CacheInfo) cacheInfos
								.get(new Random().nextInt(cacheInfos.size()));
						int i = 0;
						for (int size = cacheInfos.size(); i < size; i++) {
							CacheInfo tempCache = (CacheInfo) cacheInfos.get(i);
							if (tempCache.getUsetimes() > usetimes) {
								usetimes = tempCache.getUsetimes();
								deletedCache = tempCache;
							}
						}
						if (FileCacheManager.this.dbClient.delete(deletedCache.getUrl().toString())) {
							FileCacheManager.this.filesUtils
									.deleteImage(deletedCache.getFileName());
							if (FileCacheManager.this.dbClient.insert(cacheInfo)) {
								FileCacheManager.this.filesUtils.saveImage(
										cacheInfo.getValue(),
										cacheInfo.getFileName());
							}
						}

					} else if (FileCacheManager.this.dbClient.insert(cacheInfo)) {
						FileCacheManager.this.filesUtils.saveImage(
								cacheInfo.getValue(), cacheInfo.getFileName());
					}
				}
			} finally {
				db.endTransaction();
			}
			db.endTransaction();

			return cacheInfo;
		}
	}
	
	
	
	
	private class MfFTU implements IDownload {
		URL url = null;

		MfFTU(URL url) {
			this.url = url;
		}

		public CacheInfo execute() {
			CacheInfo cacheInfo = null;
			SQLiteDatabase db = FileCacheManager.this.dbClient.getSQLiteDatabase();
			try {
				cacheInfo = FileCacheManager.this.dbClient.select(this.url.toString(), db);
				if (cacheInfo == null) {
					cacheInfo = FileCacheManager.this.filesUtils.downloadMf(this.url);
					if (cacheInfo == null){
						return null;
					}
					
					FileCacheManager.this.dbClient.insert(cacheInfo);
				} else {
					File file = new File(MyApplication.mAppPath+"/netmfs/"+cacheInfo.getFileName());
					if(file.exists()){
						FileCacheManager.this.dbClient.update(System.currentTimeMillis(),this.url.toString(), db);
						cacheInfo.setFile(file);
					}else{
						FileCacheManager.this.dbClient.delete(url.toString());
						cacheInfo = FileCacheManager.this.filesUtils.downloadMf(this.url);
						if (cacheInfo == null){
							return null;
						}
						FileCacheManager.this.dbClient.insert(cacheInfo);
					}
				}
				
				
				List cacheInfos = FileCacheManager.this.dbClient.selectAll(db);
				if (cacheInfos != null) {
					int i = 0;
					for (int size = cacheInfos.size(); i < size; i++) {
						CacheInfo tempCache = (CacheInfo) cacheInfos.get(i);

						if ((tempCache.getCreatAt() + FileCacheManager.this.delay_Millisecond >= System.currentTimeMillis()) || (!FileCacheManager.this.dbClient.delete(tempCache.getUrl().toString())))
							continue;
						FileCacheManager.this.filesUtils.deleteFile(tempCache.getFileName());
					}
				}

			} finally {
//				db.endTransaction();
			}

			return cacheInfo;
		}
	}
}