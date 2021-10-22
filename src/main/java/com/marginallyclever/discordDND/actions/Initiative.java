package com.marginallyclever.discordDND.actions;

import java.util.ArrayList;
import java.util.List;

import com.marginallyclever.discordDND.DNDAction;
import com.marginallyclever.discordDND.DNDEvent;

import net.dv8tion.jda.api.entities.Member;

/**
 * Rolls initiative, but doesn't know player's initiative bonus or other effects.
 * @author Dan Royer
 *
 */
public class Initiative extends DNDAction {
	ArrayList<String> names = new ArrayList<String>();
	
	@Override
	public void execute(DNDEvent event) {
		List<Member> members = event.event.getTextChannel().getMembers();
		
		members.forEach((m)->{
			names.add(m.getNickname());
		});
		
		String [] elements = event.message.split("\b");
		if(elements.length>1) {
			try { 
				int extras = Integer.parseInt(elements[1]);
				while(--extras>=0) {
					names.add(Integer.toString(extras+1));
				}
			} catch(Exception e) {}
		}
		
		String msg="initiative: ";
		String add="";
		while(names.size()>0) {
			String n = names.remove((int)(Math.random()*names.size()));
			if(n==null || n.isEmpty()) continue;
			msg+=add+n;
			add=", ";
		}
		
		event.reply(msg);
	}

	@Override
	public String[] getNames() {
		return new String[] { "initiative","i" };
	}
	
	public String getHelp() {
		return "initiative - roll initiative for everyone.";
	}
}
