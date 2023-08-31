package com.busted_moments.core.api.requests.player;

public enum PlayerRank {
    ADMINISTRATOR("Administrator"),
    MODERATOR("Moderator"),
    BUILDER("Builder"),
    ITEM("Item Team"),
    GAME_MASTER("Game Master"),
    CMD("CMD"),
    MUSIC("Music"),
    HYBRID("Hybrid"),
    MEDIA("Media"),
    ART("Art"),
    PLAYER("Player");

    private final String readableString;

    PlayerRank(String readableString) {
        this.readableString = readableString;
    }

    public String toReadableString() {
        return readableString;
    }

    public static PlayerRank fromApiString(String apiString) {
        return valueOf(apiString.toLowerCase().replaceAll(" ", "_").toUpperCase());
    }
}
