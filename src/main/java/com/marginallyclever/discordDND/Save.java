package com.marginallyclever.discordDND;

public class Save extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		DiscordDND.saveCharacter(event.actorName,event.actor);
		event.reply(event.actorName+", I'll always treasure these moments together.");
	}

	@Override
	public String[] getNames() {
		return new String[] { "save" };
	}
}
