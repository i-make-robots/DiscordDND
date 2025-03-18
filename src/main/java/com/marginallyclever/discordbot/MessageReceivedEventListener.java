package com.marginallyclever.discordbot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.EventListener;

/**
 * This interface is used to listen for messages received by the Discord bot.
 */
public interface MessageReceivedEventListener extends EventListener {
    void onMessageReceived(MessageReceivedEvent event);
}
