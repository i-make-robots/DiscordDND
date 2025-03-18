package com.marginallyclever.discorddnd.dndactions;

import com.marginallyclever.discorddnd.DNDAction;
import com.marginallyclever.discorddnd.DNDEvent;
import com.marginallyclever.discorddnd.DiscordDND;

/**
 * List of all help actions.
 */
public class Help implements DNDAction {
	@Override
	public void execute(DNDEvent event) {
		StringBuilder text = new StringBuilder();
		
		for(DNDAction act : DiscordDND.actions) {
			text.append(DiscordDND.PREFIX)
					.append(act.getHelp())
					.append("\n");
		}
		
		event.reply(
			"```css\n"+text+"\n"
			+"S is text, W is a whole number. Stuff [in brackets] is optional.\n```");
	}

	@Override
	public String[] getNames() {
		return new String[] { "help","h" };
	}
	
	public String getHelp() {
		return "help - you're looking at it.";
	}
}
