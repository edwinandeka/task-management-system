package com.edwinandeka.taskmanagement.task;

import com.edwinandeka.taskmanagement.common.ResourceNotFoundException;
import com.edwinandeka.taskmanagement.user.User;
import com.edwinandeka.taskmanagement.user.UserRepository;
import com.edwinandeka.taskmanagement.user.UserResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;

    public TaskService(
            final TaskRepository taskRepository,
            final UserRepository userRepository,
            final TaskStatusHistoryRepository taskStatusHistoryRepository
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskStatusHistoryRepository = taskStatusHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDto> findAll() {
        return taskRepository.findAll().stream()
                .map(TaskService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponseDto findById(final Long id) {
        final Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskHistoryResponseDto> findHistoryByTaskId(final Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with id: " + taskId);
        }
        return taskStatusHistoryRepository.findAllByTaskIdOrderByChangedAtAsc(taskId).stream()
                .map(TaskService::toHistoryResponse)
                .toList();
    }

    @Transactional
    public TaskResponseDto create(final TaskRequestDto request, final String currentUserEmail) {
        final User creator = loadUserByEmail(currentUserEmail);
        final User assignee = resolveAssignee(request.assignedToId());

        final Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(TaskStatus.PENDING)
                .priority(request.priority())
                .dueDate(request.dueDate())
                .createdBy(creator)
                .assignedTo(assignee)
                .build();

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponseDto update(final Long id, final TaskRequestDto request) {
        final Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setAssignedTo(resolveAssignee(request.assignedToId()));

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(final Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    @Transactional
    public TaskResponseDto updateStatus(
            final Long id,
            final TaskStatusUpdateDto request,
            final String currentUserEmail
    ) {
        final Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        final TaskStatus previousStatus = task.getStatus();
        final TaskStatus newStatus = request.status();

        if (previousStatus != newStatus) {
            final User changedBy = loadUserByEmail(currentUserEmail);
            final TaskStatusHistory historyEntry = TaskStatusHistory.builder()
                    .task(task)
                    .fromStatus(previousStatus)
                    .toStatus(newStatus)
                    .changedBy(changedBy)
                    .build();
            taskStatusHistoryRepository.save(historyEntry);
            task.setStatus(newStatus);
        }

        return toResponse(taskRepository.save(task));
    }

    private User loadUserByEmail(final String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private User resolveAssignee(final Long assignedToId) {
        if (assignedToId == null) {
            return null;
        }
        return userRepository.findById(assignedToId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + assignedToId));
    }

    private static TaskHistoryResponseDto toHistoryResponse(final TaskStatusHistory history) {
        return new TaskHistoryResponseDto(
                history.getId(),
                history.getTask().getId(),
                history.getFromStatus(),
                history.getToStatus(),
                UserResponseDto.from(history.getChangedBy()),
                history.getChangedAt()
        );
    }

    private static TaskResponseDto toResponse(final Task task) {
        return new TaskResponseDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                UserResponseDto.from(task.getCreatedBy()),
                UserResponseDto.from(task.getAssignedTo()),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
