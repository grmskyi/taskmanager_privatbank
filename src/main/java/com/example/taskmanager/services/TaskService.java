package com.example.taskmanager.services;

import com.example.taskmanager.dtos.TaskDTO;
import com.example.taskmanager.models.Task;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TaskService {
    TaskDTO createTask(TaskDTO taskDTO);

    Optional<Task> getTaskById(Long id);

    List<TaskDTO> getAllTasks();

    TaskDTO updateTask(Long id, TaskDTO taskDTO);

    String deleteTask(Long id);

    TaskDTO patchTask(Long id, Map<String, Object> updates);
}
