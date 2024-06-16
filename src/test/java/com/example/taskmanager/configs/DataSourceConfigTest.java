package com.example.taskmanager.configs;


import com.example.taskmanager.datasource.DataSourceContextHolder;
import com.example.taskmanager.datasource.RoutingDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@Slf4j
@SpringBootTest
@SpringJUnitConfig
class DataSourceConfigTest {

    @Mock
    private DataSource mainDataSource;

    @Mock
    private DataSource backupDataSource;

    @InjectMocks
    private DataSourceConfig dataSourceConfig;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mainDataSource.toString()).thenReturn("mainDataSource");
        when(backupDataSource.toString()).thenReturn("backupDataSource");
    }

    /**
     * Test to verify the configuration of the data sources.
     * This includes checking the main and backup data sources, ensuring they are correctly mapped in the routing data source.
     *
     * @throws SQLException if there is an error while configuring the data sources.
     */
    @Test
    void testDataSourceConfig() throws SQLException {
        mockDataSource(mainDataSource, "jdbc:h2:mem:testdb");
        mockDataSource(backupDataSource, "jdbc:postgresql://localhost:5432/backuppgdb");

        DataSource dataSource = dataSourceConfig.dataSource(mainDataSource, backupDataSource);
        assertNotNull(dataSource);
        assertInstanceOf(AbstractRoutingDataSource.class, dataSource);

        AbstractRoutingDataSource routingDataSource = (AbstractRoutingDataSource) dataSource;
        routingDataSource.afterPropertiesSet();

        Map<Object, DataSource> dataSourceMap = routingDataSource.getResolvedDataSources();

        assertNotNull(dataSourceMap);
        assertEquals(2, dataSourceMap.size());
        assertTrue(dataSourceMap.containsKey("MAIN"));
        assertTrue(dataSourceMap.containsKey("BACKUP"));

        log.info("Main DataSource URL: {}", getJdbcUrl(mainDataSource));
        log.info("Backup DataSource URL: {}", getJdbcUrl(backupDataSource));
    }

    /**
     * Test to ensure the switching of data source to backup.
     * This test checks if the data source key is correctly set to "BACKUP" when switching.
     */
    @Test
    void testSwitchToBackup() {
        log.info("Testing switch to backup");
        DataSourceConfig.switchToBackup();
        assertEquals("BACKUP", DataSourceContextHolder.getDataSourceKey());
    }

    /**
     * Test to ensure the switching of data source to main.
     * This test checks if the data source key is correctly set to "MAIN" when switching.
     */
    @Test
    void testSwitchToMain() {
        log.info("Testing switch to main");
        DataSourceConfig.switchToMain();
        assertEquals("MAIN", DataSourceContextHolder.getDataSourceKey());
    }

    /**
     * Utility method to get the JDBC URL of a given data source.
     *
     * @param dataSource the data source from which to get the JDBC URL.
     * @return the JDBC URL as a string.
     */
    private String getJdbcUrl(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        } catch (SQLException e) {
            log.error("Failed to get JDBC URL", e);
            return "Unknown";
        }
    }

    /**
     * Utility method to mock a data source with a given URL.
     *
     * @param dataSource the data source to be mocked.
     * @param url        the JDBC URL to be returned by the mocked data source.
     * @throws SQLException if there is an error while mocking the data source.
     */
    private void mockDataSource(DataSource dataSource, String url) throws SQLException {
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(metaData.getURL()).thenReturn(url);
        when(connection.getMetaData()).thenReturn(metaData);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    /**
     * Configuration class for defining data sources for testing purposes.
     */
    @Configuration
    static class TestConfig {

        /**
         * Creates a mock DataSource bean for the main data source.
         *
         * @return a mock DataSource representing the main data source.
         */
        @Bean(name = "mainDataSource")
        @Primary
        public DataSource mainDataSource() {
            return mock(DataSource.class);
        }

        /**
         * Creates a mock DataSource bean for the backup data source.
         *
         * @return a mock DataSource representing the backup data source.
         */
        @Bean(name = "backupDataSource")
        public DataSource backupDataSource() {
            return mock(DataSource.class);
        }

        /**
         * Creates a RoutingDataSource bean that routes between the main and backup data sources.
         * The routing logic is based on the data source key set in the DataSourceContextHolder.
         *
         * @param mainDataSource   the main DataSource bean.
         * @param backupDataSource the backup DataSource bean.
         * @return a RoutingDataSource that routes between the main and backup data sources.
         */
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
    }
}