package com.filiahin.home.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class JsonStorage<T> {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String filepath;

    public JsonStorage(String filepath) {
        this.filepath = filepath;
    }

    public synchronized void save(T object) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filepath), object);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized Optional<T> load(TypeReference<T> typeReference) {
        try {
            return Optional.of(mapper.readValue(new File(filepath), typeReference));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<T> loadUnchecked(TypeReference<T> typeReference) throws IOException {
        return Optional.of(mapper.readValue(new File(filepath), typeReference));
    }
}
