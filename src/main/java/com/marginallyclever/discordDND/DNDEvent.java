package com.marginallyclever.discordDND;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DNDEvent {
	public String message;
	public MessageReceivedEvent event;
	public String actorName;
	public Character5e actor;

	public DNDEvent(String message,MessageReceivedEvent event,String actorName) {
		this.message = message;
		this.event = event;
		this.actorName = actorName;
	}
	
	public DNDEvent(String message,MessageReceivedEvent event,String actorName,Character5e actor) {
		this.message = message;
		this.event = event;
		this.actorName = actorName;
		this.actor = actor;
	}

	public void reply(String str) {
    	System.out.println(">>"+str);
		event.getChannel().sendMessage(str).queue();
    }
}
