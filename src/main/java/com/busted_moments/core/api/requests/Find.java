
package com.busted_moments.core.api.requests;

import com.busted_moments.core.api.internal.GetRequest;
import com.busted_moments.core.api.internal.RateLimit;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.json.template.JsonTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface Find {
   class Result extends JsonTemplate {
      @Entry private String username;
      @Entry private UUID uuid;
      @Entry @Nullable String world;

      public String getUsername() {
         return username;
      }

      public UUID getUuid() {
         return uuid;
      }

      public Optional<String> getWorld() {
         return Optional.ofNullable(world);
      }
   }

   @com.busted_moments.core.api.internal.Request.Definition(route = "https://thesimpleones.net/api/find?q=%s", ratelimit = RateLimit.NONE, cache_length = 0)
   class Request extends GetRequest<Result> {
      public Request(String string) {
         super(string);
      }

      @Nullable
      @Override
      protected Result get(Json json) {
         return json.wrap(Result::new);
      }
   }
}