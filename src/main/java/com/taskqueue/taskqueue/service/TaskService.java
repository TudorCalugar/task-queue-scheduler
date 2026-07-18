package com.taskqueue.taskqueue.service;

import com.taskqueue.taskqueue.model.Task;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TaskService {

    // coada propriu-zisă: task-urile de procesat, în ordine FIFO
    private final BlockingQueue<Task> queue = new LinkedBlockingQueue<>();

    // evidența tuturor task-urilor, acces rapid după id (pentru GET /tasks/{id})
    private final Map<String, Task> tasksById = new ConcurrentHashMap<>();

    public Task submit(String type, String payload) {
        Task task = new Task(type, payload);
        tasksById.put(task.getId(), task);
        queue.add(task);
        return task;
    }

    public Optional<Task> getById(String id) {
        return Optional.ofNullable(tasksById.get(id));
    }

    // scoate următorul task din coadă; dacă nu e niciunul, ASTEAPTĂ până apare
    public Task takeNext() throws InterruptedException {
        return queue.take();
    }

    // ceas separat care repune task-urile esuate in coada dupa un delay
    private final ScheduledExecutorService retryScheduler =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread t = new Thread(runnable, "retry-scheduler");
                t.setDaemon(true);
                return t;
            });

    public void requeueAfter(Task task, long delayMillis) {
        retryScheduler.schedule(() -> queue.add(task), delayMillis, TimeUnit.MILLISECONDS);
    }

}