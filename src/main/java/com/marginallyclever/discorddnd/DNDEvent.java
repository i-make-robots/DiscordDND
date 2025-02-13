package com.marginallyclever.discorddnd;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * This class is used to store the message, event, actor name, and actor for the DND actions.
 */
public class DNDEvent {
	public String message;
	public MessageReceivedEvent event;
	public String characterName;
	public Character5e actor;

	public DNDEvent(String message,MessageReceivedEvent event,String characterName) {
		this.message = message;
		this.event = event;
		this.characterName = characterName;
	}
	
	public DNDEvent(String message, MessageReceivedEvent event, String characterName, Character5e character) {
		this.message = message;
		this.event = event;
		this.characterName = characterName;
		this.actor = character;
	}

	public void reply(String str) {
    	System.out.println(">>"+str);
		event.getChannel().sendMessage(str).queue();
    }
}
