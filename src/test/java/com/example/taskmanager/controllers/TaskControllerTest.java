package com.example.taskmanager.controllers;

import com.example.taskmanager.dtos.TaskDTO;
import com.example.taskmanager.enums.Priority;
import com.example.taskmanager.exceptions.TaskNotFoundException;
import com.example.taskmanager.models.Task;
import com.example.taskmanager.services.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    private TaskDTO taskDTO;
    private Task task;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        taskDTO = TaskDTO.builder()
                .id(1L)
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

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createTask_shouldCreateTaskSuccessfully() throws Exception {
        when(taskService.createTasks(any(TaskDTO.class))).thenReturn(taskDTO);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        verify(taskService).createTasks(any(TaskDTO.class));
    }

    @Test
    void getTaskById_shouldReturnTaskWhenExists() throws Exception {
        when(taskService.getTasksById(1L)).thenReturn(Optional.of(task));

        mockMvc.perform(get("/api/v1/task/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(taskService).getTasksById(1L);
    }

    @Test
    void getTaskById_shouldReturn404WhenTaskNotFound() throws Exception {
        when(taskService.getTasksById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/task/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(taskService).getTasksById(1L);
    }

    @Test
    void getAllTasks_shouldReturnAllTasks() throws Exception {
        List<TaskDTO> tasks = Collections.singletonList(taskDTO);
        when(taskService.getAllTasks()).thenReturn(tasks);

        mockMvc.perform(get("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[0].description").value("Test Description"));

        verify(taskService).getAllTasks();
    }

    @Test
    void updateTask_shouldUpdateTaskSuccessfully() throws Exception {
        when(taskService.updateTasks(1L, taskDTO)).thenReturn(taskDTO);

        mockMvc.perform(put("/api/v1/task/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        verify(taskService).updateTasks(1L, taskDTO);
    }

    @Test
    void updateTask_shouldReturn404WhenTaskNotFound() throws Exception {
        when(taskService.updateTasks(1L, taskDTO)).thenThrow(new TaskNotFoundException("Task item with id not found, id: 1"));

        mockMvc.perform(put("/api/v1/task/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isNotFound());

        verify(taskService).updateTasks(1L, taskDTO);
    }

    @Test
    void deleteTask_shouldDeleteTaskSuccessfully() throws Exception {
        when(taskService.deleteTasks(1L)).thenReturn("Task with ID 1 has been successfully deleted.");

        mockMvc.perform(delete("/api/v1/task/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Task with ID 1 has been successfully deleted."));

        verify(taskService).deleteTasks(1L);
    }

    @Test
    void deleteTask_shouldReturn404WhenTaskNotFound() throws Exception {
        when(taskService.deleteTasks(1L)).thenThrow(new TaskNotFoundException("Task item with id not found, id: 1"));

        mockMvc.perform(delete("/api/v1/task/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(taskService).deleteTasks(1L);
    }

    @Test
    void patchTask_shouldPatchTaskSuccessfully() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Updated Title");

        when(taskService.patchTask(1L, updates)).thenReturn(taskDTO);

        mockMvc.perform(patch("/api/v1/task/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(taskService).patchTask(1L, updates);
    }

    @Test
    void patchTask_shouldReturn404WhenTaskNotFound() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Updated Title");

        when(taskService.patchTask(1L, updates)).thenThrow(new TaskNotFoundException("Task item with id not found, id: 1"));

        mockMvc.perform(patch("/api/v1/task/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());

        verify(taskService).patchTask(1L, updates);
    }
}