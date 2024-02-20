package com.filiahin.home.elan.controllers;

import com.filiahin.home.elan.dto.ControlDto;
import com.filiahin.home.elan.services.ElanControlsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/elan/control")
public class ElanControlsController {
    @Autowired
    private ElanControlsService service;

    @GetMapping("/pump/state")
    public ControlDto pumpState() {
        return service.pumpState();
    }

    @GetMapping("/pump/on")
    public void pumpOn() {
        service.pumpOn();
    }

    @GetMapping("/pump/off")
    public void pumpOff() {
        service.pumpOff();
    }

    @GetMapping("/light/state")
    public ControlDto lightState() {
        return service.lightState();
    }

    @GetMapping("/light/on")
    public void lightOn() {
        service.lightOn();
    }

    @GetMapping("/light/off")
    public void lightOff() {
        service.lightOff();
    }

    @GetMapping("/conditioner/state")
    public ControlDto conditionerState() {
        return service.conditionerState();
    }

    @GetMapping("/conditioner/on")
    public void conditionerOn() {
        service.conditionerOn();
    }

    @GetMapping("/conditioner/off")
    public void conditionerOff() {
        service.conditionerOff();
    }
}
