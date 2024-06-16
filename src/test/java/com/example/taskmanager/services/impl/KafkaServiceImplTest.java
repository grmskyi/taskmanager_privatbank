package com.example.taskmanager.services.impl;

import com.example.taskmanager.dtos.TaskDTO;
import com.example.taskmanager.enums.Priority;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class)
class KafkaServiceImplTest {
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaServiceImpl kafkaService;

    private TaskDTO taskDTO;

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
    }

    @Test
    void sendTaskToKafka_shouldSendTaskToKafkaTopic() {
        log.info("Starting test: sendTaskToKafka_shouldSendTaskToKafkaTopic");

        kafkaService.sendTaskToKafka(taskDTO);

        verify(kafkaTemplate).send("taskmanager-topic", taskDTO);
        log.info("Task sent to Kafka topic successfully");
    }
}