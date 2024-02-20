package com.filiahin.home.elan.services;

import com.filiahin.home.elan.dto.BlindsDto;
import com.filiahin.home.elan.util.ElanLoginUtil;
import com.filiahin.home.settings.SettingsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ElanBlindsService extends ElanService<BlindsDto> {
    private final static Logger log = LogManager.getLogger(ElanBlindsService.class);

    public static final String BEDROOM_BLINDS_ID = ""; // TODO set ID
    public static final String STUDY_BLINDS_ID = ""; // TODO set ID
    public static final String LIVING_ROOM_BLINDS_ID = ""; // TODO set ID
    public static final String LEFT_BALCONY_BLINDS_ID = ""; // TODO set ID
    public static final String RIGHT_BALCONY_BLINDS_ID = ""; // TODO set ID
    private static final String UP = "true";
    @Autowired
    private SettingsService settingsService;

    public void rollUp(String id) {
        try {
            roll("up", id);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void rollDown(String id) {
        try {
            roll("down", id);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void roll(String direction, String id) throws URISyntaxException, IOException, InterruptedException {
        throw new RuntimeException("Implement API call");
    }

    public void stop(String mac) {
        throw new RuntimeException("Implement API call");
    }

    // Scheduled tasks
    public void openAllBlinds() {
        if (!settingsService.openMorning()) {
            return;
        }

        for (String id : List.of(BEDROOM_BLINDS_ID, STUDY_BLINDS_ID,
                LEFT_BALCONY_BLINDS_ID, RIGHT_BALCONY_BLINDS_ID)) {
            BlindsDto dto = state(id, BlindsDto.class);
            if (!UP.equals(dto.getRollUp())) {
                rollUp(id);
            }
        }
    }

    public void closeAllBlinds() {
        if (!settingsService.closeEvening()) {
            return;
        }

        for (String id : List.of(BEDROOM_BLINDS_ID, STUDY_BLINDS_ID, LIVING_ROOM_BLINDS_ID,
                LEFT_BALCONY_BLINDS_ID, RIGHT_BALCONY_BLINDS_ID)) {
            BlindsDto dto = state(id, BlindsDto.class);
            if (UP.equals(dto.getRollUp())) {
                rollDown(id);
            }
        }
    }

    public void closeLivingRoom() {
        if (!settingsService.closeDinner()) {
            return;
        }

        BlindsDto dto = state(LIVING_ROOM_BLINDS_ID, BlindsDto.class);
        if (UP.equals(dto.getRollUp())) {
            rollDown(LIVING_ROOM_BLINDS_ID);
        }
    }
}
