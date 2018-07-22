package com.sgchanges.utils;

public class RetryManager {
	
	private static final int FOREVER = 0;
	private int interval;
	private int times;
	private int current;
	
	public RetryManager(int interval, int times) {
		super();
		this.interval = interval;
		this.times = times;
		current = 0;
	}
	
	public boolean hasNext() {
		return times == FOREVER ? true : current < times;
	}
	
	public long getNextWaitTime() {
		current ++;
		return (long)interval*1000l;
	}
	
	public void reset() {
		current = 0;
	}
	
	public int getCurrent() {
		return current;
	}
	
	public int getInterval() {
		return interval;
	}

	public int getTimes() {
		return times;
	}

}
