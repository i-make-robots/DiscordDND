package com.marginallyclever.discorddnd;

public abstract class DNDAction {
	abstract public String [] getNames();

	abstract public String getHelp();
	
	abstract public void execute(DNDEvent event);
}
