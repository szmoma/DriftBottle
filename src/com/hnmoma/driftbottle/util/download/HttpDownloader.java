package com.hnmoma.driftbottle.util.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpDownloader extends Downloader {

	protected HttpURLConnection connection;

	public HttpDownloader(String source, File destFile) throws FileNotFoundException,
			MalformedURLException {
		super(destFile);
		sourceUrl = new URL(source);
	}

	@Override
	public void download() throws IOException, InterruptedException {
		setupConnection();
		downloadFromStream();
	}

	protected void setupConnection() throws IOException {
		if (connection != null) {
			return;
		}
		connection = (HttpURLConnection) sourceUrl.openConnection();
	}

	@Override
    public InputStream getInputStream() throws IOException {
        setupConnection();
        return connection.getInputStream();
    }

	@Override
    public int totalDownloadSize() {
        return connection.getContentLength();
    }

}
