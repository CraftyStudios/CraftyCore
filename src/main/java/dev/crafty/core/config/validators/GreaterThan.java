package dev.crafty.core.config.validators;

import dev.crafty.core.config.annotation.Validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Validator(GreaterThan.GreaterThanValidator.class)
public @interface GreaterThan {
    double number();

    class GreaterThanValidator implements Validator.ValueValidator<GreaterThan> {

        @Override
        public boolean validate(Object value, GreaterThan annotation) {
            if (value instanceof Number number) {
                double num = number.doubleValue();
                return num > annotation.number();
            }

            return false;
        }
    }
}
