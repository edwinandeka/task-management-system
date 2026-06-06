package com.edwinandeka.taskmanagement.task;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/history")
public class TaskHistoryController {

    private final TaskService taskService;

    public TaskHistoryController(final TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskHistoryResponseDto> findAll(@PathVariable final Long taskId) {
        return taskService.findHistoryByTaskId(taskId);
    }
}
