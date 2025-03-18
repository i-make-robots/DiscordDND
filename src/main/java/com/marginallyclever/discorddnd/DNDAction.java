package com.marginallyclever.discorddnd;

public interface DNDAction {
	/**
	 * @return a list of names that can be used to call this action.
	 */
	String [] getNames();

	/**
	 * @return a description of what this action does and how to use it.
	 */
	String getHelp();

	/**
	 * Execute the action and report results to the {@link DNDEvent}.
	 */
	void execute(DNDEvent event);
}
