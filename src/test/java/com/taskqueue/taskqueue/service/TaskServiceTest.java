package com.taskqueue.taskqueue.service;

import com.taskqueue.taskqueue.model.Task;
import com.taskqueue.taskqueue.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void submit_creeazaTaskCuStatusPending() {
        Task result = taskService.submit("email", "salut");

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("email");
        assertThat(result.getPayload()).isEqualTo("salut");
        assertThat(result.getStatus()).isEqualTo(Task.Status.PENDING);
        assertThat(result.getRetryCount()).isEqualTo(0);
    }

    @Test
    void submit_salveazaTaskulInRepository() {
        taskService.submit("email", "salut");

        // verificam ca s-a apelat save() exact o data, cu un Task
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void getById_intoarceTaskulCandExista() {
        Task task = new Task("email", "salut");
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        Optional<Task> result = taskService.getById(task.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo("email");
    }

    @Test
    void getById_intoarceGolCandNuExista() {
        when(taskRepository.findById(anyString())).thenReturn(Optional.empty());

        Optional<Task> result = taskService.getById("id-inexistent");

        assertThat(result).isEmpty();
    }
}