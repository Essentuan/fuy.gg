package com.busted_moments.core.api.requests.player.Class;

public enum GameplayModifiers {
    HARDCORE("Hardcore"),
    IRONMAN("Ironman"),
    CRAFTSMAN("Craftsman"),
    HUNTED("Hunted");

    private final String readableString;

    GameplayModifiers(String readableString) {
        this.readableString = readableString;
    }

    public String toReadableString() {
        return readableString;
    }
}
