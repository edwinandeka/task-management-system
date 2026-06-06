package com.edwinandeka.taskmanagement.task;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(final TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponseDto> findAll() {
        return taskService.findAll();
    }

    @GetMapping("/{id}")
    public TaskResponseDto findById(@PathVariable final Long id) {
        return taskService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponseDto create(
            @Valid @RequestBody final TaskRequestDto request,
            final Authentication authentication
    ) {
        return taskService.create(request, authentication.getName());
    }

    @PutMapping("/{id}")
    public TaskResponseDto update(
            @PathVariable final Long id,
            @Valid @RequestBody final TaskRequestDto request
    ) {
        return taskService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final Long id) {
        taskService.delete(id);
    }

    @PatchMapping("/{id}/status")
    public TaskResponseDto updateStatus(
            @PathVariable final Long id,
            @Valid @RequestBody final TaskStatusUpdateDto request
    ) {
        return taskService.updateStatus(id, request);
    }
}
