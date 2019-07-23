package com.github.lock.resolver;

import com.github.lock.annotation.Lock;
import com.github.lock.exception.LockException;
import com.github.lock.util.UUIDUtil;
import com.github.lock.way.DistributedLock;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author wangdongbo
 * @since 2019/7/22.
 */
@Slf4j
@Aspect
@Order(1)
@Component
public class LockResolver {

    private static final String DOT = ".";
    private static final String DOT_SPLIT = "\\.";

    @Autowired
    private DistributedLock distributedLock;

    private static LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    @Pointcut("@annotation(com.github.lock.annotation.Lock)")
    public void lockAspectMethod() { }

    @Around("lockAspectMethod()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        if (method == null) {
            throw new LockException("method is empty");
        }
        Lock lock = method.getAnnotation(Lock.class);
        String prefix;
        if (!StringUtils.isEmpty(lock.prefix())) {
            prefix = lock.prefix();
        } else {
            prefix = joinPoint.getTarget().getClass().getSimpleName() + "_" + method.getName() + "_";
        }
        String lockKey = prefix + getArgsConcat(method, lock.parameter(), joinPoint.getArgs());
        String requestId = UUIDUtil.getID();
        try {
            if (lock.retry()) {
                int count = lock.retryCount();
                for (int i = 0; i < count; i++) {
                    if (distributedLock.tryLock(lockKey, requestId, lock.lockSeconds(), lock.tryLockSeconds())) {
                        return joinPoint.proceed();
                    }
                }
            } else if (distributedLock.lock(lockKey, requestId, lock.lockSeconds())) {
                return joinPoint.proceed();
            }
            throw new LockException(lock.desc());
        } catch (Throwable e) {
            log.warn("add lock error:{}", e.getMessage(), e);
            throw new LockException(e.getMessage(), e);
        } finally {
            distributedLock.unlock(lockKey, requestId);
        }
    }

    private String getArgsConcat(Method method, String[] parameter, Object[] args) {
        String[] params = parameterNameDiscoverer.getParameterNames(method);
        if (ArrayUtils.isEmpty(parameter)) {
            return "";
        }
        List<Object> list = Lists.newArrayListWithCapacity(parameter.length);
        Stream.of(parameter).forEach(s -> {
            if (s.contains(DOT)) {
                list.add(parseObj(s, params, args));
            } else {
                int idx = ArrayUtils.indexOf(params, s);
                list.add(args[idx]);
            }
        });
        return Joiner.on("_").join(list);
    }

    private Object parseObj(String parameter, String[] params, Object[] args) {
        String firstField = parameter.split(DOT_SPLIT)[0];
        int idx = ArrayUtils.indexOf(params, firstField);
        Object value = args[idx];
        Class valueClass = value.getClass();
        Object[] fields = ArrayUtils.remove(parameter.split(DOT_SPLIT), 0);
        for (Object field : fields) {
            try {
                Field nextField = valueClass.getDeclaredField(String.valueOf(field));
                nextField.setAccessible(true);
                value = nextField.get(value);
                valueClass = value.getClass();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }
        return value;
    }

}
