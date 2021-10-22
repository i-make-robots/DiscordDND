package com.marginallyclever.discordDND.actions;

import com.marginallyclever.discordDND.DNDAbbreviationsList;
import com.marginallyclever.discordDND.DNDAction;
import com.marginallyclever.discordDND.DNDEvent;

public class Add extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		if(event.actor==null) return;
		
		String [] parts = event.message.split("\\s");
		if(parts.length!=3) return;
		String key = DNDAbbreviationsList.get(parts[1]);
		Integer oldValue = event.actor.get(key);
		if(oldValue==null) oldValue=0;
		Integer value = Integer.parseInt(parts[2]);
		Integer newValue = oldValue + value;
		event.actor.set(key, newValue);
		event.reply(event.actorName+", your "+key+" is now "+newValue+".");
	}

	@Override
	public String[] getNames() {
		return new String[] { "add" };
	}
	
	public String getHelp() {
		return "add [stat] [amount] - add [amount] to [stat].";
	}
}
