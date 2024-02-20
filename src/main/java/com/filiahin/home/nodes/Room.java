package com.filiahin.home.nodes;

public enum Room {
    BEDROOM,
    LIVINGROOM,
    STUDY,
    BEDROOM_BALCONY,
    LIVINGROOM_BALCONY;

    public static Room room(int index) {
        return values()[index];
    }
}
