package com.example.taskmanager.services.impl;

import com.example.taskmanager.dtos.TaskDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaServiceImpl {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TASK_TOPIC = "taskmanager-topic";

    /**
     * Sends the task data to a Kafka topic.
     *
     * @param taskDTO the task data transfer object to be sent to Kafka.
     */
    public void sendTaskToKafka(TaskDTO taskDTO) {
        kafkaTemplate.send(TASK_TOPIC, taskDTO);
        log.info("Sent task {} to Kafka topic {}", taskDTO.getId(), TASK_TOPIC);
    }
}