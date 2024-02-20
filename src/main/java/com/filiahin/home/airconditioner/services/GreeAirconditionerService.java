package com.filiahin.home.airconditioner.services;

import com.filiahin.home.airconditioner.GreeAirconditionerDevice;
import com.filiahin.home.airconditioner.binding.GreeDeviceBinderService;
import com.filiahin.home.airconditioner.binding.GreeDeviceBinding;
import com.filiahin.home.airconditioner.communication.GreeCommunicationService;
import com.filiahin.home.airconditioner.dto.Command;
import com.filiahin.home.airconditioner.dto.CommandBuilder;
import com.filiahin.home.airconditioner.dto.packs.StatusResponsePack;
import com.filiahin.home.airconditioner.dto.status.FanMode;
import com.filiahin.home.airconditioner.dto.status.GreeDeviceStatus;
import com.filiahin.home.airconditioner.dto.status.OperationMode;
import com.filiahin.home.airconditioner.dto.status.Switch;
import com.filiahin.home.airconditioner.dto.status.Temperature;
import com.filiahin.home.airconditioner.dto.status.TemperatureUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.filiahin.home.airconditioner.services.GreeAirconditionerDeviceFinder.FIND_RETRIES;

public class GreeAirconditionerService {
    private static final Logger log = LogManager.getLogger(GreeAirconditionerService.class);

    private final GreeDeviceBinderService binderService;
    private final GreeCommunicationService communicationService;
    private List<GreeAirconditionerDevice> devices;

    public GreeAirconditionerService(GreeDeviceBinderService binderService, GreeCommunicationService communicationService) {
        this.binderService = binderService;
        this.communicationService = communicationService;
    }

    public List<GreeAirconditionerDevice> getDevices() {
        return devices;
    }

    public Optional<GreeAirconditionerDevice> getDevice(String macAddress) {
        return devices.stream()
                .filter(device -> device.getDeviceInfo().getMacAddress().equals(macAddress))
                .findFirst();
    }

    public void discoverDevices() {
        List<GreeAirconditionerDevice> devices = new ArrayList<>();
        int counter = 0;
        while (devices.size() == 0 && counter < FIND_RETRIES) {
            devices = GreeAirconditionerDeviceFinder.findDevices();
            ++counter;
        }
        this.devices = devices;
    }

    public boolean turnOn(GreeAirconditionerDevice device) {
        log.info("Turning on the air conditioner");
        GreeDeviceBinding binding = binderService.getBinding(device);
        if (binding == null) {
            return false;
        }

        GreeDeviceStatus status = new GreeDeviceStatus();
        status.setPower(Switch.ON);

        Command command = CommandBuilder.builder().buildControlCommand(status, binding);
        communicationService.sendCommand(device, command, Function.identity());
        return true;
    }

    public boolean turnOff(GreeAirconditionerDevice device) {
        log.info("Turning off the air conditioner");
        GreeDeviceBinding binding = binderService.getBinding(device);
        if (binding == null) {
            return false;
        }

        GreeDeviceStatus status = new GreeDeviceStatus();
        status.setPower(Switch.OFF);

        Command command = CommandBuilder.builder().buildControlCommand(status, binding);
        communicationService.sendCommand(device, command, Function.identity());
        return true;
    }

    public boolean setTemperature(GreeAirconditionerDevice device, Integer temperature) {
        log.info("Setting the temperature to {}", temperature);
        GreeDeviceBinding binding = binderService.getBinding(device);
        if (binding == null) {
            return false;
        }

        GreeDeviceStatus status = new GreeDeviceStatus();
        status.setTemperature(new Temperature(temperature, TemperatureUnit.CELSIUS));

        Command command = CommandBuilder.builder().buildControlCommand(status, binding);
        String result = communicationService.sendCommand(device, command, Function.identity());
        return true;
    }

    public GreeDeviceStatus getStatus(GreeAirconditionerDevice device) {
        log.info("Getting status of device");
        int attempts = 3;
        return getStatus(device, attempts);
    }

    public GreeDeviceStatus getStatus(GreeAirconditionerDevice device, int attempts) {
        GreeDeviceBinding binding = binderService.getBinding(device);
        if (binding == null) {
            if (attempts < 1) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find device " + device.getDeviceInfo().getMacAddress());
            }
            discoverDevices();
            return getStatus(device, --attempts);
        }

        Command command = CommandBuilder.builder().buildStatusCommand(binding);
        return communicationService.sendCommand(device, command, (json) -> StatusResponsePack.build(json, binding).toObject());
    }

    public void setMode(GreeAirconditionerDevice device, OperationMode operationMode) {
        log.info("Setting mode to {}", operationMode);
        GreeDeviceBinding binding = binderService.getBinding(device);
        if (binding == null) {
            return;
        }

        GreeDeviceStatus status = new GreeDeviceStatus();
        status.setOperationMode(operationMode);

        Command command = CommandBuilder.builder().buildControlCommand(status, binding);
        communicationService.sendCommand(device, command, Function.identity());
    }

    public void setFan(GreeAirconditionerDevice device, FanMode fanMode) {
        log.info("Setting fan to {}", fanMode);
        GreeDeviceBinding binding = binderService.getBinding(device);
        if (binding == null) {
            return;
        }

        GreeDeviceStatus status = new GreeDeviceStatus();
        status.setFanMode(fanMode);

        Command command = CommandBuilder.builder().buildControlCommand(status, binding);
        communicationService.sendCommand(device, command, Function.identity());
    }

    public void setLig(GreeAirconditionerDevice device, Switch lig) {
        log.info("Setting light indicator to {}", lig);
        GreeDeviceBinding binding = binderService.getBinding(device);
        if (binding == null) {
            return;
        }

        GreeDeviceStatus status = new GreeDeviceStatus();
        status.setLightIndicator(lig);

        Command command = CommandBuilder.builder().buildControlCommand(status, binding);
        communicationService.sendCommand(device, command, Function.identity());
    }

    public void setQuiet(GreeAirconditionerDevice device, Switch quiet) {
        log.info("Setting quiet to {}", quiet);
        GreeDeviceBinding binding = binderService.getBinding(device);
        if (binding == null) {
            return;
        }

        GreeDeviceStatus status = new GreeDeviceStatus();
        status.setQuiet(quiet);

        Command command = CommandBuilder.builder().buildControlCommand(status, binding);
        communicationService.sendCommand(device, command, Function.identity());
    }

    public void turnOffAllDevices() {
        devices.stream()
                .filter(device -> getStatus(device).getPower() == Switch.ON)
                .forEach(this::turnOff);
    }
}
