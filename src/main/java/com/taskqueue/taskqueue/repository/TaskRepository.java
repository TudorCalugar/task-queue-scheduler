package com.taskqueue.taskqueue.repository;

import com.taskqueue.taskqueue.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    // gaseste taskurile care nu s-au terminat (pentru recuperare la restart)
    List<Task> findByStatusIn(List<Task.Status> statuses);
}