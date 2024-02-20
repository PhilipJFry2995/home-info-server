package com.filiahin.home.elan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlindsDto {
    @JsonProperty("roll up")
    private String rollUp;
    @JsonProperty("set time")
    private String setTime;
    @JsonProperty("automat")
    private String automat;
}
