package com.marginallyclever.discordDND;

public class Ping extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		event.reply("pong");
	}

	@Override
	public String[] getNames() {
		return new String[] { "ping" };
	}
}
