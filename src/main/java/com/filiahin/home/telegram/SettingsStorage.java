package com.filiahin.home.telegram;

import com.fasterxml.jackson.core.type.TypeReference;
import com.filiahin.home.settings.Setting;
import com.filiahin.home.settings.Settings;
import com.filiahin.home.storage.JsonStorage;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class SettingsStorage {
    private static final String FILEPATH = "home.json";
    private final JsonStorage<List<Setting<?>>> storage;

    public SettingsStorage() {
        this.storage = new JsonStorage<>(FILEPATH);
        File file = new File(FILEPATH);
        if (!file.exists()) {
            this.storage.save(Settings.values());
        } else {
            List<Setting<?>> settings = load().get();
            if (settings.size() != Settings.values().size()) {
                Settings.values().forEach(defaultSetting -> {
                    if (!settings.contains(defaultSetting)) {
                        settings.add(defaultSetting);
                    }
                });
                this.storage.save(settings);
            }
        }
    }

    public Optional<List<Setting<?>>> load() {
        return storage.load(new TypeReference<>() {
        });
    }

    public void save(Setting<?> setting) {
        Optional<List<Setting<?>>> settingsOpt = load();
        settingsOpt.ifPresent((settings) -> {
            settings.stream()
                    .filter(s -> s.getKey().equals(setting.getKey()))
                    .findFirst()
                    .ifPresent(st ->
                            settings.set(settings.indexOf(st), setting));
            storage.save(settings);
        });
    }
}
