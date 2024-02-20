package com.filiahin.home.settings;

import com.filiahin.home.telegram.SettingsStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SettingsService {
    private final static Logger logger = LogManager.getLogger(SettingsService.class);

    @Autowired
    private SettingsStorage storage;

    public List<Setting<?>> findAll() {
        return storage.load().orElse(new ArrayList<>());
    }

    public void save(Setting<?> setting) {
        logger.info("Save setting " + setting);
        storage.save(setting);
    }

    public boolean openMorning() {
        return enabled(Settings.MORNING_OPEN.getKey());
    }

    public boolean closeEvening() {
        return enabled(Settings.EVENING_CLOSE.getKey());
    }

    public boolean closeDinner() {
        return enabled(Settings.DINNER_CLOSE.getKey());
    }

    public boolean closeBrightBlinds() {
        return enabled(Settings.CLOSE_BRIGHT_BLINDS.getKey());
    }

    public boolean notifyEveryHalfHour() {
        return enabled(Settings.NOTIFY_EVERY_HALF_HOUR.getKey());
    }

    public boolean notifyBeforeBlackZone() {
        return enabled(Settings.NOTIFY_BEFORE_BLACK_ZONE.getKey());
    }

    public boolean boostBathroomFloor() {
        return enabled(Settings.BOOST_BATHROOM_FLOOR.getKey());
    }

    public boolean autoWaterPump() {
        return enabled(Settings.AUTO_WATER_PUMP.getKey());
    }

    public boolean autoConditionerOff() {
        return enabled(Settings.AUTO_CONDITIONER_OFF.getKey());
    }

    public boolean logElectricityOn() {
        return enabled(Settings.LOG_ELECTRICITY_ON.getKey());
    }

    private boolean enabled(String key) {
        Optional<List<Setting<?>>> load = storage.load();
        return load.flatMap(settings -> settings.stream()
                        .filter(setting -> setting.getKey().equals(key))
                        .findFirst()
                        .map(s -> (Boolean) s.getValue()))
                .orElse(false);
    }
}
