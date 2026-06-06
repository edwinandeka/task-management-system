package com.edwinandeka.taskmanagement.comment;

import com.edwinandeka.taskmanagement.common.ResourceNotFoundException;
import com.edwinandeka.taskmanagement.task.Task;
import com.edwinandeka.taskmanagement.task.TaskRepository;
import com.edwinandeka.taskmanagement.user.User;
import com.edwinandeka.taskmanagement.user.UserRepository;
import com.edwinandeka.taskmanagement.user.UserResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskCommentService(
            final TaskCommentRepository taskCommentRepository,
            final TaskRepository taskRepository,
            final UserRepository userRepository
    ) {
        this.taskCommentRepository = taskCommentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskCommentResponseDto> findAllByTaskId(final Long taskId) {
        ensureTaskExists(taskId);

        return taskCommentRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(TaskCommentService::toResponse)
                .toList();
    }

    @Transactional
    public TaskCommentResponseDto create(
            final Long taskId,
            final TaskCommentRequestDto request,
            final String currentUserEmail
    ) {
        final Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        final User author = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        final TaskComment comment = TaskComment.builder()
                .task(task)
                .author(author)
                .content(request.content())
                .build();

        return toResponse(taskCommentRepository.save(comment));
    }

    private void ensureTaskExists(final Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with id: " + taskId);
        }
    }

    private static TaskCommentResponseDto toResponse(final TaskComment comment) {
        return new TaskCommentResponseDto(
                comment.getId(),
                comment.getTask().getId(),
                comment.getContent(),
                UserResponseDto.from(comment.getAuthor()),
                comment.getCreatedAt()
        );
    }
}
