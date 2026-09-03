# Big Bad Monolith → Modular Monolith

A Jakarta EE billing / time-tracking platform: users log billable hours for customers under
billing categories (each with an hourly rate), and the system produces bills and revenue reports.

Originally a single-WAR JSP/servlet monolith full of intentional anti-patterns, it has been
migrated into a **modular monolith** — a multi-module Gradle build with strict, SonarQube-enforced
boundaries between bounded contexts, dependency injection via CDI, a JAX-RS REST API, and a
scriptlet-free JSP/servlet MVC web layer. It still deploys as one WAR on WebSphere Liberty.

## Module architecture

```
:common      DB connection infrastructure (JDBC via JdbcSupport / LibertyConnectionManager),
             shared DataAccessException, time helpers, schema DDL. Depends on nothing.
:users       User bounded context
:customers   Customer bounded context
:catalog     BillingCategory bounded context
:timesheet   BillableHour bounded context   (depends on users/customers/catalog APIs)
:billing     Billing + reporting            (composes the four contexts via their APIs)
:app         Liberty WAR: JAX-RS resources, @WebServlet controllers, JSP/JSTL views, CDI wiring
```

Each domain module is layered internally:

```
<context>/api          public surface: model + <Ctx>Service & <Ctx>Repository interfaces  (the ONLY cross-module import)
<context>/service      Default<Ctx>Service  (@ApplicationScoped, @Inject the repository)
<context>/repository   Jdbc<Ctx>Repository  (implements the repository over common.JdbcSupport)
```

**Boundary rule (enforced):** a module may depend only on another module's `api` package — never its
`service`/`repository` internals; nothing depends on `:app`. This is encoded as a SonarQube
*intended architecture* model and verified against the actual dependency graph
(`sonar context architecture get-current` vs `get-intended`) — currently **zero violations**.

## Domain model

- **User** `(id, email, name)` — employees who log hours
- **Customer** `(id, name, email, address, createdAt)` — companies that are billed
- **BillingCategory** `(id, name, description, hourlyRate)` — e.g. Development, Consulting, Support
- **BillableHour** `(id, customerId, userId, categoryId, hours, note, dateLogged, createdAt)`

Dates use `java.time` (`LocalDate`/`Instant`); money and hours use `BigDecimal`.

### Database schema (Apache Derby)
```sql
users               (id, email, name)
customers           (id, name, email, address, created_at)
billing_categories  (id, name, description, hourly_rate)
billable_hours      (id, customer_id, user_id, category_id, hours, note, date_logged, created_at)
```

## Web & REST

- **MVC web UI** at `/big-bad-monolith/` — `@WebServlet` controllers (CDI-injected services) forward
  to JSTL/EL JSP views under `WEB-INF/views/`. No scriptlets; all output is HTML-escaped; mutations
  use Post/Redirect/Get. Pages: `/dashboard`, `/customers`, `/users`, `/categories`, `/hours`, `/reports`.
- **REST API** under `/big-bad-monolith/api/` (JAX-RS): `customers`, `users`, `categories`, `hours`,
  `reports` (`/customer/{id}`, `/monthly?year=&month=`, `/revenue/by-customer`, `/revenue/by-category`),
  and `bills/{customerId}`.

## Build & test

```bash
./gradlew clean build      # compile all modules + run the full test suite (JUnit 5 + AssertJ)
./gradlew test             # unit tests only
./gradlew :app:war         # produce big-bad-monolith-1.0-SNAPSHOT.war
```

Tests run against an in-memory Derby database via a shared `:common` test-fixtures harness
(`InMemoryDatabase`), so the real SQL is exercised without a container. Coverage is reported to
SonarQube via JaCoCo. Container-only paths (JNDI, servlet lifecycle) are covered by dedicated
forked-JVM integration-test source sets.

## Run on WebSphere Liberty

```bash
./gradlew :app:libertyStart     # start the server (installs the Derby driver into the server lib)
# app:            http://localhost:9080/big-bad-monolith/         (redirects to /dashboard)
# REST:           http://localhost:9080/big-bad-monolith/api/customers
./gradlew :app:libertyStop
```

Dev mode with auto-reload: `./liberty-dev.sh` (Linux/macOS) or `liberty-dev.bat` (Windows).

Liberty serves the app via a JNDI `jdbc/DefaultDataSource` (embedded Derby); the Derby JARs are
installed into the server's `derby/` library by the `:app:installDerbyLib` task. Outside a container
(local dev / tests) the DAOs fall back to an embedded `ConnectionManager` automatically.

Sample data (2 users, 3 customers, 3 categories, several billable hours) is seeded on first startup
by `DataInitializationService`.

## Migration history

The refactor landed as a sequence of small, individually green PRs, each gated on the SonarQube
quality gate: characterization test net → multi-module skeleton → carve `:common` → carve the four
contexts → carve `:billing` (+ Joda-Time → `java.time`, + extract the reports out of raw JDBC and fix
a month-end bug) → CDI + JAX-RS + MVC web layer → enforce architecture + docs. See the git history
and `AGENT.md` for details.
