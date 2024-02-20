package com.filiahin.home.elan.services;

import com.filiahin.home.elan.dto.FloorDto;
import com.filiahin.home.elan.util.ElanLoginUtil;
import com.filiahin.home.electro.ElectroEventsListener;
import com.filiahin.home.electro.ElectroService;
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

public class ElanFloorService extends ElanService<FloorDto> implements ElectroEventsListener {
    private final static Logger log = LogManager.getLogger(ElanFloorService.class);
    private static final String BATHROOM_HEAT_AREA_ID =  ""; // TODO set ID
    private static final String BALCONY_HEAT_AREA_ID =  ""; // TODO set ID
    private static final double MAX_DEGREE = 29.0;
    private static final double MAX_CORRECTION = MAX_DEGREE - 24.0;
    @Autowired
    private SettingsService settingsService;

    public ElanFloorService(ElectroService electroService) {
        electroService.subscribe(this);
    }

    public void on(String id) {
        throw new RuntimeException("Implement API call");
    }

    public void off(String id) {
        throw new RuntimeException("Implement API call");
    }

    private void turn(int power, String id) throws URISyntaxException, IOException, InterruptedException {
        throw new RuntimeException("Implement API call");
    }

    public void mode(String id, Integer mode) {
        throw new RuntimeException("Implement API call");
    }

    public void correction(String id, Double value) {
        throw new RuntimeException("Implement API call");
    }

    @Override
    public void onElectricityOn() {
        throw new RuntimeException("Implement API call");
    }
}
