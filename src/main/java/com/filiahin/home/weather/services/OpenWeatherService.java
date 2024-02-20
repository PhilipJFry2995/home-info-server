package com.filiahin.home.weather.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filiahin.home.weather.dto.WeatherDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenWeatherService {
    private final static Logger log = LogManager.getLogger(OpenWeatherService.class);
    private static final String API_KEY = "";
    private static final String CITY = "Odessa,ua";

    public WeatherDto weather() throws URISyntaxException, IOException, InterruptedException {
        String endpoint = "https://api.openweathermap.org/data/2.5/weather?q="+ CITY
                + "&units=metric"
                + "&appid=" + API_KEY;

        HttpRequest request = HttpRequest.newBuilder(new URI(endpoint))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Response for {}: {}", endpoint, response.statusCode());
        ObjectMapper mapper = new ObjectMapper();
        String body = response.body();
        return mapper.readValue(body, WeatherDto.class);
    }
}
