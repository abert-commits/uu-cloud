package org.uu.common.core.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(value = ElementType.TYPE)
@Retention(value =RetentionPolicy.RUNTIME)
public @interface HandlerAnnotation {
    int offset() default 0;
}
