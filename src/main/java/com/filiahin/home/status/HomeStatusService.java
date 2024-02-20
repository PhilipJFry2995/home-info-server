package com.filiahin.home.status;

import com.filiahin.home.climate.ClimateRecord;
import com.filiahin.home.climate.ClimateService;
import com.filiahin.home.nodes.ClimateDto;
import com.filiahin.home.nodes.Room;
import com.google.common.annotations.VisibleForTesting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HomeStatusService {
    private static final Logger log = LogManager.getLogger(HomeStatusService.class);
    private static final String BEDROOM_MAC = "";
    private static final String STUDY_MAC = "";
    private static final String LIVING_ROOM_MAC = "";

    @Autowired
    private ClimateService climateService;

    private final Map<Long, ConditionerStatus> airConditionersPeriods = new LinkedHashMap<>();
    private final List<ClimateRecord> nightRecords = new ArrayList<>(9);
    private ClimateRecord morningRecord;
    private ClimateRecord dayRecord;
    private ClimateRecord eveningRecord;

    public String voiceStatus() {
        ClimateRecord record = climateService.get();
        Map<Room, ClimateDto> rooms = record.getDto();
        String text = "It is now " + rooms.entrySet().stream().map(
                entry -> entry.getValue().getTemperature() + "° in " + entry.getKey()
        ).collect(Collectors.joining(", "));
        return text;
    }

    public String textStatus() {
        return "Отчет за " + LocalDate.now() + ":\n"
                + joinConditionerData()
                + joinClimateData();
    }

    public void clearData() {
        airConditionersPeriods.clear();
    }

    public void notifyConditioner(String mac, boolean on) {
        notifyConditioner(mac, on, System.currentTimeMillis());
    }

    @VisibleForTesting
    public void notifyConditioner(String mac, boolean on, long time) {
        airConditionersPeriods.put(time, new ConditionerStatus(mac, on));
    }

    public void notifyMorning(ClimateRecord record) {
        morningRecord = record;
    }

    public void notifyDay(ClimateRecord record) {
        dayRecord = record;
    }

    public void notifyEvening(ClimateRecord record) {
        eveningRecord = record;
    }

    public void startNightWatch(Consumer<String> nightStatistics) {
        log.info("startNightWatch, clearing night records");
        nightRecords.clear();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            ClimateRecord climateRecord = climateService.get();
            log.info(climateRecord);
            nightRecords.add(climateRecord);
        };
        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.HOURS);

        scheduler.schedule(() -> {
            scheduler.shutdown();
            String statistics = "Отчет за ночь " + LocalDate.now() + ":\n"
                    + notifyNightStatistics();
            log.info(statistics);
            nightStatistics.accept(statistics);
        }, 8, TimeUnit.HOURS);
    }

    private String notifyNightStatistics() {
        StringBuilder builder = new StringBuilder();
        nightRecords.stream()
                .filter(climateRecord -> climateRecord.getDto().containsKey(Room.BEDROOM))
                .forEach(climateRecord -> {
                    builder.append(climateRecord.getTime(), 0, 8).append(": ");
                    climateInTime(builder, climateRecord, Room.BEDROOM);
                });
        return builder.toString();
    }

    public String joinConditionerData() {
        StringBuilder builder = new StringBuilder();
        String study = conditionerTime(STUDY_MAC, "кабинете");
        String bedroom = conditionerTime(BEDROOM_MAC, "спальне");
        String living = conditionerTime(LIVING_ROOM_MAC, "гостиной");
        if (study.isEmpty() && bedroom.isEmpty() && living.isEmpty()) {
            return builder.append("Кондиционеры были выключены весь день\n").toString();
        } else {
            if (!study.isEmpty()) {
                builder.append(study).append("\n");
            }
            if (!bedroom.isEmpty()) {
                builder.append(bedroom).append("\n");
            }
            if (!living.isEmpty()) {
                builder.append(living);
            }
            return builder.toString();
        }
    }

    public String joinClimateData() {
        StringBuilder builder = new StringBuilder();
        Map<Room, String> names = Map.of(Room.BEDROOM, "Спальня", Room.STUDY, "Кабинет", Room.LIVINGROOM, "Гостиная");
        for (Room room : names.keySet()) {
            builder.append(names.get(room)).append(" ");
            if (morningRecord.getDto().containsKey(room)) {
                builder.append("\nутром: ");
                climateInTime(builder, morningRecord, room);
            }
            if (dayRecord.getDto().containsKey(room)) {
                builder.append("днем: ");
                climateInTime(builder, dayRecord, room);
            }
            if (eveningRecord.getDto().containsKey(room)) {
                builder.append("вечером: ");
                climateInTime(builder, eveningRecord, room);
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    private void climateInTime(StringBuilder builder, ClimateRecord record, Room room) {
        builder.append(record.getDto().get(room).getTemperature()).append(" C° ")
                .append(record.getDto().get(room).getHumidity()).append(" %")
                .append("\n");
    }

    private String conditionerTime(String mac, String ruName) {
        StringBuilder builder = new StringBuilder();
        List<Long> periods = airConditionersPeriods.entrySet().stream()
                .filter(entry -> entry.getValue().getMac().equals(mac))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (periods.isEmpty()) {
            return "";
        }
        boolean startedWithOn = airConditionersPeriods.get(periods.get(0)).isOn();
        long workingTime = 0L;
        if (!startedWithOn) {
            workingTime += timeSinceYesterday(periods.get(0));
        }
        int startIndex = startedWithOn ? 0 : 1;
        for (int i = startIndex; i < periods.size(); i += 2) {
            long turnedOff;
            if (i + 1 < periods.size()) {
                turnedOff = periods.get(i + 1);
            } else {
                LocalDateTime offTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(periods.get(i)), ZoneOffset.UTC)
                        .withHour(23).withMinute(0).withSecond(0);
                turnedOff = offTime.toEpochSecond(ZoneOffset.UTC) * 1000;
            }
            long turnedOn = periods.get(i);
            workingTime += turnedOff - turnedOn;
        }

        long seconds = workingTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        long totalMinutes = minutes;

        seconds %= 60;
        minutes %= 60;

        if (hours > 0) {
            builder.append(hours).append("ч. ");
        }
        if (minutes > 0) {
            builder.append(minutes).append("мин. ");
        }
        if (seconds > 0) {
            builder.append(seconds).append("сек. ");
        }
        if (hours > 0 || minutes > 0 || seconds > 0) {
            builder.append("работал кондиционер в ").append(ruName);
            builder.append("\nПотребление электроэнергии ").append(String.format("%1.5f кВт⋅ч", totalMinutes * 0.01666));
        }

        return builder.toString();
    }

    private long timeSinceYesterday(long definedTimeMillis) {
        LocalDateTime definedTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(definedTimeMillis), ZoneOffset.UTC);
        LocalDateTime yesterdayDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(definedTimeMillis), ZoneOffset.UTC);
        LocalDateTime yesterday23Hours = yesterdayDateTime.minusDays(1).withHour(23).withMinute(0).withSecond(0);
        return ChronoUnit.MILLIS.between(yesterday23Hours, definedTime);
    }
}
