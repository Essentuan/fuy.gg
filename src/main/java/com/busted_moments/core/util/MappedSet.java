package com.busted_moments.core.util;

import java.util.Set;
import java.util.function.Function;

@SuppressWarnings("ALL")
public class MappedSet<U, T> extends MappedCollection<U, T> implements Set<T> {
   public MappedSet(Set<U> set, Function<U, T> mapper) {
      super(set, mapper);
   }
}
