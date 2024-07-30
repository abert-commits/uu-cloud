package org.uu.common.redis.util;


import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
@Component("redissonUtil")
public class RedissonUtil {


    @Resource
    private RedissonClient redissonClient;

    public RLock getLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);

        return lock;
    }

    public RLock lock(String lockKey, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime, TimeUnit.SECONDS);
        return lock;
    }

    public RLock lock(String lockKey,TimeUnit unit, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime, unit);
        return lock;
    }

    public boolean tryLock(String lockKey,TimeUnit unit,long waitTime,long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void unlock(String lockKey){
        RLock lock = redissonClient.getLock(lockKey);
        lock.unlock();
    }

    public void unlock(RLock lock){
        lock.unlock();
    }
}






