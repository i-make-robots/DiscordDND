package com.marginallyclever.discorddnd.actions;

import com.marginallyclever.discorddnd.DNDAction;
import com.marginallyclever.discorddnd.DNDEvent;

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
