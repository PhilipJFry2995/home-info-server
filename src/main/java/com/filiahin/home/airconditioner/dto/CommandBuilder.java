package com.filiahin.home.airconditioner.dto;

import com.filiahin.home.airconditioner.DeviceInfo;
import com.filiahin.home.airconditioner.binding.GreeDeviceBinding;
import com.filiahin.home.airconditioner.dto.packs.BindRequestPack;
import com.filiahin.home.airconditioner.dto.packs.ControlRequestPack;
import com.filiahin.home.airconditioner.dto.packs.StatusRequestPack;
import com.filiahin.home.airconditioner.dto.status.GreeDeviceStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandBuilder {
    private static final Logger log = LogManager.getLogger(CommandBuilder.class);

    public static CommandBuilder builder() {
        return new CommandBuilder();
    }

    public Command buildScanCommand() {
        Command command = new Command();
        command.setCommandType(CommandType.SCAN);
        return command;
    }

    public Command buildBindCommand(DeviceInfo info) {
        Command command = new Command();
        command.setCommandType(CommandType.PACK);
        command.setCid("app");
        command.setI(1);
        command.setTcid(info.getMacAddress());
        command.setUid(0L);

        String packEncrypted = BindRequestPack.build(info);
        command.setPack(packEncrypted);

        return command;
    }

    public Command buildControlCommand(GreeDeviceStatus status, GreeDeviceBinding binding) {
        DeviceInfo info = binding.getDevice().getDeviceInfo();

        Command command = new Command();
        command.setCommandType(CommandType.PACK);
        command.setCid("app");
        command.setI(0);
        command.setTcid(info.getMacAddress());
        command.setUid(0L);

        String packEncrypted = ControlRequestPack.build(status, binding);
        command.setPack(packEncrypted);

        return command;
    }

    public Command buildStatusCommand(GreeDeviceBinding binding) {
        DeviceInfo info = binding.getDevice().getDeviceInfo();

        Command command = new Command();
        command.setCommandType(CommandType.PACK);
        command.setCid("app");
        command.setI(0);
        command.setTcid(info.getMacAddress());
        command.setUid(0L);

        String packEncrypted = StatusRequestPack.build(info, binding);
        command.setPack(packEncrypted);

        return command;
    }
}