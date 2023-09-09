package com.busted_moments.core.heartbeat;

import com.busted_moments.core.util.Reflection;

public interface Scheduler {
   default boolean SHOULD_EXECUTE(Task task) {
      return true;
   }

   default void REGISTER_TASKS() {
      Heartbeat.getTasks(getClass(), method -> !Reflection.isStatic(method)).forEach(method -> Heartbeat.register(method, this, this::SHOULD_EXECUTE));
   }

   default void UNREGISTER_TASKS() {
      Heartbeat.getTasks(getClass(), method -> !Reflection.isStatic(method)).forEach(method -> Heartbeat.remove(method, this));
   }
}
