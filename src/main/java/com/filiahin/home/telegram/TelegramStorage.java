package com.filiahin.home.telegram;

import com.fasterxml.jackson.core.type.TypeReference;
import com.filiahin.home.storage.JsonStorage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TelegramStorage {
    private static final String FILEPATH = "../../telegram.json";
    private final JsonStorage<TelegramStorageEntities> storage;

    public TelegramStorage() {
        this.storage = new JsonStorage<>(FILEPATH);
        File file = new File(FILEPATH);
        if (!file.exists()) {
            this.storage.save(new TelegramStorageEntities());
        }
    }

    public List<TelegramStorageEntity> loadChats() {
        Optional<TelegramStorageEntities> load = storage.load(new TypeReference<>() {
        });
        return load.map(entities -> new ArrayList<>(entities.getTelegramClients()))
                .orElse(new ArrayList<>());
    }

    public void addChatId(String id, String name) {
        Optional<TelegramStorageEntities> load = storage.load(new TypeReference<>() {
        });
        load.ifPresent(config -> {
            List<TelegramStorageEntity> telegramClients = config.getTelegramClients();
            if (telegramClients.stream().noneMatch(client -> client.getChatId().equals(id))) {
                telegramClients.add(new TelegramStorageEntity(name, TelegramStatus.GUEST, id, "", new HashSet<>()));
                storage.save(config);
            }
        });
    }

    public boolean isDetectionEnabled() {
        Optional<TelegramStorageEntities> load = storage.load(new TypeReference<>() {
        });
        return load.map(TelegramStorageEntities::isDetectMotion)
                .orElse(false);
    }

    public boolean switchDetection() {
        boolean detection = false;
        Optional<TelegramStorageEntities> load = storage.load(new TypeReference<>() {
        });
        if (load.isPresent()) {
            TelegramStorageEntities config = load.get();
            detection = !config.isDetectMotion();
            config.setDetectMotion(detection);
            storage.save(config);
        }
        return detection;
    }

    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class TelegramStorageEntities {
        private List<TelegramStorageEntity> telegramClients = new ArrayList<>();
        private boolean detectMotion;
    }

    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static final class TelegramStorageEntity {
        private String name;
        private TelegramStatus status;
        private String chatId;
        private String mac;
        private Set<String> ips;
    }
}
