package com.tom.cpl.command;

import com.tom.cpl.text.IText;
import com.tom.cpm.shared.CommandCPM;
import com.tom.cpm.shared.CommandCPMClient;

public interface CommandHandler<S> {
	public void register(LiteralCommandBuilder builder, boolean isOp);
	public String toStringPlayer(Object pl);
	public void sendSuccess(S sender, IText text);

	public default void registerCommon() {
		CommandCPM.register(this);
	}

	public default void registerClient() {
		CommandCPMClient.register(this);
	}
}
