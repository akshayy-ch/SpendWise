# SpendWise

SpendWise is a full-stack personal finance management application for
tracking expenses, income, wallets, budgets, and shared group expenses.

**Live demo:** https://spendwise-ten-chi.vercel.app/\
**Repository:** https://github.com/akshayy-ch/SpendWise

## Features

-   JWT-based registration and authentication
-   Wallet management and balance tracking
-   Expense and income tracking
-   System and custom categories
-   Overall and category budgets
-   Groups, shared expenses, and settlements
-   Dashboard analytics for spending and outstanding balances
-   Pagination, sorting, filtering, and expense search
-   In-app notifications
-   Role-protected administrative endpoints
-   OpenAPI/Swagger documentation
-   Database schema versioning with Flyway

Expenses use status/history semantics rather than physical deletion.
Financial changes are handled by the backend to help preserve balance
consistency.

## Tech Stack

**Backend** - Java 17 - Spring Boot 3.5.4 - Spring Web, Spring Data JPA,
Hibernate - Spring Security and JWT - PostgreSQL - Flyway -
springdoc-openapi / Swagger UI - Spring Boot Actuator

**Frontend** - React 19 - TypeScript - Vite - React Router - Axios

**Deployment and tooling** - Docker and Docker Compose - Nginx - Vercel
for application deployment - Neon for hosted PostgreSQL

## Architecture

The React frontend communicates with the Spring Boot REST API. The
backend handles authentication, authorization, validation, financial
calculations, and transactional updates. PostgreSQL stores application
data, and Flyway manages schema migrations.

For local Docker development, Docker Compose can run the frontend,
backend, and PostgreSQL services together.

## Run Locally

### Prerequisites

-   Java 17
-   Node.js and npm
-   PostgreSQL, or Docker with Docker Compose

### Option 1: Docker Compose

Configure the environment variables required by the Compose setup, then
run this command from the repository root:

``` bash
docker compose up --build
```

The local Compose configuration serves the frontend through Nginx and
routes `/api/` requests to the backend. Refer to `docker-compose.yml`
and the Dockerfiles for configured services and ports.

### Option 2: Run the backend and frontend separately

1.  Configure a PostgreSQL database and the backend environment
    variables listed below.

2.  From the repository root, start the backend.

    **Windows PowerShell**

    ``` powershell
    .\mvnw.cmd spring-boot:run
    ```

    **macOS/Linux**

    ``` bash
    ./mvnw spring-boot:run
    ```

3.  In a separate terminal, start the frontend:

    ``` bash
    cd frontend
    npm install
    npm run dev
    ```

The Vite development configuration proxies `/api` requests to
`http://localhost:8080`.

> Local setup may require adjusting database settings and the frontend
> proxy to match your environment.

## Environment Variables

Configure secrets and environment-specific settings outside source
control. The backend may require:

  -----------------------------------------------------------------------
  Variable                            Purpose
  ----------------------------------- -----------------------------------
  `DB_URL`                            PostgreSQL JDBC connection URL

  `DB_USERNAME`                       Database username

  `DB_PASSWORD`                       Database password

  `JWT_SECRET`                        Secret used for JWT signing

  `SPRING_PROFILES_ACTIVE`            Active Spring profile; use `prod`
                                      for production

  `APP_VERIFICATION_BASE_URL`         Base URL used by
                                      account-verification flows

  `MAIL_HOST`                         Mail server host

  `MAIL_PORT`                         Mail server port

  `MAILTRAP_USERNAME`                 Mail provider username, if
                                      configured

  `MAILTRAP_PASSWORD`                 Mail provider password, if
                                      configured
  -----------------------------------------------------------------------

Never commit `.env` files, database credentials, JWT secrets, mail
credentials, access tokens, or credential-bearing connection strings.

## Database Migrations

Flyway manages the database schema:

-   `V1__initial_schema.sql` creates the initial schema, constraints,
    indexes, and triggers.
-   `V2__seed_system_categories.sql` seeds the system categories.

The application uses Hibernate schema validation rather than relying on
Hibernate to create or update the production schema.

## API Documentation and Health

OpenAPI/Swagger UI is integrated for API exploration. The backend also
exposes an Actuator health endpoint at `/actuator/health`; whether it is
reachable publicly depends on deployment routing.

## Testing and Build

Run backend tests from the repository root.

**Windows PowerShell**

``` powershell
.\mvnw.cmd test
```

**macOS/Linux**

``` bash
./mvnw test
```

Build the frontend:

``` bash
cd frontend
npm ci
npm run build
```

## Demo Notes

The deployed demo currently auto-verifies new registrations and bypasses
verification email sending to reduce friction during demonstrations.
This is demo-specific behavior and should be replaced with genuine email
verification before operating the application as a production service.

The hosted demo may sometimes experience slower API responses. The cause
has not been confirmed. Avoid repeatedly resubmitting a financial action
when a request appears delayed; verify whether the original action
succeeded first.

Use the demo for evaluation and portfolio demonstrations rather than
storing sensitive real-world financial records.

## Project Status

The current project scope is complete and deployed. The repository
contains the backend, frontend, database migrations, container
configuration, and deployment setup.

## License

No license has been specified. All rights are reserved by default unless
a license is added to the repository.
