package com.kewen.framework.basic.utils;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.Enumeration;

/**
 * 雪花算法ID生成器
 * <p>
 * 分布式ID生成算法，生成的ID结构：
 * - 1位符号位（固定为0）
 * - 41位时间戳（毫秒级，可用约69年）
 * - 10位机器ID（支持1024个节点）
 * - 12位序列号（每毫秒最多生成4096个ID）
 * </p>
 *
 * <p>时钟回拨处理策略（可通过构造函数选择）：</p>
 * <ul>
 *   <li>THROW_EXCEPTION：直接抛出异常（默认，最安全）</li>
 *   <li>WAIT：等待时钟追上（适用于小范围回拨）</li>
 *   <li>EXTEND_SEQUENCE：扩展序列号容忍回拨（牺牲部分有序性）</li>
 * </ul>
 *
 * @author kewen
 * @since 2024-01-01
 */
public class SnowflakeIdGenerator {
    
    /**
     * 时钟回拨处理策略
     */
    public enum ClockBackwardStrategy {
        /**
         * 直接抛出异常（默认策略，最安全）
         */
        THROW_EXCEPTION,
        
        /**
         * 等待时钟追上（适用于小范围回拨，最大等待时间由maxBackwardMs决定）
         */
        WAIT,
        
        /**
         * 扩展序列号容忍回拨（在回拨期间使用额外的序列号位，牺牲部分有序性换取可用性）
         */
        EXTEND_SEQUENCE
    }
    
    // ==================== 常量定义 ====================
    
    /**
     * 起始时间戳 (2026-01-01 00:00:00 UTC)
     * 可根据项目实际需要调整，但一旦使用后不应修改
     * 
     * 计算说明：
     * - 41位时间戳最大可表示约69.7年（从起始时间算起）
     * - 使用2026年作为起始，可使用到约2095年
     */
    private static final long START_TIMESTAMP = 1767225600000L;
    
    /**
     * 各部分位数
     */
    private static final long MACHINE_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    
    /**
     * 最大值计算（使用位运算生成指定位数的最大值）
     * 
     * 计算原理（以 MACHINE_ID_BITS = 10 为例）：
     * 
     * 步骤1：-1L 的二进制表示
     *   11111111 11111111 11111111 11111111 11111111 11111111 11111111 11111111
     *   （64个1）
     * 
     * 步骤2：(-1L << 10) 左移10位
     *   11111111 11111111 11111111 11111111 11111111 11111111 11111100 00000000
     *   ↑ 高54位保持为1              ↑ 低10位变为0
     * 
     * 步骤3：~(-1L << 10) 按位取反
     *   00000000 00000000 00000000 00000000 00000000 00000000 00000011 11111111
     *   ↑ 高54位变为0              ↑ 低10位变为1
     * 
     * 步骤4：结果转换
     *   二进制 1111111111 = 十进制 1023 = 2^10 - 1
     * 
     * 等价写法（推荐第一种）：
     *   ✓ ~(-1L << n)         按位取反，简洁直观
     *   ✓ -1L ^ (-1L << n)    异或运算，利用 1^x=~x 的性质
     * 
     * 通用公式：~(-1L << n) = 2^n - 1
     * 用途：生成 n 位二进制数能表示的最大值
     */
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);  // 10位最大值 = 1023 (2^10 - 1)
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);      // 12位最大值 = 4095 (2^12 - 1)
    
    /**
     * 位移量
     */
    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;
    
    /**
     * 扩展序列号位数（用于EXTEND_SEQUENCE策略）
     * 
     * 设计思路：
     * - 原始序列号：12位，最大值4095
     * - 扩展后序列号：16位（借用4位），最大值65535
     * - 用途：在时钟回拨时使用扩展序列号，提供更大的容错空间
     * 
     * 计算过程：
     *   EXTENDED_SEQUENCE_BITS = 12 + 4 = 16
     *   MAX_EXTENDED_SEQUENCE = ~(-1L << 16) = 2^16 - 1 = 65535
     */
    private static final long EXTENDED_SEQUENCE_BITS = SEQUENCE_BITS + 4;
    private static final long MAX_EXTENDED_SEQUENCE = ~(-1L << EXTENDED_SEQUENCE_BITS);  // 16位最大值 = 65535 (2^16 - 1)
    
    // ==================== 实例字段 ====================
    
    /**
     * 机器ID（0-1023）
     */
    private final long machineId;
    
    /**
     * 序列号（0-4095）
     */
    private long sequence = 0L;
    
    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;
    
    /**
     * 扩展序列号（仅在EXTEND_SEQUENCE策略下使用）
     */
    private long extendedSequence = 0L;
    
    /**
     * 时钟回拨处理策略
     */
    private final ClockBackwardStrategy strategy;
    
    /**
     * 最大允许回拨时间（毫秒）
     */
    private final long maxBackwardMs;
    
    // ==================== 构造函数 ====================
    
    /**
     * 使用默认策略（THROW_EXCEPTION）创建生成器
     * 
     * @param machineId 机器ID（0-1023）
     */
    public SnowflakeIdGenerator(long machineId) {
        this(machineId, ClockBackwardStrategy.THROW_EXCEPTION, 10L);
    }
    
    /**
     * 指定策略创建生成器
     * 
     * @param machineId 机器ID（0-1023）
     * @param strategy 时钟回拨处理策略
     */
    public SnowflakeIdGenerator(long machineId, ClockBackwardStrategy strategy) {
        this(machineId, strategy, 10L);
    }
    
    /**
     * 完整参数构造函数
     * 
     * @param machineId 机器ID（0-1023）
     * @param strategy 时钟回拨处理策略
     * @param maxBackwardMs 最大允许回拨时间（毫秒），仅在WAIT策略下有效
     */
    public SnowflakeIdGenerator(long machineId, ClockBackwardStrategy strategy, long maxBackwardMs) {
        if (machineId > MAX_MACHINE_ID || machineId < 0) {
            throw new IllegalArgumentException(
                String.format("Machine ID must be between 0 and %d, but got %d", MAX_MACHINE_ID, machineId));
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        if (maxBackwardMs <= 0) {
            throw new IllegalArgumentException("Max backward milliseconds must be positive");
        }
        
        this.machineId = machineId;
        this.strategy = strategy;
        this.maxBackwardMs = maxBackwardMs;
    }
    
    // ==================== 核心方法 ====================
    
    /**
     * 生成下一个雪花ID
     * 
     * @return 唯一的long型ID
     */
    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();
        
        // 处理时钟回拨
        if (currentTimestamp < lastTimestamp) {
            handleClockBackward(currentTimestamp);
            currentTimestamp = System.currentTimeMillis();
        }
        
        // 处理同一毫秒内的序列号
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // 当前毫秒序列号用完，等待下一毫秒
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置
            sequence = 0L;
            extendedSequence = 0L;
        }
        
        lastTimestamp = currentTimestamp;
        
        // 组装ID：时间戳 | 机器ID | 序列号
        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (machineId << MACHINE_ID_SHIFT)
                | sequence;
    }
    
    // ==================== 时钟回拨处理方法 ====================
    
    /**
     * 处理时钟回拨
     * 
     * @param currentTimestamp 当前时间戳
     */
    private void handleClockBackward(long currentTimestamp) {
        long offset = lastTimestamp - currentTimestamp;
        
        switch (strategy) {
            case THROW_EXCEPTION:
                // 直接抛出异常
                throw new RuntimeException(
                    String.format("Clock moved backwards by %d ms, refusing to generate id", offset));
                    
            case WAIT:
                // 等待策略：检查回拨是否在可接受范围内
                if (offset > maxBackwardMs) {
                    throw new RuntimeException(
                        String.format("Clock moved backwards too much: %d ms (max allowed: %d ms)", 
                            offset, maxBackwardMs));
                }
                // 等待直到时钟追上
                waitUntilClockCatchesUp();
                break;
                
            case EXTEND_SEQUENCE:
                // 扩展序列号策略：在回拨期间使用扩展序列号
                handleClockBackwardWithExtendedSequence(offset);
                break;
                
            default:
                throw new IllegalStateException("Unknown strategy: " + strategy);
        }
    }
    
    /**
     * 等待策略：等待系统时钟追上lastTimestamp
     */
    private void waitUntilClockCatchesUp() {
        long waitStart = System.currentTimeMillis();
        try {
            while (System.currentTimeMillis() < lastTimestamp) {
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for clock to catch up", e);
        }
        
        long waitEnd = System.currentTimeMillis();
        long actualWaitMs = waitEnd - waitStart;
        
        // 记录日志（实际项目中应使用日志框架）
        if (actualWaitMs > 0) {
            System.out.printf("[SnowflakeIdGenerator] Waited %d ms for clock to catch up%n", actualWaitMs);
        }
    }
    
    /**
     * 扩展序列号策略：处理时钟回拨
     * 
     * @param offset 回拨偏移量（毫秒）
     */
    private void handleClockBackwardWithExtendedSequence(long offset) {
        // 增加扩展序列号
        extendedSequence = (extendedSequence + offset) & MAX_EXTENDED_SEQUENCE;
        
        if (extendedSequence == 0) {
            // 扩展序列号也用完了，只能等待
            throw new RuntimeException(
                String.format("Extended sequence exhausted after %d ms clock backward", offset));
        }
        
        // 记录日志
        System.out.printf("[SnowflakeIdGenerator] Using extended sequence: %d to handle %d ms backward%n", 
            extendedSequence, offset);
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 等待下一毫秒
     * 
     * @param lastTimestamp 上次时间戳
     * @return 新的时间戳
     */
    private long waitNextMillis(long lastTimestamp) {
        long currentTimestamp = System.currentTimeMillis();
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = System.currentTimeMillis();
        }
        return currentTimestamp;
    }
    
    // ==================== 静态工具方法 ====================
    
    /**
     * 根据MAC地址自动生成机器ID
     * 
     * @return 机器ID（0-1023）
     */
    public static long generateMachineIdFromMac() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null && mac.length > 0) {
                    // 使用MAC地址的哈希值生成机器ID
                    int hash = 0;
                    for (byte b : mac) {
                        hash = (hash << 5) - hash + (b & 0xFF);
                    }
                    return Math.abs(hash) % (MAX_MACHINE_ID + 1);
                }
            }
        } catch (Exception e) {
            // 忽略异常，使用随机值
        }
        
        // 如果无法获取MAC地址，使用随机值
        return new SecureRandom().nextInt((int) (MAX_MACHINE_ID + 1));
    }
    
    // ==================== Getter方法 ====================
    
    /**
     * 获取机器ID
     */
    public long getMachineId() {
        return machineId;
    }
    
    /**
     * 获取时钟回拨处理策略
     */
    public ClockBackwardStrategy getStrategy() {
        return strategy;
    }
    
    /**
     * 获取最大允许回拨时间
     */
    public long getMaxBackwardMs() {
        return maxBackwardMs;
    }
}
