package com.filiahin.home.climate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.filiahin.home.nodes.ClimateDto;
import com.filiahin.home.nodes.NodesService;
import com.filiahin.home.nodes.Room;
import com.filiahin.home.storage.JsonStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClimateService {
    private static final Logger logger = LoggerFactory.getLogger(ClimateService.class);

    private static final String FOLDERPATH = "../../climate-log";

    private final NodesService nodesService;

    public ClimateService(NodesService nodesService) {
        this.nodesService = nodesService;
        File roomDir = new File(FOLDERPATH);
        if (!roomDir.exists()) {
            roomDir.mkdirs();
        }
    }

    public void log() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String isoLocalDate = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String isoLocalTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_TIME);

        Map<Room, ClimateDto> currentState = Stream.of(Room.values())
                .map(room -> new AbstractMap.SimpleEntry<>(room, nodesService.climate(room)))
                .filter(entry -> entry.getValue().isPresent())
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().get()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        String filePathString = FOLDERPATH + "/" + isoLocalDate + ".json";
        File file = new File(filePathString);
        ClimateJson climateJson = new ClimateJson();
        JsonStorage<ClimateJson> storage = new JsonStorage<>(filePathString);
        if (file.exists()) {
            Optional<ClimateJson> jsonOpt = storage.load(new TypeReference<>() {
            });
            if (jsonOpt.isPresent()) {
                climateJson = jsonOpt.get();
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        climateJson.getRecords().add(new ClimateRecord(isoLocalTime, currentState));
        storage.save(climateJson);
    }

    public List<String> dates() {
        try {
            return Files.list(Paths.get(FOLDERPATH))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(filename -> filename.replaceAll(".json", ""))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Get all day log for an apartment
     *
     * @param date i.e. 2023-08-05
     * @return content of json file <date>.json
     */
    public Optional<ClimateJson> get(String date) {
        String filePathString = FOLDERPATH + "/" + date + ".json";
        File file = new File(filePathString);
        if (file.exists()) {
            return new JsonStorage<ClimateJson>(filePathString).load(new TypeReference<>() {
            });
        }
        return Optional.empty();
    }

    /**
     * Get current state of the apartment
     *
     * @return map of rooms and current state
     */
    public ClimateRecord get() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String isoLocalTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_TIME);

        Map<Room, ClimateDto> dtos = Stream.of(Room.values())
                .map(room -> {
                    Optional<ClimateDto> climate = nodesService.climate(room);
                    if (climate.isPresent()) {
                        return new AbstractMap.SimpleEntry<>(room, climate);
                    }
                    return new AbstractMap.SimpleEntry<>(room, Optional.empty());
                })
                .filter(entry -> entry.getValue().isPresent())
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), (ClimateDto) entry.getValue().get()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

        return new ClimateRecord(isoLocalTime, dtos);
    }

    /**
     * Get record for a day and time
     *
     * @param isoLocalDate date
     * @param isoLocalTime time
     * @return record for a specific time
     */
    public Optional<ClimateRecord> get(String isoLocalDate, String isoLocalTime) {
        String filePathString = FOLDERPATH + "/" + isoLocalDate + ".json";
        File file = new File(filePathString);
        JsonStorage<ClimateJson> storage = new JsonStorage<>(filePathString);
        if (file.exists()) {
            Optional<ClimateJson> jsonOpt = storage.load(new TypeReference<>() {
            });
            if (jsonOpt.isPresent()) {
                ClimateJson json = jsonOpt.get();
                return json.getRecords().stream()
                        .filter(record -> record.getTime().equals(isoLocalTime))
                        .findFirst();
            }
        }

        return Optional.empty();
    }
}
