package controllers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.Bunker;
import model.Player;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import parsers.CentralParser;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class GameController {
    private List<Member> members;
    private final CentralParser centralParser = new CentralParser();
    @Getter
    private boolean isRunning = false;
    @Getter
    private final ChannelManager channelManager = new ChannelManager();
    private final PlayerManager playerManager = new PlayerManager(centralParser);
    private final MessageSender messageSender = new MessageSender();
    private final ButtonController buttonController = new ButtonController();
    private int neededOpenedCharacters = 0;
    private boolean isVotingAllowed;
    private String userIDStartedGame;
    private Bunker bunker;

    public void startGame(MessageReceivedEvent event) {

        if (!conditionForGame(event)) {
            return;
        }

        isRunning = true;

        members = channelManager.getVoiceChannelMembers(event);

        if (members.isEmpty()) {
            throw new IllegalArgumentException("members cannot be null or empty");
        }

        userIDStartedGame = Objects.requireNonNull(event.getMember()).getUser().getId();

        playerManager.initializePlayers(members);
        channelManager.createGeneralChannel(event);

        try {
            bunker = centralParser.getBunker(members.size());
        } catch (RuntimeException e) {
            messageSender.sendMessage(channelManager.getGeneralChannel(), "This is number of players not supported. The bot supports from 4 to 15 players");
            return;
        }

        playerManager.sendGeneralInfoInGeneralChannel(channelManager.getGeneralChannel(), bunker);

        neededOpenedCharacters = bunker.getCharactersInFirstRound();

        channelManager.createTextChannels(event, members);

        playerManager.sendToCloseChannels(channelManager.getCloseTextChannels());
        playerManager.sendToOpenChannels(channelManager.getOpenTextChannels());

        for (Member member : members) {
            List<Button> buttons = buttonController.getButtonsForPlayer(playerManager.getCloseCharactersPlayers().get(member.getUser().getId()));
            channelManager.sendButtonsInChannel(channelManager.getOpenTextChannels().get(member.getUser().getId()), buttons);
        }

//        channelManager.sendButtonsInChannel(channelManager.getGeneralChannel(), buttonController.getButtonsForGeneralChannel());
    }

    public void endGame(MessageReceivedEvent event) {

        if (!conditionForGame(event)) {
            return;
        }

        if (!Objects.requireNonNull(event.getMember()).getUser().getId().equals(userIDStartedGame)) {
            event.getChannel().sendMessage("The game can only be finished by the player who started it").queue();
            return;
        }

        channelManager.deleteAllChannels();
        isRunning = false;
        userIDStartedGame = "";
        members = null;
    }

    private boolean conditionForGame (MessageReceivedEvent event) {
        if (!event.getChannelType().isAudio()) {
            return false;
        }

        if (!channelManager.validateBunkerCategory(event)) {
            return false;
        }

        return true;
    }

    public void doingFromButton(String buttonID, String userID, String channelName, String userName) {
        if (buttonID.equals("sex") || buttonID.equals("sexualOrientation") || buttonID.equals("age") ||
        buttonID.equals("bodyType") || buttonID.equals("humanTrait") || buttonID.equals("speciality") ||
        buttonID.equals("health") || buttonID.equals("hobby") || buttonID.equals("phobia") || buttonID.equals("inventory") ||
        buttonID.equals("additionalInfo") || buttonID.equals("speciallyOpportunity"))  {
            if (changeCharacterFromButton(buttonID, userID, channelName, userName) && !isVotingAllowed) {
                TextChannel textChannel = channelManager.getOpenTextChannels().get(userID);
                Player player = playerManager.getCloseCharactersPlayers().get(userID);
                messageSender.sendMessage(textChannel, playerManager.createMessageEmbed(player));
                channelManager.sendButtonsInChannel(textChannel, buttonController.getButtonsForPlayer(player));
            }
        }
    }

    private boolean changeCharacterFromButton(String buttonID, String userID, String channelName, String userName) {
        return playerManager.changeCharacterFromButton(buttonID, userID, channelName, userName, neededOpenedCharacters);
    }

    public void nextMove(String userID) {
        if (!userID.equalsIgnoreCase(userIDStartedGame)) {
            messageSender.sendMessage(channelManager.getGeneralChannel(), "Следующий круг может запускать только ведущий");
            return;
        }

        if (isVotingAllowed) {
            messageSender.sendMessage(channelManager.getGeneralChannel(), "Вам нужно проголосовать за исключение 1 игрока");
            return;
        }

        boolean flag = true;
        for (Member member : members) {
            if (!playerManager.canPlayerContinue(member, neededOpenedCharacters) && playerManager.getOpenCharactersPlayers().get(member.getUser().getId()) != null) {
                messageSender.sendMessage(channelManager.getGeneralChannel(),member.getUser().getName() + " не может продолжить игру. Нужно открыть ещё " + (neededOpenedCharacters - playerManager.getCloseCharactersPlayers().get(member.getUser().getId()).getOpenedCharacters()) + " характеристик(у)");
                flag = false;
            }
        }

        if (flag) {
            messageSender.sendMessage(channelManager.getGeneralChannel(), "Вам нужно проголосовать за исключение 1 игрока");
            isVotingAllowed = true;
        }
    }

    private boolean isEndGame() {
        return playerManager.getCloseCharactersPlayers().size() == bunker.getBunkerCapacity();
    }

    public void kickMember(String userID, String deleteName) {
        if (!userID.equalsIgnoreCase(userIDStartedGame)) {
            messageSender.sendMessage(channelManager.getGeneralChannel(), "Исключать может только игрок, начавший игру");
            return;
        }
        if (!isVotingAllowed) {
            messageSender.sendMessage(channelManager.getGeneralChannel(), "На данный момент не нужно исключать игроков");
            return;
        }

        String deleteID = null;

        for (Member member : members) {
            if (member.getUser().getName().equalsIgnoreCase(deleteName)) {
                deleteID = member.getUser().getId();
                break;
            }
        }
        if (deleteID == null || deleteID.isEmpty()) {
            messageSender.sendMessage(channelManager.getGeneralChannel(), "Не удалось найти такого игрока");
            return;
        }

        channelManager.deleteChannelsByPlayerID(deleteID);
        playerManager.deletePlayerByID(deleteID);

        isVotingAllowed = false;

        if (isEndGame()) {
            messageSender.sendMessage(channelManager.getGeneralChannel(), "Игра закончена!\nВ бункер сумели попасть:");
            for (Map.Entry<String, Player> entry: playerManager.getOpenCharactersPlayers().entrySet()) {
                for (Member member : members) {
                    if (member.getUser().getId().equalsIgnoreCase(entry.getKey())) {
                        messageSender.sendMessage(channelManager.getGeneralChannel(), playerManager.createMessageEmbed(playerManager.getOpenCharactersPlayers().get(member.getUser().getId())));
                        break;
                    }
                }
            }
        } else {
            messageSender.sendMessage(channelManager.getGeneralChannel(), "Нужно открыть ещё по 1 характеристике");
            neededOpenedCharacters++;
        }
    }
}
