package com.taskqueue.taskqueue.service;

import com.taskqueue.taskqueue.model.Task;
import com.taskqueue.taskqueue.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    // coada de lucru: doar id-uri, nu obiecte (obiectele traiesc in DB acum)
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    private final ScheduledExecutorService retryScheduler =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread t = new Thread(runnable, "retry-scheduler");
                t.setDaemon(true);
                return t;
            });

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task submit(String type, String payload) {
        Task task = new Task(type, payload);
        taskRepository.save(task);       // salveaza in DB
        queue.add(task.getId());         // pune doar id-ul in coada
        return task;
    }

    public Optional<Task> getById(String id) {
        return taskRepository.findById(id);
    }

    public String takeNextId() throws InterruptedException {
        return queue.take();
    }

    public void save(Task task) {
        taskRepository.save(task);
    }

    public void requeueAfter(String taskId, long delayMillis) {
        retryScheduler.schedule(() -> queue.add(taskId), delayMillis, TimeUnit.MILLISECONDS);
    }

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    // pentru recuperare la restart: task-uri ramase in lucru
    public List<Task> findUnfinished() {
        return taskRepository.findByStatusIn(
                List.of(Task.Status.PENDING, Task.Status.RUNNING));
    }
}