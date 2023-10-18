package com.busted_moments.core.api.requests.serverlist;

import com.busted_moments.core.api.internal.GetRequest;
import com.busted_moments.core.api.internal.RateLimit;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.json.template.JsonTemplate;
import com.busted_moments.core.tuples.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServerList extends JsonTemplate implements Collection<World> {
    @Entry private Map<String, World> servers;

    public World get(String world) {
        return servers.get(world);
    }

    public Map<String, World> getPlayerList() {
        Map<String, World> players = new HashMap<>();

        servers.values().stream()
                .flatMap(world -> world.stream().map(player -> new Pair<>(player, world)))
                .forEach(record -> players.put(record.one(), record.two()));

        return players;
    }

    public World findPlayer(String player) {
        return getPlayerList().get(player);
    }

    @Override
    public int size() {
        return servers.size();
    }

    @Override
    public boolean isEmpty() {
        return servers.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof World world) {
            return servers.containsValue(world);
        }

        return false;
    }

    public boolean contains(String world) {
        return servers.containsKey(world);
    }

    @NotNull
    @Override
    public Iterator<World> iterator() {
        return servers.values().iterator();
    }

    @NotNull
    @Override
    public World @NotNull [] toArray() {
        return servers.values().toArray(World[]::new);
    }

    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        return servers.values().toArray(a);
    }

    @Override
    public boolean add(World world) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return servers.values().containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends World> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonTemplate load(Json json) {
        json.getJson("servers").values().forEach(o -> {
            if (!(o instanceof Json world))
                return;

            var iter = world.getList("players", Object.class).listIterator();
            while (iter.hasNext())
                iter.set(iter.next().toString());

        });

        super.load(json);

        servers.forEach((worldName, world) -> world.world = worldName);

        return this;
    }

    @com.busted_moments.core.api.internal.Request.Definition(route = "https://athena.wynntils.com/cache/get/serverList", cache_length = 0, ratelimit = RateLimit.NONE)
    public static class Request extends GetRequest<ServerList> {
        @Override
        @org.jetbrains.annotations.Nullable
        protected ServerList get(Json json) {
            return json.wrap(ServerList::new);
        }
    }
}
