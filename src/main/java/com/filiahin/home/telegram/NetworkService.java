package com.filiahin.home.telegram;

import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NetworkService {
    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);
    public static final String ROUTER_IP = "";

    public boolean isUserAtHome(TelegramStorage.TelegramStorageEntity entity) {
        return entity.getIps().stream().anyMatch(this::isIpReachable)
                || listIps(entity.getMac()).stream().anyMatch(this::isIpReachable);
    }

    public boolean isIpReachable(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isReachable(1000);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    private Set<String> listIps(String mac) {
        return getIpAddresses().getOrDefault(mac, new HashSet<>());
    }

    private Map<String, Set<String>> getIpAddresses() {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "arp -a -v");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            List<String> lines = new ArrayList<>();
            while (true) {
                String line = r.readLine();
                if (line == null) {
                    break;
                }
                lines.add(line);
            }

            return extractIps(lines);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    private Map<String, Set<String>> extractIps(List<String> lines) {
        return lines.stream()
                .filter(line -> line.contains("static") || line.contains("dynamic"))
                .map(line -> {
                    List<String> tokens = Arrays.stream(line.split(" "))
                            .filter(StringUtils::isNotBlank)
                            .map(String::strip)
                            .collect(Collectors.toList());
                    String ip = tokens.get(0);
                    String mac = tokens.get(1);
                    return new AbstractMap.SimpleEntry<>(mac, ip);
                })
                .collect(Collectors.groupingBy(AbstractMap.SimpleEntry::getKey,
                        Collectors.mapping(AbstractMap.SimpleEntry::getValue, Collectors.toSet())));
    }
}
