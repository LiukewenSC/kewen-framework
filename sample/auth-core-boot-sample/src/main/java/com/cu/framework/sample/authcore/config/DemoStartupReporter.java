package com.cu.framework.sample.authcore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 启动横幅：把"数据文件到底落在哪"和页面地址打出来。
 * <p>
 * 建表脚本与 H2 连接串里的 {@code ./data} 是相对<b>启动目录</b>解析的。
 * IDE 的运行配置默认工作目录往往是仓库根，于是会另建一个 {@code <仓库根>/data/} 的库——
 * 应用照样启动成功，只是连的是另一份数据，看起来就像"数据没了"或"起不来（端口/文件锁被占）"。
 * 这里在启动时把绝对路径摊开说清楚，工作目录不对时直接告警
 *
 * @author kewen
 * @since 2026-10-21
 */
@Component
public class DemoStartupReporter implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoStartupReporter.class);

    /** 示例模块目录名，用于判断当前工作目录对不对 */
    private static final String MODULE_DIR = "auth-core-boot-sample";

    private final Environment environment;

    public DemoStartupReporter(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        File userDir = new File(System.getProperty("user.dir"));
        String url = environment.getProperty("spring.datasource.url", "");
        log.info("示例已就绪：页面 http://localhost:{}{}/　数据源 {}",
                environment.getProperty("server.port", "8082"),
                environment.getProperty("server.servlet.context-path", ""),
                url);
        if (!userDir.getName().equals(MODULE_DIR)) {
            log.warn("当前启动目录是 <{}>，不是 {} 模块目录：H2 的 ./data 会解析到 <{}>，"
                            + "与从模块目录启动时用的库不是同一个文件。IDE 里请把运行配置的 Working directory "
                            + "设为 $MODULE_WORKING_DIR$（即 sample/{}），或命令行 cd 进模块目录再启动。"
                            + "另外同一时刻只能有一个进程持有该数据文件，重复启动会因端口或文件锁失败。",
                    userDir.getAbsolutePath(), MODULE_DIR,
                    new File(userDir, "data").getAbsolutePath(), MODULE_DIR);
        }
    }
}
