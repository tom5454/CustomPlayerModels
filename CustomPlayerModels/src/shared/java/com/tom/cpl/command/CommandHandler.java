package com.tom.cpl.command;

import com.tom.cpl.text.IText;
import com.tom.cpm.shared.CommandCPM;

public interface CommandHandler<S> {
	public void register(LiteralCommandBuilder builder);
	public String toStringPlayer(Object pl);
	public void sendSuccess(S sender, IText text);
	public void sendFail(S sender, IText text);

	public default void register() {
		CommandCPM.register(this);
	}
}
