package com.busted_moments.core.heartbeat;

import com.busted_moments.core.heartbeat.annotations.Schedule;
import com.busted_moments.core.time.ChronoUnit;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.util.Reflection;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.busted_moments.client.FuyMain.CLASS_SCANNER;
import static com.busted_moments.client.FuyMain.LOGGER;

public class Heartbeat {
   private final static Duration WARNING_DURATION = Duration.of(1, TimeUnit.SECONDS);
   private final static Duration ERROR_DURATION = Duration.of(7, TimeUnit.SECONDS);

   private static ScheduledExecutorService TASK_THREAD_POOL;
   private static ScheduledExecutorService SCHEDULER_THREAD_POOL;
   private static ScheduledFuture<?> SCHEDULER_THREAD;

   private static Date lastCompletion = new Date();

   private static final Map<String, Task> REGISTERED_TASKS = new ConcurrentHashMap<>();

   private static boolean hasSentWarningMessage = false;

   public static void create() {
      TASK_THREAD_POOL = Executors.newScheduledThreadPool(3);
      SCHEDULER_THREAD_POOL = Executors.newScheduledThreadPool(2);

      restart();

      SCHEDULER_THREAD_POOL.scheduleAtFixedRate(Heartbeat::detectStalling, 0, 5, TimeUnit.SECONDS);

      CLASS_SCANNER.getMethodsAnnotatedWith(Schedule.class).stream()
              .filter(Reflection::isStatic)
              .forEach(method -> register(method, null, task -> true));
   }


   private static void detectStalling() {
      if (Duration.since(lastCompletion).greaterThanOrEqual(WARNING_DURATION) && !hasSentWarningMessage) {
         LOGGER.warn("Heartbeat thread may be inactive; waiting before restarting");

         hasSentWarningMessage = true;

         return;
      }

      if (Duration.since(lastCompletion).greaterThanOrEqual(ERROR_DURATION)) {
         LOGGER.error("Heartbeat has not responded; restarting");
         restart();

         hasSentWarningMessage = false;
      }
   }

   private static void restart() {
      if (SCHEDULER_THREAD != null) SCHEDULER_THREAD.cancel(true);

      SCHEDULER_THREAD = SCHEDULER_THREAD_POOL.scheduleAtFixedRate(Heartbeat::doTick, 0, 1, TimeUnit.MILLISECONDS);
   }

   private static void doTick() {
      try {
         for (Task task : REGISTERED_TASKS.values()) {
            try {
               task.cullInactiveThreads();
            } catch (Exception e) {
               LOGGER.error("Error while culling threads", e);
            }

            if (task.shouldExecute()) {
               task.getActiveThreads().add(new ExecutingThread<>(TASK_THREAD_POOL
                       .schedule(
                               () -> {
                                  try {
                                     task.getRunnable().run();
                                  } catch (Throwable e) {
                                     LOGGER.error("Error when running task {}", task.uuid, e);
                                  }
                               },
                               0,
                               TimeUnit.MILLISECONDS
                       )));

               task.setLastExecution(new Date());
            }
         }
      } catch (Throwable e) {
         LOGGER.error("Caught error while doing tick (THIS IS ABNORMAL FIX THIS IMMEDIATELY)", e);
      }

      lastCompletion = new Date();
   }

   public static void schedule(Runnable runnable, long delay, ChronoUnit unit) {
      TASK_THREAD_POOL.schedule(runnable, delay, ChronoUnit.toNative(unit));
   }

   public static void execute(Runnable runnable) {
      TASK_THREAD_POOL.execute(runnable);
   }

   public static String register(Task task) {
      UUID uuid = UUID.randomUUID();

      REGISTERED_TASKS.put(uuid.toString(), task);

      return uuid.toString();
   }

   public static String register(Task task, String uuid) {
      REGISTERED_TASKS.put(uuid, task);

      return uuid;
   }

   public static void register(Method method, @Nullable Object instance, Function<Task, Boolean> shouldExecute) {
      String uid = instance == null ? Reflection.getUID(method) : Reflection.getUID(method, instance);
      method.setAccessible(true);

      if (getTask(uid).isPresent()) return;

      new Task(method.getAnnotation(Schedule.class), () -> {
         try {
            method.invoke(instance);
         } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
         }
      }, task -> {
         REGISTERED_TASKS.put(uid, task);

         return uid;
      }) {
         @Override
         public boolean shouldExecute() {
            return shouldExecute.apply(this) && super.shouldExecute();
         }
      };
   }

   public static void remove(Method method, @Nullable Object instance) {
      REGISTERED_TASKS.remove(instance == null ? Reflection.getUID(method) : Reflection.getUID(method, instance));
   }

   public static Optional<Task> getTask(String uid) {
      return Optional.ofNullable(REGISTERED_TASKS.get(uid));
   }

   public static Optional<Task> getTask(Method method, @Nullable Object instance) {
      return getTask(instance == null ? Reflection.getUID(method) : Reflection.getUID(method, instance));
   }

   public static Stream<Method> getTasks(Class<?> clazz, Predicate<Method> predicate) {
      return Reflection.visit(clazz)
              .map(Class::getDeclaredMethods)
              .flatMap(Stream::of)
              .filter(method -> method.isAnnotationPresent(Schedule.class) && predicate.test(method));
   }
}
