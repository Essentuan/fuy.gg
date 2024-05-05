package com.busted_moments.client.screen.territories.search;

import com.busted_moments.client.events.mc.MinecraftStartupEvent;
import com.busted_moments.client.models.territory.eco.TerritoryEco;
import com.busted_moments.core.UnexpectedException;
import com.busted_moments.core.annotated.Annotated;
import net.minecraft.ChatFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.busted_moments.client.Client.CLASS_SCANNER;

public abstract class Criteria extends Annotated {
   private static final Map<String, Factory> CRITERIA = new LinkedHashMap<>();
   private static Pattern REGEX;

   private final String prefix;
   private final Operator operator;

   public Criteria(Operator operator) throws UnsupportedOperationException {
      super(Required(With.class), Required(Operators.class));

      this.prefix = getAnnotation(With.class).value();
      this.operator = operator;

      if (!Set.of(getAnnotation(Operators.class).value()).contains(operator))
         throw new UnsupportedOperationException();
   }

   private Criteria(Criteria parent) {
      this.prefix = parent.prefix;
      this.operator = parent.operator;
   }

   private Criteria(String prefix, Operator operator) {
      super(Required(Operators.class));

      this.prefix = prefix;
      this.operator = operator;

      if (!Set.of(getAnnotation(Operators.class).value()).contains(operator))
         throw new UnsupportedOperationException();
   }

   public String prefix() {
      return prefix;
   }

   public Operator operator() {
      return operator;
   }

   public abstract Compiled compile(String value);

   public List<String> suggestions() {
      return List.of();
   }

   public static Optional<Compiled> valueOf(String string) throws UnsupportedOperationException {
      Matcher matcher = REGEX.matcher(string);
      if (!matcher.matches())
         return Optional.empty();

      try {
         Factory factory = CRITERIA.get(matcher.group("criteria").toLowerCase());

         if (factory == null)
            return Optional.empty();

         Compiled compiled = factory
                 .create(Operator.from(matcher.group("operator")))
                 .compile(matcher.group("value"));

         compiled.prefix = matcher.group("criteria");
         compiled.operator = matcher.group("operator");

         return Optional.of(compiled);
      } catch (Exception e) {
         throw UnexpectedException.propagate(e);
      }
   }

   @SubscribeEvent
   private static void onMinecraftStart(MinecraftStartupEvent event) {
      int[] maxSize = { 0 };
      String operators = Arrays.stream(Operator.values())
              .map(Operator::asString)
              .peek(string -> {
                 if (string.length() > maxSize[0])
                    maxSize[0] = string.length();
              })
              .flatMapToInt(String::chars)
              .mapToObj(c -> (char) c)
              .distinct()
              .map(Object::toString)
              .collect(Collectors.joining());

      Stream<Factory> factories =
              Stream.concat(
                      CLASS_SCANNER.getSubTypesOf(Criteria.class)
                              .stream()
                              .filter(cls -> cls != Compiled.class && !Procedural.class.isAssignableFrom(cls))
                              .map(DefaultFactory::new),
                      CLASS_SCANNER.getSubTypesOf(Generator.class)
                              .stream()
                              .map(cls -> {
                                 try {
                                    return (Generator) cls.getConstructor().newInstance();
                                 } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                                          InvocationTargetException e) {
                                    throw UnexpectedException.propagate(e);
                                 }
                              }).flatMap(Supplier::get)
              );

      for (var iter = factories.iterator(); iter.hasNext(); ) {
         Factory factory = iter.next();

         CRITERIA.put(factory.prefix().toLowerCase(), factory);
      }

      REGEX = Pattern.compile("(?<criteria>[^%s]*)(?<operator>[%s]{1,%s})(?<value>.*)".formatted(operators, operators, maxSize[0]), Pattern.CASE_INSENSITIVE);
   }

   public static class Compiled extends Criteria implements Predicate<TerritoryEco> {
      private final Criteria base;
      private final Predicate<TerritoryEco> predicate;

      private String prefix;
      private String operator;
      private final String value;

      private String styled;

      public Compiled(Criteria base, String value, Predicate<TerritoryEco> predicate) throws UnsupportedOperationException {
         super(base);

         this.base = base;
         this.predicate = predicate;

         this.prefix = super.prefix();
         this.operator = operator().asString();
         this.value = value;
      }

      @Override
      public Compiled compile(String value) {
         throw new UnsupportedOperationException("Cannot compile already compiled criteria");
      }

      @Override
      public List<String> suggestions() {
         return base.suggestions();
      }

      @Override
      public boolean test(TerritoryEco territoryEco) {
         return predicate.test(territoryEco);
      }

      public String styled() {
         if (styled == null)
            styled = ChatFormatting.AQUA + prefix + ChatFormatting.YELLOW + operator + ChatFormatting.LIGHT_PURPLE + value + ChatFormatting.RESET;

         return styled;
      }
   }

   public abstract static class Procedural extends Criteria {
      public Procedural(String prefix, Operator operator) throws UnsupportedOperationException {
         super(prefix, operator);
      }
   }

   public interface Generator extends Supplier<Stream<Factory>> { }

   public interface Factory {
      String prefix();
      Set<Operator> operators();

      Class<? extends Criteria> cls();

      Criteria create(Operator operator);
   }

   @Target(ElementType.TYPE)
   @Retention(RetentionPolicy.RUNTIME)
   public @interface With {
      String value();
   }

   public static Collection<Factory> values() {
      return CRITERIA.values();
   }
}
