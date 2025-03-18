package com.marginallyclever.discorddnd.dndactions;

import com.marginallyclever.discorddnd.DNDAbbreviationsList;
import com.marginallyclever.discorddnd.DNDAction;
import com.marginallyclever.discorddnd.DNDEvent;

public class Add implements DNDAction {

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
		event.reply(event.characterName +", your "+key+" is now "+newValue+".");
	}

	@Override
	public String[] getNames() {
		return new String[] { "add" };
	}
	
	public String getHelp() {
		return "add [stat] [amount] - add [amount] to [stat].";
	}
}
