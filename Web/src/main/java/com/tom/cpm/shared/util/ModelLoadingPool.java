package com.tom.cpm.shared.util;

import java.util.concurrent.Executor;

import com.tom.cpm.web.client.java.AsyncPool;

public class ModelLoadingPool {
	private static final AsyncPool POOL = new AsyncPool();

	public static Executor workerPool() {
		return POOL;
	}

	public static Executor ioPool() {
		return POOL;
	}
}
