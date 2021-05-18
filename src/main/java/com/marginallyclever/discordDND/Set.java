package com.marginallyclever.discordDND;

public class Set extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		String [] parts = event.message.split("\\s");
		if(parts.length!=3) return;
		String key = getAbbreviation(parts[1]);
		Integer value = Integer.parseInt(parts[2]);
		event.actor.set(key, value);
		event.reply(event.actorName+", your "+key+" is now "+value+".");
	}

	@Override
	public String[] getNames() {
		return new String[] { "set" };
	}
}
