package startGame;

import controllers.GameController;
import listeners.ButtonsListener;
import listeners.TildaCommandListener;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.List;

public class StartBot {
    private static final String TOKEN = "TOKEN";
    private static final JDABuilder jdaBuilder = JDABuilder.createDefault(TOKEN);
    public static final List<Thread> gameThreads = new ArrayList<>(); // TODO: если вдруг понадобится работать над несколькими играми

    public static void main(String[] args) {
        jdaBuilder.enableIntents(
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_VOICE_STATES
        );
        GameController gameController = new GameController();
        jdaBuilder.addEventListeners(new TildaCommandListener(gameController), new ButtonsListener(gameController));
        jdaBuilder.setActivity(Activity.customStatus("type ~info"));
        jdaBuilder.build();
    }

}


