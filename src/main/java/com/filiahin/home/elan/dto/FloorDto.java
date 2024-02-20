package com.filiahin.home.elan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FloorDto {
    @JsonProperty("temperature")
    private String temperature;
    @JsonProperty("mode")
    private String mode;
    @JsonProperty("correction")
    private String correction;
    @JsonProperty("power")
    private String power;
    @JsonProperty("battery")
    private String battery;
    @JsonProperty("requested temperature")
    private String requested;
    @JsonProperty("heating")
    private String heating;
    @JsonProperty("cooling")
    private String cooling;
    @JsonProperty("old state")
    private String oldState;
    @JsonProperty("controll")
    private String control;
}
