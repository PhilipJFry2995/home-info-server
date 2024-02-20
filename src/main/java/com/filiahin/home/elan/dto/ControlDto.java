package com.filiahin.home.elan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ControlDto {
    @JsonProperty("on")
    private String on;

    @JsonProperty("delay")
    private String delay;

    @JsonProperty("automat")
    private String automat;

    @JsonProperty("locked")
    private String locked;

    @JsonProperty("delayed off: set time")
    private String delayedOffTime;

    @JsonProperty("delayed on: set time")
    private String delayedOnTime;
}
