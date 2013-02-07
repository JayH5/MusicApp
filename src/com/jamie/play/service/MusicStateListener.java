package com.jamie.play.service;

public interface MusicStateListener {
	
	public void onMetaChanged();
	
	public void onPlayStateChanged();
	
	public void onShuffleOrRepeatModeChanged();
	
	public void onRefresh();

}
