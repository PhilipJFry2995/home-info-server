package com.filiahin.home.nodes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.filiahin.home.climate.ClimateJson;
import com.filiahin.home.storage.JsonStorage;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NodesService {
    private static final String FILEPATH = "../../nodes.json";
    private static final Map<Room, AbstractNode> NODES = new HashMap<>();

    public NodesService() {
        File nodesFile = new File(FILEPATH);
        if (!nodesFile.exists()) {
            throw new RuntimeException(nodesFile.getAbsolutePath() + " doesn't exist");
        }

        readNodeConfiguration();
    }

    private void readNodeConfiguration() {
        JsonStorage<List<LinkedHashMap<String, String>>> storage = new JsonStorage<>(FILEPATH);
        Optional<List<LinkedHashMap<String, String>>> nodesOpt = storage.load(new TypeReference<>() {
        });

        if (nodesOpt.isEmpty()) {
            throw new RuntimeException("nodes not loaded doesn't exist");
        }

        nodesOpt.get()
                .forEach(node -> {
                    Room room = Room.valueOf(node.get("room"));
                    Esp32Node esp = new Esp32Node(node.get("ip"));
                    NODES.put(room, esp);
                });
    }

    public Optional<ClimateDto> climate(Room room) {
        readNodeConfiguration();

        if (!NODES.containsKey(room)) {
            return Optional.empty();
        }

        return NODES.get(room).climate();
    }
}
