package com.marginallyclever.discorddnd.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.marginallyclever.discorddnd.DNDAction;
import com.marginallyclever.discorddnd.DNDEvent;

public class Roll extends DNDAction {
	private final Pattern pattern = Pattern.compile("\\(([^\\)]+)\\)|^[^\\(\\)]+$");
	private final Pattern subpattern = Pattern.compile("([\\+\\-]?\\d+)?(d[\\+\\-]?\\d+)?(k[\\+\\-]?\\d+)?([\\+\\-]\\d+)?");

    // remove all whitespace and the roll command from the start
	private String sanitizeMessage(String input) {
		String [] parts = input.split("\\s");
		List<String> list1 = new ArrayList<>();
	    Collections.addAll(list1, parts);
	    list1.remove(0);
	    StringBuilder output = new StringBuilder();
        for (String s : list1) {
            output.append(s.trim());
        }
		return output.toString();
	}
	
	@Override
	public void execute(DNDEvent event) {
		String saneMessage = sanitizeMessage(event.message);
		System.out.println("roll="+saneMessage);

		Matcher m = pattern.matcher(saneMessage);

		List<String> groups = new ArrayList<>();
		int total = 0;
		while (m.find()) {
			groups.add(m.group());
			total += subroll(event,m.group());
		}

		if(groups.size()>1 && total>0) {
			event.reply(event.characterName + ": total "+total);
		}
	}

	private int subroll(DNDEvent event,String saneMessage) {
		Matcher m = subpattern.matcher(saneMessage);
		if(!m.find()) return 0;

		int numDice=1;
		int numKeep;
		int numSides=20;
		int modifier=0;

		if(m.group(1)!=null && !m.group(1).isEmpty()) numDice  = Integer.parseInt(m.group(1));
		if(m.group(2)!=null && !m.group(2).isEmpty()) numSides = Integer.parseInt(m.group(2).substring(1));
		if(m.group(3)!=null && !m.group(3).isEmpty()) numKeep  = Integer.parseInt(m.group(3).substring(1));
		else numKeep = numDice;
		if(m.group(4)!=null && !m.group(4).isEmpty()) modifier = Integer.parseInt(m.group(4));

		return roll(event,numDice,numSides,numKeep,modifier);
	}


	@Override
	public String[] getNames() {
		return new String[] { "roll","r" };
	}
	
	public String getHelp() {
		return "roll [<a>]d[<b>][k<c>][+/-<e>]\n"
				+"\troll <a> dice (default 1)"
				+"\twith <b> sides (default 20)"
				+"\tkeep <c> (default all, negative for disadvantage)"
				+"\tand add or subtract <e>.";
	}

    private int [] rollDice(int numDice,int numSides) {
    	int [] rolls = new int[numDice];
    	
    	Random r = new Random();
    	for(int i=0;i<numDice;++i) {
    		rolls[i] = r.nextInt(numSides)+1;
    	}
    	
    	return rolls;
    }

	/**
	 * keep some dice.  Keep it organic looking by not sorting the list.
	 * mark the rejects by making them negative amounts.
	 * @param rolls
	 * @param numKeep
	 */
    private void keepSomeHighRolls(int [] rolls,int numKeep) {
    	for(int k = numKeep;k<rolls.length;++k) {
    		int worst = 0;
    		for(int i=0;i<rolls.length;++i) {
    			if(rolls[i]>0 && rolls[worst] > rolls[i]) worst = i;
    		}
    		rolls[worst] *= -1;  // mark it as rejected but keep the value.
    	}
    }

	/**
	 * keep some dice.  Keep it organic looking by not sorting the list.
	 * mark the rejects by making them negative amounts.
	 * @param rolls
	 * @param numKeep
	 */
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
	 * @return the sum of the kept rolls plus the modifier.
     */
    public int roll(DNDEvent event,int numDice,int numSides,int numKeep,int modifier) {
    	// sanity checks
		if(numDice<=0) {
			event.reply(numDice+" dice?  https://en.meming.world/images/en/thumb/5/53/Thanos%27_Impossible.jpg/300px-Thanos%27_Impossible.jpg");
			return 0;
		} else if(numSides<=0) {
			event.reply(numSides + " sides?  https://i.pinimg.com/originals/3a/58/61/3a5861c95871f4063a7f57a2ed97988a.jpg");
			return 0;
		} else if(Math.abs(numKeep)>numDice) {
			event.reply("keep "+Math.abs(numKeep)+"?  https://i.imgflip.com/23cs0d.jpg");
			return 0;
		}

    	int [] rolls = rollDice(numDice,numSides);
    	if(numKeep!=numDice) {
    		if(numKeep>0) keepSomeHighRolls(rolls,numKeep);
    		else          keepSomeLowRolls(rolls,-numKeep);
    	}

		int sum = sumKeptRolls(rolls) + modifier;
		event.reply(event.characterName + ": "+renderResults(sum,rolls,modifier));
		return sum;
    }
    
    private String renderResults(int sum,int [] rolls,int modifier) {
    	StringBuilder rollResults = new StringBuilder();
    	String insert="";
        for (int roll : rolls) {
            String thisRoll = Integer.toString(Math.abs(roll));
            if (roll < 0) thisRoll = strikethrough(thisRoll);
            rollResults.append(insert).append(thisRoll);
            insert = " + ";
        }
    	if(modifier!=0) rollResults.append(String.format(" (%+d)", modifier));

		rollResults.append(" = ");
		rollResults.append(bold(Integer.toString(sum)));
		rollResults.append(renderExtras(rolls));
    	return rollResults.toString();
    }
    
    private String renderExtras(int [] rolls) {
    	boolean missed20 = false;
    	boolean missed1 = false;

		boolean [] found = new boolean[21];

        for (int value : rolls) {
            if (Math.abs(value) > 0) {
				if(value>=1 && value<=20) found[value] = true;
            } else {
                if (value == -20) missed20 = true;
                if (value == -1) missed1 = true;
            }
        }

		StringBuilder result = new StringBuilder();
		result.append(" ");
		if(rolls.length==1) {
				 if(found[1]) result.append("https://i.pinimg.com/originals/4f/d7/7f/4fd77f1b87b9a2a60dc69d3b8dfca767.jpg");
			else if(found[2]) result.append("https://media1.tenor.com/m/v2n3rVxTeJQAAAAd/jeff-goldblum.gif");
			//else if(found[3]) result.append("");
			//else if(found[4]) result.append("");
			//else if(found[5]) result.append("");
			//else if(found[6]) result.append("");
			//else if(found[7]) result.append("");
			//else if(found[8]) result.append("");
			//else if(found[9]) result.append("");
			//else if(found[10]) result.append("");
			//else if(found[11]) result.append("");
			//else if(found[12]) result.append("");
			//else if(found[13]) result.append("");
			//else if(found[14]) result.append("");
			//else if(found[15]) result.append("");
			//else if(found[16]) result.append("");
			//else if(found[17]) result.append("");
			//else if(found[18]) result.append("");
			//else if(found[19]) result.append("");
			else if(found[20]) result.append("https://pics.me.me/happiness-is-a-natural-20-or-at-least-a-moment-2761750.png");

			if(missed20) result.append("https://i.kym-cdn.com/entries/icons/mobile/000/009/976/First_World_Problems.jpg");
			if(missed1) result.append("https://tenor.com/view/matrix-keanu-reeves-dodge-bullets-dodging-gif-12076401");
		}

    	return result.toString();
    }
    
    private int sumKeptRolls(int[] rolls) {
    	int sum=0;
		for(int roll : rolls) {
            if (roll > 0) sum += roll;
        }
		return sum;
	}
    
    private String bold(String a) {
    	return "`"+a+"`";
    }
    
    private String strikethrough(String a) {
    	return "~~"+a+"~~";
    }
}
