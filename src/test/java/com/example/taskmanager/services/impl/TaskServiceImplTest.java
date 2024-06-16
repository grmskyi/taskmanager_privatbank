package com.example.taskmanager.services.impl;

import com.example.taskmanager.dtos.TaskDTO;
import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.exceptions.DuplicateTaskException;
import com.example.taskmanager.exceptions.TaskLimitExceededException;
import com.example.taskmanager.exceptions.TaskNotFoundException;
import com.example.taskmanager.mappers.TaskMapper;
import com.example.taskmanager.models.Task;
import com.example.taskmanager.repositories.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private KafkaTemplate<String, TaskDTO> kafkaTemplate;

    @InjectMocks
    private TaskServiceImpl taskService;

    @InjectMocks
    private KafkaServiceImpl kafkaService;

    private Task task;
    private TaskDTO taskDTO;

    @BeforeEach
    void setUp() {
        taskDTO = TaskDTO.builder()
                .title("Test Task")
                .description("Test Description")
                .createdDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .completed(false)
                .priority(Priority.HIGH)
                .build();

        task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .createdDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .completed(false)
                .priority(Priority.HIGH)
                .build();

        taskService = new TaskServiceImpl(taskMapper, taskRepository, kafkaService);
    }

    @Test
    void createTasks_shouldCreateTaskSuccessfully() {
        log.info("Starting test: createTasks_shouldCreateTaskSuccessfully");

        when(taskMapper.toEntity(taskDTO)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskDTO);

        TaskDTO result = taskService.createTasks(taskDTO);

        assertNotNull(result);
        assertEquals(taskDTO.getTitle(), result.getTitle());
        verify(taskRepository).save(task);
        verify(kafkaTemplate).send("taskmanager-topic", taskDTO);
        assertEquals(task.getTitle(), result.getTitle());

        log.info("Task created successfully: {}", result);
    }

    @Test
    void createTasks_shouldThrowExceptionWhenTaskLimitExceeded() {
        log.info("Starting test: createTasks_shouldThrowExceptionWhenTaskLimitExceeded");

        when(taskRepository.count()).thenReturn(100L);

        TaskLimitExceededException exception = assertThrows(
                TaskLimitExceededException.class, () -> taskService.createTasks(taskDTO)
        );

        assertEquals("Task limit exceeded", exception.getMessage());

        log.warn("Expected exception when limit exceeded caught: {}", exception.getMessage());
    }

    @Test
    void createTasks_shouldThrowExceptionWhenTaskTitleExists() {
        log.info("Starting test: createTasks_shouldThrowExceptionWhenTaskTitleExists");

        when(taskRepository.count()).thenReturn(10L);
        when(taskRepository.existsByTitle("Test Task")).thenReturn(true);

        DuplicateTaskException exception = assertThrows(
                DuplicateTaskException.class, () -> taskService.createTasks(taskDTO)
        );

        assertEquals("Task with title Test Task already exists", exception.getMessage());

        log.warn("Expected exception when task title is duplicate caught: {}", exception.getMessage());
    }

    @Test
    void getTasksById_shouldReturnTaskWhenExists() {
        log.info("Starting test: getTasksById_shouldReturnTaskWhenExists");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Optional<Task> result = taskService.getTasksById(1L);

        assertTrue(result.isPresent());
        assertEquals(task.getTitle(), result.get().getTitle());

        log.info("Task found successfully: {}", result.get());
    }

    @Test
    void getTasksById_shouldThrowExceptionWhenTaskNotFound() {
        log.info("Starting test: getTasksById_shouldThrowExceptionWhenTaskNotFound");

        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class, () -> taskService.getTasksById(1L)
        );

        assertEquals("Task item with id not found, id: 1", exception.getMessage());

        log.warn("Expected exception when task not found by id caught: {}", exception.getMessage());
    }

    @Test
    void getAllTasks_shouldReturnAllTasks() {
        log.info("Starting test: getAllTasks_shouldReturnAllTasks");

        List<Task> tasks = Collections.singletonList(task);
        when(taskRepository.findAll()).thenReturn(tasks);
        when(taskMapper.toDto(task)).thenReturn(taskDTO);

        List<TaskDTO> result = taskService.getAllTasks();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(taskDTO.getTitle(), result.getFirst().getTitle());

        log.info("All tasks retrieved successfully: {}", result);
    }

    @Test
    void updateTasks_shouldUpdateTaskWhenExists() {
        log.info("Starting test: updateTasks_shouldUpdateTaskWhenExists");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskDTO);

        TaskDTO result = taskService.updateTasks(1L, taskDTO);

        assertNotNull(result);
        assertEquals(taskDTO.getTitle(), result.getTitle());
        verify(taskRepository).save(task);

        log.info("Task updated successfully: {}", result);
    }

    @Test
    void updateTasks_shouldThrowExceptionWhenTaskNotFound() {
        log.info("Starting test: updateTasks_shouldThrowExceptionWhenTaskNotFound");

        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class, () -> taskService.updateTasks(1L, taskDTO)
        );

        assertEquals("Task item with id not found, id: 1", exception.getMessage());

        log.warn("Expected exception when task for update not found caught: {}", exception.getMessage());
    }

    @Test
    void deleteTasks_shouldDeleteTaskWhenExists() {
        log.info("Starting test: deleteTasks_shouldDeleteTaskWhenExists");

        when(taskRepository.existsById(1L)).thenReturn(true);

        String result = taskService.deleteTasks(1L);

        assertNotNull(result);
        assertEquals("Task with ID 1 has been successfully deleted.", result);

        verify(taskRepository).deleteById(1L);

        log.info(result);
    }

    @Test
    void deleteTasks_shouldThrowExceptionWhenTaskNotFound() {
        log.info("Starting test: deleteTasks_shouldThrowExceptionWhenTaskNotFound");

        when(taskRepository.existsById(1L)).thenReturn(false);

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class, () -> taskService.deleteTasks(1L)
        );

        assertEquals("Task item with id not found, id: 1", exception.getMessage());

        log.warn("Expected exception when task for delete not found caught: {}", exception.getMessage());
    }

    @Test
    void createTasks_shouldHandleDataAccessExceptionAndSwitchToBackup() {
        log.info("Starting test: createTasks_shouldHandleDataAccessExceptionAndSwitchToBackup");

        when(taskMapper.toEntity(taskDTO)).thenReturn(task);
        when(taskRepository.save(task))
                .thenThrow(new DataAccessException("Database error") {
                })
                .thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskDTO);

        TaskDTO result = taskService.createTasks(taskDTO);

        assertNotNull(result);
        assertEquals(taskDTO.getTitle(), result.getTitle());
        verify(taskRepository, times(2)).save(task);
        verify(kafkaTemplate).send("taskmanager-topic", taskDTO);

        log.info("Task created successfully after switching to backup: {}", result);
    }

    @Test
    void createTasks_shouldCreateTaskWithValidDetails() {
        log.info("Starting test: createTasks_shouldCreateTaskWithValidDetails");

        when(taskRepository.count()).thenReturn(0L);
        when(taskRepository.existsByTitle("Test Task")).thenReturn(false);

        when(taskMapper.toEntity(taskDTO)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskDTO);

        TaskDTO result = taskService.createTasks(taskDTO);

        assertNotNull(result);
        assertEquals(taskDTO.getTitle(), result.getTitle());
        verify(taskRepository).save(task);
        verify(kafkaTemplate).send("taskmanager-topic", taskDTO);
        assertEquals(task.getTitle(), result.getTitle());

        log.info("Task created successfully with valid details: {}", result);
    }
}