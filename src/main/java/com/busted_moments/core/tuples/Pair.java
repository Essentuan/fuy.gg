package com.busted_moments.core.tuples;

import org.apache.logging.log4j.util.BiConsumer;

public record Pair<One, Two>(One one, Two two) {
   public void then(BiConsumer<One, Two> consumer) {
      consumer.accept(one, two);
   }

   public static <One, Two> Pair<One, Two> of(One first, Two second) {
      return new Pair<>(first, second);
   }
}
