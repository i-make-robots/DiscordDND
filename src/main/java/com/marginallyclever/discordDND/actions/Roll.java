package com.marginallyclever.discordDND.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.marginallyclever.discordDND.DNDAction;
import com.marginallyclever.discordDND.DNDEvent;

public class Roll extends DNDAction {
    // remove all whitespace and the roll command from the start
	private String sanitizeMessage(String input) {
		String [] parts = input.split("\\s");
		ArrayList<String> list1 = new ArrayList<String>();
	    Collections.addAll(list1, parts);
	    list1.remove(0);
	    String output = "";
		for(int i=0;i<list1.size();++i) {
			output += list1.get(i).trim();
		}
		return output;
	}
	
	@Override
	public void execute(DNDEvent event) {
		String saneMessage = sanitizeMessage(event.message);
		//System.out.println("roll="+saneMessage);

		Pattern p = Pattern.compile("([\\+\\-]?\\d+)?(d[\\+\\-]?\\d+)(k[\\+\\-]?\\d+)?([\\+\\-]\\d+)?");
		Matcher m = p.matcher(saneMessage);
		if(m.find()) {
			//for(int i=0;i<m.groupCount()+1;++i) System.out.println(i+" > "+m.group(i));
			
			int numDice=1;
			int numKeep;
			int numSides=20;
			int modifier=0;
			
			if(m.group(1)!=null && !m.group(1).isEmpty()) numDice  = Integer.parseInt(m.group(1));
			if(m.group(2)!=null && !m.group(2).isEmpty()) numSides = Integer.parseInt(m.group(2).substring(1));
			if(m.group(3)!=null && !m.group(3).isEmpty()) numKeep  = Integer.parseInt(m.group(3).substring(1));
			else numKeep=numDice;
			if(m.group(4)!=null && !m.group(4).isEmpty()) modifier = Integer.parseInt(m.group(4));

			roll(event,numDice,numSides,numKeep,modifier);
			return;
		}
	}

	@Override
	public String[] getNames() {
		return new String[] { "roll","r" };
	}

    private int [] rollDice(int numDice,int numSides) {
    	int [] rolls = new int[numDice];
    	
    	Random r = new Random();
    	for(int i=0;i<numDice;++i) {
    		rolls[i]=r.nextInt(numSides)+1;
    	}
    	
    	return rolls;
    }

    // keep some dice.  Keep it organic looking by not sorting the list.
	// mark the rejects by making them negative amounts.
    private void keepSomeHighRolls(int [] rolls,int numKeep) {
    	for(int k = numKeep;k<rolls.length;++k) {
    		int worst = 0;
    		for(int i=0;i<rolls.length;++i) {
    			if(rolls[i]>0 && rolls[worst] > rolls[i]) worst = i;
    		}
    		rolls[worst] *= -1;  // mark it as rejected but keep the value.
    	}
    }

    // keep some dice.  Keep it organic looking by not sorting the list.
	// mark the rejects by making them negative amounts.
    private void keepSomeLowRolls(int [] rolls,int numKeep) {
    	for(int k = numKeep;k<rolls.length;++k) {
    		int worst = 0;
    		for(int i=0;i<rolls.length;++i) {
    			if(rolls[i]>0 && rolls[worst] < rolls[i]) worst = i;
    		}
    		rolls[worst] *= -1;  // mark it as rejected but keep the value.
    	}
    }
    
    /**
     * Roll dice and print result.  
     * @param event
     * @param numDice quantity of dice to roll
     * @param numSides how many sides on the dice being rolled
     * @param numKeep how many of the rolls to keep?  Must be greater than 0 and less than or equal to numDice
     * @param modifier an additional modifier bonus to add after the keeps.
     */
    public void roll(DNDEvent event,int numDice,int numSides,int numKeep,int modifier) {
    	// sanity checks
		if(numDice<=0) {
			event.reply(numDice+" dice?  https://en.meming.world/images/en/thumb/5/53/Thanos%27_Impossible.jpg/300px-Thanos%27_Impossible.jpg");
			return;
		} else if(numSides<=0) {
			event.reply(numSides + " sides?  https://i.pinimg.com/originals/3a/58/61/3a5861c95871f4063a7f57a2ed97988a.jpg");
			return;
		} else if(Math.abs(numKeep)>numDice) {
			event.reply("keep "+Math.abs(numKeep)+"?  https://i.imgflip.com/23cs0d.jpg");
			return;
		}

		
    	int [] rolls = rollDice(numDice,numSides);
    	if(numKeep!=numDice) {
    		if(numKeep>0) keepSomeHighRolls(rolls,numKeep);
    		else          keepSomeLowRolls(rolls,-numKeep);
    	}
    	
		event.reply(event.actorName + ": "+renderResults(rolls,modifier));
    }
    
    private String renderResults(int [] rolls,int modifier) {
    	int sum = sumKeptRolls(rolls) + modifier;

    	String rollResults = "";
    	String insert="";
    	for(int i=0;i<rolls.length;++i) {
    		String thisRoll = Integer.toString(Math.abs(rolls[i]));
    		if(rolls[i]<0) thisRoll = strikethrough(thisRoll);
    		rollResults+=insert+thisRoll;
    		insert=" + ";
    	}
    	if(modifier!=0) rollResults += String.format("(%+d)", modifier);

    	return rollResults + " = "+bold(Integer.toString(sum)) + renderExtras(rolls);
    }
    
    private String renderExtras(int [] rolls) {
    	boolean found20 = false;
    	boolean found1 = false;
    	boolean missed20 = false;
    	boolean missed1 = false;
    	
    	for(int i=0;i<rolls.length;++i) {
    		if(rolls[i]>0) {
    			if(rolls[i]==20) found20=true;
    			if(rolls[i]==1) found1=true;
    		} else {
    			if(rolls[i]==20) missed20=true;
    			if(rolls[i]==1) missed1=true;
    		}
    	}
    			
    	return (found20 ?getNat20Meme()    :"")
    		+  (found1  ?getNat1Meme()     :"")
    		+  (missed20?getNat20MissMeme():"")
    		+  (missed1 ?getNat1MissMeme() :"");
    }
    
    private int sumKeptRolls(int[] rolls) {
    	int sum=0;
    	for(int i=0;i<rolls.length;++i) {
    		if(rolls[i]>0) sum+=rolls[i];
    	}
		return sum;
	}

	private String getNat20Meme() {
    	return "https://discord.com/channels/690227751696859319/839193591028252752/844331758232666152";
    }
    
    private String getNat1Meme() {
    	return "https://i.pinimg.com/originals/4f/d7/7f/4fd77f1b87b9a2a60dc69d3b8dfca767.jpg";
    }
    
    private String getNat20MissMeme() {
    	return "";
    }
    
    private String getNat1MissMeme() {
    	return "https://tenor.com/view/matrix-keanu-reeves-dodge-bullets-dodging-gif-12076401";
    }
    
    private String bold(String a) {
    	return "`"+a+"`";
    }
    
    private String strikethrough(String a) {
    	return "~~"+a+"~~";
    }
}
