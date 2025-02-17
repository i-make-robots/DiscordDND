package com.marginallyclever.discorddnd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.prefs.Preferences;

import com.marginallyclever.discorddnd.actions.Add;
import com.marginallyclever.discorddnd.actions.Get;
import com.marginallyclever.discorddnd.actions.Help;
import com.marginallyclever.discorddnd.actions.Image;
import com.marginallyclever.discorddnd.actions.Initiative;
import com.marginallyclever.discorddnd.actions.Insult;
import com.marginallyclever.discorddnd.actions.Ping;
import com.marginallyclever.discorddnd.actions.Roll;
import com.marginallyclever.discorddnd.actions.Save;
import com.marginallyclever.discorddnd.actions.SavingThrow;
import com.marginallyclever.discorddnd.actions.Set;
import com.marginallyclever.discorddnd.actions.Stats;
import com.marginallyclever.discorddnd.actions.Subtract;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
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

	static private final String MY_ENTITY_ID = "698232700024127528";
	static private final String MY_ENTITY_NAME = "Simply DND 5E";
	static public final String PREFIX = "!";

	private final Map<String,Character5e> characters = new HashMap<>();
	static public ArrayList<DNDAction> actions = new ArrayList<>();

	private final JDA jda;
	private Guild guild;
	private final List<String> voiceChannels = new ArrayList<>();
	private AudioManager audioManager=null;
	
    public static void main(String[] args) {
		new DiscordDND();
	}

	private static void forgetToken() {
		Preferences preferences = Preferences.userNodeForPackage(DiscordDND.class);
		preferences.remove("token");
	}

	/**
	 * Get the bot token from Preferences.  if not available, ask the user and save it to preferences.
	 * @return the token or "" if the user cancels.
	 */
	private static String getTokenFromPreferencesOrQueryUser() {
		// read the token from preferences
		Preferences preferences = Preferences.userNodeForPackage(DiscordDND.class);
		String token = preferences.get("token", "");
		if(token.isEmpty()) {
			// prompt the user for the token
			token = javax.swing.JOptionPane.showInputDialog("Please enter your Discord bot token.");
			preferences.put("token", token);
		}
		return token;
	}

	public DiscordDND() {
		super();
		setupActions();
		// start JDA
		String token = getTokenFromPreferencesOrQueryUser();
		jda = JDABuilder.createLight(token,
						GatewayIntent.GUILD_MEMBERS,
						GatewayIntent.GUILD_MESSAGES,
						GatewayIntent.DIRECT_MESSAGES,
						GatewayIntent.MESSAGE_CONTENT,
						GatewayIntent.GUILD_VOICE_STATES)
				.addEventListeners(this)
				.build();

		// wait for JDA to be ready
		try {
			jda.awaitReady();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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

	static public String characterNameToFileName(String characterName) {
		return characterName+".5e";
	}
    		
	static public Character5e loadCharacter(String characterName) {
		Character5e character = null;
        try {
            FileInputStream fileOut = new FileInputStream(characterNameToFileName(characterName));
            ObjectInputStream objectOut = new ObjectInputStream(fileOut);
            character = (Character5e)objectOut.readObject();
            objectOut.close();
            System.out.println(characterName+" loaded.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return character;
	}
	
	static public void saveCharacter(String characterName,Character5e character) {
        try {
            FileOutputStream fileOut = new FileOutputStream(characterNameToFileName(characterName));
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(character);
            objectOut.close();
            System.out.println(characterName+" saved.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	private boolean iHaveSeenCharacterBefore(String characterName) {
		File f = new File(characterNameToFileName(characterName));
		return f.exists();
	}

	/**
	 * Join the voice channel with the given name.
	 * @param channelName the name of the voice channel to join.
	 * @return true if the channel was found and joined, false otherwise.
	 */
	public boolean joinVoiceChannel(String channelName) {
		if(guild==null) {
			logger.error("Guild not set.");
			return false;
		}

		var voiceChannel = guild.getVoiceChannelsByName(channelName, true).stream().findFirst();
		if (voiceChannel.isEmpty()) {
			logger.error("Voice channel not found: " + channelName);
			return false;
		}

		var audioManager = guild.getAudioManager();

		audioManager.openAudioConnection(voiceChannel.get());
		logger.info("Joined voice channel: " + channelName);
		return true;
	}

	public List<String> getGuilds() {
		var guilds = new ArrayList<String>();
		jda.getGuilds().forEach(guild -> guilds.add(guild.getName()));
		return guilds;
	}

	public void setGuild(String item) {
		guild = jda.getGuilds().stream().filter(g->g.getName().contentEquals(item)).findFirst().orElse(null);
		if(guild==null) {
			logger.error("Could not find guild: "+MY_ENTITY_NAME);
			return;
		}

		voiceChannels.clear();
		guild.getVoiceChannels().forEach(channel -> voiceChannels.add(channel.getName()));

		// get the audio manager
		audioManager = guild.getAudioManager();
	}

	public List<String> getVoiceChannels() {
		return voiceChannels;
	}

	public AudioManager getAudioManager() {
		return audioManager;
	}
}
