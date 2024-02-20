package com.filiahin.home.elan.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filiahin.home.elan.dto.BlindsDto;
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

import static com.filiahin.home.elan.services.ElanBlindsService.LIVING_ROOM_BLINDS_ID;

public abstract class ElanService<T> {
    private final static Logger log = LogManager.getLogger(ElanService.class);

    public T state(String id, Class<T> clazz) {
        throw new RuntimeException("Implement API call");
    }

    private T checkExceptions(String id, T dto) {
        throw new RuntimeException("Implement API call");
    }
}
