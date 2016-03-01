package com.hnmoma.driftbottle.model;

import java.util.List;

public class KeyWordInfo{
	private String lastVersion;
	private List<SensitiveWord> wordList;
	private List<SensitiveWord> delWordList;
	
	
	public String getLastVersion() {
		return lastVersion;
	}
	public void setLastVersion(String lastVersion) {
		this.lastVersion = lastVersion;
	}
	public List<SensitiveWord> getWordList() {
		return wordList;
	}
	public void setWordList(List<SensitiveWord> wordList) {
		this.wordList = wordList;
	}
	public List<SensitiveWord> getDelWordList() {
		return delWordList;
	}
	public void setDelWordList(List<SensitiveWord> delWordList) {
		this.delWordList = delWordList;
	}
}