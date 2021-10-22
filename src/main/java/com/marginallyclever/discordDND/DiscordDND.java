package com.marginallyclever.discordDND;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.login.LoginException;

import com.marginallyclever.discordDND.actions.Add;
import com.marginallyclever.discordDND.actions.Get;
import com.marginallyclever.discordDND.actions.Help;
import com.marginallyclever.discordDND.actions.Image;
import com.marginallyclever.discordDND.actions.Initiative;
import com.marginallyclever.discordDND.actions.Insult;
import com.marginallyclever.discordDND.actions.Ping;
import com.marginallyclever.discordDND.actions.Roll;
import com.marginallyclever.discordDND.actions.Save;
import com.marginallyclever.discordDND.actions.SavingThrow;
import com.marginallyclever.discordDND.actions.Set;
import com.marginallyclever.discordDND.actions.Stats;
import com.marginallyclever.discordDND.actions.Subtract;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


/**
 * This document was used to start the app and is out of date:
 * https://medium.com/discord-bots/making-a-basic-discord-bot-with-java-834949008c2b
 * 
 * This was used to generate the unique private token (which must never be put into the Github repo).
 * https://www.writebots.com/discord-bot-token/
 * @author Dan Royer
 */
public class DiscordDND extends ListenerAdapter {
	static final String MY_ENTITY_ID = "698232700024127528";
	static final String MY_ENTITY_NAME = "Simply DND 5E";
	
	static public final String PREFIX = "~";

	private Map<String,Character5e> actors = new HashMap<>();
	static public ArrayList<DNDAction> actions = new ArrayList<>();
	
    public static void main( String[] args ) throws LoginException {
        System.out.println("Hello World!");

        String token = readAllBytesFromFile(DiscordDND.class.getResource("token.txt"));
        JDA jda = JDABuilder.createDefault(token).build();
        jda.addEventListener(new DiscordDND());
    }

    private static String readAllBytesFromFile(URL filePath) {
        String content = "";
        try {
        	System.out.println("Token search: "+filePath.toURI());
            content = new String ( Files.readAllBytes( Paths.get( filePath.toURI() ) ) );
        }  catch (Exception e) {
            e.printStackTrace();
		}
        return content;
    }
    
	public DiscordDND() {
		actions.add(new Help());
		actions.add(new Get());
		actions.add(new Set());
		actions.add(new Add());
		actions.add(new Subtract());
		actions.add(new Image());
		actions.add(new Initiative());
		actions.add(new Insult());
		actions.add(new Ping());
		actions.add(new Roll());
		actions.add(new Save());
		actions.add(new SavingThrow());
		actions.add(new Stats());
	}
    
    private Character5e loadActor(String actorId,String actorName) {
		// Is this actor loaded in memory?
		if(actors.get(actorId)==null) {
			// No.  Does it exist on disk?
			if(iHaveSeenCharacterBefore(actorName)==false) {
				System.out.println(actorName + " is new to me...");
				// No.  Make a new one.
	    		Character5e newCharacter = new Character5e();
				saveCharacter(actorName,newCharacter);
			} else {
	    		// Yes.  Load it.
	    		Character5e actorLoaded = loadCharacter(actorName);
	    		// Make sure we remember it for next time.
	   			actors.put(actorId, actorLoaded);
			}
		}
		return actors.get(actorId);
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
		// Ignore me when I talk to myself.  Probably bad advice philosophically but prevents an infinite loop.  
    	if(event.getAuthor().isBot()) return;

    	String message = event.getMessage().getContentDisplay();
    	
    	// does it have the prefix?
    	if(!message.startsWith(PREFIX)) return;
    	// remove the prefix and continue
    	message = message.substring(PREFIX.length());

    	DNDEvent dndEvent;
    	if(event.isFromType(ChannelType.PRIVATE)) {
    		System.out.println("private");
	    	String actorId = event.getAuthor().getId();
	    	String actorName = event.getAuthor().getName();
	    	
	    	dndEvent = new DNDEvent(message,event,actorName);
    	} else {
	    	String actorName = event.getMember().getNickname();
	    	String actorId = event.getAuthor().getId();
	    	//System.out.println(actorName+"@"+actorId+"<<"+message);
	    	Character5e actor = loadActor(actorId,actorName);
	    	
	    	dndEvent = new DNDEvent(message,event,actorName,actor);
    	}
    	
    	String [] parts = message.split(" ");
    	for( DNDAction act : actions ) {
    		for(String name : act.getNames()) {
    			if(name.contentEquals(parts[0])) {
    				act.execute(dndEvent);
    				return;
    			}
    		}
    	}
    	
    	// whois [name] tells any notes about that actor?
    	// add [name] to list of actors
    	// note [name] [string] - adds note to actor
    	
    	super.onMessageReceived(event);
    }

	static public String actorNameToFileName(String actorName) {
		return actorName+".5e";
	}
    		
	static public Character5e loadCharacter(String actorName) {
		Character5e actor = null;
        try {
            FileInputStream fileOut = new FileInputStream(actorNameToFileName(actorName));
            ObjectInputStream objectOut = new ObjectInputStream(fileOut);
            actor = (Character5e)objectOut.readObject();
            objectOut.close();
            System.out.println(actorName+" loaded.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return actor;
	}
	
	static public void saveCharacter(String actorName,Character5e actor) {
        try {
            FileOutputStream fileOut = new FileOutputStream(actorNameToFileName(actorName));
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(actor);
            objectOut.close();
            System.out.println(actorName+" saved.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	private boolean iHaveSeenCharacterBefore(String actorName) {
		File f = new File(actorNameToFileName(actorName));
		return f.exists();
	}
}
