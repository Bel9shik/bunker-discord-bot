package listeners;

import controllers.GameController;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

public class ButtonsListener extends ListenerAdapter {

    private final GameController gameController;

    public ButtonsListener(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        gameController.doingFromButton(Objects.requireNonNull(event.getButton().getId()),
                Objects.requireNonNull(event.getMember()).getUser().getId(),
                event.getChannel().getName(),
                event.getMember().getUser().getName());
    }
}
