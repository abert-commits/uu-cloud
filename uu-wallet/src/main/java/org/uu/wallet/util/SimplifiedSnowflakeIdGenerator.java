package org.uu.wallet.util;

/**
 * 基于时间戳的简化版雪花算法
 * 依赖于时间戳和一个内部序列号 (4096位)
 *
 * @author Simon
 * @date 2023/12/29
 */
public class SimplifiedSnowflakeIdGenerator {

    private final long twepoch = 1288834974657L;
    private final long sequenceBits = 12L;
    private final long timestampLeftShift = sequenceBits;

    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & ~(-1L << sequenceBits);
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;
        return ((timestamp - twepoch) << timestampLeftShift) | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}
