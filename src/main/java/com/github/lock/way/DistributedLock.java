package com.github.lock.way;

/**
 * @author wangdongbo
 * @since 2019/7/22.
 */
public interface DistributedLock {

    /**
     * 加锁
     *
     * @param lockKey    key
     * @param requestId  唯一请求ID
     * @param seconds    锁定时间[秒]
     * @return 是否加锁成功
     */
    boolean lock(String lockKey, String requestId, int seconds);

    /**
     * 尝试加锁
     *
     * @param lockKey     key
     * @param requestId   唯一请求ID
     * @param seconds     锁定时间[秒]
     * @param trySeconds  拿不到锁,等待多久之后再次尝试加锁[秒]
     * @return  是否加锁成功
     */
    boolean tryLock(String lockKey, String requestId, int seconds, int trySeconds);

    /**
     * 释放锁
     *
     * @param lockKey    key
     * @param requestId  唯一请求ID
     * @return  是否成功释放锁
     */
    boolean unlock(String lockKey, String requestId);

}
