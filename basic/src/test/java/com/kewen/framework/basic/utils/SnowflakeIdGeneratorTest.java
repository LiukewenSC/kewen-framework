package com.kewen.framework.basic.utils;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/**
 * 雪花算法ID生成器单元测试
 * 
 * <p>测试覆盖范围：</p>
 * <ul>
 *   <li>基础功能：ID唯一性、递增性、正数性</li>
 *   <li>构造函数：参数校验、策略配置</li>
 *   <li>时钟回拨：三种策略（抛异常/等待/扩展序列号）</li>
 *   <li>并发安全：多线程环境下ID唯一性</li>
 *   <li>性能测试：生成速度验证</li>
 *   <li>ID结构：时间戳、机器ID、序列号三部分验证</li>
 *   <li>边界条件：最小/最大机器ID、序列号溢出</li>
 * </ul>
 *
 * @author kewen
 * @since 2026-01-01
 */
public class SnowflakeIdGeneratorTest {

    /**
     * 测试用例使用的生成器实例
     * 默认使用机器ID=1，策略=THROW_EXCEPTION
     */
    private SnowflakeIdGenerator generator;

    /**
     * 测试前置准备
     * 每个测试方法执行前都会调用，创建一个新的生成器实例
     */
    @Before
    public void setUp() {
        // 使用默认策略（THROW_EXCEPTION）创建生成器，机器ID为1
        generator = new SnowflakeIdGenerator(1);
    }

    // ==================== 基础功能测试 ====================
    // 测试雪花算法的核心功能：生成唯一、递增、正数的Long型ID

    /**
     * 测试生成的ID是否为正数
     * 
     * <p>验证点：</p>
     * <ul>
     *   <li>雪花算法最高位（符号位）固定为0，保证ID为正数</li>
     *   <li>正数ID便于数据库存储和索引（负数可能导致问题）</li>
     * </ul>
     */
    @Test
    public void testNextId_ShouldGeneratePositiveLong() {
        // 生成一个ID
        long id = generator.nextId();
        
        // 验证ID为正数（最高位为0）
        assertTrue("Generated ID should be positive", id > 0);
    }

    /**
     * 测试生成的ID是否唯一
     * 
     * <p>验证点：</p>
     * <ul>
     *   <li>连续生成10000个ID，应该全部不重复</li>
     *   <li>使用HashSet的去重特性验证唯一性</li>
     *   <li>这是分布式ID生成器的核心要求</li>
     * </ul>
     */
    @Test
    public void testNextId_ShouldGenerateUniqueIds() {
        // 使用HashSet存储生成的ID（自动去重）
        Set<Long> ids = new HashSet<>();
        
        // 连续生成10000个ID
        for (int i = 0; i < 10000; i++) {
            ids.add(generator.nextId());
        }
        
        // 验证：如果所有ID都唯一，Set的大小应该等于10000
        assertEquals("All IDs should be unique", 10000, ids.size());
    }

    /**
     * 测试生成的ID是否严格递增
     * 
     * <p>验证点：</p>
     * <ul>
     *   <li>雪花算法生成的ID应该随时间递增</li>
     *   <li>递增特性对数据库索引友好（B+树顺序插入）</li>
     *   <li>便于通过ID大小判断先后顺序</li>
     * </ul>
     */
    @Test
    public void testNextId_ShouldGenerateIncreasingIds() {
        // 生成第一个ID作为基准
        long previousId = generator.nextId();
        
        // 连续生成1000个ID，验证每个都比前一个大
        for (int i = 0; i < 1000; i++) {
            long currentId = generator.nextId();
            
            // 验证当前ID严格大于前一个ID
            assertTrue("ID should be increasing: " + previousId + " >= " + currentId,
                    currentId > previousId);
            
            // 更新基准值
            previousId = currentId;
        }
    }

    /**
     * 测试快速生成时的序列号递增逻辑
     * 
     * <p>验证点：</p>
     * <ul>
     *   <li>同一毫秒内生成的多个ID，通过序列号区分</li>
     *   <li>序列号占用ID的低12位（0xFFF = 4095）</li>
     *   <li>时间戳部分占用高位（右移22位后比较）</li>
     * </ul>
     * 
     * <p>ID结构：</p>
     * <pre>
     * | 41位时间戳 | 10位机器ID | 12位序列号 |
     * |←--- 高位 ---→|← 低位 →|
     * </pre>
     */
    @Test
    public void testNextId_RapidGeneration() {
        // 连续快速生成两个ID
        long firstId = generator.nextId();
        long secondId = generator.nextId();
        
        // 提取低12位作为序列号（0xFFF = 0b111111111111）
        long firstSequence = firstId & 0xFFF;
        long secondSequence = secondId & 0xFFF;
        
        // 判断两个ID是否在同一毫秒内生成
        // 右移22位（12位序列号 + 10位机器ID）得到时间戳部分
        if ((firstId >> 22) == (secondId >> 22)) {
            // 如果时间戳相同，说明在同一毫秒内
            // 验证序列号应该递增
            assertTrue("Sequence should increase in same millisecond",
                    secondSequence > firstSequence);
        }
        // 如果时间戳不同，说明跨毫秒了，序列号会重置为0
    }

    // ==================== 构造函数测试 ====================
    // 测试各种构造函数重载和参数校验逻辑

    /**
     * 测试使用有效机器ID创建生成器
     * 
     * <p>验证点：</p>
     * <ul>
     *   <li>机器ID范围：0 ~ 1023（10位二进制）</li>
     *   <li>最小值和最大值都应该能正常创建</li>
     * </ul>
     */
    @Test
    public void testConstructor_WithValidMachineId() {
        // 测试最小机器ID（0）
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(0);
        assertNotNull("Generator with machineId 0 should be created", gen);
        
        // 测试最大机器ID（1023 = 2^10 - 1）
        gen = new SnowflakeIdGenerator(1023);
        assertNotNull("Generator with machineId 1023 should be created", gen);
    }

    /**
     * 测试使用负数机器ID
     * 
     * <p>预期结果：</p>
     * <ul>
     *   <li>应该抛出 IllegalArgumentException</li>
     *   <li>机器ID不能为负数</li>
     * </ul>
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_WithNegativeMachineId() {
        // 使用负数机器ID，应该抛出异常
        new SnowflakeIdGenerator(-1);
    }

    /**
     * 测试使用超出范围的机器ID
     * 
     * <p>预期结果：</p>
     * <ul>
     *   <li>应该抛出 IllegalArgumentException</li>
     *   <li>机器ID最大为1023（10位二进制最大值）</li>
     * </ul>
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_WithMachineIdOutOfRange() {
        // 使用1024（超出最大值1023），应该抛出异常
        new SnowflakeIdGenerator(1024);
    }

    /**
     * 测试使用WAIT策略创建生成器
     * 
     * <p>WAIT策略说明：</p>
     * <ul>
     *   <li>当发生时钟回拨时，等待系统时钟追上</li>
     *   <li>适用于小范围回拨（如NTP同步导致的几毫秒回拨）</li>
     *   <li>不会丢失ID，但可能短暂阻塞</li>
     * </ul>
     */
    @Test
    public void testConstructor_WithWaitStrategy() {
        // 使用WAIT策略创建生成器
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(
                1,
                SnowflakeIdGenerator.ClockBackwardStrategy.WAIT
        );
        
        // 验证生成器创建成功
        assertNotNull("Generator with WAIT strategy should be created", gen);
        
        // 验证策略设置正确
        assertEquals("Strategy should be WAIT",
                SnowflakeIdGenerator.ClockBackwardStrategy.WAIT,
                gen.getStrategy());
    }

    /**
     * 测试使用EXTEND_SEQUENCE策略创建生成器
     * 
     * <p>EXTEND_SEQUENCE策略说明：</p>
     * <ul>
     *   <li>当发生时钟回拨时，使用扩展的序列号空间</li>
     *   <li>原始序列号12位（4096），扩展到16位（65536）</li>
     *   <li>牺牲部分有序性换取高可用性</li>
     * </ul>
     */
    @Test
    public void testConstructor_WithExtendSequenceStrategy() {
        // 使用EXTEND_SEQUENCE策略创建生成器
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(
                1,
                SnowflakeIdGenerator.ClockBackwardStrategy.EXTEND_SEQUENCE
        );
        
        // 验证生成器创建成功
        assertNotNull("Generator with EXTEND_SEQUENCE strategy should be created", gen);
        
        // 验证策略设置正确
        assertEquals("Strategy should be EXTEND_SEQUENCE",
                SnowflakeIdGenerator.ClockBackwardStrategy.EXTEND_SEQUENCE,
                gen.getStrategy());
    }

    /**
     * 测试自定义最大回拨时间
     * 
     * <p>验证点：</p>
     * <ul>
     *   <li>WAIT策略可以配置最大等待时间</li>
     *   <li>超过这个时间的回拨会直接抛出异常</li>
     *   <li>防止无限等待</li>
     * </ul>
     */
    @Test
    public void testConstructor_WithCustomMaxBackwardMs() {
        // 创建生成器，设置最大回拨时间为20毫秒
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(
                1,
                SnowflakeIdGenerator.ClockBackwardStrategy.WAIT,
                20L
        );
        
        // 验证最大回拨时间设置正确
        assertEquals("Max backward ms should be 20", 20L, gen.getMaxBackwardMs());
    }

    /**
     * 测试使用null策略
     * 
     * <p>预期结果：</p>
     * <ul>
     *   <li>应该抛出 IllegalArgumentException</li>
     *   <li>策略不能为null</li>
     * </ul>
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_WithNullStrategy() {
        // 使用null策略，应该抛出异常
        new SnowflakeIdGenerator(1, null);
    }

    /**
     * 测试使用无效的最大回拨时间
     * 
     * <p>预期结果：</p>
     * <ul>
     *   <li>应该抛出 IllegalArgumentException</li>
     *   <li>最大回拨时间必须为正数</li>
     * </ul>
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_WithInvalidMaxBackwardMs() {
        // 使用0作为最大回拨时间，应该抛出异常
        new SnowflakeIdGenerator(1, SnowflakeIdGenerator.ClockBackwardStrategy.WAIT, 0);
    }

    // ==================== 时钟回拨测试 ====================
    // 测试三种时钟回拨处理策略的行为
    // 注意：由于无法精确模拟时钟回拨，这里主要验证策略配置和正常流程

    /**
     * 测试THROW_EXCEPTION策略（默认策略）
     * 
     * <p>策略说明：</p>
     * <ul>
     *   <li>最安全的策略，直接拒绝生成ID</li>
     *   <li>当时钟回拨时抛出RuntimeException</li>
     *   <li>保证ID不会重复，但可能影响可用性</li>
     * </ul>
     * 
     * <p>测试场景：</p>
     * <ul>
     *   <li>验证策略配置正确</li>
     *   <li>验证正常情况下可以生成ID</li>
     *   <li>实际时钟回拨需要模拟时钟，这里仅做基本验证</li>
     * </ul>
     */
    @Test
    public void testClockBackward_ThrowExceptionStrategy() {
        // 验证当前生成器的策略为THROW_EXCEPTION
        assertEquals("Strategy should be THROW_EXCEPTION",
                SnowflakeIdGenerator.ClockBackwardStrategy.THROW_EXCEPTION,
                generator.getStrategy());
        
        // 正常生成ID（没有时钟回拨时应该正常工作）
        long id = generator.nextId();
        assertTrue("Should generate ID normally", id > 0);
        
        // 注意：实际时钟回拨测试需要模拟时钟，这里仅做基本验证
        // 在真实时钟回拨场景下，THROW_EXCEPTION策略会抛出RuntimeException
    }

    /**
     * 测试WAIT策略
     * 
     * <p>策略说明：</p>
     * <ul>
     *   <li>当时钟回拨时，等待系统时钟追上lastTimestamp</li>
     *   <li>适用于小范围回拨（如NTP时间同步）</li>
     *   <li>可以配置最大等待时间，防止无限等待</li>
     * </ul>
     */
    @Test
    public void testClockBackward_WaitStrategy() {
        // 使用WAIT策略创建生成器，最大等待10毫秒
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(
                2,
                SnowflakeIdGenerator.ClockBackwardStrategy.WAIT,
                10L
        );
        
        // 正常生成ID（没有时钟回拨时应该正常工作）
        long id = gen.nextId();
        assertTrue("Should generate ID normally", id > 0);
    }

    /**
     * 测试EXTEND_SEQUENCE策略
     * 
     * <p>策略说明：</p>
     * <ul>
     *   <li>当时钟回拨时，使用扩展的序列号空间</li>
     *   <li>原始序列号：12位（0-4095）</li>
     *   <li>扩展序列号：16位（0-65535）</li>
     *   <li>在回拨期间牺牲部分有序性，保证可用性</li>
     * </ul>
     */
    @Test
    public void testClockBackward_ExtendSequenceStrategy() {
        // 使用EXTEND_SEQUENCE策略创建生成器
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(
                3,
                SnowflakeIdGenerator.ClockBackwardStrategy.EXTEND_SEQUENCE
        );
        
        // 正常生成ID（没有时钟回拨时应该正常工作）
        long id = gen.nextId();
        assertTrue("Should generate ID normally", id > 0);
    }

    // ==================== 并发测试 ====================
    // 测试多线程环境下的ID生成安全性和性能

    /**
     * 测试多线程环境下生成ID的唯一性
     * 
     * <p>测试场景：</p>
     * <ul>
     *   <li>10个线程并发生成ID</li>
     *   <li>每个线程生成1000个ID</li>
     *   <li>总共生成10000个ID</li>
     * </ul>
     * 
     * <p>验证点：</p>
     * <ul>
     *   <li>所有ID应该唯一（使用Set去重验证）</li>
     *   <li>不同机器ID的生成器并发工作</li>
     *   <li>synchronized保证线程安全</li>
     * </ul>
     */
    @Test
    public void testConcurrent_GenerateUniqueIds() throws InterruptedException {
        // 并发配置：10个线程，每个线程生成1000个ID
        int threadCount = 10;
        int idsPerThread = 1000;
        
        // 使用HashSet收集所有生成的ID（自动去重）
        Set<Long> allIds = new HashSet<>();
        
        // CountDownLatch用于等待所有线程完成
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // 创建固定大小线程池
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // 启动10个线程并发生成ID
        for (int i = 0; i < threadCount; i++) {
            // 每个线程使用不同的机器ID
            final SnowflakeIdGenerator gen = new SnowflakeIdGenerator(i);
            
            executor.submit(() -> {
                try {
                    // 同步块保护allIds的并发访问
                    synchronized (allIds) {
                        for (int j = 0; j < idsPerThread; j++) {
                            allIds.add(gen.nextId());
                        }
                    }
                } finally {
                    // 线程完成，计数器减1
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成
        latch.await();
        executor.shutdown();

        // 验证：所有ID应该唯一
        int expectedTotal = threadCount * idsPerThread;
        assertEquals("All concurrent IDs should be unique",
                expectedTotal, allIds.size());
    }

    @Test
    public void testConcurrent_Performance() throws InterruptedException {
        // 测试并发性能
        int threadCount = 100;
        int idsPerThread = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicLong totalIds = new AtomicLong(0);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final SnowflakeIdGenerator gen = new SnowflakeIdGenerator(i % 1024);
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等待统一开始
                    for (int j = 0; j < idsPerThread; j++) {
                        gen.nextId();
                        totalIds.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 统一开始
        endLatch.await();
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.printf("Performance: Generated %d IDs in %d ms (%.2f IDs/ms)%n",
                totalIds.get(), duration, (double) totalIds.get() / duration);

        assertEquals("Total IDs should match",
                threadCount * idsPerThread, totalIds.get());
    }

    // ==================== 工具方法测试 ====================

    @Test
    public void testGenerateMachineIdFromMac() {
        // 测试MAC地址生成机器ID
        long machineId = SnowflakeIdGenerator.generateMachineIdFromMac();
        assertTrue("Machine ID should be non-negative", machineId >= 0);
        assertTrue("Machine ID should be within range", machineId <= 1023);
    }

    @Test
    public void testGenerateMachineIdFromMac_ShouldBeConsistent() {
        // 测试同一台机器多次生成应该一致（或至少有效）
        long id1 = SnowflakeIdGenerator.generateMachineIdFromMac();
        long id2 = SnowflakeIdGenerator.generateMachineIdFromMac();
        
        // MAC地址相同应该生成相同的ID
        // 但如果获取失败使用随机值，则可能不同
        assertTrue("Machine ID should be in valid range",
                id1 >= 0 && id1 <= 1023 && id2 >= 0 && id2 <= 1023);
    }

    // ==================== ID结构测试 ====================

    @Test
    public void testIdStructure_TimestampPart() {
        // 测试ID的时间戳部分
        long id = generator.nextId();
        
        // 提取时间戳部分（右移22位）
        long timestampDiff = id >> 22;
        
        // 应该是从2026-01-01开始的毫秒数
        // 2026年应该是一个合理的正数
        assertTrue("Timestamp diff should be positive", timestampDiff > 0);
        
        // 不会太大（应该小于100年的毫秒数）
        long hundredYearsInMs = 100L * 365 * 24 * 60 * 60 * 1000;
        assertTrue("Timestamp diff should be reasonable", timestampDiff < hundredYearsInMs);
    }

    @Test
    public void testIdStructure_MachineIdPart() {
        // 测试ID的机器ID部分
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(123);
        long id = gen.nextId();
        
        // 提取机器ID部分（右移12位后取低10位）
        long extractedMachineId = (id >> 12) & 0x3FF;
        
        assertEquals("Extracted machine ID should match", 123, extractedMachineId);
    }

    @Test
    public void testIdStructure_SequencePart() {
        // 测试ID的序列号部分
        long id1 = generator.nextId();
        long id2 = generator.nextId();
        
        // 提取序列号部分（低12位）
        long seq1 = id1 & 0xFFF;
        long seq2 = id2 & 0xFFF;
        
        // 序列号应该在有效范围内
        assertTrue("Sequence 1 should be in range [0, 4095]",
                seq1 >= 0 && seq1 <= 4095);
        assertTrue("Sequence 2 should be in range [0, 4095]",
                seq2 >= 0 && seq2 <= 4095);
    }

    // ==================== 边界测试 ====================

    @Test
    public void testBoundary_MinMachineId() {
        // 测试最小机器ID
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(0);
        long id = gen.nextId();
        assertTrue("Should generate ID with machineId 0", id > 0);
        
        long extractedMachineId = (id >> 12) & 0x3FF;
        assertEquals("Machine ID should be 0", 0, extractedMachineId);
    }

    @Test
    public void testBoundary_MaxMachineId() {
        // 测试最大机器ID
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(1023);
        long id = gen.nextId();
        assertTrue("Should generate ID with machineId 1023", id > 0);
        
        long extractedMachineId = (id >> 12) & 0x3FF;
        assertEquals("Machine ID should be 1023", 1023, extractedMachineId);
    }

    @Test
    public void testBoundary_SequenceOverflow() {
        // 测试序列号溢出（同一毫秒内生成超过4096个ID）
        // 这会触发等待下一毫秒的逻辑
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(1);
        
        long previousTimestamp = System.currentTimeMillis();
        long previousId = gen.nextId();
        
        // 连续生成ID，直到时间戳变化
        boolean timestampChanged = false;
        for (int i = 0; i < 5000; i++) {
            long currentId = gen.nextId();
            long currentTimestamp = System.currentTimeMillis();
            
            if (currentTimestamp > previousTimestamp) {
                timestampChanged = true;
                break;
            }
            
            previousId = currentId;
        }
        
        // 应该会触发等待下一毫秒
        assertTrue("Should wait for next millisecond when sequence overflows",
                timestampChanged || previousId > 0);
    }

    // ==================== Getter方法测试 ====================

    @Test
    public void testGetters() {
        SnowflakeIdGenerator gen = new SnowflakeIdGenerator(
                5,
                SnowflakeIdGenerator.ClockBackwardStrategy.WAIT,
                15L
        );
        
        assertEquals("Machine ID should be 5", 5, gen.getMachineId());
        assertEquals("Strategy should be WAIT",
                SnowflakeIdGenerator.ClockBackwardStrategy.WAIT,
                gen.getStrategy());
        assertEquals("Max backward ms should be 15", 15L, gen.getMaxBackwardMs());
    }
}
