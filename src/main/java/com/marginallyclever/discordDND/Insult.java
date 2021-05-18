package com.marginallyclever.discordDND;

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
