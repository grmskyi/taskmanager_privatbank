package com.example.taskmanager.datasource;


import com.example.taskmanager.configs.DataSourceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;

import java.util.function.Supplier;

@Slf4j
public class DatabaseOperationHandler {

    private DatabaseOperationHandler(){}

    public static <T> T execute(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataAccessException ex) {
            log.error("Main database failed, switching to backup", ex);
            DataSourceConfig.switchToBackup();
            return operation.get();
        }
    }
}
