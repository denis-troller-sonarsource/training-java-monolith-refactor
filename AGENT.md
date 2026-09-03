# Agent Instructions for big-bad-monolith

## Status: migrated to a modular monolith

This started as a legacy single-WAR JSP monolith full of intentional anti-patterns. It has since
been migrated to a **modular monolith** (see `README.md` for the full architecture). The
anti-patterns described later in this file are **historical** — the starting point the migration
removed — kept here as a record of what was fixed. The current codebase is the target of that work;
do not reintroduce the old patterns.

## Application Architecture (current)

- **Modules**: `:common`, `:users`, `:customers`, `:catalog`, `:timesheet`, `:billing`, `:app`
  (multi-module Gradle build). Each domain context is `api` / `service` / `repository`; cross-module
  code depends only on another context's `api` package. Boundaries are enforced by a SonarQube
  intended-architecture model (`sonar context architecture get-intended`) — zero violations.
- **Presentation**: `@WebServlet` MVC controllers + scriptlet-free JSTL/EL JSP views under
  `app/src/main/webapp/WEB-INF/views/`. Output is HTML-escaped; mutations use Post/Redirect/Get.
- **REST**: JAX-RS resources under `/api` in `app/.../app/rest`.
- **Business logic**: in the context/billing services (not in views).
- **Data access**: `Jdbc*Repository` classes over `common.JdbcSupport`; failures surface as
  `common.DataAccessException`.
- **DI**: CDI (`@ApplicationScoped` services, `@Inject` everywhere; no ServiceLoader bridges).
- **Dates**: `java.time` (no Joda-Time). **Database**: Apache Derby.
- **Tests**: JUnit 5 + AssertJ against an in-memory Derby harness (`:common` test fixtures), plus
  forked-JVM integration tests for container-only paths. Coverage via JaCoCo → SonarQube.

### Working in this codebase
- Put cross-context types in a context's `api` package; never import another context's
  `service`/`repository`. New repositories use `common.JdbcSupport`.
- After changing the web layer, smoke-test on Liberty (`./gradlew :app:libertyStart`, curl the pages
  and `/api/*`, check `app/build/wlp/usr/servers/defaultServer/logs/messages.log`), because
  servlet/JSP/CDI wiring is not exercised by unit tests.

## Historical anti-patterns (removed by the migration)

The original application intentionally contained **legacy anti-patterns** and **security
vulnerabilities** for educational purposes. **DO NOT reintroduce these** — they have all been fixed:

- **Presentation Layer**: JSP files with extensive Java scriptlets (removed → MVC + JSTL/EL)
- **Business Logic**: mixed into JSP pages (removed → services)
- **Data Access**: DAO classes with inconsistent error handling (→ repositories + DataAccessException)
- **No REST APIs** (→ JAX-RS added)
- **No Tests** (→ comprehensive JUnit 5 suite)

## Build/Test Commands
- **Build**: `./gradlew build` (compiles all modules + runs the JUnit 5 suite)
- **Clean build**: `./gradlew clean build`
- **Test only**: `./gradlew test`
- **Single test**: `./gradlew :<module>:test --tests "ClassName.methodName"`
- **Compile only**: `./gradlew compileJava`
- **Generate WAR**: `./gradlew war`

## Liberty Server Commands
- **Start Liberty dev mode**: `./liberty-dev.sh` (Linux/macOS) or `liberty-dev.bat` (Windows)
- **Start Liberty server**: `./gradlew libertyStart`
- **Stop Liberty server**: `./gradlew libertyStop`
- **Deploy to Liberty**: `./gradlew libertyDeploy`
- **Check server status**: `./gradlew libertyStatus`



## Application URLs (when running)
- **Main Application**: http://localhost:9080/big-bad-monolith/
- **Customer Management**: http://localhost:9080/big-bad-monolith/customers.jsp
- **Hours Logging**: http://localhost:9080/big-bad-monolith/hours.jsp
- **Reports**: http://localhost:9080/big-bad-monolith/reports.jsp
- **Categories**: http://localhost:9080/big-bad-monolith/categories.jsp
- **Users**: http://localhost:9080/big-bad-monolith/users.jsp

## Current Legacy Code Patterns (Intentional Anti-Patterns)

### JSP Layer Issues
- **Scriptlet Hell**: Hundreds of lines of Java code in JSP files
- **Direct Database Access**: JDBC connections opened in presentation layer
- **Business Logic in JSPs**: Calculations and validations in presentation
- **SQL Injection Vulnerabilities**: String concatenation in database queries
- **Resource Leaks**: Database connections not properly managed
- **No Input Validation**: Raw form parameters used directly

### DAO Layer Issues
- **Inconsistent Null Handling**: Inconsistent null checking across DAOs
- **Mixed Error Handling**: SQLException vs RuntimeException inconsistency
- **No Transaction Management**: Auto-commit mode for all operations

### Service Layer Issues
- **Tight Coupling**: Direct instantiation instead of dependency injection
- **Mixed Responsibilities**: Utility methods mixed with business logic

### Utility Classes Issues
- **Legacy Date/Time**: Uses deprecated Joda-Time instead of java.time
- **Thread Safety Issues**: Non-thread-safe implementations
- **Magic Numbers**: Hardcoded values without constants

## File Structure (current, multi-module)
```
settings.gradle / build.gradle       # multi-project build; root holds shared config + the sonar block
common/    src/main/java/.../common/         # ConnectionManager, LibertyConnectionManager, JdbcSupport,
           src/testFixtures/java/.../testsupport/   #   DataAccessException, DateTimeUtils; InMemoryDatabase harness
users/     src/main/java/.../users/{api,service,repository}/
customers/ src/main/java/.../customers/{api,service,repository}/
catalog/   src/main/java/.../catalog/{api,service,repository}/
timesheet/ src/main/java/.../timesheet/{api,service,repository}/
billing/   src/main/java/.../billing/{api,service,repository}/
app/       src/main/java/.../app/rest/       # JAX-RS resources + RestApplication
           src/main/java/.../app/web/        # @WebServlet MVC controllers + ViewSupport
           src/main/webapp/WEB-INF/views/    # scriptlet-free JSTL/EL JSP views
           src/main/java/.../StartupListener, service/DataInitializationService
           src/main/liberty/config/server.xml
```

## Migration history (completed)

The refactor was delivered as small, individually SonarQube-gated PRs; these objectives are DONE:
1. Characterization + unit test safety net (was: no tests)
2. Fixed the security issues: SQL-injection-prone patterns, resource leaks, stored XSS in the hours
   page, and the destructive customer-delete GET (now POST)
3. Consistent error handling via `DataAccessException` (was: inconsistent null/SQLException handling)
4. Removed the thread-unsafe `SimpleDateFormat`; migrated Joda-Time → `java.time`
5. Extracted business logic out of JSPs into services; proper MVC + JAX-RS; CDI dependency injection
6. Enforced module boundaries via a SonarQube intended-architecture model

## Development Guidelines

### Testing Strategy
- **Unit/characterization tests** run against an in-memory Derby harness (`:common` test fixtures).
- **Integration tests**: forked-JVM source sets for container-only paths (JNDI, servlet lifecycle).
- Keep new-code coverage ≥ 80% and duplication ≤ 3% (the SonarQube quality gate enforces this per PR).
- Smoke-test the web layer on Liberty after web changes — unit tests don't exercise JSP/servlet/CDI wiring.

### Code Style (For New/Refactored Code)
- **Java 17** with modern features
- **Package structure**: `com.sourcegraph.demo.bigbadmonolith`
- **Testing**: JUnit 5 framework (`org.junit.jupiter.*`)
- **Date/Time**: Use `java.time.*` not Joda-Time
- **Database**: Keep DAO pattern but add proper error handling
- **REST APIs**: When adding, use JAX-RS annotations
- **Validation**: Add proper input validation and sanitization
- **Error Handling**: Consistent exception handling strategies
- **Dependencies**: Add to `build.gradle`
- **Resources**: Keep web resources in `src/main/webapp/`

## Useful Sourcegraph Searches for Training

### Finding Anti-Patterns
```bash
# SQL injection vulnerabilities
content:"executeUpdate.*\\+" file:.jsp

# Null pointer vulnerabilities  
content:"\\w+\\.get\\w+\\(" -content:"null.*check" lang:java

# Resource leaks
content:"getConnection" -content:"try.*resources" file:.jsp

# Thread safety issues
content:"static.*SimpleDateFormat" lang:java

# Business logic in JSPs
content:"while.*rs\\.next" file:.jsp

# Joda-Time usage
content:"import org.joda.time" lang:java
```

### Tracking Refactoring Progress
```bash
# After adding null safety
content:"throw new IllegalArgumentException" lang:java

# After adding tests
file:Test.java content:"@Test"

# After modernizing dates
content:"java.time.Local" lang:java

# After adding REST APIs
content:"@Path" lang:java
```

## Database Information
- **Type**: Apache Derby (embedded)
- **Location**: `./data/bigbadmonolith` directory
- **Schema**: Auto-created by `ConnectionManager.java`
- **Tables**: customers, users, billing_categories, billable_hours
- **Test Data**: Created by `DataInitializationService.java`

## Common Issues During Development

### Build Issues
- Run `./gradlew clean build` if encountering compilation errors
- JSP compilation errors may not show clearly - check Liberty logs

### Database Issues
- Delete `./data/` directory to reset database
- Derby sometimes locks - restart Liberty server if needed

### Liberty Server Issues
- Check `logs/messages.log` for detailed error information
- Use `./gradlew libertyStop` then `./gradlew libertyStart` to restart

Remember: This is intentionally bad code for educational purposes!
