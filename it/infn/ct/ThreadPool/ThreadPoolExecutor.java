package it.infn.ct.ThreadPool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutor extends java.util.concurrent.ThreadPoolExecutor {
	static final int defaultCorePoolSize = 5;
	static final int defaultMaximumPoolSize = 10;
	static final long defaultKeepAliveTime = 10;
	static final TimeUnit defaultTimeUnit = TimeUnit.MINUTES;
	static final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
	private static ThreadPoolExecutor instance;

	private ThreadPoolExecutor() {
		super(defaultCorePoolSize, defaultMaximumPoolSize, defaultKeepAliveTime, defaultTimeUnit, workQueue);
	}

	synchronized static ThreadPoolExecutor getInstance() {
		if (instance == null) {
			instance = new ThreadPoolExecutor();
		}
		return instance;
	}

}
