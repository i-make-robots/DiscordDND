package com.marginallyclever.discordDND;

import java.util.HashMap;
import java.util.Map;

public class DNDAbbreviationsList {
	private static Map<String,String> abbreviations = new HashMap<>();

	private static void start() {
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
	
    static public String get(String arg0) {
    	if(abbreviations.isEmpty()) DNDAbbreviationsList.start();
    	String key = abbreviations.get(arg0);
    	if(key==null) key = arg0;
    	return key;
    }
}
