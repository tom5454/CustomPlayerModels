package com.tom.cpm.retro;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import com.tom.cpl.util.ILogger;

public class MCExecutor {
	private static final ILogger logger = new SysLogger("CPM Executor");
	private static final Queue < FutureTask<? >> scheduledTasks = Queues. < FutureTask<? >> newArrayDeque();
	private static Thread mcThread;
	public static final Executor ex = new Executor() {

		@Override
		public void execute(Runnable command) {
			addScheduledTask(command);
		}
	};

	public static void init() {
		mcThread = Thread.currentThread();
	}

	public static void executeAll() {
		synchronized (scheduledTasks) {
			while (!scheduledTasks.isEmpty()) {
				runTask(scheduledTasks.poll());
			}
		}
	}

	public static <V> V runTask(FutureTask<V> task) {
		try {
			task.run();
			return task.get();
		} catch (ExecutionException executionexception) {
			logger.error("Error executing task", executionexception);
		} catch (InterruptedException interruptedexception) {
			logger.error("Error executing task", interruptedexception);
		}

		return null;
	}

	public static <V> ListenableFuture<V> addScheduledTask(Callable<V> callableToSchedule)
	{
		if (callableToSchedule == null)throw new IllegalArgumentException("Null parameter");

		if (isCallingFromMinecraftThread()) {
			try {
				return Futures.<V>immediateFuture(callableToSchedule.call());
			} catch (Exception exception) {
				return Futures.immediateFailedFuture(exception);
			}
		} else {
			ListenableFutureTask<V> listenablefuturetask = ListenableFutureTask.<V>create(callableToSchedule);

			synchronized (scheduledTasks) {
				scheduledTasks.add(listenablefuturetask);
				return listenablefuturetask;
			}
		}
	}

	public static ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule) {
		if (runnableToSchedule == null)throw new IllegalArgumentException("Null parameter");
		return addScheduledTask(Executors.callable(runnableToSchedule));
	}

	public static boolean isCallingFromMinecraftThread() {
		return Thread.currentThread() == mcThread;
	}

	public static void tell(Runnable runnableToSchedule) {
		if (runnableToSchedule == null)throw new IllegalArgumentException("Null parameter");
		ListenableFutureTask<Object> listenablefuturetask = ListenableFutureTask.create(Executors.callable(runnableToSchedule));
		synchronized (scheduledTasks) {
			scheduledTasks.add(listenablefuturetask);
		}
	}
}
