package com.busted_moments.core.http.requests;

import com.busted_moments.core.http.AbstractRequest;
import com.busted_moments.core.http.GetRequest;
import com.busted_moments.core.http.RateLimit;
import com.busted_moments.core.json.Json;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public record Update(Version version, String file, URL url) implements Version {
   Update(Version version, Json asset) throws MalformedURLException {
      this(
              version,
              asset.getString("name"),
              new URL(asset.getString("browser_download_url"))
      );
   }

   public InputStream download() throws IOException {
      return url().openStream();
   }

   public boolean greaterThan(Version version) {
      return this.compareTo(version) > 0;
   }

   public boolean lessThan(Version version) {
      return this.compareTo(version) < 0;
   }

   @Override
   public int compareTo(@NotNull Version o) {
      return version.compareTo(o);
   }

   @Override
   public String getFriendlyString() {
      return version.getFriendlyString();
   }

   @AbstractRequest.Definition(route = "https://api.github.com/repos/Essentuan/fuy.gg/releases/latest", ratelimit = RateLimit.NONE)
   public static class Request extends GetRequest<Update> {

      @Nullable
      @Override
      protected Update get(Json json) {
         try {
            return new Update(
                    Version.parse(json.getString("tag_name").substring(1)),
                    json.getList("assets", Json.class).stream().filter(asset -> !asset.getString("name").contains("sources")).findFirst().orElseThrow()
            );
         } catch (VersionParsingException | MalformedURLException e) {
            throw new RuntimeException(e);
         }
      }
   }
}
