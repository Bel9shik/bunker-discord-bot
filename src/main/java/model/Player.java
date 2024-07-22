package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class Player {
    String playerID;
    int openedCharacters;
    String sex;
    String sexualOrientation;
    int age;
    String bodyType;
    String humanTrait;
    String speciality;
    String health;
    String hobby;
    String phobia;
    String inventory;
    String additionalInfo;
    String speciallyOpportunity;
}
