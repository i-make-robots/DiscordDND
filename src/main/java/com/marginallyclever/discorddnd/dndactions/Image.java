package com.marginallyclever.discorddnd.dndactions;

import com.marginallyclever.discorddnd.DNDAction;
import com.marginallyclever.discorddnd.DNDEvent;

public class Image implements DNDAction {

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
