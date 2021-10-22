package com.marginallyclever.discordDND.actions;

import com.marginallyclever.discordDND.DNDAction;
import com.marginallyclever.discordDND.DNDEvent;

public class Stats extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		if(event.actor==null) return;
		
		event.reply("```"+event.actor.toString()+"```");
	}

	@Override
	public String[] getNames() {
		return new String[] { "stats" };
	}
	
	public String getHelp() {
		return "stats - display all your stats.";
	}
}
