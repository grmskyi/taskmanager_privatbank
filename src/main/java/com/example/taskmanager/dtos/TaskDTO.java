package com.example.taskmanager.dtos;

import com.example.taskmanager.enums.Priority;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    private Long id;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title cannot be longer than 255 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot be longer than 1000 characters")
    private String description;

    @PastOrPresent(message = "Created date cannot be in the future")
    private LocalDateTime createdDate;

    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDateTime dueDate;

    private Boolean completed;

    @NotNull(message = "Priority cannot be null")
    private Priority priority;
}