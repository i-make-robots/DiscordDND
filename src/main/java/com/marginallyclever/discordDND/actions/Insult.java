package com.marginallyclever.discordDND.actions;

import com.marginallyclever.discordDND.DNDAction;
import com.marginallyclever.discordDND.DNDEvent;

public class Insult extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		event.reply(event.actorName+", insult you?  It wouldn't take much but I still won't bother.");	
	}

	@Override
	public String[] getNames() {
		return new String[] { "insult" };
	}
}
