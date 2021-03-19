package com.marginallyclever.discordDND;

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
 * https://medium.com/discord-bots/making-a-basic-discord-bot-with-java-834949008c2b
 * https://www.writebots.com/discord-bot-token/
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
    
    protected void roll(MessageReceivedEvent event,int numDice,int numSides,int numKeep,int modifier) {
    	int [] rolls = new int[numDice];
    	
    	Random r = new Random();
    	for(int i=0;i<numDice;++i) {
    		rolls[i]=r.nextInt(numSides)+1;
    	}
    	// reject some dice
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
		String extra = modifier>0 ? "+"+modifier : "";
    	String str = "`"+numDice+"d"+numSides;
    	if(numKeep!=numDice) {
    		str+="k"+numKeep;
    	}
		str+=extra+"` ";
    	int sum=0;
    	String insert="";
    	
    	for(int i=0;i<numDice;++i) {
    		if(rolls[i]>0) {
    			sum+=rolls[i];
        		str+=insert+rolls[i];
    		} else {
        		str+="~~"+insert+(-rolls[i])+"~~";
    		}
    		insert="+";
    	}
    	if(modifier!=0) {
    		sum+=modifier;
    		str+=" ("+extra+")";
    	}
    	str+="="+sum;
    	
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
    	System.out.println("heard:"+message);
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
    	// does the actor exist?
    	if(actors.get(actorId)==null) {
    		// no.  can it be loaded?
    		Character5e actorLoaded = loadCharacter(actorName);
    		if(actorLoaded==null) {
    			// no, make a new one.
    			actorLoaded = new Character5e();
    		}
    		// either way, make sure we remember it.
   			actors.put(actorId, actorLoaded);
    	}
    	// character must exist, so load it.
    	Character5e actor = actors.get(actorId);
    	
    	if(message.equals("help")) {
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
    		
    	} else if(message.equals("ping")) {
    		replyToEvent(event,"pong");
    		
    	} else if(message.contentEquals("image")) {
    		replyToEvent(event,"https://i.pinimg.com/originals/62/71/f8/6271f88242b6c00832edd875259f4f28.png");
    		
    	} else if(message.startsWith("get")) {
    		String [] parts = message.split("\\s");
    		if(parts.length!=2) return;
    		String key = getAbbreviation(parts[1]);
			Integer value = actor.get(key);
			if(value==null) {
				replyToEvent(event,actorName+", I don't know what '"+key+"' means...yet.");
			} else {
				replyToEvent(event,actorName+", your "+key+" is "+value+".");
			}
			
    	} else if(message.startsWith("set")) {
    		String [] parts = message.split("\\s");
    		if(parts.length!=3) return;
    		String key = getAbbreviation(parts[1]);
    		Integer value = Integer.parseInt(parts[2]);
    		actor.set(key, value);
			replyToEvent(event,actorName+", your "+key+" is now "+value+".");
			
    	} else if(message.startsWith("add")) {
    		String [] parts = message.split("\\s");
    		if(parts.length!=3) return;
    		String key = getAbbreviation(parts[1]);
    		Integer oldValue = actor.get(key);
    		if(oldValue==null) oldValue=0;
    		Integer value = Integer.parseInt(parts[2]);
    		Integer newValue = oldValue + value;
    		actor.set(key, newValue);
			replyToEvent(event,actorName+", your "+key+" is now "+newValue+".");
			
    	} else if(message.startsWith("sub")) {
    		String [] parts = message.split("\\s");
    		if(parts.length!=3) return;
    		String key = getAbbreviation(parts[1]);
    		Integer oldValue = actor.get(key);
    		if(oldValue==null) oldValue=0;
    		Integer value = Integer.parseInt(parts[2]);
    		Integer newValue = oldValue - value;
    		actor.set(key, newValue);
			replyToEvent(event,actorName+", your "+key+" is now "+newValue+".");
			
    	} else if(message.startsWith("roll") 
    			|| message.contentEquals("r")) {
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
			// read B
			for(index=0;index<p1.length();++index) {
				if(!Character.isDigit(p1.charAt(index))) break;
			}
			if(index==p1.length()) {
				// uh oh
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
			
    	} else if(message.contentEquals("stats")) {
    		// all stats
    		replyToEvent(event,"```"+actor.toString()+"```");
    	} else if(message.startsWith("st")) {
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
    		
    	} else if(message.startsWith("save")) {
    		saveCharacter(actorName, actor);
			replyToEvent(event,actorName+", I'll always treasure these moments together.");
			
    	}
    	
    	super.onMessageReceived(event);
    }
    
	public void saveCharacter(String filepath,Character5e actor) {
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
	
	public Character5e loadCharacter(String filepath) {
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
}
