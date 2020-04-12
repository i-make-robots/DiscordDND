package com.marginallyclever.discordDND;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Character5e implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Map<String,Integer> traits = new HashMap<String,Integer>();
	protected Map<String,Integer> skills = traits;
	
		
	public Character5e() {
		traits.put("strength", 0);
		traits.put("dexterity", 0);
		traits.put("constitution", 0);
		traits.put("intelligence", 0);
		traits.put("wisdom", 0);
		traits.put("charisma", 0);
		
		skills.put("acrobatics", 0);
		skills.put("animal handling", 0);
		skills.put("arcana", 0);
		skills.put("athletics", 0);
		skills.put("deception", 0);
		skills.put("history", 0);
		skills.put("insight", 0);
		skills.put("intimidation", 0);
		skills.put("investigation", 0);
		skills.put("medicine", 0);
		skills.put("nature", 0);
		skills.put("perception", 0);
		skills.put("performance", 0);
		skills.put("persuasion", 0);
		skills.put("religion", 0);
		skills.put("sleight of hand", 0);
		skills.put("stealth", 0);
		skills.put("survival", 0);

		traits.put("hit points", 0);
		traits.put("nominal hit points", 0);
	}
	
	public Integer get(String key) {
		return traits.get(key);
	}

	public void set(String key,Integer value) {
		traits.put(key,value);
	}
	
	public String toString() {
		String msg = "";
		List<String> sortedList = new ArrayList<String>(traits.keySet());
		Collections.sort(sortedList);
		
		for( String key : sortedList ) {
			msg +=key + " = "+traits.get(key)+"\n";
		}
		return msg;
	}
}
