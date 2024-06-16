package com.example.taskmanager.datasource;

public class DataSourceContextHolder {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    private DataSourceContextHolder() {}

    public static void setDataSourceKey(String key) {
        CONTEXT.set(key);
    }

    public static String getDataSourceKey() {
        return CONTEXT.get();
    }
}