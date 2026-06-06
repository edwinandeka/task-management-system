package com.edwinandeka.taskmanagement.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByCreatedById(final Long createdById);

    List<Task> findAllByAssignedToId(final Long assignedToId);
}
