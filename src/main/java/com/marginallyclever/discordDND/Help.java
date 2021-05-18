package com.marginallyclever.discordDND;

public class Help extends DNDAction {
	@Override
	public void execute(DNDEvent event) {
		event.reply(
			"```css\n"
			+DiscordDND.PREFIX+"help : This message\n"
			+DiscordDND.PREFIX+"image : A helpful image for noobs\n" 
			+DiscordDND.PREFIX+"set S W : Set attribute S to W\n"
			+DiscordDND.PREFIX+"get S : Show my attribute S\n"
			+DiscordDND.PREFIX+"add S W : Add W to attribute S\n"
			+DiscordDND.PREFIX+"sub S W : Subtract W from attribute S\n"
			+DiscordDND.PREFIX+"roll [Wa]dWb[+/-Wc] : Show result of rolling Wb sided dice Wa times and adding Wc modifier.\n"
			+DiscordDND.PREFIX+"st S : Roll a Saving Throw for a given attribute S\n"
			+DiscordDND.PREFIX+"save : Save your attributes for next time.\n"
			+DiscordDND.PREFIX+"stats : Show all my attributes.\n"
			+"\n"
			+"S is a string (text), W is a whole number. Stuff [in brackets] is optional.\n"
			+"Many attributes have abbreviations - for example, i or int will both equal intelligence.```");
	}

	@Override
	public String[] getNames() {
		return new String[] { "help","h" };
	}
}
