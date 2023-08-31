package com.busted_moments.core.annotated.required;

import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.annotated.exceptions.ArgumentParameterException;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public class RequiredValidator<T extends Annotation> extends Annotated.Validator<T> {
    public RequiredValidator(Class<T> annotation) {
        super(annotation);
    }

    @Override
    public void validate(AnnotatedElement element) throws ArgumentParameterException {
        if (!element.isAnnotationPresent(getAnnotationClass())) {
            throw new ArgumentParameterException("Annotated class %s is missing required annotation %s"
                    .formatted(element, getAnnotationClass().getSimpleName()
            ));
        }
    }

    @Override
    public T getValue(AnnotatedElement element) {
        return element.getAnnotation(getAnnotationClass());
    }
}
