# Todo App

Spring Boot todo manager with REST and MCP APIs.

## Run

Choose the profile at runtime. Both profiles connect to Oracle XE by default;
set the database password before starting:

```powershell
$env:DB_PASSWORD = "your_database_password"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

The app listens on port `9090` with the `/todo` context path. The todo UI is at
`http://localhost:9090/todo/`; the REST API is under
`http://localhost:9090/todo/api/todos`.

## Profiles

The `dev` profile uses `spring.jpa.hibernate.ddl-auto=update`. The `qa` profile
uses `validate` so it checks the existing schema without modifying it. Both use
`localhost:1521/xe` and `c##app_user` by default. URL and username can be
overridden using environment variables:

```powershell
$env:DB_URL = "jdbc:oracle:thin:@localhost:1521/xe"
$env:DB_USERNAME = "your_database_username"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=qa"
```

The QA profile validates against the existing schema and does not modify it.
Keep environment-specific credentials out of source control.
You can also select a profile when running the packaged application with
`java -jar target/todo-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev`
(replace `dev` with `qa` as needed). No profile is selected automatically.

## MCP server

The Spring AI MCP server uses the Streamable HTTP transport. Connect an MCP
client to:

```text
http://localhost:9090/todo/mcp
```

It exposes `listTodos`, `getTodo`, `createTodo`, `updateTodo`,
`setTodoCompletion`, and `deleteTodo`. MCP tools and REST endpoints share the
same `TodoService` and database.
