package com.marginallyclever.discorddnd.actions;

import com.marginallyclever.discorddnd.DNDAction;
import com.marginallyclever.discorddnd.DNDEvent;
import com.marginallyclever.discorddnd.DiscordDND;

public class Help extends DNDAction {
	@Override
	public void execute(DNDEvent event) {
		String text="";
		
		for(DNDAction act : DiscordDND.actions) {
			text += DiscordDND.PREFIX+act.getHelp()+"\n";
		}
		
		event.reply(
			"```css\n"+text+"\n"
			+"S is a string (text), W is a whole number. Stuff [in brackets] is optional.\n"
			+"Many attributes have abbreviations - for example, i or int will both equal intelligence.```");
	}

	@Override
	public String[] getNames() {
		return new String[] { "help","h" };
	}
	
	public String getHelp() {
		return "help - you're looking at it.";
	}
}
