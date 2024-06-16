package com.example.taskmanager.services;

import com.example.taskmanager.dtos.TaskDTO;
import com.example.taskmanager.models.Task;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TaskService {
    TaskDTO createTasks (TaskDTO taskDTO);

    Optional<Task> getTasksById(Long id);

    List<TaskDTO> getAllTasks();

    TaskDTO updateTasks (Long id, TaskDTO taskDTO);

    String deleteTasks(Long id);

    TaskDTO patchTask(Long id, Map<String, Object> updates);
}
