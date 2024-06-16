package com.example.taskmanager.services.impl;

import com.example.taskmanager.datasource.DatabaseOperationHandler;
import com.example.taskmanager.dtos.TaskDTO;
import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.exceptions.DuplicateTaskException;
import com.example.taskmanager.exceptions.TaskLimitExceededException;
import com.example.taskmanager.exceptions.TaskNotFoundException;
import com.example.taskmanager.mappers.TaskMapper;
import com.example.taskmanager.models.Task;
import com.example.taskmanager.repositories.TaskRepository;
import com.example.taskmanager.services.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;

    private final KafkaServiceImpl kafkaService;

    private static final int MAX_TASKS_LIMIT = 100;
    private static final String TASK_NOT_FOUND_MESSAGE = "Task item with id not found, id: ";

    /**
     * Creates a new task.
     *
     * @param taskDTO the task data transfer object containing the details of the task to be created.
     * @return the created TaskDTO object.
     */
    @Override
    public TaskDTO createTasks(TaskDTO taskDTO) {
        log.info("Starting task creation for: {}", taskDTO);
        return DatabaseOperationHandler.execute(() -> {

            validateTaskCreation(taskDTO);

            Task task = taskMapper.toEntity(taskDTO);
            Task savedTask = taskRepository.save(task);

            TaskDTO savedTaskDTO = taskMapper.toDto(savedTask);

            kafkaService.sendTaskToKafka(savedTaskDTO);

            log.info("Task created successfully: {}", savedTaskDTO);
            return savedTaskDTO;
        });
    }


    /**
     * Retrieves a task by its ID.
     *
     * @param id the ID of the task to be retrieved.
     * @return an Optional containing the Task if found, or an empty Optional if not found.
     * @throws TaskNotFoundException if the task with the specified ID is not found.
     */
    @Override
    public Optional<Task> getTasksById(Long id) {
        log.info("Fetching task by ID: {}", id);
        return DatabaseOperationHandler.execute(() -> {

            Optional<Task> task = taskRepository.findById(id).map(Optional::of).orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND_MESSAGE + id));

            log.info("Task found: {}", task);
            return task;
        });
    }


    /**
     * Retrieves all tasks.
     *
     * @return a list of TaskDTO objects representing all tasks.
     */
    @Override
    public List<TaskDTO> getAllTasks() {
        log.info("Fetching all tasks");
        return DatabaseOperationHandler.execute(() -> {

            List<Task> taskList = taskRepository.findAll();

            List<TaskDTO> taskDTOList = taskList.stream()
                    .map(taskMapper::toDto)
                    .collect(Collectors.toList());

            log.info("Tasks fetched successfully: {}", taskDTOList);
            return taskDTOList;
        });
    }


    /**
     * Updates an existing task.
     *
     * @param id      the ID of the task to be updated.
     * @param taskDTO the task data transfer object containing the updated details of the task.
     * @return the updated TaskDTO object.
     * @throws TaskNotFoundException if the task with the specified ID is not found.
     */
    @Override
    public TaskDTO updateTasks(Long id, TaskDTO taskDTO) {
        log.info("Updating task with ID: {}", id);
        return DatabaseOperationHandler.execute(() -> {

            Task existingTask = taskRepository.findById(id)
                    .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND_MESSAGE + id));

            taskMapper.updateTaskFromDto(taskDTO, existingTask);

            Task updatedTask = taskRepository.save(existingTask);

            TaskDTO updatedTaskDTO = taskMapper.toDto(updatedTask);

            log.info("Task updated successfully: {}", updatedTaskDTO);
            return updatedTaskDTO;
        });
    }


    /**
     * Deletes a task by its ID.
     *
     * @param id the ID of the task to be deleted.
     * @return a message indicating the successful deletion of the task.
     * @throws TaskNotFoundException if the task with the specified ID is not found.
     */
    @Override
    public String deleteTasks(Long id) {
        log.info("Deleting task with ID: {}", id);

        return DatabaseOperationHandler.execute(() -> {

            if (!taskRepository.existsById(id)) {
                throw new TaskNotFoundException(TASK_NOT_FOUND_MESSAGE + id);
            }

            taskRepository.deleteById(id);

            String result = "Task with ID " + id + " has been successfully deleted.";
            log.info(result);
            return result;
        });
    }


    /**
     * Partially updates fields of an existing task.
     *
     * @param id      the ID of the task to be updated.
     * @param updates a map containing the fields to be updated with their new values.
     * @return the updated TaskDTO object.
     * @throws TaskNotFoundException if the task with the specified ID is not found.
     */
    @Override
    public TaskDTO patchTask(Long id, Map<String, Object> updates) {
        log.info("Patching task with ID: {}", id);
        return DatabaseOperationHandler.execute(() -> {
            Task existingTask = taskRepository.findById(id)
                    .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND_MESSAGE + id));

            updates.forEach((field, value) -> updateField(existingTask, field, value));

            Task updatedTask = taskRepository.save(existingTask);
            TaskDTO updatedTaskDTO = taskMapper.toDto(updatedTask);

            log.info("Task patched successfully: {}", updatedTaskDTO);
            return updatedTaskDTO;
        });
    }

    private void updateField(Task task, String fieldName, Object value) {
        switch (fieldName) {
            case "title":
                task.setTitle((String) value);
                break;
            case "description":
                task.setDescription((String) value);
                break;
            case "createdDate":
                task.setCreatedDate(LocalDateTime.parse((String) value));
                break;
            case "dueDate":
                task.setDueDate(LocalDateTime.parse((String) value));
                break;
            case "completed":
                task.setCompleted((Boolean) value);
                break;
            case "priority":
                task.setPriority(Priority.valueOf((String) value));
                break;
            default:
                throw new IllegalArgumentException("Invalid field: " + fieldName);
        }
    }


    /**
     * Validates the task creation details.
     *
     * @param taskDTO the task data transfer object to be validated.
     * @throws TaskLimitExceededException if the task limit is exceeded.
     * @throws DuplicateTaskException     if a task with the same title already exists.
     */
    private void validateTaskCreation(TaskDTO taskDTO) {
        if (taskRepository.count() >= MAX_TASKS_LIMIT) {
            throw new TaskLimitExceededException("Task limit exceeded");
        }
        if (taskRepository.existsByTitle(taskDTO.getTitle())) {
            throw new DuplicateTaskException("Task with title " + taskDTO.getTitle() + " already exists");
        }
    }
}