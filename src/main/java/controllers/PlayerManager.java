package controllers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.Bunker;
import model.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import parsers.CentralParser;

import java.util.HashMap;
import java.util.List;

@Slf4j
public class PlayerManager {
    private final CentralParser centralParser;
    @Getter
    private final HashMap<String, Player> closeCharactersPlayers = new HashMap<>();
    @Getter
    private final HashMap<String, Player> openCharactersPlayers = new HashMap<>();

    public PlayerManager(CentralParser centralParser) {
        this.centralParser = centralParser;
    }

    public void initializePlayers(List<Member> members) {
        for (Member member : members) {
            closeCharactersPlayers.put(member.getUser().getId(), centralParser.getPlayer(true, member.getUser().getId()));
            openCharactersPlayers.put(member.getUser().getId(), centralParser.getPlayer(false, member.getUser().getId()));
        }
    }

    public void sendToOpenChannels(HashMap<String, TextChannel> openTextChannels) {
        for (String key : closeCharactersPlayers.keySet()) {
            openTextChannels.get(key).sendMessageEmbeds(createMessageEmbed(closeCharactersPlayers.get(key))).queue();
        }
    }

    public void sendToCloseChannels(HashMap<String, TextChannel> closeTextChannels) {
        for (String key : openCharactersPlayers.keySet()) {
            closeTextChannels.get(key).sendMessageEmbeds(createMessageEmbed(openCharactersPlayers.get(key))).queue();
        }
    }

    public MessageEmbed createMessageEmbed(Player player) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.addField("Пол: ", player.getSex(), true);
        embedBuilder.addField("Ориентация: ", player.getSexualOrientation(), true);
        embedBuilder.addField("Возраст: ", String.valueOf(player.getAge()), true);
        embedBuilder.addField("Телосложение: ", player.getBodyType(), true);
        embedBuilder.addField("Человеческая черта: ", player.getHumanTrait(), true);
        embedBuilder.addField("Специальность: ", player.getSpeciality(), true);
        embedBuilder.addField("Здоровье: ", player.getHealth(), true);
        embedBuilder.addField("Хобби: ", player.getHobby(), true);
        embedBuilder.addField("Фобия: ", player.getPhobia(), true);
        embedBuilder.addField("Инвентарь: ", player.getInventory(), true);
        embedBuilder.addField("Доп. информация: ", player.getAdditionalInfo(), true);
        embedBuilder.addField("Спец. возможность: ", player.getSpeciallyOpportunity(), true);

        return embedBuilder.build();
    }

    public boolean changeCharacterFromButton(String buttonID, String userID, String channelName, String userName, int neededOpenedCharacters) {

        if (!channelName.equals(userName)) {
            log.info("channel name: " + channelName);
            log.info("userName: " + userName);
            log.info("не тот пользователь отправил запрос");
            return false;
        }

        boolean changed = false;
        Player player = closeCharactersPlayers.get(userID);

        if (player == null || openCharactersPlayers.get(userID) == null) {
            log.error("player was not created");
            return false;
        }

        if (player.getOpenedCharacters() == neededOpenedCharacters) {
            return false;
        }

        switch (buttonID) {
            case "sex" -> {
                player.setSex(openCharactersPlayers.get(userID).getSex());
                changed = true;
            }
            case "sexualOrientation" -> {
                player.setSexualOrientation(openCharactersPlayers.get(userID).getSexualOrientation());
                changed = true;
            }
            case "age" -> {
                player.setAge(openCharactersPlayers.get(userID).getAge());
                changed = true;
            }
            case "bodyType" -> {
                player.setBodyType(openCharactersPlayers.get(userID).getBodyType());
                changed = true;
            }
            case "humanTrait" -> {
                player.setHumanTrait(openCharactersPlayers.get(userID).getHumanTrait());
                changed = true;
            }
            case "speciality" -> {
                player.setSpeciality(openCharactersPlayers.get(userID).getSpeciality());
                changed = true;
            }
            case "health" -> {
                player.setHealth(openCharactersPlayers.get(userID).getHealth());
                changed = true;
            }
            case "hobby" -> {
                player.setHobby(openCharactersPlayers.get(userID).getHobby());
                changed = true;
            }
            case "phobia" -> {
                player.setPhobia(openCharactersPlayers.get(userID).getPhobia());
                changed = true;
            }
            case "inventory" -> {
                player.setInventory(openCharactersPlayers.get(userID).getInventory());
                changed = true;
            }
            case "additionalInfo" -> {
                player.setAdditionalInfo(openCharactersPlayers.get(userID).getAdditionalInfo());
                changed = true;
            }
            case "speciallyOpportunity" -> {
                player.setSpeciallyOpportunity(openCharactersPlayers.get(userID).getSpeciallyOpportunity());
                changed = true;
            }
        }
        int openedCharacters = player.getOpenedCharacters();
        if (changed) player.setOpenedCharacters(openedCharacters + 1);

        return changed;
    }

    public void sendGeneralInfoInGeneralChannel(TextChannel generalChannel, Bunker bunker) {

        generalChannel.sendMessage("Игра скоро начнётся").queue();
        generalChannel.sendMessage(centralParser.getWorldDisaster()).queue();
        generalChannel.sendMessage("Вы остановились возле этого бункера:").queue();

        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Bunker");
        embedBuilder.addField("Вместимость: ", String.valueOf(bunker.getBunkerCapacity()), false);
        embedBuilder.addField("Раскрыть характеристик в 1 ходу: ", String.valueOf(bunker.getCharactersInFirstRound()), false);
        embedBuilder.addField("На сколько хватит еды: ", String.valueOf(bunker.getPowerDuration()), false);

        generalChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public boolean canPlayerContinue (Member member, int neededOpenedCharacters) {
        boolean canContinue = false;
        Player player = closeCharactersPlayers.get(member.getUser().getId());
        if (player != null && player.getOpenedCharacters() == neededOpenedCharacters) {
            canContinue = true;
        }
        return canContinue;
    }

    public void deletePlayerByID(String playerID) {
        closeCharactersPlayers.remove(playerID);
        openCharactersPlayers.remove(playerID);
    }

}
