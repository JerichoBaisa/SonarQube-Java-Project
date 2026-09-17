# DevOps Shack ProjectOps Studio
##### Testing Push 6
A fully functional Java 21 project operations application built for **DevOps Shack** with Spring Boot, Maven, Thymeleaf, Spring Data JPA and a persistent H2 database.

## Main functionality

- Create, read, update and delete projects
- Search across project name, summary, owner and tags
- Filter by status, category, priority and favorite state
- Sort by recent update, creation date, name, progress or priority
- Pagination
- Dashboard metrics
- Progress tracking with quick update control
- Automatic completion when progress reaches 100%
- Quick status progression
- Favorites
- Duplicate project
- Archive project
- CSV export
- Repository URL and project tags
- Start and target dates
- Persistent H2 database
- H2 web console
- REST API
- Responsive UI
- Light/dark theme preference saved in the browser
- Seed data on the first run

## Technology

- JDK 21
- Spring Boot 3.5.16
- Maven
- Spring MVC
- Thymeleaf
- Spring Data JPA / Hibernate
- Bean Validation
- H2 Database
- HTML/CSS/JavaScript

## Database

The project uses a **file-based H2 database**, so the data survives application restarts.

```properties
spring.datasource.url=jdbc:h2:file:./data/devopsshack-projectops;AUTO_SERVER=TRUE
spring.datasource.username=devopsshack
spring.datasource.password=devopsshack
```

The database files are created under:

```text
./data/
```

Typically H2 creates a file similar to:

```text
data/devopsshack-projectops.mv.db
```

H2 Console:

```text
http://localhost:8080/h2-console
```

Use this exact JDBC URL in the H2 console:

```text
jdbc:h2:file:./data/devopsshack-projectops;AUTO_SERVER=TRUE
```

Username:

```text
devopsshack
```

Password:

```text
devopsshack
```

## Run locally

Requirements:

```bash
java -version
mvn -version
```

Start directly for development:

```bash
mvn spring-boot:run
```

or:

```bash
./run.sh
```

Then open:

```text
http://localhost:8080
```

## Build executable JAR

```bash
mvn clean package
```

The JAR is generated as:

```text
target/devops-shack-projectops.jar
```

Run it:

```bash
java -jar target/devops-shack-projectops.jar
```

## REST API

Base URL:

```text
http://localhost:8080/api/projects
```

Examples:

```bash
curl http://localhost:8080/api/projects
```

Create:

```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{
    "name":"GitOps Platform",
    "summary":"Argo CD based continuous delivery platform",
    "category":"PLATFORM",
    "status":"PLANNED",
    "priority":"HIGH",
    "environment":"MULTI",
    "owner":"DevOps Shack",
    "progress":10,
    "favorite":true,
    "tags":"argocd,gitops,kubernetes"
  }'
```

Delete:

```bash
curl -X DELETE http://localhost:8080/api/projects/1
```

Update progress:

```bash
curl -X PATCH "http://localhost:8080/api/projects/1/progress?value=85"
```

## Project structure

```text
src/main/java/com/devopsshack/projectops/
├── controller/
├── model/
├── repository/
├── service/
├── config/
└── DevOpsShackProjectOpsApplication.java

src/main/resources/
├── templates/
├── static/css/
├── static/js/
└── application.properties
```
