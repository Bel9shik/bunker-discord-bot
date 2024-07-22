package model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class Bunker {
    private final int playerAmount;
    private final int bunkerCapacity;
    private final int charactersInFirstRound;
    private final String powerDuration;
}
