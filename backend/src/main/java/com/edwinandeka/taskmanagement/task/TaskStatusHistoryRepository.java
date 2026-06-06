package com.edwinandeka.taskmanagement.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskStatusHistoryRepository extends JpaRepository<TaskStatusHistory, Long> {

    List<TaskStatusHistory> findAllByTaskIdOrderByChangedAtAsc(final Long taskId);
}
