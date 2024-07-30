package org.uu.manager.annotation;



import org.springframework.data.annotation.Reference;

import java.lang.annotation.*;

@Documented
@Target({ElementType.PARAMETER,ElementType.METHOD})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface SysLog {
    String title() default  "";
    String content() default "";

}
