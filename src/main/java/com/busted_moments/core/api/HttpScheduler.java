package com.busted_moments.core.api;

import com.busted_moments.core.Promise;
import com.busted_moments.core.api.internal.Request;
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

   private static final List<Request<?>> QUEUE = new CopyOnWriteArrayList<>();

   private static final Map<String, Request<?>> OUTBOUND_REQUESTS = new ConcurrentHashMap<>();

   private static Map<String, Request<?>> CACHE = new TempMap<>((key, request) -> request.getCacheDuration());

   private synchronized static void fillOutbound() {
      QUEUE.sort(Request::compareTo);

      Iterator<Request<?>> iterator = QUEUE.iterator();

      while (iterator.hasNext() && OUTBOUND_REQUESTS.size() < MAX_PENDING_REQUESTS) {
         Request<?> request = iterator.next();

         if (request.canRequest()) {
            sendRequest(request);
         }
      }

      QUEUE.removeIf(request -> OUTBOUND_REQUESTS.containsKey(request.getUrl()));
   }

   private static void sendRequest(Request<?> request) {
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

   private static void onRequestSuccess(Request<?> request, HttpResponse<String> response) {
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

   private static void onRequestFailure(Request<?> request, Throwable ex) {
      LOGGER.error("Failed to complete outbound request {}{url={}}", request.getClass().getSimpleName(), request.getUrl(), ex);

      request.complete(Optional.empty());

      onRequestFinish(request);
   }

   private static void onRequestFinish(Request<?> request) {
      OUTBOUND_REQUESTS.remove(request.getUrl());

      fillOutbound();
   }


   private static Optional<Request<?>> returnIfEquals(Request<?> request1, Request<?> request2) {
      return (request1.getClass().equals(request2.getClass())) ?  Optional.of(request1) : Optional.empty();
   }

   public static Optional<Request<?>> getExisting(Request<?> request) {
      if (OUTBOUND_REQUESTS.containsKey(request.getUrl())) {
         return returnIfEquals(OUTBOUND_REQUESTS.get(request.getUrl()), request);
      }

      if (CACHE.containsKey(request.getUrl())) {
         return returnIfEquals(CACHE.get(request.getUrl()), request);
      }

      for (Request<?> req : QUEUE) {
         if (req.getUrl().equals(request.getUrl()) && req.getClass().equals(request.getClass())) {
            return Optional.of(req);
         }
      }

      return Optional.empty();
   }

   public static void execute(Request<?> request) {
      QUEUE.add(request);

      fillOutbound();
   }
   public static List<Request<?>> getQueue() {
      return QUEUE;
   }

   public static Map<String, Request<?>> getOutbound() {
      return OUTBOUND_REQUESTS;
   }
}