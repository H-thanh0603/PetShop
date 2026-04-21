# DB Password via Environment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the app read the MySQL password from an environment variable or a one-time startup prompt, so the database can work without hardcoding credentials.

**Architecture:** Keep `db.properties` as the non-secret source of host/port/name/config, while `DBProperties.password()` resolves secrets from system property or environment variables first. Update the Windows start script to prompt once and export the password into `PETSHOP_DB_PASSWORD` before launching Tomcat.

**Tech Stack:** Java 17, JDBC, Windows batch, Maven/Tomcat.

---

### Task 1: Resolve DB password from environment and system property

**Files:**
- Modify: `src/main/java/DAO/DBProperties.java`

- [ ] **Step 1: Inspect the current password lookup**

```java
public static String password() {
    return prop.getProperty("db.password");
}
```

- [ ] **Step 2: Implement layered password resolution**

```java
public static String password() {
    String override = System.getProperty("petshop.db.password");
    if (override == null || override.isBlank()) {
        override = System.getenv("PETSHOP_DB_PASSWORD");
    }
    if (override == null || override.isBlank()) {
        override = System.getenv("MYSQL_PASSWORD");
    }
    if (override != null && !override.isBlank()) {
        return override;
    }
    return prop.getProperty("db.password", "");
}
```

- [ ] **Step 3: Compile the project**

Run: `mvn -q -DskipTests compile`

Expected: exit code `0`

### Task 2: Prompt once in the launcher and export the secret

**Files:**
- Modify: `Start.bat`

- [ ] **Step 1: Inspect the current launcher flow**

```bat
set "PROJECT_ROOT=d:\PetShop2\PetShop"
set "WAR_FILE=%PROJECT_ROOT%\target\PetShop.war"
```

- [ ] **Step 2: Add a one-time prompt and export the password**

```bat
if defined PETSHOP_DB_PASSWORD (
    echo [INFO] Using PETSHOP_DB_PASSWORD from environment.
) else (
    echo Enter MySQL password for the PetShop app:
    set /p PETSHOP_DB_PASSWORD=
)
set "PETSHOP_DB_PASSWORD=%PETSHOP_DB_PASSWORD%"
```

- [ ] **Step 3: Keep the rest of the launcher unchanged**

```bat
call mvn clean package -DskipTests
copy "%WAR_FILE%" "%CATALINA_HOME%\webapps\"
start startup.bat
```

- [ ] **Step 4: Sanity-check the batch file manually**

Run: `Start.bat`

Expected: the console prompts for the MySQL password once, then the app starts with the entered value available to Tomcat.

### Task 3: Document the new setup

**Files:**
- Modify: `src/main/resources/db.properties`
- Modify: `README.md`

- [ ] **Step 1: Clarify that `db.properties` is non-secret**

```properties
db.host = localhost
db.port = 3306
db.username = root
db.password =
db.dbname = petvaccine
db.option = useUnicode=true&characterEncoding=utf-8
```

- [ ] **Step 2: Add a short usage note**

```md
Set `PETSHOP_DB_PASSWORD` before running `Start.bat`, or enter the password when prompted.
```

- [ ] **Step 3: Re-run compile**

Run: `mvn -q -DskipTests compile`

Expected: exit code `0`
