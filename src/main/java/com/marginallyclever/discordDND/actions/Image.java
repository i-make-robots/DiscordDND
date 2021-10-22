package com.marginallyclever.discordDND.actions;

import com.marginallyclever.discordDND.DNDAction;
import com.marginallyclever.discordDND.DNDEvent;

public class Image extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
		
	}

	@Override
	public String[] getNames() {
		return new String[] { "image" };
	}
	
	public String getHelp() {
		return "image - show a helpful guide to DND.";
	}
}
