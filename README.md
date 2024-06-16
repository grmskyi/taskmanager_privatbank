# Task Manager

A Spring Boot application for managing tasks with Kafka integration and support for multiple data sources.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Installation](#installation)
- [API Endpoints](#api-endpoints)
- [Confirmation of some task criteria](#task_criteria)
- [Contributing](#contributing)
- [License](#license)

## Introduction

Task Manager is a Spring Boot application designed to manage tasks with support for Kafka messaging and multiple data sources (main and backup). It allows creating, retrieving, updating, and deleting tasks.

## Features

- Task management (CRUD operations)
- Kafka integration for messaging
- Multi-database support (main and backup)
- Exception handling for common task-related errors

## Installation

1. Clone the repository:

```bash
    git clone https://github.com/yourusername/task-manager.git
    cd task-manager
```

2. Build the project using Maven:

```bash
    mvn clean install
```

3. Run the application:

```bash
    mvn spring-boot:run
```
4. Run this script to create table in H2/PostgreSQL:
```sql
   CREATE TABLE IF NOT EXISTS task
(
    id           SERIAL PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    created_date TIMESTAMP    NOT NULL,
    due_date     TIMESTAMP,
    completed    BOOLEAN      NOT NULL DEFAULT FALSE,
    priority     VARCHAR(50)  NOT NULL
);
```
## Api Endpoints

You can view the existing and available Endpoints here after launching the project: http://localhost:8080/swagger-ui/index.html#/
![Alt text](/screenshots_for_readme/swagger.png?raw=true "Swagger Open Api")

## Confirmation of some task criteria

This project supports monitoring newly created tasks using Kafka. The screenshot below shows the json data of the created tasks:
![Alt text](screenshots_for_readme/Kafka_monitoring.png?raw=true "Kafka monitoring result")

This project supports storing data in both H2 (the main database) and PostgreSQL (the backup database). The screenshots below show the stored data:

H2:
![Alt text](screenshots_for_readme/Dummy_data_from_H2.png?raw=true "H2 database")

PostgreSQL:
![Alt text](screenshots_for_readme/Dummy_data_from_PostgreSQL_db_backup.png?raw=true "PostgreSQL database")

## Contributing

Contributions are welcome! Please fork this repository and submit a pull request for any enhancements or bug fixes.

## License

This project is licensed under the MIT License.

This `README.md` provides a comprehensive overview of the project, including configuration details, usage instructions, and API endpoints. It also includes sections for contributing and license information.
