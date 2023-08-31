package com.busted_moments.core.api.requests.player;

public enum StoreRank {
    CHAMPION("Champion"),
    HERO("Hero"),
    VIP_PLUS("VIP+"),
    VIP("VIP"),
    REGULAR("Regular");

    private final String readableString;

    StoreRank(String readableString) {
        this.readableString = readableString;
    }

    public static StoreRank fromApiString(String apiString) {
        if (apiString == null) {
            return REGULAR;
        }

        return valueOf(apiString.toLowerCase().replace("vip+", "vip_plus").toUpperCase());
    }

    public String toReadableString() {
        return this.readableString;
    }
}
