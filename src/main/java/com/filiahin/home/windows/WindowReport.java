package com.filiahin.home.windows;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WindowReport {
    private static final Logger log = LogManager.getLogger(WindowReport.class);

    private String id;
    private boolean open;
    private int lux;
    private float temp;
    private int tilt;
    private int vibration;

    public static WindowReport of(Map<String, String> json) {
        int lux = -1;
        try {
            lux = Integer.parseInt(json.get("lux"));
        } catch (NumberFormatException e) {
            log.warn("lux is invalid, " + e.getMessage());
        }

        int tilt = -1;
        try {
            tilt = Integer.parseInt(json.get("tilt"));
        } catch (NumberFormatException e) {
            log.warn("tilt is invalid, " + e.getMessage());
        }

        int vibration = -1;
        try {
            vibration = Integer.parseInt(json.get("vibration"));
        } catch (NumberFormatException e) {
            log.warn("vibration is invalid, " + e.getMessage());
        }

        return WindowReport.builder()
                .id(json.get("id"))
                .open("open".equals(json.get("state")))
                .lux(lux)
                .temp(Float.parseFloat(json.get("temp")))
                .tilt(tilt)
                .vibration(vibration)
                .build();
    }
}
