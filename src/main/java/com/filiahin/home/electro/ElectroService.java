package com.filiahin.home.electro;

import com.fasterxml.jackson.core.type.TypeReference;
import com.filiahin.home.settings.SettingsService;
import com.filiahin.home.storage.JsonStorage;
import com.filiahin.home.telegram.NetworkService;
import com.filiahin.home.telegram.TelegramService;
import com.filiahin.home.utils.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ElectroService {
    private static final Logger logger = LoggerFactory.getLogger(ElectroService.class);
    private static final String FILEPATH = "../../electro.json";
    private static final String SCHEDULE_FILE = "../../schedule.json";
    private final JsonStorage<ElectroEntities> storage;
    private final AtomicReference<ElectroEntities> electroEntities = new AtomicReference<>();
    private final JsonStorage<ScheduleEntities> scheduleStorage;
    private final NetworkService networkService;
    private final TelegramService telegramService;
    private final List<ElectroEventsListener> listeners = new ArrayList<>();
    private boolean isElectricityAvailable;
    @Autowired
    private SettingsService settingsService;

    public ElectroService(NetworkService networkService, TelegramService telegramService) {
        this.networkService = networkService;
        this.telegramService = telegramService;
        subscribe(telegramService);
        this.storage = new JsonStorage<>(FILEPATH);
        this.scheduleStorage = new JsonStorage<>(SCHEDULE_FILE);
        File file = new File(FILEPATH);
        if (!file.exists()) {
            this.storage.save(new ElectroEntities());
            electroEntities.set(new ElectroEntities());
        } else {
            Optional<ElectroEntities> datesOpt;
            try {
                datesOpt = storage.loadUnchecked(new TypeReference<>() {
                });
            } catch (Exception e) {
                datesOpt = Optional.empty();
                logger.error("Electro storage not found!");
            }

            if (datesOpt.isEmpty()) {
                electroEntities.set(new ElectroEntities());
                storage.save(electroEntities.get());
                telegramService.sendAdminMessage("Electro data is corrupted. New file created");
            } else {
                electroEntities.set(datesOpt.get());
            }
        }
        isElectricityAvailable = false; // networkService.isIpReachable(NetworkService.ROUTER_IP);
    }

    public void subscribe(ElectroEventsListener listener) {
        this.listeners.add(listener);
    }

    public void unsubscribe(ElectroEventsListener listener) {
        this.listeners.remove(listener);
    }

    public ElectroEntities getData() {
        return electroEntities.get();
    }

    public ScheduleEntities getSchedule() {
        Optional<ScheduleEntities> datesOpt = scheduleStorage.load(new TypeReference<>() {
        });
        if (datesOpt.isEmpty()) {
            logger.error("Schedule storage not found!");
            return null;
        }
        return datesOpt.get();
    }

    public void log() {
        boolean isElectricityNowAvailable = networkService.isIpReachable(NetworkService.ROUTER_IP);
        if (isElectricityAvailable != isElectricityNowAvailable) {
            if (isElectricityNowAvailable) {
                listeners.forEach(ElectroEventsListener::onElectricityOn);
                sendDarkTimeMessage();
            } else {
                listeners.forEach(ElectroEventsListener::onElectricityOff);
            }
        }
        isElectricityAvailable = isElectricityNowAvailable;
        if (!isElectricityAvailable) {
            logger.warn("Router is not reachable. Electricity is absent");
            return;
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        String isoLocalDate = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String isoLocalDateTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        ElectroEntities dates = electroEntities.get();
        Optional<ElectroEntity> todayOpt = dates.dates.stream()
                .filter(ent -> ent.getDate().equals(isoLocalDate))
                .findFirst();
        if (todayOpt.isPresent()) {
            ElectroEntity today = todayOpt.get();
            Pair lastPeriod = today.periods
                    .get(today.periods.size() - 1);
            String lastIsoLocalDateTimeString = lastPeriod.getValue();
            if (lastIsoLocalDateTimeString == null) {
                lastIsoLocalDateTimeString = isoLocalDateTime;
            }
            LocalDateTime lastIsoLocaleDateTime = LocalDateTime.parse(lastIsoLocalDateTimeString);
            long minutes = ChronoUnit.MINUTES.between(lastIsoLocaleDateTime, localDateTime);
            if (minutes < 5) {
                lastPeriod.setValue(isoLocalDateTime);
            } else {
                today.periods.add(Pair.of(isoLocalDateTime, null));
            }
        } else {
            ElectroEntity today = new ElectroEntity();
            today.date = isoLocalDate;
            today.periods = new ArrayList<>();
            today.periods.add(Pair.of(isoLocalDateTime, null));
            dates.dates.add(today);
        }

        electroEntities.set(dates);
        storage.save(dates);

        // Send notification when black zone is near
        if (settingsService.notifyBeforeBlackZone()) {
            LocalDateTime dateTime = LocalDateTime.now();
            ScheduleEntities schedule = getSchedule();
            int dayOfWeek = dateTime.getDayOfWeek().getValue();
            Optional<ScheduleEntity> currentSchedule = schedule.getDates()
                    .stream()
                    .filter(sch -> sch.getDay().equals(String.valueOf(dayOfWeek)))
                    .findFirst();

            currentSchedule.ifPresent(sch ->
                    sch.getBlack().forEach(blackZone -> {
                        int hour = Integer.parseInt(blackZone.getKey().split(":")[0]);
                        int minute = Integer.parseInt(blackZone.getKey().split(":")[1]);

                        LocalDateTime blackZoneStart = LocalDateTime.of(dateTime.toLocalDate(),
                                LocalTime.of(hour, minute));
                        long minutes = ChronoUnit.MINUTES.between(dateTime, blackZoneStart);
                        if (minutes > 0 && minutes < 10) {
                            telegramService.sendBroadcastMessage(minutes + " min till black zone " + blackZone.getKey() + "-" + blackZone.getValue());
                        }
                    }));
        }
    }

    public void merge(ElectroEntities dates) {
        ElectroEntities electroEntities = this.electroEntities.get();
        Set<String> newDays = dates.getDates().stream().map(ElectroEntity::getDate).collect(Collectors.toSet());
        Map<String, ElectroEntity> newDaysMap = dates.getDates().stream().collect(Collectors.toMap(ElectroEntity::getDate, Function.identity()));
        Set<String> oldDays = electroEntities.getDates().stream().map(ElectroEntity::getDate).collect(Collectors.toSet());
        newDays.removeAll(oldDays);
        newDays.forEach(day -> electroEntities.getDates().add(newDaysMap.get(day)));
        this.storage.save(electroEntities);
    }

    private void sendDarkTimeMessage() {
        ElectroEntities dates = electroEntities.get();
        if (dates.dates == null || dates.dates.size() == 0) {
            return;
        }

        ElectroEntity lastAvailableDay = dates.dates.get(dates.dates.size() - 1);
        Pair lastPeriod = lastAvailableDay.periods.get(lastAvailableDay.periods.size() - 1);
        String lastIsoLocalDateTimeString = lastPeriod.getValue();
        if (lastIsoLocalDateTimeString == null) {
            return;
        }
        LocalDateTime lastIsoLocaleDateTime = LocalDateTime.parse(lastIsoLocalDateTimeString);
        telegramService.sendDarkTimeMessage(LocalDateTime.now(), lastIsoLocaleDateTime);
    }

    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static final class ElectroEntities {
        private List<ElectroEntity> dates = new ArrayList<>();
    }

    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static final class ElectroEntity {
        private String date;
        private List<Pair> periods;
    }

    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static final class ScheduleEntities {
        private List<ScheduleEntity> dates = new ArrayList<>();
    }

    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static final class ScheduleEntity {
        private String day;
        private List<Pair> black;
        private List<Pair> gray;
        private List<Pair> white;
    }
}
