package com.busted_moments.core.api.requests.player.Class;

public enum ClassType {
    ASSASSIN("Assassin", "assassin"),
    NINJA("Ninja", "ninja"),
    ARCHER("Archer", "archer"),
    HUNTER("Hunter", "hunter"),
    MAGE("Mage", "mage"),
    DARK_WIZARD("Dark Wizard", "darkwizard"),
    WARRIOR("Warrior", "warrior"),
    KNIGHT("Knight", "knight"),
    SHAMAN("Shaman", "shaman"),
    SKYSEER("Skyseer", "skyseer");

    private final String readableString;
    private final String apiString;

    ClassType(String readableString, String apiString) {
        this.readableString = readableString;
        this.apiString = apiString;
    }

    public static ClassType fromApiString(String string) {
        for (ClassType type : ClassType.values()) {
            if (type.getApiString().equals(string.toLowerCase())) {
                return type;
            }
        }

        return null;
    }

    public String getReadableString() {
        return readableString;
    }

    public String getApiString() {
        return this.apiString;
    }
}
