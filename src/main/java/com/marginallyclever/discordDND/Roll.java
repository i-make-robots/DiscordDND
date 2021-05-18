package com.marginallyclever.discordDND;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Roll extends DNDAction {

	@Override
	public void execute(DNDEvent event) {
	    // remove all whitespace and the roll command from the start
		String [] parts = event.message.split("\\s");
		ArrayList<String> list1 = new ArrayList<String>();
	    Collections.addAll(list1, parts);
	    list1.remove(0);
	    String newMessage = "";
		for(int i=0;i<list1.size();++i) {
			newMessage += list1.get(i).trim();
		}
		
		System.out.println("roll="+newMessage);
		int numDice=1;
		int numKeep=1;
		int numSides=20;
		int modifier=0;
		
		Pattern p = Pattern.compile("([\\+\\-]?\\d+)?(d[\\+\\-]?\\d+)(k[\\+\\-]?\\d+)?([\\+\\-]\\d+)?");
		Matcher m = p.matcher(newMessage);
		while(m.find()) {
			for(int i=0;i<m.groupCount()+1;++i) {
				System.out.println(i+" > "+m.group(i));
			}
			if(m.group(1) !=null && !m.group(1).isEmpty()) numDice  = Integer.parseInt(m.group(1));
			if(m.group(2) !=null && !m.group(2).isEmpty()) numSides = Integer.parseInt(m.group(2).substring(1));
			if(m.group(3) !=null && !m.group(3).isEmpty()) numKeep  = Integer.parseInt(m.group(3).substring(1));
			else numKeep=numDice;
			if(m.group(4) !=null && !m.group(4).isEmpty()) modifier = Integer.parseInt(m.group(4));

			if(numDice>0 && numSides>0 && numKeep<=numDice) {
				roll(event,numDice,numSides,numKeep,modifier);
				return;
			} else if(numDice<=0) {
				event.reply(numDice+" dice?  https://en.meming.world/images/en/thumb/5/53/Thanos%27_Impossible.jpg/300px-Thanos%27_Impossible.jpg");
			} else if(numSides<=1) {
				event.reply(numSides + " sides?  https://i.pinimg.com/originals/3a/58/61/3a5861c95871f4063a7f57a2ed97988a.jpg");
			} else if(numKeep>numDice) {
				event.reply("keep more than I roll?  https://i.imgflip.com/23cs0d.jpg");
			}
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
    	if(numKeep==rolls.length) return;
    	
    	for(int k = numKeep;k<rolls.length;++k) {
    		int worst = 0;
    		for(int i=0;i<rolls.length;++i) {
    			if(rolls[i]>0 && rolls[worst]>rolls[i]) worst = i;
    		}
    		rolls[worst] *= -1;  // mark it as rejected but keep the value.
    	}
    }

    // keep some dice.  Keep it organic looking by not sorting the list.
	// mark the rejects by making them negative amounts.
    @SuppressWarnings("unused")
    private void keepSomeLowRolls(int [] rolls,int numKeep) {
    	if(numKeep==rolls.length) return;
    	
    	for(int k = numKeep;k<rolls.length;++k) {
    		int worst = 0;
    		for(int i=0;i<rolls.length;++i) {
    			if(rolls[i]>0 && rolls[worst]<rolls[i]) worst = i;
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
    	int [] rolls = rollDice(numDice,numSides);
    	keepSomeHighRolls(rolls,numKeep);
    	
    	// sum and display
    	int sum=0;
    	for(int i=0;i<numDice;++i) {
    		if(rolls[i]>0) sum+=rolls[i];
    	}
    	sum+= modifier;
    			
    	String extra = "";
    	String message = numDice+"d"+numSides+"k"+numKeep;
    	if(modifier!=0) {
    		extra = String.format("%+d", modifier);
    		message += extra;	
    	}
    	message+=" = ";
		
    	String insert="";
    	String thisRoll;
    	for(int i=0;i<numDice;++i) {
    		thisRoll = Integer.toString(Math.abs(rolls[i]));
    		if(rolls[i]<0) thisRoll = strikethrough(thisRoll);
    		message+=insert+thisRoll;
    		insert=" + ";
    	}
    	// add modifier
    	if(modifier!=0) message+=" ("+extra+")";
    	message+=" = "+bold(Integer.toString(sum));

		event.reply(event.actorName + ": "+message);
    }
    
    private String bold(String a) {
    	return "`"+a+"`";
    }
    
    private String strikethrough(String a) {
    	return "~~"+a+"~~";
    }
}