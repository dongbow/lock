package com.github.lock.way;

import com.github.lock.support.redis.RedisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis实现分布式锁
 *
 * @author wangdongbo
 * @since 2019/7/22.
 */
@Slf4j
@Component
public class RedisLock implements DistributedLock {

    @Autowired
    private RedisTool redisTool;

    @Override
    public boolean lock(String lockKey, String requestId, int seconds) {
        return redisTool.set(lockKey, requestId, seconds);
    }

    @Override
    public boolean tryLock(String lockKey, String requestId, int seconds, int trySeconds) {
        if (lock(lockKey, requestId, seconds)) {
            return true;
        }
        try {
            TimeUnit.SECONDS.sleep(trySeconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return lock(lockKey, requestId, seconds);
    }

    @Override
    public boolean unlock(String lockKey, String requestId) {
        return redisTool.delete(lockKey, requestId);
    }
}
