package com.marginallyclever.discorddnd.actions;

import com.marginallyclever.discorddnd.DNDAction;
import com.marginallyclever.discorddnd.DNDEvent;
import com.marginallyclever.discorddnd.DiscordDND;

public class Save extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		if(event.actor==null) return;
		
		DiscordDND.saveCharacter(event.characterName,event.actor);
		event.reply(event.characterName +", I'll always treasure these moments together.");
	}

	@Override
	public String[] getNames() {
		return new String[] { "save" };
	}
	
	public String getHelp() {
		return "save - save your stats";
	}
}
