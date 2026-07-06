# Issue Tracker API

A robust, enterprise-grade project management and issue tracking REST API built with **Java (Spring Boot)**, **Spring Security 6.x**, **JPA/Hibernate**, and **PostgreSQL**. Designed to handle multi-tenant project spaces with hierarchical issues, custom state validation transitions, and role-based permissions.

## 🚀 Live Production URL
The API is deployed and running on Railway at:
👉 **[https://issue-tracker-production-c105.up.railway.app](https://issue-tracker-production-c105.up.railway.app)**

---

## 🛠 Tech Stack
*   **Backend:** Java 17 / Spring Boot 3.2.4 (Spring Web, Spring Security, Spring Data JPA)
*   **Security:** JSON Web Tokens (JJWT 0.12.x) & BCrypt password hashing
*   **Database:** PostgreSQL (with Railway volume persistence)
*   **Build Tool:** Maven
*   **Project Config:** Lombok (version 1.18.46 mapped to javac annotation processor)

---

## 💎 Core Features
1.  **Stateless JWT Security & Authentication:** Complete sign-up, sign-in, and `/auth/me` endpoints using secure cryptographic signatures.
2.  **Project-Scoped Role-Based Access Control (RBAC):** Users hold distinct member roles (`ADMIN`, `MEMBER`, `VIEWER`) scoped uniquely to each project. Write permissions are guarded dynamically by project memberships.
3.  **Strict State Machine Validator:** Issues follow a formal workflow path to prevent illegal transitions (e.g., from `BACKLOG` directly to `DONE`). Allowed flows:
    *   `BACKLOG` $\rightarrow$ `TODO`
    *   `TODO` $\rightleftharpoons$ `IN_PROGRESS` or `BACKLOG`
    *   `IN_PROGRESS` $\rightleftharpoons$ `IN_REVIEW` or `BACKLOG`
    *   `IN_REVIEW` $\rightleftharpoons$ `DONE` or `IN_PROGRESS`
    *   `DONE` $\rightarrow$ `IN_PROGRESS`
    *   `CANCELLED` $\rightarrow$ `BACKLOG`
4.  **Hierarchical Issues (Sub-tasks):** Direct self-referencing relationship mapping sub-tasks to parent tasks.
5.  **Dynamic Filtering & Pagination:** Extended search queries implemented via JPA Specifications/Hibernate Criteria builders.
6.  **Statistical Aggregations:** Computes dynamic metrics (issues grouped by status, priority, and assignees) for a project.

---

## 📊 Database Schema Relationships
```
                  ┌──────────────┐
                  │    users     │
                  └──────┬───────┘
                         │
        ┌────────────────┼────────────────┐
        │ 1              │ 1              │ 1
        ▼ *              ▼ *              ▼ *
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│   projects   │◄──┤project_members│   │issue_comments│
└──────┬───────┘1  └──────────────┘   └──────┬───────┘
       │                                     │ *
       │ 1                                   ▼ 1
       ▼ *                            ┌──────────────┐
┌──────────────┐                     │    issues    │
│ issue_labels │◄─────────────────────└──────┬───────┘
└──────────────┘ *                         * │
                                             ▼ *
                                      ┌──────────────┐
                                      │issue_label_map│
                                      └──────────────┘
```

---

## ⚙️ Local Setup and Run

### Prerequisites
*   Java 17 (or newer JDK installed)
*   PostgreSQL running locally

### Configuration
Update the local database credentials inside `src/main/resources/application.yml` or set the environment variables:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/issue_tracker_db
    username: postgres
    password: yourpassword
```

### Build & Run
1.  **Clone and Compile the Project:**
    ```bash
    mvn clean package -DskipTests
    ```
2.  **Start the Spring Boot Application:**
    ```bash
    java -jar target/issue-tracker-0.0.1-SNAPSHOT.jar
    ```
    *   *The server will start on port `8080` by default. Default test users (`admin`, `dev1`, `dev2`) are automatically bootstrapped on start.*

---

## 🧪 E2E Verification Testing
We have provided an automated integration verification shell script at the root directory.

To run tests locally:
```bash
./verify.sh
```

To run tests against the live production server:
```bash
BASE_URL="https://issue-tracker-production-c105.up.railway.app" ./verify.sh
```

The script performs the following integration sequence:
1.  Registers a new verification user.
2.  Logs in to extract a JWT authentication token.
3.  Creates a project space and sets a unique project key (`ALPHA`).
4.  Enrolls a collaborator (`dev1`) as a project member.
5.  Creates an issue in the project.
6.  Transitions the issue status (`TODO` $\rightarrow$ `IN_PROGRESS`) verifying the state machine.
7.  Creates and maps a project label to the issue.
8.  Post a collaborator comment to the issue.
9.  Fetches project-wide status, priority, and assignee statistics.
