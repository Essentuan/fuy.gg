package com.busted_moments.core.annotated.optional;


import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.annotated.exceptions.ArgumentParameterException;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public class OptionalValidator<T extends Annotation> extends Annotated.Validator<T> {
    private final T defaultValue;

    public OptionalValidator(Class<T> annotation, T defaultValue) {
        super(annotation);

        this.defaultValue = defaultValue;
    }


    @Override
    public void validate(AnnotatedElement element) throws ArgumentParameterException {}

    @Override
    public T getValue(AnnotatedElement element) {
        return element.isAnnotationPresent(getAnnotationClass()) ? element.getAnnotation(getAnnotationClass()) : defaultValue;
    }
}
