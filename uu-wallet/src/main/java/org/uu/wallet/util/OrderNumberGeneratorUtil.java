package org.uu.wallet.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 生成订单号 工具类  前缀 + 年月日时分秒 + 5位自增数
 * @author Simon
 * @date 2024/01/12
 */
@Component
public class OrderNumberGeneratorUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public String generateOrderNo(String prefix) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = fmt.format(LocalDateTime.now());

        String sequenceStr = getAndUpdateSequence();
        return prefix + timestamp + sequenceStr;
    }

    private String getAndUpdateSequence() {
        String key = "order_sequence_key";
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        // 原子地增加key的值并返回新值
        Long increment = ops.increment(key, 1);

        // 如果达到9999，则重置为0
        if (increment >= 9999) {
            ops.set(key, "0");
            return String.format("%05d", 0);
        }

        return String.format("%05d", increment);
    }
}
