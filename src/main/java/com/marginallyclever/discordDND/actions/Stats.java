package com.marginallyclever.discordDND.actions;

import com.marginallyclever.discordDND.DNDAction;
import com.marginallyclever.discordDND.DNDEvent;

public class Stats extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		event.reply("```"+event.actor.toString()+"```");
	}

	@Override
	public String[] getNames() {
		return new String[] { "stats" };
	}
}
