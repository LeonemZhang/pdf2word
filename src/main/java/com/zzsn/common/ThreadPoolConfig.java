package com.zzsn.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author 张宗涵
 * @date 2024/4/23
 */
@Configuration
public class ThreadPoolConfig {
    // 线程名称前缀
    @Value("${thread.prefix:trans-thread-}")
    private String threadPrefix;

    // 核心线程数
    @Value("${thread.core.size:16}")
    private int coreSize;

    // 最大线程数
    @Value("${thread.max.size:32}")
    private int maxSize;

    // 队列长度
    @Value("${thread.queue.size:30}")
    private int queueSize;

    // 通过bean注解注入
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // 设置线程池参数信息
        taskExecutor.setCorePoolSize(coreSize);
        taskExecutor.setMaxPoolSize(maxSize);
        taskExecutor.setQueueCapacity(queueSize);
        taskExecutor.setThreadNamePrefix(threadPrefix);
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(30);

        // 修改拒绝策略为使用当前线程执行
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 初始化线程池
        taskExecutor.initialize();
        return taskExecutor;
    }
}
