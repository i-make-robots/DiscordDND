package com.marginallyclever.discorddnd.actions;

import com.marginallyclever.discorddnd.DNDAction;
import com.marginallyclever.discorddnd.DNDEvent;

public class Stats extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		if(event.actor==null) return;
		
		event.reply("```"+event.actor+"```");
	}

	@Override
	public String[] getNames() {
		return new String[] { "stats" };
	}
	
	public String getHelp() {
		return "stats - display all your stats.";
	}
}
