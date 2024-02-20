package com.filiahin.home.elan.services;

import com.filiahin.home.elan.dto.ControlDto;
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

public class ElanControlsService extends ElanService<ControlDto> {
    private final static Logger log = LogManager.getLogger(ElanControlsService.class);
    private static final String RECIRCULATION_PUMP_ID = ""; // TODO set ID
    private static final String LIGHT_SWITCH_ID = ""; // TODO set ID
    private static final String CONDITIONERS_SWITCH_ID = ""; // TODO set ID

    public ControlDto pumpState() {
        throw new RuntimeException("Implement API call");
    }

    public void pumpOn() {
        throw new RuntimeException("Implement API call");
    }

    public void pumpOff() {
        throw new RuntimeException("Implement API call");
    }

    public ControlDto lightState() {
        throw new RuntimeException("Implement API call");
    }

    public void lightOn() {
        throw new RuntimeException("Implement API call");
    }

    public void lightOff() {
        throw new RuntimeException("Implement API call");
    }

    public ControlDto conditionerState() {
        throw new RuntimeException("Implement API call");
    }

    public void conditionerOn() {
        throw new RuntimeException("Implement API call");
    }

    public void conditionerOff() {
        throw new RuntimeException("Implement API call");
    }

    private void turn(boolean on, String id) throws URISyntaxException, IOException, InterruptedException {
        throw new RuntimeException("Implement API call");
    }
}
