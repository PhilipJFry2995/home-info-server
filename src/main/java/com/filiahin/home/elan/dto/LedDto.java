package com.filiahin.home.elan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LedDto {
    private String red;
    private String green;
    private String blue;
    private String brightness;
    private String demo;
    private String automat;
}
