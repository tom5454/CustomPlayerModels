package com.tom.cpm.web.client.java;

import java.util.concurrent.Executor;

import elemental2.promise.Promise;

public class AsyncPool implements Executor {

	@Override
	public void execute(Runnable command) {
		new Promise<>((res, rej) -> command.run());
	}

}
