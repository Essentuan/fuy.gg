package com.busted_moments.core;

import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.TimeUnit;
import com.busted_moments.core.tuples.Pair;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.busted_moments.client.FuyMain.LOGGER;

public class Promise<T> implements Future<T>, CompletionStage<T> {
   private CompletableFuture<T> future;

   public Promise(CompletionStage<T> future) {
      this.future = future.toCompletableFuture();

      thenCatch(Promise::logError);
   }

   public Promise() {
      this(new CompletableFuture<>());

      thenCatch(Promise::logError);
   }

   public Promise(CompletionStage<T> future, Consumer<T> consumer) {
      this(future);

      thenAcceptAsync(consumer);
   }

   public Promise(T value) {
      this();

      complete(value);
   }

   public Promise(Consumer<Promise<T>> consumer) {
      this();

      completeAsync(consumer);
   }

   public Promise(Supplier<T> consumer) {
      this();

      completeAsync(consumer);
   }

   protected void setFuture(CompletableFuture<T> future) {
      this.future = future;
   }

   protected void setFuture(Promise<T> promise) {
      this.future = promise.toCompletableFuture();
   }

   public <S> Promise<Pair<T, S>> combine(Promise<S> promise) {
      return thenApplyStage((result1 -> promise.thenApply(result2 -> Pair.of(result1, result2))));
   }

   public <S> Promise<Pair<T, S>> combine(Supplier<Promise<S>> promise) {
      return thenApplyStage((result1 -> promise.get().thenApply(result2 -> Pair.of(result1, result2))));
   }

   public <U> Promise<U> thenApplyStage(BiConsumer<Promise<U>, T> stage) {
      return new Promise<>((future) -> thenAcceptAsync(result -> stage.accept(future, result)));
   }

   public <U> Promise<U> thenApplyStage(Function<T, Promise<U>> stage) {
      return new Promise<>(future -> this.thenAcceptAsync((result) -> stage.apply(result).thenAcceptAsync(future::complete)));
   }

   @Override
   public <U> Promise<U> thenApply(Function<? super T, ? extends U> fn) {
      return new Promise<>(future.thenApply(fn));
   }


   @Override
   public <U> Promise<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
      return new Promise<>(future.thenApplyAsync(fn));
   }

   @Override
   public <U> Promise<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
      return new Promise<>(future.thenApplyAsync(fn, executor));
   }

   @Override
   public Promise<Void> thenAccept(Consumer<? super T> action) {
      return new Promise<>(future.thenAccept(action));
   }

   @Override
   public Promise<Void> thenAcceptAsync(Consumer<? super T> action) {
      return new Promise<>(future.thenAcceptAsync(action));
   }

   @Override
   public Promise<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
      return new Promise<>(future.thenAcceptAsync(action, executor));
   }

   @Override
   public Promise<Void> thenRun(Runnable action) {
      return new Promise<>(future.thenRun(action));
   }

   @Override
   public Promise<Void> thenRunAsync(Runnable action) {
      return new Promise<>(future.thenRunAsync(action));
   }

   @Override
   public Promise<Void> thenRunAsync(Runnable action, Executor executor) {
      return new Promise<>(future.thenRunAsync(action, executor));
   }

   @Override
   public <U, V> Promise<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
      return new Promise<>(future.thenCombine(other, fn));
   }

   @Override
   public <U, V> Promise<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
      return new Promise<>(future.thenCombineAsync(other, fn));
   }

   @Override
   public <U, V> Promise<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
      return new Promise<>(future.thenCombineAsync(other, fn, executor));
   }

   @Override
   public <U> Promise<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
      return new Promise<>(future.thenAcceptBoth(other, action));
   }

   @Override
   public <U> Promise<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
      return new Promise<>(future.thenAcceptBothAsync(other, action));
   }

   @Override
   public <U> Promise<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor) {
      return new Promise<>(future.thenAcceptBothAsync(other, action, executor));
   }

   @Override
   public Promise<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
      return new Promise<>(future.runAfterBoth(other, action));
   }

   @Override
   public Promise<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
      return new Promise<>(future.runAfterBothAsync(other, action));
   }

   @Override
   public Promise<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
      return new Promise<>(future.runAfterBothAsync(other, action, executor));
   }

   @Override
   public <U> Promise<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
      return new Promise<>(future.applyToEither(other, fn));
   }

   @Override
   public <U> Promise<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
      return new Promise<>(future.applyToEitherAsync(other, fn));
   }

   @Override
   public <U> Promise<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor) {
      return new Promise<>(future.applyToEitherAsync(other, fn, executor));
   }

   @Override
   public Promise<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
      return new Promise<>(future.acceptEither(other, action));
   }

   @Override
   public Promise<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
      return new Promise<>(future.acceptEitherAsync(other, action));
   }

   @Override
   public Promise<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor) {
      return new Promise<>(future.acceptEitherAsync(other, action, executor));
   }

   @Override
   public Promise<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
      return new Promise<>(future.runAfterEither(other, action));
   }

   @Override
   public Promise<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
      return new Promise<>(future.runAfterEitherAsync(other, action));
   }

   @Override
   public Promise<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
      return new Promise<>(future.runAfterEitherAsync(other, action, executor));
   }

   @Override
   public <U> Promise<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
      return new Promise<>(future.thenCompose(fn));
   }

   @Override
   public <U> Promise<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
      return new Promise<>(future.thenComposeAsync(fn));
   }

   @Override
   public <U> Promise<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) {
      return new Promise<>(future.thenComposeAsync(fn, executor));
   }

   @Override
   public <U> Promise<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
      return new Promise<>(future.handle(fn));
   }

   @Override
   public <U> Promise<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
      return new Promise<>(future.handleAsync(fn));
   }

   @Override
   public <U> Promise<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
      return new Promise<>(future.handleAsync(fn, executor));
   }

   @Override
   public Promise<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
      return new Promise<>(future.whenComplete(action));
   }

   @Override
   public Promise<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
      return new Promise<>(future.whenCompleteAsync(action));
   }

   @Override
   public Promise<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
      return new Promise<>(future.whenCompleteAsync(action, executor));
   }

   @Override
   public Promise<T> exceptionally(Function<Throwable, ? extends T> fn) {
      return new Promise<>(future.exceptionally(fn));
   }

   @SuppressWarnings("unchecked")
   public <E extends Throwable> Promise<T> thenCatch(Class<E> exception, Consumer<E> handler) {
      future.exceptionally((throwable -> {
         if (exception.isAssignableFrom(throwable.getClass())) {
            handler.accept((E) throwable);
         }

         return null;
      }));

      return this;
   }

   public Promise<T> thenCatch(Consumer<Throwable> handler) {
      return thenCatch(Throwable.class, handler);
   }

   @SuppressWarnings("unchecked")
   public <E extends Throwable> Promise<T> thenCatchAsync(Class<E> exception, Consumer<E> handler) {
      future.exceptionally((throwable -> {
         if (exception.isAssignableFrom(throwable.getClass())) {
            future.defaultExecutor().execute(() -> handler.accept((E) throwable));
         }

         return null;
      }));

      return this;
   }

   public Promise<T> thenCatchAsync(Consumer<Throwable> handler) {
      return thenCatchAsync(Throwable.class, handler);
   }


   @Override
   public CompletableFuture<T> toCompletableFuture() {
      return future;
   }

   @Override
   public boolean cancel(boolean mayInterruptIfRunning) {
      return future.cancel(mayInterruptIfRunning);
   }

   @Override
   public boolean isCancelled() {
      return future.isCancelled();
   }

   @Override
   public boolean isDone() {
      return future.isDone();
   }

   public boolean isPending() {
      return !isDone();
   }

   public T await() {
      return future.join();
   }

   @Override
   public T get() throws InterruptedException, ExecutionException {
      return future.get();
   }

   @Override
   @Deprecated
   public T get(long timeout, @NotNull java.util.concurrent.TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return future.get(timeout, unit);
   }

   public T get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return get(Duration.of(timeout, unit));
   }


   public T get(Duration timeout) throws InterruptedException, ExecutionException, TimeoutException {
      return future.get((long) timeout.toMills(), java.util.concurrent.TimeUnit.MILLISECONDS);
   }

   public boolean complete(T value) {
      return future.complete(value);
   }

   public Promise<T> completeAsync(Consumer<Promise<T>> consumer) {
      future.defaultExecutor().execute(() -> {
         try {
            consumer.accept(this);
         } catch (Throwable t) {
            this.Throw(t);
            LOGGER.error("", t);
         }
      });

      return this;
   }

   public Promise<T> completeAsync(Supplier<? extends T> supplier) {
      return new Promise<>(future.completeAsync(supplier));
   }

   public Promise<T> completeAsync(Supplier<? extends T> supplier, Executor executor) {
      return new Promise<>(future.completeAsync(supplier, executor));
   }

   public Promise<T> completeOnTimeout(Supplier<T> supplier, Duration duration) {
      new Promise<>().future.completeOnTimeout(null, (long) duration.toMills(), java.util.concurrent.TimeUnit.MILLISECONDS)
              .thenAccept(ignored -> {
                 if (this.isPending()) this.complete(supplier.get());
              });

      return this;
   }

   public Promise<T> completeOnTimeout(Supplier<T> supplier, long timeout, TimeUnit unit) {
      return completeOnTimeout(supplier, Duration.of(timeout, unit));
   }

   public Promise<T> completeOnTimeout(T value, Duration duration) {
      return completeOnTimeout(() -> value, duration);
   }

   public Promise<T> completeOnTimeout(T value, long timeout, TimeUnit unit) {
      return completeOnTimeout(value, Duration.of(timeout, unit));
   }

   public void Throw(Throwable ex) {
      future.completeExceptionally(ex);
   }

   public Promise<T> orTimeout(Duration duration) {
      future.orTimeout((long) duration.toMills(), java.util.concurrent.TimeUnit.MILLISECONDS);

      return this;
   }

   public Promise<T> orTimeout(long timeout, TimeUnit unit) {
      return orTimeout(Duration.of(timeout, unit));
   }

   public Promise<T> onTimeout(Runnable runnable, long timeout, TimeUnit unit) {
      return onTimeout(runnable, Duration.of(timeout, unit));
   }

   public Promise<T> onTimeout(Runnable runnable, Duration duration) {
      return orTimeout(duration).thenCatch(TimeoutException.class, (ignored) -> runnable.run());
   }

   public static Promise<Void> sleep(long duration, TimeUnit unit) {
      return new Promise<Void>().completeOnTimeout((Void) null, duration, unit);
   }

   public static Promise<Void> sleep(Duration duration) {
      return new Promise<Void>().completeOnTimeout((Void) null, duration);
   }

   public static <T, U> Promise<List<U>> allOf(Collection<Promise<T>> promises, Function<T, U> mapper) {
      return new Promise<>(future -> {
         List<U> responses = new CopyOnWriteArrayList<>();

         promises.forEach(promise -> promise.thenCatch(future::Throw).thenAccept(result -> {
                    responses.add(mapper.apply(result));

                    if (responses.size() == promises.size()) {
                       future.complete(responses);
                    }
                 })
         );
      });
   }

   public static <T> Promise<List<T>> allOf(Iterable<Promise<T>> promises) {
      return allOf(promises, result -> result);
   }

   public static <T, U> Promise<List<U>> allOf(Iterable<Promise<T>> promises, Function<T, U> mapper) {
      return allOf(Sets.newHashSet(promises), mapper);
   }

   public static <T> Promise<List<T>> allOf(Stream<Promise<T>> promises) {
      return allOf(promises, result -> result);
   }

   public static <T, U> Promise<List<U>> allOf(Stream<Promise<T>> promises, Function<T, U> mapper) {
      return allOf(promises.toList(), mapper);
   }

   @SafeVarargs
   public static <T> Promise<List<T>> allOf(Promise<T>... promises) {
      return allOf(List.of(promises));
   }

   @SafeVarargs
   public static <T, U> Promise<List<U>> allOf(Function<T, U> mapper, Promise<T>... promises) {
      return allOf(List.of(promises), mapper);
   }

   public static <K, V> Promise<Map<K, V>> all(Collection<Pair<K, Promise<V>>> pairs) {
      return allOf(pairs.stream().map(pair -> pair.two().thenApply(result -> Pair.of(pair.one(), result)))).thenApply(result ->
              result.stream().collect(Collectors.toMap(Pair::one, Pair::two))
      );
   }

   public static <T> Promise<Void> all(Stream<Promise<T>> promises) {
      return allOf(promises).thenApply(ignored -> null);
   }


   @SafeVarargs
   public static <K, V> Promise<Map<K, V>> all(Pair<K, Promise<V>>... pairs) {
      return all(List.of(pairs));
   }


   public static <T> Promise<Void> all(List<Promise<T>> promises) {
      return allOf(promises).thenApply(ignored -> null);
   }

   @SafeVarargs
   public static <T> Promise<Void> all(Promise<T>... promises) {
      return all(List.of(promises));
   }

   public static <T> Promise<T> of(CompletionStage<T> stage) {
      return new Promise<>(stage);
   }

   public static <T> Promise<T> of(Supplier<CompletionStage<T>> stage) {
      return new Promise<>(stage.get());
   }


   public static <T> Promise<T> of(T value) {
      return new Promise<>(value);
   }

   public static Promise<Void> execute(Runnable runnable) {
      return new Promise<>(promise -> {
         runnable.run();

         promise.complete(null);
      });
   }

   public static <T> Promise<T> completed() {
      return new Promise<>((T) null);
   }

   private static void logError(Throwable t) {
      if (t instanceof CompletionException e) LOGGER.error("Error in promise", e.getCause());
      else LOGGER.error("Error in promise", t);
   }

   public static class Getter<T> {
      protected Promise<T> promise = null;

      protected Supplier<Promise<T>> supplier;

      public Getter(Supplier<Promise<T>> supplier) {
         this.supplier = supplier;
      }
      public Promise<T> get() {
         return (promise == null ? (promise = supplier.get()) : promise);
      }

      public boolean isPending() {
         return promise != null;
      }

      public boolean isDone() {
         return isPending() && promise.isDone();
      }

      public void set(Promise<T> promise) {
         this.promise = promise;
      }

      public void set(T result) {
         set(new Promise<>(result));
      }

      public void reset() {
         promise = null;
      }
   }
}