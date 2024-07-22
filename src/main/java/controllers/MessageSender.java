package controllers;


import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class MessageSender {
    public void sendMessage(MessageChannel channel, String message) {
        channel.sendMessage(message).queue();
    }

    public void sendMessage(MessageChannel channel, MessageEmbed messageEmbed) {
        channel.sendMessageEmbeds(messageEmbed).queue();
    }
}
