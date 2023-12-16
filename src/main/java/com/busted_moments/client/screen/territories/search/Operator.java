package com.busted_moments.client.screen.territories.search;

import com.busted_moments.client.models.territory.eco.TerritoryEco;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public enum Operator {
   EQUALS("=") {
      @Override
      public <T extends Comparable<T>> Predicate<TerritoryEco> comparing(Function<TerritoryEco, T> extractor, @NotNull T value) {
         return t -> value.equals(extractor.apply(t));
      }
   },
   IS(":") {
      @Override
      public <T extends Comparable<T>> Predicate<TerritoryEco> comparing(Function<TerritoryEco, T> extractor, @NotNull T value) {
         return EQUALS.comparing(extractor, value);
      }
   },
   GREATER_THAN(">") {
      @Override
      public <T extends Comparable<T>> Predicate<TerritoryEco> comparing(Function<TerritoryEco, T> extractor, @NotNull T value) {
         return t -> extractor.apply(t).compareTo(value) > 0;
      }
   },
   GREATER_THAN_OR_EQUALS(">=") {
      @Override
      public <T extends Comparable<T>> Predicate<TerritoryEco> comparing(Function<TerritoryEco, T> extractor, @NotNull T value) {
         return t -> extractor.apply(t).compareTo(value) >= 0;
      }
   },
   LESS_THAN("<") {
      @Override
      public <T extends Comparable<T>> Predicate<TerritoryEco> comparing(Function<TerritoryEco, T> extractor, @NotNull T value) {
         return t -> extractor.apply(t).compareTo(value) < 0;
      }
   },
   LESS_THAN_OR_EQUALS("<=") {
      @Override
      public <T extends Comparable<T>> Predicate<TerritoryEco> comparing(Function<TerritoryEco, T> extractor, @NotNull T value) {
         return t -> extractor.apply(t).compareTo(value) <= 0;
      }
   };

   private final String operator;

   Operator(String operator) {
      this.operator = operator;
   }

   public String asString() {
      return operator;
   }

   public List<String> suggestions() {
      return List.of(operator);
   }

   public abstract <T extends Comparable<T>> Predicate<TerritoryEco> comparing(Function<TerritoryEco, T> extractor, @NotNull T value);

   private static final Map<String, Operator> values;

   static {
      values = new HashMap<>(values().length);

      for (Operator operator : values())
         values.put(operator.operator, operator);
   }

   public static Operator from(String string) {
      return values.get(string);
   }
}
