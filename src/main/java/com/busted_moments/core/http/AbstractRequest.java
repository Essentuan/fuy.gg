package com.busted_moments.core.http;

import com.busted_moments.core.Promise;
import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.ChronoUnit;
import com.busted_moments.core.util.Priority;
import com.wynntils.core.net.NetManager;
import org.apache.http.HttpStatus;

import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

import static com.busted_moments.client.FuyMain.LOGGER;

public abstract class AbstractRequest<T> extends Promise<Optional<T>> implements Comparable<AbstractRequest<?>> {
   private final String url;
   private final RateLimit ratelimit;
   private final Duration cacheDuration;

   private Priority priority = Priority.NORMAL;

   @SuppressWarnings({"unchecked"})
   public AbstractRequest(Object... args) {
      Definition config = new Annotated(getClass(), Annotated.Required(Definition.class)).getAnnotation(Definition.class);

      this.url = config.route().formatted(Stream.of(args).map(object -> URLEncoder.encode(object.toString(), StandardCharsets.UTF_8)).toArray());

      this.ratelimit = config.ratelimit();

      this.cacheDuration = Duration.of(config.cache_length(), config.cache_unit());

      Optional<AbstractRequest<?>> request = HttpScheduler.getExisting(this);

      request.ifPresentOrElse(value -> setFuture((AbstractRequest<T>) value), () -> HttpScheduler.execute(this));
   }

   public String getUrl() {
      return url;
   }

   public RateLimit getRateLimit() {
      return ratelimit;
   }

   public boolean canRequest() {
      return getRateLimit().canRequest(this);
   }

   public Duration getCacheDuration() {
      return cacheDuration;
   }

   public Priority getPriority() {
      return priority;
   }

   public AbstractRequest<T> priority(Priority priority) {
      this.priority = priority;

      return this;
   }

   protected abstract @Nullable T get(Json json);

   protected Optional<Json> getJson(HttpResponse<String> response) {
      return Json.tryParse(response.body());
   }

   protected Optional<T> process(HttpResponse<String> request) {
      Optional<Json> json;

      if (request.statusCode() != HttpStatus.SC_OK || (json = getJson(request)).isEmpty()) {
         return Optional.empty();
      }

      try {
         T result = get(json.get());

         return Optional.ofNullable(result);
      } catch (Exception e) {
         LOGGER.error("Failed to get result from request {}{url={}}", getClass().getSimpleName(), getUrl(), e);

         return Optional.empty();
      }
   }

   public boolean fulfill(HttpResponse<String> request) {
      Optional<T> result = process(request);

      complete(result);

      return result.isPresent();
   }

   private static java.time.Duration TIMEOUT = Duration.of(10, ChronoUnit.SECONDS).toNative();

   protected HttpRequest.Builder getBuilder() {
      return HttpRequest.newBuilder()
              .uri(URI.create(getUrl()))
              .timeout(TIMEOUT)
              .header("User-Agent", getUserAgent());
   }

   public abstract HttpRequest build();

   @Override
   public int compareTo(AbstractRequest<?> request) {
      return getPriority().compareTo(request.getPriority());
   }

   private static String USER_AGENT = null;

   public static String getUserAgent() {
      if (USER_AGENT == null) loadUserAgent();

      return USER_AGENT;
   }

   private static void loadUserAgent() {
      try {
         Field field = NetManager.class.getDeclaredField("USER_AGENT");

         field.setAccessible(true);

         USER_AGENT = (String) field.get(null);
      } catch (NoSuchFieldException | IllegalAccessException e) {
         throw new RuntimeException(e);
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.TYPE})
   public @interface Definition {
       String route();
       RateLimit ratelimit();
       long cache_length() default 1;
       ChronoUnit cache_unit() default ChronoUnit.MINUTES;
   }
}
