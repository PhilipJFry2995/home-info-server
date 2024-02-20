package com.filiahin.home.elan.services;

import com.filiahin.home.elan.dto.LedDto;
import com.filiahin.home.elan.util.ElanLoginUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ElanLedService extends ElanService<LedDto> {
    private final static Logger log = LogManager.getLogger(ElanLedService.class);

    public static final String LIVING_ROOM_LED_ID = ""; // TODO set ID
    private int lastBrightness = 0;

    public void disable() {
        throw new RuntimeException("Implement API call");
    }

    public int enable() {
        throw new RuntimeException("Implement API call");
    }

    public void brightness(int value) {
        setBrightness(value);
    }

    private void setBrightness(int value) {
        throw new RuntimeException("Implement API call");
    }

    public void color(int red, int green, int blue) {
        throw new RuntimeException("Implement API call");
    }
}
