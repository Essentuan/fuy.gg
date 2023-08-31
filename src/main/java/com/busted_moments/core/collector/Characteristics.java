package com.busted_moments.core.collector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Collector;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Characteristics {
   Collector.Characteristics[] value();
}
