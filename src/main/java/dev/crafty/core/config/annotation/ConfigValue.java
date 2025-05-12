package dev.crafty.core.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigValue {
    String path();
    String file() default "";
    boolean expressionable() default false;
    boolean colorize() default false;
    boolean optional() default false;
}
