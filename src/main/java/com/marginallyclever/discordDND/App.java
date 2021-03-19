package com.marginallyclever.discordDND;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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
public class App extends ListenerAdapter {
	static final String MY_ENTITY_ID = "698232700024127528";
	static final String MY_ENTITY_NAME = "Simply DND 5E";
	static final String PREFIX = "~";

	protected Map<String,String> abbreviations = new HashMap<String,String>();
	protected Map<String,Character5e> actors = new HashMap<String,Character5e>();
	
    public static void main( String[] args ) throws LoginException {
        Logger logger = LoggerFactory.getLogger(App.class);
        logger.info("Hello World");
        
        System.out.println("Hello World!");

        String token = readAllBytesJava7(App.class.getResource("token.txt"));
        JDA jda = JDABuilder.createDefault(token).build();
        jda.addEventListener(new App());
        
        /*
        builder.setToken(token);
        builder.addEventListeners(new App());
		builder.buildAsync();*/
    }

    private static String readAllBytesJava7(URL filePath) {
        String content = "";
        try {
        	System.out.println("Token search: "+filePath.toURI());
            content = new String ( Files.readAllBytes( Paths.get( filePath.toURI() ) ) );
        }  catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return content;
    }
    
	public App() {
		abbreviations.put("strength", "strength");
		abbreviations.put("str", "strength");
		abbreviations.put("s", "strength");
		abbreviations.put("dexterity", "dexterity");
		abbreviations.put("dex", "dexterity");
		abbreviations.put("d", "dexterity");
		abbreviations.put("constitution", "constitution");
		abbreviations.put("con", "constitution");
		abbreviations.put("c", "constitution");
		abbreviations.put("intelligence", "intelligence");
		abbreviations.put("int", "intelligence");
		abbreviations.put("i", "intelligence");
		abbreviations.put("wisdom", "wisdom");
		abbreviations.put("wis", "wisdom");
		abbreviations.put("w", "wisdom");
		abbreviations.put("charisma", "charisma");
		abbreviations.put("cha", "charisma");
		abbreviations.put("c", "charisma");
		abbreviations.put("hp", "hit points");
		abbreviations.put("nhp", "nominal hit points");
	}
    
    
    protected void replyToEvent(MessageReceivedEvent event,String str) {
    	System.out.println(MY_ENTITY_NAME+"@"+MY_ENTITY_ID+">>"+str);
		event.getChannel().sendMessage(str).queue();
    }
    
    /**
     * Roll dice and print result.  
     * @param event
     * @param numDice quantity of dice to roll
     * @param numSides how many sides on the dice being rolled
     * @param numKeep how many of the rolls to keep?  Must be greater than 0 and less than or equal to numDice
     * @param modifier an additional modifier bonus to add after the keeps.
     */
    protected void roll(MessageReceivedEvent event,int numDice,int numSides,int numKeep,int modifier) {
    	// roll the dice
    	int [] rolls = new int[numDice];
    	
    	Random r = new Random();
    	for(int i=0;i<numDice;++i) {
    		rolls[i]=r.nextInt(numSides)+1;
    	}
    	// Reject some dice.  Sorting the list before rejecting won't work because only low rolls would get tossed.
    	// I'll mark the rejects by making them negative amounts.
    	for(int k = numKeep;k<numDice;++k) {
    		// find the worst dice.
    		int worst = 0;
    		for(int i=0;i<numDice;++i) {
    			if(rolls[i]>0 && rolls[worst]>rolls[i]) {
    				worst = i;
    			}
    		}
    		rolls[worst] *= -1;  // mark it as rejected but keep the value.
    	}
    	
    	// sum and display
    	String str = numDice+"d"+numSides;
    	if(numKeep!=numDice) str+="k"+numKeep;
    	String extra = modifier>0 ? "+"+modifier : ""; 
		str += extra;
		// discord bold syntax
		str = "`"+str+"`";
		
    	int sum=0;
    	String insert="";
    	
    	for(int i=0;i<numDice;++i) {
    		if(rolls[i]>0) {
    			// dice i'm keeping
    			sum+=rolls[i];
        		str+=insert+rolls[i];
    		} else {
    			// dice rejected are strikethrough style
        		str+="~~"+insert+(-rolls[i])+"~~";
    		}
    		insert="+";
    	}
    	// add modifier
    	if(modifier!=0) {
    		sum+=modifier;
    		str+=" ("+extra+")";
    	}
    	str+="="+sum;
    	
    	// echo the results to the channel
    	event.getChannel().sendMessage(str).queue();
    }
    
    public String getAbbreviation(String arg0) {
    	String key = abbreviations.get(arg0);
    	if(key==null) key = arg0;
    	return key;
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
    	String message = event.getMessage().getContentDisplay();
    	
    	if(event.getAuthor().isBot()) {
    		// Ignore me when I talk to myself.  Probably bad advice philosophically but prevents an infinite loop.  
    		return;
    	}
    	
    	// does it have the prefix?
    	if(!message.startsWith(PREFIX)) {
    		// no, quit now.
    		return;
    	}
    	
    	// remove the prefix and continue
    	message = message.substring(PREFIX.length());

    	String actorName = event.getAuthor().getName();
    	String actorId = event.getAuthor().getId();
    	//System.out.println(actorName+"@"+actorId+"<<"+message);
    	
    	// Is this actor loaded in memory?
    	if(actors.get(actorId)==null) {
    		// No.  Does it exist on disk?
    		if(iHaveSeenCharacterBefore(actorName)==false) {
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
    	// Character should exist now so get it from memory.
    	Character5e actor = actors.get(actorId);
    	
    	String [] parts = message.split(" ");
    	if(parts[0].contentEquals("help" )) help(message,event,actorName,actor); 
    	if(parts[0].contentEquals("image")) image(message,event,actorName,actor); 
    	if(parts[0].contentEquals("ping" )) ping(message,event,actorName,actor);  
		if(parts[0].contentEquals("get"  )) get(message,event,actorName,actor);  
		if(parts[0].contentEquals("set"  )) set(message,event,actorName,actor);  
		if(parts[0].contentEquals("add"  )) add(message,event,actorName,actor);  
		if(parts[0].contentEquals("sub"  )) subtract(message,event,actorName,actor);  
    	if(parts[0].contentEquals("r"    )) roll2(message,event,actorName,actor);  
    	if(parts[0].contentEquals("roll" )) roll2(message,event,actorName,actor);  
    	if(parts[0].contentEquals("stats")) stats(message,event,actorName,actor);  
    	if(parts[0].contentEquals("st"   )) savingThrow(message,event,actorName,actor);  
    	if(parts[0].contentEquals("save" )) save(message,event,actorName,actor);  
    	
    	super.onMessageReceived(event);
    }

	private void image(String message,MessageReceivedEvent event,String actorName,Character5e actor) {
		replyToEvent(event,"https://i.pinimg.com/originals/62/71/f8/6271f88242b6c00832edd875259f4f28.png");
	}
	
	private void help(String message,MessageReceivedEvent event,String actorName,Character5e actor) {
		replyToEvent(event,
			"```css\n"
			+PREFIX+"help : This message\n"
			+PREFIX+"image : A helpful image for noobs\n" 
			+PREFIX+"set S W : Set attribute S to W\n"
			+PREFIX+"get S : Show my attribute S\n"
			+PREFIX+"add S W : Add W to attribute S\n"
			+PREFIX+"sub S W : Subtract W from attribute S\n"
			+PREFIX+"roll [Wa]dWb[+/-Wc] : Show result of rolling Wb sided dice Wa times and adding Wc modifier.\n"
			+PREFIX+"st S : Roll a Saving Throw for a given attribute S\n"
			+PREFIX+"save : Save your attributes for next time.\n"
			+PREFIX+"stats : Show all my attributes.\n"
			+"\n"
			+"S is a string (text), W is a whole number. Stuff [in brackets] is optional.\n"
			+"Many attributes have abbreviations - for example, i or int will both equal intelligence.```");
	}
	
	private void ping(String message,MessageReceivedEvent event,String actorName,Character5e actor) {
		replyToEvent(event,"pong");
	}
	
	private void get(String message,MessageReceivedEvent event,String actorName,Character5e actor) {
		String [] parts = message.split("\\s");
		if(parts.length!=2) return;
		String key = getAbbreviation(parts[1]);
		Integer value = actor.get(key);
		if(value==null) {
			replyToEvent(event,actorName+", I don't know what '"+key+"' means...yet.");
		} else {
			replyToEvent(event,actorName+", your "+key+" is "+value+".");
		}
	}

    private void set(String message,MessageReceivedEvent event,String actorName,Character5e actor) {
		String [] parts = message.split("\\s");
		if(parts.length!=3) return;
		String key = getAbbreviation(parts[1]);
		Integer value = Integer.parseInt(parts[2]);
		actor.set(key, value);
		replyToEvent(event,actorName+", your "+key+" is now "+value+".");
    }

    private void add(String message,MessageReceivedEvent event,String actorName,Character5e actor) {
		String [] parts = message.split("\\s");
		if(parts.length!=3) return;
		String key = getAbbreviation(parts[1]);
		Integer oldValue = actor.get(key);
		if(oldValue==null) oldValue=0;
		Integer value = Integer.parseInt(parts[2]);
		Integer newValue = oldValue + value;
		actor.set(key, newValue);
		replyToEvent(event,actorName+", your "+key+" is now "+newValue+".");
    }

    private void subtract(String message,MessageReceivedEvent event,String actorName,Character5e actor) {
		String [] parts = message.split("\\s");
		if(parts.length!=3) return;
		String key = getAbbreviation(parts[1]);
		Integer oldValue = actor.get(key);
		if(oldValue==null) oldValue=0;
		Integer value = Integer.parseInt(parts[2]);
		Integer newValue = oldValue - value;
		actor.set(key, newValue);
		replyToEvent(event,actorName+", your "+key+" is now "+newValue+".");
    }
    
	private void roll2(String message,MessageReceivedEvent event,String actorName,Character5e actor) {
		String [] parts = message.split("\\s");
		if(parts.length!=2) return;
		
		// format is AdB[kC][+/-D]
		// A,B,C,D are whole numbers.
		// C and D are optional.
		String p1 = parts[1];
		int numDice=1;
		int numKeep=-1;
		int numSides=-1;
		int modifier=0;
		int index;
		// how many dice?
		index = p1.indexOf("d");
		if(index==-1) {
			// uh oh
		}
		numDice=Integer.parseInt(p1.substring(0,index));
		// remove Ad and anything before it
		p1 = p1.substring(index+1);
		// read B by finding first non-digit character
		for(index=0;index<p1.length();++index) {
			if(!Character.isDigit(p1.charAt(index))) break;
		}
		numSides=Integer.parseInt(p1.substring(0,index));
		// is k here?
		index = p1.indexOf("k");
		if(index!=-1) {
			// remove k and anything before it
			p1 = p1.substring(index+1);
			// read C
			for(index=0;index<p1.length();++index) {
				if(!Character.isDigit(p1.charAt(index))) break;
			}
			if(index==p1.length()) {
				// uh oh
			}
			numKeep=Integer.parseInt(p1.substring(0,index));
			// can't keep more than you roll!
			if(numKeep>numDice) numKeep=numDice; 
		} else {
			numKeep = numDice;
		}
		// look for optional modifier
		Pattern pattern = Pattern.compile("[\\+\\-]");
		Matcher matcher = pattern.matcher(p1);
		int plusIndex=p1.length();
		if(matcher.find()) {
			// found!
			plusIndex = matcher.start();
			modifier=Integer.parseInt(p1.substring(plusIndex));
		}
		if(numDice>0 && numSides>0 && numKeep>0) {
			roll(event,numDice,numSides,numKeep,modifier);
		}
	}

	private void stats(String message,MessageReceivedEvent event,String actorName,Character5e actor) {
		// all stats
		replyToEvent(event,"```"+actor.toString()+"```");
	}
	
	private void savingThrow(String message,MessageReceivedEvent event,String actorName,Character5e actor) {
		// saving throw
		String [] parts = message.split("\\s");
		if(parts.length!=2) return;
		String key = getAbbreviation(parts[1]);
		Integer ability = actor.get(key);
		if(ability == null) {
			replyToEvent(event,actorName+", what's '"+key+"'?");
		} else {
			roll(event,1,20,1,ability);
		}
	}
	
    private void save(String message,MessageReceivedEvent event,String actorName,Character5e actor) {
		saveCharacter(actorName, actor);
		replyToEvent(event,actorName+", I'll always treasure these moments together.");
    }
    
	private void saveCharacter(String filepath,Character5e actor) {
        try {
            FileOutputStream fileOut = new FileOutputStream(filepath+".5e");
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(actor);
            objectOut.close();
            System.out.println(filepath+" saved.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	private Character5e loadCharacter(String filepath) {
		Character5e actor = null;
        try {
            FileInputStream fileOut = new FileInputStream(filepath+".5e");
            ObjectInputStream objectOut = new ObjectInputStream(fileOut);
            actor = (Character5e)objectOut.readObject();
            objectOut.close();
            System.out.println(filepath+" loaded.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return actor;
	}
	
	private boolean iHaveSeenCharacterBefore(String filepath) {
		File f = new File(filepath);
		return f.exists();
	}
}
