package com.filiahin.home.elan.controllers;

import com.filiahin.home.elan.dto.BlindsDto;
import com.filiahin.home.elan.services.ElanBlindsService;
import com.filiahin.home.notifications.WebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/elan/blinds")
public class ElanBlindsController {

    @Autowired
    private ElanBlindsService service;

    @Autowired
    private WebSocketController socketController;

    @GetMapping("/{mac}/state")
    public BlindsDto state(@PathVariable String mac) {
        return service.state(mac, BlindsDto.class);
    }

    @GetMapping("/{mac}/rollUp")
    public void rollUp(@PathVariable String mac, @RequestParam String sender) {
        if (ElanBlindsService.LIVING_ROOM_BLINDS_ID.equals(mac)) {
            service.rollDown(mac);
            return;
        }
        service.rollUp(mac);
        socketController.broadcast(sender, Map.of(
                "id", mac,
                "action", "rollUp"
        ));
    }

    @GetMapping("/{mac}/rollDown")
    public void rollDown(@PathVariable String mac, @RequestParam String sender) {
        if (ElanBlindsService.LIVING_ROOM_BLINDS_ID.equals(mac)) {
            service.rollUp(mac);
            return;
        }
        service.rollDown(mac);
        socketController.broadcast(sender, Map.of(
                "id", mac,
                "action", "rollDown"
        ));
    }

    @GetMapping("/{mac}/stop")
    public void stop(@PathVariable String mac) {
        service.stop(mac);
    }
}
