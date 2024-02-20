package com.filiahin.home.audio;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

@Data
@AllArgsConstructor
public class AudioMessage {
    private File file;
    private long duration;
}
