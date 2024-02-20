package com.filiahin.home.weather;

import com.filiahin.home.weather.services.OpenWeatherService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeatherConfig {
    @Bean
    public OpenWeatherService openWeatherService() {
        return new OpenWeatherService();
    }
}
