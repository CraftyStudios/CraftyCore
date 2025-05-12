package dev.crafty.core.config.validators;

import dev.crafty.core.config.annotation.Validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Validator(MultipleOf.MultipleOfValidator.class)
public @interface MultipleOf {
    int multiple();

    class MultipleOfValidator implements Validator.ValueValidator<MultipleOf> {

        @Override
        public boolean validate(Object value, MultipleOf annotation) {
            if (value instanceof Number number) {
                double num = number.intValue();
                return num % annotation.multiple() == 0;
            }

            return false;
        }
    }
}
