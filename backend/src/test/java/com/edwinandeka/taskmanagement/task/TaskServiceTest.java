package com.edwinandeka.taskmanagement.task;

import com.edwinandeka.taskmanagement.common.ResourceNotFoundException;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskStatusHistoryRepository taskStatusHistoryRepository;

    @InjectMocks
    private TaskService taskService;

    private User creator() {
        return User.builder()
                .id(1L)
                .fullName("Edwin Ospina")
                .email("edwin@test.com")
                .password("encoded")
                .role(Role.ADMIN)
                .build();
    }

    private User assignee() {
        return User.builder()
                .id(2L)
                .fullName("Jane Doe")
                .email("jane@test.com")
                .password("encoded")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("create stores task with PENDING status and assignee resolved by id")
    void createStoresTaskWithPendingStatusAndResolvedAssignee() {
        final TaskRequestDto request = new TaskRequestDto(
                "Implement login UI",
                "Build Angular login page",
                TaskPriority.HIGH,
                LocalDate.of(2026, 6, 20),
                2L
        );

        when(userRepository.findByEmail("edwin@test.com")).thenReturn(Optional.of(creator()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee()));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            final Task saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        final TaskResponseDto response = taskService.create(request, "edwin@test.com");

        final ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());

        final Task persisted = captor.getValue();
        assertThat(persisted.getTitle()).isEqualTo("Implement login UI");
        assertThat(persisted.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(persisted.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(persisted.getCreatedBy().getId()).isEqualTo(1L);
        assertThat(persisted.getAssignedTo().getId()).isEqualTo(2L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    @DisplayName("create allows null assignee")
    void createAllowsNullAssignee() {
        final TaskRequestDto request = new TaskRequestDto(
                "Write docs",
                null,
                TaskPriority.LOW,
                null,
                null
        );

        when(userRepository.findByEmail("edwin@test.com")).thenReturn(Optional.of(creator()));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final TaskResponseDto response = taskService.create(request, "edwin@test.com");

        verify(userRepository, never()).findById(any());
        assertThat(response.assignedTo()).isNull();
    }

    @Test
    @DisplayName("create throws 404 when assignee does not exist")
    void createThrowsWhenAssigneeDoesNotExist() {
        final TaskRequestDto request = new TaskRequestDto(
                "Implement login UI",
                null,
                TaskPriority.HIGH,
                null,
                999L
        );

        when(userRepository.findByEmail("edwin@test.com")).thenReturn(Optional.of(creator()));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(request, "edwin@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Assignee not found");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("findById throws 404 when task does not exist")
    void findByIdThrowsWhenTaskDoesNotExist() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    @DisplayName("delete throws 404 when task does not exist")
    void deleteThrowsWhenTaskDoesNotExist() {
        when(taskRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("updateStatus writes history entry when status actually changes")
    void updateStatusWritesHistoryEntryWhenStatusChanges() {
        final Task task = Task.builder()
                .id(10L)
                .title("Task")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .createdBy(creator())
                .build();

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("edwin@test.com")).thenReturn(Optional.of(creator()));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.updateStatus(10L, new TaskStatusUpdateDto(TaskStatus.IN_PROGRESS), "edwin@test.com");

        final ArgumentCaptor<TaskStatusHistory> captor = ArgumentCaptor.forClass(TaskStatusHistory.class);
        verify(taskStatusHistoryRepository).save(captor.capture());

        final TaskStatusHistory history = captor.getValue();
        assertThat(history.getFromStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(history.getToStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(history.getChangedBy().getEmail()).isEqualTo("edwin@test.com");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("updateStatus skips history when new status equals previous status")
    void updateStatusSkipsHistoryWhenStatusUnchanged() {
        final Task task = Task.builder()
                .id(10L)
                .title("Task")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .createdBy(creator())
                .build();

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.updateStatus(10L, new TaskStatusUpdateDto(TaskStatus.IN_PROGRESS), "edwin@test.com");

        verify(taskStatusHistoryRepository, never()).save(any(TaskStatusHistory.class));
        verify(userRepository, never()).findByEmail(any());
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("findHistoryByTaskId throws 404 when task does not exist")
    void findHistoryByTaskIdThrowsWhenTaskDoesNotExist() {
        when(taskRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.findHistoryByTaskId(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskStatusHistoryRepository, never()).findAllByTaskIdOrderByChangedAtAsc(any());
    }
}
