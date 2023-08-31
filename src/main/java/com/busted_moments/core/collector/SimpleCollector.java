package com.busted_moments.core.collector;

import com.busted_moments.core.annotated.Annotated;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class SimpleCollector<T, A, R> extends Annotated implements java.util.stream.Collector<T, A, R> {
   public SimpleCollector() {
      super(Optional(new com.busted_moments.core.collector.Characteristics() {

         @Override
         public Class<? extends Annotation> annotationType() {
            return com.busted_moments.core.collector.Characteristics.class;
         }

         @Override
         public Characteristics[] value() {
            return new Characteristics[0];
         }
      }));
   }

   protected abstract A supply();

   protected abstract void accumulate(A container, T value);

   protected abstract A combine(A left, A right);

   protected abstract R finish(A container);

   @Override
   public Supplier<A> supplier() {
      return this::supply;
   }

   @Override
   public BiConsumer<A, T> accumulator() {
      return this::accumulate;
   }

   @Override
   public BinaryOperator<A> combiner() {
      return this::combine;
   }

   @Override
   public Function<A, R> finisher() {
      return this::finish;
   }

   @Override
   public Set<Characteristics> characteristics() {
      return Set.of(getAnnotation(com.busted_moments.core.collector.Characteristics.class).value());
   }

   public static <T, A, R> java.util.stream.Collector<T, A, R> of(
           Supplier<A> supplier,
           BiConsumer<A, T> accumulator,
           BinaryOperator<A> combiner,
           Function<A, R> finisher,
           Characteristics...characteristics
   ) {
      return new java.util.stream.Collector<>() {
         @Override
         public Supplier<A> supplier() {
            return supplier;
         }

         @Override
         public BiConsumer<A, T> accumulator() {
            return accumulator;
         }

         @Override
         public BinaryOperator<A> combiner() {
            return combiner;
         }

         @Override
         public Function<A, R> finisher() {
            return finisher;
         }

         @Override
         public Set<Characteristics> characteristics() {
            return Set.of(characteristics);
         }
      };
   }
}