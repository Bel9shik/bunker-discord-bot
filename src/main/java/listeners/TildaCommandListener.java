package listeners;

import controllers.GameController;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

@Slf4j
public class TildaCommandListener extends ListenerAdapter {

    private final String infoCommand = "~info";
    private final String startGame = "~startGame";
    private final String endGame = "~endGame";
    private final String nextMove = "~nextMove";
    private final String kickMember = "~kickMember";

    private GameController gameController;

    public TildaCommandListener(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (!event.isFromGuild()) {
            event.getChannel().sendMessage("Bot works only on servers").queue();
        }

        if (event.getAuthor().isBot()) return;

        MessageChannel channel = event.getChannel();

        boolean isBunkerCat = true;

        if (!isBunkerCategory(channel)) {
            isBunkerCat = false;
        }

        Message message = event.getMessage();
        String content = message.getContentDisplay();
        String author = message.getAuthor().getName();

        if (event.getChannelType().isMessage()) {
            log.info("Received message: {} from {}", content, author);
        }

        if (content.equals(infoCommand)) {
            channel.sendMessage("in game commands:\n" +
                            startGame + "\n" +
                            endGame + "\n")
                    .queue();
        } else if (content.equals(startGame) && isBunkerCat) {
            startGame(event);
        } else if (content.equals(endGame) && isBunkerCat) {
            endGame(event);
        } else if (content.equals(nextMove) && isBunkerCat && channel.equals(gameController.getChannelManager().getGeneralChannel())) {
            nextMove(event);
        } else if (content.contains(kickMember) && isBunkerCat) {
            kickMember(event);
        } else if (content.equals(startGame) || content.equals(endGame) || content.equals(nextMove)) {
            channel.sendMessage("you can start game only in category \"bunker\", in chat from voice channel").queue();
        }

    }

    private boolean isBunkerCategory(MessageChannel channel) {
        if (channel instanceof VoiceChannel voiceChannel) {
            Category category = voiceChannel.getParentCategory();
            return category != null && category.getName().equalsIgnoreCase("bunker");
        } else if (channel instanceof TextChannel textChannel) {
            Category category = textChannel.getParentCategory();
            return category != null && category.getName().equalsIgnoreCase("bunker");
        }
        return false;
    }

    private void endGame(MessageReceivedEvent event) {
        if (!gameController.isRunning()) {
            event.getChannel().sendMessage("game not started").queue();
            return;
        }

        gameController.endGame(event);
    }

    private void startGame(MessageReceivedEvent event) {

        if (gameController.isRunning()) {
            event.getChannel().sendMessage("the game has already started").queue();
            return;
        }

        try {
            gameController.startGame(event);
        } catch (IllegalArgumentException | NullPointerException e) {
            event.getChannel().sendMessage("channel can not be empty").queue();
        }
    }

    private void nextMove(MessageReceivedEvent event) {
        if (!gameController.isRunning()) {
            event.getChannel().sendMessage("game not started").queue();
            return;
        }

        gameController.nextMove(Objects.requireNonNull(event.getMember()).getUser().getId());

    }

    private void kickMember(MessageReceivedEvent event) {
        if (!gameController.isRunning()) {
            event.getChannel().sendMessage("game not started").queue();
            return;
        }

        String[] wordsInLine = event.getMessage().getContentDisplay().split(" ");
        if (wordsInLine.length < 2) {
            event.getChannel().sendMessage("Укажите ник пользователя").queue();
            return;
        } else if (wordsInLine.length > 2) {
            event.getChannel().sendMessage("Используйте \"~kickMember *nickname*\"").queue();
            return;
        }

        gameController.kickMember(Objects.requireNonNull(event.getMember()).getUser().getId(), wordsInLine[1].trim());
    }

}
