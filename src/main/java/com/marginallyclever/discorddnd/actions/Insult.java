package com.marginallyclever.discorddnd.actions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.marginallyclever.discorddnd.DNDAction;
import com.marginallyclever.discorddnd.DNDEvent;

public class Insult extends DNDAction {
	@Override
	public void execute(DNDEvent event) {
		String message=getURLContents("https://evilinsult.com/generate_insult.php");
		if(message.isEmpty()) message = "Insult you?  I couldn't be bothered.";		
		event.reply(message);	
	}
	
	public String getURLContents(String address) {
		String message="";
		try {
			URL url = new URL(address);
			URLConnection urlConnection = url.openConnection();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String line;
			while((line = bufferedReader.readLine()) !=null) {
				message += line;
			}
		    bufferedReader.close();  
		} catch (Exception e) {}
		return message;
	}

	@Override
	public String[] getNames() {
		return new String[] { "insult" };
	}
	
	public String getHelp() {
		return "insult - get an insult. Only costs your dignity!";
	}
}
