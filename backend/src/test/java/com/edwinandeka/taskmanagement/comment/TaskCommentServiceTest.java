package com.edwinandeka.taskmanagement.comment;

import com.edwinandeka.taskmanagement.common.ResourceNotFoundException;
import com.edwinandeka.taskmanagement.task.Task;
import com.edwinandeka.taskmanagement.task.TaskPriority;
import com.edwinandeka.taskmanagement.task.TaskRepository;
import com.edwinandeka.taskmanagement.task.TaskStatus;
import com.edwinandeka.taskmanagement.user.Role;
import com.edwinandeka.taskmanagement.user.User;
import com.edwinandeka.taskmanagement.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskCommentServiceTest {

    @Mock
    private TaskCommentRepository taskCommentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskCommentService taskCommentService;

    private User author() {
        return User.builder()
                .id(1L)
                .fullName("Edwin Ospina")
                .email("edwin@test.com")
                .password("encoded")
                .role(Role.ADMIN)
                .build();
    }

    private Task task() {
        return Task.builder()
                .id(10L)
                .title("Some task")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .createdBy(author())
                .build();
    }

    @Test
    @DisplayName("create saves comment with author taken from current user email")
    void createSavesCommentWithAuthorFromCurrentUser() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task()));
        when(userRepository.findByEmail("edwin@test.com")).thenReturn(Optional.of(author()));
        when(taskCommentRepository.save(any(TaskComment.class))).thenAnswer(invocation -> {
            final TaskComment saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        final TaskCommentResponseDto response = taskCommentService.create(
                10L,
                new TaskCommentRequestDto("First comment"),
                "edwin@test.com"
        );

        final ArgumentCaptor<TaskComment> captor = ArgumentCaptor.forClass(TaskComment.class);
        verify(taskCommentRepository).save(captor.capture());

        final TaskComment persisted = captor.getValue();
        assertThat(persisted.getContent()).isEqualTo("First comment");
        assertThat(persisted.getTask().getId()).isEqualTo(10L);
        assertThat(persisted.getAuthor().getEmail()).isEqualTo("edwin@test.com");

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.taskId()).isEqualTo(10L);
        assertThat(response.author().email()).isEqualTo("edwin@test.com");
    }

    @Test
    @DisplayName("create throws 404 when task does not exist")
    void createThrowsWhenTaskDoesNotExist() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskCommentService.create(
                999L,
                new TaskCommentRequestDto("Comment"),
                "edwin@test.com"
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");

        verify(taskCommentRepository, never()).save(any(TaskComment.class));
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("findAllByTaskId throws 404 when task does not exist")
    void findAllByTaskIdThrowsWhenTaskDoesNotExist() {
        when(taskRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> taskCommentService.findAllByTaskId(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskCommentRepository, never()).findAllByTaskIdOrderByCreatedAtAsc(any());
    }
}
