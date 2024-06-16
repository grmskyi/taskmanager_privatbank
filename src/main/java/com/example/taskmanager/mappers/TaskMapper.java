package com.example.taskmanager.mappers;

import com.example.taskmanager.dtos.TaskDTO;
import com.example.taskmanager.models.Task;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskDTO toDto(Task task);

    Task toEntity(TaskDTO taskDTO);

    void updateTaskFromDto(TaskDTO dto, @MappingTarget Task entity);
}
