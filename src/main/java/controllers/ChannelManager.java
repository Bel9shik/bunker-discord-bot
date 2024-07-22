package controllers;

import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ChannelManager {
    @Getter
    private final HashMap<String, TextChannel> openTextChannels = new HashMap<>();
    @Getter
    private final HashMap<String, TextChannel> closeTextChannels = new HashMap<>();
    @Getter
    private TextChannel generalChannel;
    private Guild guild;

    public boolean validateBunkerCategory(MessageReceivedEvent event) {
        AtomicReference<Category> bunkerCategory = new AtomicReference<>();
        AtomicBoolean flag = new AtomicBoolean(false);

        event.getGuild().getCategories().forEach(category -> {
            if (category.getName().equalsIgnoreCase("bunker")) {
                flag.set(true);
                bunkerCategory.set(category);
            }
        });

        return flag.get();
    }

    public List<Member> getVoiceChannelMembers(MessageReceivedEvent event) {
        return Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel()).getMembers();
    }

    public void createGeneralChannel(MessageReceivedEvent event) {
        guild = event.getGuild();
        Category bunkerCategory = guild.getCategoriesByName("bunker", true).get(0);

        generalChannel = bunkerCategory.createTextChannel("GAME").complete();
    }

    public void createTextChannels(MessageReceivedEvent event, List<Member> members) {
        Category bunkerCategory = guild.getCategoriesByName("bunker", true).get(0);

        for (Member member : members) {
            TextChannel closeTextChannel = bunkerCategory.createTextChannel(member.getUser().getName() + "-closed")
                    .addPermissionOverride(event.getGuild().getPublicRole(), EnumSet.noneOf(Permission.class), EnumSet.of(Permission.VIEW_CHANNEL))
                    .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.noneOf(Permission.class))
                    .complete();

            closeTextChannels.put(member.getUser().getId(), closeTextChannel);

            TextChannel openTextChannel = bunkerCategory.createTextChannel(member.getUser().getName()).complete();
            openTextChannels.put(member.getUser().getId(), openTextChannel);
        }
    }

    public void deleteAllChannels() {
        openTextChannels.forEach((key, value) -> value.delete().queue());
        closeTextChannels.forEach((key, value) -> value.delete().queue());
        generalChannel.delete().queue();
        openTextChannels.clear();
        closeTextChannels.clear();
        generalChannel = null;
    }

    public void deleteChannelsByPlayerID(String deleteID) {
        System.out.println(openTextChannels.get(deleteID));
        openTextChannels.get(deleteID).delete().queue();
        openTextChannels.remove(deleteID);
        closeTextChannels.get(deleteID).delete().queue();
        closeTextChannels.remove(deleteID);
    }

    public void sendButtonsInChannel(TextChannel channel, List<Button> allButtons) {
        List<List<Button>> listList = new ArrayList<>();

        if (allButtons.size() > 5) {
            int indexLastList = 0;
            int countInOneList = 0;
            listList.add(new ArrayList<>());

            for (Button button : allButtons) {
                countInOneList++;
                if (countInOneList == 5) {
                    listList.add(new ArrayList<>());
                    indexLastList++;
                    countInOneList = 0;
                }
                listList.get(indexLastList).add(button);
            }
        } else {
            listList.add(allButtons);
        }

        MessageCreateAction messageCreateAction = channel.sendMessage("Что хотите показать?");

        for (List<Button> buttons : listList) {
            messageCreateAction.addActionRow(buttons);
        }

        messageCreateAction.queue();
    }

}
