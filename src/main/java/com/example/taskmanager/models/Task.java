package com.example.taskmanager.models;


import com.example.taskmanager.enums.Priority;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Priority cannot be null")
    private Priority priority;
}