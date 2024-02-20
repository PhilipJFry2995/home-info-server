package com.filiahin.home.windows;

import com.filiahin.home.climate.ClimateService;
import com.filiahin.home.elan.dto.BlindsDto;
import com.filiahin.home.elan.services.ElanBlindsService;
import com.filiahin.home.nodes.ClimateDto;
import com.filiahin.home.nodes.Room;
import com.filiahin.home.settings.SettingsService;
import com.filiahin.home.telegram.TelegramSmartHomeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ShellyDoorWindowService {
    private static final Logger logger = LoggerFactory.getLogger(ShellyDoorWindowService.class);
    private static final String STUDY_WINDOW_ID = "1";
    private static final String BEDROOM_WINDOW_ID = "2";
    private static final String LIVINGROOM_WINDOW_ID = "3";
    private static final int MAX_LUX = 1600;
    private static final double MAX_TEMP = 27.0;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private ClimateService climateService;

    @Autowired
    private ElanBlindsService blindsService;

    @Autowired
    private TelegramSmartHomeService telegramSmartHomeService;

    private final Set<WindowEvents> listeners;
    private final Map<String, WindowReport> currentStates;
    private final Map<String, LocalDate> todays = new HashMap<>();

    public ShellyDoorWindowService() {
        this.listeners = new HashSet<>();
        this.currentStates = new HashMap<>();
    }

    public void onWindowReport(Map<String, String> report) {
        currentStates.put(report.get("id"), WindowReport.of(report));
        this.listeners.forEach(l -> l.onWindowReport(report));

        closeBlinds(report);
    }

    public Optional<WindowReport> report(String id) {
        return Optional.ofNullable(currentStates.get(id));
    }

    public void registerListener(WindowEvents listener) {
        this.listeners.add(listener);
    }

    public void unregisterListener(WindowEvents listener) {
        this.listeners.remove(listener);
    }

    private void closeBlinds(Map<String, String> report) {
        if (!settingsService.closeBrightBlinds()) {
            return;
        }

        String id = report.get("id");
        if (todays.containsKey(id) &&
                LocalDate.now().toString().equals(todays.get(id).toString())) {
            return;
        }

        String luxStr = report.get("lux");
        String tempStr = report.get("temp");
        if (luxStr == null || tempStr == null) {
            return;
        }

        int lux = Integer.parseInt(luxStr);
        if (lux < MAX_LUX) {
            return;
        }

        double temp = Double.parseDouble(tempStr);

        Room room = null;
        String blindsMac = null;
        switch (id) {
            case STUDY_WINDOW_ID:
                room = Room.STUDY;
                blindsMac = ElanBlindsService.STUDY_BLINDS_ID;
                break;
            case BEDROOM_WINDOW_ID:
                room = Room.BEDROOM_BALCONY;
                // blindsMacs.add(ElanBlindsService.LEFT_BALCONY_BLINDS_ID); not added to avoid stucking
                blindsMac = ElanBlindsService.RIGHT_BALCONY_BLINDS_ID;
                break;
            case LIVINGROOM_WINDOW_ID:
                room = Room.LIVINGROOM;
                blindsMac = ElanBlindsService.LIVING_ROOM_BLINDS_ID;
                break;
            default:
                logger.warn("No room found for id " + id);
        }
        if (room == null) {
            return;
        }

        Map<Room, ClimateDto> record = climateService.get().getDto();
        double averageTemp = temp;
        if (record.containsKey(room)) {
            averageTemp = (temp + record.get(room).getTemperature()) / 2;
        }

        if (averageTemp < MAX_TEMP) {
            return;
        }

        BlindsDto dto = blindsService.state(blindsMac, BlindsDto.class);
        if (!Boolean.TRUE.toString().equals(dto.getRollUp())) {
            return;
        }

        todays.put(id, LocalDate.now());
        blindsService.rollDown(blindsMac);
        telegramSmartHomeService.sendBroadcastMessage("Закрываю окно в " + room + ". " + lux + "lx, " + String.format("%2.2f C°", averageTemp));
    }
}
