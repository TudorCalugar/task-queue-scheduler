package com.taskqueue.taskqueue.controller;

import com.taskqueue.taskqueue.model.Task;
import com.taskqueue.taskqueue.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    public record SubmitTaskRequest(String type, String payload) {}

    @PostMapping
    public ResponseEntity<Task> submitTask(@RequestBody SubmitTaskRequest request) {
        Task task = taskService.submit(request.type(), request.payload());
        return ResponseEntity.ok(task);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable String id) {
        return taskService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}