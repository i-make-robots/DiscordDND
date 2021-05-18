package com.marginallyclever.discordDND;

public class Get extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		String [] parts = event.message.split("\\s");
		if(parts.length!=2) return;
		String key = getAbbreviation(parts[1]);
		Integer value = event.actor.get(key);
		if(value==null) {
			event.reply(event.actorName+", I don't know what '"+key+"' means...yet.");
		} else {
			event.reply(event.actorName+", your "+key+" is "+value+".");
		}
	}

	@Override
	public String[] getNames() {
		return new String[] { "get" };
	}
}
