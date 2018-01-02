package org.mermaid.vertxmvc.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestHeader {
    String value() default "";
    boolean required() default true;
    String defaultValue() default ValueConstants.DEFAULT_NONE;
}
