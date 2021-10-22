package com.marginallyclever.discordDND.actions;

import com.marginallyclever.discordDND.DNDAction;
import com.marginallyclever.discordDND.DNDEvent;

public class Ping extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		event.reply("pong");
	}

	@Override
	public String[] getNames() {
		return new String[] { "ping" };
	}
	
	public String getHelp() {
		return "ping - get back a pong, prove the bot is alive.";
	}
}
