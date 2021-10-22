package com.marginallyclever.discordDND.actions;

import com.marginallyclever.discordDND.DNDAbbreviationsList;
import com.marginallyclever.discordDND.DNDAction;
import com.marginallyclever.discordDND.DNDEvent;

public class Get extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		if(event.actor==null) return;
		
		String [] parts = event.message.split("\\s");
		if(parts.length!=2) return;
		String key = DNDAbbreviationsList.get(parts[1]);
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
	public String getHelp() {
		return "get [stat] - get the value of [stat].";
	}
}
