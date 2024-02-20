package com.filiahin.home.elan.controllers;

import com.filiahin.home.elan.dto.LedDto;
import com.filiahin.home.elan.services.ElanLedService;
import com.filiahin.home.notifications.WebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.filiahin.home.elan.services.ElanLedService.LIVING_ROOM_LED_ID;

@RestController
@RequestMapping("/elan/led")
public class ElanLedController {

    @Autowired
    private ElanLedService service;

    @Autowired
    private WebSocketController socketController;

    @GetMapping("/{mac}/state")
    public LedDto state(@PathVariable String mac) {
        return service.state(mac, LedDto.class);
    }

    @GetMapping("/{mac}/on")
    public int on(@RequestParam String sender) {
        socketController.broadcast(sender, Map.of(
                "id", LIVING_ROOM_LED_ID,
                "action", "on"
        ));
        return service.enable();
    }

    @GetMapping("/{mac}/off")
    public void off(@RequestParam String sender) {
        socketController.broadcast(sender, Map.of(
                "id", LIVING_ROOM_LED_ID,
                "action", "off"
        ));
        service.disable();
    }

    @GetMapping("/{mac}/brightness")
    public void brightness(@RequestParam int value, @RequestParam String sender) {
        socketController.broadcast(sender, Map.of(
                "id", LIVING_ROOM_LED_ID,
                "brightness", String.valueOf(value)
        ));
        service.brightness(value);
    }

    @GetMapping("/{mac}/color")
    public void color(@RequestParam Integer red,
                      @RequestParam Integer green,
                      @RequestParam Integer blue,
                      @RequestParam String sender) {
        socketController.broadcast(sender, Map.of(
                "id", LIVING_ROOM_LED_ID,
                "red", String.valueOf(red),
                "green", String.valueOf(green),
                "blue", String.valueOf(blue)
        ));
        service.color(red, green, blue);
    }
}
