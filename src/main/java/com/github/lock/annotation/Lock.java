package com.github.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wangdongbo
 * @since 2019/7/22.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Lock {

    /**
     * 锁前缀, 为空时取'classname_method_'
     */
    String prefix() default "";

    /**
     * 方法参数名，支持方法中的入参，支持方法参数是对象时对象属性的字段
     * 不支持集合/数组参数
     */
    String[] parameter() default {};

    /**
     * 锁定时间
     */
    int lockSeconds() default 10;

    /**
     * 是否重试，默认重试
     */
    boolean retry() default true;

    /**
     * 重试次数，默认为1，retry为true时可用
     */
    int retryCount() default 1;

    /**
     * 没拿到锁时等待多久后重试，retry为true时可用
     */
    int tryLockSeconds() default 10;

    /**
     * 提示
     */
    String desc() default "数据正在操作中，请稍后再试";

}
