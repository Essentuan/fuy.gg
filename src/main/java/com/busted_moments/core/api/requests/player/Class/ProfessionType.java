package com.busted_moments.core.api.requests.player.Class;

public enum ProfessionType {
    ALCHEMISM("Alchemism"),
    ARMOURING("Armoring"),
    COMBAT("Combat"),
    COOKING("Cooking"),
    FARMING("Farming"),
    FISHING("Fishing"),
    JEWELING("Jeweling"),
    MINING("Mining"),
    SCRIBING("Scribing"),
    TAILORING("Tailoring"),
    WEAPON_SMITHING("Weapon Smithing"),
    WOODCUTTING("Wood Cutting"),
    WOODWORKING("Wood Working");

    private final String readableString;

    ProfessionType(String readableString) {
        this.readableString = readableString;
    }

    public String toReadableString() {
        return readableString;
    }
}
