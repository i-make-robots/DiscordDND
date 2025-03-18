package com.marginallyclever.discorddnd.dndactions;

import java.util.ArrayList;

import com.marginallyclever.discorddnd.DNDAction;
import com.marginallyclever.discorddnd.DNDEvent;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * Rolls initiative, but doesn't know player's initiative bonus or other effects.
 * @author Dan Royer
 *
 */
public class Initiative implements DNDAction {
	ArrayList<String> names = new ArrayList<>();
	
	@Override
	public void execute(DNDEvent event) {
		var channel = event.event.getChannel();
		TextChannel tc = channel.asTextChannel();
		var members = tc.getMembers();
		members.forEach(m -> names.add(m.getNickname()) );

		/**
		 * extra names to add.  good for npcs, monsters, etc.
		 */
		String [] elements = event.message.split("\b");
		if(elements.length>1) {
			try { 
				int extras = Integer.parseInt(elements[1]);
				while(--extras>=0) {
					names.add(Integer.toString(extras+1));
				}
			} catch(Exception e) {}
		}

		StringBuilder sb = new StringBuilder("initiative: ");

		String add="";
		while(!names.isEmpty()) {
			String n = names.remove((int)(Math.random()*names.size()));
			if(n==null || n.isEmpty()) continue;
			sb.append(add).append(n);
			add=", ";
		}
		
		event.reply(sb.toString());
	}

	@Override
	public String[] getNames() {
		return new String[] { "initiative","i" };
	}
	
	public String getHelp() {
		return "initiative - roll initiative for everyone.";
	}
}
