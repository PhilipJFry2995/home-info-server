package com.filiahin.home.climate;

import com.filiahin.home.nodes.ClimateDto;
import com.filiahin.home.nodes.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public final class ClimateRecord {
    private String time;
    private Map<Room, ClimateDto> dto;
}
