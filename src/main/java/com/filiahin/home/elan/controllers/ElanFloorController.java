package com.filiahin.home.elan.controllers;

import com.filiahin.home.elan.dto.FloorDto;
import com.filiahin.home.elan.services.ElanFloorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/elan/floor")
public class ElanFloorController {

    @Autowired
    private ElanFloorService service;

    @GetMapping("/{mac}/state")
    public FloorDto state(@PathVariable String mac) {
        return service.state(mac, FloorDto.class);
    }

    @GetMapping("/{mac}/on")
    public void on(@PathVariable String mac) {
        service.on(mac);
    }

    @GetMapping("/{mac}/off")
    public void off(@PathVariable String mac) {
        service.off(mac);
    }

    @GetMapping("/{mac}/mode/{value}")
    public void mode(@PathVariable String mac, @PathVariable Integer mode) {
        service.mode(mac, mode);
    }

    @GetMapping("/{mac}/correction/{value}")
    public void correction(@PathVariable String mac, @PathVariable Double value) {
        service.correction(mac, value);
    }
}
