package com.hnmoma.driftbottle.util.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.hnmoma.driftbottle.util.MoMaLog;


public abstract class Downloader {
	private static final String TAG = "Downloader";
	protected URL sourceUrl;
	private OutputStream outputStream;
	private DownloadListener downloadListener = null;
	private File outputFile;
	private final int BUFFER_SIZE = 1240000;//1M

	// ///////////////////////////
	public abstract InputStream getInputStream() throws IOException;

	public abstract int totalDownloadSize();

	public abstract void download() throws IOException, InterruptedException;

	// ///////////////////////////

	Downloader(File destFile) throws FileNotFoundException,
			MalformedURLException {
		outputFile = destFile;
		outputStream = new FileOutputStream(outputFile);
	}

	public void setProgressListener(DownloadListener downloadListener) {
		this.downloadListener = downloadListener;
	}

	public File getFile() {
		return outputFile;
	}
	
	public void setFile(File file) {
		outputFile = file;
	}

	protected void downloadFromStream() throws IOException,
			InterruptedException {
		MoMaLog.d(TAG, "Downloading from stream");
		InputStream input = null;
		try {
			input = getInputStream();
			throwExceptionIfInterrupted();
			copyInputToOutputStream(getInputStream());
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
				if (input != null) {
					input.close();
				}
			} catch (IOException ioe) {
				// ignore
			}
		}
		// Even if we have completely downloaded the file, we should probably
		// respect
		// the wishes of the user who wanted to cancel us.
		throwExceptionIfInterrupted();
	}

	/**
	 * stop thread
	 * 
	 * @throws InterruptedException
	 */
	private void throwExceptionIfInterrupted() throws InterruptedException {
		if (Thread.interrupted()) {
			MoMaLog.d(TAG, "Received interrupt, cancelling download");
			throw new InterruptedException();
		}
	}

	protected void copyInputToOutputStream(InputStream input)
			throws IOException, InterruptedException {

		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead = 0;
		int totalBytes = totalDownloadSize();
		throwExceptionIfInterrupted();
		sendProgress(bytesRead, totalBytes);
		while (true) {
			int count = input.read(buffer);
			throwExceptionIfInterrupted();
			bytesRead += count;
			sendProgress(bytesRead, totalBytes);
			if (count == -1) {
				MoMaLog.d(TAG, "Finished downloading from stream");
				break;
			}
			outputStream.write(buffer, 0, count);
		}
		outputStream.flush();
	}

	protected void sendProgress(int bytesRead, int totalBytes) {
		if (downloadListener != null) {
			DownloadListener.Data data = new DownloadListener.Data(bytesRead,
					totalBytes);
			downloadListener.onProgress(data);
		}
	}
}
