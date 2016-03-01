package com.hnmoma.driftbottle.cache;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.UUID;

import android.graphics.Bitmap;

public class CacheInfo implements Serializable {
	
	private static final long serialVersionUID = -9063235922001045101L;
	private long creatAt = 0L;

	private URL url = null;

	private String fileName = null;

	private long fileSize = 0L;
	private Bitmap value;
	private int usetimes;
	
	private File file;

	CacheInfo() {
	}

	public CacheInfo(URL url, long fileSize, Bitmap value) {
		this.creatAt = System.currentTimeMillis();
		this.usetimes = 0;
		this.fileName = UUID.randomUUID().toString();
		this.url = url;
		this.fileSize = fileSize;
		this.value = value;
	}
	
	public CacheInfo(URL url, long fileSize, File file) {
		this.creatAt = System.currentTimeMillis();
		this.usetimes = 0;
		this.url = url;
		this.fileSize = fileSize;
		this.file = file;
		this.fileName = file.getName();
	}

	public final long getCreatAt() {
		return this.creatAt;
	}

	public URL getUrl() {
		return this.url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setCreatAt(long creatAt) {
		this.creatAt = creatAt;
	}

	public long getFileSize() {
		return this.fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public Bitmap getValue() {
		return this.value;
	}

	public void setValue(Bitmap value) {
		this.value = value;
	}

	public int getUsetimes() {
		return this.usetimes;
	}

	public void setUsetimes(int usetimes) {
		this.usetimes = usetimes;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	
}