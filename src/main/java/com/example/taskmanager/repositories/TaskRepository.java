package com.example.taskmanager.repositories;

import com.example.taskmanager.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    boolean existsByTitle(String title);
    long count();
}
