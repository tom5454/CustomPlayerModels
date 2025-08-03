package com.tom.cpm.web.client.java;

import java.util.concurrent.Executor;

import elemental2.promise.Promise;

public class AsyncPool implements Executor {

	@Override
	public void execute(Runnable command) {
		Promise.resolve((Void) null).then(ignored -> {
			command.run();
			return null;
		});
	}

}
