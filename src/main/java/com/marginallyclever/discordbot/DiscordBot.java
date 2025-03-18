package com.marginallyclever.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class DiscordBot extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);

    private final Class<?> owner;
    private final JDA jda;
    private Guild guild;
    private final List<String> voiceChannels = new ArrayList<>();
    private AudioManager audioManager=null;
    private final EventListenerList listeners = new EventListenerList();

    private void forgetToken() {
        Preferences preferences = Preferences.userNodeForPackage(owner);
        preferences.remove("token");
    }

    /**
     * Get the bot token from Preferences.  if not available, ask the user and save it to preferences.
     * @return the token or "" if the user cancels.
     */
    private String getTokenFromPreferencesOrQueryUser() {
        // read the token from preferences
        Preferences preferences = Preferences.userNodeForPackage(owner);
        String token = preferences.get("token", "");
        if(token.isEmpty()) {
            // prompt the user for the token
            token = javax.swing.JOptionPane.showInputDialog("Please enter your Discord bot token.");
            preferences.put("token", token);
        }
        return token;
    }

    public DiscordBot(@NotNull Class<?> owner) {
        super();
        this.owner = owner;
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

    public void setGuild(String item) {
        guild = jda.getGuilds().stream().filter(g->g.getName().contentEquals(item)).findFirst().orElse(null);
        if(guild==null) {
            logger.error("Could not find guild "+item);
            return;
        }

        voiceChannels.clear();
        guild.getVoiceChannels().forEach(channel -> voiceChannels.add(channel.getName()));

        // get the audio manager
        audioManager = guild.getAudioManager();
    }

    public List<String> getGuilds() {
        var guilds = new ArrayList<String>();
        jda.getGuilds().forEach(guild -> guilds.add(guild.getName()));
        return guilds;
    }

    public List<String> getVoiceChannels() {
        return voiceChannels;
    }

    public AudioManager getAudioManager() {
        return audioManager;
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

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);
        for( var ear : listeners.getListeners(MessageReceivedEventListener.class) ) {
            ear.onMessageReceived(event);
        }
    }

    public void addMessageListener(MessageReceivedEventListener listener) {
        listeners.add(MessageReceivedEventListener.class, listener);
    }

    public void removeMessageListener(MessageReceivedEventListener listener) {
        listeners.remove(MessageReceivedEventListener.class, listener);
    }
}
