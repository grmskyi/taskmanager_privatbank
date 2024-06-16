package com.example.taskmanager.exceptions;

public class TaskLimitExceededException extends RuntimeException{
    public TaskLimitExceededException(String message) {
        super(message);
    }
}