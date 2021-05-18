package com.marginallyclever.discordDND;

public class Subtract extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		String [] parts = event.message.split("\\s");
		if(parts.length!=3) return;
		String key = DNDAbbreviationsList.get(parts[1]);
		Integer oldValue = event.actor.get(key);
		if(oldValue==null) oldValue=0;
		Integer value = Integer.parseInt(parts[2]);
		Integer newValue = oldValue - value;
		event.actor.set(key, newValue);
		event.reply(event.actorName+", your "+key+" is now "+newValue+".");
	}

	@Override
	public String[] getNames() {
		return new String[] { "subtract","sub" };
	}
}
