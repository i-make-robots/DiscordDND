package com.marginallyclever.discorddnd.dndactions;

import com.marginallyclever.discorddnd.DNDAbbreviationsList;
import com.marginallyclever.discorddnd.DNDAction;
import com.marginallyclever.discorddnd.DNDEvent;

public class Set implements DNDAction {

	@Override
	public void execute(DNDEvent event) {
		if(event.actor==null) return;
		
		String [] parts = event.message.split("\\s");
		if(parts.length!=3) return;
		String key = DNDAbbreviationsList.get(parts[1]);
		Integer value = Integer.parseInt(parts[2]);
		event.actor.set(key, value);
		event.reply(event.characterName +", your "+key+" is now "+value+".");
	}

	@Override
	public String[] getNames() {
		return new String[] { "set" };
	}
	
	public String getHelp() {
		return "set [stat] [amount] - set [stat] to [amount].";
	}
}
