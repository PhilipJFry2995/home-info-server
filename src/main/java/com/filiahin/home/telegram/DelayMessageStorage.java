package com.filiahin.home.telegram;

import com.fasterxml.jackson.core.type.TypeReference;
import com.filiahin.home.storage.JsonStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DelayMessageStorage {
    private static final String FILEPATH = "../../delay_message.txt";
    private final JsonStorage<List<String>> storage;

    public DelayMessageStorage() {
        this.storage = new JsonStorage<>(FILEPATH);
        File file = new File(FILEPATH);
        if (!file.exists()) {
            this.storage.save(new ArrayList<>());
        }
    }

    public List<String> popMessages() {
        Optional<List<String>> load = storage.load(new TypeReference<>() {
        });
        storage.save(new ArrayList<>());
        return load.orElse(new ArrayList<>());
    }

    public void addMessage(String message) {
        Optional<List<String>> load = storage.load(new TypeReference<>() {
        });
        load.ifPresent(messages -> {
            messages.add(message);
            storage.save(messages);
        });
    }
}
