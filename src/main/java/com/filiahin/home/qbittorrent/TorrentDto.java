package com.filiahin.home.qbittorrent;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TorrentDto {
    private String hash;
    private String state;
    @JsonProperty("content_path")
    private String contentPath;
    @JsonProperty("amount_left")
    private String amountLeft;
    private String downloaded;
    @JsonProperty("seen_complete")
    private String seenComplete;
    private String dlspeed;
    private String upspeed;
    private String name;
    private String progress;
    private String size;
}
