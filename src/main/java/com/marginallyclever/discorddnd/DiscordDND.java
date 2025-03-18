package com.marginallyclever.discorddnd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import com.marginallyclever.discordbot.DiscordBot;
import com.marginallyclever.discorddnd.dndactions.Add;
import com.marginallyclever.discorddnd.dndactions.Get;
import com.marginallyclever.discorddnd.dndactions.Help;
import com.marginallyclever.discorddnd.dndactions.Image;
import com.marginallyclever.discorddnd.dndactions.Initiative;
import com.marginallyclever.discorddnd.dndactions.Insult;
import com.marginallyclever.discorddnd.dndactions.Ping;
import com.marginallyclever.discorddnd.dndactions.Roll;
import com.marginallyclever.discorddnd.dndactions.Save;
import com.marginallyclever.discorddnd.dndactions.SavingThrow;
import com.marginallyclever.discorddnd.dndactions.Set;
import com.marginallyclever.discorddnd.dndactions.Stats;
import com.marginallyclever.discorddnd.dndactions.Subtract;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This document was used to start the app and is out of date:
 * <a href="https://medium.com/discord-bots/making-a-basic-discord-bot-with-java-834949008c2b">Medium.com</a></p>
 * 
 * <p>This was used to generate the unique private token (which must never be put into the Github repo).
 * <a href="https://www.writebots.com/discord-bot-token/">how to get a token</a></p>
 * @author Dan Royer
 */
public class DiscordDND extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(DiscordDND.class);

	private final DiscordBot discordBot = new DiscordBot(DiscordDND.class);

	public static final String PREFIX = "!";
	private final Map<String,Character5e> characters = new HashMap<>();
	public static ArrayList<DNDAction> actions = new ArrayList<>();

    public static void main(String[] args) {
		new DiscordDND();
	}

	public DiscordDND() {
		super();
		discordBot.addMessageListener(this::onMessageReceived);
		setupActions();
    }

	private void setupActions() {
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

	private Character5e loadCharacter(String actorId, String characterName) {
		// Is this actor loaded in memory?
		if(characters.get(actorId)==null) {
			// No.  Does it exist on disk?
			if(!iHaveSeenCharacterBefore(characterName)) {
				System.out.println(characterName + " is new to me...");
				// No.  Make a new one.
	    		Character5e newCharacter = new Character5e();
				saveCharacter(characterName,newCharacter);
			} else {
	    		// Yes.  Load it.
	    		Character5e actorLoaded = loadCharacter(characterName);
	    		// Make sure we remember it for next time.
	   			characters.put(actorId, actorLoaded);
			}
		}
		return characters.get(actorId);
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
		// Ignore me when I talk to myself.  Probably bad advice philosophically but prevents an infinite loop.  
    	if(event.getAuthor().isBot()) return;

    	String message = event.getMessage().getContentDisplay();
		logger.info(message);

    	// does it have the prefix?
    	if(!message.startsWith(PREFIX)) return;
    	// remove the prefix and continue
    	message = message.substring(PREFIX.length());

		DNDEvent dndEvent;
    	if(event.isFromType(ChannelType.PRIVATE)) {
			var user = event.getChannel().asPrivateChannel().getUser();
			if(user==null) {
				logger.error("Could not get user from private channel.");
				return;
			} else {
				String actorId = event.getAuthor().getId();
				String characterName = user.getEffectiveName();
				Character5e character = loadCharacter(actorId,characterName);

				dndEvent = new DNDEvent(message,event,characterName,character);
			}
    	} else {
			String characterName = event.getMember().getNickname() != null
					? event.getMember().getNickname()
					: event.getMember().getEffectiveName();
			String actorId = event.getAuthor().getId();
			Character5e character = loadCharacter(actorId,characterName);

			dndEvent = new DNDEvent(message,event,characterName,character);
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
    	
    	// whois [name] tells any notes about that character?
    	// add [name] to list of characters
    	// note [name] [string] - adds note to character
    	
    	super.onMessageReceived(event);
    }

	// TODO move character stuff into class
	static public String characterNameToFileName(String characterName) {
		return characterName+".5e";
	}

	// TODO move character stuff into class
	static public Character5e loadCharacter(String characterName) {
		Character5e character = null;
        try {
            FileInputStream fileOut = new FileInputStream(characterNameToFileName(characterName));
            ObjectInputStream objectOut = new ObjectInputStream(fileOut);
            character = (Character5e)objectOut.readObject();
            objectOut.close();
            System.out.println(characterName+" loaded.");
        } catch (Exception ex) {
            logger.error("Failed to load character "+characterName,ex);
        }
        return character;
	}

	// TODO move character stuff into class
	static public void saveCharacter(String characterName,Character5e character) {
        try {
            FileOutputStream fileOut = new FileOutputStream(characterNameToFileName(characterName));
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(character);
            objectOut.close();
            System.out.println(characterName+" saved.");
        } catch (Exception ex) {
			logger.error("Failed to save character "+characterName,ex);
        }
	}
	
	private boolean iHaveSeenCharacterBefore(String characterName) {
		File f = new File(characterNameToFileName(characterName));
		return f.exists();
	}
}
