package org.mermaid.vertxmvc.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CookieValue {
    String value() default "";
    String defaultValue() default ValueConstants.DEFAULT_NONE;
}
