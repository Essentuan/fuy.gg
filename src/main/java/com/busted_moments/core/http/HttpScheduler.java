package com.busted_moments.core.http;

import com.busted_moments.core.Promise;
import com.busted_moments.core.time.ChronoUnit;
import com.busted_moments.core.util.TempMap;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.busted_moments.client.FuyMain.LOGGER;

public class HttpScheduler {
   private static final int MAX_PENDING_REQUESTS = 50;

   private static final HttpClient CLIENT = HttpClient.newHttpClient();

   private static final List<AbstractRequest<?>> QUEUE = new CopyOnWriteArrayList<>();

   private static final Map<String, AbstractRequest<?>> OUTBOUND_REQUESTS = new ConcurrentHashMap<>();

   private static Map<String, AbstractRequest<?>> CACHE = new TempMap<>((key, request) -> request.getCacheDuration());

   private synchronized static void fillOutbound() {
      QUEUE.sort(AbstractRequest::compareTo);

      Iterator<AbstractRequest<?>> iterator = QUEUE.iterator();

      while (iterator.hasNext() && OUTBOUND_REQUESTS.size() < MAX_PENDING_REQUESTS) {
         AbstractRequest<?> request = iterator.next();

         if (request.canRequest()) {
            sendRequest(request);
         }
      }

      QUEUE.removeIf(request -> OUTBOUND_REQUESTS.containsKey(request.getUrl()));
   }

   private static void sendRequest(AbstractRequest<?> request) {
      request.getRateLimit().handleRequest(request);

      OUTBOUND_REQUESTS.put(request.getUrl(), request);

      Promise.of(CLIENT.sendAsync(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)))
              .completeOnTimeout((HttpResponse<String>) null, 10, ChronoUnit.SECONDS)
              .thenCatch(t -> onRequestFailure(request, t))
              .thenAccept(res -> {
                 if (res == null) {
                    request.complete(Optional.empty());
                    onRequestFinish(request);
                 } else onRequestSuccess(request, res);
              });


   }

   private static void onRequestSuccess(AbstractRequest<?> request, HttpResponse<String> response) {
      try {
         request.getRateLimit().handleResponse(response);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }

      if (request.fulfill(response)) {
         CACHE.put(request.getUrl(), request);
      }

      onRequestFinish(request);
   }

   private static void onRequestFailure(AbstractRequest<?> request, Throwable ex) {
      LOGGER.error("Failed to complete outbound request {}{url={}}", request.getClass().getSimpleName(), request.getUrl(), ex);

      request.complete(Optional.empty());

      onRequestFinish(request);
   }

   private static void onRequestFinish(AbstractRequest<?> request) {
      OUTBOUND_REQUESTS.remove(request.getUrl());

      fillOutbound();
   }


   private static Optional<AbstractRequest<?>> returnIfEquals(AbstractRequest<?> request1, AbstractRequest<?> request2) {
      return (request1.getClass().equals(request2.getClass())) ?  Optional.of(request1) : Optional.empty();
   }

   public static Optional<AbstractRequest<?>> getExisting(AbstractRequest<?> request) {
      if (OUTBOUND_REQUESTS.containsKey(request.getUrl())) {
         return returnIfEquals(OUTBOUND_REQUESTS.get(request.getUrl()), request);
      }

      if (CACHE.containsKey(request.getUrl())) {
         return returnIfEquals(CACHE.get(request.getUrl()), request);
      }

      for (AbstractRequest<?> req : QUEUE) {
         if (req.getUrl().equals(request.getUrl()) && req.getClass().equals(request.getClass())) {
            return Optional.of(req);
         }
      }

      return Optional.empty();
   }

   public static void execute(AbstractRequest<?> request) {
      QUEUE.add(request);

      fillOutbound();
   }
   public static List<AbstractRequest<?>> getQueue() {
      return QUEUE;
   }

   public static Map<String, AbstractRequest<?>> getOutbound() {
      return OUTBOUND_REQUESTS;
   }
}