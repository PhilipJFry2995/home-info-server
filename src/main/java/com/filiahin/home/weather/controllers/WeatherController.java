package com.filiahin.home.weather.controllers;

import com.filiahin.home.weather.dto.WeatherDto;
import com.filiahin.home.weather.services.OpenWeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/weather")
public class WeatherController {
    @Autowired
    private OpenWeatherService service;

    @GetMapping
    public WeatherDto state() throws URISyntaxException, IOException, InterruptedException {
        return service.weather();
    }
}
