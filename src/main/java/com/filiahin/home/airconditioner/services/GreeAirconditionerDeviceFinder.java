package com.filiahin.home.airconditioner.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filiahin.home.airconditioner.GreeAirconditionerDevice;
import com.filiahin.home.airconditioner.dto.Command;
import com.filiahin.home.airconditioner.dto.CommandResponse;
import com.filiahin.home.airconditioner.ConnectionInfo;
import com.filiahin.home.airconditioner.DeviceInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.filiahin.home.airconditioner.util.GreeAirconditionerHelper.getDatagramSocket;

public class GreeAirconditionerDeviceFinder {
    private static final Logger log = LogManager.getLogger(GreeAirconditionerDeviceFinder.class);
    public static final List<String> DEVICE_IDS = List.of(); // TODO provide list of air conditioner mac addresses
    public static final int FIND_RETRIES = 10;

    private GreeAirconditionerDeviceFinder() {
    }

    public static List<GreeAirconditionerDevice> findDevices() {
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback()) {
                    continue;    // Do not want to use the loopback interface.
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;

                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Searching for devices on broadcast address {}", broadcast);
                    }

                    return findDevices(broadcast);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<GreeAirconditionerDevice> findDevices(InetAddress broadcastAddress) {
        if (log.isInfoEnabled()) {
            log.info("Looking for air conditioner devices on {}. This might take a while...", broadcastAddress.getHostAddress());
        }

        Command command = Command.builder().buildScanCommand();
        byte[] scanCommand = command.toJson().getBytes();

        DatagramSocket datagramSocket = getDatagramSocket();
        DatagramPacket sendPacket = new DatagramPacket(scanCommand, scanCommand.length, broadcastAddress, 7000);
        try {
            datagramSocket.send(sendPacket);
        } catch (IOException e) {
            log.error("Can't send packet", e);
        }

        List<GreeAirconditionerDevice> devices = new ArrayList<>();
        Set<String> homeDevices = new HashSet<>(DEVICE_IDS);

        byte[] receiveData = new byte[1024];
        boolean timeoutRecieved = false;
        int counter = 0;
        while (!timeoutRecieved && counter < FIND_RETRIES) {
            // Receive a response
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                datagramSocket.receive(receivePacket);
                InetAddress remoteAddress = receivePacket.getAddress();
                Integer remotePort = receivePacket.getPort();

                // Read the response
                CommandResponse commandResponse = getResponseCommand(receivePacket);

                // If there was no pack, ignore the response
                if (commandResponse.getPack() == null) {
                    continue;
                }

                if (log.isDebugEnabled()) {
                    log.debug("Found device {}", commandResponse);
                }

                DeviceInfo deviceInfo = DeviceInfo.build(commandResponse);
                if (homeDevices.remove(deviceInfo.getMacAddress())) {
                    ConnectionInfo connectionInfo = ConnectionInfo.build(remoteAddress, remotePort);
                    GreeAirconditionerDevice device = new GreeAirconditionerDevice(deviceInfo, connectionInfo);
                    devices.add(device);
                }
                if (homeDevices.isEmpty()) {
                    timeoutRecieved = true;
                }
            } catch (IOException e) {
                timeoutRecieved = true;
            }
            ++counter;
        }
        if (log.isInfoEnabled()) {
            log.info("Found {} devices", devices.size());
        }
        return devices;
    }

    private static CommandResponse getResponseCommand(DatagramPacket receivePacket) throws IOException {
        String modifiedSentence = new String(receivePacket.getData());
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(modifiedSentence, CommandResponse.class);
    }
}
