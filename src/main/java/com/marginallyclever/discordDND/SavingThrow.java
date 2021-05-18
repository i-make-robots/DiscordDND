package com.marginallyclever.discordDND;

public class SavingThrow extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		String [] parts = event.message.split("\\s");
		if(parts.length!=2) return;
		String key = DNDAbbreviationsList.get(parts[1]);
		Integer ability = event.actor.get(key);
		if(ability == null) {
			event.reply(event.actorName+", what's '"+key+"'?");
		} else {
			(new Roll()).roll(event,1,20,1,ability);
		}
	}

	@Override
	public String[] getNames() {
		return new String[] { "savingThrow","st" };
	}
}
