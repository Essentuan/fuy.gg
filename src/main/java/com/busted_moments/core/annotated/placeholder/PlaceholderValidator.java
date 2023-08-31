package com.busted_moments.core.annotated.placeholder;

import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.annotated.exceptions.ArgumentParameterException;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public class PlaceholderValidator<T extends Annotation> extends Annotated.Validator<T> {
    public PlaceholderValidator(Class<T> annotation) {
        super(annotation);
    }

    @Override
    public void validate(AnnotatedElement element) throws ArgumentParameterException {}

    @Override
    public T getValue(AnnotatedElement element) {
        return element.getAnnotation(getAnnotationClass());
    }
}
