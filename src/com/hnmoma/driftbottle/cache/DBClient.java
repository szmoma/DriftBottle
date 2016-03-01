package com.hnmoma.driftbottle.cache;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hnmoma.driftbottle.util.MoMaLog;
import com.hnmoma.driftbottle.util.MoMaUtil;

public class DBClient extends SQLiteOpenHelper {
	private String modeName;
	
	private SQLiteDatabase db;
	
	private static final String LOGTAG = MoMaUtil.makeLogTag(DBClient.class);

	public DBClient(Context context, String modeName, String tag) {
		super(context, context.getPackageName() + ".cache", null, 1);
		this.modeName = (modeName + tag);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table " + this.modeName + "  (_id integer primary key autoincrement,cache_url varchar(50), create_time integer, usetimes integer,cache_filename varchar(50),cache_size integer)");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	synchronized boolean insert(CacheInfo cacheInfo) {
		db = getWritableDatabase();
		
		try {
			db.execSQL("insert into "
							+ this.modeName
							+ "(cache_url,create_time,usetimes,cache_filename,cache_size) values (?,?,?,?,?)",
					new Object[] { cacheInfo.getUrl().toString(),
							Long.valueOf(cacheInfo.getCreatAt()),
							Integer.valueOf(cacheInfo.getUsetimes()),
							cacheInfo.getFileName(),
							Long.valueOf(cacheInfo.getFileSize()) });
		} catch (SQLException e) {
			MoMaLog.e(LOGTAG, e.getMessage());
			return false;
		}
		return true;
	}

	synchronized boolean update(int usetimes, String url, SQLiteDatabase db) {
		db = getWritableDatabase();
		
		try {
			db.execSQL("update " + this.modeName
					+ " set usetimes=? where cache_url='" + url + "'",
					new Object[] { Integer.valueOf(usetimes) });
		} catch (SQLException e) {
			MoMaLog.e(LOGTAG, e.getMessage());
			db.close();
			return false;
		}
		db.close();
		return true;
	}
	
	synchronized boolean updateOther(int usetimes, String url, SQLiteDatabase db) {
		db = getWritableDatabase();
		
		try {
			db.execSQL("update " + this.modeName
					+ " set usetimes=? where cache_url not in('" + url + "')",
					new Object[] { Integer.valueOf(usetimes) });
		} catch (SQLException e) {
			MoMaLog.e(LOGTAG, e.getMessage());
			db.close();
			return false;
		}
		db.close();
		return true;
	}

	synchronized boolean update(long createTime, String url, SQLiteDatabase db) {
		db = getWritableDatabase();
		
		try {
			db.execSQL("update " + this.modeName
					+ " set create_time=? where cache_url='" + url + "'",
					new Object[] { Long.valueOf(createTime) });
		} catch (SQLException e) {
			MoMaLog.e(LOGTAG, e.getMessage());
			db.close();
			return false;
		}
		db.close();
		return true;
	}

	synchronized CacheInfo select(String url, SQLiteDatabase db) {
		String sql = "select cache_url,create_time,usetimes,cache_filename,cache_size from " + this.modeName + " where cache_url='" + url + "'";
		
		if(!db.isOpen()){
			db = getReadableDatabase();
		}
		
		Cursor cursor = db.rawQuery(sql, null);
		if ((cursor != null) && (cursor.getCount() > 0)) {
			cursor.moveToFirst();
			CacheInfo cacheInfo = new CacheInfo();
			try {
				cacheInfo.setUrl(new URL(cursor.getString(0)));
			} catch (MalformedURLException e) {
				MoMaLog.e(LOGTAG, e.getMessage());
				return null;
			}
			cacheInfo.setCreatAt(cursor.getLong(1));
			cacheInfo.setUsetimes(cursor.getInt(2));
			cacheInfo.setFileName(cursor.getString(3));
			cacheInfo.setFileSize(cursor.getLong(4));
			cursor.close();
			db.close();
			return cacheInfo;
		}
		
		db.close();
		return null;
	}

	synchronized boolean delete(String url) {
		try {
			db = getWritableDatabase();
			db.execSQL("delete from " + this.modeName + " where cache_url='" + url + "'");
		} catch (SQLException e) {
//			e.printStackTrace();
			MoMaLog.e(LOGTAG, e.getMessage());
			db.close();
			return false;
		}
		db.close();
		return true;
	}

	synchronized List<CacheInfo> selectAll(SQLiteDatabase db) {
		
		if(!db.isOpen()){
			db = getReadableDatabase();
		}
		
		Cursor cursor = db.rawQuery("select cache_url,create_time,usetimes,cache_filename,cache_size from " + this.modeName, null);
		if ((cursor != null) && (cursor.getCount() > 0)) {
			List cacheInfos = new ArrayList();
//			cursor.moveToFirst();
			while (cursor.moveToNext()) {
				CacheInfo cacheInfo = new CacheInfo();
				try {
					cacheInfo.setUrl(new URL(cursor.getString(0)));
				} catch (MalformedURLException e) {
//					e.printStackTrace();
					MoMaLog.e(LOGTAG, e.getMessage());
					db.close();
					return null;
				}
				cacheInfo.setCreatAt(cursor.getLong(1));
				cacheInfo.setUsetimes(cursor.getInt(2));
				cacheInfo.setFileName(cursor.getString(3));
				cacheInfo.setFileSize(cursor.getLong(4));
				cacheInfos.add(cacheInfo);
			}
			cursor.close();
			db.close();
			return cacheInfos;
		}
		db.close();
		return null;
	}

	synchronized SQLiteDatabase getSQLiteDatabase() {
		return getWritableDatabase();
	}
}