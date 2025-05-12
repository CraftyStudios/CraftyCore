package dev.crafty.core.config.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Validator {
    Class<? extends ValueValidator<?>> value();

    interface ValueValidator<T extends Annotation> {
        boolean validate(Object value, T annotation);
    }
}
