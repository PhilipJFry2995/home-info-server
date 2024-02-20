package com.filiahin.home.airconditioner.communication;

import com.filiahin.home.airconditioner.ConnectionInfo;
import com.filiahin.home.airconditioner.GreeAirconditionerDevice;
import com.filiahin.home.airconditioner.binding.GreeDeviceBinderService;
import com.filiahin.home.airconditioner.dto.Command;
import com.filiahin.home.airconditioner.util.GreeAirconditionerHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.function.Function;

public class GreeCommunicationService {
    private static final Logger log = LogManager.getLogger(GreeDeviceBinderService.class);

    public <T> T sendCommand(GreeAirconditionerDevice device, Command command, Function<String, T> function) {
        int attempts = 3;
        return sendCommand(device, command, function, attempts);
    }

    public <T> T sendCommand(GreeAirconditionerDevice device, Command command, Function<String, T> function, int attempts) {
        String json = command.toJson();
        if (log.isDebugEnabled()) {
            log.debug("Sending command: {}", json);
        }

        ConnectionInfo connectionInfo = device.getConnectionInfo();
        InetAddress address = connectionInfo.getAddress();
        Integer port = connectionInfo.getPort();
        DatagramPacket datagram = new DatagramPacket(json.getBytes(), json.getBytes().length, address, port);
        DatagramSocket datagramSocket = GreeAirconditionerHelper.getDatagramSocket();

        try {
            datagramSocket.send(datagram);
            byte[] receiveData = new byte[500];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            datagramSocket.receive(receivePacket);
            String responseString = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();

            while(!responseString.contains(device.getDeviceInfo().getMacAddress())) {
                datagramSocket.receive(receivePacket);
                responseString = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
            }

            return function.apply(responseString);
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Can't send command {}", command, e);
            }
            log.info("Trying to send it again {}", command, e);
            if (attempts < 1) {
                return null;
            }
            return sendCommand(device, command, function, --attempts);
        }
    }
}
