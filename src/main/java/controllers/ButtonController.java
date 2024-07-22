package controllers;

import model.Player;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

public class ButtonController {

    public List<Button> getButtonsForPlayer(Player player) {
        List<Button> allButtons = new ArrayList<>();

        if (player.getSex().equals("x")) {
            allButtons.add(Button.primary("sex", "Пол"));
        }
        if (player.getSexualOrientation().equals("x")) {
            allButtons.add(Button.primary("sexualOrientation", "Ориентация"));
        }
        if (player.getAge() == 0) {
            allButtons.add(Button.primary("age", "Возраст"));
        }
        if (player.getBodyType().equals("x")) {
            allButtons.add(Button.primary("bodyType", "Телосложение"));
        }
        if (player.getHumanTrait().equals("x")) {
            allButtons.add(Button.primary("humanTrait", "Человеческая черта"));
        }
        if (player.getSpeciality().equals("x")) {
            allButtons.add(Button.primary("speciality", "Специальность"));
        }
        if (player.getHealth().equals("x")) {
            allButtons.add(Button.primary("health", "Здоровье"));
        }
        if (player.getHobby().equals("x")) {
            allButtons.add(Button.primary("hobby", "Хобби"));
        }
        if (player.getPhobia().equals("x")) {
            allButtons.add(Button.primary("phobia", "Фобия"));
        }
        if (player.getInventory().equals("x")) {
            allButtons.add(Button.primary("inventory", "Инвентарь"));
        }
        if (player.getAdditionalInfo().equals("x")) {
            allButtons.add(Button.primary("additionalInfo", "Доп. информация"));
        }
        if (player.getSpeciallyOpportunity().equals("x")) {
            allButtons.add(Button.primary("speciallyOpportunity", "Спец. возможность"));
        }

        System.out.println("buttonsSize: " + allButtons.size());
        if (allButtons.isEmpty()) {
            return null;
        }

        return allButtons;
    }

    public List<Button> getButtonsForGeneralChannel() { //TODO: надо как-то обрабатывать. Вопрос с окончанием игры и исключение игрока

        List<Button> buttons = new ArrayList<>();

        buttons.add(Button.primary("nextMove", "Следующий ход"));
        buttons.add(Button.primary("kickMember", "Исключить игрока"));
        buttons.add(Button.danger("endGame", "Закончить игру"));

        return buttons;
    }
}
