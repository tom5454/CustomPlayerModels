package com.tom.cpm.shared.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

import com.tom.cpl.math.MathHelper;

public class ModelLoadingPool {
	private static final Executor THREAD_POOL;

	static {
		int i = MathHelper.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, getMaxThreads());
		if (i <= 0) {
			THREAD_POOL = Executors.newSingleThreadExecutor(task -> {
				Thread thread = new Thread(task);
				thread.setName("CPM Background Loading Thread");
				thread.setDaemon(true);
				thread.setUncaughtExceptionHandler(ModelLoadingPool::onThreadException);
				return thread;
			});
		} else {
			AtomicInteger atomicinteger = new AtomicInteger(1);
			THREAD_POOL = new ForkJoinPool(i, p_314383_ -> {
				ForkJoinWorkerThread forkjoinworkerthread = new ForkJoinWorkerThread(p_314383_) {
					@Override
					protected void onTermination(Throwable p_211561_) {
						if (p_211561_ != null) {
							Log.warn(this.getName() + " died", p_211561_);
						} else {
							Log.debug(this.getName() + " shutdown");
						}

						super.onTermination(p_211561_);
					}
				};
				forkjoinworkerthread.setName("CPM Background Loading Pool-" + atomicinteger.getAndIncrement());
				return forkjoinworkerthread;
			}, ModelLoadingPool::onThreadException, true);
		}
	}

	public static Executor workerPool() {
		return THREAD_POOL;
	}

	private static void onThreadException(Thread thr, Throwable ex) {
		Log.error("Exception in background loading thread: " + thr.getName(), ex);
	}

	private static int getMaxThreads() {
		String s = System.getProperty("max.bg.threads");
		if (s != null) {
			try {
				int i = Integer.parseInt(s);
				if (i >= 1 && i <= 255) {
					return i;
				}
			} catch (NumberFormatException numberformatexception) {
				// Logged by minecraft
			}
		}

		return 255;
	}
}
