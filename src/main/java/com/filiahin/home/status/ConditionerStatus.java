package com.filiahin.home.status;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConditionerStatus {
    private String mac;
    private boolean on;
}
