package com.marginallyclever.discorddnd.actions;

import com.marginallyclever.discorddnd.DNDAbbreviationsList;
import com.marginallyclever.discorddnd.DNDAction;
import com.marginallyclever.discorddnd.DNDEvent;

public class Subtract extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		if(event.actor==null) return;
		
		String [] parts = event.message.split("\\s");
		if(parts.length!=3) return;
		String key = DNDAbbreviationsList.get(parts[1]);
		Integer oldValue = event.actor.get(key);
		if(oldValue==null) oldValue=0;
		Integer value = Integer.parseInt(parts[2]);
		Integer newValue = oldValue - value;
		event.actor.set(key, newValue);
		event.reply(event.characterName +", your "+key+" is now "+newValue+".");
	}

	@Override
	public String[] getNames() {
		return new String[] { "subtract","sub" };
	}
	
	public String getHelp() {
		return "sub [stat] [amount] - sub [amount] from [stat].";
	}
}
