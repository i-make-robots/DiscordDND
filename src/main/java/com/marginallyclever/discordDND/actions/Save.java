package com.marginallyclever.discordDND.actions;

import com.marginallyclever.discordDND.DNDAction;
import com.marginallyclever.discordDND.DNDEvent;
import com.marginallyclever.discordDND.DiscordDND;

public class Save extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		if(event.actor==null) return;
		
		DiscordDND.saveCharacter(event.actorName,event.actor);
		event.reply(event.actorName+", I'll always treasure these moments together.");
	}

	@Override
	public String[] getNames() {
		return new String[] { "save" };
	}
	
	public String getHelp() {
		return "save - save your stats";
	}
}
