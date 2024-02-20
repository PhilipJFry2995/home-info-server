package com.filiahin.home.storage;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StorageDto {
    private double freeSpace;
    private double totalSpace;
}
