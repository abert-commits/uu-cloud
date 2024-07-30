package org.uu.wallet.annotation;

import org.uu.wallet.Enum.MemberOperationModuleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogMemberOperation {
    MemberOperationModuleEnum value();
}
