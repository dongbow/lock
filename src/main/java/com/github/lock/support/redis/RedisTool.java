package com.github.lock.support.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.Collections;
import java.util.Objects;

/**
 * @author wangdongbo
 * @since 2019/7/22.
 */
@Slf4j
@Component
public class RedisTool {

    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "EX";
    private static final Long RELEASE_SUCCESS = 1L;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean set(final String key, final String requestId, final int expireTime) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(requestId) || expireTime <= 0) {
            return false;
        }
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            Object nativeConnection = connection.getNativeConnection();
            try {
                String result = null;
                if (nativeConnection instanceof JedisCluster) {
                    // 集群模式
                    result = ((JedisCluster) nativeConnection).set(key, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
                } else if (nativeConnection instanceof Jedis) {
                    // 单机模式
                    result = ((Jedis) nativeConnection).set(key, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
                }
                return Objects.equals(LOCK_SUCCESS, result);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return false;
            }
        });
    }

    public boolean delete(final String key, final String requestId) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(requestId)) {
            return false;
        }
        final String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            Object nativeConnection = connection.getNativeConnection();
            try {
                Object result = null;
                if (nativeConnection instanceof JedisCluster) {
                    // 集群模式
                    result = ((JedisCluster) nativeConnection).eval(script, Collections.singletonList(key), Collections.singletonList(requestId));
                } else if (nativeConnection instanceof Jedis) {
                    // 单机模式
                    result = ((Jedis) nativeConnection).eval(script, Collections.singletonList(key), Collections.singletonList(requestId));
                }
                return Objects.equals(RELEASE_SUCCESS, result);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return false;
            }
        });
    }

}
