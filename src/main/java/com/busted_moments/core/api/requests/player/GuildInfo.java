package com.busted_moments.core.api.requests.player;

import com.busted_moments.core.Promise;
import com.busted_moments.core.api.requests.Guild;
import com.busted_moments.core.json.template.JsonTemplate;

import java.util.Optional;

public class GuildInfo extends JsonTemplate {
    @Entry @Nullable private String name;
    @Entry @Nullable  Guild.Rank rank;

    protected final Promise.Getter<Optional<Guild>> guild = new Promise.Getter<>(() -> name == null ? Promise.of(Optional.empty()) : new Guild.Request(name));

    public Promise<Optional<Guild>> asGuild() {
        return guild.get();
    }

    public String getName() {
        return name;
    }

    public Guild.Rank getRank() {
        return rank;
    }
}
