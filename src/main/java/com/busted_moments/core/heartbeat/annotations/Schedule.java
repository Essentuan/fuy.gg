package com.busted_moments.core.heartbeat.annotations;

import com.busted_moments.core.time.ChronoUnit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Schedule {
    int rate();
    ChronoUnit unit();

    boolean parallel() default false;
}