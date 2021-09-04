package com.tom.cpm.core;

public class CallbackReturnable<R> extends Callback {
	private R returnValue;

	public CallbackReturnable() {
	}

	public CallbackReturnable(R value) {
		returnValue = value;
	}

	public void setReturnValue(R returnValue) {
		this.returnValue = returnValue;
		cancel();
	}

	public R getReturnValue() {
		return this.returnValue;
	}
}
