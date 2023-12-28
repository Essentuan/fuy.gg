package com.busted_moments.core.artemis.functions;

import com.busted_moments.client.events.mc.MinecraftStartupEvent;
import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.events.EventListener;
import com.busted_moments.core.heartbeat.Scheduler;
import com.busted_moments.core.util.Reflection;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.functions.FunctionManager;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.busted_moments.client.FuyMain.CLASS_SCANNER;

public abstract class Function<T> extends com.wynntils.core.consumers.functions.Function<T> implements EventListener, Scheduler {
   private final List<String> aliases;
   private final List<Arg> args;

   protected Function() {
      Annotated annotations = new Annotated(this.getClass(), Annotated.Required(With.class));

      aliases = List.of(annotations.getAnnotation(With.class, With::value));
      args = List.of(annotations.getAnnotation(With.class, With::args));

      REGISTER_EVENTS();
      REGISTER_TASKS();
   }

   protected abstract T call(FunctionArguments args);

   @Override
   public FunctionArguments.Builder getArgumentsBuilder() {
      if (args.isEmpty())
         return FunctionArguments.OptionalArgumentBuilder.EMPTY;

      List<FunctionArguments.Argument<?>> result = new ArrayList<>(args.size());
      args.forEach(arg -> result.add(new FunctionArguments.Argument<>(arg.name(), arg.cls(), null)));

      return new FunctionArguments.RequiredArgumentBuilder(result);
   }

   @Override
   public T getValue(FunctionArguments functionArguments) {
      return call(functionArguments);
   }

   @Override
   protected List<String> getAliases() {
      return aliases;
   }

   @Target(ElementType.TYPE)
   @Retention(RetentionPolicy.RUNTIME)
   public @interface With {
      String[] value();
      Arg[] args() default {};
   }

   @SubscribeEvent(priority = EventPriority.LOW)
   public static void onMinecraftStart(MinecraftStartupEvent event) throws NoSuchMethodException {
      Method method = FunctionManager.class.getDeclaredMethod("registerFunction", com.wynntils.core.consumers.functions.Function.class);
      method.setAccessible(true);

      CLASS_SCANNER.getSubTypesOf(Function.class)
              .stream()
              .filter(Predicate.not(Reflection::isAbstract))
              .map(cls -> {
                 try {
                    var constructor = cls.getConstructor();

                    return constructor.newInstance();
                 } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                          IllegalAccessException e) {
                    throw new RuntimeException(e);
                 }
              }).forEach(function -> {
                 try {
                    method.invoke(Managers.Function, function);
                 } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                 }
              });
   }
}
