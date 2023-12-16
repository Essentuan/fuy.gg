package com.busted_moments.core.annotated;

import com.busted_moments.core.annotated.exceptions.ArgumentParameterException;
import com.busted_moments.core.annotated.optional.OptionalValidator;
import com.busted_moments.core.annotated.placeholder.PlaceholderValidator;
import com.busted_moments.core.annotated.required.RequiredValidator;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class Annotated {
    public abstract static class Validator<T extends Annotation> {
        private final Class<T> annotation;

        public Validator(Class<T> annotation) {
            this.annotation = annotation;
        }

        public Class<T> getAnnotationClass() {
            return annotation;
        }

        public abstract void validate(AnnotatedElement clazz) throws ArgumentParameterException;

        public abstract T getValue(AnnotatedElement clazz);
    }

    private final AnnotatedElement element;

    private final Map<Class<? extends Annotation>, Validator<?>> validators = new HashMap<>();

    public Annotated() {
        this(new Validator<?>[0]);
    }

    public Annotated(Validator<?>... annotations) {
        this.element = getClass();

        register(annotations);
    }

    public Annotated(Validator<?>[]... annotations) {
        this.element = getClass();

        register(Stream.of(annotations).flatMap(Stream::of).toArray(Validator[]::new));
    }

    public Annotated(AnnotatedElement element, Validator<?>[]... annotations) {
        this(element, Stream.of(annotations).flatMap(Stream::of).toArray(Validator[]::new));
    }

    public Annotated(AnnotatedElement element, Validator<?>... annotations) {
        this.element = element;

        register(annotations);
    }


    private void register(Validator<?>... annotations) {
        for (Validator<?> argumentParameter : annotations) {
            argumentParameter.validate(element);

            validators.put(argumentParameter.getAnnotationClass(), argumentParameter);
        }
    }

    protected void addAnnotation(Validator<?>... annotations) {
        register(annotations);
    }

    public <R extends Annotation> boolean hasAnnotation(Class<R> annotation) {
        return element.isAnnotationPresent(annotation);
    }

    @SuppressWarnings("unchecked")
    public <R extends Annotation> R getAnnotation(Class<R> annotation) {
        return (R) validators.get(annotation).getValue(element);
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation, R> R getAnnotation(Class<A> annotation, Function<A, R> property) {
        return property.apply((A) validators.get(annotation).getValue(element));
    }

    public static <T extends Annotation> Validator<T> Required(Class<T> clazz) {
        return new RequiredValidator<>(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> Validator<T> Optional(T defaultValue) {
        return new OptionalValidator<>((Class<T>) defaultValue.annotationType(), defaultValue);
    }

    public static <T extends Annotation> Validator<T> Placeholder(Class<T> clazz) {
        return new PlaceholderValidator<>(clazz);
    }
}
