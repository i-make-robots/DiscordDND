package com.marginallyclever.discorddnd.dndactions;

import com.marginallyclever.discorddnd.DNDAbbreviationsList;
import com.marginallyclever.discorddnd.DNDAction;
import com.marginallyclever.discorddnd.DNDEvent;

public class Get implements DNDAction {

	@Override
	public void execute(DNDEvent event) {
		if(event.actor==null) return;
		
		String [] parts = event.message.split("\\s");
		if(parts.length!=2) return;
		String key = DNDAbbreviationsList.get(parts[1]);
		Integer value = event.actor.get(key);
		if(value==null) {
			event.reply(event.characterName +", I don't know what '"+key+"' means...yet.");
		} else {
			event.reply(event.characterName +", your "+key+" is "+value+".");
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
