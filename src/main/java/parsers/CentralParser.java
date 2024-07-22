package parsers;

import lombok.extern.slf4j.Slf4j;
import model.Bunker;
import model.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class CentralParser {

    private final List<String> data;

    private final String playerCharacters = "gameParameters.txt";
    private final String bunkersInfo = "bunkersInfo.csv";
    private final String worldDisasterInfo = "worldDisasters.txt";
    private final String durationTime = "durationTime.txt";

    public CentralParser() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(playerCharacters)) {
            if (inputStream != null) {
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                    data = new ArrayList<>();
                    bufferedReader.lines().forEach(data::add);

                    for (int i = 0; i < data.size(); i++) {
                        data.set(i, data.get(i).substring(data.get(i).indexOf(":") + 1).trim());
                        data.set(i, data.get(i).replaceAll(", ", ","));
                    }
                }
            } else {
                log.error("file not found");
                throw new RuntimeException("file not found");
            }
        } catch (IOException e) {
            log.error("incorrect format file {}", "gameParameters.txt", e);
            throw new RuntimeException(e);
        }
    }

    public static final Random RANDOM = new Random();

    public Bunker getBunker(int playerAmount) {
        Bunker bunker;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(bunkersInfo)) {
            if (inputStream != null) {
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                int less, more;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data[0].contains("-")) {
                        try {
                            less = Integer.parseInt(data[0].substring(0, data[0].indexOf("-")));
                            more = Integer.parseInt(data[0].substring(data[0].indexOf("-") + 1));
                            if (playerAmount >= less && playerAmount <= more) {
                                bunker = Bunker.builder()
                                        .playerAmount(playerAmount)
                                        .bunkerCapacity(Integer.parseInt(data[1]))
                                        .charactersInFirstRound(Integer.parseInt(data[2]))
                                        .powerDuration(getDurationTime())
                                        .build();
                                return bunker;
                            }
                        } catch (NumberFormatException e) {
                            log.error("incorrect format file {}", bunkersInfo, e);
                            throw new RuntimeException("incorrect format file");
                        }
                    }
                }
                log.error("this is number of players not supported");
                throw new RuntimeException("this is number of players not supported");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        throw new RuntimeException("bunker not supported");
    }

    public String getWorldDisaster() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(worldDisasterInfo)) {
            if (inputStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    List<String> worldDisaster = new ArrayList<>();
                    reader.lines().forEach(worldDisaster::add);
                    return worldDisaster.get(RANDOM.nextInt(worldDisaster.size())) + " \nВам потребуется убежище на ближайшие " + getDurationTime();
                }
            } else {
                log.error("file not found");
                throw new RuntimeException("file not found");
            }
        }
        catch (IOException e) {
            log.error("incorrect format file \"world disaster\"", e);
            throw new RuntimeException(e);
        }
    }

    public Player getPlayer(boolean forPrivatePlayer, String playerID) {

        Player player;

        if (forPrivatePlayer) {
            player = new Player(
                    "unknown",
                    0,
                    "x",
                    "x",
                    0,
                    "x",
                    "x",
                    "x",
                    "x",
                    "x",
                    "x",
                    "x",
                    "x",
                    "x"
            );
        } else {
            String[] ages = data.get(2).split("-");

            player = new Player(
                    playerID,
                    12,
                    data.get(0).split(",")[RANDOM.nextInt(data.get(0).split(",").length)],
                    data.get(1).split(",")[RANDOM.nextInt(data.get(1).split(",").length)],
                    RANDOM.nextInt(Integer.parseInt(ages[1]) - Integer.parseInt(ages[0])) + Integer.parseInt(ages[0]),
                    data.get(3).split(",")[RANDOM.nextInt(data.get(3).split(",").length)],
                    data.get(4).split(",")[RANDOM.nextInt(data.get(4).split(",").length)],
                    data.get(5).split(",")[RANDOM.nextInt(data.get(5).split(",").length)],
                    data.get(6).split(",")[RANDOM.nextInt(data.get(6).split(",").length)],
                    data.get(7).split(",")[RANDOM.nextInt(data.get(7).split(",").length)],
                    data.get(8).split(",")[RANDOM.nextInt(data.get(8).split(",").length)],
                    data.get(9).split(",")[RANDOM.nextInt(data.get(9).split(",").length)],
                    data.get(10).split(",")[RANDOM.nextInt(data.get(10).split(",").length)],
                    data.get(11).split(",")[RANDOM.nextInt(data.get(11).split(",").length)]
            );
        }


        return player;
    }

    private String getDurationTime() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(durationTime)) {
            if (inputStream == null) {
                throw new RuntimeException("error parsing duration time file");
            }
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                List<String> allDurations = new ArrayList<>();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    allDurations.add(line.trim());
                }
                return allDurations.get(RANDOM.nextInt(allDurations.size()));
            }
        } catch (IOException e) {
            throw new RuntimeException("file not found");
        }
    }
}
