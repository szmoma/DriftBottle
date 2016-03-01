package com.hnmoma.driftbottle.util.download;

import java.io.Serializable;

public interface DownloadListener {

	public static class Data implements Serializable {
		private static final long serialVersionUID = 8954447444334039739L;
		private long currentSize;
		private long totalSize;

		public Data() {
		}

		public Data(int currentSize, int totalSize) {
			this.currentSize = currentSize;
			this.totalSize = totalSize;
		}

		public long getCurrentSize() {
			return currentSize;
		}

		public void setCurrentSize(long currentSize) {
			this.currentSize = currentSize;
		}

		public long getTotalSize() {
			return totalSize;
		}

		public void setTotalSize(long totalSize) {
			this.totalSize = totalSize;
		}

		@Override
		public String toString() {
			return "Data [currentSize=" + currentSize + ", totalSize="
					+ totalSize + "]";
		}

	}

	/**
	 * 
	 * @param data
	 *            : transfer downloaded data
	 */
	public void onProgress(Data data);

	/**
	 * 
	 * @param e
	 *            : exception
	 */
	public void onError(Exception e);

	public void onCompleted();
}
