package com.taskqueue.taskqueue.worker;

import com.taskqueue.taskqueue.model.Task;
import com.taskqueue.taskqueue.service.TaskService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class TaskWorker implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TaskWorker.class);

    private static final int WORKER_COUNT = 3;
    private static final int MAX_RETRIES = 3;
    private static final long BASE_BACKOFF_MS = 1000;

    private final TaskService taskService;
    private final ExecutorService pool;

    public TaskWorker(TaskService taskService) {
        this.taskService = taskService;
        this.pool = Executors.newFixedThreadPool(WORKER_COUNT, runnable -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void run(ApplicationArguments args) {
        recoverUnfinishedTasks();

        for (int i = 1; i <= WORKER_COUNT; i++) {
            pool.submit(this::workLoop);
        }
        log.info("{} workeri porniti, asteapta task-uri...", WORKER_COUNT);
    }

    // la pornire: repunem in coada task-urile ramase neterminate dupa un restart
    private void recoverUnfinishedTasks() {
        List<Task> unfinished = taskService.findUnfinished();
        if (unfinished.isEmpty()) {
            return;
        }
        for (Task task : unfinished) {
            if (task.getStatus() == Task.Status.RUNNING) {
                task.setStatus(Task.Status.PENDING);   // era in lucru cand a picat serverul
                taskService.save(task);
            }
            taskService.requeueAfter(task.getId(), 0);
        }
        log.info("Recuperate {} task-uri neterminate dupa restart", unfinished.size());
    }

    private void workLoop() {
        while (true) {
            try {
                String taskId = taskService.takeNextId();
                process(taskId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Worker oprit.");
                return;
            }
        }
    }

    private void process(String taskId) throws InterruptedException {
        Optional<Task> maybeTask = taskService.getById(taskId);
        if (maybeTask.isEmpty()) {
            log.warn("Task {} nu a fost gasit in DB, il ignor", taskId);
            return;
        }
        Task task = maybeTask.get();

        log.info("Procesez task {} (tip: {}) - incercarea {}",
                task.getId(), task.getType(), task.getRetryCount() + 1);

        task.setStatus(Task.Status.RUNNING);
        taskService.save(task);

        try {
            doWork(task);
            task.setStatus(Task.Status.DONE);
            taskService.save(task);
            log.info("Task {} finalizat -> DONE", task.getId());
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            handleFailure(task, e);
        }
    }

    private void doWork(Task task) throws InterruptedException {
        Thread.sleep(2000);
        if ("fail".equalsIgnoreCase(task.getType())) {
            throw new RuntimeException("esec simulat pentru task de tip 'fail'");
        }
    }

    private void handleFailure(Task task, Exception e) {
        task.incrementRetryCount();

        if (task.getRetryCount() > MAX_RETRIES) {
            task.setStatus(Task.Status.FAILED);
            taskService.save(task);
            log.error("Task {} a esuat definitiv dupa {} reincercari: {}",
                    task.getId(), MAX_RETRIES, e.getMessage());
            return;
        }

        long delay = BASE_BACKOFF_MS * (1L << (task.getRetryCount() - 1));
        task.setStatus(Task.Status.PENDING);
        taskService.save(task);

        log.warn("Task {} a esuat ({}). Retry {}/{} peste {} ms",
                task.getId(), e.getMessage(), task.getRetryCount(), MAX_RETRIES, delay);

        taskService.requeueAfter(task.getId(), delay);
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        log.info("Opresc pool-ul de workeri...");
        pool.shutdownNow();
        pool.awaitTermination(5, TimeUnit.SECONDS);
    }
}