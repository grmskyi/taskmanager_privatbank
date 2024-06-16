package com.example.taskmanager.configs;

import com.example.taskmanager.datasource.DataSourceContextHolder;
import com.example.taskmanager.datasource.RoutingDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Configuration
public class DataSourceConfig {

    @Bean(name = "mainDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource mainDataSource() {
        return org.springframework.boot.jdbc.DataSourceBuilder.create().build();
    }

    @Bean(name = "backupDataSource")
    @ConfigurationProperties(prefix = "backup.datasource")
    public DataSource backupDataSource() {
        return org.springframework.boot.jdbc.DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("mainDataSource") DataSource mainDataSource,
                                 @Qualifier("backupDataSource") DataSource backupDataSource) {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("MAIN", mainDataSource);
        dataSourceMap.put("BACKUP", backupDataSource);
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(mainDataSource);
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }

    public static void switchToBackup() {
        log.info("Switching to backup data source (PostgreSQL)");
        DataSourceContextHolder.setDataSourceKey("BACKUP");
    }

    public static void switchToMain() {
        log.info("Switching to main data source (H2)");
        DataSourceContextHolder.setDataSourceKey("MAIN");
    }
}