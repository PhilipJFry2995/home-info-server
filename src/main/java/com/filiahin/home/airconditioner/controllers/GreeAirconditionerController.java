package com.filiahin.home.airconditioner.controllers;

import com.filiahin.home.airconditioner.GreeAirconditionerDevice;
import com.filiahin.home.airconditioner.dto.status.FanMode;
import com.filiahin.home.airconditioner.dto.status.GreeDeviceStatus;
import com.filiahin.home.airconditioner.dto.status.OperationMode;
import com.filiahin.home.airconditioner.dto.status.Switch;
import com.filiahin.home.airconditioner.services.DelayedTask;
import com.filiahin.home.airconditioner.services.GreeAirconditionerDeviceFinder;
import com.filiahin.home.airconditioner.services.GreeAirconditionerService;
import com.filiahin.home.notifications.WebSocketController;
import com.filiahin.home.status.HomeStatusService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/cooperhunter")
public class GreeAirconditionerController {
    private static final Logger log = LogManager.getLogger(GreeAirconditionerController.class);

    @Autowired
    private GreeAirconditionerService airconditionerService;

    @Autowired
    private WebSocketController socketController;

    @Autowired
    private HomeStatusService homeStatusService;

    private Map<String, DelayedTask> delayedTaskMap = new HashMap<>();

    @GetMapping("/devices")
    public List<GreeAirconditionerDevice> devices() {
        List<GreeAirconditionerDevice> devices = this.airconditionerService.getDevices();
        if (devices.size() != GreeAirconditionerDeviceFinder.DEVICE_IDS.size()) {
            this.airconditionerService.discoverDevices();
        }
        return this.airconditionerService.getDevices();
    }

    @GetMapping("/{macAddress}/on")
    public String powerOn(@PathVariable String macAddress, @RequestParam String sender) {
        Optional<GreeAirconditionerDevice> device = this.airconditionerService.getDevice(macAddress);
        if (device.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find device " + macAddress);
        }

        this.airconditionerService.turnOn(device.get());
        this.homeStatusService.notifyConditioner(macAddress, true);
        socketController.broadcast(sender, Map.of(
                "id", macAddress,
                "action", "on"
        ));
        return "done";
    }

    @GetMapping("/{macAddress}/{temperature}")
    public String temperature(@PathVariable String macAddress, @PathVariable Integer temperature, @RequestParam String sender) {
        Optional<GreeAirconditionerDevice> device = this.airconditionerService.getDevice(macAddress);
        if (device.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find device " + macAddress);
        }

        this.airconditionerService.setTemperature(device.get(), temperature);
        socketController.broadcast(sender, Map.of(
                "id", macAddress,
                "temperature", String.valueOf(temperature)
        ));
        return "done";
    }

    @GetMapping("/{macAddress}/mode/{operationMode}")
    public String mode(@PathVariable String macAddress, @PathVariable OperationMode operationMode, @RequestParam String sender) {
        Optional<GreeAirconditionerDevice> device = this.airconditionerService.getDevice(macAddress);
        if (device.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find device " + macAddress);
        }

        this.airconditionerService.setMode(device.get(), operationMode);
        socketController.broadcast(sender, Map.of(
                "id", macAddress,
                "mode", String.valueOf(operationMode)
        ));
        return "done";
    }

    @GetMapping("/{macAddress}/fan/{fanMode}")
    public String fan(@PathVariable String macAddress, @PathVariable FanMode fanMode, @RequestParam String sender) {
        Optional<GreeAirconditionerDevice> device = this.airconditionerService.getDevice(macAddress);
        if (device.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find device " + macAddress);
        }

        this.airconditionerService.setFan(device.get(), fanMode);
        socketController.broadcast(sender, Map.of(
                "id", macAddress,
                "fan", String.valueOf(fanMode)
        ));
        return "done";
    }

    @GetMapping("/{macAddress}/lig/{lig}")
    public String fan(@PathVariable String macAddress, @PathVariable Switch lig, @RequestParam String sender) {
        Optional<GreeAirconditionerDevice> device = this.airconditionerService.getDevice(macAddress);
        if (device.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find device " + macAddress);
        }

        this.airconditionerService.setLig(device.get(), lig);
        socketController.broadcast(sender, Map.of(
                "id", macAddress,
                "lig", String.valueOf(lig)
        ));
        return "done";
    }

    @GetMapping("/{macAddress}/quiet/{quiet}")
    public String quiet(@PathVariable String macAddress, @PathVariable Switch quiet, @RequestParam String sender) {
        Optional<GreeAirconditionerDevice> device = this.airconditionerService.getDevice(macAddress);
        if (device.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find device " + macAddress);
        }

        this.airconditionerService.setQuiet(device.get(), quiet);
        socketController.broadcast(sender, Map.of(
                "id", macAddress,
                "quiet", String.valueOf(quiet)
        ));
        return "done";
    }

    @GetMapping("/{macAddress}/status")
    public GreeDeviceStatus getStatus(@PathVariable String macAddress) {
        Optional<GreeAirconditionerDevice> device = this.airconditionerService.getDevice(macAddress);
        if (device.isEmpty()) {
            log.info("404 Unable to find device " + macAddress);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find device " + macAddress);
        }
        return this.airconditionerService.getStatus(device.get());
    }

    @GetMapping("/{macAddress}/off")
    public String powerOff(@PathVariable String macAddress, @RequestParam String sender) {
        Optional<GreeAirconditionerDevice> device = this.airconditionerService.getDevice(macAddress);
        if (device.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find device " + macAddress);
        }

        this.airconditionerService.turnOff(device.get());
        this.homeStatusService.notifyConditioner(macAddress, false);
        socketController.broadcast(sender, Map.of(
                "id", macAddress,
                "action", "off"
        ));
        return "done";
    }

    @GetMapping("/{macAddress}/timer")
    public Integer timer(@PathVariable String macAddress) {
        return Optional.ofNullable(delayedTaskMap.get(macAddress)).map(DelayedTask::getRemainingTime).orElse(0);
    }

    @GetMapping("/{macAddress}/delay")
    public void delay(@PathVariable String macAddress, @RequestParam Integer seconds) {
        if (delayedTaskMap.containsKey(macAddress)) {
            delayedTaskMap.get(macAddress).requestStop();
            delayedTaskMap.remove(macAddress);
        }

        DelayedTask task = new DelayedTask(seconds * 1000, unused -> {
            Optional<GreeAirconditionerDevice> device = this.airconditionerService.getDevice(macAddress);
            device.ifPresent(greeAirconditionerDevice -> this.airconditionerService.turnOff(greeAirconditionerDevice));
        });
        delayedTaskMap.put(macAddress, task);
        log.info("Turn off " + macAddress + " in " + seconds + " seconds");
        new Thread(task).start();
    }

    @GetMapping("/{macAddress}/cancel_delay")
    public void cancelDelay(@PathVariable String macAddress) {
        if (delayedTaskMap.containsKey(macAddress)) {
            delayedTaskMap.get(macAddress).requestStop();
            delayedTaskMap.remove(macAddress);
        }
        log.info("Cancel timer for " + macAddress);
    }
}
