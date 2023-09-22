package cn.spear.event.core.utils;

import cn.spear.event.core.Singleton;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author luoyong
 * @date 2022/11/10
 */
@Slf4j
public final class AsyncUtils {

    public static AsyncTaskExecutor create(String namePrefix, int threadCount) {
        ThreadFactory factory = ThreadFactoryBuilder.create().setNamePrefix(namePrefix).build();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadCount);
        executor.setThreadFactory(factory);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    public static void run(Runnable runnable, String name) {
        AsyncTaskExecutor executor = Singleton.instanceFunc(
                AsyncTaskExecutor.class,
                k -> create("async-task-", 10));
        run(executor, runnable, name);
    }

    public static void run(Executor executor, Runnable runnable, String name) {
        executor.execute(() -> {
            long start = System.currentTimeMillis();
            try {
                runnable.run();
            } catch (Exception ex) {
                log.error(String.format("%s - 异步执行异常", name), ex);
            } finally {
                log.info("{} - 异步执行耗时：{} ms", name, System.currentTimeMillis() - start);
            }
        });
    }

}
